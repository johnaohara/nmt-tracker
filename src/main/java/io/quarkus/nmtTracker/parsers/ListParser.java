package io.quarkus.nmtTracker.parsers;

import java.util.ArrayList;
import java.util.List;

public class ListParser extends OutputParser<List<String>>{

    private List<String> lines = new ArrayList<>();

    @Override
    public List<String> output() {
        return lines;
    }

    @Override
    public List<String> errors() {
        return null;
    }

    @Override
    public Boolean hasErrors() {
        return false;
    }

    @Override
    public void accept(String s) {
        lines.add(s);
    }
}
