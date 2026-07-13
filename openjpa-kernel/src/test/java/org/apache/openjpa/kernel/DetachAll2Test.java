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
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.PCRegistry;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DetachAll2Test {

    private enum ExpectedOutcome { NO_EFFECT, THROWS_INVALID_STATE }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"call=null,flush=true,transazione=non attiva", false, true, false, false, ExpectedOutcome.NO_EFFECT},
            {"call=null,flush=true,transazione=attiva", false, true, true, false, ExpectedOutcome.NO_EFFECT},
            {"call=null,flush=false,transazione=non attiva", false, false, false, false, ExpectedOutcome.NO_EFFECT},
            {"call=null,flush=false,transazione=attiva", false, false, true, false, ExpectedOutcome.NO_EFFECT},
            {"call=non null,flush=true,transazione=non attiva", true, true, false, false, ExpectedOutcome.NO_EFFECT},
            {"call=non null,flush=true,transazione=attiva", true, true, true, false, ExpectedOutcome.NO_EFFECT},
            {"call=non null,flush=false,transazione=non attiva", true, false, false, false,
                ExpectedOutcome.NO_EFFECT},
            {"call=non null,flush=false,transazione=attiva", true, false, true, false, ExpectedOutcome.NO_EFFECT},

            {"call=null,flush=false,broker con istanza gestita", false, false, true, true,
                ExpectedOutcome.THROWS_INVALID_STATE},
            {"call=non null,flush=false,broker con istanza gestita", true, false, true, true,
                ExpectedOutcome.THROWS_INVALID_STATE},
        });
    }

    private final boolean call;
    private final boolean flush;
    private final boolean activeTransaction;
    private final boolean hasManagedInstance;
    private final ExpectedOutcome expected;

    private BrokerImpl broker;
    private MetaDataRepository repo;

    public DetachAll2Test(String label, boolean call, boolean flush, boolean activeTransaction,
        boolean hasManagedInstance, ExpectedOutcome expected) {
        this.call = call;
        this.flush = flush;
        this.activeTransaction = activeTransaction;
        this.hasManagedInstance = hasManagedInstance;
        this.expected = expected;
    }

    @Before
    public void setUp() {
        AbstractBrokerFactory factory = mock(AbstractBrokerFactory.class, RETURNS_DEEP_STUBS);
        StoreManager storeManager = mock(StoreManager.class, RETURNS_DEEP_STUBS);
        DelegatingStoreManager delegatingStoreManager = new DelegatingStoreManager(storeManager) { };

        Compatibility compatibility = new Compatibility();
        compatibility.setCopyOnDetach(true);

        repo = mock(MetaDataRepository.class, RETURNS_DEEP_STUBS);

        OpenJPAConfiguration conf = mock(OpenJPAConfiguration.class, RETURNS_DEEP_STUBS);
        when(conf.getCompatibilityInstance()).thenReturn(compatibility);
        when(conf.getMetaDataRepositoryInstance()).thenReturn(repo);
        when(factory.getConfiguration()).thenReturn(conf);

        broker = new BrokerImpl();
        broker.initialize(factory, delegatingStoreManager, false, ConnectionRetainModes.CONN_RETAIN_DEMAND, false);
        if (activeTransaction) {
            broker.begin();
        }
    }

    private void persistManagedInstance() {
        PersistenceCapable pc = mock(PersistenceCapable.class);
        when(pc.pcGetStateManager()).thenReturn(null);
        when(pc.pcGetGenericContext()).thenReturn(null);
        when(pc.pcIsDetached()).thenReturn(Boolean.FALSE);

        ClassMetaData meta = mock(ClassMetaData.class, RETURNS_DEEP_STUBS);
        when(meta.getIdentityType()).thenReturn(ClassMetaData.ID_DATASTORE);
        when(meta.getFields()).thenReturn(new org.apache.openjpa.meta.FieldMetaData[0]);
        when(meta.getPkAndNonPersistentManagedFmdIndexes()).thenReturn(new int[0]);
        when(meta.getPCSubclasses()).thenReturn(new Class<?>[0]);
        doReturn(meta).when(repo).getMetaData(eq(pc.getClass()), any(), anyBoolean());
        PCRegistry.register(pc.getClass(), new String[0], new Class<?>[0], new byte[0], null,
            "DetachAll2ManagedMock", pc);

        broker.persist(pc, null, null);
    }

    @Test
    public void testDetachAllFlush() {
        if (hasManagedInstance) {
            persistManagedInstance();
        }

        OpCallbacks callback = null;
        if (call) {
            callback = mock(OpCallbacks.class);
            when(callback.processArgument(anyInt(), any(), any())).thenReturn(OpCallbacks.ACT_RUN);
        }

        if (expected == ExpectedOutcome.THROWS_INVALID_STATE) {
            try {
                broker.detachAll(callback, flush);
                org.junit.Assert.fail("attesa InvalidStateException");
            } catch (org.apache.openjpa.util.InvalidStateException e) {
            }
            if (callback != null) {
                verify(callback, never()).processArgument(anyInt(), any(), any());
            }
            return;
        }

        broker.detachAll(callback, flush);

        if (callback != null) {
            verify(callback, never()).processArgument(anyInt(), any(), any());
        }
    }
}
