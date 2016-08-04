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
package br.com.autonomiccs.autonomic.plugin.common.services;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;

@RunWith(MockitoJUnitRunner.class)
public class ClusterServiceTest {

    @Spy
    @InjectMocks
    private ClusterService spy;
    @Mock
    private ClusterDao clusterDao;

    private List<ClusterVO> clusters = new ArrayList<>();

    @Test
    public void listAllClustersFromPodTest() {
        Mockito.doReturn(clusters).when(clusterDao).listByPodId(Mockito.anyLong());

        List<ClusterVO> result = spy.listAllClustersFromPod(0l);

        Mockito.verify(clusterDao).listByPodId(Mockito.anyLong());
        Assert.assertEquals(clusters, result);
    }

    @Test
    public void findByIdTest() {
        ClusterVO cluster = new ClusterVO();
        Mockito.doReturn(cluster).when(clusterDao).findById(Mockito.anyLong());

        ClusterVO result = spy.findById(0l);

        Mockito.verify(clusterDao).findById(Mockito.anyLong());
        Assert.assertEquals(cluster, result);
    }

    @Test
    public void listAllClustersOnZoneTest() {
        Mockito.doReturn(clusters).when(clusterDao).listClustersByDcId(Mockito.anyLong());

        List<ClusterVO> result = spy.listAllClustersOnZone(0l);

        Mockito.verify(clusterDao).listClustersByDcId(Mockito.anyLong());
        Assert.assertEquals(clusters, result);
    }

    @Test
    public void listAllClustersTest() {
        Mockito.doReturn(clusters).when(clusterDao).listAll();

        List<ClusterVO> result = spy.listAllClusters();

        Mockito.verify(clusterDao).listAll();
        Assert.assertEquals(clusters, result);
    }

}
