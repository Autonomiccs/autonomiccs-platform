/*
 * This program is part of Autonomiccs "autonomic-platform",
 * an open source autonomic cloud computing management platform.
 * Copyright (C) 2016 Autonomiccs, Inc.
 *
 * Licensed to the Autonomiccs, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The Autonomiccs, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package br.com.autonomiccs.autonomic.administration.plugin.hypervisors.xenserver;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.xmlrpc.XmlRpcException;
import org.springframework.stereotype.Component;

import com.cloud.host.HostVO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.xenserver.resource.XenServerConnectionPool;
import com.cloud.utils.exception.CloudRuntimeException;
import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Pool;
import com.xensource.xenapi.Types.BadServerResponse;
import com.xensource.xenapi.Types.XenAPIException;

import br.com.autonomiccs.autonomic.administration.plugin.hypervisors.HypervisorHost;
import br.com.autonomiccs.autonomic.plugin.common.services.HostService;
import br.com.autonomiccs.autonomic.plugin.common.utils.HostUtils;
import br.com.autonomiccs.autonomic.plugin.common.utils.ShellCommandUtils;
import br.com.autonomiccs.autonomic.plugin.common.utils.ThreadUtils;

@Component
public class XenHypervisor implements HypervisorHost {

    private static final String FIND_ARP_FOR_HOST_COMMAND = "arp -n %s";
    private static final String REGEX_GET_ARP_FROM_ARP_COMMAND_OUTPUT = ".+([A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}).+";
    private static final XenServerConnectionPool connPool = XenServerConnectionPool.getInstance();

    private final Pattern compileRegexToGetArp = Pattern.compile(REGEX_GET_ARP_FROM_ARP_COMMAND_OUTPUT);

    @Inject
    private ThreadUtils threadUtils;
    @Inject
    private HostUtils hostUtils;
    @Inject
    private ShellCommandUtils shellCommandUtils;
    @Inject
    private HostService hostService;

    /**
     * This method shuts down the given host. It checks if the host to be powered off is the
     * master, if yes then changes the master to be another host in the cluster.
     *
     * @note this method can be executed only after the
     *       {@link HostService#loadHostDetails(HostVO)} method.
     */
    @Override
    public void shutdownHost(HostVO hostVo) {
        String privateMacAddress = getHostPrivateMacAddress(hostVo);
        hostService.updateHostPrivaceMacAddress(hostVo, privateMacAddress);

        Connection conn = getConnection(hostVo);
        String hostToDeactivatedUuid = hostVo.getGuid();

        try {
            Host master = getMasterHost(conn);
            changeMasterIfNeeded(conn, master, hostToDeactivatedUuid);

            Host host = Host.getByUuid(conn, hostToDeactivatedUuid);
            String hostAddress = host.getAddress(conn);

            disableAndShutdownHost(conn, host);

            do {
                threadUtils.sleepThread(3);
            } while (hostUtils.isHostReachable(hostAddress));

        } catch (Exception e) {
            throw new CloudRuntimeException(String.format("Could not shut down host [uuid=%s]", hostToDeactivatedUuid), e);
        }
    }

    private String getHostPrivateMacAddress(HostVO hostVo) {
        String returnOfArpCommand = shellCommandUtils.executeCommand(String.format(FIND_ARP_FOR_HOST_COMMAND, hostVo.getPrivateIpAddress()));
        Matcher matcher = compileRegexToGetArp.matcher(returnOfArpCommand);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new CloudRuntimeException(String.format("Could not find host [name=%s, privateAddress=%s] private mac address.", hostVo.getName(), hostVo.getPrivateIpAddress()));
    }

    /**
     * Disables the host (using {@link Host#disable(Connection)}) and shuts it down
     * using the {@link Host#shutdown(Connection)} method.
     */
    private void disableAndShutdownHost(Connection conn, Host host) throws BadServerResponse, XenAPIException, XmlRpcException {
        host.disable(conn);
        host.shutdown(conn);
    }

    /**
     * If the pool has more than one host and the
     * host to be powered off is the master, it changes the master using
     * {@link #changePoolMasterHost(Connection, String)}.
     */
    private void changeMasterIfNeeded(Connection conn, Host master, String hostUuid)
            throws BadServerResponse, XenAPIException, XmlRpcException {

        if (hostUuid.equals(master.getUuid(conn)) && !isLastHostOnPool(conn)) {
            changePoolMasterHost(conn, hostUuid);
            waitChangePoolMasterHost(master, conn, hostUuid);
        }
    }

    /**
     * Returns true if the pool has exactly one {@link Host}.
     */
    private boolean isLastHostOnPool(Connection conn) throws BadServerResponse, XenAPIException, XmlRpcException {
        return Host.getAll(conn).size() == 1;
    }

    /**
     * Returns the {@link Host} that is the current master of this pool.
     */
    private Host getMasterHost(Connection conn) throws BadServerResponse,XenAPIException, XmlRpcException {
        Iterator<Pool> poolIterator = Pool.getAll(conn).iterator();
        if (poolIterator.hasNext()) {
            return poolIterator.next().getMaster(conn);
        }
        throw new CloudRuntimeException("Could not find master server for pool.");
    }

    /**
     * The new master will be selected with no specific criteria.
     */
    private void changePoolMasterHost(Connection conn, String hostUuid) throws BadServerResponse, XenAPIException, XmlRpcException {
        for (Host host : Host.getAll(conn)) {
            if (hostUuid.equals(host.getUuid(conn))) {
                continue;
            }
            if (hostUtils.isHostReachable(host.getAddress(conn))) {
                Pool.designateNewMaster(conn, host);
                return;
            }
        }
    }

    /**
     * Waits until the master of the pool changes, given a total of 90 tries,
     * each taking 3 seconds of wait.
     */
    private void waitChangePoolMasterHost(Host master, Connection conn, String hostUuid) throws BadServerResponse, XenAPIException, XmlRpcException {
        int count = 0;
        while (hostUuid.equals(master.getUuid(conn))) {
            threadUtils.sleepThread(3);
            count++;
            if (count > 90) {
                throw new CloudRuntimeException(String.format("Could not shut down host [uuid=%s]; It was not possible to assign a new master to its pool", hostUuid));
            }
        }
    }

    /**
     * Get connection with the
     * {@link XenServerConnectionPool#getConnect(String, String, Queue)},
     * passing the host uuid, pool, ip, username, password and wait time.
     */
    private Connection getConnection(HostVO host) {
        Map<String, String> params = host.getDetails();
        String username = getUsername(params);
        Queue<String> password = getPasssword(params);
        return connPool.getConnect(host.getPrivateIpAddress(), username, password);
    }

    /**
     * Returns the host user name.
     */
    private String getUsername(Map<String, String> params) {
        return params.get("username");
    }

    /**
     * Returns the host password.
     */
    private Queue<String> getPasssword(Map<String, String> params) {
        Queue<String> password = new LinkedList<>();
        password.add(params.get("password"));
        return password;
    }

    @Override
    public boolean supportsHypervisor(HypervisorType hypervisorType) {
        return HypervisorType.XenServer.equals(hypervisorType);
    }

}
