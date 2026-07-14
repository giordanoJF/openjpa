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
import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.trader.domain.Sector;
import org.apache.openjpa.trader.domain.Stock;

/**
 * Tests for LoginDialog, adapted to GWTTestCase per the environment note, following the
 * reasoning above and the attached style-reference example.
 */
public class LoginDialogLlmC2Test extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    private LoginDialog newDialog() {
        return new LoginDialog(new OpenTrader());
    }

    public void testConstructorBuildsWidgetImmediately() {
        LoginDialog dialog = newDialog();
        assertTrue(dialog.getWidget() != null);
    }

    public void testDialogNotShowingRightAfterConstruction() {
        LoginDialog dialog = newDialog();
        assertFalse(dialog.isShowing());
    }

    public void testGetTitleAfterSetTitle() {
        LoginDialog dialog = newDialog();
        dialog.setTitle("OpenTrader");
        assertEquals("OpenTrader", dialog.getTitle());
    }

    public void testGetPopupLeftAndTopAfterSet() {
        LoginDialog dialog = newDialog();
        dialog.show();
        dialog.setPopupPosition(1, 2);
        assertEquals(1, dialog.getPopupLeft());
        assertEquals(2, dialog.getPopupTop());
    }

    public void testAutoHideEnabledAfterSetTrue() {
        LoginDialog dialog = newDialog();
        dialog.setAutoHideEnabled(true);
        assertTrue(dialog.isAutoHideEnabled());
    }

    public void testCenterMakesDialogVisible() {
        LoginDialog dialog = newDialog();
        dialog.center();
        assertTrue(dialog.isShowing());
    }

    public void testHideWithTrueArgumentAlsoHidesDialog() {
        LoginDialog dialog = newDialog();
        dialog.show();
        dialog.hide(true);
        assertFalse(dialog.isShowing());
    }

    public void testInitializeStocksOnSuccessPassesStocksListThrough() {
        OpenTrader session = new OpenTrader();
        LoginDialog dialog = new LoginDialog(session);
        LoginDialog.InitializeStocks callback = dialog.new InitializeStocks();
        List<Stock> stocks = new ArrayList<Stock>();
        stocks.add(new Stock("ACME", "Acme Corp", Sector.INFRASTRUCTURE, 10.0));
        callback.onSuccess(stocks);
        assertTrue(session.initCalled);
        assertEquals(1, session.initStocks.size());
    }

    public void testInitializeStocksOnFailureRecordsError() {
        OpenTrader session = new OpenTrader();
        LoginDialog dialog = new LoginDialog(session);
        LoginDialog.InitializeStocks callback = dialog.new InitializeStocks();
        RuntimeException cause = new RuntimeException();
        callback.onFailure(cause);
        assertSame(cause, session.lastError);
    }

    public void testGetServerURIOnSuccessDoesNotThrowForPlausibleUri() {
        OpenTrader session = new OpenTrader();
        session.setService(new NoOpServiceAsync());
        LoginDialog dialog = new LoginDialog(session);
        LoginDialog.GetServerURI callback = dialog.new GetServerURI();
        callback.onSuccess("http://localhost:8080/opentrader");
        assertEquals(0, session.errorCount);
    }
}
