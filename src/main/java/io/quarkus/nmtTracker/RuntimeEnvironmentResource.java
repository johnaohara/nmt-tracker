package io.quarkus.nmtTracker;

import io.quarkus.nmtTracker.environment.Environment;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Path("/environment")
public class RuntimeEnvironmentResource extends BaseResource {

    @Inject
    Environment environment;

    @GET
    @Path("/processes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> listProcesses() throws Exception {

        AtomicReference<List<String>> runningProcesses = new AtomicReference<>();

        environment.getProcesses(processes -> {
                    runningProcesses.set(processes);
                    latch.countDown();
                }
        );

        if (waitFor(latch)) {
            return runningProcesses.get();
        } else {
            throw new Exception("Timedout waiting for list of processes");
        }
    }



}
