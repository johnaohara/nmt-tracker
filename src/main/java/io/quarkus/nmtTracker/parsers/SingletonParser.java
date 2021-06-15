package io.quarkus.nmtTracker.parsers;

import java.util.ArrayList;
import java.util.List;

public class SingletonParser  extends OutputParser<String>{
    private volatile String value = null;
    private volatile boolean hasError = false;
    private List<String> errors;
    @Override
    public String output() {
        return value;
    }

    @Override
    public List<String> errors() {
        return null;
    }

    @Override
    public Boolean hasErrors() {
        return hasError;
    }

    @Override
    public void accept(String s) {
        if(value == null ) {
            value = s;
        } else {
            errors = new ArrayList<>();
            errors.add("Value has already been set");
            hasError = true;
        }
    }
}
