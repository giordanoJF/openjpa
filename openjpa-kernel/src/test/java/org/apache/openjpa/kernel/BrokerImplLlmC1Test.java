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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.openjpa.util.UserException;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link BrokerImpl}.
 *
 * These tests exercise BrokerImpl instances that have NOT been through
 * initialize(...) (i.e. no real StoreManager / OpenJPAConfiguration is
 * wired up). This is intentional: the methods under test only touch
 * fields that are safe to access pre-initialization (assertOpen() only
 * checks the _closed flag, which is false on a fresh instance). Methods
 * that require a fully initialized broker (persist, find, commit, etc.)
 * are out of scope for these tests.
 */
public class BrokerImplLlmC1Test {

    private BrokerImpl broker;

    @Before
    public void setUp() {
        broker = new BrokerImpl();
    }

    // ------------------------------------------------------------------
    // 1. setAuthentication / getConnectionUserName / getConnectionPassword
    // ------------------------------------------------------------------

    @Test
    public void testSetAuthentication_storesUserAndPassword() {
        broker.setAuthentication("scott", "tiger");
        assertEquals("scott", broker.getConnectionUserName());
        assertEquals("tiger", broker.getConnectionPassword());
    }

    @Test
    public void testSetAuthentication_nullValuesDoNotThrow() {
        broker.setAuthentication(null, null);
        assertNull(broker.getConnectionUserName());
        assertNull(broker.getConnectionPassword());
    }

    // ------------------------------------------------------------------
    // 2. setConnectionFactoryName / getConnectionFactoryName
    // ------------------------------------------------------------------

    @Test
    public void testSetConnectionFactoryName_storesNonBlankValue() {
        broker.setConnectionFactoryName("jdbc/MyDS");
        assertEquals("jdbc/MyDS", broker.getConnectionFactoryName());
    }

    @Test
    public void testSetConnectionFactoryName_blankStringBecomesNull() {
        broker.setConnectionFactoryName("   ");
        assertNull(broker.getConnectionFactoryName());
    }

    // ------------------------------------------------------------------
    // 3. setConnectionFactory2Name / getConnectionFactory2Name
    // ------------------------------------------------------------------

    @Test
    public void testSetConnectionFactory2Name_storesNonBlankValue() {
        broker.setConnectionFactory2Name("jdbc/MyDS2");
        assertEquals("jdbc/MyDS2", broker.getConnectionFactory2Name());
    }

    @Test
    public void testSetConnectionFactory2Name_blankStringBecomesNull() {
        broker.setConnectionFactory2Name("");
        assertNull(broker.getConnectionFactory2Name());
    }

    // ------------------------------------------------------------------
    // 4. getConnectionFactory
    // ------------------------------------------------------------------

    @Test
    public void testGetConnectionFactory_returnsNullWhenNameBlank() {
        // Default _connectionFactoryName is "" -> isNotBlank is false,
        // so no JNDI lookup should be attempted and null is returned.
        assertNull(broker.getConnectionFactory());
    }

    // ------------------------------------------------------------------
    // 5. setAutoDetach / getAutoDetach
    // ------------------------------------------------------------------

    @Test
    public void testSetAutoDetach_validSingleFlagIsStored() {
        broker.setAutoDetach(AutoDetach.DETACH_CLOSE);
        assertEquals(AutoDetach.DETACH_CLOSE, broker.getAutoDetach());
    }

    @Test(expected = UserException.class)
    public void testSetAutoDetach_detachNoneCombinedWithOtherFlagThrows() {
        // DETACH_NONE must be exclusive; combining it with another flag
        // should trigger assertAutoDetachValue's validation failure.
        broker.setAutoDetach(AutoDetach.DETACH_NONE | AutoDetach.DETACH_CLOSE);
    }

    // ------------------------------------------------------------------
    // 6. setMultithreaded / getMultithreaded
    // ------------------------------------------------------------------

    @Test
    public void testSetMultithreaded_togglesFlagCorrectly() {
        assertFalse(broker.getMultithreaded());
        broker.setMultithreaded(true);
        assertTrue(broker.getMultithreaded());
        broker.setMultithreaded(false);
        assertFalse(broker.getMultithreaded());
    }

    @Test
    public void testSetMultithreaded_repeatedTogglingDoesNotThrow() {
        try {
            broker.setMultithreaded(true);
            broker.setMultithreaded(false);
            broker.setMultithreaded(true);
            broker.setMultithreaded(true);
            broker.setMultithreaded(false);
        } catch (Exception e) {
            fail("Toggling multithreaded flag repeatedly should not throw: " + e);
        }
        assertFalse(broker.getMultithreaded());
    }

    // ------------------------------------------------------------------
    // 7. setRetainState / getRetainState
    // ------------------------------------------------------------------

    @Test
    public void testGetRetainState_defaultsToFalse() {
        assertFalse(broker.getRetainState());
    }

    @Test
    public void testSetRetainState_roundTrip() {
        broker.setRetainState(true);
        assertTrue(broker.getRetainState());
        broker.setRetainState(false);
        assertFalse(broker.getRetainState());
    }

    // ------------------------------------------------------------------
    // 8. setSuppressBatchOLELogging / getSuppressBatchOLELogging
    // ------------------------------------------------------------------

    @Test
    public void testGetSuppressBatchOLELogging_defaultIsFalse() {
        // NOTE: the method's Javadoc claims "Defaults to true", but the
        // backing field _suppressBatchOLELogging is declared as false.
        // This test documents the actual (buggy/undocumented) behavior.
        assertFalse(broker.getSuppressBatchOLELogging());
    }

    @Test
    public void testSetSuppressBatchOLELogging_roundTrip() {
        broker.setSuppressBatchOLELogging(true);
        assertTrue(broker.getSuppressBatchOLELogging());
        broker.setSuppressBatchOLELogging(false);
        assertFalse(broker.getSuppressBatchOLELogging());
    }

    // ------------------------------------------------------------------
    // 9. getCachePreparedQuery / setCachePreparedQuery
    // ------------------------------------------------------------------

    @Test(expected = NullPointerException.class)
    public void testGetCachePreparedQuery_defaultStateThrowsNPE() {
        // _cachePreparedQuery defaults to true, and the getter
        // unconditionally dereferences _conf.getQuerySQLCacheInstance()
        // when true. On an uninitialized broker _conf is null.
        broker.getCachePreparedQuery();
    }

    @Test
    public void testSetCachePreparedQuery_falseAvoidsConfDereference() {
        // When the flag is false, "&&" short-circuits before touching
        // the null _conf, so this should return false without throwing.
        broker.setCachePreparedQuery(false);
        assertFalse(broker.getCachePreparedQuery());
    }

    // ------------------------------------------------------------------
    // 10. setCacheFinderQuery / getCacheFinderQuery (documents a bug)
    // ------------------------------------------------------------------

    @Test(expected = NullPointerException.class)
    public void testSetCacheFinderQuery_bugDoesNotPreventNPE() {
        // BUG: setCacheFinderQuery(flag) mistakenly assigns to
        // _cachePreparedQuery instead of _cacheFinderQuery. As a result,
        // _cacheFinderQuery remains at its default value of true, and
        // getCacheFinderQuery() still dereferences the null _conf,
        // throwing NPE even after "disabling" the flag.
        broker.setCacheFinderQuery(false);
        broker.getCacheFinderQuery();
    }

    @Test
    public void testSetCacheFinderQuery_bugActuallyMutatesCachePreparedQuery() {
        // Demonstrates the side effect of the bug described above:
        // calling setCacheFinderQuery(false) actually flips
        // _cachePreparedQuery to false, which is observable via
        // getCachePreparedQuery() (short-circuits before the null _conf).
        broker.setCacheFinderQuery(false);
        assertFalse(broker.getCachePreparedQuery());
    }
}
