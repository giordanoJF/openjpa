/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.util.UserException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LockTest {

    // NULL = pc null;
    // UNMANAGED = non gestito (PCState.TRANSIENT);
    // MANAGED_NEW = gestito, non ancora persistito (PNEW);
    // MANAGED_STORED = gestito, presente nello store (PCLEAN/PDIRTY/HOLLOW).
    private enum PcKind { NULL, UNMANAGED, MANAGED_NEW, MANAGED_STORED }

    // KNOWN = una delle costanti dichiarate su LockLevels;
    // UNKNOWN = un int
    // qualunque non tra quelle costanti.
    private enum LevelKind { KNOWN, UNKNOWN }

    // Rappresentanti BVA per timeout, come da 04_CP.tex.
    private enum TimeoutKind { OTHER_NEGATIVE, NEG_ONE, ZERO, POSITIVE }

    // NONE = call assente; ACTION = processArgument ritorna un codice azione (ACT_NONE/ACT_CASCADE/
    // ACT_RUN, equivalenti per design); THROWS = lancia OpenJPAException.
    private enum ProcessArgumentBehavior { NONE, ACTION, THROWS }

    // Rilevante solo quando processArgument == ACTION: quale delle tre costanti (assunte
    // equivalenti dal design) viene effettivamente restituita dal callback.
    private enum ActValue { NONE, CASCADE, RUN }

    private enum ExpectedOutcome {
        EXCEPTION_UNSPECIFIED,   // oracolo "eccezione (tipo non specificato)"
        EXCEPTION_PROPAGATED,    // oracolo "eccezione propagata da lock" (processArgument lancia)
        POSTCONDITION,           // oracolo "getLockLevel(pc) == level (postcondizione)"
        NO_EFFECT                // corretto da osservazione: nessuna eccezione, getLockLevel resta LOCK_NONE
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"pc=null,level=noto,timeout=altro negativo,call=null,pArg=-- (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=altro negativo,call=non null,pArg=azione (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=altro negativo,call=non null,pArg=eccezione (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=-1,call=null,pArg=-- (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.NEG_ONE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=-1,call=non null,pArg=azione (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=-1,call=non null,pArg=eccezione (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=0,call=null,pArg=-- (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.ZERO, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=0,call=non null,pArg=azione (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=0,call=non null,pArg=eccezione (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=positivo,call=null,pArg=-- (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.POSITIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=positivo,call=non null,pArg=azione (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=positivo,call=non null,pArg=eccezione (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=altro negativo,call=null,pArg=-- (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=altro negativo,call=non null,pArg=azione (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=altro negativo,call=non null,pArg=eccezione (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=-1,call=null,pArg=-- (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=-1,call=non null,pArg=azione (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=-1,call=non null,pArg=eccezione (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=0,call=null,pArg=-- (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.ZERO, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=0,call=non null,pArg=azione (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=0,call=non null,pArg=eccezione (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=positivo,call=null,pArg=-- (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=positivo,call=non null,pArg=azione (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=positivo,call=non null,pArg=eccezione (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=altro negativo,call=null,pArg=-- (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=altro negativo,call=non null,pArg=azione (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=altro negativo,call=non null,pArg=eccezione",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=non gestito,level=noto,timeout=-1,call=null,pArg=-- (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.NEG_ONE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=-1,call=non null,pArg=azione (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=-1,call=non null,pArg=eccezione",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=non gestito,level=noto,timeout=0,call=null,pArg=-- (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.ZERO, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=0,call=non null,pArg=azione (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=0,call=non null,pArg=eccezione",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=non gestito,level=noto,timeout=positivo,call=null,pArg=-- (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.POSITIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=positivo,call=non null,pArg=azione (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=positivo,call=non null,pArg=eccezione",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=non gestito,level=non noto,timeout=altro negativo,call=null,pArg=-- (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=altro negativo,call=non null,pArg=azione (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=altro negativo,call=non null,pArg=eccezione",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=non gestito,level=non noto,timeout=-1,call=null,pArg=-- (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=-1,call=non null,pArg=azione (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=-1,call=non null,pArg=eccezione",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=non gestito,level=non noto,timeout=0,call=null,pArg=-- (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.ZERO, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=0,call=non null,pArg=azione (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=0,call=non null,pArg=eccezione",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=non gestito,level=non noto,timeout=positivo,call=null,pArg=-- (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=positivo,call=non null,pArg=azione (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=positivo,call=non null,pArg=eccezione",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito non persistito,level=noto,timeout=altro negativo,call=null,pArg=-- (corretto)",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,level=noto,timeout=altro negativo,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=noto,timeout=altro negativo,call=non null,pArg=eccezione",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito non persistito,level=noto,timeout=-1,call=null,pArg=--",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.NEG_ONE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,level=noto,timeout=-1,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=noto,timeout=-1,call=non null,pArg=eccezione",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito non persistito,level=noto,timeout=0,call=null,pArg=--",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.ZERO, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,level=noto,timeout=0,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=noto,timeout=0,call=non null,pArg=eccezione",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito non persistito,level=noto,timeout=positivo,call=null,pArg=--",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.POSITIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,level=noto,timeout=positivo,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=noto,timeout=positivo,call=non null,pArg=eccezione",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito non persistito,level=non noto,timeout=altro negativo,call=null,pArg=-- (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,level=non noto,timeout=altro negativo,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=non noto,timeout=altro negativo,call=non null,pArg=eccezione",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito non persistito,level=non noto,timeout=-1,call=null,pArg=-- (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,level=non noto,timeout=-1,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=non noto,timeout=-1,call=non null,pArg=eccezione",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito non persistito,level=non noto,timeout=0,call=null,pArg=-- (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.ZERO, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,level=non noto,timeout=0,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=non noto,timeout=0,call=non null,pArg=eccezione",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito non persistito,level=non noto,timeout=positivo,call=null,pArg=-- (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,level=non noto,timeout=positivo,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=non noto,timeout=positivo,call=non null,pArg=eccezione",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito persistito,level=noto,timeout=altro negativo,call=null,pArg=-- (corretto)",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=noto,timeout=altro negativo,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=noto,timeout=altro negativo,call=non null,pArg=eccezione",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito persistito,level=noto,timeout=-1,call=null,pArg=--",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.NEG_ONE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=noto,timeout=-1,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=noto,timeout=-1,call=non null,pArg=eccezione",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito persistito,level=noto,timeout=0,call=null,pArg=--",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.ZERO, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=noto,timeout=0,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=noto,timeout=0,call=non null,pArg=eccezione",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito persistito,level=noto,timeout=positivo,call=null,pArg=--",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.POSITIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=noto,timeout=positivo,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=noto,timeout=positivo,call=non null,pArg=eccezione",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito persistito,level=non noto,timeout=altro negativo,call=null,pArg=-- (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=non noto,timeout=altro negativo,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=non noto,timeout=altro negativo,call=non null,pArg=eccezione",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito persistito,level=non noto,timeout=-1,call=null,pArg=-- (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=non noto,timeout=-1,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=non noto,timeout=-1,call=non null,pArg=eccezione",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito persistito,level=non noto,timeout=0,call=null,pArg=-- (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.ZERO, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=non noto,timeout=0,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=non noto,timeout=0,call=non null,pArg=eccezione",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito persistito,level=non noto,timeout=positivo,call=null,pArg=-- (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=non noto,timeout=positivo,call=non null,pArg=azione (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=non noto,timeout=positivo,call=non null,pArg=eccezione",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=null,level=noto,timeout=altro negativo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=altro negativo,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=-1,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=-1,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=0,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=0,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=positivo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=noto,timeout=positivo,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.NULL, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=altro negativo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=altro negativo,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=-1,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=-1,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=0,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=0,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=positivo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,level=non noto,timeout=positivo,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.NULL, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=altro negativo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=altro negativo,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=-1,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=-1,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=0,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=0,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=positivo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=noto,timeout=positivo,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.UNMANAGED, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=altro negativo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=altro negativo,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=-1,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=-1,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=0,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=0,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=positivo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,level=non noto,timeout=positivo,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.UNMANAGED, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=noto,timeout=altro negativo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=noto,timeout=altro negativo,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,level=noto,timeout=-1,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=noto,timeout=-1,call=non null,pArg=azione (ACT_RUN)",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,level=noto,timeout=0,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=noto,timeout=0,call=non null,pArg=azione (ACT_RUN)",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,level=noto,timeout=positivo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=noto,timeout=positivo,call=non null,pArg=azione (ACT_RUN)",
                PcKind.MANAGED_NEW, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,level=non noto,timeout=altro negativo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=non noto,timeout=altro negativo,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,level=non noto,timeout=-1,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=non noto,timeout=-1,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,level=non noto,timeout=0,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=non noto,timeout=0,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,level=non noto,timeout=positivo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,level=non noto,timeout=positivo,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.MANAGED_NEW, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=noto,timeout=altro negativo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=noto,timeout=altro negativo,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=noto,timeout=-1,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=noto,timeout=-1,call=non null,pArg=azione (ACT_RUN)",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=noto,timeout=0,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=noto,timeout=0,call=non null,pArg=azione (ACT_RUN)",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=noto,timeout=positivo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=noto,timeout=positivo,call=non null,pArg=azione (ACT_RUN)",
                PcKind.MANAGED_STORED, LevelKind.KNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=non noto,timeout=altro negativo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=non noto,timeout=altro negativo,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.OTHER_NEGATIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=non noto,timeout=-1,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=non noto,timeout=-1,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.NEG_ONE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=non noto,timeout=0,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=non noto,timeout=0,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.ZERO, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,level=non noto,timeout=positivo,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,level=non noto,timeout=positivo,call=non null,pArg=azione (ACT_RUN) (corretto)",
                PcKind.MANAGED_STORED, LevelKind.UNKNOWN, TimeoutKind.POSITIVE, true,
                ProcessArgumentBehavior.ACTION, ActValue.RUN, ExpectedOutcome.POSTCONDITION},
        });
    }

    private final PcKind pc;
    private final LevelKind level;
    private final TimeoutKind timeout;
    private final boolean call;
    private final ProcessArgumentBehavior processArgument;
    private final ActValue actValue;
    private final ExpectedOutcome expected;

    private BrokerImpl broker;

    public LockTest(String label, PcKind pc, LevelKind level, TimeoutKind timeout, boolean call,
        ProcessArgumentBehavior processArgument, ActValue actValue, ExpectedOutcome expected) {
        this.pc = pc;
        this.level = level;
        this.timeout = timeout;
        this.call = call;
        this.processArgument = processArgument;
        this.actValue = actValue;
        this.expected = expected;
    }

    @Before
    public void setUp() {
        AbstractBrokerFactory factory = mock(AbstractBrokerFactory.class, RETURNS_DEEP_STUBS);
        StoreManager storeManager = mock(StoreManager.class, RETURNS_DEEP_STUBS);
        DelegatingStoreManager delegatingStoreManager = new DelegatingStoreManager(storeManager) { };

        LockManager lockManager = mock(LockManager.class, RETURNS_DEEP_STUBS);
        doAnswer(invocation -> {
            int lockedLevel = invocation.getArgument(1);
            when(lockManager.getLockLevel(any())).thenReturn(lockedLevel);
            return null;
        }).when(lockManager).lock(any(), anyInt(), anyInt(), any());

        OpenJPAConfiguration conf = mock(OpenJPAConfiguration.class, RETURNS_DEEP_STUBS);
        when(conf.newLockManagerInstance()).thenReturn(lockManager);
        when(factory.getConfiguration()).thenReturn(conf);

        broker = new BrokerImpl();
        broker.initialize(factory, delegatingStoreManager, false, ConnectionRetainModes.CONN_RETAIN_DEMAND, false);
        broker.begin();
    }

    private Object buildPc() {
        switch (pc) {
            case NULL:
                return null;
            case UNMANAGED: {
                PersistenceCapable obj = mock(PersistenceCapable.class);
                when(obj.pcGetStateManager()).thenReturn(null);
                return obj;
            }
            case MANAGED_NEW:
            case MANAGED_STORED: {
                StateManagerImpl sm = mock(StateManagerImpl.class);
                when(sm.isPersistent()).thenReturn(true);
                PersistenceCapable obj = mock(PersistenceCapable.class);
                when(obj.pcGetStateManager()).thenReturn(sm);
                when(obj.pcGetGenericContext()).thenReturn(broker);
                return obj;
            }
            default:
                throw new IllegalStateException();
        }
    }

    private int buildLevel() {
        return level == LevelKind.KNOWN ? LockLevels.LOCK_READ : 9999;
    }

    private int buildTimeout() {
        switch (timeout) {
            case OTHER_NEGATIVE:
                return -2;
            case NEG_ONE:
                return -1;
            case ZERO:
                return 0;
            case POSITIVE:
                return 1;
            default:
                throw new IllegalStateException();
        }
    }

    @Test
    public void testLock() {
        Object pcValue = buildPc();
        int levelValue = buildLevel();
        int timeoutValue = buildTimeout();

        OpCallbacks callback = null;
        RuntimeException processArgumentException = null;
        if (call) {
            callback = mock(OpCallbacks.class);
            switch (processArgument) {
                case ACTION:
                    int act;
                    switch (actValue) {
                        case CASCADE:
                            act = OpCallbacks.ACT_CASCADE;
                            break;
                        case RUN:
                            act = OpCallbacks.ACT_RUN;
                            break;
                        default:
                            act = OpCallbacks.ACT_NONE;
                    }
                    when(callback.processArgument(anyInt(), any(), any())).thenReturn(act);
                    break;
                case THROWS:
                    processArgumentException = new UserException("processArgument: errore simulato");
                    when(callback.processArgument(anyInt(), any(), any())).thenThrow(processArgumentException);
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        if (expected == ExpectedOutcome.EXCEPTION_UNSPECIFIED) {
            try {
                broker.lock(pcValue, levelValue, timeoutValue, callback);
                fail("attesa un'eccezione");
            } catch (Exception e) {
                // eccezione attesa, tipo non specificato dal design
            }
        } else if (expected == ExpectedOutcome.EXCEPTION_PROPAGATED) {
            try {
                broker.lock(pcValue, levelValue, timeoutValue, callback);
                fail("attesa la propagazione dell'eccezione di processArgument");
            } catch (Exception e) {
                assertSame(processArgumentException, e);
            }
        } else if (expected == ExpectedOutcome.POSTCONDITION) {
            broker.lock(pcValue, levelValue, timeoutValue, callback);
            assertEquals(levelValue, broker.getLockLevel(pcValue));
        } else {
            broker.lock(pcValue, levelValue, timeoutValue, callback);
            assertEquals(LockLevels.LOCK_NONE, broker.getLockLevel(pcValue));
        }
    }
}
