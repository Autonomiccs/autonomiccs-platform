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
package br.com.autonomiccs.autonomic.administration.plugin;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import br.com.autonomiccs.autonomic.administration.plugin.hypervisors.HypervisorHost;
import br.com.autonomiccs.autonomic.plugin.common.enums.HostAdministrationStatus;
import br.com.autonomiccs.autonomic.plugin.common.services.HostService;

import com.cloud.host.Host;
import com.cloud.host.HostVO;

/**
 * Manages hypervisor's operations, passing the execution flow to the correct hypervisor facade.
 */
@Component(value = "hypervisorManager")
public class HypervisorManager {

    @Inject
    private HostService hostService;

    @Inject
    private List<HypervisorHost> hypervisorHosts;

    /**
     * Executes the shutdownHost method of the {@link HostVO} hypervisor. If the
     * hypervisor from {@link HostVO} does not supports shutdown, then it throws
     * a {@link RuntimeException}.
     *
     * @throws RuntimeException
     * @param hostVo
     */
    public void shutdownHost(HostVO hostVo) {
        if (!Host.Type.Routing.equals(hostVo.getType())) {
            return;
        }
        for (HypervisorHost currentHypervisorHost : hypervisorHosts) {
            if (currentHypervisorHost.supportsHypervisor(hostVo.getHypervisorType())) {
                shutdown(hostVo, currentHypervisorHost);
                return;
            }
        }
        throw new RuntimeException(String.format("The the hypervisor[%d] from host[%d] does not support shutdown to consolidate", hostVo.getHypervisorType(), hostVo.getId()));
    }

    /**
     * Calls the {@link HypervisorHost#shutdownHost(HostVO)} method and mark the
     * host administration process as {@link HostAdministrationStatus#ShutDownToConsolidate} using
     * {@link HostService#markHostAsShutdownByAdministrationAgent(long)}.
     *
     * @param hostVo
     * @param currentHypervisorHost
     */
    private void shutdown(HostVO hostVo, HypervisorHost currentHypervisorHost) {
        hostService.loadHostDetails(hostVo);
        currentHypervisorHost.shutdownHost(hostVo);
        hostService.markHostAsShutdownByAdministrationAgent(hostVo.getId());
    }

}
