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
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.PCRegistry;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.UserException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PersistTest {

    private static final class DummyPersistentClass { }

    // NULL = pc null; UNMANAGED = non gestito (TRANSIENT); MANAGED = gestito.
    private enum PcKind { NULL, UNMANAGED, MANAGED }

    private enum CascadeKind { PRESENT, ABSENT }

    private enum ProcessArgumentBehavior { NONE, ACTION, THROWS }

    private enum ActValue { NONE, CASCADE, RUN }

    private enum ExpectedOutcome {
        EXCEPTION_UNSPECIFIED,
        EXCEPTION_PROPAGATED,
        NEW_STATE_MANAGER,
        SAME_STATE_MANAGER,
        NULL_RESULT
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"pc=null,id=null,cascade=--,call=null", PcKind.NULL, false, CascadeKind.ABSENT, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NULL_RESULT},
            {"pc=null,id=null,cascade=--,call=non null,pArg=azione", PcKind.NULL, false, CascadeKind.ABSENT, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NULL_RESULT},
            {"pc=null,id=null,cascade=--,call=non null,pArg=eccezione", PcKind.NULL, false, CascadeKind.ABSENT, true,
                ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.NULL_RESULT},
            {"pc=null,id=non null,cascade=--,call=null", PcKind.NULL, true, CascadeKind.ABSENT, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NULL_RESULT},
            {"pc=null,id=non null,cascade=--,call=non null,pArg=azione", PcKind.NULL, true, CascadeKind.ABSENT, true,
                ProcessArgumentBehavior.ACTION, ActValue.NONE, ExpectedOutcome.NULL_RESULT},
            {"pc=null,id=non null,cascade=--,call=non null,pArg=eccezione", PcKind.NULL, true, CascadeKind.ABSENT,
                true, ProcessArgumentBehavior.THROWS, ActValue.NONE, ExpectedOutcome.NULL_RESULT},

            {"pc=non gestito,id=null,cascade=presente,call=null", PcKind.UNMANAGED, false, CascadeKind.PRESENT,
                false, ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NEW_STATE_MANAGER},
            {"pc=non gestito,id=null,cascade=presente,call=non null,pArg=azione", PcKind.UNMANAGED, false,
                CascadeKind.PRESENT, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.NULL_RESULT},
            {"pc=non gestito,id=null,cascade=presente,call=non null,pArg=eccezione", PcKind.UNMANAGED, false,
                CascadeKind.PRESENT, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=non gestito,id=null,cascade=assente,call=null", PcKind.UNMANAGED, false, CascadeKind.ABSENT, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NEW_STATE_MANAGER},
            {"pc=non gestito,id=null,cascade=assente,call=non null,pArg=azione", PcKind.UNMANAGED, false,
                CascadeKind.ABSENT, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.NULL_RESULT},
            {"pc=non gestito,id=null,cascade=assente,call=non null,pArg=eccezione", PcKind.UNMANAGED, false,
                CascadeKind.ABSENT, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=non gestito,id=non null,cascade=presente,call=null", PcKind.UNMANAGED, true, CascadeKind.PRESENT,
                false, ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NEW_STATE_MANAGER},
            {"pc=non gestito,id=non null,cascade=presente,call=non null,pArg=azione", PcKind.UNMANAGED, true,
                CascadeKind.PRESENT, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.NULL_RESULT},
            {"pc=non gestito,id=non null,cascade=presente,call=non null,pArg=eccezione", PcKind.UNMANAGED, true,
                CascadeKind.PRESENT, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=non gestito,id=non null,cascade=assente,call=null", PcKind.UNMANAGED, true, CascadeKind.ABSENT,
                false, ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.NEW_STATE_MANAGER},
            {"pc=non gestito,id=non null,cascade=assente,call=non null,pArg=azione", PcKind.UNMANAGED, true,
                CascadeKind.ABSENT, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.NULL_RESULT},
            {"pc=non gestito,id=non null,cascade=assente,call=non null,pArg=eccezione", PcKind.UNMANAGED, true,
                CascadeKind.ABSENT, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},

            {"pc=gestito,id=null,cascade=presente,call=null", PcKind.MANAGED, false, CascadeKind.PRESENT, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.SAME_STATE_MANAGER},
            {"pc=gestito,id=null,cascade=presente,call=non null,pArg=azione", PcKind.MANAGED, false,
                CascadeKind.PRESENT, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.SAME_STATE_MANAGER},
            {"pc=gestito,id=null,cascade=presente,call=non null,pArg=eccezione", PcKind.MANAGED, false,
                CascadeKind.PRESENT, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito,id=null,cascade=assente,call=null", PcKind.MANAGED, false, CascadeKind.ABSENT, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.SAME_STATE_MANAGER},
            {"pc=gestito,id=null,cascade=assente,call=non null,pArg=azione", PcKind.MANAGED, false,
                CascadeKind.ABSENT, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.SAME_STATE_MANAGER},
            {"pc=gestito,id=null,cascade=assente,call=non null,pArg=eccezione", PcKind.MANAGED, false,
                CascadeKind.ABSENT, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito,id=non null,cascade=presente,call=null", PcKind.MANAGED, true, CascadeKind.PRESENT, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.SAME_STATE_MANAGER},
            {"pc=gestito,id=non null,cascade=presente,call=non null,pArg=azione", PcKind.MANAGED, true,
                CascadeKind.PRESENT, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.SAME_STATE_MANAGER},
            {"pc=gestito,id=non null,cascade=presente,call=non null,pArg=eccezione", PcKind.MANAGED, true,
                CascadeKind.PRESENT, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
            {"pc=gestito,id=non null,cascade=assente,call=null", PcKind.MANAGED, true, CascadeKind.ABSENT, false,
                ProcessArgumentBehavior.NONE, ActValue.NONE, ExpectedOutcome.SAME_STATE_MANAGER},
            {"pc=gestito,id=non null,cascade=assente,call=non null,pArg=azione", PcKind.MANAGED, true,
                CascadeKind.ABSENT, true, ProcessArgumentBehavior.ACTION, ActValue.NONE,
                ExpectedOutcome.SAME_STATE_MANAGER},
            {"pc=gestito,id=non null,cascade=assente,call=non null,pArg=eccezione", PcKind.MANAGED, true,
                CascadeKind.ABSENT, true, ProcessArgumentBehavior.THROWS, ActValue.NONE,
                ExpectedOutcome.EXCEPTION_PROPAGATED},

            {"pc=non gestito,id=null,cascade=presente,call=non null,pArg=azione(ACT_CASCADE)", PcKind.UNMANAGED,
                false, CascadeKind.PRESENT, true, ProcessArgumentBehavior.ACTION, ActValue.CASCADE,
                ExpectedOutcome.NULL_RESULT},
            {"pc=non gestito,id=null,cascade=presente,call=non null,pArg=azione(ACT_RUN)", PcKind.UNMANAGED, false,
                CascadeKind.PRESENT, true, ProcessArgumentBehavior.ACTION, ActValue.RUN,
                ExpectedOutcome.NEW_STATE_MANAGER},
            {"pc=non gestito,id=null,cascade=assente,call=non null,pArg=azione(ACT_CASCADE)", PcKind.UNMANAGED,
                false, CascadeKind.ABSENT, true, ProcessArgumentBehavior.ACTION, ActValue.CASCADE,
                ExpectedOutcome.NULL_RESULT},
            {"pc=non gestito,id=null,cascade=assente,call=non null,pArg=azione(ACT_RUN)", PcKind.UNMANAGED, false,
                CascadeKind.ABSENT, true, ProcessArgumentBehavior.ACTION, ActValue.RUN,
                ExpectedOutcome.NEW_STATE_MANAGER},
            {"pc=non gestito,id=non null,cascade=presente,call=non null,pArg=azione(ACT_CASCADE)", PcKind.UNMANAGED,
                true, CascadeKind.PRESENT, true, ProcessArgumentBehavior.ACTION, ActValue.CASCADE,
                ExpectedOutcome.NULL_RESULT},
            {"pc=non gestito,id=non null,cascade=presente,call=non null,pArg=azione(ACT_RUN)", PcKind.UNMANAGED,
                true, CascadeKind.PRESENT, true, ProcessArgumentBehavior.ACTION, ActValue.RUN,
                ExpectedOutcome.NEW_STATE_MANAGER},
            {"pc=non gestito,id=non null,cascade=assente,call=non null,pArg=azione(ACT_CASCADE)", PcKind.UNMANAGED,
                true, CascadeKind.ABSENT, true, ProcessArgumentBehavior.ACTION, ActValue.CASCADE,
                ExpectedOutcome.NULL_RESULT},
            {"pc=non gestito,id=non null,cascade=assente,call=non null,pArg=azione(ACT_RUN)", PcKind.UNMANAGED,
                true, CascadeKind.ABSENT, true, ProcessArgumentBehavior.ACTION, ActValue.RUN,
                ExpectedOutcome.NEW_STATE_MANAGER},
        });
    }

    private final PcKind pcKind;
    private final boolean idNonNull;
    private final CascadeKind cascadeKind;
    private final boolean call;
    private final ProcessArgumentBehavior processArgument;
    private final ActValue actValue;
    private final ExpectedOutcome expected;

    private BrokerImpl broker;
    private MetaDataRepository repo;
    private PersistenceCapable managedPc;
    private StateManagerImpl managedSm;
    private PersistenceCapable correlatedElement;

    public PersistTest(String label, PcKind pcKind, boolean idNonNull, CascadeKind cascadeKind, boolean call,
        ProcessArgumentBehavior processArgument, ActValue actValue, ExpectedOutcome expected) {
        this.pcKind = pcKind;
        this.idNonNull = idNonNull;
        this.cascadeKind = cascadeKind;
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

        repo = mock(MetaDataRepository.class, RETURNS_DEEP_STUBS);

        OpenJPAConfiguration conf = mock(OpenJPAConfiguration.class, RETURNS_DEEP_STUBS);
        when(conf.getMetaDataRepositoryInstance()).thenReturn(repo);
        when(factory.getConfiguration()).thenReturn(conf);

        broker = new BrokerImpl();
        broker.initialize(factory, delegatingStoreManager, false, ConnectionRetainModes.CONN_RETAIN_DEMAND, false);
        broker.begin();
    }

    private ClassMetaData buildMeta() {
        ClassMetaData meta = mock(ClassMetaData.class, RETURNS_DEEP_STUBS);
        when(meta.getIdentityType()).thenReturn(ClassMetaData.ID_DATASTORE);
        if (cascadeKind == CascadeKind.PRESENT) {
            FieldMetaData cascadeField = mock(FieldMetaData.class, RETURNS_DEEP_STUBS);
            when(cascadeField.getCascadePersist()).thenReturn(ValueMetaData.CASCADE_IMMEDIATE);
            when(meta.getFields()).thenReturn(new FieldMetaData[] {cascadeField});
        } else {
            when(meta.getFields()).thenReturn(new FieldMetaData[0]);
        }
        when(meta.getPkAndNonPersistentManagedFmdIndexes()).thenReturn(new int[0]);
        when(meta.getPCSubclasses()).thenReturn(new Class<?>[0]);
        return meta;
    }

    private void buildCorrelatedIfNeeded() {
        if (cascadeKind == CascadeKind.PRESENT) {
            correlatedElement = mock(PersistenceCapable.class);
            when(correlatedElement.pcGetStateManager()).thenReturn(null);
        }
    }

    private Object buildPc() {
        buildCorrelatedIfNeeded();
        switch (pcKind) {
            case NULL:
                return null;
            case UNMANAGED: {
                PersistenceCapable pc = mock(PersistenceCapable.class);
                when(pc.pcGetStateManager()).thenReturn(null);
                when(pc.pcGetGenericContext()).thenReturn(null);
                when(pc.pcIsDetached()).thenReturn(Boolean.FALSE);

                ClassMetaData meta = buildMeta();
                doReturn(meta).when(repo).getMetaData(eq(pc.getClass()), any(), anyBoolean());
                PCRegistry.register(pc.getClass(), new String[0], new Class<?>[0], new byte[0], null,
                    "UnmanagedMock", pc);
                return pc;
            }
            case MANAGED: {
                managedSm = mock(StateManagerImpl.class, RETURNS_DEEP_STUBS);
                when(managedSm.isDetached()).thenReturn(false);
                when(managedSm.isEmbedded()).thenReturn(false);

                managedPc = mock(PersistenceCapable.class);
                when(managedPc.pcGetStateManager()).thenReturn(managedSm);
                when(managedPc.pcGetGenericContext()).thenReturn(broker);
                when(managedSm.getPersistenceCapable()).thenReturn(managedPc);
                return managedPc;
            }
            default:
                throw new IllegalStateException();
        }
    }

    private Object buildId() {
        return idNonNull ? new Object() : null;
    }

    @Test
    public void testPersist() {
        Object pcValue = buildPc();
        Object idValue = buildId();

        OpCallbacks callback = null;
        UserException processArgumentException = null;
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
                broker.persist(pcValue, idValue, callback);
                fail("attesa un'eccezione");
            } catch (Exception e) {
                // eccezione attesa, tipo non specificato dal design
            }
        } else if (expected == ExpectedOutcome.EXCEPTION_PROPAGATED) {
            try {
                broker.persist(pcValue, idValue, callback);
                fail("attesa la propagazione dell'eccezione di processArgument");
            } catch (Exception e) {
                assertSame(processArgumentException, e);
            }
        } else if (expected == ExpectedOutcome.NEW_STATE_MANAGER) {
            OpenJPAStateManager result = broker.persist(pcValue, idValue, callback);
            assertNotNull(result);
            if (cascadeKind == CascadeKind.PRESENT) {
                assertNull(correlatedElement.pcGetStateManager());
            }
        } else if (expected == ExpectedOutcome.SAME_STATE_MANAGER) {
            OpenJPAStateManager result = broker.persist(pcValue, idValue, callback);
            assertSame(managedSm, result);
            if (cascadeKind == CascadeKind.PRESENT) {
                assertNull(correlatedElement.pcGetStateManager());
            }
        } else {
            OpenJPAStateManager result = broker.persist(pcValue, idValue, callback);
            assertNull(result);
        }
    }
}
