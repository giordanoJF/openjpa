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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
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
import org.apache.openjpa.meta.MetaDataRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NewInstanceTest {

    // NULL = cls null;
    // INTERFACE_BEANS/INTERFACE_NO_BEANS = interfaccia con/senza metodi JavaBeans;
    // ABSTRACT_BEANS/ABSTRACT_NO_BEANS = classe astratta con/senza metodi JavaBeans;
    // MANAGED_CONCRETE = tipo managed concreto;
    // UNMANAGED_CONCRETE = tipo non managed concreto.
    private enum ClsKind {
        NULL, INTERFACE_BEANS, INTERFACE_NO_BEANS, ABSTRACT_BEANS, ABSTRACT_NO_BEANS,
        MANAGED_CONCRETE, UNMANAGED_CONCRETE
    }

    private enum ExpectedOutcome {
        EXCEPTION_UNSPECIFIED,
        NON_NULL_INSTANCE
    }

    private interface JavaBeansInterface {
        String getFoo();
        void setFoo(String v);
    }

    private interface PlainInterface {
        void doSomething();
    }

    private abstract static class JavaBeansAbstractClass {
        abstract String getFoo();
        abstract void setFoo(String v);
    }

    private abstract static class PlainAbstractClass {
        abstract void doSomething();
    }

    private static final class ManagedConcreteClass { }

    private static final class UnmanagedConcreteClass { }

    private final ClsKind clsKind;
    private final ExpectedOutcome expected;

    private BrokerImpl broker;
    private MetaDataRepository repo;

    public NewInstanceTest(String label, ClsKind clsKind, ExpectedOutcome expected) {
        this.clsKind = clsKind;
        this.expected = expected;
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"cls=null", ClsKind.NULL, ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"cls=interfaccia con JavaBeans", ClsKind.INTERFACE_BEANS, ExpectedOutcome.NON_NULL_INSTANCE},
            {"cls=interfaccia senza JavaBeans", ClsKind.INTERFACE_NO_BEANS, ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"cls=classe astratta con JavaBeans", ClsKind.ABSTRACT_BEANS, ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"cls=classe astratta senza JavaBeans", ClsKind.ABSTRACT_NO_BEANS, ExpectedOutcome.EXCEPTION_UNSPECIFIED},
            {"cls=tipo managed concreto", ClsKind.MANAGED_CONCRETE, ExpectedOutcome.NON_NULL_INSTANCE},
            {"cls=tipo non managed concreto", ClsKind.UNMANAGED_CONCRETE, ExpectedOutcome.EXCEPTION_UNSPECIFIED},
        });
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
    }

    private void registerManageable(Class<?> cls) {
        PersistenceCapable pcPrototype = mock(PersistenceCapable.class, RETURNS_DEEP_STUBS);
        PCRegistry.register(cls, new String[0], new Class<?>[0], new byte[0], null, cls.getSimpleName(), pcPrototype);

        ClassMetaData meta = mock(ClassMetaData.class, RETURNS_DEEP_STUBS);
        when(repo.getMetaData(eq(cls), any(), anyBoolean())).thenReturn(meta);
    }

    private Class<?> buildCls() {
        switch (clsKind) {
            case NULL:
                return null;
            case INTERFACE_BEANS:
                registerManageable(JavaBeansInterface.class);
                return JavaBeansInterface.class;
            case INTERFACE_NO_BEANS:
                // non registrata in PCRegistry: nessun percorso di enhancement plausibile per
                // un'interfaccia priva di metodi in stile JavaBeans, coerente con l'assenza di
                // qualunque fonte pubblica che descriva un meccanismo diverso.
                return PlainInterface.class;
            case ABSTRACT_BEANS:
                // BrokerImpl.newInstance rifiuta ogni classe astratta (non interfaccia) prima di
                // consultare PCRegistry/MetaDataRepository: nessuna registrazione necessaria.
                return JavaBeansAbstractClass.class;
            case ABSTRACT_NO_BEANS:
                return PlainAbstractClass.class;
            case MANAGED_CONCRETE:
                registerManageable(ManagedConcreteClass.class);
                return ManagedConcreteClass.class;
            case UNMANAGED_CONCRETE:
                return UnmanagedConcreteClass.class;
            default:
                throw new IllegalStateException();
        }
    }

    @Test
    public void testNewInstance() {
        Class<?> clsValue = buildCls();

        if (expected == ExpectedOutcome.EXCEPTION_UNSPECIFIED) {
            try {
                broker.newInstance(clsValue);
                fail("attesa un'eccezione");
            } catch (Exception e) {
                // eccezione attesa, tipo non specificato dal design
            }
        } else {
            Object result = broker.newInstance(clsValue);
            assertNotNull(result);
            assertTrue(clsValue.isInstance(result) || result instanceof PersistenceCapable);
        }
    }
}
