package br.com.autonomiccs.autonomic.plugin.common.enums;

/**
 * A cluster can be in one of two consolidation status: 'Consolidating' or
 * 'Consolidated'
 */
public enum ClusterConsolidationStatus {

    Consolidated, Consolidating;

    /**
     * Returns true if the cluster consolidation status is equals to
     * 'Consolidating'.
     *
     * @param consolidationStatus
     * @return
     */
    public static boolean isConsolidating(ClusterConsolidationStatus consolidationStatus) {
        return consolidationStatus == Consolidating;
    }

}
