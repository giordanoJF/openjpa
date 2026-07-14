package org.apache.openjpa.trader.client;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Revised after the reported failure: every test threw UnsupportedOperationException from
 * GWT.create(), and the exception message itself says to check that the test case extends
 * GWTTestCase and does not call GWT.create() from an initializer or constructor. Converted to
 * GWTTestCase accordingly; GWTTestCase extends junit.framework.TestCase (JUnit 3), so the
 * @Test/@Before annotations are replaced with plain testXxx() methods and a constructor-time
 * setUp() override. getModuleName() must return the GWT module that owns this class; based on
 * the package (org.apache.openjpa.trader.client), the module is assumed to be
 * org.apache.openjpa.trader.client.LoginDialog.
 */
public class LoginDialogTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialog";
    }

    private OpenTrader session;
    private LoginDialog dialog;

    @Override
    protected void gwtSetUp() throws Exception {
        session = new OpenTrader();
        dialog = new LoginDialog(session);
    }

    public void testConstructorBuildsAWidget() {
        assertTrue(dialog.getWidget() != null);
    }

    public void testDialogIsNotShowingAfterConstruction() {
        assertFalse(dialog.isShowing());
    }

    public void testShowMakesDialogVisible() {
        dialog.show();
        assertTrue(dialog.isShowing());
    }

    public void testHideMakesDialogInvisible() {
        dialog.show();
        dialog.hide();
        assertFalse(dialog.isShowing());
    }

    public void testSetTitleAndGetTitleRoundTrip() {
        dialog.setTitle("OpenTrader Login");
        assertEquals("OpenTrader Login", dialog.getTitle());
    }

    public void testSetPopupPositionAndGetters() {
        dialog.show();
        dialog.setPopupPosition(10, 20);
        assertEquals(10, dialog.getPopupLeft());
        assertEquals(20, dialog.getPopupTop());
    }

    public void testSetModalTrue() {
        dialog.setModal(true);
        assertTrue(dialog.isModal());
    }

    public void testSetAnimationEnabledFalse() {
        dialog.setAnimationEnabled(false);
        assertFalse(dialog.isAnimationEnabled());
    }

    public void testSetGlassEnabledTrue() {
        dialog.setGlassEnabled(true);
        assertTrue(dialog.isGlassEnabled());
    }

    public void testCenterShowsDialog() {
        dialog.center();
        assertTrue(dialog.isShowing());
    }
}
