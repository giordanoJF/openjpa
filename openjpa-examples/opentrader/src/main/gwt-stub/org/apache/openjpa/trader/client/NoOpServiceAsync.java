/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.trader.client;

import java.sql.Timestamp;
import java.util.List;

import org.apache.openjpa.trader.domain.Ask;
import org.apache.openjpa.trader.domain.Bid;
import org.apache.openjpa.trader.domain.LogStatement;
import org.apache.openjpa.trader.domain.Match;
import org.apache.openjpa.trader.domain.Stock;
import org.apache.openjpa.trader.domain.Tradable;
import org.apache.openjpa.trader.domain.Trade;
import org.apache.openjpa.trader.domain.Trader;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class NoOpServiceAsync implements TradingServiceAdapterAsync {

    public boolean getStocksCalled;

    public void getStocks(AsyncCallback<List<Stock>> callback) {
        getStocksCalled = true;
    }

    public void ask(Trader trader, Stock stock, int volume, double price, AsyncCallback<Ask> callback) {
        throw new UnsupportedOperationException();
    }

    public void bid(Trader trader, Stock stock, int volume, double price, AsyncCallback<Bid> callback) {
        throw new UnsupportedOperationException();
    }

    public void getStock(String symbol, AsyncCallback<Stock> callback) {
        throw new UnsupportedOperationException();
    }

    public void getTrades(Timestamp from, Timestamp to, AsyncCallback<List<Trade>> callback) {
        throw new UnsupportedOperationException();
    }

    public void getTrades(Trader trader, Boolean boughtOrsold, Timestamp from, Timestamp to,
            AsyncCallback<List<Trade>> callback) {
        throw new UnsupportedOperationException();
    }

    public void login(String trader, AsyncCallback<Trader> callback) {
        throw new UnsupportedOperationException();
    }

    public void matchAsk(Ask ask, AsyncCallback<List<Match>> callback) {
        throw new UnsupportedOperationException();
    }

    public void matchBid(Bid bid, AsyncCallback<List<Match>> callback) {
        throw new UnsupportedOperationException();
    }

    public void trade(Match match, AsyncCallback<Trade> callback) {
        throw new UnsupportedOperationException();
    }

    public void getLog(AsyncCallback<List<LogStatement>> callback) {
        throw new UnsupportedOperationException();
    }

    public void withdraw(Tradable t, AsyncCallback<Tradable> callback) {
        throw new UnsupportedOperationException();
    }

    public void refresh(Tradable t, AsyncCallback<Tradable> callback) {
        throw new UnsupportedOperationException();
    }

    public void getServiceURI(AsyncCallback<String> callback) {
        throw new UnsupportedOperationException();
    }
}
