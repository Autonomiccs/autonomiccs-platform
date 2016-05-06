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
package br.com.autonomiccs.autonomic.administration.plugin.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;

import br.com.autonomiccs.autonomic.administration.algorithms.ClusterAdministrationHeuristicAlgorithm;
import br.com.autonomiccs.autonomic.plugin.common.daos.ClusterJdbcDao;
import br.com.autonomiccs.autonomic.plugin.common.enums.ClusterAdministrationStatus;

@RunWith(PowerMockRunner.class)
public class AutonomicClusterManagementServiceTest {

    private AutonomicClusterManagementService autonomicClusterManagementService;
    @Mock
    private ClusterJdbcDao clusterDaoJdbc;
    @Mock
    private ClusterDao clusterDao;

    @Before
    public void setup() {
        autonomicClusterManagementService = Mockito.spy(new AutonomicClusterManagementService());
        autonomicClusterManagementService.clusterDaoJdbc = clusterDaoJdbc;
        autonomicClusterManagementService.clusterDao = clusterDao;
    }

    @Test
    public void isClusterBeingAdministratedTest() {
        Mockito.doReturn(ClusterAdministrationStatus.InProgress).when(clusterDaoJdbc).getClusterAdministrationStatus(Mockito.anyLong());
        boolean result = autonomicClusterManagementService.isClusterBeingAdministrated(0l);

        Mockito.verify(clusterDaoJdbc).getClusterAdministrationStatus(Mockito.anyLong());
        Assert.assertTrue(result);
    }

    @Test
    public void isClusterBeingAdministratedTestNotInProgress() {
        Mockito.doReturn(ClusterAdministrationStatus.Done).when(clusterDaoJdbc).getClusterAdministrationStatus(Mockito.anyLong());
        boolean result = autonomicClusterManagementService.isClusterBeingAdministrated(0l);
        Mockito.verify(clusterDaoJdbc).getClusterAdministrationStatus(Mockito.anyLong());
        Assert.assertFalse(result);
    }

    @Test
    @PrepareForTest(AutonomicClusterManagementService.class)
    public void canProcessClusterTest() throws Exception {
        ClusterAdministrationHeuristicAlgorithm algorithm = Mockito.mock(ClusterAdministrationHeuristicAlgorithm.class);
        setCanProcessCluster(algorithm, getLastAdministrationDate(), 5, 1);

        boolean result = autonomicClusterManagementService.canProcessCluster(0l, algorithm);

        Assert.assertTrue(result);
        verifyCanProcessClusterCalledMethods(algorithm, 1);
    }

    @Test
    @PrepareForTest(AutonomicClusterManagementService.class)
    public void canProcessClusterTestIsNotTimeYet() throws Exception {
        ClusterAdministrationHeuristicAlgorithm algorithm = Mockito.mock(ClusterAdministrationHeuristicAlgorithm.class);
        setCanProcessCluster(algorithm, getLastAdministrationDate(), 0, 1);

        boolean result = autonomicClusterManagementService.canProcessCluster(0l, algorithm);

        Assert.assertFalse(result);
        verifyCanProcessClusterCalledMethods(algorithm, 1);
    }

    @Test
    public void canProcessClusterTestDateNull() throws Exception {
        ClusterAdministrationHeuristicAlgorithm algorithm = Mockito.mock(ClusterAdministrationHeuristicAlgorithm.class);
        setCanProcessCluster(algorithm, null, 1, 1);

        boolean result = autonomicClusterManagementService.canProcessCluster(0l, algorithm);

        Assert.assertTrue(result);
        verifyCanProcessClusterCalledMethods(algorithm, 0);
    }

    @Test
    public void setClusterWorkInProgressTest() {
        Mockito.doNothing().when(clusterDaoJdbc).setClusterAdministrationStatus(Mockito.any(ClusterAdministrationStatus.class), Mockito.anyLong());
        autonomicClusterManagementService.setClusterWorkInProgress(0l);
        Mockito.verify(clusterDaoJdbc).setClusterAdministrationStatus(Mockito.any(ClusterAdministrationStatus.class), Mockito.anyLong());
    }

    @Test
    public void markAdministrationStatusInClusterAsDoneTest() {
        Mockito.doNothing().when(clusterDaoJdbc).setClusterLastAdministration(Mockito.any(Date.class), Mockito.anyLong());
        Mockito.doNothing().when(clusterDaoJdbc).setClusterAdministrationStatus(Mockito.any(ClusterAdministrationStatus.class), Mockito.anyLong());

        autonomicClusterManagementService.markAdministrationStatusInClusterAsDone(0l);

        InOrder inOrder = Mockito.inOrder(clusterDaoJdbc);
        inOrder.verify(clusterDaoJdbc).setClusterLastAdministration(Mockito.any(Date.class), Mockito.anyLong());
        inOrder.verify(clusterDaoJdbc).setClusterAdministrationStatus(Mockito.any(ClusterAdministrationStatus.class), Mockito.anyLong());
    }

    @Test
    @PrepareForTest(AutonomicClusterManagementService.class)
    public void removeClusterStuckProcessingTestListOfClustersNull() throws Exception {//TODO
        Date lastAdministrated = getLastAdministrationDate();
        setRemoveClusterStuckProcessing(lastAdministrated, null, ClusterAdministrationStatus.InProgress);
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(lastAdministrated);

        autonomicClusterManagementService.removeClusterStuckProcessing();
        verifyRemoveClusterStuckProcessingCalledMethods(0, 0, 0);
    }

    @Test
    @PrepareForTest(AutonomicClusterManagementService.class)
    public void removeClusterStuckProcessingTestClusterAdministrationStatusDone() throws Exception {//TODO
        List<ClusterVO> allClusters = new ArrayList<>();
        ClusterVO cluster = new ClusterVO(0l);
        allClusters.add(cluster);
        setRemoveClusterStuckProcessing(new Date(), allClusters, ClusterAdministrationStatus.Done);

        autonomicClusterManagementService.removeClusterStuckProcessing();

        verifyRemoveClusterStuckProcessingCalledMethods(1, 0, 0);
    }

    @Test
    @PrepareForTest(AutonomicClusterManagementService.class)
    public void removeClusterStuckProcessingTestSixHoursInProgress() throws Exception {//TODO
        List<ClusterVO> allClusters = createClusters();
        Date lastAdministrated = getLastAdministrationDate();
        Date current = setCurrentDate(lastAdministrated, 7);
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(current);
        setRemoveClusterStuckProcessing(lastAdministrated, allClusters, ClusterAdministrationStatus.InProgress);

        autonomicClusterManagementService.removeClusterStuckProcessing();

        verifyRemoveClusterStuckProcessingCalledMethods(1, 1, 1);
    }

    @Test
    @PrepareForTest(AutonomicClusterManagementService.class)
    public void removeClusterStuckProcessingTestLessThanSixHoursInProgress() throws Exception {//TODO
        List<ClusterVO> allClusters = createClusters();
        Date lastAdministrated = getLastAdministrationDate();
        Date current = setCurrentDate(lastAdministrated, 5);
        PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(current);
        setRemoveClusterStuckProcessing(lastAdministrated, allClusters, ClusterAdministrationStatus.InProgress);

        autonomicClusterManagementService.removeClusterStuckProcessing();

        verifyRemoveClusterStuckProcessingCalledMethods(1, 1, 0);
    }

    private Date setCurrentDate(Date lastAdministrated, int hoursToAdd) {
        Date current = new Date(lastAdministrated.getTime());
        Calendar c = Calendar.getInstance();
        c.setTime(current);
        c.add(Calendar.HOUR_OF_DAY, hoursToAdd);
        current = c.getTime();
        return current;
    }

    private void verifyRemoveClusterStuckProcessingCalledMethods(int getClusterAdministrationStatusTimes, int getClusterLastAdminstration,
            int markAdministrationStatusInClusterAsDone) {
        Mockito.verify(clusterDao).listAll();
        InOrder inOrderClusterDaoJdbc = Mockito.inOrder(clusterDaoJdbc);
        inOrderClusterDaoJdbc.verify(clusterDaoJdbc, Mockito.times(getClusterAdministrationStatusTimes)).getClusterAdministrationStatus(Mockito.anyLong());
        inOrderClusterDaoJdbc.verify(clusterDaoJdbc, Mockito.times(getClusterLastAdminstration)).getClusterLastAdminstration(Mockito.anyLong());
        Mockito.verify(autonomicClusterManagementService, Mockito.times(markAdministrationStatusInClusterAsDone)).markAdministrationStatusInClusterAsDone(Mockito.anyLong());
    }

    private void setRemoveClusterStuckProcessing(Date lastAdministrated, List<ClusterVO> allClusters, ClusterAdministrationStatus clusterAdministrationStatus) {
        Mockito.doReturn(allClusters).when(clusterDao).listAll();
        Mockito.doReturn(clusterAdministrationStatus).when(clusterDaoJdbc).getClusterAdministrationStatus(Mockito.anyLong());
        Mockito.doReturn(lastAdministrated).when(clusterDaoJdbc).getClusterLastAdminstration(Mockito.anyLong());
        Mockito.doNothing().when(autonomicClusterManagementService).markAdministrationStatusInClusterAsDone(Mockito.anyLong());
    }

    private void setCanProcessCluster(ClusterAdministrationHeuristicAlgorithm algorithm, Date date, int secondsToAdd, int intervalBetweenManagement) throws Exception {
        Calendar past = getCalendarInstance();
        Calendar current = getCalendarInstance();
        current.add(Calendar.SECOND, secondsToAdd);

        PowerMockito.mockStatic(Calendar.class);
        PowerMockito.when(Calendar.getInstance()).thenReturn(past, current);

        Mockito.doReturn(date).when(clusterDaoJdbc).getClusterLastAdminstration(Mockito.anyLong());
        Mockito.doReturn(intervalBetweenManagement).when(algorithm).getClusterIntervalBetweenConsolidation();
    }

    private Calendar getCalendarInstance() {
        Calendar past = Calendar.getInstance();
        past.set(2016, 4, 4, 1, 10, 1);
        past.set(Calendar.MILLISECOND, 0);
        return past;
    }

    private void verifyCanProcessClusterCalledMethods(ClusterAdministrationHeuristicAlgorithm algorithm, int times) {
        Mockito.verify(clusterDaoJdbc).getClusterLastAdminstration(Mockito.anyLong());
        Mockito.verify(algorithm, Mockito.times(times)).getClusterIntervalBetweenConsolidation();
    }

    private Date getLastAdministrationDate() {
        Date lastAdministrationdate = getCalendarInstance().getTime();
        return lastAdministrationdate;
    }

    private List<ClusterVO> createClusters() {
        List<ClusterVO> allClusters = new ArrayList<>();
        allClusters.add(new ClusterVO(0l));
        return allClusters;
    }
}