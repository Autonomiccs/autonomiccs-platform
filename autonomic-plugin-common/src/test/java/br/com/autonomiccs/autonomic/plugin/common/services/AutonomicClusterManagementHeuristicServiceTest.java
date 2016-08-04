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

import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.autonomiccs.autonomic.administration.algorithms.ClusterAdministrationHeuristicAlgorithm;
import br.com.autonomiccs.autonomic.administration.algorithms.impl.ClusterManagementDummyAlgorithm;
import br.com.autonomiccs.autonomic.administration.algorithms.impl.ConsolidationAlgorithmBase;

@RunWith(MockitoJUnitRunner.class)
public class AutonomicClusterManagementHeuristicServiceTest {

    private String CLUSTER_ADMINISTRATION_ALGORITHMS_IN_CONFIGURATION_KEY = "autonomiccs.clustermanager.algorithm";

    @Spy
    @InjectMocks
    private AutonomicClusterManagementHeuristicService spy;

    @Mock
    private ConfigurationDao configurationDao;

    @Test
    public void getAdministrationAlgorithmTest() {
        Mockito.when(configurationDao.getValue(Mockito.eq(CLUSTER_ADMINISTRATION_ALGORITHMS_IN_CONFIGURATION_KEY))).thenReturn("algorithmName");
        spy.getAdministrationAlgorithm();
        verifyGetAdministrationAlgorithm(1);
    }

    @Test
    public void getAdministrationAlgorithmTestAlgorithmNameIsBlank() {
        Mockito.when(configurationDao.getValue(Mockito.eq(CLUSTER_ADMINISTRATION_ALGORITHMS_IN_CONFIGURATION_KEY))).thenReturn("");
        spy.getAdministrationAlgorithm();
        verifyGetAdministrationAlgorithm(0);
    }

    @Test
    public void loadAlgorithmClassTest() throws ClassNotFoundException {
        spy.loadAlgorithmClass("br.com.autonomiccs.autonomic.administration.algorithms.impl.ClusterManagementDummyAlgorithm");
        Assert.assertEquals(1, spy.algorithmsMap.size());
        Assert.assertTrue(spy.algorithmsMap.containsKey("br.com.autonomiccs.autonomic.administration.algorithms.impl.ClusterManagementDummyAlgorithm"));
    }

    @Test(expected = ClassNotFoundException.class)
    public void loadAlgorithmClassTestExpectClassNotFoundException() throws ClassNotFoundException {
        spy.loadAlgorithmClass("this.class.does.not.exist");
    }

    @Test
    public void getInstanceOfClassTestMethodWithParameterStringHeuristicIsOnAlgorithmsMap() throws ClassNotFoundException {
        spy.algorithmsMap.put("br.com.autonomiccs.autonomic.administration.algorithms.impl.ConsolidationAlgorithmBase", ConsolidationAlgorithmBase.class);
        ClusterAdministrationHeuristicAlgorithm result = spy.getInstanceOfClass("br.com.autonomiccs.autonomic.administration.algorithms.impl.ConsolidationAlgorithmBase");
        Mockito.verify(spy).getInstanceOfClass(Mockito.anyString());
        Assert.assertEquals(ConsolidationAlgorithmBase.class, result.getClass());
    }

    @Test
    public void getInstanceOfClassTestMethodWithParameterStringAlgorithmsMapNullAlgorithmIsNull() throws ClassNotFoundException {
        Mockito.doReturn(ClusterManagementDummyAlgorithm.class).when(spy).loadAlgorithmClass(Mockito.anyString());

        ClusterAdministrationHeuristicAlgorithm result = spy.getInstanceOfClass("this.class.does.not.exist");

        verifygetInstanceOfClassCalledMethods();
        Assert.assertEquals(ClusterManagementDummyAlgorithm.class, result.getClass());
    }

    @Test
    public void getInstanceOfClassTestMethodWithParameterStringCatchException() throws ClassNotFoundException {
        Mockito.doReturn(null).when(spy).loadAlgorithmClass(Mockito.anyString());

        ClusterAdministrationHeuristicAlgorithm result = spy.getInstanceOfClass("this.class.does.not.exist");

        verifygetInstanceOfClassCalledMethods();
        Assert.assertEquals(ClusterManagementDummyAlgorithm.class, result.getClass());
    }

    @Test
    public void getInstanceOfClassTestMethodWithParameterClusterAdministrationInstantiationException() throws Exception {
        ClusterAdministrationHeuristicAlgorithm result = spy.getInstanceOfClass(TestAlgorithmThrowingInstantiationException.class);
        Mockito.verify(spy).getDummyClusterManagementHeuristc();
        Assert.assertEquals(ClusterManagementDummyAlgorithm.class, result.getClass());
    }

    @Test
    public void getInstanceOfClassTestMethodWithParameterClusterAdministrationIllegalAccessException() throws Exception {
        ClusterAdministrationHeuristicAlgorithm result = spy.getInstanceOfClass(TestAlgorithmThrowingIllegalAccessException.class);
        Mockito.verify(spy).getDummyClusterManagementHeuristc();
        Assert.assertEquals(ClusterManagementDummyAlgorithm.class, result.getClass());
    }

    private void verifygetInstanceOfClassCalledMethods() throws ClassNotFoundException {
        Mockito.verify(spy).loadAlgorithmClass(Mockito.anyString());
        Mockito.verify(spy).getInstanceOfClass(Mockito.anyString());
    }

    @Test
    public void getDummyClusterManagementHeuristcTest() {
        ClusterAdministrationHeuristicAlgorithm result = spy.getDummyClusterManagementHeuristc();
        Assert.assertEquals(ClusterManagementDummyAlgorithm.class, result.getClass());
        Mockito.verify(spy).getInstanceOfClass(Mockito.eq(ClusterManagementDummyAlgorithm.class));
    }

    private void verifyGetAdministrationAlgorithm(int times) {
        Mockito.verify(spy).getDummyClusterManagementHeuristc();
        Mockito.verify(spy, Mockito.times(times)).getInstanceOfClass(Mockito.anyString());
    }

    private class TestAlgorithmThrowingInstantiationException extends ClusterManagementDummyAlgorithm {
        @SuppressWarnings("unused")
        public TestAlgorithmThrowingInstantiationException newInstance() throws InstantiationException {
            throw new InstantiationException();
        }
    }

    private class TestAlgorithmThrowingIllegalAccessException extends ClusterManagementDummyAlgorithm {
        @SuppressWarnings("unused")
        public TestAlgorithmThrowingIllegalAccessException newInstance() throws IllegalAccessException {
            throw new IllegalAccessException();
        }
    }

}
