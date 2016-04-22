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
package br.com.autonomiccs.wakeonlan.inicialization;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import br.com.autonomiccs.autonomic.plugin.common.utils.ShellCommandUtils;
import br.com.autonomiccs.wakeonlan.initialization.InicializeSystem;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SpringApplication.class)
public class InicializeSystemTest {

    @Test
    public void createShellCommandUtilsTest() {
        InicializeSystem inicializeSystem = new InicializeSystem();
        ShellCommandUtils returnedObj = inicializeSystem.createShellCommandUtils();

        Assert.assertNotNull(returnedObj);
        Assert.assertEquals(ShellCommandUtils.class, returnedObj.getClass());
    }

    @Test
    public void mainTest() throws Exception {
        String[] args = new String[] {};
        ConfigurableApplicationContext context = PowerMockito.mock(ConfigurableApplicationContext.class);
        PowerMockito.spy(SpringApplication.class);
        PowerMockito.doReturn(context).when(SpringApplication.class, InicializeSystem.class, args);

        InicializeSystem.main(new String[] {});

        PowerMockito.verifyStatic();
        SpringApplication.run(InicializeSystem.class, args);
    }

}
