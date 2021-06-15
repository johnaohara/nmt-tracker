package io.quarkus.nmtTracker.parsers;

import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NmtParser extends OutputParser<Map<String, Long>> {

    private static final Logger LOG = Logger.getLogger(NmtParser.class);

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

    private Map<String, Long> nmtSections = new HashMap<>();
    private List<String> errors = new ArrayList<>();


    @Override
    public void accept(String line) {
        if (line.contains(_KB)) {
            List<String> matchedSections = Arrays.stream(sections).filter(section -> line.contains(section)).collect(Collectors.toList());
            if (matchedSections.size() > 1) {
//                throw new NmtLineParseException("Matched multiple sections");
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

    @Override
    public Map<String, Long> output() {
        return nmtSections;
    }

    @Override
    public  List<String> errors() {
        return this.errors;
    }

    @Override
    public Boolean hasErrors() {
        return this.errors.size() != 0;
    }
}
