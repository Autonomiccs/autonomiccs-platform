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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.utils.exception.CloudRuntimeException;

import br.com.autonomiccs.autonomic.plugin.common.daos.AutonomiccsSystemVmTemplateJdbcDao;

/**
 * This class is responsible to manage the Autonomiccs System VMs templates
 */
@Service
public class AutonomiccsSystemVmTemplateService implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected AutonomiccsSystemVmTemplateJdbcDao autonomiccsSystemVmTemplateJdbcDao;

    @Autowired
    private VMTemplateDao templateDao;

    /**
     * the base URL used to construct the link from which we download the Autonomiccs system VM template
     */
    @Autowired
    @Qualifier("autonomiccsSystemVmsTemplateUrlBase")
    protected String autonomiccsSystemVmsTemplateUrlBase;

    /**
     * Version of the system VM that is being installed.
     */
    @Autowired
    @Qualifier("systemVmTemplateVersion")
    protected String systemVmTemplateVersion;

    /**
     * Display text used in the System VM.
     */
    protected String autonomiccsSystemVmTemplateDisplayText;

    /**
     * The Autonomiccs system VM template unique name identifier.
     */
    protected String autonomiccsSystemVmTemplateName;

    /**
     * Checks if the template is registered and active in the database.
     * @return true if the template is already registered
     */
    protected boolean isTemplateRegistered(String templateName) {
        return autonomiccsSystemVmTemplateJdbcDao.isTemplateRegistered(templateName);
    }

    /**
     * Checks is there is a template registered for a given hypervisor
     * @return boolean indicating if there is a template registered for a given hypervisor
     */
    public boolean isTemplateRegisteredForHypervisor(HypervisorType hypervisorType) {
        return isTemplateRegistered(getAutonomiccsSystemVmTemplateName(hypervisorType));
    }

    public String getAutonomiccsSystemVmTemplateDisplayText(HypervisorType hypervisor) {
        return String.format("%s - %s", autonomiccsSystemVmTemplateDisplayText, hypervisor.name().toLowerCase());
    }

    public String getAutonomiccsSystemVmTemplateName(HypervisorType hypervisor) {
        return String.format("%s-%s", autonomiccsSystemVmTemplateName, hypervisor.name().toLowerCase());
    }

    /**
     * It constructs the System VMs template URL for download.
     * The pattern of the URL is: {@link #autonomiccsSystemVmsTemplateUrlBase}/{@link #getAutonomiccsSystemVmTemplateName(HypervisorType)}.hypervisorSupportedImageFormat
     * If we cannot determine a supported image format for the given hypervisor a {@link CloudRuntimeException} will be thrown.
     * @return SystemVms template URL
     */
    protected String constructSystemVmTemplateUrl(HypervisorType hypervisorType) {
        ImageFormat supportedImageFormat = HypervisorType.getSupportedImageFormat(hypervisorType);
        if (supportedImageFormat == null) {
            throw new CloudRuntimeException(String.format("Could not find a supported image format for hypervisor [%s]", hypervisorType));
        }
        return String.format("%s/%s.%s", autonomiccsSystemVmsTemplateUrlBase, getAutonomiccsSystemVmTemplateName(hypervisorType),
                supportedImageFormat.getFileExtension());
    }

    /**
     * Retrieves the URL to download the System VM template for the given hypervisor type.
     * If the hypervisor type is not supported a {@link RuntimeException} will be thrown.
     * @return URL to download the system VM
     */
    public String getSystemVmTemplateUrl(HypervisorType hypervisor) {
        switch (hypervisor) {
        case XenServer:
        case KVM:
            return constructSystemVmTemplateUrl(hypervisor);
        default:
            throw new CloudRuntimeException(String.format("We do not have a Autonomiccs System VM template for the given Hypervisor [%s] .", hypervisor));
        }
    }

    /**
     * It looks for the {@link VMTemplateVO} that represents the template of the system VM for the given hypervisor
     * @return {@link VMTemplateVO} that represents the Autonomiccs system VM for the given hypervisor
     */
    public VMTemplateVO findAutonomiccsSystemVmTemplate(HypervisorType hypervisor) {
        String autonomiccsSystemVmTemplateNameString = getAutonomiccsSystemVmTemplateName(hypervisor);
        long templateId = autonomiccsSystemVmTemplateJdbcDao.searchAutonomiccsSystemVmTemplateIdForHypervisor(autonomiccsSystemVmTemplateNameString);
        return templateDao.findById(templateId);
    }

    @Override
    /**
     * This method is executed by Spring framework.
     * It configures the template's display text and name
     */
    public void afterPropertiesSet() throws Exception {
        logger.info("Autonomiccs system VMs template service initialized.");

        autonomiccsSystemVmTemplateDisplayText = String.format("Autonomiccs System VM template (%s)", systemVmTemplateVersion);
        logger.debug(String.format("Autonomiccs system VMs template display text configured [%s]", autonomiccsSystemVmTemplateDisplayText));

        autonomiccsSystemVmTemplateName = String.format("autonomiccs-systemVm-%s", systemVmTemplateVersion);
        logger.debug(String.format("Autonomiccs system VMs template name configured [%s]", systemVmTemplateVersion));
    }

    public boolean isTemplateRegisteredAndReadyForHypervisor(HypervisorType hypervisorType) {
        return autonomiccsSystemVmTemplateJdbcDao.isTemplateRegisteredAndReady(getAutonomiccsSystemVmTemplateName(hypervisorType));
    }

}
