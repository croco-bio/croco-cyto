package de.lmu.ifi.bio.croco.cyto.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.bio.crco.util.CroCoLogger;
import de.lmu.ifi.bio.crco.util.CroCoProperties;

public class CytoscapeProperties extends AbstractConfigDirPropsReader {
	public static String baseDirStr = "de.lmu.ifi.bio.croco.cyto.baseDir";
	public static String urlStr ="de.lmu.ifi.bio.croco.cyto.service";
	public static String urlEvidenceLookUp ="de.lmu.ifi.bio.croco.cyto.EvidenceLookup.details";
	
	public static String fileName ="croco-conf.conf";
	public static String name ="croco-conf";
	
    public CytoscapeProperties() {
        super(name, fileName, CyProperty.SavePolicy.CONFIG_DIR);
    }
    
	/*
	public static Properties getProperties() {
		if ( properties == null){
		    
			File configBaseDir = new File(System.getProperty("user.home"),CyProperty.DEFAULT_PROPS_CONFIG_DIR);
			File configFile = new File(configBaseDir, "/cytoscape3.props");
			
			if (! configFile.exists())
			{
			    LoggerFactory.getLogger(CytoscapeProperties.class).error(configFile + " does not exist");
                return new Properties();
			}
			    
			
			CroCoLogger.getLogger().debug("Read:" + configFile);
			properties= new Properties();
			try{
				FileReader reader = new FileReader(configFile);
				properties.load(reader);
				reader.close();
				
				if (! properties.containsKey(urlEvidenceLookUp)){
					properties.put(urlEvidenceLookUp,CroCoProperties.getInstance().getValue(urlEvidenceLookUp));
				}
				if (! properties.containsKey(urlStr)){
					properties.put(urlStr,CroCoProperties.getInstance().getValue(urlStr));
				}
				if (! properties.containsKey(baseDirStr)){
					properties.put(baseDirStr, configBaseDir+ "/croco");
				}
			}catch(IOException e){
				throw new RuntimeException("Could not read properties",e);
			}
			
		}
		return properties;
	}
	public static void storeProperties() {
		File configBaseDir = new File(System.getProperty("user.home"),CyProperty.DEFAULT_PROPS_CONFIG_DIR);
		File configFile = new File(configBaseDir, "/cytoscape3.props");
		try{
			FileWriter out = new FileWriter(configFile);
			properties.store(out, null);
		
			out.close();
		}catch(IOException e){
			throw new RuntimeException("Could not save properties",e);
		}
		
	}
	*/
}
