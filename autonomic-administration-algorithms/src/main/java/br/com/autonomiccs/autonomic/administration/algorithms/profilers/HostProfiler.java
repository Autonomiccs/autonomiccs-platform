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
package br.com.autonomiccs.autonomic.administration.algorithms.profilers;

import java.util.List;

import br.com.autonomiccs.autonomic.administration.algorithms.pojos.ClusterVmProfile;
import br.com.autonomiccs.autonomic.administration.algorithms.pojos.HostProfile;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.VmResources;

/**
 * This class calculates Hosts and VMs profiles from a cluster, those profiles are used by
 * algorithms from LRG; thus, this class is not necessary for every algorithm. It creates host
 * {@link HostProfile} and cluster profiles {@link ClusterVmProfile}.
 */
public class HostProfiler {

    protected final static int BYTES_TO_MEGA_BYTES = 1000000;
    private List<HostResources> hosts;

    public HostProfiler(List<HostResources> hosts) {
        this.hosts = hosts;
    }

    /**
     * It divides each host resource (number of CPUs, CPU frequency and memory) by cluster's VMs
     * profile for the same resource. Gives a proportion of VMs this host can support.
     *
     * @param host
     * @param vmsProfile
     * @return {@link HostProfile}
     */
    public HostProfile hostVMsResourceProportion(HostResources host) {
        ClusterVmProfile clusterVmsProfile = createClusterVmsProfile(getHostsVmsResources());
        HostProfile hostProfile = new HostProfile();

        hostProfile.setCpusProfile(host.getCpus() / clusterVmsProfile.getCpusProfile());
        hostProfile.setCpuSpeedProfile((host.getSpeed() * host.getCpuOverprovisioning()) / clusterVmsProfile.getCpuSpeedProfile());
        hostProfile.setMemoryProfile(((host.getTotalMemoryInBytes() / BYTES_TO_MEGA_BYTES) * host.getMemoryOverprovisioning()) / clusterVmsProfile.getMemoryProfile());

        return hostProfile;
    }

    /**
     * It calculates the average CPU and memory usage of VMs.
     *
     * @param vmsProfile
     * @return {@link ClusterVmProfile}
     */
    public ClusterVmProfile createClusterVmsProfile(ClusterVmProfile vmsProfile) {
        double numberOfInstances = vmsProfile.getNumberOfInstances();
        vmsProfile.setCpusProfile(vmsProfile.getTotalCpus() / numberOfInstances);
        vmsProfile.setCpuSpeedProfile(vmsProfile.getTotalCpuSpeed() / numberOfInstances);
        vmsProfile.setMemoryProfile(vmsProfile.getTotalMemory() / numberOfInstances);

        return vmsProfile;
    }

    /**
     * It calculates the sum of VMs resources from the list of hosts. It creates a
     * {@link ClusterVmProfile} summing the {@link VmResources}. It contains the number of VMs,
     * number of cpus, the cpu speed and memory size.
     *
     * @return {@link ClusterVmProfile}
     */
    public ClusterVmProfile getHostsVmsResources() {
        ClusterVmProfile vmsProfile = new ClusterVmProfile();
        for(HostResources host : hosts) {
            List<VmResources> vmsOnHost = host.getVmsResources();
            for(VmResources vmResources : vmsOnHost) {
                vmsProfile.setNumberOfInstances(vmsProfile.getNumberOfInstances() + 1);
                vmsProfile.setTotalCpus(vmsProfile.getTotalCpus() + vmResources.getNumberOfCpus());
                vmsProfile.setTotalCpuSpeed((int) (vmsProfile.getTotalCpuSpeed() + vmResources.getCpuSpeed()));
                vmsProfile.setTotalMemory((int) (vmsProfile.getTotalMemory() + vmResources.getMemoryInMegaBytes()));
            }
        }
        return vmsProfile;
    }

}
