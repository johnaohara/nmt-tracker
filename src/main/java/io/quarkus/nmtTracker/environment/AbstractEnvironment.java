package io.quarkus.nmtTracker.environment;

import io.quarkus.nmtTracker.parsers.ListParser;
import io.quarkus.nmtTracker.parsers.OutputParser;
import io.quarkus.nmtTracker.parsers.SingletonParser;
import io.vertx.core.Vertx;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public abstract class AbstractEnvironment implements Environment {

    public AbstractEnvironment(Vertx vertx) {
        this.vertx = vertx;
    }

    protected Vertx vertx;

    private static final Logger LOG = Logger.getLogger(AbstractEnvironment.class);

    protected AtomicReference<Boolean> runNmt = new AtomicReference<>(false);
    protected AtomicReference<String> curProcessExpr = new AtomicReference<>();
    protected AtomicReference<String> javaPid = new AtomicReference<>();

    public void stopNmt() {
        this.runNmt.set(false);
    }

    public void startNmt() {
        this.runNmt.set(true);
    }

    public Boolean isRunning() {
        return runNmt.get();
    }

    public void updateProcessExpr(String newProcExpr) {
        this.curProcessExpr.set(newProcExpr);
    }

    protected void executeListCommand(ProcessBuilder process, Consumer<List<String>> resultConsumer) {
        this.executeCommand(process, resultConsumer, new ListParser());
    }

    protected void executeSingletonCommand(ProcessBuilder process, Consumer<String> resultConsumer) {
        this.executeCommand(process, resultConsumer, new SingletonParser());
    }

    protected <T> void executeCommand(ProcessBuilder pmapProcess, Consumer<T> resultConsumer, OutputParser<T> parser) {
        executeBlocking(pmapProcess
                , parser
                , singletonOutput -> resultConsumer.accept(singletonOutput));
    }

    protected <T> void executeBlocking(ProcessBuilder processBuilder, OutputParser<T> parser, Consumer<T> resultsConsumer) {
        vertx.<OutputParser<T>>executeBlocking(fut -> {
                    processExecutor(processBuilder, line -> {
                        LOG.trace(line);
                        parser.accept(line);
                    });
                    if (!parser.hasErrors()) {
                        fut.complete(parser);
                    } else {
                        fut.fail(String.join(", ", parser.errors()));
                    }
                },
                fut -> {
                    if (fut.succeeded()) {
                        resultsConsumer.accept(fut.result().output());
                    } else {
                        LOG.error(fut.cause());
                    }
                });
    }

    protected void processExecutor(ProcessBuilder processBuilder, Consumer<String> lineConsumer) {
        Process p = null;
        BufferedReader reader = null;
        try {

            p = processBuilder.start();

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


}
