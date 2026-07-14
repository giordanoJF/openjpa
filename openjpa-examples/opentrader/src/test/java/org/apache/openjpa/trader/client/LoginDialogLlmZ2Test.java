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

public class LoginDialogLlmZ2Test extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    public void testConstructorWithNullSessionDoesNotThrow() {
        LoginDialog dialog = new LoginDialog(null);
        assertTrue(dialog.getWidget() != null);
    }

    public void testConstructorWithValidSessionBuildsWidget() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        assertTrue(dialog.getWidget() != null);
    }

    public void testSetTitleNullIsRejectedOrNormalized() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        dialog.setTitle(null);
        assertEquals("", dialog.getTitle());
    }

    public void testSetTitleValidValue() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        dialog.setTitle("Login");
        assertEquals("Login", dialog.getTitle());
    }

    public void testSetPopupPositionRequiresAttach() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        dialog.show();
        dialog.setPopupPosition(5, 7);
        assertEquals(5, dialog.getPopupLeft());
        assertEquals(7, dialog.getPopupTop());
    }

    public void testShowThenIsShowingTrue() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        dialog.show();
        assertTrue(dialog.isShowing());
    }

    public void testHideThenIsShowingFalse() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        dialog.show();
        dialog.hide();
        assertFalse(dialog.isShowing());
    }

    public void testSetAutoHideEnabledTrue() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        dialog.setAutoHideEnabled(true);
        assertTrue(dialog.isAutoHideEnabled());
    }

    public void testSetVisibleFalse() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        dialog.setVisible(false);
        assertFalse(dialog.isVisible());
    }

    public void testGetServerURIOnFailureRecordsError() {
        OpenTrader session = new OpenTrader();
        LoginDialog dialog = new LoginDialog(session);
        LoginDialog.GetServerURI callback = dialog.new GetServerURI();
        RuntimeException failure = new RuntimeException("service unavailable");
        callback.onFailure(failure);
        assertSame(failure, session.lastError);
    }
}
