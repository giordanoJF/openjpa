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

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RandoopErrorTest {

    public static boolean debug = false;

    @Ignore("error-revealing: NullPointerException dovuta a initialize() mai chiamato, comportamento atteso")
    @Test
    public void test01() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RandoopErrorTest.test01");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        java.lang.Object obj1 = brokerImpl0.clone();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        brokerImpl0.setOptimistic(true);
    }

    @Ignore("error-revealing: NullPointerException dovuta a initialize() mai chiamato, comportamento atteso")
    @Test
    public void test02() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RandoopErrorTest.test02");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        brokerImpl0.setTransactionListenerCallbackMode(0);
    }

    @Ignore("error-revealing: NullPointerException dovuta a initialize() mai chiamato, comportamento atteso")
    @Test
    public void test03() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RandoopErrorTest.test03");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        brokerImpl0.removeTransactionListener((java.lang.Object) 8);
    }

    @Ignore("error-revealing: NullPointerException dovuta a initialize() mai chiamato, comportamento atteso")
    @Test
    public void test04() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RandoopErrorTest.test04");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        java.lang.Object obj1 = brokerImpl0.clone();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        boolean boolean2 = brokerImpl0.getCacheFinderQuery();
    }

    @Ignore("error-revealing: NullPointerException dovuta a initialize() mai chiamato, comportamento atteso")
    @Test
    public void test05() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RandoopErrorTest.test05");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        boolean boolean2 = brokerImpl0.getCacheFinderQuery();
    }

    @Ignore("error-revealing: NullPointerException dovuta a initialize() mai chiamato, comportamento atteso")
    @Test
    public void test06() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RandoopErrorTest.test06");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration3 = brokerImpl0.pushFetchConfiguration();
    }

    @Ignore("error-revealing: NullPointerException dovuta a initialize() mai chiamato, comportamento atteso")
    @Test
    public void test07() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RandoopErrorTest.test07");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        brokerImpl0.setLifecycleListenerCallbackMode(8);
    }

    @Ignore("error-revealing: NullPointerException dovuta a initialize() mai chiamato, comportamento atteso")
    @Test
    public void test08() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RandoopErrorTest.test08");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        java.lang.Object obj1 = brokerImpl0.clone();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        java.util.Map<java.lang.String, java.lang.Object> strMap2 = brokerImpl0.getProperties();
    }

    @Ignore("error-revealing: NullPointerException dovuta a initialize() mai chiamato, comportamento atteso")
    @Test
    public void test09() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RandoopErrorTest.test09");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setConnectionFactoryName("hi!");
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        brokerImpl0.close();
    }

    @Ignore("error-revealing: NullPointerException dovuta a initialize() mai chiamato, comportamento atteso")
    @Test
    public void test10() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RandoopErrorTest.test10");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setDetachState(1);
        brokerImpl0.setOptimistic(false);
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        brokerImpl0.rollbackAndResume();
    }

    @Ignore("error-revealing: NullPointerException dovuta a initialize() mai chiamato, comportamento atteso")
    @Test
    public void test11() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RandoopErrorTest.test11");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setConnectionFactoryName("hi!");
        java.lang.String str5 = brokerImpl0.getConnectionFactoryName();
        boolean boolean6 = brokerImpl0.getIgnoreChanges();
        brokerImpl0.preFlush();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        java.util.Collection collection8 = brokerImpl0.getDirtyObjects();
    }

    @Ignore("error-revealing: NullPointerException dovuta a initialize() mai chiamato, comportamento atteso")
    @Test
    public void test12() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RandoopErrorTest.test12");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        boolean boolean3 = brokerImpl0.isNew((java.lang.Object) 100.0f);
        brokerImpl0.setDetachedNew(false);
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        java.util.Collection<java.lang.Object> objCollection6 = brokerImpl0.getTransactionListeners();
    }
}

