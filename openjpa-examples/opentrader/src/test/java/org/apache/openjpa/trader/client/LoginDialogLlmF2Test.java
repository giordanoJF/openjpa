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

public class LoginDialogLlmF2Test extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    private LoginDialog newDialog() {
        return new LoginDialog(new OpenTrader());
    }

    public void testPopupPositionAtZeroBoundary() {
        LoginDialog dialog = newDialog();
        dialog.show();
        dialog.setPopupPosition(0, 0);
        assertEquals(0, dialog.getPopupLeft());
        assertEquals(0, dialog.getPopupTop());
    }

    public void testPopupPositionNegativeValue() {
        LoginDialog dialog = newDialog();
        dialog.show();
        dialog.setPopupPosition(-1, -1);
        assertEquals(-1, dialog.getPopupLeft());
        assertEquals(-1, dialog.getPopupTop());
    }

    public void testTitleEmptyStringBoundary() {
        LoginDialog dialog = newDialog();
        dialog.setTitle("");
        assertEquals("", dialog.getTitle());
    }

    public void testGetServerURIOnSuccessWithEmptyUriDoesNotThrow() {
        OpenTrader session = new OpenTrader();
        session.setService(new NoOpServiceAsync());
        LoginDialog dialog = new LoginDialog(session);
        LoginDialog.GetServerURI callback = dialog.new GetServerURI();
        callback.onSuccess("");
        assertEquals(0, session.errorCount);
    }

    public void testGetServerURIOnFailurePropagatesToSession() {
        OpenTrader session = new OpenTrader();
        LoginDialog dialog = new LoginDialog(session);
        LoginDialog.GetServerURI callback = dialog.new GetServerURI();
        RuntimeException cause = new RuntimeException();
        callback.onFailure(cause);
        assertEquals(1, session.errorCount);
    }

    public void testInitializeStocksOnFailureDoesNotCallInit() {
        OpenTrader session = new OpenTrader();
        LoginDialog dialog = new LoginDialog(session);
        LoginDialog.InitializeStocks callback = dialog.new InitializeStocks();
        callback.onFailure(new RuntimeException());
        assertFalse(session.initCalled);
    }

    public void testHideTrueBehavesLikeHide() {
        LoginDialog dialog = newDialog();
        dialog.show();
        dialog.hide(true);
        assertFalse(dialog.isShowing());
    }

    public void testAutoHideEnabledBoundaryFalse() {
        LoginDialog dialog = newDialog();
        dialog.setAutoHideEnabled(false);
        assertFalse(dialog.isAutoHideEnabled());
    }

    public void testModalBoundaryTrue() {
        LoginDialog dialog = newDialog();
        dialog.setModal(true);
        assertTrue(dialog.isModal());
    }

    public void testConstructorBuildsWidget() {
        LoginDialog dialog = newDialog();
        assertNotNull(dialog.getWidget());
    }
}
