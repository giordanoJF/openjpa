import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RegressionTest0 {

    public static boolean debug = false;

    @Test
    public void test01() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test01");
        java.lang.String str0 = org.apache.openjpa.kernel.BrokerImpl.StateManagerId.STRING_PREFIX;
        org.junit.Assert.assertEquals("'" + str0 + "' != '" + "openjpasm:" + "'", str0, "openjpasm:");
    }

    @Test
    public void test02() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test02");
        int int0 = org.apache.openjpa.kernel.LockLevels.LOCK_WRITE;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 20 + "'", int0 == 20);
    }

    @Test
    public void test03() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test03");
        int int0 = org.apache.openjpa.kernel.StoreContext.OID_NODELETED;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 4 + "'", int0 == 4);
    }

    @Test
    public void test04() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test04");
        int int0 = org.apache.openjpa.kernel.DetachState.DETACH_FGS;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 0 + "'", int0 == 0);
    }

    @Test
    public void test05() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test05");
        int int0 = org.apache.openjpa.event.CallbackModes.CALLBACK_IGNORE;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 4 + "'", int0 == 4);
    }

    @Test
    public void test06() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test06");
        int[] intArray0 = org.apache.openjpa.kernel.AutoDetach.values;
        org.junit.Assert.assertNotNull(intArray0);
        org.junit.Assert.assertArrayEquals(intArray0, new int[] { 2, 4, 8, 16, 32 });
    }

    @Test
    public void test07() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test07");
        int int0 = org.apache.openjpa.kernel.StoreContext.OID_COPY;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 8 + "'", int0 == 8);
    }

    @Test
    public void test08() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test08");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        java.lang.Class class2 = null;
        java.lang.Class[] classArray3 = new java.lang.Class[] { class2 };
        // The following exception was thrown during execution in test generation
        try {
            brokerImpl0.addLifecycleListener((java.lang.Object) '4', classArray3);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.event.LifecycleEventManager.addListener(Object, java.lang.Class[])\" because \"this._lifeEventManager\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(classArray3);
        org.junit.Assert.assertArrayEquals(classArray3, new java.lang.Class[] { null });
    }

    @Test
    public void test09() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test09");
        int int0 = org.apache.openjpa.kernel.AutoClear.CLEAR_DATASTORE;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 0 + "'", int0 == 0);
    }

    @Test
    public void test10() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test10");
        int int0 = org.apache.openjpa.kernel.DetachState.DETACH_ALL;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 2 + "'", int0 == 2);
    }

    @Test
    public void test11() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test11");
        int int0 = org.apache.openjpa.kernel.RestoreState.RESTORE_IMMUTABLE;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 1 + "'", int0 == 1);
    }

    @Test
    public void test12() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test12");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        java.lang.Class class3 = null;
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration5 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Iterator iterator7 = brokerImpl0.extentIterator(class3, false, fetchConfiguration5, true);
            org.junit.Assert.fail("Expected exception of type org.apache.openjpa.util.GeneralException; message: Cannot invoke \"org.apache.openjpa.kernel.FetchConfiguration.clone()\" because the return value of \"org.apache.openjpa.kernel.Broker.getFetchConfiguration()\" is null");
        } catch (org.apache.openjpa.util.GeneralException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test13() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test13");
        java.util.BitSet bitSet0 = org.apache.openjpa.kernel.StoreContext.EXCLUDE_ALL;
        org.junit.Assert.assertNotNull(bitSet0);
        org.junit.Assert.assertEquals(bitSet0.toString(), "{}");
    }

    @Test
    public void test14() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test14");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setConnectionFactoryName("hi!");
        org.apache.openjpa.kernel.AbstractBrokerFactory abstractBrokerFactory5 = null;
        org.apache.openjpa.kernel.DelegatingStoreManager delegatingStoreManager6 = null;
        // The following exception was thrown during execution in test generation
        try {
            brokerImpl0.initialize(abstractBrokerFactory5, delegatingStoreManager6, true, (int) (byte) -1, false);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.kernel.AbstractBrokerFactory.getConfiguration()\" because \"factory\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test15() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test15");
        int int0 = org.apache.openjpa.kernel.DetachState.DETACH_FETCH_GROUPS;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 0 + "'", int0 == 0);
    }

    @Test
    public void test16() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test16");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        java.lang.Object obj1 = brokerImpl0.clone();
        java.util.Collection collection2 = null;
        org.apache.openjpa.kernel.OpCallbacks opCallbacks3 = null;
        // The following exception was thrown during execution in test generation
        try {
            brokerImpl0.lockAll(collection2, opCallbacks3);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.util.Collection.isEmpty()\" because \"objs\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(obj1);
    }

    @Test
    public void test17() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test17");
        int int0 = org.apache.openjpa.event.CallbackModes.CALLBACK_LOG;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 8 + "'", int0 == 8);
    }

    @Test
    public void test18() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test18");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setDetachState(1);
        java.lang.Class class6 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.openjpa.kernel.Query query8 = brokerImpl0.newQuery("", class6, (java.lang.Object) 20);
            org.junit.Assert.fail("Expected exception of type org.apache.openjpa.util.GeneralException; message: Cannot invoke \"org.apache.openjpa.kernel.DelegatingStoreManager.newQuery(String)\" because \"this._store\" is null");
        } catch (org.apache.openjpa.util.GeneralException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test19() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test19");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setDetachState(1);
        java.lang.Class class5 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.lang.Object obj6 = brokerImpl0.newInstance(class5);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.lang.Class.isInterface()\" because \"cls\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test20() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test20");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setDetachState(1);
        brokerImpl0.setOptimistic(false);
        // The following exception was thrown during execution in test generation
        try {
            brokerImpl0.rollbackToSavepoint();
            org.junit.Assert.fail("Expected exception of type org.apache.openjpa.util.UserException; message: Cannot rollback/release last savepoint as no savepoint has been set.");
        } catch (org.apache.openjpa.util.UserException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test21() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test21");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        java.lang.Object obj1 = brokerImpl0.clone();
        java.lang.Class class2 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.openjpa.kernel.Extent extent4 = brokerImpl0.newExtent(class2, false);
            org.junit.Assert.fail("Expected exception of type org.apache.openjpa.util.GeneralException; message: Cannot invoke \"org.apache.openjpa.kernel.FetchConfiguration.clone()\" because the return value of \"org.apache.openjpa.kernel.Broker.getFetchConfiguration()\" is null");
        } catch (org.apache.openjpa.util.GeneralException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(obj1);
    }

    @Test
    public void test22() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test22");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setDetachState(1);
        brokerImpl0.setOptimistic(false);
        // The following exception was thrown during execution in test generation
        try {
            org.apache.openjpa.kernel.Query query9 = brokerImpl0.newQuery("hi!", (java.lang.Object) (byte) -1);
            org.junit.Assert.fail("Expected exception of type org.apache.openjpa.util.GeneralException; message: Cannot invoke \"org.apache.openjpa.kernel.DelegatingStoreManager.newQuery(String)\" because \"this._store\" is null");
        } catch (org.apache.openjpa.util.GeneralException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test23() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test23");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        boolean boolean3 = brokerImpl0.isNew((java.lang.Object) 100.0f);
        java.util.Collection collection4 = null;
        org.apache.openjpa.kernel.OpCallbacks opCallbacks5 = null;
        // The following exception was thrown during execution in test generation
        try {
            brokerImpl0.nontransactionalAll(collection4, opCallbacks5);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.util.Collection.iterator()\" because \"objs\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNull(fetchConfiguration1);
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + false + "'", boolean3 == false);
    }

    @Test
    public void test24() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test24");
        int int0 = org.apache.openjpa.kernel.ConnectionRetainModes.CONN_RETAIN_ALWAYS;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 2 + "'", int0 == 2);
    }

    @Test
    public void test25() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test25");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setDetachState(1);
        brokerImpl0.setOptimistic(false);
        boolean boolean7 = brokerImpl0.getOrderDirtyObjects();
        int[] intArray9 = org.apache.openjpa.kernel.AutoDetach.values;
        org.apache.openjpa.kernel.BrokerImpl brokerImpl17 = new org.apache.openjpa.kernel.BrokerImpl();
        java.lang.Object obj18 = brokerImpl17.clone();
        org.apache.openjpa.kernel.BrokerImpl brokerImpl20 = new org.apache.openjpa.kernel.BrokerImpl();
        java.lang.Object obj21 = brokerImpl20.clone();
        org.apache.openjpa.kernel.BrokerImpl brokerImpl25 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration26 = brokerImpl25.getFetchConfiguration();
        org.apache.openjpa.kernel.BrokerImpl brokerImpl29 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration30 = brokerImpl29.getFetchConfiguration();
        org.apache.openjpa.meta.ClassMetaData classMetaData31 = null;
        org.apache.openjpa.kernel.Seq seq32 = brokerImpl29.getIdentitySequence(classMetaData31);
        org.apache.openjpa.kernel.BrokerImpl brokerImpl43 = new org.apache.openjpa.kernel.BrokerImpl();
        java.lang.Object obj44 = brokerImpl43.clone();
        org.apache.openjpa.kernel.BrokerImpl brokerImpl45 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration46 = brokerImpl45.getFetchConfiguration();
        boolean boolean48 = brokerImpl45.isNew((java.lang.Object) 100.0f);
        java.util.BitSet bitSet52 = org.apache.openjpa.kernel.StoreContext.EXCLUDE_ALL;
        org.apache.openjpa.kernel.BrokerImpl brokerImpl56 = new org.apache.openjpa.kernel.BrokerImpl();
        java.lang.Object[] objArray58 = new java.lang.Object[] { (-1), intArray9, '#', "hi!", 1, (-1.0d), 10L, 10.0d, (-1.0f), obj18, 100, obj21, true, 10.0d, 8, fetchConfiguration26, (short) 100, 4, seq32, 1.0f, 0.0d, 0.0f, 1.0f, 10.0f, 100L, 4, "hi!", '#', 20, brokerImpl43, 100.0f, 100.0d, 10.0d, 4, bitSet52, "", true, (short) 1, brokerImpl56, 0L };
        java.util.ArrayList<java.lang.Object> objList59 = new java.util.ArrayList<java.lang.Object>();
        boolean boolean60 = java.util.Collections.addAll((java.util.Collection<java.lang.Object>) objList59, objArray58);
        // The following exception was thrown during execution in test generation
        try {
            boolean boolean61 = brokerImpl0.isCached((java.util.List<java.lang.Object>) objList59);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.kernel.ManagedCache.getById(Object, boolean)\" because \"this._cache\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + false + "'", boolean7 == false);
        org.junit.Assert.assertNotNull(intArray9);
        org.junit.Assert.assertArrayEquals(intArray9, new int[] { 2, 4, 8, 16, 32 });
        org.junit.Assert.assertNotNull(obj18);
        org.junit.Assert.assertNotNull(obj21);
        org.junit.Assert.assertNull(fetchConfiguration26);
        org.junit.Assert.assertNull(fetchConfiguration30);
        org.junit.Assert.assertNull(seq32);
        org.junit.Assert.assertNotNull(obj44);
        org.junit.Assert.assertNull(fetchConfiguration46);
        org.junit.Assert.assertTrue("'" + boolean48 + "' != '" + false + "'", boolean48 == false);
        org.junit.Assert.assertNotNull(bitSet52);
        org.junit.Assert.assertEquals(bitSet52.toString(), "{}");
        org.junit.Assert.assertNotNull(objArray58);
        org.junit.Assert.assertTrue("'" + boolean60 + "' != '" + true + "'", boolean60 == true);
    }

    @Test
    public void test26() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test26");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        boolean boolean3 = brokerImpl0.isNew((java.lang.Object) 100.0f);
        java.lang.Object obj4 = brokerImpl0.getConnectionFactory();
        org.junit.Assert.assertNull(fetchConfiguration1);
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + false + "'", boolean3 == false);
        org.junit.Assert.assertNull(obj4);
    }

    @Test
    public void test27() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test27");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setDetachState(1);
        brokerImpl0.setOptimistic(false);
        org.apache.openjpa.kernel.OpCallbacks opCallbacks9 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.lang.Object obj10 = brokerImpl0.attach((java.lang.Object) "openjpasm:", false, opCallbacks9);
            org.junit.Assert.fail("Expected exception of type org.apache.openjpa.util.NoTransactionException; message: To perform this operation, it must be written within a transaction, or your settings must allow nontransactional writes and must not detach all nontransactional reads.");
        } catch (org.apache.openjpa.util.NoTransactionException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test28() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test28");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        java.lang.Class class4 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.openjpa.kernel.Query query6 = brokerImpl0.newQuery("hi!", class4, (java.lang.Object) 1);
            org.junit.Assert.fail("Expected exception of type org.apache.openjpa.util.GeneralException; message: Cannot invoke \"org.apache.openjpa.kernel.DelegatingStoreManager.newQuery(String)\" because \"this._store\" is null");
        } catch (org.apache.openjpa.util.GeneralException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test29() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test29");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setDetachState(1);
        brokerImpl0.setOptimistic(false);
        java.lang.Class class7 = null;
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration9 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Iterator iterator11 = brokerImpl0.extentIterator(class7, true, fetchConfiguration9, false);
            org.junit.Assert.fail("Expected exception of type org.apache.openjpa.util.GeneralException; message: Cannot invoke \"org.apache.openjpa.kernel.FetchConfiguration.clone()\" because the return value of \"org.apache.openjpa.kernel.Broker.getFetchConfiguration()\" is null");
        } catch (org.apache.openjpa.util.GeneralException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test30() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test30");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setConnectionFactoryName("hi!");
        java.lang.String str5 = brokerImpl0.getConnectionFactoryName();
        boolean boolean6 = brokerImpl0.getIgnoreChanges();
        int int7 = brokerImpl0.getRestoreState();
        org.apache.openjpa.kernel.Broker broker8 = brokerImpl0.getBroker();
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "hi!" + "'", str5, "hi!");
        org.junit.Assert.assertTrue("'" + boolean6 + "' != '" + false + "'", boolean6 == false);
        org.junit.Assert.assertTrue("'" + int7 + "' != '" + 1 + "'", int7 == 1);
        org.junit.Assert.assertNotNull(broker8);
    }

    @Test
    public void test31() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test31");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setConnectionFactoryName("hi!");
        java.lang.String str5 = brokerImpl0.getConnectionFactoryName();
        boolean boolean6 = brokerImpl0.getIgnoreChanges();
        brokerImpl0.preFlush();
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "hi!" + "'", str5, "hi!");
        org.junit.Assert.assertTrue("'" + boolean6 + "' != '" + false + "'", boolean6 == false);
    }

    @Test
    public void test32() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test32");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setDetachState(1);
        brokerImpl0.setOptimistic(false);
        // The following exception was thrown during execution in test generation
        try {
            brokerImpl0.rollbackToSavepoint("");
            org.junit.Assert.fail("Expected exception of type org.apache.openjpa.util.NoTransactionException; message: Can only perform operation while a transaction is active.");
        } catch (org.apache.openjpa.util.NoTransactionException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test33() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test33");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setAutoDetach((int) (byte) 1);
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test34() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test34");
        int int0 = org.apache.openjpa.kernel.StoreContext.OID_NOVALIDATE;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 2 + "'", int0 == 2);
    }

    @Test
    public void test35() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test35");
        int int0 = org.apache.openjpa.kernel.AutoDetach.DETACH_NONTXREAD;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 8 + "'", int0 == 8);
    }

    @Test
    public void test36() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test36");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setConnectionFactoryName("hi!");
        brokerImpl0.setPostLoadOnMerge(false);
        boolean boolean7 = brokerImpl0.isActive();
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + false + "'", boolean7 == false);
    }

    @Test
    public void test37() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test37");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        java.lang.Class class1 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.lang.Object obj2 = brokerImpl0.newInstance(class1);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.lang.Class.isInterface()\" because \"cls\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test38() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test38");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setConnectionFactoryName("hi!");
        brokerImpl0.setPostLoadOnMerge(false);
        org.apache.openjpa.kernel.OpCallbacks opCallbacks10 = null;
        // The following exception was thrown during execution in test generation
        try {
            brokerImpl0.lock((java.lang.Object) 100.0f, (int) (short) 0, (-1), opCallbacks10);
            org.junit.Assert.fail("Expected exception of type org.apache.openjpa.util.NoTransactionException; message: Can only perform operation while a transaction is active.");
        } catch (org.apache.openjpa.util.NoTransactionException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test39() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test39");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setConnectionFactoryName("hi!");
        brokerImpl0.setPostLoadOnMerge(false);
        brokerImpl0.setPostLoadOnMerge(true);
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test40() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test40");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setDetachState(1);
        brokerImpl0.setOptimistic(false);
        boolean boolean7 = brokerImpl0.getOrderDirtyObjects();
        org.apache.openjpa.meta.FieldMetaData fieldMetaData8 = null;
        org.apache.openjpa.kernel.Seq seq9 = brokerImpl0.getValueSequence(fieldMetaData8);
        // The following exception was thrown during execution in test generation
        try {
            brokerImpl0.beforeCompletion();
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.lib.log.Log.isTraceEnabled()\" because \"this._log\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + false + "'", boolean7 == false);
        org.junit.Assert.assertNull(seq9);
    }

    @Test
    public void test41() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test41");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        java.util.Collection collection2 = null;
        org.apache.openjpa.kernel.OpCallbacks opCallbacks3 = null;
        java.lang.Object[] objArray4 = brokerImpl0.detachAll(collection2, opCallbacks3);
        org.junit.Assert.assertNull(fetchConfiguration1);
        org.junit.Assert.assertNull(objArray4);
    }

    @Test
    public void test42() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test42");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        org.apache.openjpa.meta.ClassMetaData classMetaData2 = null;
        org.apache.openjpa.kernel.Seq seq3 = brokerImpl0.getIdentitySequence(classMetaData2);
        // The following exception was thrown during execution in test generation
        try {
            brokerImpl0.releaseSavepoint("");
            org.junit.Assert.fail("Expected exception of type org.apache.openjpa.util.NoTransactionException; message: Can only perform operation while a transaction is active.");
        } catch (org.apache.openjpa.util.NoTransactionException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNull(fetchConfiguration1);
        org.junit.Assert.assertNull(seq3);
    }

    @Test
    public void test43() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test43");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        boolean boolean3 = brokerImpl0.getSyncWithManagedTransactions();
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + false + "'", boolean3 == false);
    }

    @Test
    public void test44() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test44");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setDetachState(1);
        brokerImpl0.setOptimistic(false);
        boolean boolean7 = brokerImpl0.getOrderDirtyObjects();
        org.apache.openjpa.meta.FieldMetaData fieldMetaData8 = null;
        org.apache.openjpa.kernel.Seq seq9 = brokerImpl0.getValueSequence(fieldMetaData8);
        // The following exception was thrown during execution in test generation
        try {
            java.util.Set<java.lang.String> strSet10 = brokerImpl0.getSupportedProperties();
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.conf.OpenJPAConfiguration.getPropertyKeys()\" because \"this._conf\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + false + "'", boolean7 == false);
        org.junit.Assert.assertNull(seq9);
    }

    @Test
    public void test45() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test45");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        org.apache.openjpa.meta.ClassMetaData classMetaData2 = null;
        org.apache.openjpa.kernel.Seq seq3 = brokerImpl0.getIdentitySequence(classMetaData2);
        org.apache.openjpa.kernel.DelegatingStoreManager delegatingStoreManager4 = brokerImpl0.getStoreManager();
        org.junit.Assert.assertNull(fetchConfiguration1);
        org.junit.Assert.assertNull(seq3);
        org.junit.Assert.assertNull(delegatingStoreManager4);
    }

    @Test
    public void test46() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test46");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setDetachState(1);
        brokerImpl0.setOptimistic(false);
        boolean boolean7 = brokerImpl0.isActive();
        org.apache.openjpa.kernel.BrokerImpl brokerImpl8 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean10 = brokerImpl8.isLoading((java.lang.Object) '#');
        brokerImpl8.setDetachState(1);
        brokerImpl8.setOptimistic(false);
        boolean boolean15 = brokerImpl8.getOrderDirtyObjects();
        org.apache.openjpa.meta.FieldMetaData fieldMetaData16 = null;
        org.apache.openjpa.kernel.Seq seq17 = brokerImpl8.getValueSequence(fieldMetaData16);
        java.lang.Object obj18 = brokerImpl0.getObjectId((java.lang.Object) seq17);
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + false + "'", boolean7 == false);
        org.junit.Assert.assertTrue("'" + boolean10 + "' != '" + false + "'", boolean10 == false);
        org.junit.Assert.assertTrue("'" + boolean15 + "' != '" + false + "'", boolean15 == false);
        org.junit.Assert.assertNull(seq17);
        org.junit.Assert.assertNull(obj18);
    }

    @Test
    public void test47() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test47");
        int int0 = org.apache.openjpa.kernel.LockLevels.LOCK_READ;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 10 + "'", int0 == 10);
    }

    @Test
    public void test48() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test48");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        boolean boolean3 = brokerImpl0.isNew((java.lang.Object) 100.0f);
        brokerImpl0.setIgnoreChanges(false);
        java.util.Collection collection6 = null;
        org.apache.openjpa.kernel.OpCallbacks opCallbacks8 = null;
        brokerImpl0.retrieveAll(collection6, false, opCallbacks8);
        // The following exception was thrown during execution in test generation
        try {
            brokerImpl0.begin();
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.kernel.AbstractBrokerFactory.syncWithManagedTransaction(org.apache.openjpa.kernel.BrokerImpl, boolean)\" because \"this._factory\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNull(fetchConfiguration1);
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + false + "'", boolean3 == false);
    }

    @Test
    public void test49() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test49");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        boolean boolean3 = brokerImpl0.isNew((java.lang.Object) 100.0f);
        boolean boolean4 = brokerImpl0.getSyncWithManagedTransactions();
        org.junit.Assert.assertNull(fetchConfiguration1);
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + false + "'", boolean3 == false);
        org.junit.Assert.assertTrue("'" + boolean4 + "' != '" + false + "'", boolean4 == false);
    }

    @Test
    public void test50() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test50");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setConnectionFactoryName("hi!");
        boolean boolean5 = brokerImpl0.isActive();
        brokerImpl0.setOrderDirtyObjects(false);
        brokerImpl0.unlock();
        // The following exception was thrown during execution in test generation
        try {
            brokerImpl0.assertActiveTransaction();
            org.junit.Assert.fail("Expected exception of type org.apache.openjpa.util.NoTransactionException; message: Can only perform operation while a transaction is active.");
        } catch (org.apache.openjpa.util.NoTransactionException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertTrue("'" + boolean5 + "' != '" + false + "'", boolean5 == false);
    }

    @Test
    public void test51() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test51");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setConnectionFactoryName("hi!");
        boolean boolean5 = brokerImpl0.isActive();
        brokerImpl0.setOrderDirtyObjects(false);
        brokerImpl0.unlock();
        org.apache.openjpa.kernel.InverseManager inverseManager9 = brokerImpl0.getInverseManager();
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertTrue("'" + boolean5 + "' != '" + false + "'", boolean5 == false);
        org.junit.Assert.assertNull(inverseManager9);
    }

    @Test
    public void test52() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test52");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        org.apache.openjpa.meta.ClassMetaData classMetaData2 = null;
        org.apache.openjpa.kernel.Seq seq3 = brokerImpl0.getIdentitySequence(classMetaData2);
        org.apache.openjpa.kernel.OpCallbacks opCallbacks5 = null;
        brokerImpl0.evict((java.lang.Object) 1, opCallbacks5);
        boolean boolean7 = brokerImpl0.getIgnoreChanges();
        org.junit.Assert.assertNull(fetchConfiguration1);
        org.junit.Assert.assertNull(seq3);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + false + "'", boolean7 == false);
    }

    @Test
    public void test53() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test53");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        org.apache.openjpa.meta.ClassMetaData classMetaData2 = null;
        org.apache.openjpa.kernel.Seq seq3 = brokerImpl0.getIdentitySequence(classMetaData2);
        brokerImpl0.setPostLoadOnMerge(false);
        org.apache.openjpa.kernel.BrokerImpl brokerImpl6 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean8 = brokerImpl6.isLoading((java.lang.Object) '#');
        brokerImpl6.setConnectionFactoryName("hi!");
        boolean boolean11 = brokerImpl6.isActive();
        org.apache.openjpa.kernel.Extent extent12 = null;
        org.apache.openjpa.kernel.OpCallbacks opCallbacks13 = null;
        brokerImpl6.evictAll(extent12, opCallbacks13);
        boolean boolean15 = brokerImpl0.isDetached((java.lang.Object) opCallbacks13);
        org.junit.Assert.assertNull(fetchConfiguration1);
        org.junit.Assert.assertNull(seq3);
        org.junit.Assert.assertTrue("'" + boolean8 + "' != '" + false + "'", boolean8 == false);
        org.junit.Assert.assertTrue("'" + boolean11 + "' != '" + false + "'", boolean11 == false);
        org.junit.Assert.assertTrue("'" + boolean15 + "' != '" + false + "'", boolean15 == false);
    }

    @Test
    public void test54() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test54");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setConnectionFactoryName("hi!");
        boolean boolean5 = brokerImpl0.isActive();
        brokerImpl0.setOrderDirtyObjects(false);
        org.apache.openjpa.meta.ClassMetaData classMetaData8 = null;
        org.apache.openjpa.kernel.Seq seq9 = brokerImpl0.getIdentitySequence(classMetaData8);
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertTrue("'" + boolean5 + "' != '" + false + "'", boolean5 == false);
        org.junit.Assert.assertNull(seq9);
    }

    @Test
    public void test55() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test55");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        boolean boolean3 = brokerImpl0.isNew((java.lang.Object) 100.0f);
        brokerImpl0.setDetachedNew(false);
        java.util.Collection collection6 = null;
        org.apache.openjpa.kernel.OpCallbacks opCallbacks7 = null;
        java.lang.Object[] objArray8 = brokerImpl0.detachAll(collection6, opCallbacks7);
        // The following exception was thrown during execution in test generation
        try {
            brokerImpl0.removeLifecycleListener((java.lang.Object) 100.0d);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.openjpa.event.LifecycleEventManager.removeListener(Object)\" because \"this._lifeEventManager\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNull(fetchConfiguration1);
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + false + "'", boolean3 == false);
        org.junit.Assert.assertNull(objArray8);
    }

    @Test
    public void test56() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test56");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setDetachState(1);
        brokerImpl0.setOptimistic(false);
        org.apache.openjpa.kernel.OpCallbacks opCallbacks8 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.lang.Object obj9 = brokerImpl0.detach((java.lang.Object) (short) 10, opCallbacks8);
            org.junit.Assert.fail("Expected exception of type org.apache.openjpa.util.GeneralException; message: Cannot invoke \"org.apache.openjpa.conf.OpenJPAConfiguration.getProxyManagerInstance()\" because the return value of \"org.apache.openjpa.kernel.BrokerImpl.getConfiguration()\" is null");
        } catch (org.apache.openjpa.util.GeneralException e) {
            // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test57() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test57");
        int int0 = org.apache.openjpa.kernel.AutoDetach.DETACH_COMMIT;
        org.junit.Assert.assertTrue("'" + int0 + "' != '" + 4 + "'", int0 == 4);
    }

    @Test
    public void test58() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test58");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        boolean boolean3 = brokerImpl0.isNew((java.lang.Object) 100.0f);
        brokerImpl0.setDetachedNew(false);
        java.util.Collection collection6 = null;
        org.apache.openjpa.kernel.OpCallbacks opCallbacks7 = null;
        java.lang.Object[] objArray8 = brokerImpl0.detachAll(collection6, opCallbacks7);
        boolean boolean9 = brokerImpl0.getNontransactionalWrite();
        org.junit.Assert.assertNull(fetchConfiguration1);
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + false + "'", boolean3 == false);
        org.junit.Assert.assertNull(objArray8);
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
    }

    @Test
    public void test59() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test59");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setDetachState(1);
        brokerImpl0.setOptimistic(false);
        boolean boolean7 = brokerImpl0.getOrderDirtyObjects();
        org.apache.openjpa.meta.FieldMetaData fieldMetaData8 = null;
        org.apache.openjpa.kernel.Seq seq9 = brokerImpl0.getValueSequence(fieldMetaData8);
        brokerImpl0.preFlush();
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + false + "'", boolean7 == false);
        org.junit.Assert.assertNull(seq9);
    }

    @Test
    public void test60() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test60");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setConnectionFactoryName("hi!");
        boolean boolean5 = brokerImpl0.isActive();
        brokerImpl0.setOrderDirtyObjects(false);
        brokerImpl0.unlock();
        boolean boolean9 = brokerImpl0.getIgnoreChanges();
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertTrue("'" + boolean5 + "' != '" + false + "'", boolean5 == false);
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
    }

    @Test
    public void test61() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test61");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        boolean boolean3 = brokerImpl0.isNew((java.lang.Object) 100.0f);
        org.apache.openjpa.kernel.BrokerImpl brokerImpl4 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration5 = brokerImpl4.getFetchConfiguration();
        org.apache.openjpa.meta.ClassMetaData classMetaData6 = null;
        org.apache.openjpa.kernel.Seq seq7 = brokerImpl4.getIdentitySequence(classMetaData6);
        org.apache.openjpa.kernel.OpCallbacks opCallbacks9 = null;
        brokerImpl4.evict((java.lang.Object) 1, opCallbacks9);
        int int11 = brokerImpl4.getRestoreState();
        org.apache.openjpa.kernel.OpCallbacks opCallbacks14 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.openjpa.kernel.OpenJPAStateManager openJPAStateManager15 = brokerImpl0.persist((java.lang.Object) int11, (java.lang.Object) (byte) 0, false, opCallbacks14);
            org.junit.Assert.fail("Expected exception of type org.apache.openjpa.util.NoTransactionException; message: To perform this operation, it must be written within a transaction, or your settings must allow nontransactional writes and must not detach all nontransactional reads.");
        } catch (org.apache.openjpa.util.NoTransactionException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNull(fetchConfiguration1);
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + false + "'", boolean3 == false);
        org.junit.Assert.assertNull(fetchConfiguration5);
        org.junit.Assert.assertNull(seq7);
        org.junit.Assert.assertTrue("'" + int11 + "' != '" + 1 + "'", int11 == 1);
    }
}

