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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cloud.utils.exception.CloudRuntimeException;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;

/**
 * This class provides methods to perform SSH connections.
 */
@Component
public class SshUtils implements InitializingBean {

    private Logger logger = Logger.getLogger(this.getClass());
    private int CONNECT_TIMEOUT = 60000;

    @Autowired
    protected ThreadUtils threadUtils;

    private File certificate;

    /**
     * This method will create and authenticate a session using a public key method. The
     * certificate that will be used is the Autonomiccs default certificated that is installed by
     * default in the Autonomiccs system VM template
     */
    protected Session authenticateSshSessionWithPublicKey(Connection sshConnection) throws IOException {
        sshConnection.connect(null, CONNECT_TIMEOUT, CONNECT_TIMEOUT);
        if (!sshConnection.authenticateWithPublicKey("root", certificate, null)) {
            throw new CloudRuntimeException(String.format("Unable to authenticate to (%s)", sshConnection.getHostname()));
        }
        return sshConnection.openSession();
    }

    /**
     * @param address
     *            The host Management IP address that the connection will be created
     * @return
     *         An SSH connection with the specified host at the port 22
     */
    protected Connection getSshConnectionWithHost(String address) {
        return new Connection(address, 22);
    }

    /**
     * It executes a given command to the host with a given IP address. Waits 15
     * minutes after the command execution, avoiding to close the session before
     * it sends successfully the command.
     */
    public void executeCommandOnHostViaSsh(String hostIp, String command) {
        Connection sshConnectionWithHost = getSshConnectionWithHost(hostIp);
        try {
            Session sshSession = authenticateSshSessionWithPublicKey(sshConnectionWithHost);
            sshSession.execCommand(command);
            waitUntilCommandFinishes(hostIp, command, sshSession);
            sshSession.close();
        } catch (IOException e) {
            logger.info("Unable to execute command: " + command, e);
        }
        sshConnectionWithHost.close();
    }

    /**
     * This method holds the execution until the SSH connection gives an exit status.
     */
    protected void waitUntilCommandFinishes(String hostIp, String command, Session sshSession) {
        Integer exitStatus = sshSession.getExitStatus();
        while (exitStatus == null) {
            exitStatus = sshSession.getExitStatus();
            threadUtils.sleepThread(5);
        }
        logger.info(String.format("Command [%s] executed on [%s] has the following exitStatus [%d]", command, hostIp, exitStatus));
    }

    /**
     * Copy a local file to a remote directory, uses mode 0600 when creating the
     * file on the remote side.
     *
     * @param localfilePath
     *            The file that will be sent to the host
     * @param remotePath
     *            Remote target directory. Use an empty string to specify the
     *            default directory.
     * @param address
     *            The Management IP address from machine that will be connected.
     * @throws IOException
     */
    public void sendFileToHost(File localfilePath, String remoteFileName, String remotePath, String address) {
        Connection sshConnectionWithHost = getSshConnectionWithHost(address);
        try {
            authenticateSshSessionWithPublicKey(sshConnectionWithHost);
            SCPClient scp = new SCPClient(sshConnectionWithHost);
            scp.put(localfilePath.getAbsolutePath(), remoteFileName, remotePath, "0755");
        } catch (IOException e) {
            logger.error(String.format("Error while sending file [%s] to host [%s]", localfilePath, address), e);
        }
        sshConnectionWithHost.close();
    }

    /**
     * It creates a temporary file and copy the RSA public key to this temporary file.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        certificate = File.createTempFile(UUID.randomUUID().toString(), ".tmp");

        InputStream certificateStreamOriginal = getClass().getResourceAsStream("/id_rsa.ppk");
        FileOutputStream certificateStreamWorkingFile = new FileOutputStream(certificate);

        IOUtils.copy(certificateStreamOriginal, certificateStreamWorkingFile);

        IOUtils.closeQuietly(certificateStreamOriginal);
        IOUtils.closeQuietly(certificateStreamWorkingFile);
    }
}
