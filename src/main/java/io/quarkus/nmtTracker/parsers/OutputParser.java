package io.quarkus.nmtTracker.parsers;

import java.util.List;
import java.util.function.Consumer;

public abstract class OutputParser<K> implements Consumer<String> {
    public abstract K output();
    public abstract List<String> errors();
    public abstract Boolean hasErrors();
}
