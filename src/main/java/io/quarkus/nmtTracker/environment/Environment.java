package io.quarkus.nmtTracker.environment;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface Environment {

    void getProcesses(Consumer<List<String>> processesConsumer);

    void getPmap(Consumer<List<String>> resultConsumer);

    void getPmapNew(String container, String pid, Consumer<List<String>> resultConsumer);

    void getProcessNmtSections(Consumer<Map<String, Long>> resultConsumer);

    void getProcessLogs(Consumer<List<String>> logsConsumer);

    void getStats(Consumer<List<String>> statsConsumer);

    void updateProcessExpr(String newProcExpr);

    void stopNmt();

    void startNmt();

    Boolean isRunning();
}
