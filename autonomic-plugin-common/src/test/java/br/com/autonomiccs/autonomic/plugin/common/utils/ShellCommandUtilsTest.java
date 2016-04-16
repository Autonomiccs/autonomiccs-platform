package br.com.autonomiccs.autonomic.plugin.common.utils;


import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ShellCommandUtils.class)
public class ShellCommandUtilsTest {

    @Test
    public void executeCommandTest() throws IOException, InterruptedException{
        Process process = PowerMockito.mock(Process.class);
        Runtime runtime = PowerMockito.mock(Runtime.class);
        PowerMockito.mockStatic(Runtime.class);

        String commandOutput = "TEST";
        InputStream input = IOUtils.toInputStream(commandOutput, "utf-8");

        PowerMockito.when(Runtime.getRuntime()).thenReturn(runtime);

        String command = "test";
        PowerMockito.when(runtime.exec(command)).thenReturn(process);
        PowerMockito.when(process.waitFor()).thenReturn(1);
        PowerMockito.when(process.getInputStream()).thenReturn(input);

        ShellCommandUtils shell = new ShellCommandUtils();
        String response = shell.executeCommand(command);
        Assert.assertEquals(commandOutput, response);
    }
}