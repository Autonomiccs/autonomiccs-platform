package br.com.autonomiccs.autonomic.plugin.common.pojos;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.vm.VMInstanceVO;

@Entity
@SuppressWarnings("serial")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue(value = "AutonomiccsSystemVm")
public class AutonomiccsSystemVm extends VMInstanceVO {

    public AutonomiccsSystemVm(long id, long serviceOfferingId, String name, long templateId, HypervisorType hypervisorType, long guestOSId, long dataCenterId, long domainId,
            long accountId, long userId, boolean haEnabled) {
        super(id, serviceOfferingId, name, name, Type.Instance, templateId, hypervisorType, guestOSId, domainId, accountId, userId, haEnabled);
    }

    protected AutonomiccsSystemVm() {
        super();
    }

    @Column(name = "public_ip_address", nullable = false)
    private String publicIpAddress;

    @Column(name = "management_ip_address", nullable = false)
    private String managementIpAddress;

    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public String getManagementIpAddress() {
        return managementIpAddress;
    }

    public void setManagementIpAddress(String managementIpAddress) {
        this.managementIpAddress = managementIpAddress;
    }

}
