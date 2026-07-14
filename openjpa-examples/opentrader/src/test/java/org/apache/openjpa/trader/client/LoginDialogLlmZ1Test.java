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

package org.apache.openjpa.trader.client;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Revised after the reported failure: every test threw UnsupportedOperationException from
 * GWT.create(), and the exception message itself says to check that the test case extends
 * GWTTestCase and does not call GWT.create() from an initializer or constructor. Converted to
 * GWTTestCase accordingly; GWTTestCase extends junit.framework.TestCase (JUnit 3), so the
 * @Test/@Before annotations are replaced with plain testXxx() methods and a constructor-time
 * setUp() override. getModuleName() must return the GWT module that owns this class; based on
 * the package (org.apache.openjpa.trader.client), the module is assumed to be
 * org.apache.openjpa.trader.client.LoginDialog.
 */
public class LoginDialogLlmZ1Test extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    private OpenTrader session;
    private LoginDialog dialog;

    @Override
    protected void gwtSetUp() throws Exception {
        session = new OpenTrader();
        dialog = new LoginDialog(session);
    }

    public void testConstructorBuildsAWidget() {
        assertTrue(dialog.getWidget() != null);
    }

    public void testDialogIsNotShowingAfterConstruction() {
        assertFalse(dialog.isShowing());
    }

    public void testShowMakesDialogVisible() {
        dialog.show();
        assertTrue(dialog.isShowing());
    }

    public void testHideMakesDialogInvisible() {
        dialog.show();
        dialog.hide();
        assertFalse(dialog.isShowing());
    }

    public void testSetTitleAndGetTitleRoundTrip() {
        dialog.setTitle("OpenTrader Login");
        assertEquals("OpenTrader Login", dialog.getTitle());
    }

    public void testSetPopupPositionAndGetters() {
        dialog.show();
        dialog.setPopupPosition(10, 20);
        assertEquals(10, dialog.getPopupLeft());
        assertEquals(20, dialog.getPopupTop());
    }

    public void testSetModalTrue() {
        dialog.setModal(true);
        assertTrue(dialog.isModal());
    }

    public void testSetAnimationEnabledFalse() {
        dialog.setAnimationEnabled(false);
        assertFalse(dialog.isAnimationEnabled());
    }

    public void testSetGlassEnabledTrue() {
        dialog.setGlassEnabled(true);
        assertTrue(dialog.isGlassEnabled());
    }

    public void testCenterShowsDialog() {
        dialog.center();
        assertTrue(dialog.isShowing());
    }
}
