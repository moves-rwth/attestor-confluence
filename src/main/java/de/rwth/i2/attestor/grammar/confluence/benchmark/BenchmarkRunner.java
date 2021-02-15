package de.rwth.i2.attestor.grammar.confluence.benchmark;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.GrammarBuilder;
import de.rwth.i2.attestor.grammar.ConfluenceWrapperGrammar;
import de.rwth.i2.attestor.main.Attestor;
import de.rwth.i2.attestor.main.scene.DefaultScene;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.seplog.InductivePredicatesParser;

import java.io.IOException;

public class BenchmarkRunner {


    public static Grammar getSeparationLogicGrammar(String grammarName) throws IOException {
        DefaultScene scene = new DefaultScene();
        SceneObject sceneObject = new SceneObject(scene) {};
        InductivePredicatesParser parser = new InductivePredicatesParser(sceneObject);
        Grammar grammarWithoutCollapsedRules = parser.parseFromUrl(
                Attestor.class.getClassLoader().getResource("confluenceTestGrammars/" + grammarName + ".sid")
        );
        GrammarBuilder grammarBuilder = Grammar.builder();
        grammarBuilder.addRules(grammarWithoutCollapsedRules);
        grammarBuilder.updateCollapsedRules();
        return grammarBuilder.build();
    }

    public static ConfluenceWrapperGrammar getSeparationLogicNamedGrammar(String grammarName) throws IOException {
        return new ConfluenceWrapperGrammar(getSeparationLogicGrammar(grammarName), grammarName);
    }
}
