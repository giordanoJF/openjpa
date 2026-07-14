package org.apache.openjpa.trader.client;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * JUnit 4-style test cases (adapted to GWTTestCase per the environment note) for LoginDialog,
 * following the style of the attached Calculator.add example: construct, act, assertEquals.
 */
public class LoginDialogTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    private LoginDialog newDialog() {
        return new LoginDialog(new OpenTrader());
    }

    public void testGetPopupLeftMatchesSetValue() {
        LoginDialog dialog = newDialog();
        dialog.show();
        dialog.setPopupPosition(42, 0);
        assertEquals(42, dialog.getPopupLeft());
    }

    public void testGetPopupTopMatchesSetValue() {
        LoginDialog dialog = newDialog();
        dialog.show();
        dialog.setPopupPosition(0, 99);
        assertEquals(99, dialog.getPopupTop());
    }

    public void testGetTitleMatchesSetValue() {
        LoginDialog dialog = newDialog();
        dialog.setTitle("test title");
        assertEquals("test title", dialog.getTitle());
    }

    public void testIsModalMatchesSetValue() {
        LoginDialog dialog = newDialog();
        dialog.setModal(false);
        assertFalse(dialog.isModal());
    }

    public void testIsAnimationEnabledMatchesSetValue() {
        LoginDialog dialog = newDialog();
        dialog.setAnimationEnabled(true);
        assertTrue(dialog.isAnimationEnabled());
    }

    public void testIsGlassEnabledMatchesSetValue() {
        LoginDialog dialog = newDialog();
        dialog.setGlassEnabled(true);
        assertTrue(dialog.isGlassEnabled());
    }

    public void testIsVisibleMatchesSetValue() {
        LoginDialog dialog = newDialog();
        dialog.setVisible(true);
        assertTrue(dialog.isVisible());
    }

    public void testIsShowingAfterShow() {
        LoginDialog dialog = newDialog();
        dialog.show();
        assertTrue(dialog.isShowing());
    }

    public void testIsShowingAfterCenter() {
        LoginDialog dialog = newDialog();
        dialog.center();
        assertTrue(dialog.isShowing());
    }

    public void testInitializeStocksOnSuccessCallsInit() {
        OpenTrader session = new OpenTrader();
        LoginDialog dialog = new LoginDialog(session);
        LoginDialog.InitializeStocks callback = dialog.new InitializeStocks();
        callback.onSuccess(null);
        assertTrue(session.initCalled);
    }
}
