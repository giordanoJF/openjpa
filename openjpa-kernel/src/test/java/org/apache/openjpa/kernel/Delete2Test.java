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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
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

// Iterazione mutation testing (Capitolo Adeguatezza): nuova categoria per pc in delete(Object,
// OpCallbacks), "gestito e detached", riusata dalla categoria "detached" gia' stabilita dalla
// Javadoc di isDetached() (Capitolo 3). Ipotesi non verificata sul codice di delete, solo per
// convenzione generale JPA. Appendice report/data/delete2_combinazioni.csv.
@RunWith(Parameterized.class)
public class Delete2Test {

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"pc=gestito e detached,call=null", false},
            {"pc=gestito e detached,call=non null", true},
        });
    }

    private final boolean call;

    private BrokerImpl broker;

    public Delete2Test(String label, boolean call) {
        this.call = call;
    }

    @Before
    public void setUp() {
        AbstractBrokerFactory factory = mock(AbstractBrokerFactory.class, RETURNS_DEEP_STUBS);
        StoreManager storeManager = mock(StoreManager.class, RETURNS_DEEP_STUBS);
        DelegatingStoreManager delegatingStoreManager = new DelegatingStoreManager(storeManager) { };

        OpenJPAConfiguration conf = mock(OpenJPAConfiguration.class, RETURNS_DEEP_STUBS);
        when(factory.getConfiguration()).thenReturn(conf);

        broker = new BrokerImpl();
        broker.initialize(factory, delegatingStoreManager, false, ConnectionRetainModes.CONN_RETAIN_DEMAND, false);
        broker.begin();
    }

    @Test
    public void testDeleteDetached() {
        StateManagerImpl sm = mock(StateManagerImpl.class, RETURNS_DEEP_STUBS);
        when(sm.isPersistent()).thenReturn(true);
        when(sm.isDetached()).thenReturn(true);
        PersistenceCapable pcValue = mock(PersistenceCapable.class);
        when(pcValue.pcGetStateManager()).thenReturn(sm);
        when(pcValue.pcGetGenericContext()).thenReturn(broker);

        OpCallbacks callback = null;
        if (call) {
            callback = mock(OpCallbacks.class);
            when(callback.processArgument(anyInt(), any(), any())).thenReturn(OpCallbacks.ACT_RUN);
        }

        // ipotesi confermata dall'esecuzione (non dedotta dal codice di delete): UserException
        // "You cannot perform operation delete on detached object...".
        try {
            broker.delete(pcValue, callback);
            fail("attesa un'eccezione per istanza detached");
        } catch (UserException e) {
            assertTrue(e.getMessage().contains("detached"));
        }
    }
}
