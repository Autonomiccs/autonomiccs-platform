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
package br.com.autonomiccs.autonomic.plugin.common.guru;

import java.util.ArrayList;
import java.util.List;

import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cloud.agent.manager.Commands;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.Pod;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Networks.TrafficType;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.VirtualMachineProfileImpl;

import br.com.autonomiccs.autonomic.plugin.common.beans.AutonomiccsSystemVm;
import br.com.autonomiccs.autonomic.plugin.common.daos.AutonomiccsSystemVmDao;

@RunWith(PowerMockRunner.class)
public class AutonomiccsSystemVmsGuruTest {

    private AutonomiccsSystemVmsGuru spy;
    @Mock
    private VirtualMachineManager virtualMachineManager;
    @Mock
    private AutonomiccsSystemVmDao autonomiccsSystemVmDao;
    @Mock
    private ConfigurationDao configurationDao;
    @Mock
    private DataCenterDao datacenterDao;

    private NicProfile nic;
    private Pod pod;
    private DeployDestination dest;

    @Before
    public void setup() {
        spy = Mockito.spy(new AutonomiccsSystemVmsGuru());
        spy.virtualMachineManager = virtualMachineManager;
        spy.autonomiccsSystemVmDao = autonomiccsSystemVmDao;
        spy.configurationDao = configurationDao;
        spy.datacenterDao = datacenterDao;
    }

    @Test
    public void afterPropertiesSetTest() throws Exception {
        Mockito.doNothing().when(virtualMachineManager).registerGuru(VirtualMachine.Type.Instance, spy);
        spy.afterPropertiesSet();
        Mockito.verify(virtualMachineManager).registerGuru(VirtualMachine.Type.Instance, spy);
    }

    @Test
    public void finalizeCommandsOnStartTest() {
        boolean result = spy.finalizeCommandsOnStart(Mockito.any(Commands.class), Mockito.any(VirtualMachineProfile.class));
        Assert.assertEquals(true, result);
    }

    @Test
    public void finalizeDeploymentTest() throws ResourceUnavailableException {
        ReservationContext context = Mockito.mock(ReservationContext.class);
        Commands cmds = Mockito.mock(Commands.class);
        DataCenter dc = Mockito.mock(DataCenter.class);

        VirtualMachineProfileImpl profile = Mockito.mock(VirtualMachineProfileImpl.class);
        Mockito.when(profile.getNics()).thenReturn(new ArrayList<NicProfile>());
        Mockito.when(profile.getId()).thenReturn(0l);

        AutonomiccsSystemVm autonomiccsSystemVm = Mockito.mock(AutonomiccsSystemVm.class);
        Mockito.doReturn(autonomiccsSystemVm).when(autonomiccsSystemVmDao).findById(Mockito.anyLong());

        DeployDestination dest = Mockito.mock(DeployDestination.class);
        Mockito.when(dest.getDataCenter()).thenReturn(dc);

        Mockito.doNothing().when(spy).setIpAddressOfEachNic(Mockito.any(AutonomiccsSystemVm.class), Mockito.any(DataCenter.class), Matchers.anyListOf(NicProfile.class));
        Mockito.doReturn(true).when(autonomiccsSystemVmDao).update(Mockito.anyLong(), Mockito.any(AutonomiccsSystemVm.class));

        boolean result = spy.finalizeDeployment(cmds, profile, dest, context);
        Assert.assertEquals(true, result);

        InOrder inOrder = Mockito.inOrder(autonomiccsSystemVmDao, spy);
        inOrder.verify(autonomiccsSystemVmDao).findById(Mockito.anyLong());
        inOrder.verify(spy).setIpAddressOfEachNic(Mockito.any(AutonomiccsSystemVm.class), Mockito.any(DataCenter.class), Matchers.anyListOf(NicProfile.class));
        inOrder.verify(autonomiccsSystemVmDao).update(Mockito.anyLong(), Mockito.any(AutonomiccsSystemVm.class));
    }

    @Test
    public void setEachNicIpAddressTestNetworkTypeAdvanced() {
        AutonomiccsSystemVm autonomiccsSystemVm = Mockito.mock(AutonomiccsSystemVm.class);
        DataCenter dc = Mockito.mock(DataCenter.class);
        Mockito.when(dc.getNetworkType()).thenReturn(NetworkType.Advanced);

        List<NicProfile> nics = createNicsOfManagementTrafficControlTrafficAndTheGivenTraffic(TrafficType.Public);
        configureDoNothingAtSetEachNicIpAddressTests(autonomiccsSystemVm);

        spy.setIpAddressOfEachNic(autonomiccsSystemVm, dc, nics);

        verifySetEachNicIpAddressCalledMethods(autonomiccsSystemVm, 1, 1, 1);
    }

    @Test
    public void setEachNicIpAddressTestNetworkTypeAdvancedDcIsSecurityGroupEnabled() {
        AutonomiccsSystemVm autonomiccsSystemVm = Mockito.mock(AutonomiccsSystemVm.class);
        DataCenter dc = setupDataCenter(NetworkType.Basic, true);

        List<NicProfile> nics = createNicsOfManagementTrafficControlTrafficAndTheGivenTraffic(TrafficType.Guest);

        configureDoNothingAtSetEachNicIpAddressTests(autonomiccsSystemVm);

        spy.setIpAddressOfEachNic(autonomiccsSystemVm, dc, nics);

        verifySetEachNicIpAddressCalledMethods(autonomiccsSystemVm, 1, 1, 1);
    }

    @Test
    public void setEachNicIpAddressTestNetworkTypeAdvancedDcIsSecurityGroupNotEnabled() {
        AutonomiccsSystemVm autonomiccsSystemVm = Mockito.mock(AutonomiccsSystemVm.class);
        DataCenter dc = setupDataCenter(NetworkType.Advanced, false);

        List<NicProfile> nics = createNicsOfManagementTrafficControlTrafficAndTheGivenTraffic(TrafficType.Guest);

        configureDoNothingAtSetEachNicIpAddressTests(autonomiccsSystemVm);

        spy.setIpAddressOfEachNic(autonomiccsSystemVm, dc, nics);

        verifySetEachNicIpAddressCalledMethods(autonomiccsSystemVm, 1, 1, 0);
    }

    @Test
    public void setEachNicIpAddressTestNetworkTypeBasic() {
        AutonomiccsSystemVm autonomiccsSystemVm = Mockito.mock(AutonomiccsSystemVm.class);
        DataCenter dc = setupDataCenter(NetworkType.Basic, true);

        List<NicProfile> nics = createNicsOfManagementTrafficControlTrafficAndTheGivenTraffic(TrafficType.Guest);

        configureDoNothingAtSetEachNicIpAddressTests(autonomiccsSystemVm);

        spy.setIpAddressOfEachNic(autonomiccsSystemVm, dc, nics);

        verifySetEachNicIpAddressCalledMethods(autonomiccsSystemVm, 1, 1, 1);
    }

    @Test
    public void finalizeExpungeTest() {
        VirtualMachine vm = Mockito.mock(VirtualMachine.class);
        AutonomiccsSystemVm autonomiccsSystemVm = Mockito.mock(AutonomiccsSystemVm.class);
        configureFinalizeExpungeTest(vm, autonomiccsSystemVm);

        spy.finalizeExpunge(vm);

        Mockito.verify(autonomiccsSystemVmDao).findById(Mockito.anyLong());
        InOrder inOrder = Mockito.inOrder(autonomiccsSystemVm);
        inOrder.verify(autonomiccsSystemVm).setPublicIpAddress(null);
        inOrder.verify(autonomiccsSystemVm).setPrivateMacAddress(null);
        inOrder.verify(autonomiccsSystemVm).setPrivateIpAddress(null);
        inOrder.verify(autonomiccsSystemVm).setManagementIpAddress(null);
        Mockito.verify(autonomiccsSystemVmDao).update(Mockito.anyLong(), Mockito.any(AutonomiccsSystemVm.class));
    }

    @Test
    public void finalizeExpungeTestAutonomiccsSystemVmIsNull() {
        VirtualMachine vm = Mockito.mock(VirtualMachine.class);
        configureFinalizeExpungeTest(vm, null);

        spy.finalizeExpunge(vm);

        Mockito.verify(autonomiccsSystemVmDao, Mockito.times(0)).update(Mockito.anyLong(), Mockito.any(AutonomiccsSystemVm.class));
    }

    @Test
    public void finalizeStartTest() {
        boolean result = spy.finalizeStart(Mockito.any(VirtualMachineProfile.class), Mockito.anyLong(), Mockito.any(Commands.class), Mockito.any(ReservationContext.class));
        Assert.assertEquals(true, result);
    }

    @Test
    public void configureIpAndMaskOnNicWithIpAddressNullOrNotNullTestIpAddressNotNull() {
        StringBuilder buf = new StringBuilder();
        NicProfile nic = Mockito.mock(NicProfile.class);
        Mockito.when(nic.getIPv4Address()).thenReturn("ipv4Address");
        Mockito.when(nic.getIPv4Netmask()).thenReturn("ipv4Netmask");

        spy.configureIpAndMaskOnNicWithIpAddressNullOrNotNull(buf, nic, 0);

        Assert.assertEquals(" eth0ip=ipv4Address eth0mask=ipv4Netmask", buf.toString());
    }

    @Test
    public void configureIpAndMaskOnNicWithIpAddressNullOrNotNullTestIpAddressNull() {
        spy.ipAddressInternet = "ipAddressInternet";
        spy.maskForIpAddressInternet = "maskForIpAddressInternet";
        StringBuilder buf = new StringBuilder();
        NicProfile nic = Mockito.mock(NicProfile.class);
        Mockito.when(nic.getIPv4Address()).thenReturn(null);

        spy.configureIpAndMaskOnNicWithIpAddressNullOrNotNull(buf, nic, 0);

        Assert.assertEquals(" eth0ip=ipAddressInternet eth0mask=maskForIpAddressInternet", buf.toString());
    }

    @Test
    public void finalizeVirtualMachineProfileTestFullExecution() {
        boolean result = finalizeVirtualMachineProfileTestWithNoNicsExternalDhcpStr("true", createNicsOfManagementTrafficControlTrafficAndTheGivenTraffic(TrafficType.Guest));

        Assert.assertEquals(true, result);
        verifyFinalizeVirtualMachineProfileCalledMethods(true, 1);
    }

    @Test
    public void finalizeVirtualMachineProfileTestWithNoNicsExternalDhcpStrFalse() {
        boolean result = finalizeVirtualMachineProfileTestWithNoNicsExternalDhcpStr("false", new ArrayList<NicProfile>());

        Assert.assertEquals(true, result);
        verifyFinalizeVirtualMachineProfileCalledMethods(false, 0);
    }

    @Test
    public void finalizeVirtualMachineProfileTestWithNoNicsextErnalDhcpStrNull() {
        boolean result = finalizeVirtualMachineProfileTestWithNoNicsExternalDhcpStr(null, new ArrayList<NicProfile>());

        Assert.assertEquals(true, result);
        verifyFinalizeVirtualMachineProfileCalledMethods(false, 0);
    }

    @Test
    public void configurePasswordTest() {
        StringBuilder buf = new StringBuilder();
        Mockito.doReturn("true").when(configurationDao).getValue(Mockito.eq("system.vm.random.password"));
        Mockito.doReturn("123").when(configurationDao).getValue(Mockito.eq("system.vm.password"));

        spy.configurePassword(buf);

        Assert.assertEquals(" vmpassword=123", buf.toString());
    }

    @Test
    public void configurePasswordTestNotRandomPassword() {
        StringBuilder buf = new StringBuilder();
        Mockito.doReturn("false").when(configurationDao).getValue(Mockito.eq("system.vm.random.password"));

        spy.configurePassword(buf);

        Assert.assertEquals(StringUtils.EMPTY, buf.toString());
    }

    @Test
    public void configureBasicVmProfileTest() {
        StringBuilder buf = new StringBuilder();
        VirtualMachine vm = Mockito.mock(VirtualMachine.class);
        Mockito.when(vm.getHostName()).thenReturn("vmHostName");
        DataCenter dc = Mockito.mock(DataCenter.class);
        Mockito.when(dc.getId()).thenReturn(0l);

        Pod pod = Mockito.mock(Pod.class);
        Mockito.when(pod.getId()).thenReturn(0l);

        DeployDestination dest = Mockito.mock(DeployDestination.class);
        Mockito.when(dest.getDataCenter()).thenReturn(dc);
        Mockito.when(dest.getPod()).thenReturn(pod);

        VirtualMachineProfile profile = Mockito.mock(VirtualMachineProfile.class);
        Mockito.when(profile.getBootArgsBuilder()).thenReturn(buf);
        Mockito.when(profile.getVirtualMachine()).thenReturn(vm);
        Mockito.when(profile.getId()).thenReturn(123l);

        StringBuilder result = spy.configureBasicVmProfile(profile, dest);

        Assert.assertEquals(" template=domP host=localhost name=vmHostName zone=0 pod=0 guid=autonomiccsSystemVm.123 autonomiccsSystemVm_vm=123", result.toString());
        InOrder inOrder = Mockito.inOrder(profile, vm, dest, dc, pod);
        inOrder.verify(profile).getBootArgsBuilder();
        inOrder.verify(profile).getVirtualMachine();
        inOrder.verify(vm).getHostName();
        inOrder.verify(dest).getDataCenter();
        inOrder.verify(dc).getId();
        inOrder.verify(dest).getPod();
        inOrder.verify(pod).getId();
        inOrder.verify(profile, Mockito.times(2)).getId();
    }

    @Test
    public void configureNicDhcpAndDnsTest() {
        StringBuilder buf = new StringBuilder();
        DataCenterVO dc = configureDataCenterDns("internalDns1", "internalDns2", "dns1", "dns2");
        VirtualMachineProfile profile = configureDcProfileAndVm(dc);

        spy.configureNicDhcpAndDns(profile, buf, true);

        Assert.assertEquals(" bootproto=dhcp internaldns1=internalDns1 internaldns2=internalDns2 dns1=dns1 dns2=dns2", buf.toString());
    }

    @Test
    public void configureNicDhcpAndDnsTestNotExternalDhcp() {
        StringBuilder buf = new StringBuilder();
        DataCenterVO dc = configureDataCenterDns("internalDns1", "internalDns2", "dns1", "dns2");
        VirtualMachineProfile profile = configureDcProfileAndVm(dc);

        spy.configureNicDhcpAndDns(profile, buf, false);

        Assert.assertEquals(" internaldns1=internalDns1 internaldns2=internalDns2 dns1=dns1 dns2=dns2", buf.toString());
    }

    @Test
    public void configureNicDhcpAndDnsTestAllDnsBlank() {
        StringBuilder buf = new StringBuilder();
        DataCenterVO dc = configureDataCenterDns("", "", "", "");
        VirtualMachineProfile profile = configureDcProfileAndVm(dc);

        spy.configureNicDhcpAndDns(profile, buf, true);

        Assert.assertEquals(" bootproto=dhcp internaldns1= internaldns2= dns1= dns2=", buf.toString());
    }

    @Test
    public void configureNicDhcpAndDnsTestAllDnsNull() {
        StringBuilder buf = new StringBuilder();
        DataCenterVO dc = configureDataCenterDns(null, null, null, null);
        VirtualMachineProfile profile = configureDcProfileAndVm(dc);

        spy.configureNicDhcpAndDns(profile, buf, true);

        Assert.assertEquals(" bootproto=dhcp internaldns1=null dns1=null", buf.toString());
    }

    @Test
    public void configureNicGatewayAndManagementNetworkTestBufferEmpty() throws Exception {
        StringBuilder buf = new StringBuilder();
        configureTestConfigureNicGatewayAndManagementNetwork(false, TrafficType.Guest);

        spy.configureNicGatewayAndManagementNetwork(dest, buf, nic);

        Assert.assertEquals(StringUtils.EMPTY, buf.toString());
    }

    @Test
    @PrepareForTest(NetUtils.class)
    public void configureNicGatewayAndManagementNetworkTestTrafficManagementAndIsNotValidCIDR() throws Exception {
        StringBuilder buf = new StringBuilder();
        PowerMockito.mockStatic(NetUtils.class);
        PowerMockito.doReturn(false).when(NetUtils.class, "isValidCIDR", Mockito.anyString());
        configureTestConfigureNicGatewayAndManagementNetwork(true, TrafficType.Management);

        spy.configureNicGatewayAndManagementNetwork(dest, buf, nic);

        Assert.assertEquals(" gateway=IPv4Gateway localgw=localgw", buf.toString());
    }

    @Test
    @PrepareForTest(NetUtils.class)
    public void configureNicGatewayAndManagementNetworkTestIsValidCidrAndTrafficTypeManagement() throws Exception {
        StringBuilder buf = new StringBuilder();
        PowerMockito.mockStatic(NetUtils.class);
        PowerMockito.doReturn(true).when(NetUtils.class, "isValidCIDR", Mockito.anyString());
        configureTestConfigureNicGatewayAndManagementNetwork(true, TrafficType.Management);

        spy.configureNicGatewayAndManagementNetwork(dest, buf, nic);

        Assert.assertEquals(" gateway=IPv4Gateway mgmtcidr=mgmt_cidr localgw=localgw", buf.toString());
    }

    @Test
    public void configureNicGatewayAndManagementNetworkTestTrafficTypeNotManagement() {
        StringBuilder buf = new StringBuilder();
        configureTestConfigureNicGatewayAndManagementNetwork(true, TrafficType.Guest);

        spy.configureNicGatewayAndManagementNetwork(dest, buf, nic);

        Assert.assertEquals(" gateway=IPv4Gateway", buf.toString());
    }

    @Test
    public void configureNicGatewayAndManagementNetworkTestNotDefaultNicAndTrafficManagement() {
        StringBuilder buf = new StringBuilder();
        configureTestConfigureNicGatewayAndManagementNetwork(false, TrafficType.Management);

        spy.configureNicGatewayAndManagementNetwork(dest, buf, nic);

        Assert.assertEquals(" localgw=localgw", buf.toString());
    }

    @Test
    @PrepareForTest(NetUtils.class)
    public void configureNicGatewayAndManagementNetworkTestTrafficManagementAndIsValidCIDR() throws Exception {
        StringBuilder buf = new StringBuilder();
        PowerMockito.mockStatic(NetUtils.class);
        PowerMockito.doReturn(true).when(NetUtils.class, "isValidCIDR", Mockito.anyString());

        configureTestConfigureNicGatewayAndManagementNetwork(false, TrafficType.Management);

        spy.configureNicGatewayAndManagementNetwork(dest, buf, nic);

        Assert.assertEquals(" mgmtcidr=mgmt_cidr localgw=localgw", buf.toString());
    }

    private void addNic(List<NicProfile> nicList, TrafficType trafficTypeNic) {
        NicProfile nic = Mockito.mock(NicProfile.class);
        Mockito.when(nic.getDeviceId()).thenReturn(1);
        Mockito.when(nic.getTrafficType()).thenReturn(trafficTypeNic);
        Mockito.when(nic.getIPv4Address()).thenReturn("0.0.0.0");
        nicList.add(nic);
    }

    private void verifySetEachNicIpAddressCalledMethods(AutonomiccsSystemVm autonomiccsSystemVm, int setManagementIpTimes, int setPrivateIpTimes, int setPublicIpTimes) {
        Mockito.verify(autonomiccsSystemVm, Mockito.times(setManagementIpTimes)).setManagementIpAddress(Mockito.anyString());
        Mockito.verify(autonomiccsSystemVm, Mockito.times(setPrivateIpTimes)).setPrivateIpAddress(Mockito.anyString());
        Mockito.verify(autonomiccsSystemVm, Mockito.times(setPublicIpTimes)).setPublicIpAddress(Mockito.anyString());
    }

    private void configureDoNothingAtSetEachNicIpAddressTests(AutonomiccsSystemVm autonomiccsSystemVm) {
        Mockito.doNothing().when(autonomiccsSystemVm).setManagementIpAddress(Mockito.anyString());
        Mockito.doNothing().when(autonomiccsSystemVm).setPrivateIpAddress(Mockito.anyString());
        Mockito.doNothing().when(autonomiccsSystemVm).setPublicIpAddress(Mockito.anyString());
    }

    private List<NicProfile> createNicsOfManagementTrafficControlTrafficAndTheGivenTraffic(TrafficType tf) {
        List<NicProfile> nics = new ArrayList<>();
        addNic(nics, TrafficType.Management);
        addNic(nics, TrafficType.Control);
        addNic(nics, tf);
        return nics;
    }

    private DataCenter setupDataCenter(NetworkType nt, boolean isSecurityGroupEnabled) {
        DataCenter dc = Mockito.mock(DataCenter.class);
        Mockito.when(dc.getNetworkType()).thenReturn(nt);
        Mockito.when(dc.isSecurityGroupEnabled()).thenReturn(isSecurityGroupEnabled);
        return dc;
    }

    private void configureConfigurationDao(String isExternalIpAllocatorEnabled, String isVmRandomPassword, String vmPassword) {
        Mockito.doReturn(isExternalIpAllocatorEnabled).when(configurationDao).getValue(Mockito.eq("direct.attach.network.externalIpAllocator.enabled"));
        Mockito.doReturn(isVmRandomPassword).when(configurationDao).getValue(Mockito.eq("system.vm.random.password"));
        Mockito.doReturn(vmPassword).when(configurationDao).getValue(Mockito.eq("system.vm.password"));
    }

    private void verifyFinalizeVirtualMachineProfileCalledMethods(boolean configureNicDhcpAndDns, int nics) {
        InOrder spyInOrder = Mockito.inOrder(spy);
        spyInOrder.verify(spy).configureBasicVmProfile(Mockito.any(VirtualMachineProfile.class), Mockito.any(DeployDestination.class));
        spyInOrder.verify(spy).configurePassword(Mockito.any(StringBuilder.class));
        InOrder inOrder = Mockito.inOrder(configurationDao);
        inOrder.verify(configurationDao).getValue(Mockito.eq("direct.attach.network.externalIpAllocator.enabled"));
        inOrder.verify(configurationDao).getValue(Mockito.eq("system.vm.random.password"));
        inOrder.verify(configurationDao).getValue(Mockito.eq("system.vm.password"));
        spyInOrder.verify(spy, Mockito.times(nics)).configureIpAndMaskOnNicWithIpAddressNullOrNotNull(Mockito.any(StringBuilder.class), Mockito.any(NicProfile.class),
                Mockito.anyInt());
        spyInOrder.verify(spy, Mockito.times(nics)).configureNicGatewayAndManagementNetwork(Mockito.any(DeployDestination.class), Mockito.any(StringBuilder.class),
                Mockito.any(NicProfile.class));
        spyInOrder.verify(spy).configureNicDhcpAndDns(Mockito.any(VirtualMachineProfile.class), Mockito.any(StringBuilder.class), Mockito.eq(configureNicDhcpAndDns));
    }

    private DeployDestination configurePod() {
        Pod pod = Mockito.mock(Pod.class);
        Mockito.when(pod.getGateway()).thenReturn("gateway");
        DeployDestination dest = Mockito.mock(DeployDestination.class);
        Mockito.when(dest.getPod()).thenReturn(pod);
        return dest;
    }

    private boolean finalizeVirtualMachineProfileTestWithNoNicsExternalDhcpStr(String str, List<NicProfile> nics) {
        ReservationContext context = Mockito.mock(ReservationContext.class);
        DeployDestination dest = configurePod();
        VirtualMachineProfile profile = Mockito.mock(VirtualMachineProfile.class);
        Mockito.when(profile.getNics()).thenReturn(nics);

        StringBuilder buf = new StringBuilder();
        configureConfigurationDao(str, "true", "vmPassword");
        Mockito.doReturn(buf).when(spy).configureBasicVmProfile(Mockito.any(VirtualMachineProfile.class), Mockito.any(DeployDestination.class));
        Mockito.doReturn("bootArgs").when(spy).configureNicDhcpAndDns(Mockito.any(VirtualMachineProfile.class), Mockito.any(StringBuilder.class), Mockito.anyBoolean());

        return spy.finalizeVirtualMachineProfile(profile, dest, context);
    }

    private VirtualMachineProfile configureDcProfileAndVm(DataCenterVO dc) {
        VirtualMachine vm = Mockito.mock(VirtualMachine.class);
        Mockito.when(vm.getDataCenterId()).thenReturn(0l);
        VirtualMachineProfile profile = Mockito.mock(VirtualMachineProfile.class);
        Mockito.when(profile.getVirtualMachine()).thenReturn(vm);
        Mockito.when(datacenterDao.findById(Mockito.anyLong())).thenReturn(dc);
        return profile;
    }

    private DataCenterVO configureDataCenterDns(String internalDns1, String internalDns2, String dns1, String dns2) {
        DataCenterVO dc = Mockito.mock(DataCenterVO.class);
        Mockito.when(dc.getInternalDns1()).thenReturn(internalDns1);
        Mockito.when(dc.getInternalDns2()).thenReturn(internalDns2);
        Mockito.when(dc.getDns1()).thenReturn(dns1);
        Mockito.when(dc.getDns2()).thenReturn(dns2);
        return dc;
    }

    private void configureFinalizeExpungeTest(VirtualMachine vm, AutonomiccsSystemVm autonomiccsSystemVm) {
        Mockito.when(vm.getId()).thenReturn(0l);
        Mockito.when(autonomiccsSystemVmDao.findById(vm.getId())).thenReturn(autonomiccsSystemVm);
        Mockito.doReturn(true).when(autonomiccsSystemVmDao).update(Mockito.anyLong(), Mockito.any(AutonomiccsSystemVm.class));
    }

    private void configureTestConfigureNicGatewayAndManagementNetwork(boolean isDefaultNic, TrafficType trafficType) {
        pod = Mockito.mock(Pod.class);
        Mockito.when(pod.getGateway()).thenReturn("localgw");
        Mockito.when(configurationDao.getValue(Mockito.anyString())).thenReturn("mgmt_cidr");

        nic = Mockito.mock(NicProfile.class);
        Mockito.when(nic.isDefaultNic()).thenReturn(isDefaultNic);
        Mockito.when(nic.getTrafficType()).thenReturn(trafficType);
        Mockito.when(nic.getIPv4Gateway()).thenReturn("IPv4Gateway");

        dest = Mockito.mock(DeployDestination.class);
        Mockito.when(dest.getPod()).thenReturn(pod);
    }

}
