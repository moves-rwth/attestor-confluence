package de.rwth.i2.attestor.grammar.confluence.benchmark;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.NamedGrammar;
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
        return parser.parseFromUrl(
                Attestor.class.getClassLoader().getResource("confluenceTestGrammars/" + grammarName + ".sid")
        );
    }

    public static NamedGrammar getSeparationLogicNamedGrammar(String grammarName) throws IOException {
        return new NamedGrammar(getSeparationLogicGrammar(grammarName), grammarName);
    }
}
