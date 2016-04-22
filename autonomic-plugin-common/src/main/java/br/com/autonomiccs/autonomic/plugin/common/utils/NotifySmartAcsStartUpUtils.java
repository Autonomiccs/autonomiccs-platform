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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotifySmartAcsStartUpUtils {

    private Logger logger = Logger.getLogger(getClass());

    private final UUID uuid = UUID.randomUUID();

    @Autowired
    private HttpUtils httpUtils;

    public void sendModuleStartUp(Class<?> clazz) {
        try {
            URI uri = new URI("http", "smartcloudstack.lrg.ufsc.br:8080", "/boot/log/" + uuid, String.format("msg=Module initialized[%s]", ObjectUtils.toString(clazz)), null);
            httpUtils.executeHttpGetRequest(uri.toURL());
        } catch (IOException | URISyntaxException e) {
            logger.info("Problems while notifying home about the modules startup.");
        }
    }

}
