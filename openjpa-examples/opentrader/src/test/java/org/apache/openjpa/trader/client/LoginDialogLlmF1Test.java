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
 * JUnit 4-style test cases (adapted to GWTTestCase per the environment note) for LoginDialog,
 * following the style of the attached Calculator.add example: construct, act, assertEquals.
 */
public class LoginDialogLlmF1Test extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    private LoginDialog newDialog() {
        return new LoginDialog(new OpenTrader());
    }

    public void testGetPopupLeftMatchesSetValue() {
        LoginDialog dialog = newDialog();
        dialog.show();
        dialog.setPopupPosition(42, 0);
        assertEquals(42, dialog.getPopupLeft());
    }

    public void testGetPopupTopMatchesSetValue() {
        LoginDialog dialog = newDialog();
        dialog.show();
        dialog.setPopupPosition(0, 99);
        assertEquals(99, dialog.getPopupTop());
    }

    public void testGetTitleMatchesSetValue() {
        LoginDialog dialog = newDialog();
        dialog.setTitle("test title");
        assertEquals("test title", dialog.getTitle());
    }

    public void testIsModalMatchesSetValue() {
        LoginDialog dialog = newDialog();
        dialog.setModal(false);
        assertFalse(dialog.isModal());
    }

    public void testIsAnimationEnabledMatchesSetValue() {
        LoginDialog dialog = newDialog();
        dialog.setAnimationEnabled(true);
        assertTrue(dialog.isAnimationEnabled());
    }

    public void testIsGlassEnabledMatchesSetValue() {
        LoginDialog dialog = newDialog();
        dialog.setGlassEnabled(true);
        assertTrue(dialog.isGlassEnabled());
    }

    public void testIsVisibleMatchesSetValue() {
        LoginDialog dialog = newDialog();
        dialog.setVisible(true);
        assertTrue(dialog.isVisible());
    }

    public void testIsShowingAfterShow() {
        LoginDialog dialog = newDialog();
        dialog.show();
        assertTrue(dialog.isShowing());
    }

    public void testIsShowingAfterCenter() {
        LoginDialog dialog = newDialog();
        dialog.center();
        assertTrue(dialog.isShowing());
    }

    public void testInitializeStocksOnSuccessCallsInit() {
        OpenTrader session = new OpenTrader();
        LoginDialog dialog = new LoginDialog(session);
        LoginDialog.InitializeStocks callback = dialog.new InitializeStocks();
        callback.onSuccess(null);
        assertTrue(session.initCalled);
    }
}
