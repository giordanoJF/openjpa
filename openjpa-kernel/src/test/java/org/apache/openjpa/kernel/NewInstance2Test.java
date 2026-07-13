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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.meta.MetaDataRepository;
import org.junit.Before;
import org.junit.Test;

// Iterazione mutation testing (Capitolo Adeguatezza): rafforza l'oracolo di newInstance(Class)
// per il caso "tipo non managed concreto", gia' testato in NewInstanceTest con un'eccezione
// generica. Javadoc: "@throws IllegalArgumentException if cls is not a managed type or
// interface" -- oracolo piu' preciso, dedotto dalla stessa Javadoc gia' letta per il capitolo 3,
// non dal codice. Appendice report/data/newInstance2_combinazioni.csv.
public class NewInstance2Test {

    private static final class UnmanagedConcreteClass2 { }

    private BrokerImpl broker;

    @Before
    public void setUp() {
        AbstractBrokerFactory factory = mock(AbstractBrokerFactory.class, RETURNS_DEEP_STUBS);
        StoreManager storeManager = mock(StoreManager.class, RETURNS_DEEP_STUBS);
        DelegatingStoreManager delegatingStoreManager = new DelegatingStoreManager(storeManager) { };

        MetaDataRepository repo = mock(MetaDataRepository.class, RETURNS_DEEP_STUBS);

        OpenJPAConfiguration conf = mock(OpenJPAConfiguration.class, RETURNS_DEEP_STUBS);
        when(conf.getMetaDataRepositoryInstance()).thenReturn(repo);
        when(factory.getConfiguration()).thenReturn(conf);

        broker = new BrokerImpl();
        broker.initialize(factory, delegatingStoreManager, false, ConnectionRetainModes.CONN_RETAIN_DEMAND, false);
    }

    @Test
    public void testNewInstanceUnmanagedConcreteThrowsIllegalArgumentException() {
        try {
            broker.newInstance(UnmanagedConcreteClass2.class);
            fail("attesa IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // oracolo confermato: tipo di eccezione dichiarato dalla Javadoc
        }
    }
}
