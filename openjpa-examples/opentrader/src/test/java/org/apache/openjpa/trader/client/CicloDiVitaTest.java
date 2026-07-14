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
