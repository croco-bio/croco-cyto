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

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.internal.utils.ServiceUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

/**
 * Copies the (x,y) location from one network view to another.
 * @author pesch
 *
 */
public class CopyLayout extends CroCoLayout{

	private List<CyNetworkView> selectedNetworkViews = new ArrayList<CyNetworkView>();
	private CyNetworkView fromNetwork = null;
	
	private BundleContext context;
	
	public CopyLayout(BundleContext context,UndoSupport undoSupport) {
		super("copyLayout", "Layout Copy", undoSupport);
		this.context = context;
	}

	
	public void showDialog( CyNetworkView selectedNetwork, Set<CyNetworkView> networkViews){
		final JDialog dialog = new JDialog(new JFrame(),ModalityType.APPLICATION_MODAL);
		dialog.setTitle("Copy Layout");
		dialog.setLayout(new MigLayout());
		
		this.selectedNetworkViews = new ArrayList<CyNetworkView>();

		final JList fromNetworkList = new JList();
		DefaultListModel model = new  DefaultListModel();
		for(CyNetworkView networkView : networkViews){
			String name = networkView.getModel().getRow(networkView.getModel()).get(CyNetwork.NAME, String.class);
			ListData<CyNetworkView> data = new ListData<CyNetworkView>(networkView,name,false);
			if ( networkView.equals(selectedNetwork)) data.checkBox.setSelected(true);
			model.addElement(data);
		}
		ButtonGroup group = new ButtonGroup();
		for(int i = 0 ; i < model.getSize();i++){
			group.add(((ListData)model.get(i)).checkBox);
		}
		
		fromNetworkList.setModel(model);

		fromNetworkList.addMouseListener(new CheckedAdapter());

		fromNetworkList.setCellRenderer(new CellRenderer());
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
		dialog.add(new JLabel("From Network"),"wrap");
		dialog.add(fromNetworkList,"width 300,wrap");
		dialog.add(new JLabel("To Networks"),"wrap");
		dialog.add(networkList,"width 300,wrap");
		
		
		
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
				
				ListModel fromModel = fromNetworkList.getModel();
				for(int i = 0 ; i < fromModel.getSize();i++){
					if ( ((ListData)fromModel.getElementAt(i)).checkBox.isSelected() ) {
						fromNetwork = (CyNetworkView) ((ListData)fromModel.getElementAt(i)).data;
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
	public TaskIterator createTaskIterator(CyNetworkView view, Object arg1, Set<View<CyNode>> arg2, String arg3) {
		final CyNetworkViewManager networkViewManager = ServiceUtil.getService(context, CyNetworkViewManager.class,null);

		showDialog(view, networkViewManager.getNetworkViewSet());
		final HashMap<String,View<CyNode>> idToNodeView = new HashMap<String,View<CyNode>>();
		System.out.println(fromNetwork);
		for(View<CyNode> nodeView : fromNetwork.getNodeViews()){
			String nodeId = fromNetwork.getModel().getRow(nodeView.getModel()).get(CyNetwork.NAME,String.class);
			idToNodeView.put(nodeId, nodeView);
		}
		System.out.println(idToNodeView);
		TaskIterator tk = new TaskIterator();
		tk.append(new Task(){

			@Override
			public void cancel() {
				
			}

			@Override
			public void run(TaskMonitor arg0) throws Exception {
				for(CyNetworkView network:selectedNetworkViews ) {
					for(View<CyNode> node : network.getNodeViews()){
						String nodeId = network.getModel().getRow(node.getModel()).get(CyNetwork.NAME,String.class);

						View<CyNode> sourceNode = idToNodeView.get(nodeId);
						if ( sourceNode == null) {
							System.out.println("Skip" + nodeId);
							continue;
						}
						System.out.println("Apply on:" + nodeId);
						Double xLoc = sourceNode.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
						Double yLoc = sourceNode.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
						node.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, xLoc);
						node.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, yLoc);

					}
				}
				
				
				
			}
			
		});
		
		return tk;
	}

}
