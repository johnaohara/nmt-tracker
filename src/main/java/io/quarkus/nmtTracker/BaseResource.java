package io.quarkus.nmtTracker;

import javax.inject.Inject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class BaseResource {

    @Inject
    protected CountDownLatch latch;

    protected boolean waitFor(CountDownLatch latch) {
        return this.waitFor(latch, 3, TimeUnit.SECONDS);
    }

    protected boolean waitFor(CountDownLatch latch, long timeout, TimeUnit timeUnit) {

        boolean success = false;
        try {
            success = latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return success;
    }
}
