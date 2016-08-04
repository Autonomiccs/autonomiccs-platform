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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cloud.utils.exception.CloudRuntimeException;

@PrepareForTest(ThreadUtils.class)
@RunWith(PowerMockRunner.class)
public class ThreadUtilsTest {

    private final static long ONE_SECOND_IN_MILLISECONDS = 1000l;
    private ThreadUtils spy;

    @Before
    public void setup() {
        spy = Mockito.spy(new ThreadUtils());
        PowerMockito.mockStatic(Thread.class);
    }

    @Test
    public void sleepThreadTest() throws InterruptedException {
        PowerMockito.doNothing().when(Thread.class);
        Thread.sleep(Mockito.anyLong());

        spy.sleepThread(2);

        PowerMockito.verifyStatic();
        Thread.sleep(Mockito.eq(2 * ONE_SECOND_IN_MILLISECONDS));
    }

    @Test(expected = CloudRuntimeException.class)
    public void sleepThreadTestCatchInterruptedException() throws InterruptedException {

        PowerMockito.doThrow(new InterruptedException()).when(Thread.class);
        Thread.sleep(Mockito.anyLong());

        spy.sleepThread(2);

        PowerMockito.verifyStatic();
        Thread.sleep(Mockito.eq(2 * ONE_SECOND_IN_MILLISECONDS));
    }

}
