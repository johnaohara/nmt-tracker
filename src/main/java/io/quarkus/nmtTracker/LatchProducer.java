package io.quarkus.nmtTracker;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import java.util.concurrent.CountDownLatch;

@RequestScoped
public class LatchProducer {

    @Produces
    public CountDownLatch newLatch(){
        return new CountDownLatch(1);
    }
}
