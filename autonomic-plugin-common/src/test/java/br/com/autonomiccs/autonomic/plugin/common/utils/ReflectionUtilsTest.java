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

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.cloud.utils.exception.CloudRuntimeException;

import br.com.autonomiccs.autonomic.algorithms.commons.beans.HostResources;

@RunWith(MockitoJUnitRunner.class)
public class ReflectionUtilsTest {

    private ReflectionUtils spy;

    @Before
    public void setup() {
        spy = Mockito.spy(new ReflectionUtils());
    }

    @Test
    public void setFieldIntoObjectTest() throws Exception {
        HostResources host = new HostResources();
        String expectedName = "name";
        spy.setFieldIntoObject(host, "hostName", expectedName);
        Mockito.verify(spy).getDeclaredField(Mockito.any(), Mockito.anyString());
        Assert.assertEquals(expectedName, host.getHostName());
    }

    @Test(expected = CloudRuntimeException.class)
    public void setFieldIntoObjectTestExpectCloudRuntimeExceptionWithIllegalArgumentException() throws Exception {
        spy.setFieldIntoObject(new HostResources(), "hostName", 0l);
        Mockito.verify(spy).getDeclaredField(Mockito.any(), Mockito.anyString());
    }

    @Test(expected = CloudRuntimeException.class)
    public void setFieldIntoObjectTestExpectCloudRuntimeExceptionFieldDoesNotExists() throws Exception {
        spy.setFieldIntoObject(new HostResources(), "fieldThatDoesNotExists", "name");
        Mockito.verify(spy).getDeclaredField(Mockito.any(), Mockito.anyString());
    }

    @Test
    public void getDeclaredFieldTestFieldNotExistsIntoObject() throws Exception {
        Field result = spy.getDeclaredField(HostResources.class, "fieldThatDoesNotExist");
        Assert.assertEquals(null, result);
    }

    @Test
    public void getDeclaredFieldTestFieldExistsIntoObject() throws Exception {
        Field result = spy.getDeclaredField(new HostResources(), "hostName");
        Assert.assertEquals("private java.lang.String br.com.autonomiccs.autonomic.algorithms.commons.beans.HostResources.hostName", result.toString());
    }

}
