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
package br.com.autonomiccs.autonomic.administration.algorithms.impl;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import br.com.autonomiccs.autonomic.algorithms.commons.beans.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.beans.VmResources;

public class ConsolidationXenHaPreferenceForBigHostsHomogeneousEnvironmentTest extends ConsolidationAlgorithmsTest {

    private ConsolidationXenHaPreferenceForBigHostsHomogeneousEnvironment consolidationAlgorithm;

    @Before
    public void setup() {
        consolidationAlgorithm = Mockito.spy(new ConsolidationXenHaPreferenceForBigHostsHomogeneousEnvironment());
    }

    @Test
    public void consolidateClusterWithTwoHomogeneousHostsTest() {
        List<HostResources> hosts = createClusterHomogeneousHosts(2);
        Map<Long, HostResources> migrations = consolidationAlgorithm.mapVMsToHost(hosts);
        Assert.assertEquals(0, migrations.size());
    }

    @Test
    public void consolidateClusterWithThreeHomogeneousHostsTest() {
        consolidationAlgorithm.vmsDispersionHomogeneousHosts = Mockito.spy(new VmsDispersionAlgorithmForHomogeneousEnvironment());
        List<HostResources> hosts = createClusterHomogeneousHosts(3);

        Map<Long, HostResources> migrations = consolidationAlgorithm.mapVMsToHost(hosts);

        Assert.assertEquals(0, migrations.size());

        InOrder inOrder = Mockito.inOrder(consolidationAlgorithm.vmsDispersionHomogeneousHosts);
        inOrder.verify(consolidationAlgorithm.vmsDispersionHomogeneousHosts).rankHosts(hosts);
        inOrder.verify(consolidationAlgorithm.vmsDispersionHomogeneousHosts).mapVMsToHost(hosts);
    }

    @Test
    public void consolidateClusterWithFourHomogeneousHostsTest() {
        List<HostResources> hosts = createClusterHomogeneousHosts(4);

        Map<Long, HostResources> migrations = consolidationAlgorithm.mapVMsToHost(hosts);

        Assert.assertEquals(1, migrations.size());

        InOrder inOrder = Mockito.inOrder(consolidationAlgorithm);
        inOrder.verify(consolidationAlgorithm).canMigrateVmToHost(Mockito.any(VmResources.class), Mockito.any(HostResources.class));
        inOrder.verify(consolidationAlgorithm).updateHostUsedResources(Mockito.any(VmResources.class), Mockito.any(HostResources.class));
    }

    @Test
    public void consolidateClusterWithTenHomogeneousHostsTest() {
        List<HostResources> hosts = createClusterHomogeneousHosts(10);
        Map<Long, HostResources> migrations = consolidationAlgorithm.mapVMsToHost(hosts);
        Assert.assertEquals(7, migrations.size());
    }

    @Test
    public void removeOneIfPossibleTestListNotEmpty() {
        List<HostResources> hosts = createClusterHomogeneousHosts(2);
        consolidationAlgorithm.removeOneIfPossible(hosts);
        Assert.assertEquals(1, hosts.size());
    }

    @Test
    public void removeOneIfPossibleTestListEmpty() {
        List<HostResources> hosts = createClusterHomogeneousHosts(0);
        consolidationAlgorithm.removeOneIfPossible(hosts);
        Assert.assertEquals(0, hosts.size());
    }

    @Test
    public void rankHostToPowerOffOneIdleHostOutOfOneTest() {
        List<HostResources> clusterHomogeneousWithOneHost = createClusterHomogeneousHosts(1);
        List<HostResources> rankedHostToPowerOff = rankHostToPowerOffTestExecution(clusterHomogeneousWithOneHost);

        Assert.assertEquals(0, rankedHostToPowerOff.size());
    }

    @Test
    public void rankHostToPowerOffTwoIdleHostOutOfTwoTest() {
        List<HostResources> clusterHomogeneousWithOneHost = createClusterHomogeneousHosts(2);
        List<HostResources> rankedHostToPowerOff = rankHostToPowerOffTestExecution(clusterHomogeneousWithOneHost);

        Assert.assertEquals(0, rankedHostToPowerOff.size());
    }

    @Test
    public void rankHostToPowerOffthreeIdleHostsOutOfThreeTest() {
        List<HostResources> clusterHomogeneousWithOneHost = createClusterHomogeneousHosts(3);
        List<HostResources> rankedHostToPowerOff = rankHostToPowerOffTestExecution(clusterHomogeneousWithOneHost);

        Assert.assertEquals(0, rankedHostToPowerOff.size());
    }

    @Test
    public void rankHostToPowerOffFourHostsIdleOutOfFourTest() {
        List<HostResources> clusterHomogeneousWithOneHost = createClusterHomogeneousHosts(4);
        List<HostResources> rankedHostToPowerOff = rankHostToPowerOffTestExecution(clusterHomogeneousWithOneHost);

        Assert.assertEquals(1, rankedHostToPowerOff.size());
    }

    @Test
    public void rankHostToPowerOffTenHostsIdleOurOfTwelveTest() {
        List<HostResources> clusterHomogeneousWithOneHost = createClusterHomogeneousHosts(10);
        consolidationAlgorithm.rankHosts(clusterHomogeneousWithOneHost);
        consolidationAlgorithm.totalNumbersOfHostsInCluster = 12;
        List<HostResources> rankedHostToPowerOff = consolidationAlgorithm.rankHostToPowerOff(clusterHomogeneousWithOneHost);

        Assert.assertEquals(9, rankedHostToPowerOff.size());
    }

    private List<HostResources> rankHostToPowerOffTestExecution(List<HostResources> clusterHomogeneousWithOneHost) {
        consolidationAlgorithm.rankHosts(clusterHomogeneousWithOneHost);
        return consolidationAlgorithm.rankHostToPowerOff(clusterHomogeneousWithOneHost);
    }

}
