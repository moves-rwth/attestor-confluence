package de.rwth.i2.attestor.grammar.confluence.main;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.ConfluenceWrapperGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionPhase;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.ConfluenceCheckPhase;
import de.rwth.i2.attestor.main.AbstractAttestor;
import de.rwth.i2.attestor.main.Attestor;
import de.rwth.i2.attestor.main.scene.DefaultScene;
import de.rwth.i2.attestor.phases.parser.ParseGrammarPhase;

public class ConfluenceTool extends AbstractAttestor {
    @Override
    protected void registerPhases(String[] args) throws Exception {
        registry
                .addPhase(new ConfluenceCommandLinePhase(scene, args))
                .addPhase(new ParseGrammarPhase(scene))
                .addPhase(new ConfluenceCheckPhase(scene))
                .addPhase(new CompletionPhase(scene))
                .addPhase(new ConfluenceOutputPhase(scene))
                .execute();
    }

    public static Grammar parsePredefinedGrammar(String grammarName) {
        ParseGrammarPhase parseGrammarPhase = new ParseGrammarPhase(new DefaultScene());
        parseGrammarPhase.loadGrammarFromURL(Attestor.class.getClassLoader()
                .getResource("predefinedGrammars/" + grammarName + ".json"));
        return parseGrammarPhase.getGrammar();
    }

    public static ConfluenceWrapperGrammar parseGrammar(String grammarName) {
        return new ConfluenceWrapperGrammar(parsePredefinedGrammar(grammarName), grammarName);
    }
}
