
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

import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.allocation.algorithms.impl.ScoredClustersAllocationAlgorithmPreferenceForSmallHosts.HostUpwardComparator;

@RunWith(MockitoJUnitRunner.class)
public class HostUpwardComparatorTest {

    private HostUpwardComparator hostComparator = new HostUpwardComparator();

    @Test
    public void compareTestH1BiggerThanH2() {
        HostResources h1 = getScoredHostMock(1d);
        HostResources h2 = getScoredHostMock(0d);
        int result = hostComparator.compare(h1, h2);

        verifyGetScoreExecution(h1, h2);
        Assert.assertEquals(1, result);
    }

    @Test
    public void compareTestH1SmallerThanH2() {
        HostResources h1 = getScoredHostMock(0d);
        HostResources h2 = getScoredHostMock(1d);
        int result = hostComparator.compare(h1, h2);

        verifyGetScoreExecution(h1, h2);
        Assert.assertEquals(-1, result);
    }

    private void verifyGetScoreExecution(HostResources h1, HostResources h2) {
        Mockito.verify(h1).getScore();
        Mockito.verify(h2).getScore();
    }

    private HostResources getScoredHostMock(double score) {
        HostResources h = Mockito.mock(HostResources.class);
        Mockito.when(h.getScore()).thenReturn(score);
        return h;
    }

}
