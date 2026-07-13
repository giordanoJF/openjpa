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
public class Lock2Test {

    private enum PcKind { NULL, UNMANAGED, MANAGED_NEW, MANAGED_STORED }

    private enum CallBehavior { NONE, ACT_NONE, ACT_CASCADE, ACT_RUN, THROWS }

    private enum ExpectedOutcome { NO_EFFECT, POSTCONDITION, EXCEPTION_PROPAGATED }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"pc=null,call=null", PcKind.NULL, CallBehavior.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,call=azione (ACT_NONE)", PcKind.NULL, CallBehavior.ACT_NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,call=azione (ACT_CASCADE)", PcKind.NULL, CallBehavior.ACT_CASCADE, ExpectedOutcome.NO_EFFECT},
            {"pc=null,call=azione (ACT_RUN)", PcKind.NULL, CallBehavior.ACT_RUN, ExpectedOutcome.NO_EFFECT},
            {"pc=null,call=lancia eccezione", PcKind.NULL, CallBehavior.THROWS, ExpectedOutcome.NO_EFFECT},

            {"pc=non gestito,call=null", PcKind.UNMANAGED, CallBehavior.NONE, ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,call=azione (ACT_NONE)", PcKind.UNMANAGED, CallBehavior.ACT_NONE,
                ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,call=azione (ACT_CASCADE)", PcKind.UNMANAGED, CallBehavior.ACT_CASCADE,
                ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,call=azione (ACT_RUN)", PcKind.UNMANAGED, CallBehavior.ACT_RUN,
                ExpectedOutcome.NO_EFFECT},
            {"pc=non gestito,call=lancia eccezione", PcKind.UNMANAGED, CallBehavior.THROWS,
                ExpectedOutcome.EXCEPTION_PROPAGATED},

            {"pc=gestito non persistito,call=null", PcKind.MANAGED_NEW, CallBehavior.NONE,
                ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,call=azione (ACT_NONE)", PcKind.MANAGED_NEW, CallBehavior.ACT_NONE,
                ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,call=azione (ACT_CASCADE)", PcKind.MANAGED_NEW, CallBehavior.ACT_CASCADE,
                ExpectedOutcome.NO_EFFECT},
            {"pc=gestito non persistito,call=azione (ACT_RUN)", PcKind.MANAGED_NEW, CallBehavior.ACT_RUN,
                ExpectedOutcome.POSTCONDITION},
            {"pc=gestito non persistito,call=lancia eccezione", PcKind.MANAGED_NEW, CallBehavior.THROWS,
                ExpectedOutcome.EXCEPTION_PROPAGATED},

            {"pc=gestito persistito,call=null", PcKind.MANAGED_STORED, CallBehavior.NONE,
                ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,call=azione (ACT_NONE)", PcKind.MANAGED_STORED, CallBehavior.ACT_NONE,
                ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,call=azione (ACT_CASCADE)", PcKind.MANAGED_STORED, CallBehavior.ACT_CASCADE,
                ExpectedOutcome.NO_EFFECT},
            {"pc=gestito persistito,call=azione (ACT_RUN)", PcKind.MANAGED_STORED, CallBehavior.ACT_RUN,
                ExpectedOutcome.POSTCONDITION},
            {"pc=gestito persistito,call=lancia eccezione", PcKind.MANAGED_STORED, CallBehavior.THROWS,
                ExpectedOutcome.EXCEPTION_PROPAGATED},
        });
    }

    private final PcKind pc;
    private final CallBehavior callBehavior;
    private final ExpectedOutcome expected;

    private BrokerImpl broker;

    public Lock2Test(String label, PcKind pc, CallBehavior callBehavior, ExpectedOutcome expected) {
        this.pc = pc;
        this.callBehavior = callBehavior;
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
        FetchConfiguration fc = broker.getFetchConfiguration();
        when(fc.getWriteLockLevel()).thenReturn(LockLevels.LOCK_READ);
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

    @Test
    public void testLockObjCallbacks() {
        Object pcValue = buildPc();

        OpCallbacks callback = null;
        RuntimeException processArgumentException = null;
        if (callBehavior != CallBehavior.NONE) {
            callback = mock(OpCallbacks.class);
            if (callBehavior == CallBehavior.THROWS) {
                processArgumentException = new UserException("processArgument: errore simulato");
                when(callback.processArgument(anyInt(), any(), any())).thenThrow(processArgumentException);
            } else {
                int act;
                switch (callBehavior) {
                    case ACT_CASCADE:
                        act = OpCallbacks.ACT_CASCADE;
                        break;
                    case ACT_RUN:
                        act = OpCallbacks.ACT_RUN;
                        break;
                    default:
                        act = OpCallbacks.ACT_NONE;
                }
                when(callback.processArgument(anyInt(), any(), any())).thenReturn(act);
            }
        }

        if (expected == ExpectedOutcome.EXCEPTION_PROPAGATED) {
            try {
                broker.lock(pcValue, callback);
                fail("attesa la propagazione dell'eccezione di processArgument");
            } catch (Exception e) {
                assertSame(processArgumentException, e);
            }
        } else {
            broker.lock(pcValue, callback);
            if (expected == ExpectedOutcome.NO_EFFECT) {
                assertEquals(LockLevels.LOCK_NONE, broker.getLockLevel(pcValue));
            } else {
                assertEquals(LockLevels.LOCK_READ, broker.getLockLevel(pcValue));
            }
        }
    }
}
