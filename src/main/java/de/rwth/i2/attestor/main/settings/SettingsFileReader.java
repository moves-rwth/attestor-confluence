package de.rwth.i2.attestor.main.settings;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import de.rwth.i2.attestor.LTLFormula;
import de.rwth.i2.attestor.generated.lexer.LexerException;
import de.rwth.i2.attestor.generated.parser.ParserException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import de.rwth.i2.attestor.util.DebugMode;

/**
 * Populates {@link Settings} from a settings file.
 *
 * @author Hannah Arndt, Christoph, Christina
 */
public class SettingsFileReader {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LogManager.getLogger( "SettingsFileReader" );

    /**
     * The JSONObject obtained from parsing the provided JSON file.
     */
	private JSONObject jsonSettings;

    /**
     * Creates a SettingsFileReader to parse a JSON file.
     * @param file A JSON file that contains settings.
     */
	public SettingsFileReader( String file ) {
		Scanner scan;
		try {

			scan = new Scanner(new FileReader(file));
			StringBuilder str = new StringBuilder();
			while (scan.hasNext())
				str.append(scan.nextLine()).append("\n");
			scan.close();

			jsonSettings = new JSONObject(str.toString());
			Settings settings = Settings.getInstance();
			
			//this.input = settings.getJSONObject("input");
			//initializeOptionsFromJson( settings.getJSONObject("options") );
			//this.options = settings.getJSONObject("options");
			//this.output = settings.getJSONObject("output");
			
		} catch (FileNotFoundException e) {
		    logger.fatal(
		           "The settings file '" + file + "' could not be found. Execution is aborted.\n"
                    + "\tThe home directory of this tool is '" + System.getProperty("user.dir") + "'"
            );
			logger.fatal("Settings file '" + file + "' could not be found. Aborting.");
			System.exit(1);
		}
	}

    /**
     * Populates all input settings from the parsed settings file.
     * @param settings All settings.
     * @return The populated input settings.
     */
	public InputSettings getInputSettings( Settings settings ){
		JSONObject jsonInput = jsonSettings.getJSONObject( "input" );
		InputSettings input = settings.input();
		
		if( jsonInput.has( "defaultPath" ) ){
			input.setDefaultPath( jsonInput.getString( "defaultPath" ) );
		}
		
		JSONObject programSettings = jsonInput.getJSONObject( "program" );
		if( programSettings.has( "classpath" )){
			input.setClasspath( programSettings.getString( "classpath" )  );
		}else if( !jsonInput.has( "defaultPath" )){
			logger.error("You must define a default path or a classpath");
		}
		input.setClassName( programSettings.getString( "class" ) );
		input.setMethodName( programSettings.getString( "method" ) );
		
		JSONObject grammarSettings = jsonInput.getJSONObject( "grammar" );
		if( grammarSettings.has( "path" )){
			input.setPathToGrammar( grammarSettings.getString( "path" )  );
		}else if( !jsonInput.has( "defaultPath" )){
			logger.error("You must define a default path or a path for grammar");
		}
		input.setGrammarName( grammarSettings.getString( "file" ) );
		
		JSONObject initialSettings = jsonInput.getJSONObject( "initialState" );
		if( initialSettings.has( "path" ) ){
			input.setPathToInput( initialSettings.getString( "path" ) );
		}else if( !jsonInput.has( "defaultPath" )){
			logger.error("You must define a default path or a path for the initial state");
		}
		input.setInputName( initialSettings.getString( "file" ) );
		
		return input;
	}

    /**
     * Populates all option settings from the parsed settings file.
     * @param settings All settings.
     * @return The populated option settings.
     */
	public OptionSettings getOptionSettings( Settings settings ){
		JSONObject jsonOptions = jsonSettings.getJSONObject( "options" );
		OptionSettings options = settings.options();
		
		if(jsonOptions.has( "mode" )) {
			options.setIndexedMode( jsonOptions.get( "mode" ).equals( "indexed" ) );
		}

		DebugMode.ENABLED = jsonOptions.has("logging");

		if( jsonOptions.has( "depth" )) {
			options.setMinDereferenceDepth( jsonOptions.getInt( "depth" ) );
		}

		if( jsonOptions.has( "maximalStateSpace") ) {
			options.setMaxStateSpaceSize( jsonOptions.getInt( "maximalStateSpace" ) );
		}

		if( jsonOptions.has(  "maximalHeap" ) ) {
			options.setMaxStateSize( jsonOptions.getInt( "maximalHeap" ) );
		}

		if( jsonOptions.has( "aggressiveThreshold" )) {
			options.setAggressiveAbstractionThreshold( jsonOptions.getInt( "aggressiveThreshold" ));
		}

		if( jsonOptions.has( "aggressiveReturn" ) ){
			options.setAggressiveReturnAbstraction( jsonOptions.getBoolean( "aggressiveReturn" ) );
		}

		if( jsonOptions.has( "removeDeadVariables" ) ){
			options.setRemoveDeadVariables( jsonOptions.getBoolean( "removeDeadVariables" ) );
		}
		
		return options;
	}

    /**
     * Populates all output settings from the parsed settings file.
     * @param settings All settings.
     * @return The populated output settings.
     */
	public OutputSettings getOutputSettings( Settings settings ){
		JSONObject jsonOutput = jsonSettings.getJSONObject( "output" );
		OutputSettings output = settings.output();
		
		if( jsonOutput.has( "defaultPath" )){
			output.setDefaultPath( jsonOutput.getString( "defaultPath" ) );
		}
		
		if( jsonOutput.has( "stateSpace" ) ){
			output.setExportStateSpace( true );
			JSONObject jsonStateSpace = jsonOutput.getJSONObject( "stateSpace" );
			if( jsonStateSpace.has( "path" ) ){
				output.setPathForStateSpace( jsonStateSpace.getString( "path" ) );
			}
			if( jsonStateSpace.has( "folder" ) ){
				output.setFolderForStateSpace( jsonStateSpace.getString( "folder" ) );
			}
		}
		
		if( jsonOutput.has( "terminalStates" ) ){
			output.setExportTerminalStates( true );
			JSONObject jsonTerminalStates = jsonOutput.getJSONObject( "terminalStates" );
			if( jsonTerminalStates.has( "path" ) ){
				output.setPathForTerminalStates( jsonTerminalStates.getString( "path" ) );
			}
			if( jsonTerminalStates.has( "folder" ) ){
				output.setPathForTerminalStates( jsonTerminalStates.getString( "folder" ) );
			}
		}
		
		if( jsonOutput.has( "grammar" ) ){
			output.setExportGrammar( true );
			JSONObject jsonGrammar = jsonOutput.getJSONObject( "grammar" );
			if( jsonGrammar.has( "path" ) ){
				output.setPathForGrammar( jsonGrammar.getString( "path" ) );
			}
			if( jsonGrammar.has( "folder" ) ){
				output.setFolderForGrammar( jsonGrammar.getString( "folder" ) );
			}
		}
		
		if( jsonOutput.has( "bigStates" ) ){
			output.setExportBigStates( true );
			JSONObject jsonBS = jsonOutput.getJSONObject( "bigStates" );
			output.exportBigStatesThreshold( jsonBS.getInt( "threshold" ) );
			if( jsonBS.has( "path" ) ){
				output.setPathForBigStates( jsonBS.getString( "path" ) );
			}
			if( jsonBS.has( "folder" ) ){
				output.setFolderForBigStates( jsonBS.getString( "folder" ) );
			}
		}
		
		return output;
	}

	/**
	 * Populates the model checking settings with the input from the parsed settings file.
	 * @param settings all settings
	 * @return the populated model checking settings
	 */
	public ModelCheckingSettings getMCSettings(Settings settings){
		JSONObject jsonMC = jsonSettings.getJSONObject( "modelChecking" );
		ModelCheckingSettings mc = settings.modelChecking();

		if( jsonMC.has( "enabled" )){
			mc.setModelCheckingEnabled(jsonMC.getBoolean("enabled"));
		}
		if( jsonMC.has("formulae")){
			String formulaeString = jsonMC.getString("formulae");
			for(String formula : formulaeString.split(",")){
				try {
					mc.addFormula(new LTLFormula(formula));
				} catch (Exception e) {
					logger.log(Level.WARN, "The input " + formula + " is not a valid LTL formula. Skipping it.");

				}
			}
		}

		return mc;
	}
	
}
