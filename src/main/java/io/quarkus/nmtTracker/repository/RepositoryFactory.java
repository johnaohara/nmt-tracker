package io.quarkus.nmtTracker.repository;

import javax.enterprise.inject.Produces;
import java.util.concurrent.atomic.AtomicReference;

public class RepositoryFactory {

    static AtomicReference<StatsRepository> statsRepository = new AtomicReference<>(null);

    @Produces
    public StatsRepository initialiseRepository(){
        if(statsRepository.get() == null){
            statsRepository.set(new ProcessRepository());
        }
        return statsRepository.get();
    }
}
