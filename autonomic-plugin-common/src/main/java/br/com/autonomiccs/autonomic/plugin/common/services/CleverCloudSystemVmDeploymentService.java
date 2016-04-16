package br.com.autonomiccs.autonomic.plugin.common.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.cloudstack.engine.orchestration.service.NetworkOrchestrationService;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.host.HostVO;
import com.cloud.network.Network;
import com.cloud.network.NetworkModel;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.NicProfile;
import com.cloud.vm.VirtualMachineManager;

import br.com.autonomiccs.autonomic.plugin.common.daos.CleverCloudSystemVmDao;
import br.com.autonomiccs.autonomic.plugin.common.enums.SystemVmType;
import br.com.autonomiccs.autonomic.plugin.common.pojos.CleverCloudsSystemVm;
import br.com.autonomiccs.autonomic.plugin.common.utils.HostUtils;
import br.com.autonomiccs.autonomic.plugin.common.utils.SshUtils;
import br.com.autonomiccs.autonomic.plugin.common.utils.ThreadUtils;

/**
 * This class is used to deploy and manage clever cloud system VMs.
 */
@Service
public class CleverCloudSystemVmDeploymentService implements InitializingBean {

    private final Logger logger = Logger.getLogger(getClass());
    private final String commandToInstallOpenJDK7 = "aptitude -y install openjdk-7-jdk";

    private ServiceOfferingVO cleverCloudsSystemVmServiceOffering;

    @Autowired
    private SshUtils sshUtils;

    @Autowired
    private CleverCloudSystemVmTemplateService cleverCloudSystemVmTemplateService;

    @Autowired
    private CleverCloudSystemVmDao cleverCloudSystemVmDao;

    @Autowired
    private HostService hostService;

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private DataCenterDao dataCenterDao;

    @Autowired
    private AccountManager accountManager;

    @Autowired
    private NetworkDao networkDao;

    @Autowired
    private NetworkModel networkModel;

    @Autowired
    private NetworkOfferingDao networkOfferingDao;

    @Autowired
    private NetworkOrchestrationService networkManager;

    @Autowired
    private VirtualMachineManager virtualMachineManager;

    @Autowired
    private CleverCloudsServiceOfferingService cleverCloudsServiceOfferingService;

    @Autowired
    private HostUtils hostUtils;

    @Autowired
    private ThreadUtils threadUtils;

    /**
     * Will call the deploySystemVM method and will install openJDK 1.7 using
     * SSH in the SystemVM that is created.
     *
     * @param hostId
     *            in which the VM is being deployed
     * @param systemVmType
     *            type of vm being deployed
     * @return {@link CleverCloudsSystemVm} that represents the deployed VM
     */
    public CleverCloudsSystemVm deploySystemVmWithJAVA(Long hostId, SystemVmType systemVmType) {
        CleverCloudsSystemVm vmInstance = deploySystemVm(hostId, systemVmType);
        String managementIp = vmInstance.getManagementIpAddress();
        sshUtils.executeCommandOnHostViaSsh(managementIp, "aptitude update");
        sshUtils.executeCommandOnHostViaSsh(managementIp, commandToInstallOpenJDK7);
        return vmInstance;
    }

    /**
     * It deployed a clever clouds system VM into the provided host. We will
     * discover the template based on the hypervisor type of the host. The
     * prefix that has to be informed is used to mark the type of agent that is
     * being deployed.
     * This method will wait until the deployed VM is up. If the VM for some reason do not boot, a runtime exception will be thrown.
     *
     * @param hostId
     *            in which the VM is being deployed
     * @param systemVmType
     *            type of vm being deployed
     * @return {@link CleverCloudsSystemVm} that represents the deployed VM
     */
    public CleverCloudsSystemVm deploySystemVm(Long hostId, SystemVmType systemVmType) {
        HostVO host = hostService.findHostById(hostId);
        if (host == null) {
            throw new RuntimeException(String.format("Could not find a host with the provieded id [%d]", hostId));
        }
        if (!cleverCloudSystemVmTemplateService.isTemplateRegisteredAndReadyForHypervisor(host.getHypervisorType())) {
            throw new RuntimeException(String.format("There is no Clever cloud system VM for hypervisor [%s], so we do not deploy a system Vm for it.", host.getHypervisorType()));
        }
        VMTemplateVO systemVmTemplate = cleverCloudSystemVmTemplateService.findCleverCloudSystemVmTemplate(host.getHypervisorType());
        if (systemVmTemplate == null) {
            throw new RuntimeException(String.format("Could not find a System VM template for the host hypervisors [%s]", host.getHypervisorType()));
        }

        Account systemAcct = accountManager.getSystemAccount();

        long id = cleverCloudSystemVmDao.getNextInSequence(Long.class, "id");
        String name = createCleverCloudSystemVmNameForType(id, systemVmType, getVirtualMachineInstanceSuffix());

        long dataCenterId = host.getDataCenterId();
        DataCenterDeployment plan = new DataCenterDeployment(dataCenterId, host.getPodId(), host.getClusterId(), host.getId(), null, null);

        NetworkVO defaultNetwork = getDefaultNetwork(dataCenterId);
        List<? extends NetworkOffering> offerings = networkModel.getSystemAccountNetworkOfferings(NetworkOffering.SystemControlNetwork, NetworkOffering.SystemManagementNetwork);
        LinkedHashMap<Network, List<? extends NicProfile>> networks = new LinkedHashMap<Network, List<? extends NicProfile>>(offerings.size() + 1);
        NicProfile defaultNic = new NicProfile();
        defaultNic.setDefaultNic(true);
        defaultNic.setDeviceId(2);

        networks.put(networkManager.setupNetwork(systemAcct, networkOfferingDao.findById(defaultNetwork.getNetworkOfferingId()), plan, null, null, false).get(0),
                new ArrayList<NicProfile>(Arrays.asList(defaultNic)));

        for (NetworkOffering offering : offerings) {
            networks.put(networkManager.setupNetwork(systemAcct, offering, plan, null, null, false).get(0), new ArrayList<NicProfile>());
        }

        CleverCloudsSystemVm cleverCloudsSystemVm = new CleverCloudsSystemVm(id, cleverCloudsSystemVmServiceOffering.getId(), name, systemVmTemplate.getId(),
                systemVmTemplate.getHypervisorType(), systemVmTemplate.getGuestOSId(), dataCenterId, systemAcct.getDomainId(), systemAcct.getId(),
                accountManager.getSystemUser().getId(), cleverCloudsSystemVmServiceOffering.getOfferHA());
        cleverCloudsSystemVm.setDynamicallyScalable(systemVmTemplate.isDynamicallyScalable());
        cleverCloudsSystemVm = cleverCloudSystemVmDao.persist(cleverCloudsSystemVm);

        try {
            virtualMachineManager.allocate(name, systemVmTemplate, cleverCloudsSystemVmServiceOffering, networks, plan, null);

            cleverCloudsSystemVm = cleverCloudSystemVmDao.findById(id);
            virtualMachineManager.advanceStart(cleverCloudsSystemVm.getUuid(), null, null);
            cleverCloudsSystemVm = cleverCloudSystemVmDao.findById(id);
        } catch (ConcurrentOperationException | ResourceUnavailableException | OperationTimedoutException | InsufficientCapacityException e) {
            throw new RuntimeException("Insufficient capacity exception when deploying a clever cloud system VM.", e);
        }

        for (int i = 0; i < 100; i++) {
            logger.debug(String.format("Checking for the %d time(s) if the system VM [name=%s], [id=%d] is reachable ", i, cleverCloudsSystemVm.getInstanceName(),
                    cleverCloudsSystemVm.getId()));
            if (hostUtils.isHostReachable(cleverCloudsSystemVm.getManagementIpAddress())) {
                logger.info(String.format("We noticed out that the system VM [name=%s], [id=%d] is reachable after %d tries.", cleverCloudsSystemVm.getInstanceName(),
                        cleverCloudsSystemVm.getId(), i));
                break;
            }
            threadUtils.sleepThread(5);
        }
        if (!hostUtils.isHostReachable(cleverCloudsSystemVm.getManagementIpAddress())) {
            throw new RuntimeException(String.format("The system VM [name=%s], [id=%d] is not reachable, maybe a problem has happened while starting it.",
                    cleverCloudsSystemVm.getInstanceName(),
                    cleverCloudsSystemVm.getId()));
        }
        return cleverCloudsSystemVm;
    }

    private NetworkVO getDefaultNetwork(long dataCenterId) {
        DataCenterVO dc = dataCenterDao.findById(dataCenterId);
        if (dc.getNetworkType() == NetworkType.Advanced && dc.isSecurityGroupEnabled()) {
            List<NetworkVO> networks = networkDao.listByZoneSecurityGroup(dataCenterId);
            if (networks == null || networks.size() == 0) {
                throw new CloudRuntimeException("Can not found security enabled network in SG Zone " + dc);
            }
            return networks.get(0);
        } else {
            TrafficType defaultTrafficType = TrafficType.Public;
            if (dc.getNetworkType() == NetworkType.Basic || dc.isSecurityGroupEnabled()) {
                defaultTrafficType = TrafficType.Guest;
            }
            List<NetworkVO> defaultNetworks = networkDao.listByZoneAndTrafficType(dataCenterId, defaultTrafficType);

            // api should never allow this situation to happen
            if (defaultNetworks.size() != 1) {
                throw new CloudRuntimeException("Found " + defaultNetworks.size() + " networks of type " + defaultTrafficType + " when expect to find 1");
            }
            return defaultNetworks.get(0);
        }
    }

    /**
     * The name created follows the convention: system VM type suffix-id of
     * VM-instanceSuffix
     *
     * @param id
     * @param systemVmType
     * @param instanceSuffix
     * @return the instance name for clever clouds system VMs
     */
    private String createCleverCloudSystemVmNameForType(long id, SystemVmType systemVmType, String instanceSuffix) {
        return String.format("%s-%d-%s", systemVmType.getNamePrefix(), id, instanceSuffix);
    }

    /**
     * Retrieves the virtual machine name suffix from the database. The suffix
     * is defined by the "instance.name" parameter.
     *
     * @return virtual machine instance suffix
     */
    private String getVirtualMachineInstanceSuffix() {
        Map<String, String> configs = getConfigurationsFromDatabase();
        return configs.get("instance.name");
    }

    private Map<String, String> getConfigurationsFromDatabase() {
        return this.configurationDao.getConfiguration("management-server", new HashMap<String, Object>());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadCleverCloudSystemVmServiceOffering();
        if (cleverCloudsSystemVmServiceOffering == null) {
            throw new RuntimeException("Could not register the clever cloud system VMs service offering.");
        }
    }

    private void loadCleverCloudSystemVmServiceOffering() {
        cleverCloudsSystemVmServiceOffering = cleverCloudsServiceOfferingService.searchCleverCloudsServiceOffering();
        if (cleverCloudsSystemVmServiceOffering == null) {
            cleverCloudsServiceOfferingService.createCleverCloudsServiceOffering();
            cleverCloudsSystemVmServiceOffering = cleverCloudsServiceOfferingService.searchCleverCloudsServiceOffering();
        }
    }
}
