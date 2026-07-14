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

import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.trader.domain.Stock;
import org.apache.openjpa.trader.domain.Trader;

/**
 * Test double for the real {@code OpenTrader} entry point, limited to the surface
 * {@link LoginDialog} calls: {@link #getService()}, {@link #init(Trader, String, List)},
 * {@link #handleError(Throwable)}. The real class depends on org.cobogw.gwt, which is not
 * resolvable from Maven Central, so it cannot be compiled as part of this module. Records
 * every call it receives so tests can verify interactions without Mockito, which cannot be
 * used inside GWTTestCase test methods (they are translated to JavaScript by the GWT compiler).
 */
public class OpenTrader {

    private TradingServiceAdapterAsync service;
    public boolean initCalled;
    public Trader initTrader;
    public String initServerURI;
    public List<Stock> initStocks;
    public Throwable lastError;
    public int errorCount;

    public void setService(TradingServiceAdapterAsync service) {
        this.service = service;
    }

    public TradingServiceAdapterAsync getService() {
        return service;
    }

    void init(Trader trader, String uri, List<Stock> stocks) {
        initCalled = true;
        initTrader = trader;
        initServerURI = uri;
        initStocks = stocks == null ? null : new ArrayList<Stock>(stocks);
    }

    void handleError(Throwable t) {
        lastError = t;
        errorCount++;
    }
}
