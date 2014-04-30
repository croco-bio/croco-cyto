package de.lmu.ifi.bio.croco.cyto;

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.bio.crco.util.CroCoProperties;
import de.lmu.ifi.bio.croco.cyto.layout.CopyLayout;
import de.lmu.ifi.bio.croco.cyto.layout.GroupLayout;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context)  {
		try{
			
			//init config
			Properties props = CroCoProperties.getInstance().getProperties();
			PropertyConfigurator.configure(props);
			
			CyApplicationManager cyApplicationManager = getService(context, CyApplicationManager.class);
		
			//register services
			DavidGoEnrichment action = new DavidGoEnrichment(context,cyApplicationManager, "David GO enrichment");
			EvidenceLookup lookup = new EvidenceLookup(context,cyApplicationManager, "Evidence look-up");
			
			CyLayoutAlgorithmManager manager = getService(context,CyLayoutAlgorithmManager.class);
			CyLayoutAlgorithm defaultLayout = manager.getDefaultLayout();
			for(CyLayoutAlgorithm layout : manager.getAllLayouts()){
				if ( layout.getName().equals("force-directed")) defaultLayout = layout;
			}
			UndoSupport undo = this.getService(context, UndoSupport.class);
			
			GroupLayout layout = new GroupLayout(context,defaultLayout,manager.getAllLayouts(),undo);
			CopyLayout copyLayout = new CopyLayout(context,undo);
			
			
			Properties dict = new Properties();
			dict.setProperty(PREFERRED_MENU, "CroCo.TFBS");
			dict.setProperty(MENU_GRAVITY, "-1"); 
		
			CcPath ccPath = new CcPath(context);
			
			
			Properties properties = new Properties();
	
			registerAllServices(context, ccPath, properties);
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