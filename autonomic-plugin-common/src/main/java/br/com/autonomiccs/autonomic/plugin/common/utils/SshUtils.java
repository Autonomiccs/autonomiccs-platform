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

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;

@Component
public class SshUtils implements InitializingBean {

    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private ThreadUtils threadUtils;

    private File certificate;

    /**
     * %TODO melhorar a doc.
     *
     * @param sshConnection
     *            An SSH connection to which the session will be generated
     * @return
     *         A created session for the connection passed in param
     * @throws IOException
     */

    private Session authenticateSshSessionWithPublicKey(Connection sshConnection) throws IOException {
        sshConnection.connect(null, 60000, 60000);
        if (!sshConnection.authenticateWithPublicKey("root", certificate, null)) {
            throw new RuntimeException(String.format("Unable to authenticate to (%s)", sshConnection.getHostname()));
        }
        return sshConnection.openSession();
    }

    /**
     * @param address
     *            The host Management IP address that the connection will be created
     * @return
     *         An SSH connection with the specified host
     */
    private Connection getSshConnectionWithHost(String address) {
        return new Connection(address, 22);
    }

    /**
     * Executes a given command to the host with a given IP address. Waits 15
     * minutes after the command execution, avoiding to close the session before
     * it sends successfully the command.
     *
     * @param hostIp
     * @param command
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

    private void waitUntilCommandFinishes(String hostIp, String command, Session sshSession) {
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

    @Override
    public void afterPropertiesSet() throws Exception {
        certificate = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
        InputStream certificateStream = getClass().getResourceAsStream("/id_rsa.ppk");
        IOUtils.copy(certificateStream, new FileOutputStream(certificate));
    }
}
