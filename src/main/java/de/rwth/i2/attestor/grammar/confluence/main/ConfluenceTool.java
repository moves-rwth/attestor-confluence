package de.rwth.i2.attestor.grammar.confluence.main;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.main.AbstractAttestor;
import de.rwth.i2.attestor.phases.parser.ParseGrammarPhase;

public class ConfluenceTool extends AbstractAttestor {
    @Override
    protected void registerPhases(String[] args) throws Exception {
        registry
                .addPhase(new ConfluenceCommandLinePhase(scene, args))
                .addPhase(new ParseGrammarPhase(scene))
                .execute();
    }

    public static Grammar parseGrammar(String defaultGrammar) {
        ConfluenceTool confluenceTool = new ConfluenceTool();
        confluenceTool.run(new String[]{defaultGrammar});
        ParseGrammarPhase parseGrammarPhase = (ParseGrammarPhase) confluenceTool.registry.getPhases().get(1);
        return parseGrammarPhase.getGrammar();
    }
}
