package br.com.autonomiccs.wakeonlan.service;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.com.autonomiccs.autonomic.plugin.common.utils.ShellCommandUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(StartHostService.class)
public class StartHostServiceTest {

    @Test
    public void installWakeOnLanTest() throws Exception {
        ShellCommandUtils shellCommandUtils = Mockito.mock(ShellCommandUtils.class);
        String mac = "4f:2e:34:d9:fe:76";
        String commandToBeExecuted = String.format("%s %s", "/usr/bin/wakeonlan", mac);
        when(shellCommandUtils.executeCommand(commandToBeExecuted)).thenReturn("test");

        StartHostService service = new StartHostService();
        service.shellCommandUtils = shellCommandUtils;

        String commandReturn = service.startHost(mac);

        Assert.assertEquals("test", commandReturn);
        Mockito.verify(shellCommandUtils, Mockito.times(1)).executeCommand(commandToBeExecuted);
    }
}