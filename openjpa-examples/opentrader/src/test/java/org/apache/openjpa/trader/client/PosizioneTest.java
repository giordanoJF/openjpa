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

public class PosizioneTest extends GWTTestCase {

    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    private LoginDialog shownDialog() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        dialog.show();
        return dialog;
    }

    private void assertPosition(int left, int top) {
        LoginDialog dialog = shownDialog();
        dialog.setPopupPosition(left, top);
        assertEquals(left, dialog.getPopupLeft());
        assertEquals(top, dialog.getPopupTop());
    }

    public void testNegativoNegativo() { assertPosition(-1, -1); }
    public void testNegativoZero()     { assertPosition(-1, 0); }
    public void testNegativoPositivo() { assertPosition(-1, 1); }
    public void testZeroNegativo()     { assertPosition(0, -1); }
    public void testZeroZero()         { assertPosition(0, 0); }
    public void testZeroPositivo()     { assertPosition(0, 1); }
    public void testPositivoNegativo() { assertPosition(1, -1); }
    public void testPositivoZero()     { assertPosition(1, 0); }
    public void testPositivoPositivo() { assertPosition(1, 1); }
}
