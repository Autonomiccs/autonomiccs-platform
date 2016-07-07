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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class HostUtilsTest {

    private HostUtils spy;

    @Before
    public void setup() {
        spy = Mockito.spy(new HostUtils());
    }

    @Test
    public void isHostReachableTest() {
        executeIsHostReachableTest(true);
    }

    @Test
    public void isHostReachableTestFalse() {
        executeIsHostReachableTest(false);
    }

    @Test
    public void isHostReacheableOnPort22Test() {
        configureExecuteAndVerifyIsHostReacheableOnPort22Test(true);
    }

    @Test
    public void isHostReacheableOnPort22TestFalse() {
        configureExecuteAndVerifyIsHostReacheableOnPort22Test(false);
    }

    @Test
    public void isHostReachableOnPortTest() {
        configureExecuteAndVerifyIsHostReachableOnPortTest(true);
    }

    @Test
    public void isHostReachableOnPortTestFalse() {
        configureExecuteAndVerifyIsHostReachableOnPortTest(false);
    }

    @Test
    @PrepareForTest(HostUtils.class)
    public void isHostReachableOnPortWithAddressPortAndTimeoutArgsTest() throws Exception {
        Socket soc = configureIsHostReachableOnPortWithAddressPortAndTimeoutArgsTest();
        executeAndVerifyIsHostReachableOnPortWithAddressPortAndTimeoutArgsTest(soc, "address", 1, true);
    }

    @Test
    @PrepareForTest(HostUtils.class)
    public void isHostReachableOnPortWithAddressPortAndTimeoutArgsTestBlankAddress() throws Exception {
        Socket soc = configureIsHostReachableOnPortWithAddressPortAndTimeoutArgsTest();
        executeAndVerifyIsHostReachableOnPortWithAddressPortAndTimeoutArgsTest(soc, "", 0, false);
    }

    @Test
    @PrepareForTest(HostUtils.class)
    public void isHostReachableOnPortWithAddressPortAndTimeoutArgsTestCatchIOException() throws Exception {
        Socket soc = configureIsHostReachableOnPortWithAddressPortAndTimeoutArgsTest();
        Mockito.doThrow(IOException.class).when(soc).connect(Mockito.any(SocketAddress.class), Mockito.anyInt());
        executeAndVerifyIsHostReachableOnPortWithAddressPortAndTimeoutArgsTest(soc, "address", 1, false);
    }

    @Test
    public void isHostReachableOnPort8080TestTrue() {
        configureExecuteAndVerifyIsHostReachableOnPort8080Test(true);
    }

    @Test
    public void isHostReachableOnPort8080TestFalse() {
        configureExecuteAndVerifyIsHostReachableOnPort8080Test(false);
    }

    private void executeIsHostReachableTest(boolean expected) {
        Mockito.doReturn(expected).when(spy).isHostReacheableOnPort22(Mockito.anyString());
        boolean result = spy.isHostReachable("addrees");
        Mockito.verify(spy).isHostReacheableOnPort22(Mockito.anyString());
        Assert.assertEquals(expected, result);
    }

    private void configureExecuteAndVerifyIsHostReacheableOnPort22Test(boolean expected) {
        Mockito.doReturn(expected).when(spy).isHostReachableOnPort(Mockito.anyString(), Mockito.eq(22));
        boolean result = spy.isHostReachable("addrees");
        Mockito.verify(spy).isHostReachableOnPort(Mockito.anyString(), Mockito.eq(22));
        Assert.assertEquals(expected, result);
    }

    private void configureExecuteAndVerifyIsHostReachableOnPortTest(boolean expected) {
        Mockito.doReturn(expected).when(spy).isHostReachableOnPort(Mockito.anyString(), Mockito.eq(123), Mockito.eq(15000));
        boolean result = spy.isHostReachableOnPort("addrees", 123);
        Mockito.verify(spy).isHostReachableOnPort(Mockito.anyString(), Mockito.eq(123), Mockito.eq(15000));
        Assert.assertEquals(expected, result);
    }

    private void configureExecuteAndVerifyIsHostReachableOnPort8080Test(boolean expected) {
        Mockito.doReturn(expected).when(spy).isHostReachableOnPort(Mockito.anyString(), Mockito.eq(8080));
        boolean result = spy.isHostReachableOnPort8080("addrees");
        Mockito.verify(spy).isHostReachableOnPort(Mockito.anyString(), Mockito.eq(8080));
        Assert.assertEquals(expected, result);
    }

    private void executeAndVerifyIsHostReachableOnPortWithAddressPortAndTimeoutArgsTest(Socket soc, String address, int times, boolean expected) throws Exception, IOException {
        boolean result = spy.isHostReachableOnPort(address, 123, 15000);

        Mockito.verify(soc, Mockito.times(times)).connect(Mockito.any(SocketAddress.class), Mockito.anyInt());
        Assert.assertEquals(expected, result);
    }

    private Socket configureIsHostReachableOnPortWithAddressPortAndTimeoutArgsTest() throws Exception, IOException {
        PowerMockito.mockStatic(Socket.class);
        InetSocketAddress inetSocketAddress = Mockito.mock(InetSocketAddress.class);
        PowerMockito.whenNew(InetSocketAddress.class).withArguments(Mockito.anyString(), Mockito.anyInt()).thenReturn(inetSocketAddress);

        Socket soc = Mockito.mock(Socket.class);
        PowerMockito.whenNew(Socket.class).withNoArguments().thenReturn(soc);
        Mockito.doNothing().when(soc).connect(Mockito.any(SocketAddress.class), Mockito.anyInt());
        return soc;
    }

}
