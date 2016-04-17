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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Execute shell commands
 */
@Component
public class ShellCommandUtils {

    private final Logger logger = Logger.getLogger(this.getClass());

    /**
     * Executes the specified shell command and wait for the
     * end of command execution to continue with the application
     * flow.
     *
     * @param command
     *            The command that will be executed.
     * @return
     *         A <code>String</code> that is the result from
     *         command executed.
     */
    public String executeCommand(String command) {
        Writer output = new StringWriter();
        try {
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            IOUtils.copy(p.getInputStream(), output);
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage());
        }
        return output.toString();
    }

}