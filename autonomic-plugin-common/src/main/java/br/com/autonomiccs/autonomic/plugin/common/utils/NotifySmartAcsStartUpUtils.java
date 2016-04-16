package br.com.autonomiccs.autonomic.plugin.common.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotifySmartAcsStartUpUtils {

    private Logger logger = Logger.getLogger(getClass());

    private final UUID uuid = UUID.randomUUID();

    @Autowired
    private HttpUtils httpUtils;

    public void sendModuleStartUp(Class<?> clazz) {
        try {
            URI uri = new URI("http", "smartcloudstack.lrg.ufsc.br:8080", "/boot/log/" + uuid, String.format("msg=Module initialized[%s]", ObjectUtils.toString(clazz)), null);
            httpUtils.executeHttpGetRequest(uri.toURL());
        } catch (IOException | URISyntaxException e) {
            logger.info("Problems while notifying home about the modules startup.");
        }
    }

}
