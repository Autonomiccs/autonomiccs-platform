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

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ShellCommandUtils.class)
public class ShellCommandUtilsTest {

    private ShellCommandUtils shellCommandUtils;
    private String command = "test";

    @Before
    public void beforeTest(){
        shellCommandUtils = new ShellCommandUtils();
    }

    @Test
    public void executeCommandTest() throws IOException, InterruptedException{
        Process processMock = Mockito.mock(Process.class);
        Runtime runtimeMock = configureAndGetRuntimeMock();

        String commandOutput = "TEST";
        InputStream input = IOUtils.toInputStream(commandOutput, "utf-8");

        Mockito.when(runtimeMock.exec(command)).thenReturn(processMock);
        Mockito.when(processMock.waitFor()).thenReturn(1);
        Mockito.when(processMock.getInputStream()).thenReturn(input);

        String response = shellCommandUtils.executeCommand(command);
        Assert.assertEquals(commandOutput, response);
        Mockito.verify(processMock).waitFor();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void executeCommandTestDealingWithIoException() throws IOException, InterruptedException {
        shellCommandUtils.logger = Mockito.mock(Logger.class);

        Runtime runtimeMock = configureAndGetRuntimeMock();
        Mockito.when(runtimeMock.exec(command)).thenThrow(IOException.class);

        String commandOutput = shellCommandUtils.executeCommand(command);

        executeChecksForExceptionTests(commandOutput, IOException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void executeCommandTestDealingWithInterruptedException() throws IOException, InterruptedException {
        shellCommandUtils.logger = Mockito.mock(Logger.class);

        Process processMock = Mockito.mock(Process.class);
        Runtime runtimeMock = configureAndGetRuntimeMock();
        Mockito.when(runtimeMock.exec(command)).thenReturn(processMock);
        Mockito.when(processMock.waitFor()).thenThrow(InterruptedException.class);

        String commandOutput = shellCommandUtils.executeCommand(command);

        Mockito.verify(processMock).waitFor();
        executeChecksForExceptionTests(commandOutput, InterruptedException.class);
    }

    private void executeChecksForExceptionTests(String commandOutput, Class<? extends Exception> expectedException) {
        Mockito.verify(shellCommandUtils.logger).error(Mockito.startsWith(String.format("An error happened while executing command[%s]", command)), Mockito.any(expectedException));
        Assert.assertEquals("", commandOutput);
    }

    private Runtime configureAndGetRuntimeMock() {
        Runtime runtimeMock = Mockito.mock(Runtime.class);
        PowerMockito.mockStatic(Runtime.class);

        PowerMockito.when(Runtime.getRuntime()).thenReturn(runtimeMock);
        return runtimeMock;
    }
}