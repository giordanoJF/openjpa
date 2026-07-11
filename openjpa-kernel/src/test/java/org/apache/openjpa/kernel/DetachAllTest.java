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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;

import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.util.UserException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DetachAllTest {

    // NULL = objs null;
    // EMPTY = collezione vuota;
    // ONE_MANAGED = un elemento gestito;
    // ONE_UNMANAGED = un elemento non gestito;
    // MIXED = un elemento gestito e un elemento non gestito.
    private enum ObjsKind { NULL, EMPTY, ONE_MANAGED, ONE_UNMANAGED, MIXED }

    // NONE = call assente; ACTION = processArgument ritorna un codice azione (ACT_NONE/ACT_CASCADE/
    // ACT_RUN, equivalenti per design); THROWS = lancia OpenJPAException.
    private enum ProcessArgumentBehavior { NONE, ACTION, THROWS }

    // Rilevante solo quando processArgument == ACTION: quale delle tre costanti (assunte
    // equivalenti dal design) viene effettivamente restituita dal callback.
    private enum ActValue { NONE, CASCADE, RUN }

    private enum ExpectedOutcome {
        NULL_RESULT,             // detachAll ritorna null (corretto da osservazione)
        EMPTY_ARRAY,             // array vuoto
        EXCEPTION_PROPAGATED,    // eccezione di processArgument propagata da detachAll
        ONE_ELEMENT_DETACHED,    // un elemento, copia scollegata dal broker (identita' diversa, sm nullo)
        ONE_ELEMENT_UNCHANGED,   // un elemento, stessa istanza originale invariata (corretto da osservazione)
        MIXED_MANAGED_DETACHED,  // due elementi: il gestito diventa una copia scollegata, il non gestito invariato
        MIXED_MANAGED_UNCHANGED  // due elementi, entrambi invariati (nessun detach applicato)
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"objs=null,call=null,pArg=-- (corretto)",
                ObjsKind.NULL, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.NULL_RESULT},
            {"objs=null,call=non null,pArg=azione (corretto)",
                ObjsKind.NULL, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.NULL_RESULT},
            {"objs=null,call=non null,pArg=eccezione (corretto)",
                ObjsKind.NULL, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.NULL_RESULT},
            {"objs=vuota,call=null,pArg=--",
                ObjsKind.EMPTY, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.EMPTY_ARRAY},
            {"objs=vuota,call=non null,pArg=azione",
                ObjsKind.EMPTY, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.EMPTY_ARRAY},
            {"objs=vuota,call=non null,pArg=eccezione (corretto)",
                ObjsKind.EMPTY, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EMPTY_ARRAY},
            {"objs=un elemento gestito,call=null,pArg=--",
                ObjsKind.ONE_MANAGED, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.ONE_ELEMENT_DETACHED},
            {"objs=un elemento gestito,call=non null,pArg=azione (ACT_NONE) (corretto)",
                ObjsKind.ONE_MANAGED, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.ONE_ELEMENT_UNCHANGED},
            {"objs=un elemento gestito,call=non null,pArg=eccezione",
                ObjsKind.ONE_MANAGED, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"objs=un elemento non gestito,call=null,pArg=-- (corretto)",
                ObjsKind.ONE_UNMANAGED, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.ONE_ELEMENT_UNCHANGED},
            {"objs=un elemento non gestito,call=non null,pArg=azione (ACT_NONE) (corretto)",
                ObjsKind.ONE_UNMANAGED, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.ONE_ELEMENT_UNCHANGED},
            {"objs=un elemento non gestito,call=non null,pArg=eccezione",
                ObjsKind.ONE_UNMANAGED, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"objs=elementi misti,call=null,pArg=-- (corretto)",
                ObjsKind.MIXED, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.MIXED_MANAGED_DETACHED},
            {"objs=elementi misti,call=non null,pArg=azione (ACT_NONE) (corretto)",
                ObjsKind.MIXED, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.MIXED_MANAGED_UNCHANGED},
            {"objs=elementi misti,call=non null,pArg=eccezione",
                ObjsKind.MIXED, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"objs=null,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                ObjsKind.NULL, true, ProcessArgumentBehavior.ACTION, ActValue.CASCADE,
                ExpectedOutcome.NULL_RESULT},
            {"objs=null,call=non null,pArg=azione (ACT_RUN) (corretto)",
                ObjsKind.NULL, true, ProcessArgumentBehavior.ACTION, ActValue.RUN,
                ExpectedOutcome.NULL_RESULT},
            {"objs=vuota,call=non null,pArg=azione (ACT_CASCADE)",
                ObjsKind.EMPTY, true, ProcessArgumentBehavior.ACTION, ActValue.CASCADE,
                ExpectedOutcome.EMPTY_ARRAY},
            {"objs=vuota,call=non null,pArg=azione (ACT_RUN)",
                ObjsKind.EMPTY, true, ProcessArgumentBehavior.ACTION, ActValue.RUN,
                ExpectedOutcome.EMPTY_ARRAY},
            {"objs=un elemento gestito,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                ObjsKind.ONE_MANAGED, true, ProcessArgumentBehavior.ACTION, ActValue.CASCADE,
                ExpectedOutcome.ONE_ELEMENT_UNCHANGED},
            {"objs=un elemento gestito,call=non null,pArg=azione (ACT_RUN) (corretto)",
                ObjsKind.ONE_MANAGED, true, ProcessArgumentBehavior.ACTION, ActValue.RUN,
                ExpectedOutcome.ONE_ELEMENT_DETACHED},
            {"objs=un elemento non gestito,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                ObjsKind.ONE_UNMANAGED, true, ProcessArgumentBehavior.ACTION, ActValue.CASCADE,
                ExpectedOutcome.ONE_ELEMENT_UNCHANGED},
            {"objs=un elemento non gestito,call=non null,pArg=azione (ACT_RUN) (corretto)",
                ObjsKind.ONE_UNMANAGED, true, ProcessArgumentBehavior.ACTION, ActValue.RUN,
                ExpectedOutcome.ONE_ELEMENT_UNCHANGED},
            {"objs=elementi misti,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                ObjsKind.MIXED, true, ProcessArgumentBehavior.ACTION, ActValue.CASCADE,
                ExpectedOutcome.MIXED_MANAGED_UNCHANGED},
            {"objs=elementi misti,call=non null,pArg=azione (ACT_RUN) (corretto)",
                ObjsKind.MIXED, true, ProcessArgumentBehavior.ACTION, ActValue.RUN,
                ExpectedOutcome.MIXED_MANAGED_DETACHED},
        });
    }

    private final ObjsKind objsKind;
    private final boolean call;
    private final ProcessArgumentBehavior processArgument;
    private final ActValue actValue;
    private final ExpectedOutcome expected;

    private BrokerImpl broker;
    private PersistenceCapable managedElement;
    private PersistenceCapable unmanagedElement;

    public DetachAllTest(String label, ObjsKind objsKind, boolean call, ProcessArgumentBehavior processArgument,
        ActValue actValue, ExpectedOutcome expected) {
        this.objsKind = objsKind;
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

        Compatibility compatibility = new Compatibility();
        compatibility.setCopyOnDetach(true);

        OpenJPAConfiguration conf = mock(OpenJPAConfiguration.class, RETURNS_DEEP_STUBS);
        when(conf.getCompatibilityInstance()).thenReturn(compatibility);
        when(factory.getConfiguration()).thenReturn(conf);

        broker = new BrokerImpl();
        broker.initialize(factory, delegatingStoreManager, false, ConnectionRetainModes.CONN_RETAIN_DEMAND, false);
    }

    private PersistenceCapable buildManagedElement() {
        ClassMetaData meta = mock(ClassMetaData.class, RETURNS_DEEP_STUBS);
        when(meta.getFields()).thenReturn(new FieldMetaData[0]);
        when(meta.getPrimaryKeyFields()).thenReturn(new FieldMetaData[0]);
        when(meta.usesDetachedState()).thenReturn(Boolean.FALSE);

        StateManagerImpl sm = mock(StateManagerImpl.class, RETURNS_DEEP_STUBS);
        when(sm.getMetaData()).thenReturn(meta);
        when(sm.getLoaded()).thenReturn(new BitSet());

        PersistenceCapable pc = mock(PersistenceCapable.class);
        when(pc.pcGetStateManager()).thenReturn(sm);
        when(pc.pcGetGenericContext()).thenReturn(broker);
        when(sm.getPersistenceCapable()).thenReturn(pc);

        PersistenceCapable detachedPc = mock(PersistenceCapable.class);
        when(pc.pcNewInstance(any(), anyBoolean())).thenReturn(detachedPc);

        managedElement = pc;
        return pc;
    }

    private PersistenceCapable buildUnmanagedElement() {
        PersistenceCapable pc = mock(PersistenceCapable.class);
        when(pc.pcGetStateManager()).thenReturn(null);
        unmanagedElement = pc;
        return pc;
    }

    private Collection<Object> buildObjs() {
        switch (objsKind) {
            case NULL:
                return null;
            case EMPTY:
                return Collections.emptyList();
            case ONE_MANAGED:
                return Collections.singletonList((Object) buildManagedElement());
            case ONE_UNMANAGED:
                return Collections.singletonList((Object) buildUnmanagedElement());
            case MIXED:
                return Arrays.asList((Object) buildManagedElement(), buildUnmanagedElement());
            default:
                throw new IllegalStateException();
        }
    }

    @Test
    public void testDetachAll() {
        Collection<Object> objsValue = buildObjs();

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

        switch (expected) {
            case NULL_RESULT:
                assertNull(broker.detachAll(objsValue, callback));
                break;
            case EMPTY_ARRAY:
                assertEquals(0, broker.detachAll(objsValue, callback).length);
                break;
            case EXCEPTION_PROPAGATED:
                try {
                    broker.detachAll(objsValue, callback);
                    fail("attesa la propagazione dell'eccezione di processArgument");
                } catch (Exception e) {
                    assertSame(processArgumentException, e);
                }
                break;
            case ONE_ELEMENT_DETACHED: {
                // "detached instance" = copia scollegata dal broker (Javadoc: "unmanaged copies"),
                // non l'istanza gestita originale: identita' diversa e nessuno StateManager attivo.
                Object[] result = broker.detachAll(objsValue, callback);
                assertEquals(1, result.length);
                assertNotSame(managedElement, result[0]);
                assertNull(((PersistenceCapable) result[0]).pcGetStateManager());
                break;
            }
            case ONE_ELEMENT_UNCHANGED: {
                Object[] result = broker.detachAll(objsValue, callback);
                assertEquals(1, result.length);
                Object original = objsKind == ObjsKind.ONE_MANAGED ? managedElement : unmanagedElement;
                assertSame(original, result[0]);
                break;
            }
            case MIXED_MANAGED_DETACHED: {
                Object[] result = broker.detachAll(objsValue, callback);
                assertEquals(2, result.length);
                assertNotSame(managedElement, result[0]);
                assertNull(((PersistenceCapable) result[0]).pcGetStateManager());
                assertSame(unmanagedElement, result[1]);
                break;
            }
            case MIXED_MANAGED_UNCHANGED: {
                Object[] result = broker.detachAll(objsValue, callback);
                assertEquals(2, result.length);
                assertSame(managedElement, result[0]);
                assertSame(unmanagedElement, result[1]);
                break;
            }
            default:
                throw new IllegalStateException();
        }
    }
}
