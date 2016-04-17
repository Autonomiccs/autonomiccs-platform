/*
 * This program is part of Autonomiccs "autonomic-platform",
 * an open source autonomic cloud computing management platform.
 * Copyright (C) 2016 Autonomiccs, Inc.
 *
 * Licensed to the Autonomiccs, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
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
package br.com.autonomiccs.autonomic.plugin.common.utils;

import org.springframework.stereotype.Component;

import com.cloud.utils.exception.CloudRuntimeException;

/**
 * Util operations over threads (for example, sleep)
 */
@Component
public class ThreadUtils {

    /**
     * The thread executing this method sleeps a given amount of seconds.
     *
     * @param secondsToSleep
     */
    public void sleepThread(int secondsToSleep) {
        try {
            Thread.sleep(secondsToSleep * 1000);
        } catch (InterruptedException e) {
            throw new CloudRuntimeException(e);
        }
    }

}
