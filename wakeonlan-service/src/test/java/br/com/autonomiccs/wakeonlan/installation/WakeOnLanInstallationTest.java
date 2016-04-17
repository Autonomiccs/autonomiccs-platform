/*
 * This program is part of Autonomiccs "autonomic-platform",
 * an open source autonomic cloud computing management platform.
 * Copyright (C) 2016 Autonomiccs, Inc.
 *
 * Licensed to the Autonomiccs, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Versnce
 * with the License.  You may obtain a copy of the License at
 *
 *    http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package br.com.autonomiccs.wakeonlan.installation;

import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.com.autonomiccs.autonomic.plugin.common.utils.ShellCommandUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WakeOnLanInstallation.class)
public class WakeOnLanInstallationTest {

    private WakeOnLanInstallation wakeOnLanInstallation;
    private ShellCommandUtils shellCommandUtils;

    @Before
    public void beforeTest() {
        wakeOnLanInstallation = Mockito.spy(new WakeOnLanInstallation());
        shellCommandUtils = Mockito.mock(ShellCommandUtils.class);
    }

    @Test
    public void installWakeOnLanTestIfNotInstalled() throws Exception {
        File file = PowerMockito.mock(File.class);
        PowerMockito.when(file.exists()).thenReturn(false);
        String command = "aptitude -y install wakeonlan";
        PowerMockito.whenNew(File.class).withArguments("/usr/bin/wakeonlan").thenReturn(file);

        when(shellCommandUtils.executeCommand(command)).thenReturn(command);

        wakeOnLanInstallation.shellCommandUtils = shellCommandUtils;
        wakeOnLanInstallation.installWakeOnLan();
        Mockito.verify(shellCommandUtils, Mockito.times(1)).executeCommand(command);
    }

    @Test
    public void installWakeOnLanTestWakeOnLanAlreadyInstalled() throws Exception {
        File file = PowerMockito.mock(File.class);
        PowerMockito.when(file.exists()).thenReturn(true);
        String command = "aptitude -y install wakeonlan";
        PowerMockito.whenNew(File.class).withArguments("/usr/bin/wakeonlan").thenReturn(file);

        when(shellCommandUtils.executeCommand(command)).thenReturn(command);

        wakeOnLanInstallation.shellCommandUtils = shellCommandUtils;
        wakeOnLanInstallation.installWakeOnLan();

        Mockito.verify(shellCommandUtils, Mockito.times(0)).executeCommand(command);
    }

    @Test
    public void afterPropertiesSetTestinstallWakeOnLanBeingUsed() throws Exception {
        Mockito.doNothing().when(wakeOnLanInstallation).installWakeOnLan();
        wakeOnLanInstallation.afterPropertiesSet();

        Mockito.verify(wakeOnLanInstallation).installWakeOnLan();
    }
}