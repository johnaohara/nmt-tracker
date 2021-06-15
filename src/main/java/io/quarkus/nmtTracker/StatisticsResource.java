package io.quarkus.nmtTracker;

import io.quarkus.nmtTracker.environment.Environment;
import io.quarkus.nmtTracker.repository.StatsRepository;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Path("/stats")
public class StatisticsResource extends BaseResource {

    @Inject
    StatsRepository statsRepository;

    @Inject
    Environment environment;

    @GET
    @Path("/logs")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getLogs() throws Exception {
        return statsRepository.getLogs();
    }

    @GET
    @Path("/pmap/direct/{server}/{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getNewPmap(@PathParam("server") String server, @PathParam("pid") String pid ) throws Exception {
        AtomicReference<List<String>> pmapOut = new AtomicReference<>();

        environment.getPmapNew(server, pid, pmapOutput -> {
                    pmapOut.set(pmapOutput);
                    latch.countDown();
                }
        );

        if (waitFor(latch, 10, TimeUnit.SECONDS)) {
            return pmapOut.get();
        } else {
            throw new Exception("Timedout waiting for pmap output");
        }
    }

    @GET
    @Path("/pmap")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getPmap() throws Exception {
        return statsRepository.getPmap();
    }

    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getDockerStats() throws Exception {
        return statsRepository.getStats();
    }

    @GET
    @Path("/nmt")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Long> getNmtData() throws Exception {
        return statsRepository.getNmtData();
    }

    @GET
    @Path("/processes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> listProcesses() throws Exception {
        return  statsRepository.getProcesses();
    }

}