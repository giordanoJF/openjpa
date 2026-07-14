package org.apache.openjpa.trader.client;

import com.google.gwt.junit.client.GWTTestCase;

public class CostruttoreTest extends GWTTestCase {

    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    public void testSessionNull() {
        LoginDialog dialog = new LoginDialog(null);
        assertTrue(dialog.getWidget() != null);
        assertFalse(dialog.isShowing());
    }

    public void testSessionValida() {
        LoginDialog dialog = new LoginDialog(new OpenTrader());
        assertTrue(dialog.getWidget() != null);
        assertFalse(dialog.isShowing());
    }
}
