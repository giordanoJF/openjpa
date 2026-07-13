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

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

// Iterazione 2 (nuovo metodo, Capitolo Adeguatezza): flush(), scelto per significativita'
// funzionale (una delle operazioni JPA fondamentali) tra i metodi segnalati da JaCoCo come
// scoperti. Appendice report/data/flush_combinazioni.csv. Non copre il ramo di errore dello
// store (documentato in Javadoc: "may set the rollback only flag... if it encounters an
// error"): richiederebbe una configurazione dello StoreManager mock non banale, fuori budget
// per questa iterazione; resta un'estensione futura, non un'assunzione dedotta dal codice.
@RunWith(Parameterized.class)
public class FlushTest {

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"nessuna transazione attiva", false},
            {"transazione attiva senza istanze dirty", true},
        });
    }

    private final boolean activeTransaction;

    private BrokerImpl broker;

    public FlushTest(String label, boolean activeTransaction) {
        this.activeTransaction = activeTransaction;
    }

    @Test
    public void testFlush() {
        AbstractBrokerFactory factory = mock(AbstractBrokerFactory.class, RETURNS_DEEP_STUBS);
        StoreManager storeManager = mock(StoreManager.class, RETURNS_DEEP_STUBS);
        DelegatingStoreManager delegatingStoreManager = new DelegatingStoreManager(storeManager) { };

        OpenJPAConfiguration conf = mock(OpenJPAConfiguration.class, RETURNS_DEEP_STUBS);
        when(conf.supportedOptions()).thenReturn(Collections.singleton(OpenJPAConfiguration.OPTION_INC_FLUSH));
        when(factory.getConfiguration()).thenReturn(conf);

        broker = new BrokerImpl();
        broker.initialize(factory, delegatingStoreManager, false, ConnectionRetainModes.CONN_RETAIN_DEMAND, false);
        if (activeTransaction) {
            broker.begin();
        }

        // nessuna eccezione attesa in entrambi i casi (oracolo da Javadoc)
        broker.flush();
        assertFalse(broker.getRollbackOnly());
    }
}
