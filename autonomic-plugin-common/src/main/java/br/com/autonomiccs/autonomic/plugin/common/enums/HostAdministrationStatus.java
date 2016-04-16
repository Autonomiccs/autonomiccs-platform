package br.com.autonomiccs.autonomic.plugin.common.enums;

/**
 * Consolidation status of the cluster (Consolidating, ConsolidationFailed or Consolidated)
 * */
public enum HostAdministrationStatus {

    FailedToShutDown,
    ShutDownToConsolidate,
    FailedToStart,
    Up;
}
