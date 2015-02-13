package de.lmu.ifi.bio.croco.cyto.util;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.LoggerFactory;

import de.lmu.ifi.bio.crco.connector.BufferedService;
import de.lmu.ifi.bio.crco.connector.QueryService;
import de.lmu.ifi.bio.crco.connector.RemoteWebService;

public class QueryServiceWrapper {
	
	private QueryService service;
	private String currentUrl = null;
	
	private QueryServiceWrapper (String remoteUrl) throws IOException, SQLException {

		File baseDir = new File(CytoscapeProperties.getProperties().get(CytoscapeProperties.baseDirStr).toString());
		if ( !baseDir.isDirectory()){
			boolean ret = baseDir.mkdir();
			if( !ret)
				throw new RuntimeException(String.format("%s is not a directory/ or can not be created. Change the %s propertie entry in the Cytoscape properties (Edit/Preferences/Properties)", baseDir.getAbsoluteFile().toString(),CytoscapeProperties.baseDirStr));
		}
		
		LoggerFactory.getLogger(QueryServiceWrapper.class).info(String.format("Remote service:  %s ",remoteUrl));
		LoggerFactory.getLogger(QueryServiceWrapper.class).info(String.format("Croco base dir:  %s ",baseDir.getAbsoluteFile().toString()));
		
		RemoteWebService remoteService = new RemoteWebService(remoteUrl);
	
		service = new BufferedService(remoteService, baseDir );
		this.currentUrl = remoteUrl;
		
		
	}
	public QueryService getService(){
		return service;
	}
	
	private static QueryServiceWrapper instance = null;
	public static Long getVersion(String baseUrl) throws IOException{
		return RemoteWebService.getServiceVersion(baseUrl);
	}
	
	public static QueryServiceWrapper getInstance(String url) throws IOException, SQLException{
		
		if ( instance == null || !url.equals(instance.currentUrl)) {
			
			instance = new QueryServiceWrapper(url);
			
		}
		return instance;
	}
	public static QueryServiceWrapper getInstance(){
		
		if ( instance == null  ) {
			try{
				String remoteUrl = CytoscapeProperties.getProperties().get(CytoscapeProperties.urlStr).toString();
				
				instance = new QueryServiceWrapper(remoteUrl);
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
		return instance;
	}
}
