package de.rwth.i2.attestor.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import de.rwth.i2.attestor.UnitTestGlobalSettings;
import de.rwth.i2.attestor.main.settings.Settings;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.GeneralNonterminal;

public class TestJsonToIndexedHC {


	@BeforeClass
	public static void init() {

		UnitTestGlobalSettings.reset();
		Settings.getInstance().options().setIndexedMode(true);
	}

	@Before
	public void initClass() {
		GeneralNonterminal.getNonterminal("TestJson", 2, new boolean[]{false,false});
	}
	
	@Test
	public void testAnnotated() {
		String graphEncoding = "{\n"
				+"	\"nodes\":[\n"
				+"		{\n"
				+"			\"type\":\"type\",\n"
				+"			\"number\":2\n"
				+"		}\n"
				+"	],\n"
				+"	\"externals\":[],\n"
				+"	\"variables\":[],\n"
				+"	\"selectors\":[\n"
				+"		{\n"
				+"			\"label\":\"label\",\n"
				+"			\"annotation\":\"ann\",\n"
				+"			\"origin\":0,\n"
				+"			\"target\":1\n"
				+"		}\n"
				+"	],\n"
				+"	\"hyperedges\":[]\n"
				+"}";
		
		HeapConfiguration parsed 
			= JsonToIndexedHC.jsonToHC(new JSONObject(graphEncoding));
		
		assertEquals( ExpectedHCs.getExpected_Annotated(), parsed );
	}
	
	@Test
	public void testBottom() {
		String graphEncoding = "{\n"
				+"	\"nodes\":[\n"
				+"		{\n"
				+"			\"type\":\"type\",\n"
				+"			\"number\":2\n"
				+"		}\n"
				+"	],\n"
				+"	\"externals\":[],\n"
				+"	\"variables\":[],\n"
				+"	\"selectors\":[],\n"
				+"	\"hyperedges\":[\n"
				+"			{\n"
				+"			\"label\":\"TestJson\",\n"
				+"			\"index\":[\"Z\"],\n"
				+"			\"tentacles\":[0,1]\n"
				+"		}]\n"
				+"}\n";
		
		
		HeapConfiguration parsed 
		= JsonToIndexedHC.jsonToHC(new JSONObject(graphEncoding));
	
		assertEquals( ExpectedHCs.getExpected_Bottom(), parsed );
	}
	
	@Test
	public void testTwoElementStack() {
		String graphEncoding = "{\n"
				+"	\"nodes\":[\n"
				+"		{\n"
				+"			\"type\":\"type\",\n"
				+"			\"number\":2\n"
				+"		}\n"
				+"	],\n"
				+"	\"externals\":[],\n"
				+"	\"variables\":[],\n"
				+"	\"selectors\":[],\n"
				+"	\"hyperedges\":[\n"
				+"			{\n"
				+"			\"label\":\"TestJson\",\n"
				+"			\"index\":[\"s\",\"Z\"],\n"
				+"			\"tentacles\":[0,1]\n"
				+"		}]\n"
				+"}\n";
		
		HeapConfiguration parsed 
		= JsonToIndexedHC.jsonToHC(new JSONObject(graphEncoding));
	
		assertEquals( ExpectedHCs.getExpected_TwoElementStack(), parsed );
	}
	
	@Test
	public void testStackWithVar() {
		String graphEncoding = "{\n"
				+"	\"nodes\":[\n"
				+"		{\n"
				+"			\"type\":\"type\",\n"
				+"			\"number\":2\n"
				+"		}\n"
				+"	],\n"
				+"	\"externals\":[],\n"
				+"	\"variables\":[],\n"
				+"	\"selectors\":[],\n"
				+"	\"hyperedges\":[\n"
				+"			{\n"
				+"			\"label\":\"TestJson\",\n"
				+"			\"index\":[\"s\",\"()\"],\n"
				+"			\"tentacles\":[0,1]\n"
				+"		}]\n"
				+"}\n";
		
		HeapConfiguration parsed = JsonToIndexedHC.jsonToHC(new JSONObject(graphEncoding));
	
		assertEquals( ExpectedHCs.getExpected_StackWithVar(), parsed );
	}
	
	@Test
	public void testAbstractStack() {
		String graphEncoding = "{\n"
				+"	\"nodes\":[\n"
				+"		{\n"
				+"			\"type\":\"type\",\n"
				+"			\"number\":2\n"
				+"		}\n"
				+"	],\n"
				+"	\"externals\":[],\n"
				+"	\"variables\":[],\n"
				+"	\"selectors\":[],\n"
				+"	\"hyperedges\":[\n"
				+"			{\n"
				+"			\"label\":\"TestJson\",\n"
				+"			\"index\":[\"s\",\"_X\"],\n"
				+"			\"tentacles\":[0,1]\n"
				+"		}]\n"
				+"}\n";
		
		HeapConfiguration parsed = JsonToIndexedHC.jsonToHC(new JSONObject(graphEncoding));
	
		assertEquals( ExpectedHCs.getExpected_StackWithAbs(), parsed );
	}
	
	@Test
	public void testFail_AbstractStack() {
		try{
		String graphEncoding = "{\n"
				+"	\"nodes\":[\n"
				+"		{\n"
				+"			\"type\":\"type\",\n"
				+"			\"number\":2\n"
				+"		}\n"
				+"	],\n"
				+"	\"externals\":[],\n"
				+"	\"variables\":[],\n"
				+"	\"selectors\":[],\n"
				+"	\"hyperedges\":[\n"
				+"			{\n"
				+"			\"label\":\"TestJson\",\n"
				+"			\"index\":[\"_X\",\"s\"],\n"
				+"			\"tentacles\":[0,1]\n"
				+"		}]\n"
				+"}\n";
		
			JsonToIndexedHC.jsonToHC(new JSONObject(graphEncoding));
			fail("abstract index symbols may only occur at the end of index");
		}catch( AssertionError e ){
			//expected
		}
	}
	
	@Test
	public void testFail_Bottom() {
		try{
		String graphEncoding = "{\n"
				+"	\"nodes\":[\n"
				+"		{\n"
				+"			\"type\":\"type\",\n"
				+"			\"number\":2\n"
				+"		}\n"
				+"	],\n"
				+"	\"externals\":[],\n"
				+"	\"variables\":[],\n"
				+"	\"selectors\":[],\n"
				+"	\"hyperedges\":[\n"
				+"			{\n"
				+"			\"label\":\"TestJson\",\n"
				+"			\"index\":[\"Z\",\"s\"],\n"
				+"			\"tentacles\":[0,1]\n"
				+"		}]\n"
				+"}\n";
		
			JsonToIndexedHC.jsonToHC(new JSONObject(graphEncoding));
			fail("bottom index symbols may only occur at the end of index");
		}catch( AssertionError e ){
			//expected
		}
	}
	
	@Test
	public void testFail_StackVariable() {
		try{
		String graphEncoding = "{\n"
				+"	\"nodes\":[\n"
				+"		{\n"
				+"			\"type\":\"type\",\n"
				+"			\"number\":2\n"
				+"		}\n"
				+"	],\n"
				+"	\"externals\":[],\n"
				+"	\"variables\":[],\n"
				+"	\"selectors\":[],\n"
				+"	\"hyperedges\":[\n"
				+"			{\n"
				+"			\"label\":\"TestJson\",\n"
				+"			\"index\":[\"()\",\"Z\"],\n"
				+"			\"tentacles\":[0,1]\n"
				+"		}]\n"
				+"}\n";
		
			JsonToIndexedHC.jsonToHC(new JSONObject(graphEncoding));
			fail("variable index symbols may only occur at the end of index");
		}catch( AssertionError e ){
			//expected
		}
	}
	
	@Test
	public void testFail_noBottom() {
		try{
		String graphEncoding = "{\n"
				+"	\"nodes\":[\n"
				+"		{\n"
				+"			\"type\":\"type\",\n"
				+"			\"number\":2\n"
				+"		}\n"
				+"	],\n"
				+"	\"externals\":[],\n"
				+"	\"variables\":[],\n"
				+"	\"selectors\":[],\n"
				+"	\"hyperedges\":[\n"
				+"			{\n"
				+"			\"label\":\"TestJson\",\n"
				+"			\"index\":[\"s\",\"s\"],\n"
				+"			\"tentacles\":[0,1]\n"
				+"		}]\n"
				+"}\n";
		
			JsonToIndexedHC.jsonToHC(new JSONObject(graphEncoding));
			fail("abstract index symbols may only occur at the end of index");
		}catch( AssertionError e ){
			//expected
		}
	}
	

}
