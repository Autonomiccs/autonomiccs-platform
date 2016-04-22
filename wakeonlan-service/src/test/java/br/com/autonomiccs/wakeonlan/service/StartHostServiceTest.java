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
package br.com.autonomiccs.wakeonlan.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.autonomiccs.autonomic.plugin.common.utils.ShellCommandUtils;

@RunWith(MockitoJUnitRunner.class)
public class StartHostServiceTest {

    @Test
    public void startHostTest() throws Exception {
        String mac = "4f:2e:34:d9:fe:76";
        String commandToBeExecuted = String.format("%s %s", "/usr/bin/wakeonlan", mac);

        ShellCommandUtils shellCommandUtils = Mockito.mock(ShellCommandUtils.class);
        Mockito.when(shellCommandUtils.executeCommand(commandToBeExecuted)).thenReturn("test");

        StartHostService service = new StartHostService();
        service.shellCommandUtils = shellCommandUtils;

        String commandReturn = service.startHost(mac);

        Assert.assertEquals("test", commandReturn);
        Mockito.verify(shellCommandUtils).executeCommand(commandToBeExecuted);
    }
}