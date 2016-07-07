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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cloudstack.api.command.user.template.RegisterTemplateCmd;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.cloud.exception.ResourceAllocationException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.template.TemplateApiService;
import com.cloud.user.AccountService;
import com.cloud.utils.exception.CloudRuntimeException;

import br.com.autonomiccs.autonomic.administration.algorithms.ClusterAdministrationHeuristicAlgorithm;
import br.com.autonomiccs.autonomic.administration.algorithms.impl.ClusterManagementDummyAlgorithm;
import br.com.autonomiccs.autonomic.administration.algorithms.impl.ConsolidationAlgorithmBase;
import br.com.autonomiccs.autonomic.plugin.common.services.AutonomicClusterManagementHeuristicService;
import br.com.autonomiccs.autonomic.plugin.common.services.AutonomiccsSystemVmTemplateService;
import br.com.autonomiccs.autonomic.plugin.common.services.GuestOsService;
import br.com.autonomiccs.autonomic.plugin.common.services.HostService;
import br.com.autonomiccs.autonomic.plugin.common.utils.ReflectionUtils;

@RunWith(MockitoJUnitRunner.class)
public class AutonomiccsSystemVirtualMachinesTemplateRegisterTest {

    @Spy
    @InjectMocks
    private AutonomiccsSystemVirtualMachinesTemplateRegister spy;
    @Mock
    private TemplateApiService templateService;
    @Mock
    private GuestOsService guestOsService;
    @Mock
    private HostService hostService;
    @Mock
    private AutonomiccsSystemVmTemplateService autonomiccsSystemVmTemplateService;
    @Mock
    private AutonomicClusterManagementHeuristicService autonomicManagementHeuristicService;
    @Mock
    private AccountService accountService;
    @Mock
    private ReflectionUtils reflectionUtils;

    private final Long allAvailableZoneMagicNumber = -1L;
    private final Long systemAccountDomainId = 1L;
    private String guestOsName = "Debian GNU/Linux 7(64-bit)";

    @Test
    public void registerTemplatesIfNeededTestHeuristicCannotShutDownHost() throws Exception {
        configureWithOneHypervisorType(new ClusterManagementDummyAlgorithm(), true, false);

        spy.registerTemplatesIfNeeded();

        verifyRegisterTemplatesIfNeededCalledMethods(1, 1, 1, 1);
    }

    @Test
    public void registerTemplatesIfNeededTestHeuristicCannotShutDownHostAndNoDeactivatedHost() throws Exception {
        configureWithOneHypervisorType(new ClusterManagementDummyAlgorithm(), false, false);

        spy.registerTemplatesIfNeeded();

        verifyRegisterTemplatesIfNeededCalledMethods(1, 0, 0, 0);
    }

    @Test
    public void registerTemplatesIfNeededTestHeuristicCanShutDownHostAndNoDeactivatedHost() throws Exception {
        configureWithOneHypervisorType(new ConsolidationAlgorithmBase(), false, false);

        spy.registerTemplatesIfNeeded();

        verifyRegisterTemplatesIfNeededCalledMethods(0, 1, 1, 1);
    }

    @Test
    public void registerTemplatesIfNeededTestIsTemplateRegisteredForHypervisorTrue() throws Exception {
        configureWithOneHypervisorType(new ConsolidationAlgorithmBase(), false, true);

        spy.registerTemplatesIfNeeded();

        verifyRegisterTemplatesIfNeededCalledMethods(0, 1, 1, 0);
    }

    @Test
    public void registerTemplatesIfNeededTestIsTemplateRegisteredForHypervisorFalse() throws Exception {
        configureWithOneHypervisorType(new ConsolidationAlgorithmBase(), true, false);

        spy.registerTemplatesIfNeeded();

        verifyRegisterTemplatesIfNeededCalledMethods(0, 1, 1, 1);
    }

    @Test
    public void registerTemplatesIfNeededTestHeuristicCanShutDownHostEmptyHypervisorList() throws Exception {
        configureRegisterTemplatesIfNeededTest(new ConsolidationAlgorithmBase(), true, new ArrayList<HypervisorType>(), false);

        spy.registerTemplatesIfNeeded();

        verifyRegisterTemplatesIfNeededCalledMethods(0, 1, 0, 0);
    }

    @Test
    public void registerTemplatesIfNeededTestCatchException() throws Exception {
        configureWithOneHypervisorType(new ConsolidationAlgorithmBase(), true, false);
        Mockito.doThrow(Exception.class).when(templateService).registerTemplate(Mockito.any(RegisterTemplateCmd.class));
        spy.logger = Mockito.mock(Logger.class);

        spy.registerTemplatesIfNeeded();

        verifyRegisterTemplatesIfNeededCalledMethods(0, 1, 1, 1);
        String exceptionMessage = "Error while registering a Autonomiccs system vm. ";
        Mockito.verify(spy.logger).error(Mockito.eq(exceptionMessage), Mockito.any(Exception.class));
    }

    @Test
    public void createRegisterTemplateCommandForHypervisorTest() {
        Map<Object, Object> details = new HashMap<>();
        Mockito.doReturn("return").when(autonomiccsSystemVmTemplateService).getAutonomiccsSystemVmTemplateDisplayText(Mockito.any(HypervisorType.class));
        Mockito.doNothing().when(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("displayText"), Mockito.anyString());
        Mockito.doReturn("return").when(spy).getSupportedImageFormat(Mockito.any(HypervisorType.class));
        Mockito.doNothing().when(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("format"), Mockito.anyString());
        Mockito.doNothing().when(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("hypervisor"), Mockito.eq("Any"));
        Mockito.doReturn("return").when(autonomiccsSystemVmTemplateService).getAutonomiccsSystemVmTemplateName(Mockito.any(HypervisorType.class));
        Mockito.doNothing().when(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("templateName"), Mockito.anyString());
        Mockito.doReturn(0l).when(guestOsService).getGuestOsUuid(Mockito.anyString());
        Mockito.doNothing().when(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("osTypeId"), Mockito.anyLong());
        Mockito.doNothing().when(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("zoneId"), Mockito.eq(allAvailableZoneMagicNumber));
        Mockito.doReturn("return").when(autonomiccsSystemVmTemplateService).getSystemVmTemplateUrl(Mockito.any(HypervisorType.class));
        Mockito.doNothing().when(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("url"), Mockito.anyString());
        Mockito.doNothing().when(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("accountName"), Mockito.eq("system"));
        Mockito.doNothing().when(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("domainId"), Mockito.eq(systemAccountDomainId));
        Mockito.doNothing().when(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("projectId"), Mockito.eq(null));
        Mockito.doNothing().when(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("requiresHvm"), Mockito.eq(false));
        Mockito.doReturn(details).when(spy).createTemplateDetails();
        Mockito.doNothing().when(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("details"), Matchers.<Map<Object, Object>> any());
        Mockito.doNothing().when(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("_accountService"), Mockito.eq(accountService));

        spy.createRegisterTemplateCommandForHypervisor(HypervisorType.Any);

        Mockito.verify(autonomiccsSystemVmTemplateService).getAutonomiccsSystemVmTemplateDisplayText(Mockito.any(HypervisorType.class));
        Mockito.verify(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("displayText"), Mockito.anyString());
        Mockito.verify(spy).getSupportedImageFormat(Mockito.any(HypervisorType.class));
        Mockito.verify(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("format"), Mockito.anyString());
        Mockito.verify(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("hypervisor"), Mockito.eq("Any"));
        Mockito.verify(autonomiccsSystemVmTemplateService).getAutonomiccsSystemVmTemplateName(Mockito.any(HypervisorType.class));
        Mockito.verify(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("templateName"), Mockito.anyString());
        Mockito.verify(guestOsService).getGuestOsUuid(Mockito.eq(guestOsName));
        Mockito.verify(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("osTypeId"), Mockito.anyLong());
        Mockito.verify(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("zoneId"), Mockito.eq(allAvailableZoneMagicNumber));
        Mockito.verify(autonomiccsSystemVmTemplateService).getSystemVmTemplateUrl(Mockito.any(HypervisorType.class));
        Mockito.verify(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("url"), Mockito.anyString());
        Mockito.verify(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("accountName"), Mockito.eq("system"));
        Mockito.verify(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("domainId"), Mockito.eq(systemAccountDomainId));
        Mockito.verify(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("projectId"), Mockito.eq(null));
        Mockito.verify(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("requiresHvm"), Mockito.eq(false));
        Mockito.verify(spy).createTemplateDetails();
        Mockito.verify(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("details"), Matchers.<Map<Object, Object>> any());
        Mockito.verify(reflectionUtils).setFieldIntoObject(Mockito.any(RegisterTemplateCmd.class), Mockito.eq("_accountService"), Mockito.eq(accountService));
    }

    @Test
    public void createTemplateDetailsTest() {
        Map<Object, Object> details = new HashMap<>();
        Map<Object, Object> innerMap = new HashMap<>();
        innerMap.put("hypervisortoolsversion", "xenserver56");
        details.put(0, innerMap);

        Map<Object, Object> result = spy.createTemplateDetails();

        Assert.assertEquals(details.get(0).toString(), result.get(0).toString());
    }

    @Test(expected = CloudRuntimeException.class)
    public void getSupportedImageFormatTestExpectCloudRuntimeException() {
        spy.getSupportedImageFormat(HypervisorType.Any);
    }

    @Test
    public void getSupportedImageFormatTest() {
        String result = spy.getSupportedImageFormat(HypervisorType.XenServer);
        Assert.assertEquals("VHD", result);
    }

    private void configureRegisterTemplatesIfNeededTest(ClusterAdministrationHeuristicAlgorithm algorithm, boolean existDeactivatedHost,
            List<HypervisorType> allHypervisorsTypeInCloud, boolean isTemplateRegistered) throws Exception {
        Mockito.doReturn(algorithm).when(autonomicManagementHeuristicService).getAdministrationAlgorithm();
        Mockito.doReturn(existDeactivatedHost).when(hostService).isThereAnyHostOnCloudDeactivatedByOurManager();
        Mockito.doReturn(allHypervisorsTypeInCloud).when(hostService).getAllHypervisorsTypeInCloud();
        Mockito.doReturn(isTemplateRegistered).when(autonomiccsSystemVmTemplateService).isTemplateRegisteredForHypervisor(Mockito.any(HypervisorType.class));
        Mockito.doReturn(new RegisterTemplateCmd()).when(spy).createRegisterTemplateCommandForHypervisor(Mockito.any(HypervisorType.class));
        Mockito.doReturn(null).when(templateService).registerTemplate(Mockito.any(RegisterTemplateCmd.class));
    }

    private void verifyRegisterTemplatesIfNeededCalledMethods(int isThereAnyHostOnCloudDeactivateTimes, int getAllHypervisorsTimes, int isTemplateRegisteredTimes,
            int createRegisterTemplateTimes) throws Exception {
        Mockito.verify(autonomicManagementHeuristicService).getAdministrationAlgorithm();
        Mockito.verify(hostService, Mockito.times(isThereAnyHostOnCloudDeactivateTimes)).isThereAnyHostOnCloudDeactivatedByOurManager();
        Mockito.verify(hostService, Mockito.times(getAllHypervisorsTimes)).getAllHypervisorsTypeInCloud();
        Mockito.verify(autonomiccsSystemVmTemplateService, Mockito.times(isTemplateRegisteredTimes)).isTemplateRegisteredForHypervisor(Mockito.any(HypervisorType.class));
        Mockito.verify(spy, Mockito.times(createRegisterTemplateTimes)).createRegisterTemplateCommandForHypervisor(Mockito.any(HypervisorType.class));
        Mockito.verify(templateService, Mockito.times(createRegisterTemplateTimes)).registerTemplate(Mockito.any(RegisterTemplateCmd.class));
    }

    private void configureWithOneHypervisorType(ClusterAdministrationHeuristicAlgorithm algorithm, boolean existDeactivatedHost, boolean isTemplateRegistered)
            throws Exception, URISyntaxException, ResourceAllocationException {
        List<HypervisorType> allHypervisorsTypeInCloud = new ArrayList<>();
        allHypervisorsTypeInCloud.add(HypervisorType.Any);
        configureRegisterTemplatesIfNeededTest(algorithm, existDeactivatedHost, allHypervisorsTypeInCloud, isTemplateRegistered);
    }

}
