package org.apache.openjpa.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 tests for {@link BrokerImpl}.
 *
 * These tests exercise BrokerImpl in its freshly-constructed state
 * (i.e. before {@code initialize(...)} has been called), which is
 * sufficient to cover the simple property accessors below as well as
 * a few methods whose behavior depends on collaborators (such as
 * {@code _conf} or {@code _cache}) that remain {@code null} until the
 * broker is initialized. Where a method depends on such an
 * uninitialized collaborator, the test documents the resulting
 * exception rather than mocking the collaborator.
 */
public class BrokerImplTest {

    private BrokerImpl broker;

    @Before
    public void setUp() {
        broker = new BrokerImpl();
    }

    // ------------------------------------------------------------------
    // getConnectionFactoryName / setConnectionFactoryName
    // ------------------------------------------------------------------

    @Test
    public void testSetConnectionFactoryName_NormalValueIsStored() {
        broker.setConnectionFactoryName("jdbc/MyFactory");
        assertEquals("jdbc/MyFactory", broker.getConnectionFactoryName());
    }

    @Test
    public void testSetConnectionFactoryName_BlankValueTrimmedToNull() {
        // boundary: a whitespace-only string is trimmed to null
        broker.setConnectionFactoryName("   ");
        assertNull(broker.getConnectionFactoryName());
    }

    // ------------------------------------------------------------------
    // getConnectionFactory2Name / setConnectionFactory2Name
    // ------------------------------------------------------------------

    @Test
    public void testSetConnectionFactory2Name_NormalValueIsStored() {
        broker.setConnectionFactory2Name("jdbc/MyFactory2");
        assertEquals("jdbc/MyFactory2", broker.getConnectionFactory2Name());
    }

    @Test
    public void testSetConnectionFactory2Name_BlankValueTrimmedToNull() {
        // boundary: empty string is trimmed to null
        broker.setConnectionFactory2Name("");
        assertNull(broker.getConnectionFactory2Name());
    }

    // ------------------------------------------------------------------
    // getMultithreaded / setMultithreaded
    // ------------------------------------------------------------------

    @Test
    public void testGetMultithreaded_DefaultIsFalse() {
        // boundary: default (un-set) state
        assertFalse(broker.getMultithreaded());
    }

    @Test
    public void testSetMultithreaded_TrueIsReflectedByGetter() {
        broker.setMultithreaded(true);
        assertTrue(broker.getMultithreaded());
    }

    // ------------------------------------------------------------------
    // getAutoClear / setAutoClear
    // ------------------------------------------------------------------

    @Test
    public void testSetAutoClear_BoundaryMinValue() {
        broker.setAutoClear(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, broker.getAutoClear());
    }

    @Test
    public void testSetAutoClear_BoundaryMaxValue() {
        broker.setAutoClear(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, broker.getAutoClear());
    }

    // ------------------------------------------------------------------
    // getDetachState / setDetachState
    // ------------------------------------------------------------------

    @Test
    public void testSetDetachState_BoundaryZero() {
        broker.setDetachState(0);
        assertEquals(0, broker.getDetachState());
    }

    @Test
    public void testSetDetachState_NegativeValue() {
        broker.setDetachState(-1);
        assertEquals(-1, broker.getDetachState());
    }

    // ------------------------------------------------------------------
    // getAllowReferenceToSiblingContext / setAllowReferenceToSiblingContext
    // ------------------------------------------------------------------

    @Test
    public void testGetAllowReferenceToSiblingContext_DefaultIsFalse() {
        // boundary: default (un-set) state
        assertFalse(broker.getAllowReferenceToSiblingContext());
    }

    @Test
    public void testSetAllowReferenceToSiblingContext_TrueIsReflectedByGetter() {
        broker.setAllowReferenceToSiblingContext(true);
        assertTrue(broker.getAllowReferenceToSiblingContext());
    }

    // ------------------------------------------------------------------
    // getPostLoadOnMerge / setPostLoadOnMerge
    // ------------------------------------------------------------------

    @Test
    public void testGetPostLoadOnMerge_DefaultIsFalse() {
        // boundary: default (un-set) state
        assertFalse(broker.getPostLoadOnMerge());
    }

    @Test
    public void testSetPostLoadOnMerge_TrueIsReflectedByGetter() {
        broker.setPostLoadOnMerge(true);
        assertTrue(broker.getPostLoadOnMerge());
    }

    // ------------------------------------------------------------------
    // getCachePreparedQuery / setCachePreparedQuery
    // ------------------------------------------------------------------

    @Test
    public void testSetCachePreparedQuery_FalseShortCircuitsWithoutTouchingConfiguration() {
        // boundary: once the flag is false, getCachePreparedQuery() short-circuits
        // and never dereferences the (uninitialized, null) configuration object.
        broker.setCachePreparedQuery(false);
        assertFalse(broker.getCachePreparedQuery());
    }

    @Test(expected = NullPointerException.class)
    public void testGetCachePreparedQuery_DefaultTrueThrowsWhenConfigurationUnset() {
        // The flag defaults to true, so getCachePreparedQuery() must evaluate
        // _conf.getQuerySQLCacheInstance(); since the broker was never
        // initialize()'d, _conf is null and a NullPointerException results.
        broker.getCachePreparedQuery();
    }

    // ------------------------------------------------------------------
    // getCacheFinderQuery / setCacheFinderQuery
    // ------------------------------------------------------------------

    @Test(expected = NullPointerException.class)
    public void testGetCacheFinderQuery_DefaultTrueThrowsWhenConfigurationUnset() {
        // The flag defaults to true, so getCacheFinderQuery() must evaluate
        // _conf.getFinderCacheInstance(); since the broker was never
        // initialize()'d, _conf is null and a NullPointerException results.
        broker.getCacheFinderQuery();
    }

    @Test
    public void testSetCacheFinderQuery_ActuallyUpdatesCachePreparedQueryFlag() {
        // Documents the observed behavior of setCacheFinderQuery(boolean):
        // it assigns to _cachePreparedQuery rather than _cacheFinderQuery,
        // so the change becomes visible through getCachePreparedQuery(),
        // which can then be read back without triggering a NullPointerException.
        broker.setCacheFinderQuery(false);
        assertFalse(broker.getCachePreparedQuery());
    }

    // ------------------------------------------------------------------
    // isCached
    // ------------------------------------------------------------------

    @Test
    public void testIsCached_EmptyListReturnsTrueWithoutTouchingCache() {
        // boundary: an empty oid list means every element (vacuously) is
        // already loaded, so the method returns true without dereferencing
        // the (uninitialized, null) managed cache.
        List<Object> oids = new ArrayList<>();
        assertTrue(broker.isCached(oids));
    }

    @Test(expected = NullPointerException.class)
    public void testIsCached_NonEmptyListThrowsWhenCacheUnset() {
        // A non-empty oid list forces a lookup against the (uninitialized,
        // null) managed cache, resulting in a NullPointerException.
        List<Object> oids = Collections.singletonList((Object) "some-oid");
        broker.isCached(oids);
    }
}
