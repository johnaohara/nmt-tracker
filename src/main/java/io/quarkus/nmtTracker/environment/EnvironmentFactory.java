package io.quarkus.nmtTracker.environment;

import io.quarkus.nmtTracker.environment.docker.DockerEnvironment;
import io.vertx.core.Vertx;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

public class EnvironmentFactory {

    static AtomicReference<Environment> environment = new AtomicReference<>(null);

    @Inject
    Vertx vertx;

    @Produces
    public Environment initialiseRepository(){ //TODO: make configurable
        if(environment.get() == null){
            environment.set(new DockerEnvironment(vertx));
        }
        return environment.get();
    }
}
