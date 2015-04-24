package de.lmu.ifi.bio.croco.cyto.ui;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.lmu.ifi.bio.croco.connector.QueryService;
import de.lmu.ifi.bio.croco.data.CroCoNode;
import de.lmu.ifi.bio.croco.data.Identifiable;
import de.lmu.ifi.bio.croco.data.NetworkMetaInformation;
import de.lmu.ifi.bio.croco.util.CroCoLogger;
import de.lmu.ifi.bio.croco.util.ontology.NetworkOntology.LeafNode;
import de.lmu.ifi.bio.croco.cyto.ui.transferable.NetworkMetaInformationTransferable;


public class NetworkTree extends JTree implements TreeSelectionListener,DragGestureListener,DragSourceListener{

	private static final long serialVersionUID = 1L;
	CroCoNode<NetworkMetaInformation> origRoot;
	CroCoNode<NetworkMetaInformation> root;
	
	public CroCoNode<NetworkMetaInformation> getRoot()
	{
	    return root;
	}
	private static HashMap<CroCoNode<NetworkMetaInformation>,TreeNode> treeNodeMapping = new HashMap<CroCoNode<NetworkMetaInformation>,TreeNode> ();
    
	public class NetworkHierachyTreeNode implements TreeNode {

	    private CroCoNode<NetworkMetaInformation> operatorable;


	    
	    public CroCoNode<NetworkMetaInformation> getOperatorable(){
	        return operatorable;
	    }

	    
	    public NetworkHierachyTreeNode(CroCoNode<NetworkMetaInformation> operatorable){
	        this.operatorable = operatorable;
	    }

	    

	    @Override
	    public TreeNode getChildAt(int childIndex) {
	        CroCoNode<NetworkMetaInformation> childNode = operatorable.getChildren().get(childIndex);
	        return getTreeNode(childNode);
	    }


	    @Override
	    public int getChildCount() {
	        if ( operatorable.getChildren() == null)
	        {
	            try
	            {
	                operatorable.initChildren(origRoot);
	            }catch(Exception e)
    	        {
   	                throw new RuntimeException(e);
    	        }
	        }
	        return operatorable.getChildren().size();
	    }


	    @Override
	    public TreeNode getParent() {
	        return null;
	    }


	    @Override
	    public int getIndex(TreeNode node) {
	        return 0;
	    }

	    @Override
	    public boolean getAllowsChildren() {
	        return true;
	    }

	    @Override
	    public String toString(){
	        if ( this.operatorable == null) return "<<null>>";
	        return this.operatorable.toString();
	    }
	    
	    @Override
	    public boolean isLeaf() {
	        
	        return operatorable instanceof LeafNode;
	    }

	    @Override
	    public Enumeration children() {
	        return Collections.enumeration(this.operatorable.getChildren());
	        
	        //return this.operatorable.getChildren().iterator();
	    }
	    
	    
	    public TreeNode getTreeNode(CroCoNode node){
	        if (!treeNodeMapping.containsKey(node)){
	            treeNodeMapping.put(node,new NetworkHierachyTreeNode(node));
	        }
	        return treeNodeMapping.get(node);
	    }
	    
	}
	private DragSource dragSource = null;

	public NetworkTree(QueryService service)  {

		CroCoLogger.getLogger().info("Load root");

		CroCoNode<NetworkMetaInformation> rootNode = null;
		try{
			CroCoNode<NetworkMetaInformation> root = service.getNetworkOntology(false);
			this.origRoot = root;
			rootNode= new CroCoNode<NetworkMetaInformation>(root);
			rootNode.setData(root.getData());
			this.root = rootNode;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		
		NetworkHierachyTreeNode root = new NetworkHierachyTreeNode(rootNode);

		DefaultTreeModel model = new DefaultTreeModel(root);
		
		this.addTreeSelectionListener(this);
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this,DnDConstants.ACTION_MOVE, this);


		this.setRootVisible(false);
		setModel(model);

		setShowsRootHandles(true);

		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}
	/*
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
	*/
	public List<NetworkMetaInformation> getLeafs(NetworkHierachyTreeNode node){
		List<NetworkMetaInformation> leafs = new ArrayList<NetworkMetaInformation>();
		Stack<NetworkHierachyTreeNode> nodes = new Stack<NetworkHierachyTreeNode>();
		nodes.add(node);
		HashSet<Integer> taxIds = new HashSet<Integer>();
		while(!nodes.isEmpty()){
			NetworkHierachyTreeNode top = nodes.pop();

			if ( top.isLeaf() ) {
			    NetworkMetaInformation nh = top.getOperatorable().getData().iterator().next();
			    
				taxIds.add(nh.getTaxId());
				if ( top.getOperatorable() instanceof CroCoNode)

					leafs.add( nh);
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

		NetworkMetaInformationTransferable toTransfer = null;
		try {

			toTransfer = new NetworkMetaInformationTransferable(new ArrayList<NetworkMetaInformation>(node.getOperatorable().getData()));
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		CroCoLogger.getLogger().debug("Selected elements:\t" + node);
		dragSource.startDrag(e, DragSource.DefaultMoveDrop, toTransfer, this);


	}
	@Override
	public void dragEnter(DragSourceDragEvent e) {

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
