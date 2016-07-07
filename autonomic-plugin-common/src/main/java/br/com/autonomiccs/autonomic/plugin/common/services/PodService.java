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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.autonomiccs.autonomic.plugin.common.daos.HostJdbcDao;

import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.HostPodDao;

/**
 * This class is used to manage Pod object into the database.
 */
@Service
public class PodService {

    @Autowired
    private HostPodDao hostPodDao;

    @Autowired
    private HostJdbcDao hostJdbcDao;

    /**
     * List all Pods from a given zone.
     * @return {@link List<HostPodVo>} all pods of the given zone.
     */
    public List<HostPodVO> getAllPodsEnabledFromZone(long zoneId) {
        return hostPodDao.listByDataCenterId(zoneId);
    }

    /**
     * It returns a Pod ({@link HostPodVO}) with the given id.
     */
    public HostPodVO findPodById(Long podId) {
        return hostPodDao.findById(podId);
    }

    /**
     * It returns true if there is at least one host deactivated by the Autonomiccs platform at the
     * pod with the given id.
     */
    public boolean isThereAnyHostOnPodDeactivatedByOurManager(long id) {
        return hostJdbcDao.isThereAnyHostOnPodDeactivatedByOurManager(id);
    }

}
