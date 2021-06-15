package io.quarkus.nmtTracker;

import io.quarkus.nmtTracker.environment.Environment;
import io.quarkus.nmtTracker.repository.StatsRepository;
import io.quarkus.scheduler.Scheduled;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
class StatsScheduler {

    private static final Logger LOG = Logger.getLogger(StatsScheduler.class);

    @Inject
    StatsRepository statsRepository;

    @Inject
    Environment environment;

    @Scheduled(every = "1s", identity = "stats-job")
    void gatherStats() {
        if (environment.isRunning()) {
            LOG.debug("Running JCMD");

            environment.getProcessNmtSections(results ->
                    statsRepository.updateNmtDate(results)
            );

            environment.getPmap(result ->
                    statsRepository.updatePmap(result)
            );

            environment.getProcesses(processes -> {
                statsRepository.updateProcesses(processes);
            });

        }
    }
}
