package io.quarkus.nmtTracker;

import io.quarkus.nmtTracker.environment.Environment;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/control")
public class ControlResource {

    @Inject
    Environment environment;

    @GET
    @Path("/nmt/stop")
    @Produces(MediaType.APPLICATION_JSON)
    public String stopNmt() throws Exception {
        environment.stopNmt();
        return "{'Response': 'ok'}";
    }

    @GET
    @Path("/nmt/start")
    @Produces(MediaType.APPLICATION_JSON)
    public String startNmt() throws Exception {
        environment.startNmt();
        return "{'Response': 'ok'}";
    }

    @GET
    @Path("/processExpr/{procExpr}")
    @Produces(MediaType.TEXT_PLAIN)
    public String updateProcExpr(@PathParam("procExpr") String newProcExpr){
        environment.updateProcessExpr(newProcExpr);
        return "new process expr: " + newProcExpr;
    }

}