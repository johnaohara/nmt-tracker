package io.quarkus.nmtTracker.environment.bareMetal;

import io.quarkus.nmtTracker.environment.AbstractEnvironment;
import io.quarkus.nmtTracker.environment.Environment;
import io.vertx.core.Vertx;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BareMetalEnvironment extends AbstractEnvironment {

    public BareMetalEnvironment(Vertx vertx) {
        super(vertx);
    }

    public List<String> getProcesses() {
        Long curPID = ProcessHandle.current().pid();
        AtomicReference<Long> parentPid = new AtomicReference<>((long) 0);
        ProcessHandle.current().parent().ifPresent(processHandle -> parentPid.set(processHandle.pid()));

        return ProcessHandle.allProcesses()
                .filter(process -> process.info().commandLine().orElse("").matches(curProcessExpr.get()))
                .filter(process -> process.pid() != curPID && process.pid() != parentPid.get())
                .map(process -> Long.toString(process.pid()))
                .collect(Collectors.toList());
    }

    @Override
    public void getProcesses(Consumer<List<String>> processesConsumer) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void getPmap(Consumer<List<String>> resultConsumer) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void getPmapNew(String container, String pid, Consumer<List<String>> resultConsumer) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void getProcessNmtSections(Consumer<Map<String, Long>> resultConsumer) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void getProcessLogs(Consumer<List<String>> resultConsumer) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void getStats(Consumer<List<String>> statsConsumer) {
        throw new RuntimeException("Not yet implemented");
    }
}
