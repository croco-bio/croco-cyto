package de.lmu.ifi.bio.croco.cyto.ui;

import java.awt.Rectangle;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.lmu.ifi.bio.crco.connector.QueryService;
import de.lmu.ifi.bio.crco.data.NetworkHierachyNode;
import de.lmu.ifi.bio.crco.util.CroCoLogger;
import de.lmu.ifi.bio.croco.cyto.ui.transferable.NetworkHierachyNodeTransferable;
import de.lmu.ifi.bio.croco.cyto.util.QueryServiceWrapper;


public class NetworkTree extends JTree implements TreeSelectionListener,DragGestureListener,DragSourceListener{

	private static final long serialVersionUID = 1L;


	private DragSource dragSource = null;

	public NetworkTree()  {

		CroCoLogger.getLogger().info("Load root");

		NetworkHierachyNode rootNode = null;
		try{
			rootNode = QueryServiceWrapper.getInstance().getService().getNetworkHierachy(null);
		}catch(Exception e){
			e.printStackTrace();
		}
		NetworkHierachyTreeNode root = new NetworkHierachyTreeNode(rootNode);
		

		DefaultTreeModel model = new DefaultTreeModel(root);

		this.addTreeSelectionListener(this);
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this,1, this);


		this.setRootVisible(false);
		setModel(model);

		setShowsRootHandles(true);

		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}
	public NetworkHierachyTreeNode getNetwork(String path){
		NetworkHierachyTreeNode rootNode = (NetworkHierachyTreeNode) this.getModel().getRoot();
		String[] tokens = path.split("/");
		for(String token : tokens){
		    token = token.trim().toLowerCase();
		    
			if(token.length() == 0) continue;
			NetworkHierachyTreeNode newRoot = null;
			List<String> children = new ArrayList();
			for(int i = 0 ; i< rootNode.getChildCount() ; i++){
				NetworkHierachyTreeNode child =  (NetworkHierachyTreeNode) rootNode.getChildAt(i);
				children.add(child.getOperatorable().getName().trim().toLowerCase());
				if (child.getOperatorable().getName().trim().toLowerCase().equals(token)) {
					newRoot = child;
					break;
				}
			}
			if ( newRoot == null) 
			{
			    CroCoLogger.getLogger().info("Stop at:" + token + " " + children);
			    return null;
			}
			rootNode = newRoot;
		}
		
		return rootNode;
	}
	
	public List<NetworkHierachyNode> getLeafs(NetworkHierachyTreeNode node){
		List<NetworkHierachyNode> leafs = new ArrayList<NetworkHierachyNode>();
		Stack<NetworkHierachyTreeNode> nodes = new Stack<NetworkHierachyTreeNode>();
		nodes.add(node);
		HashSet<Integer> taxIds = new HashSet<Integer>();
		while(!nodes.isEmpty()){
			NetworkHierachyTreeNode top = nodes.pop();
			/*
			if (  top.getOperatorable().getChildren() == null){
				Helper.loadChildren((OperatorNode)top);
			}
			 */
			if ( top.isLeaf() ) {
				taxIds.add(top.getOperatorable().getTaxId());
				if ( top.getOperatorable() instanceof NetworkHierachyNode)

					leafs.add( new  NetworkHierachyNode((NetworkHierachyNode)  top.getOperatorable()));
			}else{
				for(int i = 0 ; i< top.getChildCount(); i++){
					nodes.add((NetworkHierachyTreeNode)(top.getChildAt(i)));
				}

			}

		}
		return leafs;
	}


	@Override
	public void dragGestureRecognized(DragGestureEvent e) {

		TreePath path = this.getSelectionPath();
		if ((path == null) || (path.getPathCount() <= 1)) {
			// We can't move the root node or an empty selection
			return;
		}

		NetworkHierachyTreeNode node = (NetworkHierachyTreeNode) path.getLastPathComponent();

		List<NetworkHierachyNode> leafs = getLeafs(node);

		NetworkHierachyNodeTransferable toTransfer = null;
		try {

			toTransfer = new NetworkHierachyNodeTransferable(leafs);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		CroCoLogger.getLogger().debug("Selected elements:\t" + leafs);
		dragSource.startDrag(e, DragSource.DefaultMoveNoDrop, toTransfer, this);


	}
	@Override
	public void dragEnter(DragSourceDragEvent e) {
		DragSourceContext context = e.getDragSourceContext();  
		context.setCursor(DragSource.DefaultCopyDrop);  

	}
	@Override
	public void dragOver(DragSourceDragEvent dsde) {
	}
	@Override
	public void dropActionChanged(DragSourceDragEvent dsde) {
	}
	@Override
	public void dragExit(DragSourceEvent dse) {
	}
	@Override
	public void dragDropEnd(DragSourceDropEvent dsde) {
	}

	public NetworkHierachyTreeNode getSelectedNode() {
		return SelectedNode;
	}

	protected TreePath SelectedTreePath = null;
	protected NetworkHierachyTreeNode SelectedNode = null;
	@Override
	public void valueChanged(TreeSelectionEvent evt) {
		SelectedTreePath = evt.getNewLeadSelectionPath();
		if (SelectedTreePath == null) {
			SelectedNode = null;
			return;
		}
		SelectedNode =  (NetworkHierachyTreeNode)SelectedTreePath.getLastPathComponent();

	}


}
