package br.com.autonomiccs.autonomic.plugin.common.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class HostUtils {

    public boolean isHostReachable(String addrees) {
        return isHostReacheableOnPort22(addrees);
    }

    private boolean isHostReacheableOnPort22(String addrees) {
        return isHostReachableOnPort(addrees, 22);
    }

    public boolean isHostReachableOnPort(String addrees, int openPort) {
        return isHostReachableOnPort(addrees, openPort, 15000);
    }

    private boolean isHostReachableOnPort(String addr, int openPort, int timeOutMillis) {
        if (StringUtils.isBlank(addr)) {
            return false;
        }
        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public boolean isHostReachableOnPort8080(String iPv4Address) {
        return isHostReachableOnPort(iPv4Address, 8080);
    }

}
