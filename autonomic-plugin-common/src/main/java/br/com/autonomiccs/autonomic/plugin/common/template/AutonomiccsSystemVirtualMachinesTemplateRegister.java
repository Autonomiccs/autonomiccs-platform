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
package br.com.autonomiccs.autonomic.plugin.common.template;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cloudstack.api.command.user.template.RegisterTemplateCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cloud.hypervisor.Hypervisor;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.template.TemplateApiService;
import com.cloud.user.AccountService;
import com.cloud.utils.exception.CloudRuntimeException;

import br.com.autonomiccs.autonomic.plugin.common.services.AutonomicClusterManagementHeuristicService;
import br.com.autonomiccs.autonomic.plugin.common.services.AutonomiccsSystemVmTemplateService;
import br.com.autonomiccs.autonomic.plugin.common.services.GuestOsService;
import br.com.autonomiccs.autonomic.plugin.common.services.HostService;
import br.com.autonomiccs.autonomic.plugin.common.utils.ReflectionUtils;

/**
 * This class is intended to manage the register of Autonomiccs' SystemVMs templates.
 * The template (or future updates to it) will be registered at boot time using the normal flow of ACS template register.
 * Additionally, this class will monitor the addition of clusters of new hypervisors and download new templates if needed.
 **/
@Component
public class AutonomiccsSystemVirtualMachinesTemplateRegister implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final long ONE_MINUTE_IN_MILLISECONDS = 60000;
    private static final int NUMBER_OF_MINUTES_BETWEEN_REGISTRATION_OF_TEMPLATES = 10;

    @Autowired
    private TemplateApiService templateService;

    @Autowired
    private GuestOsService guestOsService;

    @Autowired
    private HostService hostService;

    @Autowired
    private AutonomiccsSystemVmTemplateService autonomiccsSystemVmTemplateService;

    @Autowired
    private AutonomicClusterManagementHeuristicService autonomicManagementHeuristicService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ReflectionUtils reflectionUtils;

    /**
     * This attribute is used to indicate that the template can be used in any of the zones that exist in the Cloud environment.
     */
    private final Long allAvailableZoneMagicNumber = -1L;

    private final Long systemAccountDomainId = 1L;

    /**
     * Guest OS name of the System VM's OS.
     */
    private String guestOsName = "Debian GNU/Linux 7(64-bit)";

    /**
     * This method constantly checks if there is the need to download a Autonomiccs system template for the current hypervisors of the environment.
     * The initial delay is configure for 1 minute after the application startup.
     * The interval between the execution of the {@link #registerTemplatesIfNeeded()}, that time is controlled by {@link #NUMBER_OF_MINUTES_BETWEEN_REGISTRATION_OF_TEMPLATES}
     */
    @Scheduled(initialDelay = ONE_MINUTE_IN_MILLISECONDS, fixedDelay = ONE_MINUTE_IN_MILLISECONDS * NUMBER_OF_MINUTES_BETWEEN_REGISTRATION_OF_TEMPLATES)
    public void registerTemplatesIfNeeded() {
        if (!autonomicManagementHeuristicService.getAdministrationAlgorithm().canHeuristicShutdownHosts() && !hostService.isThereAnyHostOnCloudDeactivatedByOurManager()) {
            return;
        }
        List<HypervisorType> allHypervisorsTypeInCloud = hostService.getAllHypervisorsTypeInCloud();
        for (HypervisorType hypervisorType : allHypervisorsTypeInCloud) {
            if (!autonomiccsSystemVmTemplateService.isTemplateRegisteredForHypervisor(hypervisorType)) {
                RegisterTemplateCmd templateCommandForHypervisor = createRegisterTemplateCommandForHypervisor(hypervisorType);
                try {
                    templateService.registerTemplate(templateCommandForHypervisor);
                } catch (Exception e) {
                    logger.error("Error while registering a Autonomiccs system vm. ", e);
                }
            }
        }
    }

    /**
     * It creates and returns a {@link RegisterTemplateCmd} that aims to register the Autonomiccs SystemVms Template.
     * It will always register the template for all available zones, setting the {@link RegisterTemplateCmd#getZoneId()} attribute to {@link #allAvailableZoneMagicNumber}.
     * @return {@link RegisterTemplateCmd} to be used to register Autonomiccs system VMs template
     */
    private RegisterTemplateCmd createRegisterTemplateCommandForHypervisor(HypervisorType hypervisor) {
        RegisterTemplateCmd registerTemplateCmd = new RegisterTemplateCmd();
        reflectionUtils.setFieldIntoObject(registerTemplateCmd, "displayText", autonomiccsSystemVmTemplateService.getAutonomiccsSystemVmTemplateDisplayText(hypervisor));
        reflectionUtils.setFieldIntoObject(registerTemplateCmd, "format", getSupportedImageFormat(hypervisor));
        reflectionUtils.setFieldIntoObject(registerTemplateCmd, "hypervisor", hypervisor.name());
        reflectionUtils.setFieldIntoObject(registerTemplateCmd, "templateName", autonomiccsSystemVmTemplateService.getAutonomiccsSystemVmTemplateName(hypervisor));
        reflectionUtils.setFieldIntoObject(registerTemplateCmd, "osTypeId", guestOsService.getGuestOsUuid(guestOsName));
        reflectionUtils.setFieldIntoObject(registerTemplateCmd, "zoneId", allAvailableZoneMagicNumber);
        reflectionUtils.setFieldIntoObject(registerTemplateCmd, "url", autonomiccsSystemVmTemplateService.getSystemVmTemplateUrl(hypervisor));
        reflectionUtils.setFieldIntoObject(registerTemplateCmd, "accountName", "system");
        reflectionUtils.setFieldIntoObject(registerTemplateCmd, "domainId", systemAccountDomainId);
        reflectionUtils.setFieldIntoObject(registerTemplateCmd, "projectId", null);
        reflectionUtils.setFieldIntoObject(registerTemplateCmd, "requiresHvm", false);
        Map<Object, Object> details = createTemplateDetails();
        reflectionUtils.setFieldIntoObject(registerTemplateCmd, "details", details);
        reflectionUtils.setFieldIntoObject(registerTemplateCmd, "_accountService", accountService);
        return registerTemplateCmd;

    }

    private Map<Object, Object> createTemplateDetails() {
        Map<Object, Object> details = new HashMap<>();
        Map<Object, Object> innerMap = new HashMap<>();
        innerMap.put("hypervisortoolsversion", "xenserver56");
        details.put(0, innerMap);
        return details;
    }

    /**
     * It returns the supported image format for the given hypervisor.
     * It is used the method {@link Hypervisor.HypervisorType#getSupportedImageFormat(HypervisorType)} to retrieve the supported image format.
     *
     * @return Supported Image Format
     */
    private String getSupportedImageFormat(HypervisorType hypervisor) {
        ImageFormat supportedImageFormat = Hypervisor.HypervisorType.getSupportedImageFormat(hypervisor);
        if (supportedImageFormat != null) {
            return supportedImageFormat.name();
        }
        throw new CloudRuntimeException(String.format("Could not find a supported image format for hypervisor [%s]", hypervisor));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Autonomiccs system VMs templates register initialized.");
    }
}
