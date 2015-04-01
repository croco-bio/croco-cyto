package de.lmu.ifi.bio.croco.cyto.ui;

import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.tree.TreeNode;

import de.lmu.ifi.bio.crco.data.NetworkOperationNode;

/**
 * A wrapper for NetworkOperationNode nodes (see croco-api).
 * @author pesch
 *
 */
public class NetworkOperatorTreeNode implements TreeNode {

	private String name;
	
	public NetworkOperationNode getOperatorable(){
		return operatorable;
	}

	public NetworkOperatorTreeNode(NetworkOperationNode operatorable){
		this.operatorable = operatorable;
	}

	
	private NetworkOperationNode operatorable;
	public void setName(String name){
		this.name = name;
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		NetworkOperationNode childNode = operatorable.getChildren().get(childIndex);
		return NetworkOperatorTreeNode.getTreeNode(childNode);
	}


	@Override
	public int getChildCount() {
		return operatorable.getChildren().size();
	}


	@Override
	public TreeNode getParent() {
		throw new UnsupportedOperationException();
	}


	@Override
	public int getIndex(TreeNode node) {
		return 0; //TOOD: understand
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public String toString(){
		String ret = "";
		if ( name != null) 
			ret = name;
		else if ( this.operatorable != null) 
			ret = this.operatorable.toString() ;
		return ret.toString();
	}
	
	@Override
	public boolean isLeaf() {
		return this.operatorable.getChildren() == null || this.operatorable.getChildren().size()==0;
	}



	@Override
	public Enumeration children() {
		return this.operatorable.getChildren().elements();
	}
	

	
	private static HashMap<NetworkOperationNode,TreeNode> treeNodeMapping = new HashMap<NetworkOperationNode,TreeNode>();
	
	public static TreeNode getTreeNode(NetworkOperationNode node){
		if (!treeNodeMapping.containsKey(node)){
			treeNodeMapping.put(node,new NetworkOperatorTreeNode(node));
		}
		return treeNodeMapping.get(node);
	}
	
}
