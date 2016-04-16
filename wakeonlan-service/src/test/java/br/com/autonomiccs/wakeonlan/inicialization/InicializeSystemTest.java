package br.com.autonomiccs.wakeonlan.inicialization;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import br.com.autonomiccs.autonomic.plugin.common.utils.ShellCommandUtils;
import br.com.autonomiccs.wakeonlan.initialization.InicializeSystem;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SpringApplication.class)
public class InicializeSystemTest {

    @Test
    public void createShellCommandUtilsTest() {
        InicializeSystem inicializeSystem = new InicializeSystem();
        ShellCommandUtils returnedObj = inicializeSystem.createShellCommandUtils();

        Assert.assertNotNull(returnedObj);
        Assert.assertEquals(ShellCommandUtils.class, returnedObj.getClass());
    }

    @Test
    public void mainTest() throws Exception {
        String[] args = new String[] {};
        ConfigurableApplicationContext context = PowerMockito.mock(ConfigurableApplicationContext.class);
        PowerMockito.spy(SpringApplication.class);
        PowerMockito.doReturn(context).when(SpringApplication.class, InicializeSystem.class, args);

        InicializeSystem.main(new String[] {});

        PowerMockito.verifyStatic();
        SpringApplication.run(InicializeSystem.class, args);
    }

}
