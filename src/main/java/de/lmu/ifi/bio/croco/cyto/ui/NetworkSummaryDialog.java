package de.lmu.ifi.bio.croco.cyto.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Stack;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.lmu.ifi.bio.crco.network.NetworkSummary;
import de.lmu.ifi.bio.crco.util.CroCoLogger;

public class NetworkSummaryDialog  extends JDialog{

	private static final long serialVersionUID = 1L;

	public NetworkSummaryDialog(JFrame frame, List<NetworkSummary> summary){
		super(frame,"Network summary", true);
		
		CroCoLogger.getLogger().debug("Show network summary");
		init(summary);
		this.pack();
		this.setVisible(true);
	}
	
	public void init( List<NetworkSummary> summary){
		this.setLayout(new FlowLayout());
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
		
		Stack<NetworkSummary>elements =new Stack<NetworkSummary>();
		Stack<DefaultMutableTreeNode>treeNodes =new Stack<DefaultMutableTreeNode>();

		for(NetworkSummary s : summary){
			elements.add(s);
			treeNodes.add(root);
		}
		
		while(!elements.isEmpty()){
			
			NetworkSummary top = elements.pop();
		
			DefaultMutableTreeNode treeNode = treeNodes.pop();
			
			DefaultMutableTreeNode newTop = new DefaultMutableTreeNode(top);
			treeNode.add(newTop);
			
			if ( top.getChildren() != null){
				for(NetworkSummary child : top.getChildren()){
					
					treeNodes.add(newTop);
					elements.add(child);
				}
			}
			
		}
		DefaultTreeModel model = new DefaultTreeModel(root);
		JTree tree = new JTree();
		tree.setModel(model);
		tree.setRootVisible(false);
		tree.setPreferredSize(new Dimension(600,400));
		tree.setMinimumSize(new Dimension(600,400));
		this.add(tree);
	//	this.add(panel);
	}
}
