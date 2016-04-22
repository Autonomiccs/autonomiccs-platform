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
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HostUtils {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public boolean isHostReachable(String addrees) {
        return isHostReacheableOnPort22(addrees);
    }

    private boolean isHostReacheableOnPort22(String addrees) {
        return isHostReachableOnPort(addrees, 22);
    }

    public boolean isHostReachableOnPort(String addrees, int openPort) {
        return isHostReachableOnPort(addrees, openPort, 15000);
    }

    private boolean isHostReachableOnPort(String addr, int openPort, int timeOutMillis) {
        if (StringUtils.isBlank(addr)) {
            return false;
        }
        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
            }
            return true;
        } catch (IOException ex) {
            logger.debug(String.format("Error while checking if host[%s] on port [%d] is reacheable", addr, openPort), ex);
            return false;
        }
    }

    public boolean isHostReachableOnPort8080(String iPv4Address) {
        return isHostReachableOnPort(iPv4Address, 8080);
    }

}
