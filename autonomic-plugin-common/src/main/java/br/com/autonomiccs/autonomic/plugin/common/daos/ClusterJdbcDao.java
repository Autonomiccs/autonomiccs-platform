package br.com.autonomiccs.autonomic.plugin.common.daos;

import java.util.Date;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import br.com.autonomiccs.autonomic.plugin.common.enums.ClusterConsolidationStatus;

public class ClusterJdbcDao extends JdbcDaoSupport {

    private String sqlGetClusterConsolidationStatus = "SELECT consolidation_status FROM cluster WHERE id=?;";
    private String sqlSetClusterConsolidationStatus = "UPDATE cluster SET consolidation_status=? WHERE id=?;";

    private String sqlGetClusterLastConsolidated = "SELECT last_consolidated FROM cluster WHERE id=?;";
    private String sqlSetClusterLastConsolidated = "UPDATE cluster SET last_consolidated=? WHERE id=?;";

    /**
     * Selects the 'consolidation_status' column from the 'cluster' table.
     *
     * @param clusterId
     * @return
     */
    public ClusterConsolidationStatus getClusterConsolidationStatus(long clusterId) {
        String statusAsString = getJdbcTemplate().queryForObject(sqlGetClusterConsolidationStatus, String.class, clusterId);
        if (statusAsString == null) {
            return null;
        }
        return ClusterConsolidationStatus.valueOf(statusAsString);
    }

    /**
     * Updates the 'consolidation_status' column from the 'cluster' table.
     *
     * @param clusterConsolidationStatus
     * @param clusterId
     */
    public void setClusterConsolidationStatus(ClusterConsolidationStatus clusterConsolidationStatus, long clusterId) {
        Object[] args = {clusterConsolidationStatus.toString(), clusterId};
        getJdbcTemplate().update(sqlSetClusterConsolidationStatus, args);
    }

    /**
     * Selects the 'last_consolidated' column from the 'cluster' table.
     *
     * @param clusterId
     * @return
     */
    public Date getClusterLastConsolidated(long clusterId) {
        return getJdbcTemplate().queryForObject(sqlGetClusterLastConsolidated, Date.class, clusterId);
    }

    /**
     * Updates the 'last_consolidated' column from the 'cluster' table.
     *
     * @param date
     * @param clusterId
     */
    public void setClusterLastConsolidated(Date date, long clusterId) {
        Object[] args = { date, clusterId };
        getJdbcTemplate().update(sqlSetClusterLastConsolidated, args);
    }

}
