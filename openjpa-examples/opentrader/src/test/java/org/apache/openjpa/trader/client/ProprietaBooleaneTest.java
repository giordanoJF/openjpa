package org.apache.openjpa.trader.client;

import com.google.gwt.junit.client.GWTTestCase;

public class ProprietaBooleaneTest extends GWTTestCase {

    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    private LoginDialog newDialog() {
        return new LoginDialog(new OpenTrader());
    }

    public void testModalTrue()  { LoginDialog d = newDialog(); d.setModal(true);  assertTrue(d.isModal()); }
    public void testModalFalse() { LoginDialog d = newDialog(); d.setModal(false); assertFalse(d.isModal()); }

    public void testAnimationEnabledTrue()  { LoginDialog d = newDialog(); d.setAnimationEnabled(true);  assertTrue(d.isAnimationEnabled()); }
    public void testAnimationEnabledFalse() { LoginDialog d = newDialog(); d.setAnimationEnabled(false); assertFalse(d.isAnimationEnabled()); }

    public void testAutoHideEnabledTrue()  { LoginDialog d = newDialog(); d.setAutoHideEnabled(true);  assertTrue(d.isAutoHideEnabled()); }
    public void testAutoHideEnabledFalse() { LoginDialog d = newDialog(); d.setAutoHideEnabled(false); assertFalse(d.isAutoHideEnabled()); }

    public void testGlassEnabledTrue()  { LoginDialog d = newDialog(); d.setGlassEnabled(true);  assertTrue(d.isGlassEnabled()); }
    public void testGlassEnabledFalse() { LoginDialog d = newDialog(); d.setGlassEnabled(false); assertFalse(d.isGlassEnabled()); }

    public void testVisibleTrue()  { LoginDialog d = newDialog(); d.setVisible(true);  assertTrue(d.isVisible()); }
    public void testVisibleFalse() { LoginDialog d = newDialog(); d.setVisible(false); assertFalse(d.isVisible()); }
}
