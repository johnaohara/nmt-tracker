package io.quarkus.nmtTracker.repository;

import java.util.List;
import java.util.Map;

public interface StatsRepository {

    void updateNmtDate(Map<String, Long> data);

    void updateLogs(List<String> logs);

    void updatePmap(List<String> pmap);

    void updateProcesses(List<String> processes);


    Map<String, Long> getNmtData();

    List<String> getLogs();

    List<String> getPmap();

    List<String> getStats();

    List<String> getProcesses();

    void updateDockerStats(List<String> data);

}
