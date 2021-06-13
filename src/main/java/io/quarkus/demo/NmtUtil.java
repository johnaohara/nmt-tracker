package io.quarkus.demo;

import io.quarkus.demo.err.NmtLineParseException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ApplicationScoped
public class NmtUtil {

    private static final Logger LOG = Logger.getLogger(NmtUtil.class);

    private static final Pattern _COMITTED_PATTERN = Pattern.compile("committed=(.*?)KB");
    private static final Pattern _RESERVED_PATTERN = Pattern.compile("reserved=(.*?)KB");
    private static final String _KB = "KB";

    private static String[] sections = {"File Name"
            , "Java Heap"
            , "Class"
            , "Thread"
            , "Code"
            , "GC"
            , "Compiler"
            , "Internal"
            , "Other"
            , "Symbol"
            , "Native Memory Tracking"
            , "Shared class space"
            , "Area Chunk"
            , "Logging"
            , "Arguments"
            , "Module"
            , "Synchronizer"
            , "Safepoint"
            , "Total"
    };

    private AtomicReference<String> curProcessExpr = new AtomicReference<>();

    @Inject
    public NmtUtil(@ConfigProperty(name = "nvm-tracker.process-expr") Optional<String> processExpr) {
        this.curProcessExpr.set(processExpr.orElse(""));
    }

    public void updateProcessExpr(String newProcExpr){
        this.curProcessExpr.set(newProcExpr);
    }

    public List<Long> getPids() {

        Long curPID = ProcessHandle.current().pid();
        AtomicReference<Long> parentPid = new AtomicReference<>((long) 0);
        ProcessHandle.current().parent().ifPresent(processHandle -> parentPid.set(processHandle.pid()));

        return ProcessHandle.allProcesses()
                .filter(process -> process.info().commandLine().orElse("").matches(curProcessExpr.get()))
                .filter(process -> process.pid() != curPID && process.pid() != parentPid.get())
                .map(process -> process.pid())
                .collect(Collectors.toList());
    }

    public Map<String, Long> getProcessNmtSections() {
        List<Long> quarkusPids = getPids();

        if (quarkusPids.size() < 1){
            LOG.error("Could not find process to track!");
            return null;
        }
        if ( quarkusPids.size() > 1) {
            LOG.error("Too many process running, could not determine which process to track!");
            return null;
        }

        NmtUtil.NmtParser nmtParser = new NmtUtil.NmtParser();

        ProcessBuilder jcmdProcess = new ProcessBuilder("jcmd", quarkusPids.get(0).toString(), "VM.native_memory");

        Process p = null;
        BufferedReader reader = null;
        try {

            p = jcmdProcess.start();

            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                LOG.trace(line);
                try {
                    nmtParser.parseLine(line);
                } catch (NmtLineParseException nmtLineParseException) {
                    LOG.warnf("There was a problem parsing the following line: `%s`", line);
                }

            }
        } catch (IOException exception) {
            LOG.errorf("IOException occurred obtaining native memory tracking data: %s", exception.getMessage());
        } finally {
            if (p != null) {
                try {
                    p.getInputStream().readAllBytes();
                    p.getInputStream().close();
                    p.getErrorStream().readAllBytes();
                    p.getErrorStream().close();
                    p.getOutputStream().close();
                } catch (IOException ioException) {
                    LOG.error(ioException.getMessage());
                }
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
        return nmtParser.getNmtSections();
    }

    public class NmtParser {

        private Map<String, Long> nmtSections;

        public NmtParser() {
            nmtSections = new HashMap<>();
        }

        public void parseLine(String line) throws NmtLineParseException {
            if (line.contains(_KB)) {
                List<String> matchedSections = Arrays.stream(sections).filter(section -> line.contains(section)).collect(Collectors.toList());
                if (matchedSections.size() > 1) {
                    throw new NmtLineParseException("Matched multiple sections");
                }
                if (matchedSections.size() < 1) {
                    LOG.debugf("Did not match any sections for %s\n", line);
                } else {

                    Matcher matcher = _COMITTED_PATTERN.matcher(line);
                    if (matcher.find()) {
                        nmtSections.put(matchedSections.get(0), Long.valueOf(matcher.group(1)));
                    }
                }

            }

        }

        public Map<String, Long> getNmtSections() {
            return nmtSections;
        }

    }

}
