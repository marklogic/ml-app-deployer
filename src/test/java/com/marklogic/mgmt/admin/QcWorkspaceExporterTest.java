package com.marklogic.mgmt.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.mgmt.util.QueryConsoleWorkspace;
import com.marklogic.mgmt.util.QueryConsoleWorkspaceManager;
import com.marklogic.xcc.template.XccTemplate;


public class QcWorkspaceExporterTest extends AbstractAppDeployerTest {

	private String defaultPathToWsOnFileSystem = "data/";
	private List<String> urisToDelete = new ArrayList<String>();
	private List<String> filesToDelete = new ArrayList<String>();
	private XccTemplate xcc;
	
	@Before
	public void setUp() {
		xcc = new XccTemplate(format("xcc://%s:%s@%s:8000/%s", appConfig.getRestAdminUsername(),
	            appConfig.getRestAdminPassword(), appConfig.getHost(), "App-Services"));
	}
	
	@After
	public void tearDown() {
		deleteFiles(filesToDelete);
		deleteUris(urisToDelete);
	}
	
	//@Test
	public void testImportExport() {

		//make sure a workspace exists in QC to test
		QueryConsoleWorkspace qcwm = new QueryConsoleWorkspace();
		String newWorkspaceName = getNewWorkspaceName();
		
		String newWorkspaceUri = qcwm.importQcWorkspaceByXml(
					getSampleExportedWorkspace(newWorkspaceName));
	
		assertNotNull(newWorkspaceUri);
		assertTrue(newWorkspaceUri.contains("/workspaces/"));
		
		//test exporting the workspace we just created
		String exportedWorkspaceName = QueryConsoleWorkspaceManager.exportWorkspace(newWorkspaceName);
		assertTrue(exportedWorkspaceName.contains("Workspace"));
		logger.info("exported workspace: " + exportedWorkspaceName);
		
		//delete the workspace from query console now that it's been exported to file system
		deleteUris(Arrays.asList(newWorkspaceUri));
		
		//import the workspace into query console
		String importedWsName = QueryConsoleWorkspaceManager.importWorkspace(newWorkspaceName);
		assertTrue(importedWsName.contains(newWorkspaceUri));
		logger.info("imported workspace: " +importedWsName);
		
		//ensure workspace is in query console
		String workspace = xcc.executeAdhocQuery("xquery version '1.0-ml'; fn:doc("+newWorkspaceUri+")");
		assertTrue(workspace.contains("query name='Query 1'"));
		
		filesToDelete.add(getFilePathAndName(exportedWorkspaceName));
		urisToDelete.add(importedWsName);
		
	}

    @Test
	public void testExport() {
		QueryConsoleWorkspace qcwm = new QueryConsoleWorkspace();
		
		String response = qcwm.exportQcWorkspace("No Workspace");
		assertEquals(response, "No workspace found");
		
		String wsNameOnFileSystem = qcwm.exportQcWorkspace("Workspace");
		assertTrue(wsNameOnFileSystem.contains("Workspace"));
		
		filesToDelete.add(getFilePathAndName(wsNameOnFileSystem));
		logger.info("Workspace has been saved as " + wsNameOnFileSystem);
	}

	
	@Test
	public void testImport() {
		QueryConsoleWorkspace qcwm = new QueryConsoleWorkspace();
		String wsUri = qcwm.importQcWorkspaceByXml(getSampleExportedWorkspace(getNewWorkspaceName()));
		logger.info(wsUri);
		assertTrue(wsUri.contains("/workspaces/"));
		urisToDelete.add(wsUri);
}

	private String getSampleExportedWorkspace(String newWsName) {
		String xml = "<export><workspace name='" +newWsName+"'><query name='Query 1' focus='false' active='true' mode='xquery'>xquery version '1.0-ml';"+
		"cts:uris((), (), cts:word-query('test'))</query><query name='Query 2' focus='true' active='true' mode='xquery'>xquery version '1.0-ml';"+
		"fn:doc('/foo/doc2.xml')</query></workspace></export>";
		return xml;
	}

	private String getNewWorkspaceName() {
		return "Workspace"+ (int)(Math.random() * 1000000000);
	}

	private String getFilePathAndName(String workspaceName) {
		//TODO fix this so it's not hardcoded; rather it should use the same path as class loader
		return "bin/" + defaultPathToWsOnFileSystem + workspaceName + ".xml";
	}

	private void deleteFiles(List<String> filesToDelete) {
		for (String path : filesToDelete) {
			logger.info("path: " + path);
			try {
				Files.deleteIfExists(Paths.get(path));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void deleteUris(List<String> uris) {
		for (String uri : uris) {
			logger.info("deleting uri: " + uri);
			xcc.executeAdhocQuery( "xquery version '1.0-ml'; "
					+ "if (fn:doc('"+uri+"')) then (xdmp:document-delete('"+uri+"')) else () ");
		}
	}
	
}
