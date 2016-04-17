/*
 * This program is part of Autonomiccs "autonomic-platform",
 * an open source autonomic cloud computing management platform.
 * Copyright (C) 2016 Autonomiccs, Inc.
 *
 * Licensed to the Autonomiccs, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package br.com.autonomiccs.autonomic.algorithms.commons.services;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesUp;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

public class CloudResourcesServiceTest extends ClusterResourcesServiceTest {

    private CloudResourcesService cloudResourcesService = new CloudResourcesService();

    @Test
    public void createCloudResourcesTestOneCluster() {
        List<ClusterResourcesUp> clustersResourcesUp = createAllClustersResources(1);
        executeCreateCloudResourcesTest(clustersResourcesUp);
    }

    @Test
    public void createCloudResourcesTestFourCluster() {
        List<ClusterResourcesUp> clustersResourcesUp = createAllClustersResources(4);
        executeCreateCloudResourcesTest(clustersResourcesUp);
    }

    private void executeCreateCloudResourcesTest(List<ClusterResourcesUp> clustersResourcesUp) {
        int amountCLusters = clustersResourcesUp.size();
        CloudResources cloud = cloudResourcesService.createCloudResources(clustersResourcesUp);
        Assert.assertEquals(1 * amountCLusters, cloud.getClusters().size());
        Assert.assertEquals(4 * amountCLusters, cloud.getCpus());
        Assert.assertEquals(512 * amountCLusters, cloud.getUsedMemory());
        Assert.assertEquals(1000l * amountCLusters, cloud.getUsedCpu());
        Assert.assertEquals(2000l * amountCLusters, cloud.getCpuSpeed());
        Assert.assertEquals(8192l * MEGA_BYTES_TO_BYTES * amountCLusters, cloud.getMemoryInBytes());
    }

    private List<ClusterResourcesUp> createAllClustersResources(int nOfClusters) {
        List<ClusterResourcesUp> clusters = new ArrayList<ClusterResourcesUp>();
        for (int i = 1; i <= nOfClusters; i++) {
            List<HostResources> hosts = createClusterHomogeneousHosts(1);
            ClusterResourcesUp cluster = clusterResourcesService.createClusterResourcesUp(random.nextLong(), "cluster", hosts);
            clusters.add(cluster);
        }
        return clusters;
    }
}
