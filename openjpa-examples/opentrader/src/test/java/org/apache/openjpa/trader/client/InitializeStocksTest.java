package org.apache.openjpa.trader.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.trader.domain.Sector;
import org.apache.openjpa.trader.domain.Stock;

import com.google.gwt.junit.client.GWTTestCase;

public class InitializeStocksTest extends GWTTestCase {

    public String getModuleName() {
        return "org.apache.openjpa.trader.LoginDialogTest";
    }

    private LoginDialog.InitializeStocks newCallback(OpenTrader session) {
        LoginDialog dialog = new LoginDialog(session);
        return dialog.new InitializeStocks();
    }

    public void testOnFailure() {
        OpenTrader session = new OpenTrader();
        LoginDialog.InitializeStocks callback = newCallback(session);

        RuntimeException caught = new RuntimeException();
        callback.onFailure(caught);

        assertEquals(1, session.errorCount);
        assertSame(caught, session.lastError);
        assertFalse(session.initCalled);
    }

    public void testOnSuccessNull() {
        OpenTrader session = new OpenTrader();
        LoginDialog.InitializeStocks callback = newCallback(session);

        callback.onSuccess(null);

        assertTrue(session.initCalled);
        assertNull(session.initStocks);
    }

    public void testOnSuccessVuota() {
        OpenTrader session = new OpenTrader();
        LoginDialog.InitializeStocks callback = newCallback(session);

        callback.onSuccess(new ArrayList<Stock>());

        assertTrue(session.initCalled);
        assertEquals(0, session.initStocks.size());
    }

    public void testOnSuccessConElementi() {
        OpenTrader session = new OpenTrader();
        LoginDialog.InitializeStocks callback = newCallback(session);
        List<Stock> stocks = new ArrayList<Stock>();
        stocks.add(new Stock("OJPA", "OpenJPA Inc.", Sector.INFRASTRUCTURE, 42.0));

        callback.onSuccess(stocks);

        assertTrue(session.initCalled);
        assertEquals(1, session.initStocks.size());
    }
}
