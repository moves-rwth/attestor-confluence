package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.util.Pair;

import java.util.Collection;

public interface TikzReporter {
    Collection<Pair<String, TikzReporter>> getTikzExporter();
}
