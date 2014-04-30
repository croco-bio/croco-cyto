package de.lmu.ifi.bio.croco.cyto.ui;

import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.tree.TreeNode;

import de.lmu.ifi.bio.crco.data.NetworkHierachyNode;

public class NetworkHierachyTreeNode implements TreeNode {

	public NetworkHierachyNode getOperatorable(){
		return operatorable;
	}

	
	public NetworkHierachyTreeNode(NetworkHierachyNode operatorable){
		this.operatorable = operatorable;
	}

	
	private NetworkHierachyNode operatorable;


	@Override
	public TreeNode getChildAt(int childIndex) {
		NetworkHierachyNode childNode = operatorable.getChildren().get(childIndex);
		return NetworkHierachyTreeNode.getTreeNode(childNode);
	}


	@Override
	public int getChildCount() {
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
		return this.operatorable.getChildren() == null || this.operatorable.getChildren().size()==0;
	}

	@Override
	public Enumeration children() {
		return this.operatorable.getChildren().elements();
	}
	
	private static HashMap<NetworkHierachyNode,TreeNode> treeNodeMapping = new HashMap<NetworkHierachyNode,TreeNode> ();
	
	public static TreeNode getTreeNode(NetworkHierachyNode node){
		if (!treeNodeMapping.containsKey(node)){
			treeNodeMapping.put(node,new NetworkHierachyTreeNode(node));
		}
		return treeNodeMapping.get(node);
	}
	
}