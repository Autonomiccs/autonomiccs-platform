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
package br.com.autonomiccs.autonomic.allocation.algorithms.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

@RunWith(MockitoJUnitRunner.class)
public class ScoredClustersAllocationAlgorithmPreferenceForBigHostsTest extends ScoredClustersAllocationAlgorithm {

    private ScoredClustersAllocationAlgorithmPreferenceForBigHosts spyAlgorithm;
    private List<HostResources> hosts;

    @Before
    public void setup() {
        spyAlgorithm = PowerMockito.spy(new ScoredClustersAllocationAlgorithmPreferenceForBigHosts());
        hosts = new ArrayList<HostResources>();
    }

    @Test
    public void sortHostsTest() {
        Mockito.doNothing().when(spyAlgorithm).sortHostsDownwardScore(hosts);
        spyAlgorithm.sortHosts(hosts);

        Mockito.verify(spyAlgorithm).sortHostsDownwardScore(hosts);
    }

    @Test
    public void sortHostsDownwardScoreTest() {
        List<HostResources> hosts = createHostsWithScore();

        spyAlgorithm.sortHostsDownwardScore(hosts);

        Assert.assertEquals(4.0, hosts.get(0).getScore(), 0);
        Assert.assertEquals(3.0, hosts.get(1).getScore(), 0);
        Assert.assertEquals(2.0, hosts.get(2).getScore(), 0);
        Assert.assertEquals(1.0, hosts.get(3).getScore(), 0);
    }

}
