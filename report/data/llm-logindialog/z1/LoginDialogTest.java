package org.apache.openjpa.trader.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link LoginDialog}.
 *
 * LoginDialog's constructor requires an OpenTrader collaborator that is not part of the
 * attached source; it is mocked with Mockito since only its usage (getService()) is visible
 * from LoginDialog's own code.
 */
public class LoginDialogTest {

    private OpenTrader session;
    private LoginDialog dialog;

    @Before
    public void setUp() {
        session = mock(OpenTrader.class);
        dialog = new LoginDialog(session);
    }

    @Test
    public void testConstructorBuildsAWidget() {
        assertTrue(dialog.getWidget() != null);
    }

    @Test
    public void testDialogIsNotShowingAfterConstruction() {
        assertFalse(dialog.isShowing());
    }

    @Test
    public void testShowMakesDialogVisible() {
        dialog.show();
        assertTrue(dialog.isShowing());
    }

    @Test
    public void testHideMakesDialogInvisible() {
        dialog.show();
        dialog.hide();
        assertFalse(dialog.isShowing());
    }

    @Test
    public void testSetTitleAndGetTitleRoundTrip() {
        dialog.setTitle("OpenTrader Login");
        assertEquals("OpenTrader Login", dialog.getTitle());
    }

    @Test
    public void testSetPopupPositionAndGetters() {
        dialog.setPopupPosition(10, 20);
        assertEquals(10, dialog.getPopupLeft());
        assertEquals(20, dialog.getPopupTop());
    }

    @Test
    public void testSetModalTrue() {
        dialog.setModal(true);
        assertTrue(dialog.isModal());
    }

    @Test
    public void testSetAnimationEnabledFalse() {
        dialog.setAnimationEnabled(false);
        assertFalse(dialog.isAnimationEnabled());
    }

    @Test
    public void testSetGlassEnabledTrue() {
        dialog.setGlassEnabled(true);
        assertTrue(dialog.isGlassEnabled());
    }

    @Test
    public void testCenterShowsDialog() {
        dialog.center();
        assertTrue(dialog.isShowing());
    }
}
