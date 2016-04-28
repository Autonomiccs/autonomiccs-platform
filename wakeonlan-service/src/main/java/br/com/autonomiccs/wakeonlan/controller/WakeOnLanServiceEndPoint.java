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
package br.com.autonomiccs.wakeonlan.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.autonomiccs.wakeonlan.services.WakeOnLanHostService;

/**
 * This class listens the /boot/wakeonlan/macaddress endpoint url
 * and calls the {@link WakeOnLanHostService} when endpoint is accessed
 */
@Path("/")
@Component
public class WakeOnLanServiceEndPoint {

    @Autowired
    private WakeOnLanHostService startHostService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * It executes the Wake on LAN command in the virtual machine to start up
     * the host specified by MAC.
     *
     * @param mac
     *            The MAC address of the host that you will wake up
     * @return
     *         A <code>String</code> that contains the result text from wake on lan
     */
    @GET
    @Path("wakeonlan/{mac}")
    public String startHost(@PathParam("mac") String mac) {
        logger.info("Waking up host: " + mac);
        return startHostService.startHost(mac);
    }
}