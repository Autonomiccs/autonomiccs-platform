/*
 * This program is part of Autonomiccs "autonomic-platform",
 * an open source autonomic cloud computing management platform.
 * Copyright (C) 2016 Autonomiccs, Inc.
 *
 * Licensed to the Autonomiccs, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The Autonomiccs, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package br.com.autonomiccs.autonomic.plugin.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class HttpUtilsTest {

    private HttpUtils spy;
    private URL url;

    @Before
    public void setup() {
        spy = Mockito.spy(new HttpUtils());
        url = PowerMockito.mock(URL.class);
    }

    @Test
    public void wakeHaltedHostUsingHttpGetTest() throws Exception {
        Mockito.doReturn("result").when(spy).executeHttpGetRequest(Mockito.any(URL.class));
        String result = spy.wakeHaltedHostUsingHttpGet("systemVmIp", "hostMac");
        verifyWakeHaltedHostUsingHttpGetTest("result", result);
    }

    @Test
    public void wakeHaltedHostUsingHttpGetTestCatchIOException() throws Exception {
        configureWakeHaltedHostUsingHttpGetTest();
        Mockito.doThrow(IOException.class).when(spy).executeHttpGetRequest(Mockito.any(URL.class));

        String result = spy.wakeHaltedHostUsingHttpGet("systemVmIp", "hostMac");

        verifyWakeHaltedHostUsingHttpGetTest(null, result);
    }

    @Test
    @PrepareForTest({ HttpUtils.class, IOUtils.class })
    public void executeHttpGetRequestTest() throws Exception {
        HttpURLConnection httpConn = configureExecuteHttpGetRequestTest(url, 200);
        spy.executeHttpGetRequest(url);
        verifyExecuteHttpGetRequestTest(httpConn, 1);
    }

    @Test
    @PrepareForTest({ HttpUtils.class, IOUtils.class })
    public void executeHttpGetRequestTestErrorCode() throws Exception {
        HttpURLConnection httpConn = configureExecuteHttpGetRequestTest(url, 400);

        String result = spy.executeHttpGetRequest(url);

        verifyExecuteHttpGetRequestTest(httpConn, 0);
        Assert.assertEquals(String.format("Error in HTTP GET : code [%d]", 400), result);
    }

    private HttpURLConnection configureExecuteHttpGetRequestTest(URL url, int responseCode) throws IOException {
        InputStream inStream = Mockito.mock(InputStream.class);
        PowerMockito.mockStatic(IOUtils.class);
        HttpURLConnection httpConn = Mockito.mock(HttpURLConnection.class);
        Mockito.doReturn(responseCode).when(httpConn).getResponseCode();
        Mockito.doReturn(inStream).when(httpConn).getInputStream();
        Mockito.when(url.openConnection()).thenReturn(httpConn);
        return httpConn;
    }

    private void verifyExecuteHttpGetRequestTest(HttpURLConnection httpConn, int times) throws ProtocolException, IOException {
        InOrder inOrder = Mockito.inOrder(httpConn);
        inOrder.verify(httpConn).setRequestMethod(Mockito.eq("GET"));
        inOrder.verify(httpConn).getResponseCode();
        inOrder.verify(httpConn, Mockito.times(times)).getInputStream();

        PowerMockito.verifyStatic(Mockito.times(times));
        IOUtils.copy(Mockito.any(InputStream.class), Mockito.any(StringWriter.class));
    }

    private void configureWakeHaltedHostUsingHttpGetTest() throws Exception, IOException {
        URL url = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withArguments(Mockito.eq("http://%s:8080/boot/wakeonlan/%s"), Mockito.anyString(), Mockito.anyString()).thenReturn(url);
        Mockito.doReturn("result").when(spy).executeHttpGetRequest(Mockito.any(URL.class));
    }

    private void verifyWakeHaltedHostUsingHttpGetTest(String expected, String result) throws IOException {
        Mockito.verify(spy).executeHttpGetRequest(Mockito.any(URL.class));
        Assert.assertEquals(expected, result);
    }

}