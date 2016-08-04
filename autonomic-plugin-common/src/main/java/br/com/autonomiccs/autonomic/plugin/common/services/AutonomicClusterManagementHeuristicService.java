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

import java.util.HashMap;
import java.util.Map;

import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.autonomiccs.autonomic.administration.algorithms.ClusterAdministrationHeuristicAlgorithm;
import br.com.autonomiccs.autonomic.administration.algorithms.impl.ClusterManagementDummyAlgorithm;

/**
 * This class manages the instantiation of cluster administration heuristics; those classes
 * implement the interface {@link ClusterAdministrationHeuristicAlgorithm}.
 */
@Service("autonomicClusterManagementHeuristicService")
public class AutonomicClusterManagementHeuristicService {

    public final static String CLUSTER_ADMINISTRATION_ALGORITHMS_IN_CONFIGURATION_KEY = "autonomiccs.clustermanager.algorithm";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Map<String, Class<? extends ClusterAdministrationHeuristicAlgorithm>> algorithmsMap = new HashMap<>();

    @Autowired
    protected ConfigurationDao configurationDao;

    /**
     * Returns the instance of the Administration algorithm configured by the system administrator;
     * if it can not find the configured heuristic it returns the
     * {@link ClusterManagementDummyAlgorithm}.
     */
    public ClusterAdministrationHeuristicAlgorithm getAdministrationAlgorithm() {
        String algorithmName = configurationDao.getValue(CLUSTER_ADMINISTRATION_ALGORITHMS_IN_CONFIGURATION_KEY);
        if (StringUtils.isBlank(algorithmName)) {
            return getDummyClusterManagementHeuristc();
        }
        return getInstanceOfClass(algorithmName);
    }

    /**
     * This method tries to instantiate a object with the given full qualified class name.
     */
    protected ClusterAdministrationHeuristicAlgorithm getInstanceOfClass(String algorithmName) {
        Class<? extends ClusterAdministrationHeuristicAlgorithm> clusterManagerHeuristicAlgorithmClass = algorithmsMap.get(algorithmName);
        if (clusterManagerHeuristicAlgorithmClass != null) {
            return getInstanceOfClass(clusterManagerHeuristicAlgorithmClass);
        }
        try {
            Class<? extends ClusterAdministrationHeuristicAlgorithm> algorithmClass = loadAlgorithmClass(algorithmName);
            return getInstanceOfClass(algorithmClass);
        } catch (Exception e) {
            logger.debug(String.format("Could not load heuristics from algorithm [algorithm class name=%s], using the Dummy management algorithm", algorithmName), e);
            return getDummyClusterManagementHeuristc();
        }
    }

    /**
     * It returns the algorithm class with the given class name; it also adds the class object into
     * the map of algorithms ({@link AutonomicClusterManagementHeuristicService#algorithmsMap}).
     */
    @SuppressWarnings("unchecked")
    protected Class<? extends ClusterAdministrationHeuristicAlgorithm> loadAlgorithmClass(String algorithmName) throws ClassNotFoundException {
        logger.info(String.format("Loading heuristics from algorithm [algorithm class name=%s]", algorithmName));
        Class<? extends ClusterAdministrationHeuristicAlgorithm> algorithmClass = (Class<? extends ClusterAdministrationHeuristicAlgorithm>)Class.forName(algorithmName.trim());
        algorithmsMap.put(algorithmName, algorithmClass);
        return algorithmClass;
    }

    /**
     * It creates an instance of the given algorithm class; if the instantiation throws an exception
     * (InstantiationException or IllegalAccessException), it returns an instance of
     * {@link ClusterManagementDummyAlgorithm}.
     */
    protected ClusterAdministrationHeuristicAlgorithm getInstanceOfClass(Class<? extends ClusterAdministrationHeuristicAlgorithm> clusterManagerHeuristicAlgorithmClass) {
        try {
            return clusterManagerHeuristicAlgorithmClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Error while instantiating object, using the dummy algorithm.", e);
            return getDummyClusterManagementHeuristc();
        }
    }

    /**
     * It creates and returns one instance of the class {@link ClusterManagementDummyAlgorithm}.
     */
    protected ClusterAdministrationHeuristicAlgorithm getDummyClusterManagementHeuristc() {
        return getInstanceOfClass(ClusterManagementDummyAlgorithm.class);
    }
}
