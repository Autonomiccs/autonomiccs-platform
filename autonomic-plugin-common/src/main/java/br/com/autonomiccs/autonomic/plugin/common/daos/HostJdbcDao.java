package br.com.autonomiccs.autonomic.plugin.common.daos;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.cloud.host.Status;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.resource.ResourceState;

import br.com.autonomiccs.autonomic.plugin.common.enums.HostAdministrationStatus;
import br.com.autonomiccs.autonomic.plugin.common.enums.StartType;

/**
 * Offers support for some 'select' and 'update' sql statements in table 'host'.
 */
public class HostJdbcDao extends JdbcDaoSupport {

    private String sqlGetConsolidationStatus = "SELECT consolidation_status FROM host WHERE id=?;";
    private String sqlSetConsolidationStatus = "UPDATE host SET consolidation_status=? WHERE id=?;";
    private String sqlGetStartType = "SELECT start_type FROM host WHERE id=?;";
    private String sqlGetStatus = "SELECT status FROM host WHERE id=?;";
    private String sqlGetResourceState = "SELECT resource_state FROM host WHERE id=?;";

    public HostAdministrationStatus getConsolidationStatus(long hostId) {
        String hostConsolidationStatusAsAtring = getJdbcTemplate().queryForObject(sqlGetConsolidationStatus, String.class, hostId);
        if (StringUtils.isBlank(hostConsolidationStatusAsAtring)) {
            return null;
        }
        return HostAdministrationStatus.valueOf(hostConsolidationStatusAsAtring);
    }

    /**
     * Updates the 'consolidation_status' column from the 'host' table.
     *
     * @param hostConsolidationStatus
     * @param hostId
     */
    public void setConsolidationStatus(HostAdministrationStatus hostConsolidationStatus, long hostId) {
        Object[] args = {ObjectUtils.toString(hostConsolidationStatus), hostId};
        getJdbcTemplate().update(sqlSetConsolidationStatus, args);
    }

    /**
     * Selects the 'start_type' column from the 'host' table.
     *
     * @param hostId
     * @return
     */
    public StartType getStartType(long hostId) {
        return StartType.valueOf(getJdbcTemplate().queryForObject(sqlGetStartType, String.class, hostId));
    }

    /**
     * Selects the 'status' column from the 'host' table.
     *
     * @param hostId
     * @return
     */
    public Status getStatus(long hostId) {
        return Status.valueOf(getJdbcTemplate().queryForObject(sqlGetStatus, String.class, hostId));
    }

    /**
     * Selects the 'resource_state' column from the 'host' table.
     *
     * @param hostId
     * @return
     */
    public ResourceState getResourceState(long hostId) {
        return ResourceState.valueOf(getJdbcTemplate().queryForObject(sqlGetResourceState, String.class, hostId));
    }

    private String sqlSetAllHypervisorsTypeInCloud = "select hypervisor_type from host where removed is null and hypervisor_type is not null group by hypervisor_type";
    /**
     * It loads all of the hypervisors types in use in the whole cloud environment
     * @return List<HypervisorType>
     */
    public List<HypervisorType> getAllHypervisorsTypeInCloud() {
        List<String> hypervisorTypesAsString = getJdbcTemplate().queryForList(sqlSetAllHypervisorsTypeInCloud, String.class);
        List<HypervisorType> hypervisorTypes = new ArrayList<Hypervisor.HypervisorType>();
        for (String s : hypervisorTypesAsString) {
            hypervisorTypes.add(HypervisorType.valueOf(s));
        }
        return hypervisorTypes;
    }

    private String sqlCheckIsThereAnyHostOnCloudDeactivatedByOurManager = "select id from host where removed is null and consolidation_status = 'ShutDownToConsolidate'";
    public boolean isThereAnyHostOnCloudDeactivatedByOurManager() {
        List<Long> hostsIds = getJdbcTemplate().queryForList(sqlCheckIsThereAnyHostOnCloudDeactivatedByOurManager, Long.class);
        return CollectionUtils.isNotEmpty(hostsIds);
    }

    private String sqlCheckIsThereAnyHostOnPodDeactivatedByOurManager = "select id from host where removed is null and consolidation_status = 'ShutDownToConsolidate' and pod_id = ?";
    public boolean isThereAnyHostOnPodDeactivatedByOurManager(long id) {
        List<Long> hostsIds = getJdbcTemplate().queryForList(sqlCheckIsThereAnyHostOnPodDeactivatedByOurManager, Long.class, id);
        return CollectionUtils.isNotEmpty(hostsIds);
    }
}
