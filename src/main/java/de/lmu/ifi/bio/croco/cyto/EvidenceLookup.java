package de.lmu.ifi.bio.croco.cyto;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.internal.utils.ServiceUtil;
import org.osgi.framework.BundleContext;

import de.lmu.ifi.bio.crco.util.CroCoLogger;
import de.lmu.ifi.bio.croco.cyto.util.CytoscapeProperties;

/**
 * @author pesch
 *
 */
public class EvidenceLookup  extends AbstractCyAction {

	private static final long serialVersionUID = 1L;
	private BundleContext context;
	private String urlEvidence ;
	public EvidenceLookup(BundleContext context, CyApplicationManager cyApplicationManager, final String menuTitle, String urlEvidence) {
		
		super(menuTitle, cyApplicationManager, null, null);
		this.context = context;
		this.urlEvidence = urlEvidence;
		setPreferredMenu("Apps.CroCo-Cyto");
	
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		CyApplicationManager appMgr = ServiceUtil.getService(context, CyApplicationManager.class, null);
		
		CyNetwork currentNetwork = appMgr.getCurrentNetwork();
		List<CyNode> nodes = CyTableUtil.getNodesInState(currentNetwork,"selected",true);
		StringBuffer geneList = new StringBuffer();
		for(CyNode node : nodes){
			
			String selectedNode = currentNetwork.getRow(node).get("Name", String.class);
			geneList.append(selectedNode + ",");
		
		}
		List<CyEdge> edges = CyTableUtil.getEdgesInState(currentNetwork,"selected",true);
		if ( edges.size() == 1){
			CyNode sourceNode = edges.get(0).getSource();
			CyNode targetNode = edges.get(0).getTarget();
			
			

			URI uri;
			try {
				uri = new URI(
						String.format(
								"%s?factor=%s&target=%s",urlEvidence,
								currentNetwork.getRow(sourceNode).get("Name", String.class),
								currentNetwork.getRow(targetNode).get("Name", String.class) 
								)
						);
				Desktop.getDesktop().browse(uri);
			} catch (Exception ex) {
				CroCoLogger.getLogger().fatal(ex.getMessage());
				throw new RuntimeException(ex);
			} 
				
			
			
		}else{
	        JOptionPane.showMessageDialog(null, "Select one network in any network", "Error", JOptionPane.WARNING_MESSAGE);
	        
		}
	}

}
