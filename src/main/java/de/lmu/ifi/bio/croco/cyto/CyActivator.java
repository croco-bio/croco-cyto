package de.lmu.ifi.bio.croco.cyto;

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.bio.croco.util.CroCoLogger;
import de.lmu.ifi.bio.croco.util.CroCoProperties;
import de.lmu.ifi.bio.croco.cyto.layout.CopyLayout;
import de.lmu.ifi.bio.croco.cyto.layout.GroupLayout;
import de.lmu.ifi.bio.croco.cyto.util.CytoscapeProperties;

public class CyActivator extends AbstractCyActivator {
    
	@Override
	public void start(BundleContext context)  {
		try{
		    //init the CroCoProperties
		    CroCoProperties.init(CroCoLogger.class.getClassLoader().getResourceAsStream("connet-croco.config"));
            
		    
	
			CyApplicationManager cyApplicationManager = getService(context, CyApplicationManager.class);
		
			//create services
			DavidGoEnrichment action = new DavidGoEnrichment(context,cyApplicationManager, "David GO enrichment");
			EvidenceLookup lookup = new EvidenceLookup(context,cyApplicationManager, "Evidence look-up",CroCoProperties.getInstance().getValue(CytoscapeProperties.urlEvidenceLookUp));
            
			//create (layout) services
			CyLayoutAlgorithmManager manager = getService(context,CyLayoutAlgorithmManager.class);
			CyLayoutAlgorithm defaultLayout = manager.getDefaultLayout();
			for(CyLayoutAlgorithm layout : manager.getAllLayouts()){
				if ( layout.getName().equals("force-directed")) defaultLayout = layout;
			}
			UndoSupport undo = this.getService(context, UndoSupport.class);
			
			GroupLayout layout = new GroupLayout(context,defaultLayout,manager.getAllLayouts(),undo);
			CopyLayout copyLayout = new CopyLayout(context,undo);
			
			
			Properties dict = new Properties();
			dict.setProperty(PREFERRED_MENU, "CroCo");
			dict.setProperty(MENU_GRAVITY, "-1"); 
		
			CroCoCyto crocoCyto = new CroCoCyto(context);
			
	
			
			Properties properties = new Properties();
			registerAllServices(context, crocoCyto, properties);
			registerAllServices(context, action, properties);
			
			registerAllServices(context, lookup, properties);
			registerAllServices(context, layout, properties);
			registerAllServices(context, copyLayout, properties);
			
		
		}catch(Exception e){
			LoggerFactory.getLogger(CyActivator.class).equals(e.getMessage());
			throw new RuntimeException(e);
		}
	}

}
