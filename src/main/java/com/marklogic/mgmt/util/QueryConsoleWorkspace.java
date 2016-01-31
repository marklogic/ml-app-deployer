package com.marklogic.mgmt.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.marklogic.mgmt.ManageConfig;
import com.marklogic.rest.util.Fragment;
import com.marklogic.rest.util.RestTemplateUtil;

public class QueryConsoleWorkspace {

	//fix this regex to be meaningful
	private static final String EXPORTED_WS_XML_PATTERN = "<export>(.*?)</export>";
	private String workspaceName;
	private String userName;
	private String targetDirectory = "data/";
    private RestTemplate restTemplate;
	private ManageConfig config;
	private ClassLoader classLoader;
	private String evalEndPoint;
	private String pathToWsOnFileSystem = "data/";
	private static final String PORT = "8000";
	private static final String EXPORTER_XQY = "xqy/qc-workspace-exporter.xqy";
	private static final String IMPORTER_XQY = "xqy/qc-workspace-importer.xqy";
	
	public QueryConsoleWorkspace(){
		this.config = new ManageConfig();
		this.restTemplate = RestTemplateUtil.newRestTemplate(
				config.getHost(), 8000, config.getAdminUsername(),
				config.getAdminPassword());
		this.classLoader = getClass().getClassLoader();
		this.evalEndPoint = "http://" + config.getHost() + ":" + PORT + "/LATEST/eval";
	}
	
	public QueryConsoleWorkspace(String workspaceName, String userName, String  targetDirectory){
		this.workspaceName = workspaceName;
		this.userName = userName;
		this.setTargetDirectory(targetDirectory);
		this.config = new ManageConfig();
		this.restTemplate = RestTemplateUtil.newRestTemplate(
				config.getHost(), 8000, config.getAdminUsername(),
				config.getAdminPassword());
		this.classLoader = getClass().getClassLoader();
		this.evalEndPoint = "http://" + config.getHost() + ":" + PORT + "/LATEST/eval";
	}
	
	private ResponseEntity<Object> postRequest(String xqueryPath, String externalVars) {

		String xqy = fileToString(xqueryPath);
		
		LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("vars", externalVars);
		parts.add("xquery", xqy);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Arrays.asList(MediaType.parseMediaType("multipart/mixed")));
		//headers.setAccept(Arrays.asList(MediaType.));
				
		HttpEntity<LinkedMultiValueMap<String, Object>> request = new HttpEntity<LinkedMultiValueMap<String, Object>>(
				parts, headers);

		FormHttpMessageConverter formConverter = new FormHttpMessageConverter() {
		    @Override
		    public boolean canRead(Class<?> clazz, MediaType mediaType) {
		        if (clazz == Object.class) {
		            return true;
		        }
		        return super.canRead(clazz, mediaType);
		    }
		};
		
		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
		converters.add(formConverter);
		restTemplate.setMessageConverters(converters);
		ResponseEntity<Object> response = restTemplate.exchange(evalEndPoint,
				HttpMethod.POST, request, Object.class);
		return response;
	}
	
	public String exportQcWorkspace(String workspaceName){
		ResponseEntity<Object> response = postRequest(EXPORTER_XQY, "{\"user\":\"admin\",\"workspace\":\"" +workspaceName+ "\"}");
		String wsLocalFileName = null;
		try {
			wsLocalFileName = writeWorkspaceToLocalFile(response);
		} catch (IOException e) {
			e.printStackTrace();
			return "Workspace export failed." + e.getMessage();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return "Workspace export failed." + e.getMessage();
		}
		return wsLocalFileName; 
	}
	
	public String exportQcWorkspace(){
		Assert.notNull(this.workspaceName);
		return exportQcWorkspace(this.workspaceName);
	}

	public String importQcWorkspaceByXml(String workspaceContent) {
		ResponseEntity<Object> response =  postRequest(IMPORTER_XQY, "{\"user\":\"admin\",\"exported-workspace\":\"" +workspaceContent + "\"}");
		String body = response.getBody().toString();
		Matcher matcher = Pattern.compile("/workspaces/(.*?).xml").matcher(body);
		String newWorkspaceUri = matcher.find() == true ? (matcher.group(0)) : null;
		return newWorkspaceUri;				
		
	}
	
	public String importQcWorkspace(String workspaceName){
		String workspaceContent = fileToString(getFilePathAndNameForClassLoader(workspaceName));
		return importQcWorkspaceByXml(workspaceContent);
	}

	public String importQcWorkspace(){
		Assert.notNull(this.workspaceName);
		return importQcWorkspace(this.workspaceName);	
	}

	private String fileToString(String filePath) {
		URL url = classLoader.getResource(filePath);
		String path = url.getFile();
		File file = new File(path);
		String xqy = null;
		try {
			xqy = readInputStreamToString(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return xqy;
	}
	
	private String readInputStreamToString(InputStream in)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in,
				StandardCharsets.UTF_8));
		String str = null;
		StringBuilder sb = new StringBuilder(8192);
		while ((str = br.readLine()) != null) {
			sb.append(str);
		}
		return sb.toString();
	}

	private static void writeToFile(String fileName, String content) throws IOException {
		Path path = Paths.get(fileName);
		Files.write(path, content.getBytes());
}

	private String getFilePathAndName(String workspaceName) {
		//TODO fix this so it's not hardcoded; rather it should use the same path as class loader
		return "bin/" + targetDirectory + workspaceName + ".xml";
	}
	
	private String getFilePathAndNameForClassLoader(String workspaceName) {
		//TODO fix this so we don't have to futz with the path
		return targetDirectory + workspaceName + ".xml";
	}
	
	//TODO fix this clunky azz shiznit. I can't even.
	private String writeWorkspaceToLocalFile(ResponseEntity<Object> response2) throws IOException, URISyntaxException {
		//TODO use regex instead and make this more robust
		String body = response2.getBody().toString();
		String wsName;
		if (body.contains("No workspace found")) {
			wsName = "No workspace found";
		} else {
			//TODO why tf is the [ is getting put in here. 
			String start = body.split("X-Path: /export")[1];
			String next = start.split("--")[0].trim();
			String cleaned = next.replace("[", "");
			Fragment fragment = new Fragment(cleaned, new Namespace[] {});
			List<Element> elements = fragment.getElements("//export/workspace");
			wsName = elements.get(0).getAttribute("name").getValue();
			String wsPath = getFilePathAndName(wsName);
			writeToFile(wsPath, cleaned);
		}
		return wsName;
	}

//TODO why is this not matching EXPORTED_WS_XML_PATTERN?
	private String findPattern(String regex, String textToSearch) {
		Matcher matcher = Pattern.compile(regex).matcher(textToSearch);
		return matcher.find() == true ? (matcher.group(0)) : null;
	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPathToWsOnFileSystem() {
		return pathToWsOnFileSystem;
	}

	public void setPathToWsOnFileSystem(String pathToWsOnFileSystem) {
		this.pathToWsOnFileSystem = pathToWsOnFileSystem;
	}

	public String getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(String targetDirectory) {
		this.targetDirectory = targetDirectory;
	}
	
}
