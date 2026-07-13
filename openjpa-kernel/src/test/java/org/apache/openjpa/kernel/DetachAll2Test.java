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

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

// Iterazione di miglioramento su detachAll(OpCallbacks, boolean) (Capitolo Adeguatezza, appendice
// report/data/detachAll2_combinazioni.csv). Riusa l'infrastruttura di setup di DetachAllTest.
@RunWith(Parameterized.class)
public class DetachAll2Test {

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"call=null,flush=true,transazione=non attiva", false, true, false},
            {"call=null,flush=true,transazione=attiva", false, true, true},
            {"call=null,flush=false,transazione=non attiva", false, false, false},
            {"call=null,flush=false,transazione=attiva", false, false, true},
            {"call=non null,flush=true,transazione=non attiva", true, true, false},
            {"call=non null,flush=true,transazione=attiva", true, true, true},
            {"call=non null,flush=false,transazione=non attiva", true, false, false},
            {"call=non null,flush=false,transazione=attiva", true, false, true},
        });
    }

    private final boolean call;
    private final boolean flush;
    private final boolean activeTransaction;

    private BrokerImpl broker;

    public DetachAll2Test(String label, boolean call, boolean flush, boolean activeTransaction) {
        this.call = call;
        this.flush = flush;
        this.activeTransaction = activeTransaction;
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
        if (activeTransaction) {
            broker.begin();
        }
    }

    @Test
    public void testDetachAllFlush() {
        OpCallbacks callback = null;
        if (call) {
            callback = mock(OpCallbacks.class);
            when(callback.processArgument(anyInt(), any(), any())).thenReturn(OpCallbacks.ACT_RUN);
        }

        // broker senza istanze gestite: caso base per isolare l'effetto di flush/call/transazione,
        // nessuna eccezione attesa in nessuna delle 8 combinazioni.
        broker.detachAll(callback, flush);

        // ipotesi da verificare con l'esecuzione, non dedotta dal codice: con nessuna istanza
        // gestita, il callback viene comunque invocato?
        if (callback != null) {
            verify(callback, never()).processArgument(anyInt(), any(), any());
        }
    }
}
