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
