package com.rjrudin.marklogic.mgmt;

import org.junit.Test;

import com.rjrudin.marklogic.rest.util.Fragment;

public class ManageRestApisTest extends AbstractMgmtTest {
	
	@Test
	public void getRestApiConfigTest() {
		 Fragment frag = manageClient.getXml("/v1/rest-apis", "rapi", "http://marklogic.com/rest-api");
		 assertTrue(frag.elementExists("//rapi:name[text() = 'App-Services']"));
	}

}
