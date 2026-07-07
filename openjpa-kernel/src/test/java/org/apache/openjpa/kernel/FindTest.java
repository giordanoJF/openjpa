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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.PCRegistry;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.util.MetaDataException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class FindTest {

    private static final class DummyPersistentClass { }

    // VALID = oggetto conforme al tipo usato come identificatore della classe persistente
    // INVALID = oggetto non-null e non VALID;
    private enum OidKind { NULL, INVALID, VALID }

    // NONE = FindCallbacks assente
    // THROWS = lancia eccezione
    // RETURNS_ID = sostituisce l'oid con un nuovo id
    private enum ProcessArgumentBehavior { NONE, THROWS, RETURNS_ID, RETURNS_NULL }

    // RETURNS_NULL/RETURNS_OBJECT = valore che, per ipotesi di design, determina il valore di ritorno di find.
    private enum ProcessReturnBehavior { NONE, RETURNS_NULL, RETURNS_OBJECT }

    private enum ExpectedOutcome {
        NULL,
        EXCEPTION_UNSPECIFIED,
        EXCEPTION_PROPAGATED,     // oracolo "eccezione propagata da find" (processArgument lancia)
        HOLLOW,                   // oracolo "istanza hollow non-null": PCState.HOLLOW, Javadoc dichiarato
                                   // "Hollow; exists in data store" (istanza con identità nota ma
                                   // campi non ancora caricati)
        NON_NULL,                 // oracolo "istanza non-null"
        CACHED_INSTANCE,          // oracolo "istanza in cache"
        PROCESS_RETURN_INSTANCE   // oracolo "oggetto ritornato da processReturn"
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"oid=null,validate=true,cache=--,store=--,call=null,pArg=--,pRet=-- (corretto)",
                OidKind.NULL, true, null, null, false,
                ProcessArgumentBehavior.NONE, ProcessReturnBehavior.NONE, ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"oid=null,validate=false,cache=--,store=--,call=null,pArg=--,pRet=-- (corretto)",
                OidKind.NULL, false, null, null, false,
                ProcessArgumentBehavior.NONE, ProcessReturnBehavior.NONE, ExpectedOutcome.NULL},
            {"oid=invalido,validate=true,cache=--,store=--,call=null,pArg=--,pRet=-- (corretto)",
                OidKind.INVALID, true, null, null, false,
                ProcessArgumentBehavior.NONE, ProcessReturnBehavior.NONE, ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"oid=invalido,validate=false,cache=--,store=--,call=null,pArg=--,pRet=--",
                OidKind.INVALID, false, null, null, false,
                ProcessArgumentBehavior.NONE, ProcessReturnBehavior.NONE, ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"oid=valido,validate=true,cache=presente,store=presente,call=null,pArg=--,pRet=--",
                OidKind.VALID, true, Boolean.TRUE, Boolean.TRUE, false,
                ProcessArgumentBehavior.NONE, ProcessReturnBehavior.NONE, ExpectedOutcome.NON_NULL},
            {"oid=valido,validate=true,cache=non presente,store=presente,call=null,pArg=--,pRet=--",
                OidKind.VALID, true, Boolean.FALSE, Boolean.TRUE, false,
                ProcessArgumentBehavior.NONE, ProcessReturnBehavior.NONE, ExpectedOutcome.NON_NULL},
            {"oid=valido,validate=true,cache=presente,store=non presente,call=null,pArg=--,pRet=--",
                OidKind.VALID, true, Boolean.TRUE, Boolean.FALSE, false,
                ProcessArgumentBehavior.NONE, ProcessReturnBehavior.NONE, ExpectedOutcome.NULL},
            {"oid=valido,validate=true,cache=non presente,store=non presente,call=null,pArg=--,pRet=--",
                OidKind.VALID, true, Boolean.FALSE, Boolean.FALSE, false,
                ProcessArgumentBehavior.NONE, ProcessReturnBehavior.NONE, ExpectedOutcome.NULL},
            {"oid=valido,validate=false,cache=presente,store=presente,call=null,pArg=--,pRet=--",
                OidKind.VALID, false, Boolean.TRUE, Boolean.TRUE, false,
                ProcessArgumentBehavior.NONE, ProcessReturnBehavior.NONE, ExpectedOutcome.CACHED_INSTANCE},
            {"oid=valido,validate=false,cache=non presente,store=presente,call=null,pArg=--,pRet=--",
                OidKind.VALID, false, Boolean.FALSE, Boolean.TRUE, false,
                ProcessArgumentBehavior.NONE, ProcessReturnBehavior.NONE, ExpectedOutcome.HOLLOW},
            {"oid=valido,validate=false,cache=presente,store=non presente,call=null,pArg=--,pRet=--",
                OidKind.VALID, false, Boolean.TRUE, Boolean.FALSE, false,
                ProcessArgumentBehavior.NONE, ProcessReturnBehavior.NONE, ExpectedOutcome.CACHED_INSTANCE},
            {"oid=valido,validate=false,cache=non presente,store=non presente,call=null,pArg=--,pRet=--",
                OidKind.VALID, false, Boolean.FALSE, Boolean.FALSE, false,
                ProcessArgumentBehavior.NONE, ProcessReturnBehavior.NONE, ExpectedOutcome.HOLLOW},
            {"oid=null,validate=true,cache=presente,store=presente,call=non null,pArg=id,pRet=obj",
                OidKind.NULL, true, Boolean.TRUE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=null,validate=true,cache=presente,store=presente,call=non null,pArg=id,pRet=null",
                OidKind.NULL, true, Boolean.TRUE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=null,validate=true,cache=non presente,store=presente,call=non null,pArg=id,pRet=obj",
                OidKind.NULL, true, Boolean.FALSE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=null,validate=true,cache=non presente,store=presente,call=non null,pArg=id,pRet=null",
                OidKind.NULL, true, Boolean.FALSE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=null,validate=true,cache=presente,store=non presente,call=non null,pArg=id,pRet=obj",
                OidKind.NULL, true, Boolean.TRUE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=null,validate=true,cache=presente,store=non presente,call=non null,pArg=id,pRet=null",
                OidKind.NULL, true, Boolean.TRUE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=null,validate=true,cache=non presente,store=non presente,call=non null,pArg=id,pRet=obj",
                OidKind.NULL, true, Boolean.FALSE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=null,validate=true,cache=non presente,store=non presente,call=non null,pArg=id,pRet=null",
                OidKind.NULL, true, Boolean.FALSE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=null,validate=true,cache=--,store=--,call=non null,pArg=null,pRet=obj (corretto)",
                OidKind.NULL, true, null, null, true,
                ProcessArgumentBehavior.RETURNS_NULL, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"oid=null,validate=true,cache=--,store=--,call=non null,pArg=null,pRet=null (corretto)",
                OidKind.NULL, true, null, null, true,
                ProcessArgumentBehavior.RETURNS_NULL, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"oid=null,validate=true,cache=--,store=--,call=non null,pArg=exc,pRet=obj",
                OidKind.NULL, true, null, null, true,
                ProcessArgumentBehavior.THROWS, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"oid=null,validate=true,cache=--,store=--,call=non null,pArg=exc,pRet=null",
                OidKind.NULL, true, null, null, true,
                ProcessArgumentBehavior.THROWS, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"oid=null,validate=false,cache=presente,store=presente,call=non null,pArg=id,pRet=obj",
                OidKind.NULL, false, Boolean.TRUE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=null,validate=false,cache=presente,store=presente,call=non null,pArg=id,pRet=null (corretto)",
                OidKind.NULL, false, Boolean.TRUE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=null,validate=false,cache=non presente,store=presente,call=non null,pArg=id,pRet=obj",
                OidKind.NULL, false, Boolean.FALSE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=null,validate=false,cache=non presente,store=presente,call=non null,pArg=id,pRet=null (corretto)",
                OidKind.NULL, false, Boolean.FALSE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=null,validate=false,cache=presente,store=non presente,call=non null,pArg=id,pRet=obj",
                OidKind.NULL, false, Boolean.TRUE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=null,validate=false,cache=presente,store=non presente,call=non null,pArg=id,pRet=null (corretto)",
                OidKind.NULL, false, Boolean.TRUE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=null,validate=false,cache=non presente,store=non presente,call=non null,pArg=id,pRet=obj",
                OidKind.NULL, false, Boolean.FALSE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=null,validate=false,cache=non presente,store=non presente,call=non null,pArg=id,pRet=null (corretto)",
                OidKind.NULL, false, Boolean.FALSE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=null,validate=false,cache=--,store=--,call=non null,pArg=null,pRet=obj",
                OidKind.NULL, false, null, null, true,
                ProcessArgumentBehavior.RETURNS_NULL, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=null,validate=false,cache=--,store=--,call=non null,pArg=null,pRet=null (corretto)",
                OidKind.NULL, false, null, null, true,
                ProcessArgumentBehavior.RETURNS_NULL, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=null,validate=false,cache=--,store=--,call=non null,pArg=exc,pRet=obj",
                OidKind.NULL, false, null, null, true,
                ProcessArgumentBehavior.THROWS, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"oid=null,validate=false,cache=--,store=--,call=non null,pArg=exc,pRet=null",
                OidKind.NULL, false, null, null, true,
                ProcessArgumentBehavior.THROWS, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"oid=invalido,validate=true,cache=presente,store=presente,call=non null,pArg=id,pRet=obj",
                OidKind.INVALID, true, Boolean.TRUE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=invalido,validate=true,cache=presente,store=presente,call=non null,pArg=id,pRet=null",
                OidKind.INVALID, true, Boolean.TRUE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=invalido,validate=true,cache=non presente,store=presente,call=non null,pArg=id,pRet=obj",
                OidKind.INVALID, true, Boolean.FALSE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=invalido,validate=true,cache=non presente,store=presente,call=non null,pArg=id,pRet=null",
                OidKind.INVALID, true, Boolean.FALSE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=invalido,validate=true,cache=presente,store=non presente,call=non null,pArg=id,pRet=obj",
                OidKind.INVALID, true, Boolean.TRUE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=invalido,validate=true,cache=presente,store=non presente,call=non null,pArg=id,pRet=null",
                OidKind.INVALID, true, Boolean.TRUE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=invalido,validate=true,cache=non presente,store=non presente,call=non null,pArg=id,pRet=obj",
                OidKind.INVALID, true, Boolean.FALSE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=invalido,validate=true,cache=non presente,store=non presente,call=non null,pArg=id,pRet=null",
                OidKind.INVALID, true, Boolean.FALSE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=invalido,validate=true,cache=--,store=--,call=non null,pArg=null,pRet=obj (corretto)",
                OidKind.INVALID, true, null, null, true,
                ProcessArgumentBehavior.RETURNS_NULL, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"oid=invalido,validate=true,cache=--,store=--,call=non null,pArg=null,pRet=null (corretto)",
                OidKind.INVALID, true, null, null, true,
                ProcessArgumentBehavior.RETURNS_NULL, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"oid=invalido,validate=true,cache=--,store=--,call=non null,pArg=exc,pRet=obj",
                OidKind.INVALID, true, null, null, true,
                ProcessArgumentBehavior.THROWS, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"oid=invalido,validate=true,cache=--,store=--,call=non null,pArg=exc,pRet=null",
                OidKind.INVALID, true, null, null, true,
                ProcessArgumentBehavior.THROWS, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"oid=invalido,validate=false,cache=presente,store=presente,call=non null,pArg=id,pRet=obj",
                OidKind.INVALID, false, Boolean.TRUE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=invalido,validate=false,cache=presente,store=presente,call=non null,pArg=id,pRet=null (corretto)",
                OidKind.INVALID, false, Boolean.TRUE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=invalido,validate=false,cache=non presente,store=presente,call=non null,pArg=id,pRet=obj",
                OidKind.INVALID, false, Boolean.FALSE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=invalido,validate=false,cache=non presente,store=presente,call=non null,pArg=id,pRet=null (corretto)",
                OidKind.INVALID, false, Boolean.FALSE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=invalido,validate=false,cache=presente,store=non presente,call=non null,pArg=id,pRet=obj",
                OidKind.INVALID, false, Boolean.TRUE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=invalido,validate=false,cache=presente,store=non presente,call=non null,pArg=id,pRet=null (corretto)",
                OidKind.INVALID, false, Boolean.TRUE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=invalido,validate=false,cache=non presente,store=non presente,call=non null,pArg=id,pRet=obj",
                OidKind.INVALID, false, Boolean.FALSE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=invalido,validate=false,cache=non presente,store=non presente,call=non null,pArg=id,pRet=null (corretto)",
                OidKind.INVALID, false, Boolean.FALSE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=invalido,validate=false,cache=--,store=--,call=non null,pArg=null,pRet=obj",
                OidKind.INVALID, false, null, null, true,
                ProcessArgumentBehavior.RETURNS_NULL, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=invalido,validate=false,cache=--,store=--,call=non null,pArg=null,pRet=null (corretto)",
                OidKind.INVALID, false, null, null, true,
                ProcessArgumentBehavior.RETURNS_NULL, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=invalido,validate=false,cache=--,store=--,call=non null,pArg=exc,pRet=obj",
                OidKind.INVALID, false, null, null, true,
                ProcessArgumentBehavior.THROWS, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"oid=invalido,validate=false,cache=--,store=--,call=non null,pArg=exc,pRet=null",
                OidKind.INVALID, false, null, null, true,
                ProcessArgumentBehavior.THROWS, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"oid=valido,validate=true,cache=presente,store=presente,call=non null,pArg=id,pRet=obj",
                OidKind.VALID, true, Boolean.TRUE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=valido,validate=true,cache=presente,store=presente,call=non null,pArg=id,pRet=null",
                OidKind.VALID, true, Boolean.TRUE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=valido,validate=true,cache=non presente,store=presente,call=non null,pArg=id,pRet=obj",
                OidKind.VALID, true, Boolean.FALSE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=valido,validate=true,cache=non presente,store=presente,call=non null,pArg=id,pRet=null",
                OidKind.VALID, true, Boolean.FALSE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=valido,validate=true,cache=presente,store=non presente,call=non null,pArg=id,pRet=obj",
                OidKind.VALID, true, Boolean.TRUE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=valido,validate=true,cache=presente,store=non presente,call=non null,pArg=id,pRet=null",
                OidKind.VALID, true, Boolean.TRUE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=valido,validate=true,cache=non presente,store=non presente,call=non null,pArg=id,pRet=obj",
                OidKind.VALID, true, Boolean.FALSE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=valido,validate=true,cache=non presente,store=non presente,call=non null,pArg=id,pRet=null",
                OidKind.VALID, true, Boolean.FALSE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=valido,validate=true,cache=--,store=--,call=non null,pArg=null,pRet=obj (corretto)",
                OidKind.VALID, true, null, null, true,
                ProcessArgumentBehavior.RETURNS_NULL, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"oid=valido,validate=true,cache=--,store=--,call=non null,pArg=null,pRet=null (corretto)",
                OidKind.VALID, true, null, null, true,
                ProcessArgumentBehavior.RETURNS_NULL, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"oid=valido,validate=true,cache=--,store=--,call=non null,pArg=exc,pRet=obj",
                OidKind.VALID, true, null, null, true,
                ProcessArgumentBehavior.THROWS, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"oid=valido,validate=true,cache=--,store=--,call=non null,pArg=exc,pRet=null",
                OidKind.VALID, true, null, null, true,
                ProcessArgumentBehavior.THROWS, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"oid=valido,validate=false,cache=presente,store=presente,call=non null,pArg=id,pRet=obj",
                OidKind.VALID, false, Boolean.TRUE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=valido,validate=false,cache=presente,store=presente,call=non null,pArg=id,pRet=null (corretto)",
                OidKind.VALID, false, Boolean.TRUE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=valido,validate=false,cache=non presente,store=presente,call=non null,pArg=id,pRet=obj",
                OidKind.VALID, false, Boolean.FALSE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=valido,validate=false,cache=non presente,store=presente,call=non null,pArg=id,pRet=null (corretto)",
                OidKind.VALID, false, Boolean.FALSE, Boolean.TRUE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=valido,validate=false,cache=presente,store=non presente,call=non null,pArg=id,pRet=obj",
                OidKind.VALID, false, Boolean.TRUE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=valido,validate=false,cache=presente,store=non presente,call=non null,pArg=id,pRet=null (corretto)",
                OidKind.VALID, false, Boolean.TRUE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=valido,validate=false,cache=non presente,store=non presente,call=non null,pArg=id,pRet=obj",
                OidKind.VALID, false, Boolean.FALSE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=valido,validate=false,cache=non presente,store=non presente,call=non null,pArg=id,pRet=null (corretto)",
                OidKind.VALID, false, Boolean.FALSE, Boolean.FALSE, true,
                ProcessArgumentBehavior.RETURNS_ID, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=valido,validate=false,cache=--,store=--,call=non null,pArg=null,pRet=obj",
                OidKind.VALID, false, null, null, true,
                ProcessArgumentBehavior.RETURNS_NULL, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.PROCESS_RETURN_INSTANCE},
            {"oid=valido,validate=false,cache=--,store=--,call=non null,pArg=null,pRet=null (corretto)",
                OidKind.VALID, false, null, null, true,
                ProcessArgumentBehavior.RETURNS_NULL, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.NULL},
            {"oid=valido,validate=false,cache=--,store=--,call=non null,pArg=exc,pRet=obj",
                OidKind.VALID, false, null, null, true,
                ProcessArgumentBehavior.THROWS, ProcessReturnBehavior.RETURNS_OBJECT, ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"oid=valido,validate=false,cache=--,store=--,call=non null,pArg=exc,pRet=null",
                OidKind.VALID, false, null, null, true,
                ProcessArgumentBehavior.THROWS, ProcessReturnBehavior.RETURNS_NULL, ExpectedOutcome.EXCEPTION_PROPAGATED},
        });
    }

    private final OidKind oid;
    private final boolean validate;
    private final Boolean cache;
    private final Boolean store;
    private final boolean call;
    private final ProcessArgumentBehavior processArgument;
    private final ProcessReturnBehavior processReturn;
    private final ExpectedOutcome expected;

    private BrokerImpl broker;
    private MetaDataRepository repo;
    private StoreManager storeManager;

    public FindTest(String label, OidKind oid, boolean validate, Boolean cache, Boolean store, boolean call,
        ProcessArgumentBehavior processArgument, ProcessReturnBehavior processReturn, ExpectedOutcome expected) {
        this.oid = oid;
        this.validate = validate;
        this.cache = cache;
        this.store = store;
        this.call = call;
        this.processArgument = processArgument;
        this.processReturn = processReturn;
        this.expected = expected;
    }

    @Before
    public void setUp() {
        AbstractBrokerFactory factory = mock(AbstractBrokerFactory.class, RETURNS_DEEP_STUBS);
        storeManager = mock(StoreManager.class, RETURNS_DEEP_STUBS);
        DelegatingStoreManager delegatingStoreManager = new DelegatingStoreManager(storeManager) { };

        repo = mock(MetaDataRepository.class, RETURNS_DEEP_STUBS);

        OpenJPAConfiguration conf = mock(OpenJPAConfiguration.class, RETURNS_DEEP_STUBS);
        when(conf.supportedOptions()).thenReturn(Collections.singleton(OpenJPAConfiguration.OPTION_NONTRANS_READ));
        when(conf.getMetaDataRepositoryInstance()).thenReturn(repo);
        when(factory.getConfiguration()).thenReturn(conf);

        broker = new BrokerImpl();
        broker.initialize(factory, delegatingStoreManager, false, ConnectionRetainModes.CONN_RETAIN_DEMAND, false);
        broker.setNontransactionalRead(true);
    }

    private PersistenceCapable newTrackedPc() {
        PersistenceCapable pc = mock(PersistenceCapable.class);
        when(pc.pcGetGenericContext()).thenReturn(broker);
        doAnswer(invocation -> {
            when(pc.pcGetStateManager()).thenReturn(invocation.getArgument(0));
            return null;
        }).when(pc).pcReplaceStateManager(any());
        return pc;
    }

    private Object buildOid() {
        return oid == OidKind.NULL ? null : new Object();
    }

    private void setupValidOidMocks(Object oidToResolve, Boolean storePresent) {
        PersistenceCapable pcPrototype = mock(PersistenceCapable.class, RETURNS_DEEP_STUBS);
        when(pcPrototype.pcNewInstance(any(), anyBoolean())).thenAnswer(invocation -> newTrackedPc());
        when(pcPrototype.pcNewInstance(any(), any(), anyBoolean())).thenAnswer(invocation -> newTrackedPc());
        PCRegistry.register(DummyPersistentClass.class, new String[0], new Class<?>[0], new byte[0],
            null, "DummyPersistentClass", pcPrototype);

        ClassMetaData meta = mock(ClassMetaData.class, RETURNS_DEEP_STUBS);
        when(meta.getIdentityType()).thenReturn(ClassMetaData.ID_DATASTORE);
        doReturn(DummyPersistentClass.class).when(meta).getDescribedType();
        when(meta.getFields()).thenReturn(new FieldMetaData[] {mock(FieldMetaData.class)});
        when(meta.getPkAndNonPersistentManagedFmdIndexes()).thenReturn(new int[0]);
        when(meta.getPCSubclasses()).thenReturn(new Class<?>[0]);
        when(repo.getMetaData(eq(oidToResolve), any(), anyBoolean())).thenReturn(meta);

        boolean storeHasIt = Boolean.TRUE.equals(storePresent);
        when(storeManager.initialize(any(), any(), any(), any())).thenReturn(storeHasIt);
        when(storeManager.exists(any(), any())).thenReturn(storeHasIt);

        // Solo per validate=true, store=non presente: forza il controllo reale dello store nel ramo
        // "non ancora in cache", senza toccare le altre tuple che dipendono dal comportamento di default.
        if (validate && !storeHasIt) {
            when(broker.getFetchConfiguration().requiresLoad()).thenReturn(true);
        }
    }

    @Test
    public void testFind() {
        Object oidValue = buildOid();
        if (oid == OidKind.INVALID) {
            when(repo.getMetaData(eq(oidValue), any(), anyBoolean()))
                .thenThrow(new MetaDataException("oid non valido: nessuna metadata risolvibile"));
        }
        if (oid == OidKind.VALID) {
            setupValidOidMocks(oidValue, store);
        }

        FindCallbacks callback = null;
        Object processReturnSentinel = null;
        RuntimeException processArgumentException = null;
        if (call) {
            callback = mock(FindCallbacks.class);
            switch (processArgument) {
                case THROWS:
                    processArgumentException = new RuntimeException("processArgument: errore simulato");
                    when(callback.processArgument(any())).thenThrow(processArgumentException);
                    break;
                case RETURNS_NULL:
                    when(callback.processArgument(any())).thenReturn(null);
                    break;
                case RETURNS_ID: {
                    Object resolvedOid = new Object();
                    when(callback.processArgument(any())).thenReturn(resolvedOid);
                    setupValidOidMocks(resolvedOid, store);
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
            switch (processReturn) {
                case RETURNS_NULL:
                    when(callback.processReturn(any(), any())).thenReturn(null);
                    break;
                case RETURNS_OBJECT:
                    processReturnSentinel = new Object();
                    when(callback.processReturn(any(), any())).thenReturn(processReturnSentinel);
                    break;
                default:
                    break;
            }
        }

        Object primed = null;
        if (!call && oid == OidKind.VALID && Boolean.TRUE.equals(cache)) {
            primed = broker.find(oidValue, true, null);
        }

        if (expected == ExpectedOutcome.EXCEPTION_UNSPECIFIED) {
            try {
                broker.find(oidValue, validate, callback);
                fail("attesa un'eccezione");
            } catch (Exception e) {
                // eccezione attesa, tipo non specificato dal design
            }
        } else if (expected == ExpectedOutcome.EXCEPTION_PROPAGATED) {
            try {
                broker.find(oidValue, validate, callback);
                fail("attesa la propagazione dell'eccezione di processArgument");
            } catch (Exception e) {
                assertSame(processArgumentException, e);
            }
        } else {
            Object result = broker.find(oidValue, validate, callback);
            switch (expected) {
                case NULL:
                    assertNull(result);
                    break;
                case NON_NULL:
                    assertNotNull(result);
                    break;
                case CACHED_INSTANCE:
                    assertSame(primed, result);
                    break;
                case HOLLOW:
                    assertNotNull(result);
                    assertEquals(PCState.HOLLOW, broker.getStateManager(result).getPCState());
                    break;
                case PROCESS_RETURN_INSTANCE:
                    assertSame(processReturnSentinel, result);
                    break;
                default:
                    fail("outcome non ancora gestito: " + expected);
            }
        }
    }
}
