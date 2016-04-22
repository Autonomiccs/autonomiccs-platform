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
package br.com.autonomiccs.wakeonlan.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.autonomiccs.autonomic.plugin.common.utils.ShellCommandUtils;

/**
 * This class executes the wake on LAN command line in the virtual
 * machine that runs this application
 */
@Service
public class StartHostService {

    @Autowired
    protected ShellCommandUtils shellCommandUtils;

    private final String wakeOnLanCommand = "/usr/bin/wakeonlan";

    /**
     * It executes the Wake on LAN command in the virtual machine to start up
     * the host that has the given MAC.
     *
     * @param mac
     *            The MAC address of the machine to wake up
     * @return
     *         A <code>String</code> that contains the result text from wake on LAN command
     */
    public String startHost(String mac) {
        return shellCommandUtils.executeCommand(String.format("%s %s", wakeOnLanCommand, mac));
    }
}
