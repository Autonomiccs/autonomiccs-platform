package br.com.autonomiccs.autonomic.plugin.common.daos;

import java.util.Date;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import br.com.autonomiccs.autonomic.plugin.common.enums.ClusterAdministrationStatus;

public class ClusterJdbcDao extends JdbcDaoSupport {

    private String sqlGetClusterAdministrationStatus = "SELECT administration_status FROM cluster WHERE id=?;";
    private String sqlSetClusterAdministrationStatus = "UPDATE cluster SET administration_status=? WHERE id=?;";

    private String sqlGetClusterLastAdministration = "SELECT last_administration FROM cluster WHERE id=?;";
    private String sqlSetClusterLastAdministration = "UPDATE cluster SET last_administration=? WHERE id=?;";

    /**
     * Selects the 'last_administration' column from the 'cluster' table.
     *
     * @param clusterId
     * @return
     */
    public ClusterAdministrationStatus getClusterAdministrationStatus(long clusterId) {
        String statusAsString = getJdbcTemplate().queryForObject(sqlGetClusterAdministrationStatus, String.class, clusterId);
        if (statusAsString == null) {
            return null;
        }
        return ClusterAdministrationStatus.valueOf(statusAsString);
    }

    /**
     * Updates the 'last_administration' column from the 'cluster' table.
     *
     * @param clusterConsolidationStatus
     * @param clusterId
     */
    public void setClusterAdministrationStatus(ClusterAdministrationStatus clusterConsolidationStatus, long clusterId) {
        Object[] args = {clusterConsolidationStatus.toString(), clusterId};
        getJdbcTemplate().update(sqlSetClusterAdministrationStatus, args);
    }

    /**
     * Selects the 'last_administration' column from the 'cluster' table.
     *
     * @param clusterId
     * @return
     */
    public Date getClusterLastAdminstration(long clusterId) {
        return getJdbcTemplate().queryForObject(sqlGetClusterLastAdministration, Date.class, clusterId);
    }

    /**
     * Updates the 'last_administration' column from the 'cluster' table.
     *
     * @param date
     * @param clusterId
     */
    public void setClusterLastAdministration(Date date, long clusterId) {
        Object[] args = { date, clusterId };
        getJdbcTemplate().update(sqlSetClusterLastAdministration, args);
    }

}
