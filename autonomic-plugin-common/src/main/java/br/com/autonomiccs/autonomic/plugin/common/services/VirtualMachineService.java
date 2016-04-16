package br.com.autonomiccs.autonomic.plugin.common.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.VMInstanceDao;

@Service
public class VirtualMachineService {

    @Autowired
    private VMInstanceDao vmInstanceDao;

    public VMInstanceVO searchVmInstanceById(Long vmId) {
        return vmInstanceDao.findById(vmId);
    }

    public void update(long id, VMInstanceVO vmInstance) {
        vmInstanceDao.update(id, vmInstance);
    }

    public void remove(long id) {
        vmInstanceDao.remove(id);
    }
}
