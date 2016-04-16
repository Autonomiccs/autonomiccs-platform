package br.com.autonomiccs.wakeonlan.jersey;

import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JerseyConfigTest {

    @Test
    public void jerseyConfigTest() {
        JerseyConfig jerseyConfig = new JerseyConfig();
        Set<Class<?>> set = jerseyConfig.getClasses();
        Iterator<Class<?>> iter = set.iterator();
        Assert.assertEquals(1, set.size());
        Assert.assertEquals("class br.com.autonomiccs.wakeonlan.controller.WakeOnLanServiceEndPoint", iter.next().toString());
    }

}
