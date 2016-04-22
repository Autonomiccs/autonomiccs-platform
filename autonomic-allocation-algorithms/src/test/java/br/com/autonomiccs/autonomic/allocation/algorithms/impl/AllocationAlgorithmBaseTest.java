
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
package br.com.autonomiccs.autonomic.allocation.algorithms.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesAvailableToStart;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.allocation.algorithms.AllocationAlgorithm;

@RunWith(MockitoJUnitRunner.class)
public class AllocationAlgorithmBaseTest {

    private AllocationAlgorithmBase spyAlgorithm;

    @Before
    public void setUp() {
        spyAlgorithm = Mockito.spy(new AllocationAlgorithmBase());
    }

    @Test
    public void implementsAllocationAlgorithm() {
        Assert.assertTrue(AllocationAlgorithm.class.isAssignableFrom(AllocationAlgorithmBase.class));
    }

    @Test
    public void cloneListOfClustersTest() {
        List<ClusterResourcesAvailableToStart> clusters = new ArrayList<ClusterResourcesAvailableToStart>();
        ClusterResourcesAvailableToStart cluster1 = Mockito.mock(ClusterResourcesAvailableToStart.class);
        clusters.add(cluster1);
        List<ClusterResourcesAvailableToStart> clonedClusters = spyAlgorithm.cloneListOfClusters(clusters);

        Assert.assertNotNull(clonedClusters);
        Assert.assertEquals(1, clonedClusters.size());
        Assert.assertEquals(clusters, clonedClusters);
        Assert.assertEquals(cluster1, clonedClusters.get(0));
    }

    @Test
    public void cloneListOfHostsTest() {
        List<HostResources> hosts = new ArrayList<HostResources>();
        HostResources host = Mockito.mock(HostResources.class);
        hosts.add(host);

        List<HostResources> clonedHosts = spyAlgorithm.cloneListOfHosts(hosts);

        Assert.assertNotNull(clonedHosts);
        Assert.assertEquals(1, clonedHosts.size());
        Assert.assertEquals(hosts, clonedHosts);
        Assert.assertEquals(host, clonedHosts.get(0));
    }

    @Test
    public void rankClustersToAllocationTest() {
        List<ClusterResourcesAvailableToStart> clusters = new ArrayList<ClusterResourcesAvailableToStart>();
        Mockito.doReturn(clusters).when(spyAlgorithm).cloneListOfClusters(clusters);

        List<ClusterResourcesAvailableToStart> clonedClustersToAllocation = spyAlgorithm.rankClustersToAllocation(clusters);

        Mockito.verify(spyAlgorithm).cloneListOfClusters(clusters);
        Assert.assertEquals(clusters, clonedClustersToAllocation);
    }

    @Test
    public void needsToActivateHostTest() {
        CloudResources cloudCapacity = Mockito.mock(CloudResources.class);
        Assert.assertFalse(spyAlgorithm.needsToActivateHost(cloudCapacity));
    }

    @Test
    public void rankHostsToStartTest() {
        List<HostResources> hosts = new ArrayList<HostResources>();
        Mockito.doReturn(hosts).when(spyAlgorithm).cloneListOfHosts(hosts);

        List<HostResources> clonedHostsToStart = spyAlgorithm.rankHostsToStart(hosts);

        Mockito.verify(spyAlgorithm).cloneListOfHosts(hosts);
        Assert.assertEquals(hosts, clonedHostsToStart);
    }

}
