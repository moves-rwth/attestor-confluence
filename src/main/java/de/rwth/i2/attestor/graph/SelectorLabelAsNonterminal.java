package de.rwth.i2.attestor.graph;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents the label of a selector edge. It has to implement the Nonterminal interface so it can be
 * used as a label for an edge in the Graph interface.
 * Because it is a selector it must have a rank of 2 and the 0th tentacle is not a reduction tentacle because there
 * already is an outgoing selector (the selector itself).
 */
public class SelectorLabelAsNonterminal implements Nonterminal {
    /**
     * The label of the selector.
     */
    private final String label;

    private SelectorLabelAsNonterminal(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int getRank() {
        return 2;
    }

    @Override
    public boolean isReductionTentacle(int tentacle) {
        return tentacle == 1;
    }

    @Override
    public void setReductionTentacle(int tentacle) {
        throw new UnsupportedOperationException("Cannot set reduction tentacle of selector");
    }

    @Override
    public void unsetReductionTentacle(int tentacle) {
        throw new UnsupportedOperationException("Cannot unset reduction tentacle of selector");
    }


    public static final class Factory {

        private final Map<String, SelectorLabelAsNonterminal>
                knownSelectors = new LinkedHashMap<>();

        public SelectorLabelAsNonterminal get(String name) {

            if (!knownSelectors.containsKey(name)) {
                throw new IllegalArgumentException("Requested selector does not exist. Requested was "
                        + name + ". Known selectors are: " + knownSelectors);
            }
            return knownSelectors.get(name);
        }

        /**
         * Method to create selectors. If the selector already exists, a reference to the existing one
         * will be returned.
         *
         * @param label               The label of the requested selector.
         * @return The requested selector. If this object does not exist, it will be created first.
         */
        public SelectorLabelAsNonterminal create(String label) {

            SelectorLabelAsNonterminal res;
            if (!knownSelectors.containsKey(label)) {
                res = new SelectorLabelAsNonterminal(label);
                knownSelectors.put(label, res);
            } else {
                res = knownSelectors.get(label);
            }
            return res;
        }
    }
}
