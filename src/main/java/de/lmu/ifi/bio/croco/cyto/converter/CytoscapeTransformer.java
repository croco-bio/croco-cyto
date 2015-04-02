package de.lmu.ifi.bio.croco.cyto.converter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.internal.utils.ServiceUtil;
import org.osgi.framework.BundleContext;

import de.lmu.ifi.bio.croco.data.Entity;
import de.lmu.ifi.bio.croco.network.Network;
import de.lmu.ifi.bio.croco.operation.converter.Convert;
import de.lmu.ifi.bio.croco.util.CroCoLogger;
import de.lmu.ifi.bio.croco.util.StringUtil;
import de.lmu.ifi.bio.croco.util.Tuple;

public class CytoscapeTransformer implements Convert<org.cytoscape.model.CyNetwork>{
	private BundleContext context;
	public CytoscapeTransformer(BundleContext context){
		this.context = context;
	}
	
	@Override
	public CyNetwork convert(Network network)  {
		
		
		
		CyTableFactory tableFactory = ServiceUtil.getService(context, CyTableFactory.class,null);
		CyTableManager tableManager  = ServiceUtil.getService(context, CyTableManager.class,null);
		
		
		CyNetworkFactory networkFactory = ServiceUtil.getService(context, CyNetworkFactory.class,null);
		CyNetwork cytoNetwork = networkFactory.createNetwork();
	    CyTable table = tableFactory.createTable("Table", "name", String.class, true, true);
	   
	    
	    String identifier = "Identifier"; 
	    String evidence= "Evidence Count";
 //       table.createColumn(evidence, Integer.class, false);
        
        
	    tableManager.addTable(table);
	    cytoNetwork.getDefaultNodeTable().createColumn(identifier, String.class, true);
	    cytoNetwork.getDefaultNodeTable().createColumn(evidence, String.class, true);
	    cytoNetwork.getDefaultEdgeTable().createColumn(evidence, String.class, true);
		   
	    
		CroCoLogger.getLogger().info("Transforming network");
		
		cytoNetwork.getRow(cytoNetwork).set(CyNetwork.NAME, network.toString());
		
		HashMap<Entity,CyNode> nodeMapping = new HashMap<Entity,CyNode>();
		for(Entity gene : network.getNodes()){
			CyNode node1 = cytoNetwork.addNode();
			
			cytoNetwork.getRow(node1).set(CyNetwork.NAME, gene.getName());
			cytoNetwork.getRow(node1).set(identifier, gene.getIdentifier());
			nodeMapping.put(gene, node1);
		}
		HashMap<CyNode,Set<Integer>> nodeToGroupId = new HashMap<CyNode,Set<Integer>>();
		for(int edgeId: network.getEdgeIds()){
			Tuple<Entity, Entity>  edge = network.getEdge(edgeId); 
			CyNode node1 =nodeMapping.get(edge.getFirst());
			CyNode node2 =nodeMapping.get(edge.getSecond());
			List<Object> objects = network.getAnnotation(edgeId,Network.EdgeOption.GroupId);
			CyEdge cytoEdge = cytoNetwork.addEdge(node1, node2, true);
			HashSet<Object> o = new HashSet<Object>(objects);
			StringBuffer label = new StringBuffer();
			for(Object obj : o){
				Integer groupId = (Integer) obj;
				if ( label.length() > 0 ) label.append(",");
				label.append(groupId);
				if (! nodeToGroupId.containsKey(node1)){
					nodeToGroupId.put(node1, new HashSet<Integer>());
				}
				if (! nodeToGroupId.containsKey(node2)){
					nodeToGroupId.put(node2, new HashSet<Integer>());
				}
				nodeToGroupId.get(node1).add(groupId);
				nodeToGroupId.get(node2).add(groupId);
				
			}
			
			cytoNetwork.getRow(cytoEdge).set(evidence, label.toString());
		}
		for(CyNode node  : cytoNetwork.getNodeList()){
			cytoNetwork.getRow(node).set(evidence, StringUtil.getAsString(nodeToGroupId.get(node), ','));
		}
		
		return cytoNetwork;
	}

}
