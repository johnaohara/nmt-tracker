package io.quarkus.demo;

import io.quarkus.scheduler.Scheduled;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
class JcmdScheduler {

    private static final Logger LOG = Logger.getLogger(JcmdScheduler.class);

    @Inject
    NmtUtil nmtUtil;

    @Inject
    NmtRepository nmtRepository;

    @Scheduled(every = "0.5s", identity = "Jcmd-job")
    void schedule() {
        LOG.debug("Running JCMD");
        nmtUtil.getProcessNmtSections(results -> nmtRepository.updateNmtDate(results));
        ;

    }
}
