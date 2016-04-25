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
package br.com.autonomiccs.autonomic.plugin.common.daos;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.cloud.utils.exception.CloudRuntimeException;

public class AutonomiccsSystemVmTemplateJdbcDao extends JdbcDaoSupport {

    private String sqlIsTemplateRegistered = "select count(id) from vm_template where removed is null and state <> 'Inactive' and name = ?";

    private String sqlIsTemplateRegisteredAndReady = "select count(id) from template_view where state = 'READY' and removed is null and name = ?";

    private String sqlSearchAutonomiccsSystemVmTemplateIdForHypervisor = "select id from template_view where state = 'READY' and removed is null and name = ?";

    /**
     * Checks if the template name is registered into the database.
     * @return true if the template name is found in our database.
     */
    public boolean isTemplateRegistered(String templateName) {
        return executeTemplateQueryAndRetrieveBoolean(templateName, sqlIsTemplateRegistered);
    }

    private boolean executeTemplateQueryAndRetrieveBoolean(String templateName, String sql) {
        Integer numberOfRegister = getJdbcTemplate().queryForObject(sql, new Object[] {templateName}, Integer.class);
        if (numberOfRegister > 1) {
            throw new CloudRuntimeException(String.format("More than one template with name [%s]", templateName));
        }
        return BooleanUtils.toBoolean(numberOfRegister);
    }

    public boolean isTemplateRegisteredAndReady(String autonomiccsSystemVmTemplateName) {
        return executeTemplateQueryAndRetrieveBoolean(autonomiccsSystemVmTemplateName, sqlIsTemplateRegisteredAndReady);
    }

    public long searchAutonomiccsSystemVmTemplateIdForHypervisor(String autonomiccsSystemVmTemplateName) {
        return getJdbcTemplate().queryForObject(sqlSearchAutonomiccsSystemVmTemplateIdForHypervisor, Long.class, autonomiccsSystemVmTemplateName);
    }
}
