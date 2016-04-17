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
package br.com.autonomiccs.autonomic.administration.algorithms.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.VmResources;

public class VmsDispersionAlgorithmForHomogeneousEnvironmentTest extends ConsolidationAlgorithmsTest {

    private VmsDispersionAlgorithmForHomogeneousEnvironment vmsDistributionAlgorithm;

    @Before
    public void setup() {
        vmsDistributionAlgorithm = Mockito.spy(new VmsDispersionAlgorithmForHomogeneousEnvironment());
    }

    @Test
    public void heuristicCannotShutdownHostTest() {
        Assert.assertFalse(vmsDistributionAlgorithm.canHeuristicShutdownHosts());
    }

    @Test
    public void rankHostHomogeneousMemoryConfigurationTest() {
        List<HostResources> hostsList = new ArrayList<HostResources>();
        HostResources h0 = createHostConfiguringUsedMemory(1024);
        hostsList.add(h0);
        HostResources h1 = createHostConfiguringUsedMemory(512);
        hostsList.add(h1);
        HostResources h2 = createHostConfiguringUsedMemory(2048);
        hostsList.add(h2);

        List<HostResources> rankedHostsList = vmsDistributionAlgorithm.rankHosts(hostsList);

        Assert.assertEquals(hostsList.get(1).getUsedMemoryInMegaBytes(), rankedHostsList.get(0).getUsedMemoryInMegaBytes());
        Assert.assertEquals(hostsList.get(0).getUsedMemoryInMegaBytes(), rankedHostsList.get(1).getUsedMemoryInMegaBytes());
        Assert.assertEquals(hostsList.get(2).getUsedMemoryInMegaBytes(), rankedHostsList.get(2).getUsedMemoryInMegaBytes());

        verifyRankHostCalledMethods(hostsList, rankedHostsList);
    }

    @Test
    public void rankHostWithOneHostOfZeroMemoryUsageTest() {
        List<HostResources> hostsList = new ArrayList<HostResources>();
        HostResources h0 = createHostConfiguringUsedMemory(1024);
        hostsList.add(h0);
        HostResources h1 = createEmptyHost();
        hostsList.add(h1);

        List<HostResources> rankedHostsList = vmsDistributionAlgorithm.rankHosts(hostsList);

        Assert.assertEquals(hostsList.get(1).getUsedMemoryInMegaBytes(), rankedHostsList.get(0).getUsedMemoryInMegaBytes());
        Assert.assertEquals(hostsList.get(0).getUsedMemoryInMegaBytes(), rankedHostsList.get(1).getUsedMemoryInMegaBytes());

        verifyRankHostCalledMethods(hostsList, rankedHostsList);
    }

    @Test
    public void mapVMsToHostTest() {
        HostResources h0 = createHostWithSmallVms(2);
        HostResources h1 = createHostWithSmallVms(1);
        HostResources h2 = createHostWithMediumVms(2);

        List<HostResources> hostsList = new ArrayList<HostResources>();
        hostsList.add(h0);
        hostsList.add(h1);
        hostsList.add(h2);

        List<HostResources> rankedHosts = vmsDistributionAlgorithm.rankHosts(hostsList);
        Map<Long, HostResources> mapTest = vmsDistributionAlgorithm.mapVMsToHost(rankedHosts);

        Assert.assertEquals(1, mapTest.size());

        long mapValues[] = new long[mapTest.size()];
        for (HostResources host : mapTest.values()) {
            mapValues[0] = host.getUsedMemoryInMegaBytes();
        }
        Assert.assertEquals(1536, mapValues[0]);

        verifyMapVmsToHostCalledMethods(rankedHosts, 3);
    }

    @Test
    public void mapVMsToHostTestWith2HostsHightVmStd() {
        List<VmResources> vms0 = createSmallVms(2);
        vms0.addAll(createMediumVms(4));
        HostResources h0 = createHostConfiguringVms(vms0);

        List<VmResources> vms1 = createHugeVms(1);
        HostResources h1 = createHostConfiguringVms(vms1);

        List<HostResources> hostsList = new ArrayList<HostResources>();
        hostsList.add(h0);
        hostsList.add(h1);

        List<HostResources> rankedHosts = vmsDistributionAlgorithm.rankHosts(hostsList);
        Map<Long, HostResources> mapTest = vmsDistributionAlgorithm.mapVMsToHost(rankedHosts);

        Assert.assertEquals(1, mapTest.size());
        verifyMapVmsToHostCalledMethods(rankedHosts, 3);
    }

    @Test
    public void mapVMsToHostTestWith5Hosts() {
        HostResources h0 = createHostWithSmallVms(2);

        HostResources h1 = createHostWithMediumVms(3);

        List<VmResources> vms2 = createSmallVms(2);
        vms2.add(createVmResourcesBig());
        HostResources h2 = createHostConfiguringVms(vms2);

        List<VmResources> vms3 = createHugeVms(1);
        vms3.add(createVmResourcesBig());
        HostResources h3 = createHostConfiguringVms(vms3);

        List<VmResources> vms4 = createSmallVms(2);
        vms4.add(createVmResourcesMedium());
        HostResources h4 = createHostConfiguringVms(vms4);

        List<HostResources> hostsList = new ArrayList<HostResources>();
        hostsList.add(h0);
        hostsList.add(h1);
        hostsList.add(h2);
        hostsList.add(h3);
        hostsList.add(h4);

        List<HostResources> rankedHosts = vmsDistributionAlgorithm.rankHosts(hostsList);
        Map<Long, HostResources> mapTest = vmsDistributionAlgorithm.mapVMsToHost(rankedHosts);

        Assert.assertEquals(2, mapTest.size());

        verifyMapVmsToHostCalledMethods(rankedHosts, 3);
    }

    @Test
    public void mapVMsToHostTestHostsWithoutLoad() {
        HostResources h0 = createEmptyHost();
        HostResources h1 = createEmptyHost();

        List<HostResources> hostsList = new ArrayList<HostResources>();
        hostsList.add(h0);
        hostsList.add(h1);

        List<HostResources> rankedHosts = vmsDistributionAlgorithm.rankHosts(hostsList);
        Map<Long, HostResources> mapTest = vmsDistributionAlgorithm.mapVMsToHost(rankedHosts);

        Assert.assertEquals(mapTest.size(), 0);
        verifyMapVmsToHostCalledMethods(rankedHosts, 0);
    }

    @Test
    public void mapVMsToHostTestDistributeToHostsWithoutLoad() {
        HostResources h0 = createHostWithSmallVms(3);

        List<HostResources> hostsList = new ArrayList<HostResources>();
        hostsList.add(h0);
        hostsList.add(createEmptyHost());
        hostsList.add(createEmptyHost());

        List<HostResources> rankedHosts = vmsDistributionAlgorithm.rankHosts(hostsList);
        Map<Long, HostResources> mapTest = vmsDistributionAlgorithm.mapVMsToHost(rankedHosts);

        Assert.assertEquals(2, mapTest.size());
        verifyMapVmsToHostCalledMethods(rankedHosts, 3);
    }

    @Test
    public void mapVMsToHostTestWithSameWorkload() {
        HostResources h0 = createHostWithMediumVms(1);
        HostResources h1 = createHostWithMediumVms(1);

        List<HostResources> hostsList = new ArrayList<HostResources>();
        hostsList.add(h0);
        hostsList.add(h1);

        List<HostResources> rankedHosts = vmsDistributionAlgorithm.rankHosts(hostsList);
        Map<Long, HostResources> mapTest = vmsDistributionAlgorithm.mapVMsToHost(rankedHosts);
        Assert.assertEquals(mapTest.size(), 0);
        verifyMapVmsToHostCalledMethods(rankedHosts, 0);
    }

    private void verifyRankHostCalledMethods(List<HostResources> hostsList, List<HostResources> rankedHostsList) {
        InOrder inOrder = Mockito.inOrder(vmsDistributionAlgorithm);
        inOrder.verify(vmsDistributionAlgorithm).clusterMemoryUsagePercentage(hostsList);
        inOrder.verify(vmsDistributionAlgorithm).cloneListOfHosts(hostsList);
        inOrder.verify(vmsDistributionAlgorithm).setEachHostScore(rankedHostsList);
        inOrder.verify(vmsDistributionAlgorithm).sortHosts(rankedHostsList);
    }

    private void verifyMapVmsToHostCalledMethods(List<HostResources> rankedHosts, int times) {
        InOrder inOrder = Mockito.inOrder(vmsDistributionAlgorithm);
        inOrder.verify(vmsDistributionAlgorithm, Mockito.times(times)).cloneListOfHosts(rankedHosts);
        inOrder.verify(vmsDistributionAlgorithm, Mockito.times(times)).simulateVmsMigrations(Mockito.anyListOf(HostResources.class), Mockito.anyDouble());
    }

}
