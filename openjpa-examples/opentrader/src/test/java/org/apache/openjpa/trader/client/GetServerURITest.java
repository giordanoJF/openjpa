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

public class GetServerURITest extends GWTTestCase {

    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    private LoginDialog.GetServerURI newCallback(OpenTrader session, NoOpServiceAsync service) {
        session.setService(service);
        LoginDialog dialog = new LoginDialog(session);
        return dialog.new GetServerURI();
    }

    public void testOnFailure() {
        OpenTrader session = new OpenTrader();
        LoginDialog dialog = new LoginDialog(session);
        LoginDialog.GetServerURI callback = dialog.new GetServerURI();

        RuntimeException caught = new RuntimeException();
        callback.onFailure(caught);

        assertEquals(1, session.errorCount);
        assertSame(caught, session.lastError);
    }

    public void testOnSuccessNull() {
        OpenTrader session = new OpenTrader();
        NoOpServiceAsync service = new NoOpServiceAsync();
        LoginDialog.GetServerURI callback = newCallback(session, service);

        callback.onSuccess(null);

        assertTrue(service.getStocksCalled);
        assertEquals(0, session.errorCount);
    }

    public void testOnSuccessVuota() {
        OpenTrader session = new OpenTrader();
        NoOpServiceAsync service = new NoOpServiceAsync();
        LoginDialog.GetServerURI callback = newCallback(session, service);

        callback.onSuccess("");

        assertTrue(service.getStocksCalled);
        assertEquals(0, session.errorCount);
    }

    public void testOnSuccessValida() {
        OpenTrader session = new OpenTrader();
        NoOpServiceAsync service = new NoOpServiceAsync();
        LoginDialog.GetServerURI callback = newCallback(session, service);

        callback.onSuccess("http://localhost:8080/opentrader");

        assertTrue(service.getStocksCalled);
        assertEquals(0, session.errorCount);
    }

    public void testOnSuccessNonValida() {
        OpenTrader session = new OpenTrader();
        NoOpServiceAsync service = new NoOpServiceAsync();
        LoginDialog.GetServerURI callback = newCallback(session, service);

        callback.onSuccess("not-a-valid-uri");

        assertTrue(service.getStocksCalled);
        assertEquals(0, session.errorCount);
    }

    public void testOnSuccessValidaNonCorretta() {
        OpenTrader session = new OpenTrader();
        NoOpServiceAsync service = new NoOpServiceAsync();
        LoginDialog.GetServerURI callback = newCallback(session, service);

        callback.onSuccess("http://server.invalid/opentrader");

        assertTrue(service.getStocksCalled);
        assertEquals(0, session.errorCount);
    }
}
