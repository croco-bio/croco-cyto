package de.lmu.ifi.bio.croco.cyto;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.internal.utils.ServiceUtil;
import org.osgi.framework.BundleContext;

import de.lmu.ifi.bio.crco.util.CroCoLogger;


/**
 * Creates a new menu item under Apps menu section.
 *
 */
public class DavidGoEnrichment extends AbstractCyAction {

	private static String davidQueryURL ="http://david.abcc.ncifcrf.gov/api.jsp";
	
	private static final long serialVersionUID = 1L;
	private BundleContext context;
	public DavidGoEnrichment(BundleContext context, CyApplicationManager cyApplicationManager, final String menuTitle) {
		
		super(menuTitle, cyApplicationManager, null, null);
		this.context = context;
		setPreferredMenu("Apps.CroCo-Cyto");
	
	}

	public void actionPerformed(ActionEvent e) {

		CyApplicationManager appMgr = ServiceUtil.getService(context, CyApplicationManager.class, null);
		
		CyNetwork currentNetwork = appMgr.getCurrentNetwork();
		List<CyNode> nodes = CyTableUtil.getNodesInState(currentNetwork,"selected",true);
		StringBuffer geneList = new StringBuffer();
		for(CyNode node : nodes){
			
			String selectedNode = currentNetwork.getRow(node).get("Name", String.class);
			geneList.append(selectedNode + ",");
		
		}
		if ( geneList.length() > 0){
			
			URI uri;
			try {
				uri = new URI(String.format("%s?type=ENSEMBL_GENE_ID&ids=%s&tool=summary",davidQueryURL,geneList.substring(0,geneList.length()-1)));
				Desktop.getDesktop().browse(uri);
			} catch (Exception ex) {
				CroCoLogger.getLogger().fatal(ex.getMessage());
				throw new RuntimeException(ex);
			} 
			
		
		}

		 
	}
}
