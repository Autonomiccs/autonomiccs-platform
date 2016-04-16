package br.com.autonomiccs.autonomic.plugin.common.daos;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import br.com.autonomiccs.autonomic.plugin.common.enums.SystemVmType;

/**
 * This class is intended to deal with operations regarding the clever clouds system VMs.
 */
public class CleverCloudSystemVmJdbcDao extends JdbcDaoSupport {

    private String sqlGetStartHostServiceVmIdFromPod = "select id from vm_instance where removed is null and pod_id  = ? and account_id = 1 and instance_name like ? ";

    public Long getStartHostServiceVmIdFromPod(Long podId, SystemVmType systemVmType) {
        try {
            return getJdbcTemplate().queryForObject(sqlGetStartHostServiceVmIdFromPod, Long.class, podId, systemVmType.getNamePrefix() + "%");
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

}
