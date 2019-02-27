package de.rwth.i2.attestor.grammar.confluence.main;

import de.rwth.i2.attestor.main.AbstractAttestor;
import de.rwth.i2.attestor.main.Attestor;

public class Main {

    public static void main(String[] args) {

        AbstractAttestor main = new ConfluenceTool();
        main.run(args);
    }
}
