package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.io.tikzOutput.TikzExport;
import de.rwth.i2.attestor.main.Attestor;
import de.rwth.i2.attestor.main.scene.DefaultScene;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.seplog.InductivePredicatesParser;

import java.io.IOException;

public class TestGrammars {

    public static void main(String[] args) {
        try {
            Grammar grammar = getSeparationLogicGrammar("SimpleDLL");
            TikzExport exporter = new TikzExport("reports/test_grammar.tex", true);
            exporter.exportGrammar(grammar, "LinkedTree", true);
            exporter.finishExport();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

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
