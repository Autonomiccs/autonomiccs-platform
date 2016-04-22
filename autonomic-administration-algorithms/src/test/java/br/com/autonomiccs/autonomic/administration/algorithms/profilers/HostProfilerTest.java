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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.autonomiccs.autonomic.administration.algorithms.impl.ConsolidationAlgorithmsTest;
import br.com.autonomiccs.autonomic.administration.algorithms.pojos.ClusterVmProfile;
import br.com.autonomiccs.autonomic.administration.algorithms.pojos.HostProfile;

@RunWith(MockitoJUnitRunner.class)
public class HostProfilerTest extends ConsolidationAlgorithmsTest {

    private HostProfiler spyProfiler;

    @Before
    public void setup() {
        spyProfiler = Mockito.spy(new HostProfiler(createHosts()));
    }

    @Test
    public void hostVMsResourceProportionTest() {
        HostProfile hostProfile = spyProfiler.hostVMsResourceProportion(createHost(4, 1000l, 0l, 5000l, 0l));

        Assert.assertNotNull(hostProfile);
        Assert.assertEquals(1.0, hostProfile.getCpuSpeedProfile(), 0.0);
        Assert.assertEquals(4, hostProfile.getCpusProfile(), 0.0);
        Assert.assertEquals(10, hostProfile.getMemoryProfile(), 0.0);

        InOrder inOrder = Mockito.inOrder(spyProfiler);
        inOrder.verify(spyProfiler).getHostsVmsResources();
        inOrder.verify(spyProfiler).createClusterVmsProfile(Mockito.any(ClusterVmProfile.class));
    }

    @Test
    public void createClusterVmsProfileTest() {
        ClusterVmProfile vmsProfile = spyProfiler.getHostsVmsResources();
        ClusterVmProfile result = spyProfiler.createClusterVmsProfile(vmsProfile);
        Assert.assertEquals(1, result.getCpusProfile(), 0.0);
        Assert.assertEquals(1000, result.getCpuSpeedProfile(), 0.0);
        Assert.assertEquals(500, result.getMemoryProfile(), 0.0);
    }

    @Test
    public void getHostsVmsResources() {
        ClusterVmProfile vmsProfile = spyProfiler.getHostsVmsResources();
        Assert.assertEquals(3, vmsProfile.getNumberOfInstances());
        Assert.assertEquals(3, vmsProfile.getTotalCpus());
        Assert.assertEquals(3000, vmsProfile.getTotalCpuSpeed());
        Assert.assertEquals(1500, vmsProfile.getTotalMemory());
    }

}
