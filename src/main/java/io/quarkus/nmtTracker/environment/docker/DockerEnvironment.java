package io.quarkus.nmtTracker.environment.docker;

import io.quarkus.nmtTracker.environment.AbstractEnvironment;
import io.quarkus.nmtTracker.parsers.NmtParser;
import io.vertx.core.Vertx;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DockerEnvironment extends AbstractEnvironment {

    private static final Logger LOG = Logger.getLogger(DockerEnvironment.class);

    public DockerEnvironment(Vertx vertx) {
        super(vertx);
    }

    @Override
    public void getProcesses(Consumer<List<String>> resultConsumer) {
        executeListCommand(
                new ProcessBuilder(
                        "docker"
                        , "ps"
                        , "--format"
                        , "{{.Names}}"
                )
                , resultConsumer);
    }

    @Override
    public void getPmapNew(String container, String pid, Consumer<List<String>> resultConsumer) {
        executeListCommand(
                new ProcessBuilder(
                        "docker"
                        , "exec"
                        , container
                        , "pmap"
                        , "-x"
                        , pid
                )
                , resultConsumer);
    }

    public void getPmap(Consumer<List<String>> resultConsumer) {

        executeListCommand(
                new ProcessBuilder(
                        "docker"
                        , "exec"
                        , curProcessExpr.get()
                        , "pmap"
                        , "-x"
                        , javaPid.get()
                )
                , resultConsumer);
    }


    public void updateProcessExpr(String newDockerContainer) {
        super.updateProcessExpr(newDockerContainer);
        getDockerContainerJavaPID();
    }


    public void getProcessNmtSections(Consumer<Map<String, Long>> resultConsumer) {
        ProcessBuilder jcmdProcess = new ProcessBuilder("docker"
                , "exec"
                , this.curProcessExpr.get()
                , "jcmd"
                , javaPid.get()
                , "VM.native_memory"
        );

        this.executeCommand(jcmdProcess, resultConsumer,new NmtParser());
    }

    public void getProcessLogs(Consumer<List<String>> resultConsumer) {
        executeListCommand(
                new ProcessBuilder("docker"
                        , "logs"
                        , this.curProcessExpr.get()
                )
                , resultConsumer);
    }

    public void getStats(Consumer<List<String>> statsConsumer) {
        executeListCommand(
                new ProcessBuilder("docker"
                        , "stats"
                        , this.curProcessExpr.get()
                )
                , statsConsumer);
    }

    private void getDockerContainerJavaPID() {

        executeListCommand(
                new ProcessBuilder("docker"
                        , "exec"
                        , this.curProcessExpr.get()
                        , "ps"
                        , "-eaf"
                )
                , procs -> {
                    List<String> filterdProcs = procs.stream().filter(proc -> proc.contains("java")).collect(Collectors.toList());
                    if(filterdProcs.size() > 1){
                        LOG.error("Found more than one java process running in container: ".concat(this.curProcessExpr.get()));
                    }
                    javaPid.set(filterdProcs.get(0).split("\\s+")[1]);
                });
    }
}
