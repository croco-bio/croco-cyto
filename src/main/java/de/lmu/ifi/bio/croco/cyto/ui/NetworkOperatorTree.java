package de.lmu.ifi.bio.croco.cyto.ui;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.slf4j.LoggerFactory;

import de.lmu.ifi.bio.crco.connector.QueryService;
import de.lmu.ifi.bio.crco.data.ContextTreeNode;
import de.lmu.ifi.bio.crco.data.NetworkHierachyNode;
import de.lmu.ifi.bio.crco.data.NetworkOperationNode;
import de.lmu.ifi.bio.crco.data.Species;
import de.lmu.ifi.bio.crco.data.exceptions.OperationNotPossibleException;
import de.lmu.ifi.bio.crco.operation.GeneSetFilter;
import de.lmu.ifi.bio.crco.operation.ReadNetwork;
import de.lmu.ifi.bio.crco.operation.Shuffle;
import de.lmu.ifi.bio.crco.operation.SupportFilter;
import de.lmu.ifi.bio.crco.operation.Transfer;
import de.lmu.ifi.bio.crco.operation.ortholog.OrthologMappingInformation;
import de.lmu.ifi.bio.crco.operation.ortholog.OrthologRepository;
import de.lmu.ifi.bio.croco.cyto.ui.transferable.NetworkHierachyNodeTransferable;
import de.lmu.ifi.bio.croco.cyto.ui.transferable.OperatorableTransferable;

public class NetworkOperatorTree extends JTree implements DropTargetListener,DragGestureListener,DragSourceListener {

	
	private static final long serialVersionUID = 1L;
	private NetworkOperatorTreeNode root;
	private DefaultTreeModel model;
	private DragSource dragSource = null;
	private QueryService service;
	
	/**
	 * Functionality provided via the context menu
	 * @author pesch
	 *
	 */
	class ContextMenu extends JPopupMenu{
		
		private static final long serialVersionUID = 1L;

		public ContextMenu(){
			final List<NetworkOperatorTreeNode> selectedNodes = NetworkOperatorTree.this.getSelectedNetworkOperatorTreeNode();
			
			JMenuItem remove = new JMenuItem("Remove");
			this.add(remove);
			//remove next from selection list
			remove.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
				
					for(NetworkOperatorTreeNode selectedNode: selectedNodes){
						NetworkOperationNode node = selectedNode.getOperatorable();
						
						if ( node == null || node.getParent() == null) continue;;
						LoggerFactory.getLogger(getClass()).debug("Parent:\t" + node.getParent());
						node.getParent().removeChild(node);
					}
					NetworkOperatorTree.this.model.reload();
				}
				
			});
			JMenuItem rename = new JMenuItem("Rename");
			this.add(rename);
			if (selectedNodes.size() == 1 ){
				rename.setEnabled(true);
			}else{
				rename.setEnabled(false);
			}
			rename.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					NetworkOperatorTreeNode node = selectedNodes.get(0);
					if ( node == null) return;
					String ret = (String)JOptionPane.showInputDialog(
		                    null,
		                    "Support Filter","Filter",
		                    JOptionPane.PLAIN_MESSAGE,
		                    null,
		                    null,
		                    node.toString());
					if ( ret != null) node.setName(ret);
				}
				
			});
			
			//shows transfer dialog
			JMenuItem transfer = new JMenuItem("Transfer");
			if (selectedNodes.size() == 1 &&  selectedNodes.get(0).getOperatorable().getTaxId() != null){
				transfer.setEnabled(true);
			}else{
				transfer.setEnabled(false);
			}
			
			this.add(transfer);
			transfer.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg) {
				    List<NetworkOperationNode> tmp = new ArrayList<NetworkOperationNode>();
				    for(NetworkOperatorTreeNode treeNode : selectedNodes)
				    {
				        tmp.add(treeNode.getOperatorable());
				    }
				    showTransferDialog(root,tmp);
				}
			});
			JMenuItem setContext = new JMenuItem("Gene Ontology context");
			
			if ( selectedNodes.size() == 1 && selectedNodes.get(0).getOperatorable() != null && ReadNetwork.class.isInstance(selectedNodes.get(0).getOperatorable().getOperator()))
			    setContext.setEnabled(true);
			else
			    setContext.setEnabled(false);
			
			setContext.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ContextTreeNode context = NetworkOperatorTree.this.goSelection();
                    ReadNetwork node = (ReadNetwork) selectedNodes.get(0).getOperatorable().getOperator();
                    
                    node.setInput(ReadNetwork.ContextTreeNode, context);
                }
			    
			});
			this.add(setContext); 
			
		}
	}
	
	private void showTransferDialog(NetworkOperatorTreeNode root,List<NetworkOperationNode> selectedNodes)
	{
	    
	    List<OrthologMappingInformation> orthologMappings = null;
        try{
            orthologMappings = service.getOrthologMappingInformation(null, null, null);
        }catch(Exception e)
        {
            throw new RuntimeException(e);
        }
        
        Set<Species> species = new HashSet<Species>();
        for(NetworkOperationNode node : selectedNodes)
        {
            species.add(node.getSpecies());
        }
        
        OrthologMappingSelection orthologMappingDialog = new OrthologMappingSelection(orthologMappings,species); 
        int ret = orthologMappingDialog.showDialog();
        
        if ( ret != JOptionPane.OK_OPTION) return; //user did not press ok
        Species target = orthologMappingDialog.getSelectedTargetSpecies();
        
        
	    for(NetworkOperationNode node : selectedNodes)
	    {
	        //NetworkOperationNode node = treeNode.getOperatorable();
	        
	        LoggerFactory.getLogger(getClass()).debug(String.format("Selected transfer from: %s to %s",node.getSpecies(),target));
            
	        
	        if ( target.equals(node.getSpecies()) )
	        {
	            if ( node.getParent() != null)
	                node.getParent().removeChild(node);
	                
	            root.getOperatorable().addChild(node);
	            continue;
	            
	        }
	        
	        List<OrthologMappingInformation> selected = new ArrayList<OrthologMappingInformation>();
	        
	        for(OrthologMappingInformation om : orthologMappings)
	        {
	            if ( 
	               (! om.getSpecies1().equals(node.getSpecies()) || !om.getSpecies2().equals(target) )&& 
	               ( ! om.getSpecies2().equals(node.getSpecies()) || !om.getSpecies1().equals(target))) continue;
                selected.add(om);
	        }
	        LoggerFactory.getLogger(getClass()).info("Selected ortholog mappings:" + selected);
	        	        
	        if ( selected.size() == 0)
	        {
	            throw new RuntimeException("No ortholog mapping available");
	        }
	        
	        Transfer transfer = new Transfer();
	        
	        transfer.setInput(Transfer.OrthologMappingInformation, selected);
	        transfer.setInput(Transfer.OrthologRepository,OrthologRepository.getInstance( service));
	      
	        try{
	            transfer.checkParameter();
	        }catch(OperationNotPossibleException e){
	            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
	            return;
	        }
	        
	    
	        
	        //Species fromSpecies = selectedOrthologMapping.getSpecies1().equals(node.getSpecies())?selectedOrthologMapping.getSpecies1():selectedOrthologMapping.getSpecies2();;
	        //Species targetSpecies = selectedOrthologMapping.getSpecies1().equals(node.getSpecies())?selectedOrthologMapping.getSpecies2():selectedOrthologMapping.getSpecies1();;
	        
	            
	        NetworkOperationNode rootOperatorable = ( (NetworkOperatorTreeNode)NetworkOperatorTree.this.getModel().getRoot()).getOperatorable();
	        
	        NetworkOperationNode transferNode = new NetworkOperationNode(rootOperatorable,target,transfer);
	        
	        if ( node.getParent() != null)
	            node.getParent().removeChild(node);
	        
	        transferNode.addChild(node);
	            
	        root.getOperatorable().addChild(transferNode);
	        
	    }
	    model.reload();
        
	}
	/**
	 * Expand or collapse all nodes 
	 * 
	 * @param tree
	 * @param expand
	 */
	public void expandAll( boolean expand) {
		Stack<TreeNode> stack = new Stack<TreeNode>();
		stack.add((NetworkOperatorTreeNode)this.getModel().getRoot());
		TreePath path =null;
		while(!stack.isEmpty()){

			TreeNode top = stack.pop();
			if ( path == null)
				path = new TreePath(top);
			else{
				path = path.pathByAddingChild(top);
				this.expandPath(path);
			}
			for(int i = 0 ; i <top.getChildCount();i++){
				stack.add((NetworkOperatorTreeNode) top.getChildAt(i));
			}
		}
		

	}

	public List<NetworkOperatorTreeNode> getSelectedNetworkOperatorTreeNode(){
		List<NetworkOperatorTreeNode> selected = new ArrayList<NetworkOperatorTreeNode>();
		if ( this.getSelectionPaths() == null) return selected;
		
		 for(TreePath path : this.getSelectionPaths() ) {
			 selected.add(((NetworkOperatorTreeNode)path.getLastPathComponent()));
		 }
		 return selected;
	}

	public NetworkOperatorTree(QueryService service){
		DropTarget target = new DropTarget(this, this); 
		root = new NetworkOperatorTreeNode(new NetworkOperationNode());
		model = new DefaultTreeModel(root);
		this.service = service;
		this.setRootVisible(false);
		dragSource = new DragSource();
		//DragGestureRecognizer recognizer = 
		dragSource.createDefaultDragGestureRecognizer(this,DnDConstants.ACTION_MOVE, this);
	   // setCellRenderer(new NetworkTreeRenderer());
		this.setModel(model);

		MouseListener ml = new MouseAdapter() {
		     public void mousePressed(MouseEvent e) {
		    	 if ( e.getButton() != MouseEvent.BUTTON1 && NetworkOperatorTree.this.getSelectedNetworkOperatorTreeNode().size() > 0){ //right click? Button2?
	
			    	 ContextMenu contextMenu = new ContextMenu();
			    	 contextMenu.setVisible(true);
			    	 contextMenu.show(NetworkOperatorTree.this,e.getX(), e.getY());
			    	 
		    	 }
		     }
		 };
		 this.addMouseListener(ml);

	}


	public void addNode(NetworkOperationNode operationNode){

		root.getOperatorable().addChild(operationNode);
		
		model.reload();

	}


	@Override
	public void dragEnter(DropTargetDragEvent e) {
	//	e.acceptDrag(1);
	//    System.out.println("HERE");
	}

	@Override
	public void dragExit(DropTargetEvent e) {

	}

	@Override
	public void dragOver(DropTargetDragEvent dsde) {
		dsde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
	}
	private boolean isAddable(NetworkOperationNode root, List<NetworkOperationNode> newChildren){
		
	    if ( root.getOperator() != null){
            if ( 
                (
                        root.getOperator().getClass().equals(SupportFilter.class)  || root.getOperator().getClass().equals(Shuffle.class) || root.getOperator().getClass().equals(Transfer.class) ||  root.getOperator().getClass().equals(GeneSetFilter.class) )  
                &&
                root.getChildren().size()+newChildren.size() >1){
                LoggerFactory.getLogger(getClass()).debug(String.format("%s has already a child",root.getOperator().getClass().getSimpleName()));
                return false;
            }
        }
        
	    
		
		for(NetworkOperationNode newNode:newChildren ){
			boolean ret = isAddable(root,newNode);
			if ( !ret ) return ret;
			LoggerFactory.getLogger(getClass()).debug("Remove:\t" + newNode + " from:\t" + newNode.getParent()); 
		}
		
		
		return true;

	}
	private boolean isAddable(NetworkOperationNode root, NetworkOperationNode newNode){
		if ( root.getChildren().contains(newNode)){
			LoggerFactory.getLogger(getClass()).debug("Node already contained");
			return false;
		}
		if ( root.getOperator() != null){
			if ( root.getOperator().getClass().equals(ReadNetwork.class)){
				LoggerFactory.getLogger(getClass()).debug("Cannot add to ReadNetwork");
				return false;
			}
			if ( 
				(
						root.getOperator().getClass().equals(SupportFilter.class)  || root.getOperator().getClass().equals(Shuffle.class) || root.getOperator().getClass().equals(Transfer.class) ||  root.getOperator().getClass().equals(GeneSetFilter.class) )  
				&&
				root.getChildren().size() !=0){
				LoggerFactory.getLogger(getClass()).debug(String.format("%s has already a child",root.getOperator().getClass().getSimpleName()));
				return false;
			}
		}
		
		if (root.getTaxId() != null && newNode.getTaxId() != null && !root.getTaxId().equals(newNode.getTaxId())){
			 JOptionPane.showMessageDialog(
					 null, 
					 String.format("Operation not possible between the two different species %s and %s. Transfer one of the species (right click Transfer in the network selection list).",Species.getSpecies(root.getTaxId()).getName(),Species.getSpecies(newNode.getTaxId()).getName()),
					 "Error", 
					 JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		
		NetworkOperationNode parent = root;
		while(parent != null){
			if ( parent.equals(newNode)){
				return false;
			}
			
			parent =parent.getParent();
		}
		

		return true;
	}

	private NetworkOperatorTreeNode getSelectedDropRoot(DropTargetDropEvent e){
		Point loc = e.getLocation();
		NetworkOperatorTreeNode ret = null;
		
		TreePath destinationPath = getPathForLocation(loc.x, loc.y);
		if ( destinationPath == null || destinationPath.getLastPathComponent() == null   ){ //add to root
			ret = this.root;
		}else{
			ret =(NetworkOperatorTreeNode) destinationPath.getLastPathComponent();
		}
		return ret;
	}
	
	private void updateRoot(NetworkOperationNode root){
		if ( root.equals(this.root.getOperatorable())) return;
		while(true){
			if ( root.getParent().equals(this.root.getOperatorable())) break;
			root =  root.getParent();
		}
		
		Stack<NetworkOperationNode> toProcess = new Stack<NetworkOperationNode>();
		toProcess.add(root);
		while(!toProcess.isEmpty()){
			NetworkOperationNode top = toProcess.pop();
			HashSet<Species> species = new HashSet<Species>();
			
			for(NetworkOperationNode child : top.getChildren()){
				toProcess.add(child);
				if ( child.getSpecies().getTaxId() != null) species.add(child.getSpecies());
			}
			if (! Transfer.class.isInstance(top.getOperator()))
			{
			    if ( species.size() == 1){
	                top.setSpecies(species.iterator().next());
	            }    
			}
			
		}
	}
	private NetworkOperationNode getNetworkOperationNode(NetworkHierachyNode nh)
	{
	    ReadNetwork reader = new ReadNetwork();
        reader.setInput(ReadNetwork.GlobalRepository, false);
        reader.setInput(ReadNetwork.QueryService, service);
        reader.setInput(ReadNetwork.NetworkHierachyNode, nh);
       /*
        if( context != null){
            reader.setInput(ReadNetwork.ContextTreeNode, context);
        }
        */
        NetworkOperationNode node = new NetworkOperationNode(null,nh.getTaxId(),reader);
        return node;
       
	}
	
	public ContextTreeNode previous_context = null;
    
	private ContextTreeNode goSelection()
	{
	    try {
	        GoBrowser browser = new GoBrowser(null,previous_context,service);
	        previous_context = browser.showDialog();
	        LoggerFactory.getLogger(getClass()).debug("Selected context:" + previous_context);
	    } catch (Exception e1) {
	        previous_context = null;
	        e1.printStackTrace();
	        LoggerFactory.getLogger(getClass()).error(e1.getMessage() + ".Set context to none");
	    }
	    
	    return previous_context;
	}
	public void addNetworks(Collection<NetworkHierachyNode> n,NetworkOperationNode selectedRoot ){
	  
		List<NetworkOperationNode> networkOperationNodes = new ArrayList<NetworkOperationNode>();
		for(NetworkHierachyNode nh : n){
		    
		    networkOperationNodes.add(getNetworkOperationNode(nh));
		}
		
		LoggerFactory.getLogger(getClass()).debug(String.format("Move element: %s to %s",n.toString(),selectedRoot.toString()));
		LoggerFactory.getLogger(getClass()).debug("Insert nodes to root");
	
	
		if (! NetworkOperationNode.class.isInstance(selectedRoot )){
			LoggerFactory.getLogger(getClass()).error("Networks can not be added to networks");
		}
		
		boolean isAddable = isAddable(selectedRoot,networkOperationNodes);	
		if ( !isAddable) return;
		
		for(NetworkOperationNode node :networkOperationNodes ){
			(selectedRoot).addChild(node);
		}
		updateRoot(selectedRoot);
		
		model.reload();
	}
	public void addNetworks(List<NetworkHierachyNode> n,NetworkOperatorTreeNode selectedRoot ){
		this.addNetworks(n,selectedRoot.getOperatorable());
	}
	
	@Override
	public void drop(DropTargetDropEvent e) {
		Transferable tr = e.getTransferable();
		if (tr.isDataFlavorSupported( NetworkHierachyNodeTransferable.INFO_FLAVOR) ) { //moving networks
			try {
				
				List<NetworkHierachyNode> n = (List<NetworkHierachyNode>) tr.getTransferData( NetworkHierachyNodeTransferable.INFO_FLAVOR );
				NetworkOperatorTreeNode selectedRoot = getSelectedDropRoot(e);
				
		        Set<Integer> taxIds = new HashSet<Integer>();
		        Integer taxId = null;
		        for(NetworkHierachyNode newNode:n ){
		            taxIds.add(newNode.getTaxId());
		            taxId = newNode.getTaxId();
		        }
		        if ( selectedRoot.getOperatorable().getTaxId() != null && !selectedRoot.getOperatorable().getTaxId().equals(taxId) ){
		            JOptionPane.showMessageDialog(
		                     null, 
		                     String.format("Operation not possible between the two different species %s and %s. Transfer one of the species (right click Transfer in the network selection list).",Species.getSpecies(selectedRoot.getOperatorable().getTaxId()).getName(),Species.getSpecies(taxId).getName()),
		                     "Error", 
		                     JOptionPane.WARNING_MESSAGE);
		            return;
		        }
		        
		        if ( taxIds.size() != 1 )
		        {
		            List<NetworkOperationNode> nops = new ArrayList<NetworkOperationNode>();
		            
		            for(NetworkHierachyNode newNode:n )
		            {
		                nops.add(getNetworkOperationNode(newNode));
		            }
		            this.showTransferDialog(selectedRoot,nops);
		            updateRoot(selectedRoot.getOperatorable());
		        }else{
		            addNetworks(n,selectedRoot);
		        }
				
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
			
			paintImmediately(getVisibleRect());
			e.getDropTargetContext().dropComplete(true);
		} else if (tr.isDataFlavorSupported(OperatorableTransferable.INFO_FLAVOR)){ //moving nodes in operator tree
			try {
				LoggerFactory.getLogger(getClass()).debug("Data flower operatorable transfer");
				NetworkOperationNode n = (NetworkOperationNode) tr.getTransferData( OperatorableTransferable.INFO_FLAVOR );
				NetworkOperatorTreeNode selectedRoot = getSelectedDropRoot(e);
				
				LoggerFactory.getLogger(getClass()).debug(String.format("Move element: %s to %s",n.toString(),selectedRoot.toString()));
				if (! NetworkOperationNode.class.isInstance(selectedRoot.getOperatorable())){
					LoggerFactory.getLogger(getClass()).debug("Can not move to root" + selectedRoot.equals(root));
					return;
				}
				
				boolean isAddable = isAddable((NetworkOperationNode)selectedRoot.getOperatorable(),n);	
				if ( !isAddable) return;
				n.getParent().removeChild(n); 
				LoggerFactory.getLogger(getClass()).debug("Add node:\t" + n + " to:\t" + root); 
					
				((NetworkOperationNode)selectedRoot.getOperatorable()).addChild(n);
				updateRoot( selectedRoot.getOperatorable());
				
				model.reload();
				
				paintImmediately(getVisibleRect());
				e.getDropTargetContext().dropComplete(true);
			} catch (UnsupportedFlavorException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}else{
			System.err.println("Rejected");
			e.rejectDrop();
		}
		
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent arg0) {
	}

	@Override
	public void dragGestureRecognized(DragGestureEvent e) {
		TreePath path = this.getSelectionPath();
		if ((path == null) || (path.getPathCount() <= 1)) {
			return;
		}

		NetworkOperatorTreeNode node = (NetworkOperatorTreeNode) path.getLastPathComponent();
		
		LoggerFactory.getLogger(getClass()).debug("Move:\t" + node);
		OperatorableTransferable toTransfer = null;
		try {
			toTransfer = new OperatorableTransferable(this.getSelectedNetworkOperatorTreeNode().get(0).getOperatorable());
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		dragSource.startDrag(e, DragSource.DefaultMoveNoDrop, toTransfer, this);


	}
	@Override
	public void dragEnter(DragSourceDragEvent e) {
		DragSourceContext context = e.getDragSourceContext();  
		context.setCursor(DragSource.DefaultMoveDrop);    
		/*
		//Point p = e.getLocation();  
		DragSourceContext context = e.getDragSourceContext();  
		//Component comp = context.getComponent();  
		Point loc = e.getLocation();
		NetworkOperatorTreeNode ret = null;
		
		TreePath destinationPath = getPathForLocation(loc.x, loc.y);
		if ( destinationPath == null || destinationPath.getLastPathComponent() == null   ){ //add to root
			ret = this.root;
		}else{
			ret =(NetworkOperatorTreeNode) destinationPath.getLastPathComponent();
		}
		System.out.println("Drag enter:" + ret);
		int myaction = e.getDropAction();  

		if( (myaction != 0)) {   

			context.setCursor(DragSource.DefaultMoveDrop);      
		} else {  

			context.setCursor(DragSource.DefaultCopyNoDrop);          
		}  
		*/

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

}
