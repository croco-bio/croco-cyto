package de.lmu.ifi.bio.croco.cyto.ui;

import java.awt.Dimension;
import java.util.List;
import java.util.Stack;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.miginfocom.swing.MigLayout;
import de.lmu.ifi.bio.croco.network.NetworkSummary;
import de.lmu.ifi.bio.croco.util.CroCoLogger;

/**
 * Dialog to show the network operation summary.
 * @author pesch
 *
 */
public class NetworkSummaryDialog  extends JDialog{

	private static final long serialVersionUID = 1L;

	public NetworkSummaryDialog(JFrame frame, List<NetworkSummary> summary){
		super(frame,"Network summary", true);
		init(summary);
		this.setPreferredSize(new Dimension(600, 400));
		this.pack();
		this.setVisible(true);
	}
	public static void main(String[] args)
	{
	    JFrame frame = new JFrame();
	    NetworkSummaryDialog n = new NetworkSummaryDialog(frame,null);
	    
	}
	public void init( List<NetworkSummary> summary){
	    
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
		
		if ( summary != null)
		{
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
		}
		DefaultTreeModel model = new DefaultTreeModel(root);
		
		JTree tree = new JTree();
		tree.setModel(model);
		tree.setRootVisible(false);
		
        
		this.setContentPane(tree);
		
	    for (int i = 0; i < tree.getRowCount(); i++) {
	          tree.expandRow(i);
	    }
	    
	}
}
