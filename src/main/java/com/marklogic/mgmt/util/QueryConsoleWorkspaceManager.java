package com.marklogic.mgmt.util;


public class QueryConsoleWorkspaceManager {

	//move to properties file
	private static String defaultTargetDirectory = "data/";
	private static String defaultUserName = "admin";


	public static String exportWorkspace(String workspaceName) {
		exportWorkspace(workspaceName, defaultUserName, defaultTargetDirectory);
		return workspaceName;
	}
	
	public static String importWorkspace(String workspaceName) {
		importWorkspace(workspaceName, defaultUserName, defaultTargetDirectory);
		return workspaceName;
	}
	
	public static String exportWorkspace(String workspaceName, String userName) {
		exportWorkspace(workspaceName, userName, defaultTargetDirectory);
		return workspaceName;
	}
	
	public static String importWorkspace(String workspaceName, String userName) {
		importWorkspace(workspaceName, userName, defaultTargetDirectory);
		return workspaceName;
	}
	
	public static String exportWorkspace(String workspaceName, String userName, String targetDirectory) {
		QueryConsoleWorkspace qcws = new QueryConsoleWorkspace(workspaceName, userName, targetDirectory);
		return qcws.exportQcWorkspace();
	}
	
	public static String importWorkspace(String workspaceName, String userName, String targetDirectory) {
		QueryConsoleWorkspace qcws = new QueryConsoleWorkspace(workspaceName, userName, targetDirectory);
		return qcws.importQcWorkspace();
	}
	
}
