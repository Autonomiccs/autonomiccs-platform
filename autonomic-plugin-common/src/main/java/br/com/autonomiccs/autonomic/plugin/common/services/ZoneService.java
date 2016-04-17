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
package br.com.autonomiccs.autonomic.plugin.common.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;

/**
 * This class is intended to manage Zones configurations.
 */
@Service
public class ZoneService {

    @Autowired
    private DataCenterDao dataCenterDao;

    /**
     * It will list all of the enabled zones of the cloud environment.
     *
     * @return List<DataCenterVO> that represents of of the enabled zones in the cloud
     */
    public List<DataCenterVO> listAllZonesEnabled() {
        return dataCenterDao.listEnabledZones();
    }
}
