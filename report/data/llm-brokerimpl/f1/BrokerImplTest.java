
package org.apache.openjpa.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 tests for {@link BrokerImpl}.
 *
 * These tests exercise BrokerImpl methods that operate purely on the
 * broker's internal state and do not require the heavyweight
 * initialize(...) call (which needs a fully wired AbstractBrokerFactory,
 * DelegatingStoreManager and OpenJPAConfiguration). lock()/unlock() are
 * no-ops until setMultithreaded(true) is invoked, and assertOpen() only
 * checks the (initially false) _closed flag, so a plain "new BrokerImpl()"
 * is sufficient to exercise this behavior.
 */
public class BrokerImplTest {

    private BrokerImpl broker;

    @Before
    public void setUp() {
        broker = new BrokerImpl();
    }

    // ------------------------------------------------------------------
    // setAuthentication / getConnectionUserName / getConnectionPassword
    // ------------------------------------------------------------------

    @Test
    public void testSetAuthenticationStoresUserAndPassword() {
        broker.setAuthentication("scott", "tiger");
        assertEquals("scott", broker.getConnectionUserName());
        assertEquals("tiger", broker.getConnectionPassword());
    }

    @Test
    public void testConnectionUserNameAndPasswordDefaultToNull() {
        assertNull(broker.getConnectionUserName());
        assertNull(broker.getConnectionPassword());
    }

    // ------------------------------------------------------------------
    // setMultithreaded / getMultithreaded
    // ------------------------------------------------------------------

    @Test
    public void testSetMultithreadedTrueIsReflectedByGetter() {
        assertFalse(broker.getMultithreaded());
        broker.setMultithreaded(true);
        assertTrue(broker.getMultithreaded());
    }

    @Test
    public void testSetMultithreadedFalseAfterTrueDoesNotThrow() {
        broker.setMultithreaded(true);
        broker.setMultithreaded(false);
        assertFalse(broker.getMultithreaded());
        // lock()/unlock() must remain safe no-ops once the internal lock is cleared
        broker.lock();
        broker.unlock();
    }

    // ------------------------------------------------------------------
    // setIgnoreChanges / getIgnoreChanges
    // ------------------------------------------------------------------

    @Test
    public void testSetIgnoreChangesUpdatesState() {
        assertFalse(broker.getIgnoreChanges());
        broker.setIgnoreChanges(true);
        assertTrue(broker.getIgnoreChanges());
        broker.setIgnoreChanges(false);
        assertFalse(broker.getIgnoreChanges());
    }

    // ------------------------------------------------------------------
    // setRetainState / getRetainState
    // ------------------------------------------------------------------

    @Test
    public void testSetRetainStateUpdatesState() {
        assertFalse(broker.getRetainState());
        broker.setRetainState(true);
        assertTrue(broker.getRetainState());
    }

    // ------------------------------------------------------------------
    // setAutoClear / getAutoClear
    // ------------------------------------------------------------------

    @Test
    public void testSetAutoClearUpdatesState() {
        assertEquals(Broker.CLEAR_DATASTORE, broker.getAutoClear());
        broker.setAutoClear(Broker.CLEAR_ALL);
        assertEquals(Broker.CLEAR_ALL, broker.getAutoClear());
    }

    // ------------------------------------------------------------------
    // setAutoDetach(int) / getAutoDetach
    // ------------------------------------------------------------------

    @Test
    public void testSetAutoDetachStoresFlags() {
        assertEquals(0, broker.getAutoDetach());
        broker.setAutoDetach(AutoDetach.DETACH_CLOSE);
        assertEquals(AutoDetach.DETACH_CLOSE, broker.getAutoDetach());
    }

    @Test
    public void testSetAutoDetachOverwritesPreviousValue() {
        broker.setAutoDetach(AutoDetach.DETACH_CLOSE);
        broker.setAutoDetach(AutoDetach.DETACH_COMMIT);
        assertEquals(AutoDetach.DETACH_COMMIT, broker.getAutoDetach());
    }

    // ------------------------------------------------------------------
    // setDetachedNew / isDetachedNew
    // ------------------------------------------------------------------

    @Test
    public void testSetDetachedNewDefaultsToTrue() {
        assertTrue(broker.isDetachedNew());
    }

    @Test
    public void testSetDetachedNewCanBeSetFalse() {
        broker.setDetachedNew(false);
        assertFalse(broker.isDetachedNew());
    }

    // ------------------------------------------------------------------
    // putUserObject / getUserObject
    // ------------------------------------------------------------------

    @Test
    public void testGetUserObjectReturnsNullWhenNotSet() {
        assertNull(broker.getUserObject("missing-key"));
    }

    @Test
    public void testPutAndGetUserObjectRoundTrips() {
        Object previous = broker.putUserObject("key1", "value1");
        assertNull(previous);
        assertEquals("value1", broker.getUserObject("key1"));
    }

    @Test
    public void testPutUserObjectReturnsPreviousValue() {
        broker.putUserObject("key1", "value1");
        Object previous = broker.putUserObject("key1", "value2");
        assertEquals("value1", previous);
        assertEquals("value2", broker.getUserObject("key1"));
    }

    @Test
    public void testPutUserObjectWithNullValueRemovesEntry() {
        broker.putUserObject("key1", "value1");
        Object removed = broker.putUserObject("key1", null);
        assertEquals("value1", removed);
        assertNull(broker.getUserObject("key1"));
    }

    // ------------------------------------------------------------------
    // clone()
    // ------------------------------------------------------------------

    @Test
    public void testCloneReturnsDistinctEqualStateInstance() throws CloneNotSupportedException {
        broker.setAuthentication("scott", "tiger");
        Object clone = broker.clone();
        assertNotNull(clone);
        assertNotSame(broker, clone);
        assertTrue(clone instanceof BrokerImpl);
        assertEquals("scott", ((BrokerImpl) clone).getConnectionUserName());
    }

    // ------------------------------------------------------------------
    // setConnectionFactoryName / getConnectionFactoryName / getConnectionFactory
    // ------------------------------------------------------------------

    @Test
    public void testConnectionFactoryNameDefaultsToEmptyString() {
        assertEquals("", broker.getConnectionFactoryName());
    }

    @Test
    public void testSetConnectionFactoryNameTrimsBlankToNull() {
        broker.setConnectionFactoryName("   ");
        assertNull(broker.getConnectionFactoryName());
    }

    @Test
    public void testSetConnectionFactoryNameStoresTrimmedValue() {
        broker.setConnectionFactoryName("  myConnectionFactory  ");
        assertEquals("myConnectionFactory", broker.getConnectionFactoryName());
    }

    @Test
    public void testGetConnectionFactoryReturnsNullWhenNameBlank() {
        // default _connectionFactoryName is "" (blank), so no JNDI lookup should occur
        assertNull(broker.getConnectionFactory());
    }
}
