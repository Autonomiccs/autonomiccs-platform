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
package br.com.autonomiccs.wakeonlan.installation;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.autonomiccs.autonomic.plugin.common.utils.ShellCommandUtils;

/**
 * This class have the necessary methods to install the wake on LAN command in the host
 */
@Component
public class WakeOnLanInstallation implements InitializingBean {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected ShellCommandUtils shellCommandUtils;

    private final String wakeOnLanCommand = "/usr/bin/wakeonlan";

    private final String aptitudeInstallWakeOnLan = "aptitude -y install wakeonlan";

    /**
     * It tries to install the wake on LAN program in the host;
     * if the wake on LAN is already installed, it does nothing, otherwise it installs the wake on
     * LAN.
     */
    protected void installWakeOnLan() {
        logger.info("Checking if wakeonlan is installed.");
        File file = new File(wakeOnLanCommand);
        boolean isWakeOnLanInstalled = file.exists();
        if (isWakeOnLanInstalled) {
            logger.info("Wakeonlan is already installed.");
            return;
        }
        logger.info("Wakeonlan is not installed.");
        logger.info("Installing wakeonlan.");
        String logInstallation = shellCommandUtils.executeCommand(String.format("%s", aptitudeInstallWakeOnLan));
        logger.info(logInstallation);
        logger.info("Installation finished.");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        installWakeOnLan();
    }

}
