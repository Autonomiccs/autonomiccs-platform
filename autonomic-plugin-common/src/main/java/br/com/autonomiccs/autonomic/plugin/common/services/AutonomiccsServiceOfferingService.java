package br.com.autonomiccs.autonomic.plugin.common.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.Storage;
import com.cloud.vm.VirtualMachine;

@Service
public class AutonomiccsServiceOfferingService {

    private static final int AUTONOMICCS_SYSTEM_VM_VM_RAMSIZE = 512;
    private static final int AUTONOMICCS_SYSTEM_VM_CPUMHZ = 1000;
    private static final String AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_UNIQUE_NAME = "Autonomiccs S.O.";
    private static final String AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_NAME = "Autonomiccs system VMs service offering";

    @Autowired
    private ServiceOfferingDao serviceOfferingDao;

    private ServiceOfferingVO searchServiceOfferingByName(String name) {
        List<ServiceOfferingVO> allServiceOffering = serviceOfferingDao.listAll();
        for(ServiceOfferingVO so : allServiceOffering){
            if (name.equals(so.getUniqueName())) {
                return so;
            }
        }
        return null;
    }


    public ServiceOfferingVO searchAutonomiccsServiceOffering() {
        return searchServiceOfferingByName(AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_UNIQUE_NAME);
    }

    public ServiceOfferingVO searchServiceOfferingById(long id) {
        return serviceOfferingDao.findById(id);
    }

    public void createAutonomiccsServiceOffering() {
        List<ServiceOfferingVO> offerings = serviceOfferingDao.createSystemServiceOfferings(AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_NAME,
                AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_UNIQUE_NAME, 1, AUTONOMICCS_SYSTEM_VM_VM_RAMSIZE, AUTONOMICCS_SYSTEM_VM_CPUMHZ, 0, 0, false, null,
                Storage.ProvisioningType.THIN, true, null, true, VirtualMachine.Type.Instance, true);
        if (offerings == null || offerings.size() < 2) {
            throw new RuntimeException("Data integrity problem : Autonomiccs System Offering For VMs has been removed?");
        }
    }
}
