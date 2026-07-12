package org.apache.openjpa.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import org.apache.openjpa.util.InvalidStateException;
import org.apache.openjpa.util.NoTransactionException;
import org.apache.openjpa.util.UserException;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link BrokerImpl}.
 *
 * These tests exercise BrokerImpl methods that can be safely invoked on a
 * freshly constructed, un-initialized broker (i.e. without calling
 * {@code initialize(...)}, which requires a fully wired
 * AbstractBrokerFactory / OpenJPAConfiguration / DelegatingStoreManager).
 * Methods were selected so that their default code paths do not touch any
 * of the transient collaborator fields (_conf, _store, _runtime, _cache,
 * etc.) that remain null before initialization.
 */
public class BrokerImplTest {

    private BrokerImpl broker;

    @Before
    public void setUp() {
        broker = new BrokerImpl();
    }

    /**
     * Helper to flip the private "_closed" field via reflection, used to
     * exercise the exceptional branch of assertOpen().
     */
    private void setClosed(boolean closed) throws Exception {
        Field f = BrokerImpl.class.getDeclaredField("_closed");
        f.setAccessible(true);
        f.set(broker, closed);
    }

    // ------------------------------------------------------------------
    // setAuthentication(String, String) / getConnectionUserName() /
    // getConnectionPassword()
    // ------------------------------------------------------------------

    @Test
    public void testSetAuthenticationStoresUserAndPassword() {
        broker.setAuthentication("scott", "tiger");
        assertEquals("scott", broker.getConnectionUserName());
        assertEquals("tiger", broker.getConnectionPassword());
    }

    @Test
    public void testSetAuthenticationWithNullsClearsCredentials() {
        broker.setAuthentication("scott", "tiger");
        broker.setAuthentication(null, null);
        assertNull(broker.getConnectionUserName());
        assertNull(broker.getConnectionPassword());
    }

    // ------------------------------------------------------------------
    // getMultithreaded() / setMultithreaded(boolean)
    // ------------------------------------------------------------------

    @Test
    public void testGetMultithreadedDefaultsToFalse() {
        assertFalse(broker.getMultithreaded());
    }

    @Test
    public void testSetMultithreadedTrueUpdatesFlag() {
        broker.setMultithreaded(true);
        assertTrue(broker.getMultithreaded());
    }

    @Test
    public void testSetMultithreadedFalseAfterTrueUpdatesFlag() {
        broker.setMultithreaded(true);
        broker.setMultithreaded(false);
        assertFalse(broker.getMultithreaded());
    }

    // ------------------------------------------------------------------
    // setAutoDetach(int) / getAutoDetach()
    // ------------------------------------------------------------------

    @Test
    public void testSetAutoDetachValidFlagIsStored() {
        broker.setAutoDetach(AutoDetach.DETACH_CLOSE);
        assertEquals(AutoDetach.DETACH_CLOSE, broker.getAutoDetach());
    }

    @Test
    public void testSetAutoDetachDetachNoneCombinedWithOtherFlagThrows() {
        try {
            broker.setAutoDetach(AutoDetach.DETACH_NONE | AutoDetach.DETACH_CLOSE);
            fail("Expected a UserException when DETACH_NONE is combined with another flag");
        } catch (UserException expected) {
            // expected: DETACH_NONE must be exclusive
        }
    }

    // ------------------------------------------------------------------
    // setDetachState(int) / getDetachState()
    // ------------------------------------------------------------------

    @Test
    public void testGetDetachStateDefaultValue() {
        // Default value assigned at field declaration (DETACH_LOADED).
        assertEquals(DetachState.DETACH_LOADED, broker.getDetachState());
    }

    @Test
    public void testSetDetachStateUpdatesValue() {
        broker.setDetachState(DetachState.DETACH_ALL);
        assertEquals(DetachState.DETACH_ALL, broker.getDetachState());
    }

    // ------------------------------------------------------------------
    // setDetachedNew(boolean) / isDetachedNew()
    // ------------------------------------------------------------------

    @Test
    public void testIsDetachedNewDefaultsToTrue() {
        assertTrue(broker.isDetachedNew());
    }

    @Test
    public void testSetDetachedNewFalseUpdatesValue() {
        broker.setDetachedNew(false);
        assertFalse(broker.isDetachedNew());
    }

    // ------------------------------------------------------------------
    // setRetainState(boolean) / getRetainState()
    // ------------------------------------------------------------------

    @Test
    public void testGetRetainStateDefaultsToFalse() {
        assertFalse(broker.getRetainState());
    }

    @Test
    public void testSetRetainStateTrueUpdatesValue() {
        broker.setRetainState(true);
        assertTrue(broker.getRetainState());
    }

    // ------------------------------------------------------------------
    // putUserObject(Object, Object) / getUserObject(Object)
    // ------------------------------------------------------------------

    @Test
    public void testPutAndGetUserObjectRoundTrips() {
        Object key = "myKey";
        Object value = "myValue";
        Object previous = broker.putUserObject(key, value);
        assertNull(previous);
        assertEquals(value, broker.getUserObject(key));
    }

    @Test
    public void testGetUserObjectForUnknownKeyReturnsNull() {
        assertNull(broker.getUserObject("doesNotExist"));
    }

    @Test
    public void testPutUserObjectWithNullValueRemovesExistingEntry() {
        Object key = "removeMe";
        broker.putUserObject(key, "value");
        Object removed = broker.putUserObject(key, null);
        assertEquals("value", removed);
        assertNull(broker.getUserObject(key));
    }

    // ------------------------------------------------------------------
    // isActive()
    // ------------------------------------------------------------------

    @Test
    public void testIsActiveDefaultsToFalseOnNewBroker() {
        assertFalse(broker.isActive());
    }

    // ------------------------------------------------------------------
    // getRollbackOnly()
    // ------------------------------------------------------------------

    @Test
    public void testGetRollbackOnlyDefaultsToFalseWhenNoTransactionActive() {
        assertFalse(broker.getRollbackOnly());
    }

    @Test
    public void testSetRollbackOnlyWithoutActiveTransactionThrows() {
        try {
            broker.setRollbackOnly();
            fail("Expected NoTransactionException when there is no active transaction");
        } catch (NoTransactionException expected) {
            // expected: cannot mark rollback-only without an active transaction
        }
    }

    // ------------------------------------------------------------------
    // assertOpen()
    // ------------------------------------------------------------------

    @Test
    public void testAssertOpenDoesNotThrowWhenBrokerIsOpen() {
        // Should simply return without throwing on a fresh, un-closed broker.
        broker.assertOpen();
    }

    @Test
    public void testAssertOpenThrowsWhenBrokerIsClosed() throws Exception {
        setClosed(true);
        try {
            broker.assertOpen();
            fail("Expected InvalidStateException when broker has been closed");
        } catch (InvalidStateException expected) {
            // expected: operations are not allowed on a closed broker
        }
    }
}
