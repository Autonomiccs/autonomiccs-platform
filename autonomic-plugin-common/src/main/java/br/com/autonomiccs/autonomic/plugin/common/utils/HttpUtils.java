package br.com.autonomiccs.autonomic.plugin.common.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

/**
 * Realizes http requests
 */
@Component
public class HttpUtils {
    /**
     * Send an HTTP get request to systemVM wake the host up.
     *
     * @param systemVmIp
     *            The management IP address from systemVM.
     * @param hostMac
     *            The MAC address from host that will be waked up.
     * @return
     *         The response from HTTP get or the error code.
     */
    public String wakeHaltedHostUsingHttpGet(String systemVmIp, String hostMac) {
        try {
            URL url = new URL(String.format("http://%s:8080/boot/wakeonlan/%s", systemVmIp, hostMac));
            return executeHttpGetRequest(url);
        } catch (IOException e) {
            return e.getMessage();
        }

    }

    public String executeHttpGetRequest(URL url) throws IOException, ProtocolException {
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            return String.format("Error in HTTP GET : code [%d]", responseCode);
        }
        StringWriter output = new StringWriter();
        IOUtils.copy(con.getInputStream(), output);
        return output.toString();
    }
}
