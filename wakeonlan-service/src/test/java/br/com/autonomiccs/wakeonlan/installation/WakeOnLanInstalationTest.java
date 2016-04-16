package br.com.autonomiccs.wakeonlan.installation;

import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.com.autonomiccs.autonomic.plugin.common.utils.ShellCommandUtils;
import br.com.autonomiccs.wakeonlan.installation.WakeOnLanInstalation;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WakeOnLanInstalation.class)
public class WakeOnLanInstalationTest {

    @Test
    public void installWakeOnLanIfNotInstalledTest() throws Exception {
        File file = PowerMockito.mock(File.class);
        PowerMockito.when(file.exists()).thenReturn(false);
        String command = "aptitude -y install wakeonlan";
        PowerMockito.whenNew(File.class).withArguments("/usr/bin/wakeonlan").thenReturn(file);

        ShellCommandUtils shellCommandUtils = Mockito.mock(ShellCommandUtils.class);
        when(shellCommandUtils.executeCommand(command)).thenReturn(command);

        WakeOnLanInstalation install = new WakeOnLanInstalation();
        install.shellCommandUtils = shellCommandUtils;
        install.afterPropertiesSet();
        Mockito.verify(shellCommandUtils, Mockito.times(1)).executeCommand(command);
    }

    @Test
    public void wakeOnLanAeadyInstalledTest() throws Exception {
        File file = PowerMockito.mock(File.class);
        PowerMockito.when(file.exists()).thenReturn(true);
        String command = "aptitude -y install wakeonlan";
        PowerMockito.whenNew(File.class).withArguments("/usr/bin/wakeonlan").thenReturn(file);

        ShellCommandUtils shellCommandUtils = Mockito.mock(ShellCommandUtils.class);
        when(shellCommandUtils.executeCommand(command)).thenReturn(command);

        WakeOnLanInstalation install = new WakeOnLanInstalation();
        install.shellCommandUtils = shellCommandUtils;
        install.afterPropertiesSet();

        Mockito.verify(shellCommandUtils, Mockito.times(0)).executeCommand(command);
    }
}