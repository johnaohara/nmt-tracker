package io.quarkus.nmtTracker.repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

//@ApplicationScoped
public class ProcessRepository implements StatsRepository {

    private Map<String, Long> nmtData;
    private List<String> logs;
    private List<String> pmap;
    private List<String> stats;
    private List<String> processes;

    private final Object nmtLock = new Object();
    private final Object logsLock = new Object();
    private final Object pmapLock = new Object();
    private final Object statsLock = new Object();
    private final Object processesLock = new Object();

    public void updateNmtDate(Map<String, Long> data) {
        synchronized (nmtLock) {
            if (data != null) {
                nmtData = Collections.unmodifiableMap(data);
            } else {
                nmtData = null;
            }
        }
    }

    public void updateLogs(List<String> data) {
        synchronized (logsLock) {
            if (data != null) {
                logs = Collections.unmodifiableList(data);
            } else {
                logs = null;
            }
        }
    }

    public void updatePmap(List<String> data) {
        synchronized (pmapLock) {
            if (data != null) {
                pmap = Collections.unmodifiableList(data);
            } else {
                pmap = null;
            }
        }
    }

    @Override
    public void updateProcesses(List<String> procs) {
        synchronized (processesLock) {
            if (procs != null) {
                processes = Collections.unmodifiableList(procs);
            } else {
                processes = null;
            }
        }
    }

    public Map<String, Long> getNmtData() {
        synchronized (nmtLock) {
            return nmtData;
        }
    }

    public List<String> getLogs() {
        synchronized (logsLock) {
            return logs;
        }
    }

    public List<String> getPmap() {
        synchronized (pmapLock) {
            return pmap;
        }
    }

    public List<String> getStats() {
        synchronized (statsLock) {
            return stats;
        }
    }

    @Override
    public List<String> getProcesses() {
        synchronized (processesLock) {
            return processes;
        }
    }

    public void updateDockerStats(List<String> data) {
        synchronized (statsLock) {
            if (data != null) {
                stats = Collections.unmodifiableList(data);
            } else {
                stats = null;
            }
        }
    }
}
