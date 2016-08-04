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
package br.com.autonomiccs.autonomic.plugin.common.daos.configurations;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cloud.utils.db.DbProperties;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DbProperties.class)
public class ProvideDataBasePropertiesTest {

    private ProvideDataBaseProperties spy;

    @Before
    public void setup() {
        spy = Mockito.spy(new ProvideDataBaseProperties());
    }

    @Test
    public void getPropertiesFileFromApacheCloudStackTest() throws Exception {
        Properties properties = Mockito.mock(Properties.class);
        PowerMockito.mockStatic(DbProperties.class);

        PowerMockito.doReturn(properties).when(DbProperties.class, "getDbProperties");

        Properties result = spy.getPropertiesFileFromApacheCloudStack();

        PowerMockito.verifyStatic();
        DbProperties.getDbProperties();
        Assert.assertEquals(properties, result);
    }

}
