package org.apache.openjpa.trader.client;

import com.google.gwt.junit.client.GWTTestCase;

public class TitoloTest extends GWTTestCase {

    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    private LoginDialog newDialog() {
        return new LoginDialog(new OpenTrader());
    }

    public void testTitleNull() {
        LoginDialog dialog = newDialog();
        dialog.setTitle(null);
        assertEquals("", dialog.getTitle());
    }

    public void testTitleVuota() {
        LoginDialog dialog = newDialog();
        dialog.setTitle("");
        assertEquals("", dialog.getTitle());
    }

    public void testTitleValida() {
        LoginDialog dialog = newDialog();
        dialog.setTitle("OpenTrader Login");
        assertEquals("OpenTrader Login", dialog.getTitle());
    }
}
