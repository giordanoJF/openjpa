package org.apache.openjpa.trader.client;

import com.google.gwt.junit.client.GWTTestCase;

public class GetServerURITest extends GWTTestCase {

    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    private LoginDialog.GetServerURI newCallback(OpenTrader session, NoOpServiceAsync service) {
        session.setService(service);
        LoginDialog dialog = new LoginDialog(session);
        return dialog.new GetServerURI();
    }

    public void testOnFailure() {
        OpenTrader session = new OpenTrader();
        LoginDialog dialog = new LoginDialog(session);
        LoginDialog.GetServerURI callback = dialog.new GetServerURI();

        RuntimeException caught = new RuntimeException();
        callback.onFailure(caught);

        assertEquals(1, session.errorCount);
        assertSame(caught, session.lastError);
    }

    public void testOnSuccessNull() {
        OpenTrader session = new OpenTrader();
        NoOpServiceAsync service = new NoOpServiceAsync();
        LoginDialog.GetServerURI callback = newCallback(session, service);

        callback.onSuccess(null);

        assertTrue(service.getStocksCalled);
        assertEquals(0, session.errorCount);
    }

    public void testOnSuccessVuota() {
        OpenTrader session = new OpenTrader();
        NoOpServiceAsync service = new NoOpServiceAsync();
        LoginDialog.GetServerURI callback = newCallback(session, service);

        callback.onSuccess("");

        assertTrue(service.getStocksCalled);
        assertEquals(0, session.errorCount);
    }

    public void testOnSuccessValida() {
        OpenTrader session = new OpenTrader();
        NoOpServiceAsync service = new NoOpServiceAsync();
        LoginDialog.GetServerURI callback = newCallback(session, service);

        callback.onSuccess("http://localhost:8080/opentrader");

        assertTrue(service.getStocksCalled);
        assertEquals(0, session.errorCount);
    }

    public void testOnSuccessNonValida() {
        OpenTrader session = new OpenTrader();
        NoOpServiceAsync service = new NoOpServiceAsync();
        LoginDialog.GetServerURI callback = newCallback(session, service);

        callback.onSuccess("not-a-valid-uri");

        assertTrue(service.getStocksCalled);
        assertEquals(0, session.errorCount);
    }

    public void testOnSuccessValidaNonCorretta() {
        OpenTrader session = new OpenTrader();
        NoOpServiceAsync service = new NoOpServiceAsync();
        LoginDialog.GetServerURI callback = newCallback(session, service);

        callback.onSuccess("http://server.invalid/opentrader");

        assertTrue(service.getStocksCalled);
        assertEquals(0, session.errorCount);
    }
}
