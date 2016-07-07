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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

import com.cloud.utils.exception.CloudRuntimeException;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.ServerHostKeyVerifier;
import com.trilead.ssh2.Session;

@RunWith(PowerMockRunner.class)
public class SshUtilsTest {

    private SshUtils spy;
    private ThreadUtils threadUtils;

    private int CONNECT_TIMEOUT = 60000;
    private Session sshSession;
    private Connection sshConnectionWithHost;

    @Before
    public void setup() {
        threadUtils = Mockito.mock(ThreadUtils.class);
        spy = Mockito.spy(new SshUtils());
        spy.threadUtils = threadUtils;
        sshSession = Mockito.mock(Session.class);
        sshConnectionWithHost = Mockito.mock(Connection.class);
    }

    @Test
    public void authenticateSshSessionWithPublicKeyTest() throws IOException {
        Session sess = Mockito.mock(Session.class);
        Connection sshConnection = setupAuthenticateSshSessionWithPublicKeyTest(sess, true);

        Session result = spy.authenticateSshSessionWithPublicKey(sshConnection);

        Assert.assertEquals(sess, result);
    }

    @Test(expected = CloudRuntimeException.class)
    public void authenticateSshSessionWithPublicKeyTestExpectCloudRuntimeException() throws IOException {
        Session sess = Mockito.mock(Session.class);
        Connection sshConnection = setupAuthenticateSshSessionWithPublicKeyTest(sess, false);
        spy.authenticateSshSessionWithPublicKey(sshConnection);
    }

    @Test
    public void getSshConnectionWithHostTest() throws Exception {
        Connection result = spy.getSshConnectionWithHost("address");
        Assert.assertEquals("address", result.getHostname());
    }

    @Test
    public void executeCommandOnHostViaSshTest() throws Exception {
        configureExecuteCommandOnHostViaSshTest(sshConnectionWithHost, sshSession);

        spy.executeCommandOnHostViaSsh("hostIp", "command");

        verifyExecuteCommandOnHostViaSshTestCalledMethods(sshConnectionWithHost, sshSession, 1);
    }

    @Test
    public void executeCommandOnHostViaSshTestCatchIOException() throws Exception {
        configureExecuteCommandOnHostViaSshTest(sshConnectionWithHost, sshSession);
        Mockito.doThrow(IOException.class).when(sshSession).execCommand(Mockito.eq("command"));

        spy.executeCommandOnHostViaSsh("hostIp", "command");

        verifyExecuteCommandOnHostViaSshTestCalledMethods(sshConnectionWithHost, sshSession, 0);
    }

    @Test
    public void waitUntilCommandFinishesTest() {
        Mockito.when(sshSession.getExitStatus()).thenReturn(null, null, new Integer(1));
        Mockito.doNothing().when(threadUtils).sleepThread(Mockito.anyInt());

        spy.waitUntilCommandFinishes("hostIp", "command", sshSession);

        Mockito.verify(sshSession, Mockito.times(3)).getExitStatus();
        Mockito.verify(threadUtils, Mockito.times(2)).sleepThread(Mockito.eq(5));
    }

    @Test
    @PrepareForTest(SshUtils.class)
    public void sendFileToHostTest() throws Exception {
        File localfilePath = Mockito.mock(File.class);
        SCPClient scp = configureSendFileToHostTest(localfilePath);

        spy.sendFileToHost(localfilePath, "remoteFileName", "remotePath", "address");

        verifySendFileToHostTest(localfilePath, scp, 1, 1);
    }

    @Test
    @PrepareForTest(SshUtils.class)
    public void sendFileToHostTestCatchIOException() throws Exception {
        File localfilePath = Mockito.mock(File.class);
        SCPClient scp = configureSendFileToHostTest(localfilePath);
        Mockito.doThrow(IOException.class).when(spy).authenticateSshSessionWithPublicKey(Mockito.eq(sshConnectionWithHost));

        spy.sendFileToHost(localfilePath, "remoteFileName", "remotePath", "address");

        verifySendFileToHostTest(localfilePath, scp, 0, 0);
    }

    @Test
    @PrepareForTest(IOUtils.class)
    public void afterPropertiesSetTest() throws Exception {
        PowerMockito.mockStatic(IOUtils.class);

        spy.afterPropertiesSet();

        PowerMockito.verifyStatic();
        IOUtils.copy(Mockito.any(InputStream.class), Mockito.any(FileOutputStream.class));
        IOUtils.closeQuietly(Mockito.any(InputStream.class));
        IOUtils.closeQuietly(Mockito.any(FileOutputStream.class));
    }

    private Connection setupAuthenticateSshSessionWithPublicKeyTest(Session sess, boolean returning) throws IOException {
        String nullString = null;
        Connection sshConnection = Mockito.mock(Connection.class);
        Mockito.doReturn(null).when(sshConnection).connect(Mockito.any(ServerHostKeyVerifier.class), Mockito.eq(CONNECT_TIMEOUT), Mockito.eq(CONNECT_TIMEOUT));
        Mockito.doReturn(returning).when(sshConnection).authenticateWithPublicKey(Mockito.eq("root"), Mockito.any(File.class), Mockito.eq(nullString));
        Mockito.doReturn(sess).when(sshConnection).openSession();
        return sshConnection;
    }

    private void verifyExecuteCommandOnHostViaSshTestCalledMethods(Connection sshConnectionWithHost, Session sshSession, int times) throws IOException {
        InOrder inOrder = Mockito.inOrder(spy, sshSession, sshConnectionWithHost);
        inOrder.verify(spy).authenticateSshSessionWithPublicKey(Mockito.eq(sshConnectionWithHost));
        inOrder.verify(sshSession).execCommand(Mockito.anyString());
        inOrder.verify(spy, Mockito.times(times)).waitUntilCommandFinishes(Mockito.eq("hostIp"), Mockito.eq("command"), Mockito.eq(sshSession));
        inOrder.verify(sshSession, Mockito.times(times)).close();
        inOrder.verify(sshConnectionWithHost).close();
    }

    private void configureExecuteCommandOnHostViaSshTest(Connection sshConnectionWithHost, Session sshSession) throws IOException {
        Mockito.doReturn(sshConnectionWithHost).when(spy).getSshConnectionWithHost(Mockito.anyString());
        Mockito.doReturn(sshSession).when(spy).authenticateSshSessionWithPublicKey(Mockito.eq(sshConnectionWithHost));
        Mockito.doNothing().when(spy).waitUntilCommandFinishes(Mockito.eq("hostIp"), Mockito.eq("command"), Mockito.eq(sshSession));
    }

    private SCPClient configureSendFileToHostTest(File localfilePath) throws Exception {
        Mockito.doReturn("string").when(localfilePath).getAbsolutePath();
        Mockito.doReturn(sshConnectionWithHost).when(spy).getSshConnectionWithHost(Mockito.anyString());
        Mockito.doReturn(sshSession).when(spy).authenticateSshSessionWithPublicKey(Mockito.eq(sshConnectionWithHost));

        SCPClient scp = Mockito.mock(SCPClient.class);
        Mockito.doNothing().when(scp).put(Mockito.anyString(), Mockito.eq("remoteFileName"), Mockito.eq("remotePath"), Mockito.eq("0755"));

        PowerMockito.mock(SCPClient.class);
        PowerMockito.whenNew(SCPClient.class).withArguments(Mockito.eq(sshConnectionWithHost)).thenReturn(scp);

        return scp;
    }

    private void verifySendFileToHostTest(File localfilePath, SCPClient scp, int timesGetAbsolutePath, int timesPut) throws IOException {
        Mockito.verify(spy).authenticateSshSessionWithPublicKey(Mockito.eq(sshConnectionWithHost));
        Mockito.verify(localfilePath, Mockito.times(timesGetAbsolutePath)).getAbsolutePath();
        Mockito.verify(scp, Mockito.times(timesPut)).put(Mockito.anyString(), Mockito.eq("remoteFileName"), Mockito.eq("remotePath"), Mockito.eq("0755"));
        Mockito.verify(sshConnectionWithHost).close();
    }

}
