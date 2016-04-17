package br.com.autonomiccs.autonomic.plugin.common.enums;

/**
 * A cluster can be in one of two administration status: {@link #InProgress} or
 * {@link #Done}. That means, the agents has already worked on it, or the agent has already finished its work there.
 */
public enum ClusterAdministrationStatus {

    Done, InProgress;

    /**
     * Returns true if the cluster consolidation status is equals to
     * {@link #InProgress}.
     *
     * @param administrationStatus
     * @return true if the administrationStatus is {@link #InProgress}
     */
    public static boolean isClusterBeingManaged(ClusterAdministrationStatus administrationStatus) {
        return administrationStatus == InProgress;
    }

}
