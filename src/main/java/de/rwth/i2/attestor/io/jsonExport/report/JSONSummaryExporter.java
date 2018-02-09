package de.rwth.i2.attestor.io.jsonExport.report;

import de.rwth.i2.attestor.LTLFormula;
import de.rwth.i2.attestor.io.HttpExporter;
import de.rwth.i2.attestor.io.SummaryExporter;
import de.rwth.i2.attestor.main.AbstractPhase;
import de.rwth.i2.attestor.main.scene.Scene;
import de.rwth.i2.attestor.phases.communication.ModelCheckingSettings;
import de.rwth.i2.attestor.phases.modelChecking.ModelCheckingPhase;
import de.rwth.i2.attestor.phases.modelChecking.modelChecker.ModelCheckingResult;
import de.rwth.i2.attestor.phases.parser.CLIPhase;
import de.rwth.i2.attestor.stateSpaceGeneration.StateSpace;
import org.json.JSONStringer;
import org.json.JSONWriter;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by christina on 05.12.17.
 */
public class JSONSummaryExporter implements SummaryExporter {

    protected final HttpExporter httpExporter;

    public JSONSummaryExporter(HttpExporter httpExporter) {

        this.httpExporter = httpExporter;
    }

    public void exportForReport(Scene scene, StateSpace statespace, ModelCheckingPhase mcPhase, ModelCheckingSettings mcSettings, CLIPhase cliPhase, List<AbstractPhase> phases)  {

        JSONStringer jsonStringer = new JSONStringer();

        jsonStringer.array();

        writeSummary(jsonStringer, cliPhase, scene, statespace, mcPhase, mcSettings);

        writeRuntime(jsonStringer, phases);

        writeStateSpaceInfo(jsonStringer, scene, statespace);

        writeMessage(jsonStringer);

        writeMCResults(jsonStringer, mcPhase);

        jsonStringer.endArray();


        try {
            httpExporter.sendSummaryRequest(scene.getIdentifier(),jsonStringer.toString());
        } catch (UnsupportedEncodingException e) {
            // todo, json stringer returns wrong format, this should not happen!!
        }

    }

    private void writeMCResults(JSONWriter jsonWriter, ModelCheckingPhase mcPhase) {
        jsonWriter.object()
                .key("mcresults")
                .array();

        for (Map.Entry<LTLFormula, ModelCheckingResult> result : mcPhase.getLTLResults().entrySet()) {
            String modelCheckingResult = ModelCheckingResult.getString(result.getValue());
            jsonWriter.object()
                    .key("id")
                    .value(Objects.hashCode(result.getKey().getFormulaString()))
                    .key("formula")
                    .value(result.getKey().getFormulaString())
                    .key("satisfied")
                    .value(modelCheckingResult)
                    .endObject();
        }

        jsonWriter.endArray()
                .endObject();
    }

    private void writeMessage(JSONWriter jsonWriter) {

        jsonWriter.object()
                .key("messages")
                .array()
                .endArray()
                .endObject();
    }

    private void writeStateSpaceInfo(JSONWriter jsonWriter, Scene scene, StateSpace statespace) {
        jsonWriter.object()
                .key("stateSpace")
                .array()
                .object()
                .key("name")
                .value("w/ procedure calls")
                .key("states")
                .value(scene.getNumberOfGeneratedStates())
                .endObject()
                .object()
                .key("name")
                .value("w/o procedure calls")
                .key("states")
                .value(statespace.getStates().size())
                .endObject()
                .object()
                .key("name")
                .value("final states")
                .key("states")
                .value(statespace.getFinalStateIds().size())
                .endObject()
                .endArray()
                .endObject();

    }

    private void writeRuntime(JSONWriter jsonWriter, List<AbstractPhase> phases) {

        jsonWriter.object()
                .key("runtime")
                .array()
                .object()
                .key("phases")
                .array();

        int elapsedTotal = 0;
        int elapsedVerify = 0;
        // Generate JSON output and calculate total time
        for (AbstractPhase p : phases) {
            double elapsed = p.getElapsedTime();
            if(p.getName() != "Report output generation") {
                elapsedTotal += elapsed;

                jsonWriter.object()
                    .key("name")
                    .value(p.getName())
                    .key("time")
                    .value(elapsed)
                    .endObject();
                if (p.isVerificationPhase()) {
                    elapsedVerify += elapsed;
                }
            }
        }

        jsonWriter.endArray()
                .endObject()
                .object()
                .key("total")
                .array()
                .object()
                .key("name")
                .value("total")
                .key("time")
                .value(elapsedTotal)
                .endObject()
                .object()
                .key("name")
                .value("totalVerification")
                .key("time")
                .value(elapsedVerify)
                .endObject()
                .endArray()
                .endObject()
                .endArray()
                .endObject();
    }

    private void writeSummary(JSONWriter jsonWriter, CLIPhase cliPhase, Scene scene, StateSpace statespace, ModelCheckingPhase mcPhase, ModelCheckingSettings mcSettings) {
        jsonWriter.object()
                .key("summary")
                .array()
                .object()
                .key("numberStates")
                .value(scene.getNumberOfGeneratedStates())
                .endObject()
                .object()
                .key("numberTerminalStates")
                .value(statespace.getFinalStateIds().size())
                .endObject()
                .object()
                .key("numberFormulaeSuccess")
                .value(mcPhase.getNumberSatFormulae())
                .endObject()
                .object()
                .key("numberFormulaeFail")
                .value(mcSettings.getFormulae().size() - mcPhase.getNumberSatFormulae())

                .endObject()
                .endArray()
                .endObject()
        ;
    }

}
