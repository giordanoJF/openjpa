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

import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.trader.domain.Sector;
import org.apache.openjpa.trader.domain.Stock;

import com.google.gwt.junit.client.GWTTestCase;

public class InitializeStocksTest extends GWTTestCase {

    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    private LoginDialog.InitializeStocks newCallback(OpenTrader session) {
        LoginDialog dialog = new LoginDialog(session);
        return dialog.new InitializeStocks();
    }

    public void testOnFailure() {
        OpenTrader session = new OpenTrader();
        LoginDialog.InitializeStocks callback = newCallback(session);

        RuntimeException caught = new RuntimeException();
        callback.onFailure(caught);

        assertEquals(1, session.errorCount);
        assertSame(caught, session.lastError);
        assertFalse(session.initCalled);
    }

    public void testOnSuccessNull() {
        OpenTrader session = new OpenTrader();
        LoginDialog.InitializeStocks callback = newCallback(session);

        callback.onSuccess(null);

        assertTrue(session.initCalled);
        assertNull(session.initStocks);
    }

    public void testOnSuccessVuota() {
        OpenTrader session = new OpenTrader();
        LoginDialog.InitializeStocks callback = newCallback(session);

        callback.onSuccess(new ArrayList<Stock>());

        assertTrue(session.initCalled);
        assertEquals(0, session.initStocks.size());
    }

    public void testOnSuccessConElementi() {
        OpenTrader session = new OpenTrader();
        LoginDialog.InitializeStocks callback = newCallback(session);
        List<Stock> stocks = new ArrayList<Stock>();
        stocks.add(new Stock("OJPA", "OpenJPA Inc.", Sector.INFRASTRUCTURE, 42.0));

        callback.onSuccess(stocks);

        assertTrue(session.initCalled);
        assertEquals(1, session.initStocks.size());
    }
}
