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

public class CicloDiVitaTest extends GWTTestCase {

    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    private LoginDialog newDialog() {
        return new LoginDialog(new OpenTrader());
    }

    public void testShowFromNonMostrato() {
        LoginDialog d = newDialog();
        d.show();
        assertTrue(d.isShowing());
    }

    public void testHideFromNonMostrato() {
        LoginDialog d = newDialog();
        d.hide();
        assertFalse(d.isShowing());
    }

    public void testHideTrueFromNonMostrato() {
        LoginDialog d = newDialog();
        d.hide(true);
        assertFalse(d.isShowing());
    }

    public void testCenterFromNonMostrato() {
        LoginDialog d = newDialog();
        d.center();
        assertTrue(d.isShowing());
    }

    public void testShowFromMostrato() {
        LoginDialog d = newDialog();
        d.show();
        d.show();
        assertTrue(d.isShowing());
    }

    public void testHideFromMostrato() {
        LoginDialog d = newDialog();
        d.show();
        d.hide();
        assertFalse(d.isShowing());
    }

    public void testHideTrueFromMostrato() {
        LoginDialog d = newDialog();
        d.show();
        d.hide(true);
        assertFalse(d.isShowing());
    }

    public void testCenterFromMostrato() {
        LoginDialog d = newDialog();
        d.show();
        d.center();
        assertTrue(d.isShowing());
    }
}
