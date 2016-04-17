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
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

/**
 * Realizes http requests
 */
@Component
public class HttpUtils {
    /**
     * Send an HTTP get request to systemVM wake the host up.
     *
     * @param systemVmIp
     *            The management IP address from systemVM.
     * @param hostMac
     *            The MAC address from host that will be waked up.
     * @return
     *         The response from HTTP get or the error code.
     */
    public String wakeHaltedHostUsingHttpGet(String systemVmIp, String hostMac) {
        try {
            URL url = new URL(String.format("http://%s:8080/boot/wakeonlan/%s", systemVmIp, hostMac));
            return executeHttpGetRequest(url);
        } catch (IOException e) {
            return e.getMessage();
        }

    }

    public String executeHttpGetRequest(URL url) throws IOException, ProtocolException {
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            return String.format("Error in HTTP GET : code [%d]", responseCode);
        }
        StringWriter output = new StringWriter();
        IOUtils.copy(con.getInputStream(), output);
        return output.toString();
    }
}
