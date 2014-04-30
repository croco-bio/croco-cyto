package de.lmu.ifi.bio.croco.cyto.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.lmu.ifi.bio.crco.connector.QueryService;
import de.lmu.ifi.bio.crco.data.ContextTreeNode;
import de.lmu.ifi.bio.crco.data.HierachyNode;
import de.lmu.ifi.bio.crco.util.CroCoLogger;
import de.lmu.ifi.bio.croco.cyto.util.QueryServiceWrapper;

public class GOBrowserTree extends JTree implements TreeSelectionListener, TreeWillExpandListener{

	class GoNode implements TreeNode{
		private ContextTreeNode node;
		private Vector<TreeNode> children ;
		public GoNode(ContextTreeNode node){
			this.node = node;
			this.children = new  Vector<TreeNode>();
		}
		public ContextTreeNode getData() {
			return node;
		}
		 public int getChildCount() {
			 return node.getNumLeafs();
		 }
		 public int getInternalChildCount(){
			 if ( children == null) return 0;
			 return children.size();
		 }
		@Override
		public TreeNode getChildAt(int childIndex) {
			return children.get(childIndex);
		}

		@Override
		public TreeNode getParent() {
			return null;
		}

		@Override
		public int getIndex(TreeNode node) {
			return 0;
		}
		public String toString(){
			return node.getDescription();
		}
		@Override
		public boolean getAllowsChildren() {
			return true;
		}
		public void add(TreeNode node){
			this.children.add(node);
		}
		@Override
		public boolean isLeaf() {
			return (this.node.getNumLeafs()==0);
		}

		@Override
		public Enumeration children() {
			return children.elements();
		}
	}
	
	public static void main(String[] args) throws Exception{
		GOBrowserTree browser = new GOBrowserTree(null);
		JFrame frame = new JFrame();
		 JScrollPane scrp = new JScrollPane(browser);
		frame.add(scrp);
		
		frame.setVisible(true);
		frame.pack();
	}
	public void setData(List<ContextTreeNode> nodes) throws Exception{
		ContextTreeNode searchResult = new ContextTreeNode(null,"Search result","Search result",nodes.size());
		GoNode root = new GoNode(searchResult);
		for(ContextTreeNode child :nodes){
			GoNode c = new GoNode(child);
			root.add(c);
			searchResult.addChild(child);
		}
		
		DefaultTreeModel model = new DefaultTreeModel(root);
		this.setModel(model);
	}
	public GOBrowserTree(ContextTreeNode context) throws Exception{
		

		
		init(context);
		this.expandRow(0);

		this.addTreeSelectionListener(this);
		this.setRootVisible(true);
		

		addTreeWillExpandListener(this);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);	
	        
	}
	
	public void loadChildren(GoNode node) throws Exception{
		
	
		ContextTreeNode context = (ContextTreeNode) node.getData();
	
		if ( context.getChildren() != null) return;
		CroCoLogger.getLogger().debug("Load children for:\t" + node);
		QueryService service = QueryServiceWrapper.getInstance().getService();
		List<HierachyNode> children =new  ArrayList<HierachyNode> ();
		
		for(ContextTreeNode child : service.getChildren(context)){
			GoNode c = new GoNode(child);
			node.add(c);
			children.add(child);
		}
		context.setChildren(children);
	}



	@Override
	public void valueChanged(TreeSelectionEvent e) {
		
	}





	@Override
	public void treeWillExpand(TreeExpansionEvent event)	throws ExpandVetoException {
		GoNode root = (GoNode) event.getPath().getLastPathComponent();
		try {
			loadChildren((GoNode) root);
	
		} catch (Exception e) {
	
			e.printStackTrace();
		}
	}





	@Override
	public void treeWillCollapse(TreeExpansionEvent event)throws ExpandVetoException {
		
	}
	public void init(ContextTreeNode context) throws Exception {
		QueryService service = QueryServiceWrapper.getInstance().getService();
		 
		GoNode root = null;
		if ( context == null){
			CroCoLogger.getLogger().info("Load root");
			ContextTreeNode  rootNode = service.getContextTreeNode("GO:0008150");
			root = new GoNode(rootNode);
		}else{
			CroCoLogger.getLogger().info("Set root:" + context);

			ContextTreeNode rootNode = context;
			root = new GoNode(rootNode);
			
		
		}
	
		loadChildren(root);
		
		if ( root.getInternalChildCount() != root.getData().getNumLeafs()){
			for(HierachyNode child : root.getData().getChildren()){
				GoNode c = new GoNode((ContextTreeNode)child);
				root.add(c);
			}
		}
		

		DefaultTreeModel model = new DefaultTreeModel(root);
		
		TreeNode[] nodes = model.getPathToRoot(root);  
		TreePath path = new TreePath(nodes); 
		
		this.setSelectionPath(path);
		setModel(model);
	}


}
