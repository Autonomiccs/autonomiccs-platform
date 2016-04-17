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
package br.com.autonomiccs.autonomic.plugin.common.daos;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import br.com.autonomiccs.autonomic.plugin.common.enums.SystemVmType;

/**
 * This class is intended to deal with operations regarding the Autonomiccs system VMs.
 */
public class AutonomiccsSystemVmJdbcDao extends JdbcDaoSupport {

    private String sqlGetStartHostServiceVmIdFromPod = "select id from vm_instance where removed is null and pod_id  = ? and account_id = 1 and instance_name like ? ";

    public Long getStartHostServiceVmIdFromPod(Long podId, SystemVmType systemVmType) {
        try {
            return getJdbcTemplate().queryForObject(sqlGetStartHostServiceVmIdFromPod, Long.class, podId, systemVmType.getNamePrefix() + "%");
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

}
