package br.com.autonomiccs.autonomic.plugin.common.daos.configurations;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Configures the CloudStack database, inserting (if needed)
 * 'consolidation_status' and 'start_type' columns into 'host' table; It also
 * inserts 'consolidation_status' and 'last_consolidated' columns into 'cluster'
 * table.
 */
public class ConfigureDatabaseDao extends JdbcDaoSupport {

    private String sqlHasHostConsolidationStatusColumn = "SHOW COLUMNS FROM host LIKE 'consolidation_status';";
    private String sqlCreateHostConsolidationStatusColumn = "ALTER TABLE host ADD consolidation_status VARCHAR(60) AFTER status;";

    private String sqlHasHostStartTypeColumn = "SHOW COLUMNS FROM host LIKE 'start_type';";
    private String sqlCreateHostStartTypeColumn = "ALTER TABLE host ADD start_type VARCHAR(20) NOT NULL DEFAULT 'WakeOnLan' AFTER consolidation_status;";

    private String sqlHasClusterConsolidationStatusColumn = "SHOW COLUMNS FROM cluster LIKE 'consolidation_status';";
    private String sqlCreateClusterConsolidationStatusColumn = "ALTER TABLE cluster ADD consolidation_status VARCHAR(60);";

    private String sqlHasClusterLastConsolidatedColumn = "SHOW COLUMNS FROM cluster LIKE 'last_consolidated';";
    private String sqlCreateClusterLastConsolidatedColumn = "ALTER TABLE cluster ADD last_consolidated DATETIME;";

    private String sqlHasClusterManagerAlgorithmsInConfiguration = "SELECT * FROM configuration WHERE name='smart.cloudstack.clustermanager.algorithm';";
    private String sqlInsertIntoConfigurationClusterAlgorithms = "INSERT INTO configuration (category,instance,component,name,value,description,default_value,updated,scope,is_dynamic) VALUES ('Advanced','DEFAULT','autonomicClusterManager','smart.cloudstack.clustermanager.algorithm','br.ufsc.lrg.cloudstack.autonomic.consolidation.algorithm.impl.VmsDispersionAlgorithmForHomogeneousEnvironment','Full qualified heuristic class name to be used to guide the agent during the cluster management process.','br.ufsc.lrg.cloudstack.autonomic.consolidation.algorithm.impl.ClusterManagerDummyAlgorithm',null,null,0);";

    private String sqlHasCleverCloudsSystemVmTable = "SHOW TABLES LIKE 'CleverCloudsSystemVm';";
    private String sqlCreateCleverCloudsSystemVmTable = "CREATE TABLE CleverCloudsSystemVm(id BIGINT(20) UNSIGNED, public_ip_address VARCHAR(40), management_ip_address VARCHAR(40));";

    @Override
    protected void initDao() throws Exception {
        super.initDao();
        if (!hasHostConsolidationStatusColumn()) {
            createHostConsolidationStatusColumn();
        }
        if (!hasHostStartTypeColumn()) {
            createHostStartTypeColumn();
        }
        if (!hasClusterConsolidationStatusColumn()) {
            createClusterConsolidationStatusColumn();
        }
        if (!hasClusterLastConsolidatedColumn()) {
            createClusterLastConsolidatedColumn();
        }
        if (!hasClusterManagerAlgorithmsInConfiguration()) {
            insertClusterManagerAlgorithmsInConfiguration();
        }
        if (!hasCleverCloudsSystemVmTable()) {
            createCleverCloudsSystemVmTable();
        }
    }

    /**
     * TODO Documentation
     *
     * @return
     */
    private boolean hasHostConsolidationStatusColumn() {
        return (!getJdbcTemplate().queryForList(sqlHasHostConsolidationStatusColumn).isEmpty());
    }

    /**
     * TODO Documentation
     */
    private void createHostConsolidationStatusColumn() {
        getJdbcTemplate().execute(sqlCreateHostConsolidationStatusColumn);
    }

    /**
     * TODO Documentation
     *
     * @return
     */
    private boolean hasHostStartTypeColumn() {
        return !getJdbcTemplate().queryForList(sqlHasHostStartTypeColumn).isEmpty();
    }

    /**
     * TODO Documentation
     */
    private void createHostStartTypeColumn() {
        getJdbcTemplate().execute(sqlCreateHostStartTypeColumn);
    }

    /**
     * TODO Documentation
     *
     * @return
     */
    private boolean hasClusterConsolidationStatusColumn() {
        return !getJdbcTemplate().queryForList(sqlHasClusterConsolidationStatusColumn).isEmpty();
    }

    /**
     * TODO Documentation
     */
    private void createClusterConsolidationStatusColumn() {
        getJdbcTemplate().execute(sqlCreateClusterConsolidationStatusColumn);
    }

    /**
     * TODO Documentation
     *
     * @return
     */
    private boolean hasClusterLastConsolidatedColumn() {
        return !getJdbcTemplate().queryForList(sqlHasClusterLastConsolidatedColumn).isEmpty();
    }

    /**
     * TODO Documentation
     */
    private void createClusterLastConsolidatedColumn() {
        getJdbcTemplate().execute(sqlCreateClusterLastConsolidatedColumn);
    }

    /**
     * TODO Documentation
     *
     * @return
     */
    private boolean hasClusterManagerAlgorithmsInConfiguration() {
        return !getJdbcTemplate().queryForList(sqlHasClusterManagerAlgorithmsInConfiguration).isEmpty();
    }

    /**
     * TODO
     */
    private void insertClusterManagerAlgorithmsInConfiguration() {
        getJdbcTemplate().execute(sqlInsertIntoConfigurationClusterAlgorithms);
    }

    /**
     * TODO
     * @return
     */
    public boolean hasCleverCloudsSystemVmTable() {
        return !getJdbcTemplate().queryForList(sqlHasCleverCloudsSystemVmTable).isEmpty();
    }

    /**
     * TODO
     */
    private void createCleverCloudsSystemVmTable() {
        getJdbcTemplate().execute(sqlCreateCleverCloudsSystemVmTable);
    }

}
