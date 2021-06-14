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
    ProcessRepository nmtRepository;


    @Scheduled(every = "1s", identity = "nmt-job")
    void scheduleNmt() {
        LOG.debug("Running JCMD");
        nmtUtil.getProcessNmtSections(results ->
                nmtRepository.updateNmtDate(results)
        );

//        nmtUtil.getProcessLogs(results ->
//                nmtRepository.updateLogs(results)
//        );

        nmtUtil.getPmap(result ->
                nmtRepository.updatePmap(result)
        );

//        nmtUtil.getDockerStats(stats ->
//            nmtRepository.updateDockerStats(stats)
//        );

    }
}
