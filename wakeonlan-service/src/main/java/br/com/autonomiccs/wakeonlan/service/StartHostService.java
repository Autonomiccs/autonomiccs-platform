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
