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
public class WakeOnLanInstalation implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected ShellCommandUtils shellCommandUtils;

    private final String wakeOnLanCommand = "/usr/bin/wakeonlan";

    private final String aptitudeInstallWakeOnLan = "aptitude -y install wakeonlan";

    /**
     * It tries to install the wake on LAN program in the host;
     * if the wake on LAN is already installed, it does nothing, otherwise it installs the wake on
     * LAN.
     */
    private void installWakeOnLan() {
        logger.info("Checking if wakeonlan is installed.");
        File file = new File(wakeOnLanCommand);
        boolean isWakeOnLanInstalled = file.exists();
        if (isWakeOnLanInstalled) {
            logger.info("Wakeonlan is already installed.");
            return;
        }
        logger.info("Wakeonlan is not installed.");
        logger.info("Installing wakeonlan.");
        String logInstalation = shellCommandUtils.executeCommand(String.format("%s", aptitudeInstallWakeOnLan));
        logger.info(logInstalation);
        logger.info("Instalation finished.");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        installWakeOnLan();
    }

}
