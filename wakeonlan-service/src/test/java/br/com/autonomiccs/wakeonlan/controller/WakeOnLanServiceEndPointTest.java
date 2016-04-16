package br.com.autonomiccs.wakeonlan.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.autonomiccs.wakeonlan.service.StartHostService;

@RunWith(MockitoJUnitRunner.class)
public class WakeOnLanServiceEndPointTest {

    @InjectMocks
    private WakeOnLanServiceEndPoint wakeOnLanServiceEndPoint;
    @Spy
    private StartHostService startHostService;

    @Test
    public void consultarTest() {
        Mockito.doReturn("mac").when(startHostService).startHost(Mockito.anyString());
        wakeOnLanServiceEndPoint.startHost("");
        Mockito.verify(startHostService).startHost("");
    }
}
