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

import org.springframework.stereotype.Component;

import com.cloud.utils.exception.CloudRuntimeException;

/**
 * Util operations over threads (for example, sleep)
 */
@Component
public class ThreadUtils {

    private final static long ONE_SECOND_IN_MILLISECONDS = 1000l;

    /**
     * The thread executing this method sleeps a given amount of seconds.
     * If an {@link InterruptedException} occurs, we do not swallow the exception;
     * we want to throw a runtime exception and we also do as described
     * in http://www.ibm.com/developerworks/library/j-jtp05236/ to restore the interrupt context.
     */
    public void sleepThread(int secondsToSleep) {
        try {
            Thread.sleep(secondsToSleep * ONE_SECOND_IN_MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CloudRuntimeException(e);
        }
    }

}
