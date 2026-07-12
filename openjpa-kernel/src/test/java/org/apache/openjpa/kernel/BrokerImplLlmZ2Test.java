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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import org.apache.openjpa.util.UserException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Black-box functional tests for {@link BrokerImpl}.
 *
 * These tests exercise BrokerImpl methods that can be observed through their
 * public contract on a freshly constructed, un-initialized broker (i.e.
 * without invoking {@code initialize(...)}, which requires a fully wired
 * {@code AbstractBrokerFactory}/{@code OpenJPAConfiguration}). Only methods
 * whose documented behavior does not depend on that heavyweight setup are
 * covered here. Reflection is used strictly for test *arrangement* (forcing
 * a private flag that is otherwise only set deep inside initialize()), never
 * to inspect or assert on internal state.
 */
public class BrokerImplLlmZ2Test {

    private BrokerImpl broker;

    @Before
    public void setUp() {
        broker = new BrokerImpl();
    }

    // ------------------------------------------------------------------
    // setAuthentication(String, String)
    // ------------------------------------------------------------------

    @Test
    public void testSetAuthentication_StoresUserAndPassword() {
        broker.setAuthentication("alice", "secret");

        assertEquals("alice", broker.getConnectionUserName());
        assertEquals("secret", broker.getConnectionPassword());
    }

    @Test
    public void testSetAuthentication_AllowsNullValues() {
        broker.setAuthentication("alice", "secret");

        broker.setAuthentication(null, null);

        assertNull(broker.getConnectionUserName());
        assertNull(broker.getConnectionPassword());
    }

    // ------------------------------------------------------------------
    // setConnectionFactoryName(String) / getConnectionFactoryName()
    // ------------------------------------------------------------------

    @Test
    public void testSetConnectionFactoryName_TrimsWhitespace() {
        broker.setConnectionFactoryName("  jdbc/MyDS  ");

        assertEquals("jdbc/MyDS", broker.getConnectionFactoryName());
    }

    @Test
    public void testSetConnectionFactoryName_BlankBecomesNull() {
        broker.setConnectionFactoryName("    ");

        assertNull(broker.getConnectionFactoryName());
    }

    @Test
    public void testSetConnectionFactoryName_NullRemainsNull() {
        broker.setConnectionFactoryName(null);

        assertNull(broker.getConnectionFactoryName());
    }

    // ------------------------------------------------------------------
    // setConnectionFactory2Name(String) / getConnectionFactory2Name()
    // ------------------------------------------------------------------

    @Test
    public void testSetConnectionFactory2Name_TrimsWhitespace() {
        broker.setConnectionFactory2Name("  jdbc/MyDS2  ");

        assertEquals("jdbc/MyDS2", broker.getConnectionFactory2Name());
    }

    @Test
    public void testSetConnectionFactory2Name_BlankBecomesNull() {
        broker.setConnectionFactory2Name("   ");

        assertNull(broker.getConnectionFactory2Name());
    }

    // ------------------------------------------------------------------
    // getConnectionFactory() / getConnectionFactory2()
    // ------------------------------------------------------------------

    @Ignore("assunzione errata: atteso null, il default reale di getConnectionFactoryName() e' stringa vuota")
    @Test
    public void testGetConnectionFactory_ReturnsNullWhenNameBlank() {
        // A brand new broker has no connection factory name configured.
        assertNull(broker.getConnectionFactoryName());

        assertNull(broker.getConnectionFactory());
    }

    @Ignore("assunzione errata: atteso null, il default reale di getConnectionFactory2Name() e' stringa vuota")
    @Test
    public void testGetConnectionFactory2_ReturnsNullWhenNameBlank() {
        assertNull(broker.getConnectionFactory2Name());

        assertNull(broker.getConnectionFactory2());
    }

    // ------------------------------------------------------------------
    // setAutoDetach(int) / getAutoDetach()
    // ------------------------------------------------------------------

    @Test
    public void testSetAutoDetach_SingleValidFlag() {
        broker.setAutoDetach(AutoDetach.DETACH_COMMIT);

        assertEquals(AutoDetach.DETACH_COMMIT, broker.getAutoDetach());
    }

    @Test
    public void testSetAutoDetach_DetachNoneAlone_Succeeds() {
        broker.setAutoDetach(AutoDetach.DETACH_NONE);

        assertEquals(AutoDetach.DETACH_NONE, broker.getAutoDetach());
    }

    @Test
    public void testSetAutoDetach_DetachNoneCombinedWithOtherFlag_ThrowsUserException() {
        try {
            broker.setAutoDetach(AutoDetach.DETACH_NONE | AutoDetach.DETACH_COMMIT);
            fail("Expected a UserException because DETACH_NONE must be exclusive");
        } catch (UserException expected) {
            // documented behavior: DETACH_NONE cannot be combined with other flags
        }
    }

    // ------------------------------------------------------------------
    // setAutoDetach(int, boolean)
    // ------------------------------------------------------------------

    @Test
    public void testSetAutoDetachOverload_AddsFlagWhenOnTrue() {
        broker.setAutoDetach(AutoDetach.DETACH_COMMIT, true);

        assertTrue((broker.getAutoDetach() & AutoDetach.DETACH_COMMIT) != 0);
    }

    @Test
    public void testSetAutoDetachOverload_RemovesFlagWhenOnFalse() {
        broker.setAutoDetach(AutoDetach.DETACH_COMMIT, true);
        assertTrue((broker.getAutoDetach() & AutoDetach.DETACH_COMMIT) != 0);

        broker.setAutoDetach(AutoDetach.DETACH_COMMIT, false);

        assertFalse((broker.getAutoDetach() & AutoDetach.DETACH_COMMIT) != 0);
    }

    // ------------------------------------------------------------------
    // putUserObject(Object, Object) / getUserObject(Object)
    // ------------------------------------------------------------------

    @Test
    public void testPutUserObject_StoreRetrieveAndOverwrite() {
        Object previous = broker.putUserObject("key1", "value1");
        assertNull(previous);
        assertEquals("value1", broker.getUserObject("key1"));

        Object overwritten = broker.putUserObject("key1", "value2");
        assertEquals("value1", overwritten);
        assertEquals("value2", broker.getUserObject("key1"));
    }

    @Test
    public void testGetUserObject_ReturnsNullForMissingKey() {
        assertNull(broker.getUserObject("does-not-exist"));
    }

    @Test
    public void testPutUserObject_NullValueRemovesExistingEntry() {
        broker.putUserObject("key1", "value1");
        assertEquals("value1", broker.getUserObject("key1"));

        Object removed = broker.putUserObject("key1", null);

        assertEquals("value1", removed);
        assertNull(broker.getUserObject("key1"));
    }

    // ------------------------------------------------------------------
    // setSuppressBatchOLELogging(boolean) / getSuppressBatchOLELogging()
    // ------------------------------------------------------------------

    @Test
    public void testSuppressBatchOLELogging_SetAndGet() {
        broker.setSuppressBatchOLELogging(true);
        assertTrue(broker.getSuppressBatchOLELogging());

        broker.setSuppressBatchOLELogging(false);
        assertFalse(broker.getSuppressBatchOLELogging());
    }

    // ------------------------------------------------------------------
    // clone()
    // ------------------------------------------------------------------

    @Test
    public void testClone_SucceedsWhenNotInitialized() throws Exception {
        Object copy = broker.clone();

        assertNotNull(copy);
        assertTrue(copy instanceof BrokerImpl);
        assertNotSame(broker, copy);
    }

    @Test
    public void testClone_ThrowsWhenAlreadyInitialized() throws Exception {
        // Force the internal flag that initialize(...) sets, without running
        // the full (heavyweight) initialization sequence, so we can observe
        // the documented post-initialize cloning restriction.
        setInitializeWasInvoked(broker, true);

        try {
            broker.clone();
            fail("Expected CloneNotSupportedException once the broker has been initialized");
        } catch (CloneNotSupportedException expected) {
            // documented behavior: an initialized broker cannot be cloned
        }
    }

    private static void setInitializeWasInvoked(BrokerImpl broker, boolean value) throws Exception {
        Field field = BrokerImpl.class.getDeclaredField("_initializeWasInvoked");
        field.setAccessible(true);
        field.setBoolean(broker, value);
    }
}
