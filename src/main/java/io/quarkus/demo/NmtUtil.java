package io.quarkus.demo;

import io.quarkus.demo.err.NmtLineParseException;
import io.vertx.core.Vertx;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ApplicationScoped
public class NmtUtil {

    Vertx vertx;

    private static final Logger LOG = Logger.getLogger(NmtUtil.class);

    private static final Pattern _COMITTED_PATTERN = Pattern.compile("committed=(.*?)KB");
    private static final Pattern _RESERVED_PATTERN = Pattern.compile("reserved=(.*?)KB");
    private static final String _KB = "KB";

    private static String[] sections = {"File Name"
            , "Java Heap"
            , "Class"
            , "Thread"
            , "Code"
            , "GC"
            , "Compiler"
            , "Internal"
            , "Other"
            , "Symbol"
            , "Native Memory Tracking"
            , "Shared class space"
            , "Area Chunk"
            , "Logging"
            , "Arguments"
            , "Module"
            , "Synchronizer"
            , "Safepoint"
            , "Total"
    };

    private AtomicReference<String> curProcessExpr = new AtomicReference<>();
    private AtomicReference<String> dockerContainer = new AtomicReference<>();
    private AtomicReference<String> dockerJavaPid = new AtomicReference<>();
    private AtomicReference<Boolean> runNmt = new AtomicReference<>(false);

    @Inject
    public NmtUtil(@ConfigProperty(name = "nvm-tracker.process-expr") Optional<String> processExpr,
                   @ConfigProperty(name = "nvm-tracker.docker-container") Optional<String> dockerContainer,
                   Vertx vertx) {
        this.curProcessExpr.set(processExpr.orElse(""));
        this.dockerContainer.set(dockerContainer.orElse(""));
        this.vertx = vertx;
        if (!"".equals(this.dockerContainer)) {
            getDockerContainerJavaPID();
        }
    }

    public void updateProcessExpr(String newProcExpr) {
        this.curProcessExpr.set(newProcExpr);
    }

    public void updateDockerContainer(String newDockerContainer) {
        this.dockerContainer.set(newDockerContainer);
        getDockerContainerJavaPID();
    }

    private void getDockerContainerJavaPID() {
        vertx.executeBlocking(fut -> {
                    ProcessBuilder pidProcess = new ProcessBuilder("docker"
                            , "exec"
                            , this.dockerContainer.get()
                            , "ps"
                            , "-eaf"
                    );

                    processExecutor(pidProcess, line -> {
                        LOG.trace(line);
                        if (line.contains("java")) {
                            String pid = line.split("\\s+")[1];
                            dockerJavaPid.set(pid);
                        }
                    });
                    fut.complete();
                },
                result -> {
                    LOG.trace("Finsihed getting PID");
                });
    }

    public List<Long> getPids() {

        Long curPID = ProcessHandle.current().pid();
        AtomicReference<Long> parentPid = new AtomicReference<>((long) 0);
        ProcessHandle.current().parent().ifPresent(processHandle -> parentPid.set(processHandle.pid()));

        return ProcessHandle.allProcesses()
                .filter(process -> process.info().commandLine().orElse("").matches(curProcessExpr.get()))
                .filter(process -> process.pid() != curPID && process.pid() != parentPid.get())
                .map(process -> process.pid())
                .collect(Collectors.toList());
    }

    public void getProcessLogs(Consumer<List<String>> resultConsumer) {
        vertx.<List<String>>executeBlocking(fut -> {
                    if (runNmt.get() && dockerJavaPid != null && dockerJavaPid.get() != null) {
                        List<String> logs = new ArrayList();

                        ProcessBuilder jcmdProcess = new ProcessBuilder("docker"
                                , "logs"
                                , this.dockerContainer.get()
                        );

                        processExecutor(jcmdProcess, line -> {
                            LOG.trace(line);
                            logs.add(line);
                        });
                        fut.complete(logs);
                    } else {
                        fut.fail("Unable to parse jcmd result");
                    }
                },
                results -> {
                    if (results.succeeded()) {
                        resultConsumer.accept(results.result());
                    } else {
                        LOG.error(results.cause());
                    }
                });
    }

    public void getDockerStats(Consumer<List<String>> statsConsumer){
        vertx.<List<String>>executeBlocking(fut -> {
            if (runNmt.get() && dockerJavaPid != null && dockerJavaPid.get() != null) {
                List<String> statsOutput = new ArrayList();

                ProcessBuilder jcmdProcess = new ProcessBuilder("docker"
                        , "stats"
                        , this.dockerContainer.get()
                );

                processExecutor(jcmdProcess, line -> {
                    LOG.trace(line);
                    statsOutput.add(line);
                });
                fut.complete(statsOutput);
            } else {
                fut.fail("Unable to parse jcmd result");
            }
                },
                results -> {
                    if (results.succeeded()) {
                        statsConsumer.accept(results.result());
                    } else {
                        LOG.error(results.cause());
                    }
                });
    }

    public void getHostPID(Consumer<String> resultConsumer) {
        vertx.<String>executeBlocking(fut -> {
            if (runNmt.get() && dockerJavaPid != null && dockerJavaPid.get() != null) {
                AtomicReference<String> hostPid = new AtomicReference<>("");
                ProcessBuilder jcmdProcess = new ProcessBuilder("docker"
                        , "top"
                        , this.dockerContainer.get()
                );

                processExecutor(jcmdProcess, line -> {
                    LOG.trace(line);
                    if(line.contains("java")){
                        hostPid.set(line.split("\\s+")[1]);
                    }
                });
                fut.complete(hostPid.get());
            } else {
                fut.fail("Unable to parse jcmd result");
            }
                },
                results -> {
                    if (results.succeeded()) {
                        resultConsumer.accept(results.result());
                    } else {
                        LOG.error(results.cause());
                    }
                });

    }

    public void getPmap(String hostPid, Consumer<List<String>> resultConsumer){
        vertx.<List<String>>executeBlocking(fut -> {
                    if (runNmt.get() && dockerJavaPid != null && dockerJavaPid.get() != null) {
                        List<String> pmapOutput = new ArrayList();

                        ProcessBuilder jcmdProcess = new ProcessBuilder("sudo"
                                , "pmap"
                                , "-x"
                                , hostPid
                        );

                        processExecutor(jcmdProcess, line -> {
                            LOG.trace(line);
                            pmapOutput.add(line);
                        });
                        fut.complete(pmapOutput);
                    } else {
                        fut.fail("Unable to parse jcmd result");
                    }
                },
                results -> {
                    if (results.succeeded()) {
                        resultConsumer.accept(results.result());
                    } else {
                        LOG.error(results.cause());
                    }
                });
    }

        public void getProcessNmtSections(Consumer<Map<String, Long>> resultConsumer) {
        vertx.<Map<String, Long>>executeBlocking(fut -> {
                    if (runNmt.get() && dockerJavaPid != null && dockerJavaPid.get() != null) {

                        NmtUtil.NmtParser nmtParser = new NmtUtil.NmtParser();

                        ProcessBuilder jcmdProcess = new ProcessBuilder("docker"
                                , "exec"
                                , this.dockerContainer.get()
                                , "jcmd"
                                , dockerJavaPid.get()
                                , "VM.native_memory"
                        );

                        processExecutor(jcmdProcess, line -> {
                            LOG.trace(line);
                            try {
                                nmtParser.parseLine(line);
                            } catch (NmtLineParseException nmtLineParseException) {
                                LOG.warnf("There was a problem parsing the following line: `%s`", line);
                            }
                        });
                        fut.complete(nmtParser.getNmtSections());
                    } else {
                        fut.fail("Unable to parse jcmd result");
                    }

                },
                results -> {
                    if (results.succeeded()) {
                        resultConsumer.accept(results.result());
                    } else {
                        LOG.error(results.cause());
                    }
                });
    }

    private void processExecutor(ProcessBuilder jcmdProcess, Consumer<String> lineConsumer) {
        Process p = null;
        BufferedReader reader = null;
        try {

            p = jcmdProcess.start();

            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                lineConsumer.accept(line);
            }
        } catch (IOException exception) {
            LOG.errorf("IOException occurred obtaining native memory tracking data: %s", exception.getMessage());
        } finally {
            if (p != null) {
                try {
                    p.getInputStream().readAllBytes();
                    p.getInputStream().close();
                    p.getErrorStream().readAllBytes();
                    p.getErrorStream().close();
                    p.getOutputStream().close();
                } catch (IOException ioException) {
                    LOG.error(ioException.getMessage());
                }
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage());
                }
            }
        }

    }

    public void stopNmt() {
        this.runNmt.set(false);
    }

    public void startNmt() {
        this.runNmt.set(true);
    }


    public class NmtParser {

        private Map<String, Long> nmtSections;

        public NmtParser() {
            nmtSections = new HashMap<>();
        }

        public void parseLine(String line) throws NmtLineParseException {
            if (line.contains(_KB)) {
                List<String> matchedSections = Arrays.stream(sections).filter(section -> line.contains(section)).collect(Collectors.toList());
                if (matchedSections.size() > 1) {
                    throw new NmtLineParseException("Matched multiple sections");
                }
                if (matchedSections.size() < 1) {
                    LOG.debugf("Did not match any sections for %s\n", line);
                } else {

                    Matcher matcher = _COMITTED_PATTERN.matcher(line);
                    if (matcher.find()) {
                        nmtSections.put(matchedSections.get(0), Long.valueOf(matcher.group(1)));
                    }
                }

            }

        }

        public Map<String, Long> getNmtSections() {
            return nmtSections;
        }

    }

}
