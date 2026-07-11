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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.PCRegistry;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.util.UserException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AttachAllTest {

    private static final class DummyPersistentClass { }

    // NULL = objs null; EMPTY = collezione vuota; ALL_NEW = tutti TRANSIENT (mai gestiti);
    // ALL_OLD = tutti precedentemente detached; ALL_ATTACHED = tutti gia' gestiti da questo broker;
    // MIXED_VALID = almeno due elementi di tipo diverso tra new/old/attached; MIXED_INVALID = almeno
    // un elemento PDELETED (composizione del resto scelta in fase di implementazione, non BVA).
    private enum ObjsKind { NULL, EMPTY, ALL_NEW, ALL_OLD, ALL_ATTACHED, MIXED_VALID, MIXED_INVALID }

    private enum ProcessArgumentBehavior { NONE, ACTION, THROWS }

    private enum ActValue { NONE, CASCADE, RUN }

    private enum ExpectedOutcome {
        NULL_RESULT,                    // objs=null: attachAll ritorna null direttamente (corretto da osservazione)
        EMPTY_ARRAY,
        EXCEPTION_UNSPECIFIED,          // eccezione attesa, tipo non specificato dal design
        EXCEPTION_PROPAGATED,           // singola eccezione di processArgument, propagata cosi' com'e'
        EXCEPTION_PROPAGATED_WRAPPED,   // piu' elementi, ciascuno lancia: AttachManager le accumula e le
                                         // avvolge in un'unica UserException con throwable annidati (corretto
                                         // da osservazione: non e' piu' la stessa istanza dell'eccezione originale)
        PERSISTED_COPY,                 // copyNew=true, elemento pienamente processato: copia dell'originale
        PERSISTED_SAME,                 // copyNew=false, elemento pienamente processato: stessa istanza
        UNCHANGED,                      // azione (ACT_NONE, rappresentante): elemento non processato, invariato
        OLD_MERGED,                     // all old, pienamente processato: istanza esistente trovata via find,
                                         // diversa dall'originale scollegato
        MIXED_PROCESSED,                // misti validi, pArg=--: entrambi gli elementi pienamente processati
        MIXED_UNCHANGED                 // misti (validi o con invalido), azione: nessun elemento processato
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"objs=null,copyNew=true,call=null,pArg=-- (corretto)",
                ObjsKind.NULL, true, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.NULL_RESULT},
            {"objs=null,copyNew=true,call=non null,pArg=azione (corretto)",
                ObjsKind.NULL, true, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.NULL_RESULT},
            {"objs=null,copyNew=true,call=non null,pArg=eccezione",
                ObjsKind.NULL, true, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.NULL_RESULT},
            {"objs=null,copyNew=false,call=null,pArg=-- (corretto)",
                ObjsKind.NULL, false, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.NULL_RESULT},
            {"objs=null,copyNew=false,call=non null,pArg=azione (corretto)",
                ObjsKind.NULL, false, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.NULL_RESULT},
            {"objs=null,copyNew=false,call=non null,pArg=eccezione (corretto)",
                ObjsKind.NULL, false, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.NULL_RESULT},
            {"objs=vuota,copyNew=true,call=null,pArg=--",
                ObjsKind.EMPTY, true, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.EMPTY_ARRAY},
            {"objs=vuota,copyNew=true,call=non null,pArg=azione",
                ObjsKind.EMPTY, true, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.EMPTY_ARRAY},
            {"objs=vuota,copyNew=true,call=non null,pArg=eccezione",
                ObjsKind.EMPTY, true, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EMPTY_ARRAY},
            {"objs=vuota,copyNew=false,call=null,pArg=--",
                ObjsKind.EMPTY, false, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.EMPTY_ARRAY},
            {"objs=vuota,copyNew=false,call=non null,pArg=azione",
                ObjsKind.EMPTY, false, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.EMPTY_ARRAY},
            {"objs=vuota,copyNew=false,call=non null,pArg=eccezione",
                ObjsKind.EMPTY, false, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EMPTY_ARRAY},
            {"objs=all new,copyNew=true,call=null,pArg=--",
                ObjsKind.ALL_NEW, true, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.PERSISTED_COPY},
            {"objs=all new,copyNew=true,call=non null,pArg=azione (corretto)",
                ObjsKind.ALL_NEW, true, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.UNCHANGED},
            {"objs=all new,copyNew=true,call=non null,pArg=eccezione",
                ObjsKind.ALL_NEW, true, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"objs=all new,copyNew=false,call=null,pArg=--",
                ObjsKind.ALL_NEW, false, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.PERSISTED_SAME},
            {"objs=all new,copyNew=false,call=non null,pArg=azione (corretto)",
                ObjsKind.ALL_NEW, false, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.UNCHANGED},
            {"objs=all new,copyNew=false,call=non null,pArg=eccezione",
                ObjsKind.ALL_NEW, false, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"objs=all old,copyNew=true,call=null,pArg=--",
                ObjsKind.ALL_OLD, true, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.OLD_MERGED},
            {"objs=all old,copyNew=true,call=non null,pArg=azione (corretto)",
                ObjsKind.ALL_OLD, true, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.UNCHANGED},
            {"objs=all old,copyNew=true,call=non null,pArg=eccezione",
                ObjsKind.ALL_OLD, true, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"objs=all old,copyNew=false,call=null,pArg=--",
                ObjsKind.ALL_OLD, false, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.OLD_MERGED},
            {"objs=all old,copyNew=false,call=non null,pArg=azione (corretto)",
                ObjsKind.ALL_OLD, false, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.UNCHANGED},
            {"objs=all old,copyNew=false,call=non null,pArg=eccezione",
                ObjsKind.ALL_OLD, false, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"objs=all attached,copyNew=true,call=null,pArg=-- (corretto)",
                ObjsKind.ALL_ATTACHED, true, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.PERSISTED_COPY},
            {"objs=all attached,copyNew=true,call=non null,pArg=azione",
                ObjsKind.ALL_ATTACHED, true, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.UNCHANGED},
            {"objs=all attached,copyNew=true,call=non null,pArg=eccezione",
                ObjsKind.ALL_ATTACHED, true, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"objs=all attached,copyNew=false,call=null,pArg=--",
                ObjsKind.ALL_ATTACHED, false, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.PERSISTED_SAME},
            {"objs=all attached,copyNew=false,call=non null,pArg=azione",
                ObjsKind.ALL_ATTACHED, false, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.UNCHANGED},
            {"objs=all attached,copyNew=false,call=non null,pArg=eccezione",
                ObjsKind.ALL_ATTACHED, false, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"objs=misti validi eterogenei,copyNew=true,call=null,pArg=--",
                ObjsKind.MIXED_VALID, true, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.MIXED_PROCESSED},
            {"objs=misti validi eterogenei,copyNew=true,call=non null,pArg=azione (corretto)",
                ObjsKind.MIXED_VALID, true, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.MIXED_UNCHANGED},
            {"objs=misti validi eterogenei,copyNew=true,call=non null,pArg=eccezione (corretto)",
                ObjsKind.MIXED_VALID, true, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED_WRAPPED},
            {"objs=misti validi eterogenei,copyNew=false,call=null,pArg=--",
                ObjsKind.MIXED_VALID, false, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.MIXED_PROCESSED},
            {"objs=misti validi eterogenei,copyNew=false,call=non null,pArg=azione (corretto)",
                ObjsKind.MIXED_VALID, false, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.MIXED_UNCHANGED},
            {"objs=misti validi eterogenei,copyNew=false,call=non null,pArg=eccezione (corretto)",
                ObjsKind.MIXED_VALID, false, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED_WRAPPED},
            {"objs=misti almeno 1 invalido,copyNew=true,call=null,pArg=--",
                ObjsKind.MIXED_INVALID, true, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"objs=misti almeno 1 invalido,copyNew=true,call=non null,pArg=azione (corretto)",
                ObjsKind.MIXED_INVALID, true, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.MIXED_UNCHANGED},
            {"objs=misti almeno 1 invalido,copyNew=true,call=non null,pArg=eccezione (corretto)",
                ObjsKind.MIXED_INVALID, true, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED_WRAPPED},
            {"objs=misti almeno 1 invalido,copyNew=false,call=null,pArg=--",
                ObjsKind.MIXED_INVALID, false, false, ProcessArgumentBehavior.NONE, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"objs=misti almeno 1 invalido,copyNew=false,call=non null,pArg=azione (corretto)",
                ObjsKind.MIXED_INVALID, false, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.MIXED_UNCHANGED},
            {"objs=misti almeno 1 invalido,copyNew=false,call=non null,pArg=eccezione (corretto)",
                ObjsKind.MIXED_INVALID, false, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED_WRAPPED},
            {"objs=all new,copyNew=true,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                ObjsKind.ALL_NEW, true, true, ProcessArgumentBehavior.ACTION, ActValue.CASCADE,
                ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"objs=all new,copyNew=true,call=non null,pArg=azione (ACT_RUN) (corretto)",
                ObjsKind.ALL_NEW, true, true, ProcessArgumentBehavior.ACTION, ActValue.RUN,
                ExpectedOutcome.PERSISTED_COPY},
            {"objs=all old,copyNew=true,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                ObjsKind.ALL_OLD, true, true, ProcessArgumentBehavior.ACTION, ActValue.CASCADE,
                ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"objs=all old,copyNew=true,call=non null,pArg=azione (ACT_RUN) (corretto)",
                ObjsKind.ALL_OLD, true, true, ProcessArgumentBehavior.ACTION, ActValue.RUN,
                ExpectedOutcome.OLD_MERGED},
            {"objs=all attached,copyNew=true,call=non null,pArg=azione (ACT_CASCADE)",
                ObjsKind.ALL_ATTACHED, true, true, ProcessArgumentBehavior.ACTION, ActValue.CASCADE,
                ExpectedOutcome.UNCHANGED},
            {"objs=all attached,copyNew=true,call=non null,pArg=azione (ACT_RUN) (corretto)",
                ObjsKind.ALL_ATTACHED, true, true, ProcessArgumentBehavior.ACTION, ActValue.RUN,
                ExpectedOutcome.PERSISTED_COPY},
            {"objs=misti validi eterogenei,copyNew=true,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                ObjsKind.MIXED_VALID, true, true, ProcessArgumentBehavior.ACTION, ActValue.CASCADE,
                ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"objs=misti validi eterogenei,copyNew=true,call=non null,pArg=azione (ACT_RUN) (corretto)",
                ObjsKind.MIXED_VALID, true, true, ProcessArgumentBehavior.ACTION, ActValue.RUN,
                ExpectedOutcome.MIXED_PROCESSED},
            {"objs=misti almeno 1 invalido,copyNew=true,call=non null,pArg=azione (ACT_CASCADE) (corretto)",
                ObjsKind.MIXED_INVALID, true, true, ProcessArgumentBehavior.ACTION, ActValue.CASCADE,
                ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"objs=misti almeno 1 invalido,copyNew=true,call=non null,pArg=azione (ACT_RUN) (corretto)",
                ObjsKind.MIXED_INVALID, true, true, ProcessArgumentBehavior.ACTION, ActValue.RUN,
                ExpectedOutcome.EXCEPTION_UNSPECIFIED},
        });
    }

    private final ObjsKind objsKind;
    private final boolean copyNew;
    private final boolean call;
    private final ProcessArgumentBehavior processArgument;
    private final ActValue actValue;
    private final ExpectedOutcome expected;

    private BrokerImpl broker;
    private MetaDataRepository repo;
    private StoreManager storeManager;

    public AttachAllTest(String label, ObjsKind objsKind, boolean copyNew, boolean call,
        ProcessArgumentBehavior processArgument, ActValue actValue, ExpectedOutcome expected) {
        this.objsKind = objsKind;
        this.copyNew = copyNew;
        this.call = call;
        this.processArgument = processArgument;
        this.actValue = actValue;
        this.expected = expected;
    }

    @Before
    public void setUp() {
        AbstractBrokerFactory factory = mock(AbstractBrokerFactory.class, RETURNS_DEEP_STUBS);
        storeManager = mock(StoreManager.class, RETURNS_DEEP_STUBS);
        DelegatingStoreManager delegatingStoreManager = new DelegatingStoreManager(storeManager) { };

        repo = mock(MetaDataRepository.class, RETURNS_DEEP_STUBS);

        OpenJPAConfiguration conf = mock(OpenJPAConfiguration.class, RETURNS_DEEP_STUBS);
        when(conf.getMetaDataRepositoryInstance()).thenReturn(repo);
        when(factory.getConfiguration()).thenReturn(conf);

        broker = new BrokerImpl();
        broker.initialize(factory, delegatingStoreManager, false, ConnectionRetainModes.CONN_RETAIN_DEMAND, false);
        broker.begin();
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

    private ClassMetaData registerDummyClass(Object oidToResolve, boolean storePresent) {
        PersistenceCapable pcPrototype = mock(PersistenceCapable.class, RETURNS_DEEP_STUBS);
        when(pcPrototype.pcNewInstance(any(), anyBoolean())).thenAnswer(invocation -> newTrackedPc());
        when(pcPrototype.pcNewInstance(any(), any(), anyBoolean())).thenAnswer(invocation -> newTrackedPc());
        PCRegistry.register(DummyPersistentClass.class, new String[0], new Class<?>[0], new byte[0],
            null, "DummyPersistentClass", pcPrototype);

        ClassMetaData meta = mock(ClassMetaData.class, RETURNS_DEEP_STUBS);
        when(meta.getRepository()).thenReturn(repo);
        when(meta.getIdentityType()).thenReturn(ClassMetaData.ID_DATASTORE);
        doReturn(DummyPersistentClass.class).when(meta).getDescribedType();
        when(meta.getFields()).thenReturn(new FieldMetaData[0]);
        when(meta.getDefinedFields()).thenReturn(new FieldMetaData[0]);
        when(meta.getPkAndNonPersistentManagedFmdIndexes()).thenReturn(new int[0]);
        when(meta.getPrimaryKeyFields()).thenReturn(new FieldMetaData[0]);
        when(meta.getPCSubclasses()).thenReturn(new Class<?>[0]);
        when(repo.getMetaData(any(Object.class), any(), anyBoolean())).thenReturn(meta);
        when(repo.getMetaData(any(Class.class), any(), anyBoolean())).thenReturn(meta);

        when(storeManager.initialize(any(), any(), any(), any())).thenReturn(storePresent);
        when(storeManager.exists(any(), any())).thenReturn(storePresent);
        return meta;
    }

    private PersistenceCapable buildNewElement() {
        registerDummyClass(new Object(), false);
        PersistenceCapable pc = mock(PersistenceCapable.class);
        when(pc.pcGetStateManager()).thenReturn(null);
        when(pc.pcGetGenericContext()).thenReturn(broker);
        doAnswer(invocation -> {
            when(pc.pcGetStateManager()).thenReturn(invocation.getArgument(0));
            return null;
        }).when(pc).pcReplaceStateManager(any());
        when(pc.pcGetDetachedState()).thenReturn(null);
        when(pc.pcIsDetached()).thenReturn(Boolean.FALSE);
        when(pc.pcNewInstance(any(), anyBoolean())).thenAnswer(invocation -> newTrackedPc());
        when(pc.pcNewInstance(any(), any(), anyBoolean())).thenAnswer(invocation -> newTrackedPc());
        return pc;
    }

    private PersistenceCapable buildAttachedElement() {
        ClassMetaData meta = registerDummyClass(new Object(), false);
        StateManagerImpl sm = mock(StateManagerImpl.class, RETURNS_DEEP_STUBS);
        when(sm.getBroker()).thenReturn(broker);
        when(sm.isPersistent()).thenReturn(true);
        when(sm.isDetached()).thenReturn(false);
        when(sm.isEmbedded()).thenReturn(false);
        when(sm.getMetaData()).thenReturn(meta);

        PersistenceCapable pc = mock(PersistenceCapable.class);
        when(pc.pcGetStateManager()).thenReturn(sm);
        when(pc.pcGetGenericContext()).thenReturn(broker);
        when(pc.pcGetDetachedState()).thenReturn(null);
        when(pc.pcIsDetached()).thenReturn(Boolean.FALSE);
        when(pc.pcNewInstance(any(), anyBoolean())).thenAnswer(invocation -> newTrackedPc());
        when(pc.pcNewInstance(any(), any(), anyBoolean())).thenAnswer(invocation -> newTrackedPc());
        when(sm.getPersistenceCapable()).thenReturn(pc);
        return pc;
    }

    private PersistenceCapable buildOldElement() {
        Object oid = new Object();
        registerDummyClass(oid, true);
        when(storeManager.newDataStoreId(any(), any())).thenReturn(oid);

        PersistenceCapable pc = mock(PersistenceCapable.class);
        when(pc.pcGetStateManager()).thenReturn(null);
        when(pc.pcGetDetachedState()).thenReturn(new Object[] {new Object(), null, null});
        when(pc.pcIsDetached()).thenReturn(Boolean.TRUE);
        return pc;
    }

    private PersistenceCapable buildInvalidElement() {
        StateManagerImpl sm = mock(StateManagerImpl.class, RETURNS_DEEP_STUBS);
        when(sm.getBroker()).thenReturn(broker);
        when(sm.isPersistent()).thenReturn(true);
        when(sm.isDeleted()).thenReturn(true);
        when(sm.getPCState()).thenReturn(PCState.PDELETED);

        PersistenceCapable pc = mock(PersistenceCapable.class);
        when(pc.pcGetStateManager()).thenReturn(sm);
        when(pc.pcGetGenericContext()).thenReturn(broker);
        when(pc.pcGetDetachedState()).thenReturn(null);
        when(pc.pcIsDetached()).thenReturn(Boolean.FALSE);
        when(sm.getPersistenceCapable()).thenReturn(pc);
        return pc;
    }

    private Collection<Object> buildObjs() {
        switch (objsKind) {
            case NULL:
                return null;
            case EMPTY:
                return Collections.emptyList();
            case ALL_NEW:
                return Collections.singletonList((Object) buildNewElement());
            case ALL_OLD:
                return Collections.singletonList((Object) buildOldElement());
            case ALL_ATTACHED:
                return Collections.singletonList((Object) buildAttachedElement());
            case MIXED_VALID: {
                List<Object> objs = new ArrayList<>();
                objs.add(buildNewElement());
                objs.add(buildAttachedElement());
                return objs;
            }
            case MIXED_INVALID: {
                List<Object> objs = new ArrayList<>();
                objs.add(buildNewElement());
                objs.add(buildInvalidElement());
                return objs;
            }
            default:
                throw new IllegalStateException();
        }
    }

    @Test
    public void testAttachAll() {
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
                assertNull(broker.attachAll(objsValue, copyNew, callback));
                break;
            case EXCEPTION_UNSPECIFIED:
                try {
                    broker.attachAll(objsValue, copyNew, callback);
                    fail("attesa un'eccezione");
                } catch (Exception e) {
                    // eccezione attesa, tipo non specificato dal design
                }
                break;
            case EXCEPTION_PROPAGATED:
                try {
                    broker.attachAll(objsValue, copyNew, callback);
                    fail("attesa la propagazione dell'eccezione di processArgument");
                } catch (Exception e) {
                    assertSame(processArgumentException, e);
                }
                break;
            case EXCEPTION_PROPAGATED_WRAPPED:
                try {
                    broker.attachAll(objsValue, copyNew, callback);
                    fail("attesa la propagazione (avvolta) dell'eccezione di processArgument");
                } catch (UserException e) {
                    boolean found = false;
                    for (Throwable nested : e.getNestedThrowables()) {
                        found = found || nested == processArgumentException;
                    }
                    if (!found) {
                        fail("l'eccezione di processArgument non compare tra i throwable annidati");
                    }
                }
                break;
            case EMPTY_ARRAY: {
                Object[] result = broker.attachAll(objsValue, copyNew, callback);
                assertEquals(0, result.length);
                break;
            }
            case PERSISTED_COPY: {
                PersistenceCapable original = (PersistenceCapable) objsValue.iterator().next();
                Object[] result = broker.attachAll(objsValue, copyNew, callback);
                assertEquals(1, result.length);
                assertNotNull(result[0]);
                assertNotSame(original, result[0]);
                break;
            }
            case PERSISTED_SAME: {
                PersistenceCapable original = (PersistenceCapable) objsValue.iterator().next();
                Object[] result = broker.attachAll(objsValue, copyNew, callback);
                assertEquals(1, result.length);
                assertSame(original, result[0]);
                break;
            }
            case UNCHANGED: {
                PersistenceCapable original = (PersistenceCapable) objsValue.iterator().next();
                Object[] result = broker.attachAll(objsValue, copyNew, callback);
                assertEquals(1, result.length);
                assertSame(original, result[0]);
                break;
            }
            case OLD_MERGED: {
                PersistenceCapable original = (PersistenceCapable) objsValue.iterator().next();
                Object[] result = broker.attachAll(objsValue, copyNew, callback);
                assertEquals(1, result.length);
                assertNotNull(result[0]);
                assertNotSame(original, result[0]);
                break;
            }
            case MIXED_PROCESSED: {
                java.util.Iterator<Object> it = objsValue.iterator();
                PersistenceCapable firstOriginal = (PersistenceCapable) it.next();
                PersistenceCapable secondOriginal = (PersistenceCapable) it.next();
                Object[] result = broker.attachAll(objsValue, copyNew, callback);
                assertEquals(2, result.length);
                if (copyNew) {
                    assertNotSame(firstOriginal, result[0]);
                    assertNotSame(secondOriginal, result[1]);
                } else {
                    assertSame(firstOriginal, result[0]);
                    assertSame(secondOriginal, result[1]);
                }
                break;
            }
            case MIXED_UNCHANGED: {
                java.util.Iterator<Object> it = objsValue.iterator();
                PersistenceCapable firstOriginal = (PersistenceCapable) it.next();
                PersistenceCapable secondOriginal = (PersistenceCapable) it.next();
                Object[] result = broker.attachAll(objsValue, copyNew, callback);
                assertEquals(2, result.length);
                assertSame(firstOriginal, result[0]);
                assertSame(secondOriginal, result[1]);
                break;
            }
            default:
                fail("outcome non ancora gestito: " + expected);
        }
    }
}
