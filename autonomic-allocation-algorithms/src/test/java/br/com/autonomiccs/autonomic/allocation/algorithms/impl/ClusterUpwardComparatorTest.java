
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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResources;
import br.com.autonomiccs.autonomic.allocation.algorithms.impl.ScoredClustersAllocationAlgorithmPreferenceForSmallHosts.ClusterUpwardComparator;

@RunWith(MockitoJUnitRunner.class)
public class ClusterUpwardComparatorTest {

    private ClusterUpwardComparator clusterComparator = new ClusterUpwardComparator();

    @Test
    public void compareTestH1BiggerThanH2() {
        ClusterResources c1 = getScoredClusterMock(1d);
        ClusterResources c2 = getScoredClusterMock(0d);
        int result = clusterComparator.compare(c1, c2);

        verifyGetScoreExecution(c1, c2);
        Assert.assertEquals(1, result);
    }

    @Test
    public void compareTestH1SmallerThanH2() {
        ClusterResources c1 = getScoredClusterMock(0d);
        ClusterResources c2 = getScoredClusterMock(1d);
        int result = clusterComparator.compare(c1, c2);

        verifyGetScoreExecution(c1, c2);
        Assert.assertEquals(-1, result);
    }

    private void verifyGetScoreExecution(ClusterResources c1, ClusterResources c2) {
        Mockito.verify(c1).getScore();
        Mockito.verify(c2).getScore();
    }

    private ClusterResources getScoredClusterMock(double score) {
        ClusterResources c = Mockito.mock(ClusterResources.class);
        Mockito.when(c.getScore()).thenReturn(score);
        return c;
    }

}
