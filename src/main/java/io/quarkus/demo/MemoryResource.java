package io.quarkus.demo;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/process")
public class MemoryResource {

    @Inject
    NmtUtil nmtUtil;

    @Inject
    NmtRepository nmtRepository;

    @GET
    @Path("/pids")
    @Produces(MediaType.TEXT_PLAIN)
    public List<Long> getPids() {
        return nmtUtil.getPids();
    }

    @GET
    @Path("/pid")
    @Produces(MediaType.TEXT_PLAIN)
    public Long getPid() throws Exception {
        List<Long> pids = nmtUtil.getPids();
        if (pids.size() != 1){
            throw new Exception("Could not determine pid of exactly 1 process!");
        }
        return pids.get(0);
    }

    @GET
    @Path("/nmt")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Long> getNmtData() throws Exception {
        return nmtRepository.getNmtData();
    }
}