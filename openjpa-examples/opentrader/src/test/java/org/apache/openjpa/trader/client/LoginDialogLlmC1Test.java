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
import java.util.Collections;

import org.apache.openjpa.trader.domain.Stock;

public class LoginDialogLlmC1Test extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    public void testConstructorWithValidSessionDoesNotThrow() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        assertNotNull(dialog);
    }

    public void testConstructorWithNullSessionDoesNotThrow() {
        LoginDialog dialog = new LoginDialog(null);
        assertNotNull(dialog);
    }

    public void testSetPopupPositionRoundTrip() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        dialog.show();
        dialog.setPopupPosition(3, 4);
        assertEquals(3, dialog.getPopupLeft());
        assertEquals(4, dialog.getPopupTop());
    }

    public void testSetTitleNull() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        dialog.setTitle(null);
        assertEquals("", dialog.getTitle());
    }

    public void testSetModalRoundTrip() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        dialog.setModal(true);
        assertTrue(dialog.isModal());
    }

    public void testSetVisibleRoundTrip() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        dialog.setVisible(false);
        assertFalse(dialog.isVisible());
    }

    public void testShowSetsIsShowingTrue() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        dialog.show();
        assertTrue(dialog.isShowing());
    }

    public void testHideSetsIsShowingFalse() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        dialog.show();
        dialog.hide();
        assertFalse(dialog.isShowing());
    }

    public void testGetServerURIOnFailureRecordsErrorOnSession() {
        OpenTrader session = new OpenTrader();
        LoginDialog dialog = new LoginDialog(session);
        LoginDialog.GetServerURI callback = dialog.new GetServerURI();
        RuntimeException cause = new RuntimeException();
        callback.onFailure(cause);
        assertSame(cause, session.lastError);
    }

    public void testInitializeStocksOnSuccessNotifiesSession() {
        OpenTrader session = new OpenTrader();
        LoginDialog dialog = new LoginDialog(session);
        LoginDialog.InitializeStocks callback = dialog.new InitializeStocks();
        callback.onSuccess(Collections.<Stock>emptyList());
        assertTrue(session.initCalled);
    }
}
