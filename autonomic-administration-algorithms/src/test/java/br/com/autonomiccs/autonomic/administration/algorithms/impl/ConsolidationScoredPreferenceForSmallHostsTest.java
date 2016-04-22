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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import br.com.autonomiccs.autonomic.administration.algorithms.pojos.HostProfile;
import br.com.autonomiccs.autonomic.administration.algorithms.profilers.HostProfiler;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

public class ConsolidationScoredPreferenceForSmallHostsTest extends ConsolidationAlgorithmsTest {

    private ConsolidationScoredPreferenceForSmallHosts spyAlgorithm;

    @Before
    public void setup() {
        spyAlgorithm = Mockito.spy(new ConsolidationScoredPreferenceForSmallHosts());
    }

    @Test
    public void rankHostsTest() {
        spyAlgorithm.rankHosts(createHosts());
        InOrder inOrder = Mockito.inOrder(spyAlgorithm);
        inOrder.verify(spyAlgorithm).cloneListOfHosts(Mockito.anyListOf(HostResources.class));
        inOrder.verify(spyAlgorithm).setEachHostScore(Mockito.anyListOf(HostResources.class));
        inOrder.verify(spyAlgorithm).sortHosts(Mockito.anyListOf(HostResources.class));
    }

    @Test
    public void sortHostsTest() {
        spyAlgorithm.sortHosts(createHosts());
        Mockito.verify(spyAlgorithm).sortHostsUpwardScore(Mockito.anyListOf(HostResources.class));
    }

    @Test
    public void sortHostsUpwardScoreTest() {
        List<HostResources> hostsToSort = setupOfSortHostTests();
        spyAlgorithm.sortHostsUpwardScore(hostsToSort);

        List<HostResources> expectedHostsOrder = new ArrayList<HostResources>();
        expectedHostsOrder.add(createHostResourcesWithScore(0));
        expectedHostsOrder.add(createHostResourcesWithScore(3));
        expectedHostsOrder.add(createHostResourcesWithScore(5));
        expectedHostsOrder.add(createHostResourcesWithScore(12));

        checkIfSortIsCorrect(hostsToSort, expectedHostsOrder);
    }

    @Test
    public void sortHostsDownwardScoreTest() {
        List<HostResources> hostsToSort = setupOfSortHostTests();
        spyAlgorithm.sortHostsDownwardScore(hostsToSort);

        List<HostResources> expectedHostsOrder = new ArrayList<HostResources>();
        expectedHostsOrder.add(createHostResourcesWithScore(12));
        expectedHostsOrder.add(createHostResourcesWithScore(5));
        expectedHostsOrder.add(createHostResourcesWithScore(3));
        expectedHostsOrder.add(createHostResourcesWithScore(0));

        checkIfSortIsCorrect(hostsToSort, expectedHostsOrder);
    }

    @Test
    public void getCloudExpetedMemoryUsagePercentageAfterHostShutdownTest() {
        HostResources host = createHost(2, 1000l, 0l, 2000l, 0l);
        CloudResources cloud = new CloudResources(null, 4000l * BYTES_TO_MEGA_BYTES, 10000l * BYTES_TO_MEGA_BYTES, 0, 0, 0);
        Assert.assertEquals(0.5, spyAlgorithm.getCloudExpetedMemoryUsagePercentageAfterHostShutdown(host, cloud), 0.0);
    }

    @Test
    public void getCloudExpetedCpuUsageAfterHostShutdownTest() {
        HostResources host = createHost(2, 1000l, 0l, 2000l, 0l);
        CloudResources cloud = new CloudResources(null, 0, 0, 3000l, 2000, 4);
        Assert.assertEquals(0.5, spyAlgorithm.getCloudExpetedCpuUsageAfterHostShutdown(host, cloud), 0.0);
        Mockito.verify(spyAlgorithm).getCloudCpuCapacity(cloud);
    }

    @Test
    public void getCloudCpuCapacityTest() {
        CloudResources cloud = new CloudResources(null, 0, 0, 3000l, 2000, 4);
        Assert.assertEquals(8000, spyAlgorithm.getCloudCpuCapacity(cloud), 0.0);
    }

    @Test
    public void internalCanPowerOffAnotherHostInCloudTestFalseCausedByCpu() {
        CloudResources cloud = new CloudResources(null, 0l, 10000l * BYTES_TO_MEGA_BYTES, 7001l, 2000l, 5);
        Assert.assertFalse(spyAlgorithm.internalCanPowerOffAnotherHostInCloud(cloud));
        verifyMethodsCalledFromInternalCanPowerOffAnotherHostInCloud(cloud, 1, 1, 0);
    }

    @Test
    public void internalCanPowerOffAnotherHostInCloudTestFalseCausedByMemory() {
        CloudResources cloud = new CloudResources(null, 7000l * BYTES_TO_MEGA_BYTES, 10000l * BYTES_TO_MEGA_BYTES, 0, 2000l, 5);
        Assert.assertFalse(spyAlgorithm.internalCanPowerOffAnotherHostInCloud(cloud));

        verifyMethodsCalledFromInternalCanPowerOffAnotherHostInCloud(cloud, 1, 1, 1);
    }

    @Test
    public void internalCanPowerOffAnotherHostInCloudTestTrue() {
        CloudResources cloud = new CloudResources(null, 5000l * BYTES_TO_MEGA_BYTES, 10000l * BYTES_TO_MEGA_BYTES, 5000, 2000l, 5);
        Assert.assertTrue(spyAlgorithm.internalCanPowerOffAnotherHostInCloud(cloud));

        verifyMethodsCalledFromInternalCanPowerOffAnotherHostInCloud(cloud, 1, 1, 1);
    }

    @Test
    public void cloudMemoryUsagePercentageTest() {
        CloudResources cloud = new CloudResources(null, 5000l * BYTES_TO_MEGA_BYTES, 10000l * BYTES_TO_MEGA_BYTES, 5000, 2000l, 5);
        Assert.assertEquals(0.5, spyAlgorithm.cloudMemoryUsagePercentage(cloud), 0.0);
    }

    @Test
    public void cloudCpuUsagePercentageTest() {
        CloudResources cloud = new CloudResources(null, 5000l * BYTES_TO_MEGA_BYTES, 10000l * BYTES_TO_MEGA_BYTES, 5000, 2000l, 5);
        Assert.assertEquals(0.5, spyAlgorithm.cloudCpuUsagePercentage(cloud), 0.0);
        Mockito.verify(spyAlgorithm).getCloudCpuCapacity(cloud);
    }

    @Test
    public void setEachHostScoreTest() {
        Mockito.doReturn(1.0).when(spyAlgorithm).calculateHostScore(Mockito.any(HostResources.class));
        List<HostResources> hosts = createHosts();
        spyAlgorithm.setEachHostScore(hosts);
        Mockito.verify(spyAlgorithm, Mockito.times(2)).calculateHostScore(Mockito.any(HostResources.class));
        for (HostResources host : hosts) {
            Assert.assertEquals(1.0, host.getScore(), 0.0);
        }
    }

    @Test
    public void calculateHostScoreTest() {
        List<HostResources> hosts = createHosts();
        HostProfile hostProfile = Mockito.mock(HostProfile.class);
        Mockito.when(hostProfile.getCpuSpeedProfile()).thenReturn(5.0);
        Mockito.when(hostProfile.getCpusProfile()).thenReturn(2.0);
        Mockito.when(hostProfile.getMemoryProfile()).thenReturn(1.0);

        HostProfiler spyProfiler = Mockito.spy(new HostProfiler(hosts));
        spyAlgorithm.profiler = spyProfiler;
        Mockito.doReturn(hostProfile).when(spyProfiler).hostVMsResourceProportion(Mockito.any(HostResources.class));
        double result = spyAlgorithm.calculateHostScore(hosts.get(0));

        Assert.assertEquals(10.0, result, 0.0);
        Mockito.verify(spyProfiler).hostVMsResourceProportion(Mockito.any(HostResources.class));
        Mockito.verify(hostProfile).getCpuSpeedProfile();
        Mockito.verify(hostProfile).getCpusProfile();
        Mockito.verify(hostProfile).getMemoryProfile();
    }

    @Test
    public void rankHostToPowerOffTest() {
        List<HostResources> hosts = createHosts();

        Mockito.doReturn(hosts).when(spyAlgorithm).setEachHostScore(Mockito.anyListOf(HostResources.class));
        Mockito.doNothing().when(spyAlgorithm).sortHostsDownwardScore(hosts);

        spyAlgorithm.rankHostToPowerOff(hosts);

        InOrder inOrder = Mockito.inOrder(spyAlgorithm);
        inOrder.verify(spyAlgorithm).cloneListOfHosts(Mockito.anyListOf(HostResources.class));
        inOrder.verify(spyAlgorithm).setEachHostScore(Mockito.anyListOf(HostResources.class));
        inOrder.verify(spyAlgorithm).sortHostsDownwardScore(Mockito.anyListOf(HostResources.class));
    }

    @Test
    public void internalCanPowerOffHostTestFalseCausedByCpu() {
        HostResources host = createHost(3, 2000l, 0l, 2000l, 0l);
        CloudResources cloud = new CloudResources(null, 5000l * BYTES_TO_MEGA_BYTES, 10000l * BYTES_TO_MEGA_BYTES, 5000, 2000l, 5);
        Assert.assertFalse(spyAlgorithm.internalCanPowerOffHost(host, cloud));
        verifyInternalCannPowerOffCalledMethods(1, 0);
    }

    @Test
    public void internalCanPowerOffHostTestFalseCausedByMemory() {
        HostResources host = createHost(2, 1000l, 0l, 3000l, 0l);
        CloudResources cloud = new CloudResources(null, 5000l * BYTES_TO_MEGA_BYTES, 10000l * BYTES_TO_MEGA_BYTES, 5000, 2000l, 5);
        Assert.assertFalse(spyAlgorithm.internalCanPowerOffHost(host, cloud));
        verifyInternalCannPowerOffCalledMethods(1, 1);
    }

    @Test
    public void internalCanPowerOffHostTestTrue() {
        HostResources host = createHost(2, 1000l, 0l, 2000l, 0l);
        CloudResources cloud = new CloudResources(null, 5000l * BYTES_TO_MEGA_BYTES, 10000l * BYTES_TO_MEGA_BYTES, 5000, 2000l, 5);
        Assert.assertTrue(spyAlgorithm.internalCanPowerOffHost(host, cloud));
        verifyInternalCannPowerOffCalledMethods(1, 1);
    }

    private List<HostResources> setupOfSortHostTests() {
        List<HostResources> hostsToSort = new ArrayList<HostResources>();
        hostsToSort.add(createHostResourcesWithScore(0));
        hostsToSort.add(createHostResourcesWithScore(5));
        hostsToSort.add(createHostResourcesWithScore(12));
        hostsToSort.add(createHostResourcesWithScore(3));
        return hostsToSort;
    }

    private void checkIfSortIsCorrect(List<HostResources> hostsToSort, List<HostResources> expectedHostsOrder) {
        for (int i = 0; i < expectedHostsOrder.size(); i++) {
            Assert.assertEquals(expectedHostsOrder.get(i).getScore(), hostsToSort.get(i).getScore(), 0);
        }
    }

    private HostResources createHostResourcesWithScore(int score) {
        HostResources hostResources = new HostResources();
        hostResources.setScore(score);
        return hostResources;
    }

    private void verifyMethodsCalledFromInternalCanPowerOffAnotherHostInCloud(CloudResources cloud, int cpuUsageTimes, int cpuCapacityTimes, int memoryUsageTimes) {
        InOrder inOrder = Mockito.inOrder(spyAlgorithm);
        inOrder.verify(spyAlgorithm, Mockito.times(cpuUsageTimes)).cloudCpuUsagePercentage(cloud);
        inOrder.verify(spyAlgorithm, Mockito.times(cpuCapacityTimes)).getCloudCpuCapacity(cloud);
        inOrder.verify(spyAlgorithm, Mockito.times(memoryUsageTimes)).cloudMemoryUsagePercentage(cloud);
    }

    private void verifyInternalCannPowerOffCalledMethods(int timesCpuUsage, int timesMemoryUsage) {
        InOrder inOrder = Mockito.inOrder(spyAlgorithm);
        inOrder.verify(spyAlgorithm, Mockito.times(timesCpuUsage)).getCloudExpetedCpuUsageAfterHostShutdown(Mockito.any(HostResources.class), Mockito.any(CloudResources.class));
        inOrder.verify(spyAlgorithm, Mockito.times(timesMemoryUsage)).getCloudExpetedMemoryUsagePercentageAfterHostShutdown(Mockito.any(HostResources.class),
                Mockito.any(CloudResources.class));
    }

}
