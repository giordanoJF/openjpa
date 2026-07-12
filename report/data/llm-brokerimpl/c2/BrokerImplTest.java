package org.apache.openjpa.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import org.apache.openjpa.util.UserException;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link BrokerImpl}.
 *
 * These tests deliberately avoid calling {@link BrokerImpl#initialize} because doing so
 * requires a fully wired {@code AbstractBrokerFactory}/{@code DelegatingStoreManager}/
 * {@code OpenJPAConfiguration} graph. Instead they exercise methods that are safe to call
 * on a bare, uninitialized broker (plain field mutators, documented normalization
 * behavior, and validation logic), plus the specific, deterministic failure mode
 * (NullPointerException) that occurs when a method needs configuration that was never set.
 */
public class BrokerImplTest {

    private BrokerImpl broker;

    @Before
    public void setUp() {
        broker = new BrokerImpl();
    }

    /**
     * Reads a private field via reflection since some fields under test have no
     * corresponding public accessor that can be safely invoked without initialize().
     */
    private Object getPrivateField(String name) throws Exception {
        Field f = BrokerImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(broker);
    }

    // ------------------------------------------------------------------
    // 1. setAuthentication
    // ------------------------------------------------------------------

    @Test
    public void testSetAuthenticationStoresUserAndPassword() {
        broker.setAuthentication("alice", "s3cr3t");
        assertEquals("alice", broker.getConnectionUserName());
        assertEquals("s3cr3t", broker.getConnectionPassword());
    }

    @Test
    public void testSetAuthenticationAllowsNullValues() {
        broker.setAuthentication(null, null);
        assertNull(broker.getConnectionUserName());
        assertNull(broker.getConnectionPassword());
    }

    // ------------------------------------------------------------------
    // 2. setConnectionFactoryName / getConnectionFactoryName
    // ------------------------------------------------------------------

    @Test
    public void testSetConnectionFactoryNameTrimsWhitespace() {
        broker.setConnectionFactoryName("  jndi/MyFactory  ");
        assertEquals("jndi/MyFactory", broker.getConnectionFactoryName());
    }

    @Test
    public void testSetConnectionFactoryNameBlankBecomesNull() {
        broker.setConnectionFactoryName("   ");
        assertNull(broker.getConnectionFactoryName());
    }

    // ------------------------------------------------------------------
    // 3. setConnectionFactory2Name / getConnectionFactory2Name
    // ------------------------------------------------------------------

    @Test
    public void testSetConnectionFactory2NameTrimsAndStores() {
        broker.setConnectionFactory2Name("  jndi/Factory2  ");
        assertEquals("jndi/Factory2", broker.getConnectionFactory2Name());
    }

    @Test
    public void testSetConnectionFactory2NameNullStaysNull() {
        broker.setConnectionFactory2Name(null);
        assertNull(broker.getConnectionFactory2Name());
    }

    // ------------------------------------------------------------------
    // 4. getConnectionFactory
    // ------------------------------------------------------------------

    @Test
    public void testGetConnectionFactoryReturnsNullWhenNameBlank() {
        // default state: _connectionFactoryName == ""
        assertNull(broker.getConnectionFactory());
    }

    // ------------------------------------------------------------------
    // 5. getConnectionFactory2
    // ------------------------------------------------------------------

    @Test
    public void testGetConnectionFactory2ReturnsNullWhenNameBlank() {
        // default state: _connectionFactory2Name == ""
        assertNull(broker.getConnectionFactory2());
    }

    // ------------------------------------------------------------------
    // 6. setAutoDetach / getAutoDetach
    // ------------------------------------------------------------------

    @Test
    public void testSetAutoDetachStoresValidCombination() {
        int flags = AutoDetach.DETACH_CLOSE | AutoDetach.DETACH_COMMIT;
        broker.setAutoDetach(flags);
        assertEquals(flags, broker.getAutoDetach());
    }

    @Test(expected = UserException.class)
    public void testSetAutoDetachNoneCombinedWithOtherFlagThrows() {
        broker.setAutoDetach(AutoDetach.DETACH_NONE | AutoDetach.DETACH_CLOSE);
    }

    // ------------------------------------------------------------------
    // 7. setDetachedNew / isDetachedNew
    // ------------------------------------------------------------------

    @Test
    public void testIsDetachedNewDefaultsToTrue() {
        assertTrue(broker.isDetachedNew());
    }

    @Test
    public void testSetDetachedNewFalseUpdatesFlag() {
        broker.setDetachedNew(false);
        assertFalse(broker.isDetachedNew());
    }

    // ------------------------------------------------------------------
    // 8. putUserObject / getUserObject
    // ------------------------------------------------------------------

    @Test
    public void testPutUserObjectStoresAndRetrievesValue() {
        Object previous = broker.putUserObject("key1", "value1");
        assertNull(previous);
        assertEquals("value1", broker.getUserObject("key1"));
    }

    @Test
    public void testPutUserObjectNullValueRemovesKey() {
        broker.putUserObject("key2", "value2");
        Object removed = broker.putUserObject("key2", null);
        assertEquals("value2", removed);
        assertNull(broker.getUserObject("key2"));
    }

    @Test
    public void testGetUserObjectReturnsNullWhenNeverSet() {
        assertNull(broker.getUserObject("neverSet"));
    }

    // ------------------------------------------------------------------
    // 9. setCachePreparedQuery / getCachePreparedQuery
    // ------------------------------------------------------------------

    @Test
    public void testSetCachePreparedQueryUpdatesField() throws Exception {
        broker.setCachePreparedQuery(false);
        assertEquals(Boolean.FALSE, getPrivateField("_cachePreparedQuery"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetCachePreparedQueryThrowsWithoutConfiguration() {
        // _conf is null because initialize() was never called.
        broker.getCachePreparedQuery();
    }

    // ------------------------------------------------------------------
    // 10. setCacheFinderQuery / getCacheFinderQuery
    // ------------------------------------------------------------------

    @Test
    public void testSetCacheFinderQueryActuallyMutatesCachePreparedQueryField() throws Exception {
        // Documents existing (buggy) behavior: setCacheFinderQuery(false) writes to
        // _cachePreparedQuery instead of _cacheFinderQuery. If this test starts
        // failing because _cacheFinderQuery changed to false, the underlying bug
        // has likely been fixed and this test (and the production code) should be
        // revisited together.
        broker.setCacheFinderQuery(false);

        assertEquals("expected the (buggy) setter to leave _cacheFinderQuery untouched",
            Boolean.TRUE, getPrivateField("_cacheFinderQuery"));
        assertEquals("expected the (buggy) setter to have overwritten _cachePreparedQuery",
            Boolean.FALSE, getPrivateField("_cachePreparedQuery"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetCacheFinderQueryThrowsWithoutConfiguration() {
        // _conf is null because initialize() was never called.
        broker.getCacheFinderQuery();
    }
}
