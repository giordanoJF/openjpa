import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ErrorTest0 {

    public static boolean debug = false;

    @Test
    public void test01() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ErrorTest0.test01");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        java.lang.Object obj1 = brokerImpl0.clone();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        brokerImpl0.setOptimistic(true);
    }

    @Test
    public void test02() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ErrorTest0.test02");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        brokerImpl0.setTransactionListenerCallbackMode(0);
    }

    @Test
    public void test03() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ErrorTest0.test03");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        brokerImpl0.removeTransactionListener((java.lang.Object) 8);
    }

    @Test
    public void test04() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ErrorTest0.test04");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        java.lang.Object obj1 = brokerImpl0.clone();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        boolean boolean2 = brokerImpl0.getCacheFinderQuery();
    }

    @Test
    public void test05() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ErrorTest0.test05");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        boolean boolean2 = brokerImpl0.getCacheFinderQuery();
    }

    @Test
    public void test06() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ErrorTest0.test06");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration3 = brokerImpl0.pushFetchConfiguration();
    }

    @Test
    public void test07() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ErrorTest0.test07");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        brokerImpl0.setLifecycleListenerCallbackMode(8);
    }

    @Test
    public void test08() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ErrorTest0.test08");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        java.lang.Object obj1 = brokerImpl0.clone();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        java.util.Map<java.lang.String, java.lang.Object> strMap2 = brokerImpl0.getProperties();
    }

    @Test
    public void test09() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ErrorTest0.test09");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setConnectionFactoryName("hi!");
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        brokerImpl0.close();
    }

    @Test
    public void test10() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ErrorTest0.test10");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setDetachState(1);
        brokerImpl0.setOptimistic(false);
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        brokerImpl0.rollbackAndResume();
    }

    @Test
    public void test11() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ErrorTest0.test11");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        boolean boolean2 = brokerImpl0.isLoading((java.lang.Object) '#');
        brokerImpl0.setConnectionFactoryName("hi!");
        java.lang.String str5 = brokerImpl0.getConnectionFactoryName();
        boolean boolean6 = brokerImpl0.getIgnoreChanges();
        brokerImpl0.preFlush();
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        java.util.Collection collection8 = brokerImpl0.getDirtyObjects();
    }

    @Test
    public void test12() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "ErrorTest0.test12");
        org.apache.openjpa.kernel.BrokerImpl brokerImpl0 = new org.apache.openjpa.kernel.BrokerImpl();
        org.apache.openjpa.kernel.FetchConfiguration fetchConfiguration1 = brokerImpl0.getFetchConfiguration();
        boolean boolean3 = brokerImpl0.isNew((java.lang.Object) 100.0f);
        brokerImpl0.setDetachedNew(false);
        // during test generation this statement threw an exception of type java.lang.NullPointerException in error
        java.util.Collection<java.lang.Object> objCollection6 = brokerImpl0.getTransactionListeners();
    }
}

