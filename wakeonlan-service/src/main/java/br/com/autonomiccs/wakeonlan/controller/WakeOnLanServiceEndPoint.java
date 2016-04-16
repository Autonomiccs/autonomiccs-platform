package br.com.autonomiccs.wakeonlan.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.autonomiccs.wakeonlan.service.StartHostService;

/**
 * This class listens the /boot/wakeonlan/macaddress endpoint url
 * and calls the {@link StartHostService} when endpoint is accessed
 */
@Path("/")
@Component
public class WakeOnLanServiceEndPoint {

    @Autowired
    private StartHostService startHostService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * It executes the Wake on LAN command in the virtual machine to start up
     * the host specified by MAC.
     *
     * @param mac
     *            The MAC address of the host that you will wake up
     * @return
     *         A <code>String</code> that contains the result text from wake on lan
     */
    @GET
    @Path("wakeonlan/{mac}")
    public String startHost(@PathParam("mac") String mac) {
        logger.info("Waking up host: " + mac);
        return startHostService.startHost(mac);
    }
}