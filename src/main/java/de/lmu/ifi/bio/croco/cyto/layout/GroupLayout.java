package de.lmu.ifi.bio.croco.cyto.layout;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListModel;
import net.miginfocom.swing.MigLayout;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.internal.utils.ServiceUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

public class GroupLayout extends CroCoLayout{
	private CyLayoutAlgorithm layout;
	private List<CyNetworkView> selectedNetworkViews = new ArrayList<CyNetworkView>();
	
	private BundleContext context;
	private Collection<CyLayoutAlgorithm> layouts;
	
	public GroupLayout(BundleContext context,CyLayoutAlgorithm defaultLayout, Collection<CyLayoutAlgorithm> layouts,UndoSupport undoSupport){
		super("groupLayout", "Group Layouter", undoSupport);
		this.layout = defaultLayout;
		this.context = context;
		this.layouts = layouts;
	}

	@Override
	public Object createLayoutContext() {
		System.out.println("Query content");
		return layout.createLayoutContext();
	}


	
	public void showDialog( Collection<CyLayoutAlgorithm> layouts, Set<CyNetworkView> networkViews){
		final JDialog dialog = new JDialog(new JFrame(),ModalityType.APPLICATION_MODAL);
		dialog.setTitle("Group Layout");
		dialog.setLayout(new MigLayout());
		
		this.selectedNetworkViews = new ArrayList<CyNetworkView>();

		final JList layoutList = new JList();
		DefaultListModel model = new  DefaultListModel();
		for(CyLayoutAlgorithm layout : layouts){
			ListData<CyLayoutAlgorithm> data = new ListData<CyLayoutAlgorithm>(layout,layout.getName(),false);
			if ( this.layout.equals(layout)) data.checkBox.setSelected(true);
			model.addElement(data);
		}
		ButtonGroup group = new ButtonGroup();
		for(int i = 0 ; i < model.getSize();i++){
			group.add(((ListData)model.get(i)).checkBox);
		}
		
		layoutList.setModel(model);

		layoutList.addMouseListener(new CheckedAdapter());

		layoutList.setCellRenderer(new CellRenderer());
		final JList networkList = new JList();
		model = new  DefaultListModel();
		for(CyNetworkView networkView : networkViews){
			String name = networkView.getModel().getRow(networkView.getModel()).get(CyNetwork.NAME, String.class);
			ListData<CyNetworkView> data = new ListData<CyNetworkView>(networkView,name,true);
			model.addElement(data);
		}
	

		networkList.setModel(model);

		networkList.addMouseListener(new CheckedAdapter());
		networkList.setCellRenderer(new CellRenderer());
		dialog.add(new JLabel("Layout"),"wrap");
		dialog.add(networkList,"width 300,wrap");
		
		dialog.add(layoutList,"width 300,wrap");
		dialog.add(new JLabel("Networks"),"wrap");
		
		
		JButton apply = new JButton("Apply layout");
		apply.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				ListModel networkListModel = networkList.getModel();
				for(int i = 0 ; i < networkListModel.getSize();i++){
					if ( ((ListData)networkListModel.getElementAt(i)).checkBox.isSelected() ) {
						selectedNetworkViews.add( (CyNetworkView) ((ListData)networkListModel.getElementAt(i)).data);
					}
				}
				
				ListModel layoutListModel = layoutList.getModel();
				for(int i = 0 ; i < layoutListModel.getSize();i++){
					if ( ((ListData)layoutListModel.getElementAt(i)).checkBox.isSelected() ) {
						layout = (CyLayoutAlgorithm) ((ListData)layoutListModel.getElementAt(i)).data;
					}
				}
				dialog.dispose();
			}
			
		});
		dialog.add(apply,"grow");
		dialog.setResizable(false);
		dialog.pack();
		dialog.setVisible(true);
		

	}
	@Override
	public TaskIterator createTaskIterator(CyNetworkView view, Object layoutContext,Set<View<CyNode>> nodes, String arg3) {
	
		
		
		final CyNetworkViewManager networkViewManager = ServiceUtil.getService(context, CyNetworkViewManager.class,null);

		showDialog(layouts, networkViewManager.getNetworkViewSet());
		layoutContext = this.createLayoutContext();
		if (  this.selectedNetworkViews.size() == 0) return layout.createTaskIterator(view, layoutContext, nodes, arg3);
		CyNetworkFactory networkFactory = ServiceUtil.getService(context, CyNetworkFactory.class,null);
		final CyNetwork union = networkFactory.createNetwork();
		final HashMap<String,CyNode> nodeIdMapping =new HashMap<String,CyNode>();

		for(CyNetworkView network: selectedNetworkViews ) {
			for(View<CyNode> node : network.getNodeViews()){
				String name = network.getModel().getRow(node.getModel()).get(CyNetwork.NAME,String.class);
				if (! nodeIdMapping.containsKey(name)){
					CyNode newNode = union.addNode();
					union.getRow(newNode).set(CyNetwork.NAME, name);
					nodeIdMapping.put(name, newNode);
				}

			}
		}


		for(CyNetworkView network: selectedNetworkViews ) {
			for(View<CyEdge> edge : network.getEdgeViews()){
				String sourceName = network.getModel().getRow(edge.getModel().getSource()).get(CyNetwork.NAME,String.class);
				String targetName = network.getModel().getRow(edge.getModel().getTarget()).get(CyNetwork.NAME,String.class);

				CyNode sourceNode = nodeIdMapping.get(sourceName);
				CyNode targetNode = nodeIdMapping.get(targetName);

				union.addEdge(sourceNode,targetNode, edge.getModel().isDirected());
			}
		}

		final CyNetworkManager manager = ServiceUtil.getService(context,CyNetworkManager.class,null);
		manager.addNetwork(union);
		CyNetworkViewFactory networkViewFactory = ServiceUtil.getService(context, CyNetworkViewFactory.class,null);
		final CyNetworkView myView = networkViewFactory.createNetworkView(union);
		networkViewManager.addNetworkView(myView);

		TaskIterator taskIterator = layout.createTaskIterator(myView, layoutContext, nodes, arg3);
		Task applyLayout = new Task(){

			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {

				for(CyNetworkView network:selectedNetworkViews ) {
					for(View<CyNode> node : network.getNodeViews()){
						String nodeId = network.getModel().getRow(node.getModel()).get(CyNetwork.NAME,String.class);

						View<CyNode> targetNode = myView.getNodeView(nodeIdMapping.get(nodeId));

						Double xLoc = targetNode.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
						Double yLoc = targetNode.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
						node.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, xLoc);
						node.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, yLoc);

					}
				}
				
				
			}

			@Override
			public void cancel() {

			}

		};
		Task clean = new Task(){

			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				manager.destroyNetwork(union);
			}

			@Override
			public void cancel() {
				// TODO Auto-generated method stub
				
			}
			
		};
		taskIterator.append(applyLayout);
		taskIterator.append(clean);

		return taskIterator;
	}


}
