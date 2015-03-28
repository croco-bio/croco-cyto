package de.lmu.ifi.bio.croco.cyto;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
import org.cytoscape.io.webservice.SearchWebServiceClient;
import org.cytoscape.io.webservice.swing.AbstractWebServiceGUIClient;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.internal.utils.ServiceUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.bio.crco.connector.BufferedService;
import de.lmu.ifi.bio.crco.connector.QueryService;
import de.lmu.ifi.bio.crco.connector.RemoteWebService;
import de.lmu.ifi.bio.crco.data.CroCoNode;
import de.lmu.ifi.bio.crco.data.CroCoNode.GeneralFilter;
import de.lmu.ifi.bio.crco.data.Entity;
import de.lmu.ifi.bio.crco.data.NetworkHierachyNode;
import de.lmu.ifi.bio.crco.data.NetworkOperationNode;
import de.lmu.ifi.bio.crco.data.NetworkType;
import de.lmu.ifi.bio.crco.data.Option;
import de.lmu.ifi.bio.crco.data.Species;
import de.lmu.ifi.bio.crco.network.Network;
import de.lmu.ifi.bio.crco.network.NetworkSummary;
import de.lmu.ifi.bio.crco.operation.Difference;
import de.lmu.ifi.bio.crco.operation.GeneSetFilter;
import de.lmu.ifi.bio.crco.operation.GeneSetFilter.FilterType;
import de.lmu.ifi.bio.crco.operation.GeneralOperation;
import de.lmu.ifi.bio.crco.operation.Intersect;
import de.lmu.ifi.bio.crco.operation.OperationUtil;
import de.lmu.ifi.bio.crco.operation.ReadNetwork;
import de.lmu.ifi.bio.crco.operation.SupportFilter;
import de.lmu.ifi.bio.crco.operation.Transfer;
import de.lmu.ifi.bio.crco.operation.Union;
import de.lmu.ifi.bio.crco.operation.ortholog.OrthologRepository;
import de.lmu.ifi.bio.crco.operation.progress.ProgressInformation;
import de.lmu.ifi.bio.crco.operation.progress.ProgressListener;
import de.lmu.ifi.bio.crco.util.CroCoLogger;
import de.lmu.ifi.bio.crco.util.CroCoProperties;
import de.lmu.ifi.bio.croco.cyto.converter.CytoscapeTransformer;
import de.lmu.ifi.bio.croco.cyto.ui.Help;
import de.lmu.ifi.bio.croco.cyto.ui.NetworkInfoList;
import de.lmu.ifi.bio.croco.cyto.ui.NetworkOperatorTree;
import de.lmu.ifi.bio.croco.cyto.ui.NetworkOperatorTreeNode;
import de.lmu.ifi.bio.croco.cyto.ui.NetworkSummaryDialog;
import de.lmu.ifi.bio.croco.cyto.ui.NetworkTree;
import de.lmu.ifi.bio.croco.cyto.ui.NetworkTree.NetworkHierachyTreeNode;
import de.lmu.ifi.bio.croco.cyto.util.CytoscapeProperties;


public class CcPath extends AbstractWebServiceGUIClient  implements NetworkImportWebServiceClient, SearchWebServiceClient{
	private static int MAX_BEFORE_WARN = 20;
	private static String description ="CroCo enables the comparative network analysis on both standard conventional global and context-specific regulatory networks. CroCo is a tool suite to conduct differential analysis on derived condition specific networks from ENCODE ChIP-seq/ChIP-chip and DNase-seq together with static network for eukaryotic model organisms.";;
    private QueryService service;    
	
	public static void main(String[] args) throws Exception{
		
	    CroCoProperties.init(CroCoLogger.class.getClassLoader().getResourceAsStream("connet-croco.config"));
        
        
		CcPath ccPath = new CcPath(null);
		JFrame f = new JFrame();
		f.setTitle("CroCo Test");
		f.add(ccPath.gui);
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.pack();
		f.setResizable(false);
		f.setVisible(true);
	}
	private BundleContext context;
	public CcPath(BundleContext context) throws Exception {
		super("http://services.bio.ifi.lmu.de/croco", "CroCo-Cyto",description );
		LoggerFactory.getLogger(getClass()).info("Init web service client");
		gui = new JPanel();
		
		this.context = context;
		this.init();
		
	}
	private void init() throws Exception{

		JPanel mainPanel = (JPanel) gui;
		mainPanel.setPreferredSize(new Dimension (900,640));
		mainPanel.setLayout (new BorderLayout());

		JPanel view = new JPanel(new MigLayout());
		
		final JPanel content = new JPanel(new MigLayout());
		JPanel connectionPane = new JPanel(new MigLayout());
		connectionPane.add(new JLabel("CroCo web service:"));
		final JTextField connectionField = new JTextField();
        final JTextField bufferDir = new JTextField();

		Image startUpImage = null;
		try {
		    
			connectionField.setText(CroCoProperties.getInstance().getValue(CytoscapeProperties.urlStr));
			startUpImage = ImageIO.read(CroCoLogger.class.getClassLoader().getResourceAsStream("startup-img.png"));
			 File cytoBaseDir = new File(System.getProperty("user.home"),CyProperty.DEFAULT_PROPS_CONFIG_DIR + "/croco");
			 bufferDir.setText(cytoBaseDir.toString());
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		
		connectionPane.add(connectionField,"gapleft 30, width 500, grow");
		
		JButton connectBtn = new JButton("Connect");
		
		connectBtn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String url = connectionField.getText();
				File buffer = new File(bufferDir.getText());
				if (! buffer.exists())
				{
				    boolean ret = buffer.mkdirs();
				    if ( !ret )
				    {
				        JOptionPane.showMessageDialog(null,"Check dir", "Cannot create buffer dir:" +buffer, JOptionPane.ERROR_MESSAGE);
				        return;
				    }    
				}
				
				try{
				    
			        RemoteWebService remoteService = new RemoteWebService(url);
                    Long version = remoteService.getVersion();
                    LoggerFactory.getLogger(getClass()).info("Service version:" + version);

			        service = new BufferedService(remoteService, buffer );
			        
					
				}catch(Exception ex){
				    ex.printStackTrace();
				    JOptionPane.showMessageDialog(null, "Cannot connect to:" + url + "\n" + ex.getMessage(), "Connection failure" , JOptionPane.ERROR_MESSAGE);
					LoggerFactory.getLogger(getClass()).error(ex.getMessage());
					return;
				}
				
				content.removeAll();
				createNetworkView(content);
				//view.repaint();
				content.revalidate();
				//gui.repaint();
			}
			
		});
		connectionPane.add(connectBtn,"gapleft 15");
		JButton help = new JButton("Help");
		
		connectionPane.add(help,"gapleft 50,wrap");
		help.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				Help help = new Help(null);				
				help.init();
				
				help.setVisible(true);
				
			}
			
		
		});
		connectionPane.add(new JLabel("Buffer dir:"));
        
		connectionPane.add(bufferDir,"gapleft 30, width 500, grow,wrap");
        
		JLabel info =new JLabel("<html><font color='#AA000000'>The first connection attempt to the croco-repo may take a few minutes.</font></html>");
		connectionPane.add(info,"span,center,wrap");
		connectionPane.setBorder(BorderFactory.createEtchedBorder());
		view.add(connectionPane,"span,wrap");
		view.add(content);
		if ( startUpImage != null){
			JLabel imgStartUplnl = new JLabel();
			imgStartUplnl.setIcon(new ImageIcon(startUpImage));
			content.add(imgStartUplnl,"gapleft 175");
		}
		    
	     mainPanel.add(view, BorderLayout.CENTER);     
	        
	 }

	 public void createNetworkView(JPanel view){
	
		 
		 
		 final NetworkTree networkTree = new NetworkTree(service);

		 JScrollPane scrp = new JScrollPane(networkTree);
		 view.add(new JLabel("<html><p style='font-size:1.2em'>croco-repo</p></html>"));
		 view.add(new JLabel("<html><p style='font-size:1.2em'>Network details</p></html>"));
		 JLabel example = new JLabel("<HTML><FONT color=\"#000099\"><U>Load example</U></FONT></HTML>");
		 example.setOpaque(false);
		 example.setBackground(Color.WHITE);
		 example.setForeground(Color.BLUE);
		 example.setCursor(new Cursor(Cursor.HAND_CURSOR));
		 example.setToolTipText("Common network inferred from Mouse MEL and Human K562 open chromatin networks");
		 
		 view.add(example, "align right,wrap");
		 
		 view.add(scrp, "width 500, height 500");
		 final JLabel image = new JLabel();
		 
		 final NetworkInfoList networkInfo = new NetworkInfoList(service);
		 JPanel infoView = new JPanel(new MigLayout()); 
		 infoView.add(networkInfo,"width 370, wrap");
		 infoView.add(image,"wrap");
		 
		 JScrollPane scrp2 = new JScrollPane(infoView);
		  view.add(scrp2,"width 400, height 500, span 2,wrap");
		 
		  
		  final NetworkOperatorTree operations = new NetworkOperatorTree(service);
		  JScrollPane scrp3 = new JScrollPane(operations);
		  JPanel operationsView = new JPanel();
	
		  List<Class<? extends GeneralOperation>> possibleOperations = new ArrayList<Class<? extends GeneralOperation>>();
		  possibleOperations.add(Union.class);
		  possibleOperations.add(Intersect.class);
		  possibleOperations.add(SupportFilter.class);
		  possibleOperations.add(Difference.class);
		  possibleOperations.add(GeneSetFilter.class);
		  
		  operationsView.setLayout(new MigLayout());
		  
		  CroCoLogger.getLogger().info("Register operations");
		  JScrollPane scrp4 = new JScrollPane(operationsView);
		  view.add(new JLabel("Network Selection List"));
		  view.add(new JLabel("Network Operation Panel"),"wrap");
		  
		  view.add(scrp3,"width 500, height 400,");
		  view.add(scrp4,",width 220,height 400");
		  
		  final JButton okButton = new JButton("Produce final network");
		  okButton.setEnabled(false);
		  okButton.setToolTipText("Select networks from the croco-repo and drop them into the network selection list");
		  view.add(okButton,"align right,grow,wrap");
		  
		
		  
	      example.addMouseListener(new MouseAdapter() {
	            @Override
	            public void mouseClicked(MouseEvent e) {
	            	NetworkOperationNode parent = ((NetworkOperatorTreeNode)operations.getModel().getRoot()).getOperatorable();
					try{
					    LoggerFactory.getLogger(CcPath.class).error("Set transfer node");
						Transfer transferOperation = new Transfer();
		            	transferOperation.setInput(Transfer.OrthologMappingInformation,service.getOrthologMappingInformation(null, Species.Human, Species.Mouse));
		    			transferOperation.setInput(Transfer.OrthologRepository,OrthologRepository.getInstance(service));
		            	
		            	NetworkOperationNode transfer = new NetworkOperationNode(parent,Species.Human.getTaxId(),transferOperation);
		            	
		            	LoggerFactory.getLogger(CcPath.class).error("Read MEL");
                        
		            	GeneralFilter f1 = new GeneralFilter(Option.TaxId,10090+"");
		            	GeneralFilter f2 = new GeneralFilter(Option.NetworkType,NetworkType.OpenChrom.name());
		            	GeneralFilter f3 = new GeneralFilter(Option.ConfidenceThreshold,"1.0E-6");
		            	GeneralFilter f4 = new GeneralFilter(Option.MotifSet,"Combined set");
		            	GeneralFilter f5 = new GeneralFilter(Option.OpenChromType,"DNase");
		            	GeneralFilter f6 = new GeneralFilter(Option.cellLine,"MEL");
                        
		            	CroCoNode mel = networkTree.getRoot().getNode("MEL", f1,f2,f3,f4,f5,f6);
		            	
						NetworkOperationNode melInterst = new NetworkOperationNode(parent,Species.Mouse.getTaxId(),new Intersect());
						
						operations.addNetworks(mel.getNetworks(), melInterst);
						transfer.addChild(melInterst);
						
						LoggerFactory.getLogger(CcPath.class).error("Read K562");
                        
						f1 = new GeneralFilter(Option.TaxId,9606+"");
						f6 = new GeneralFilter(Option.cellLine,"K562");
						CroCoNode k562 = networkTree.getRoot().getNode("MEL", f1,f2,f3,f4,f5,f6);
                        
						
						NetworkOperationNode k562Interest= new NetworkOperationNode(parent,Species.Human.getTaxId(),new Intersect());
						
						operations.addNetworks(k562.getNetworks(), k562Interest);
                        
						NetworkOperationNode intersect = new NetworkOperationNode(parent,Species.Human.getTaxId(),new Intersect());
			            intersect.addChild(transfer);
			            intersect.addChild(k562Interest);
			            operations.addNode(intersect);
			            
			            operations.expandAll(true);
					}catch(Exception ex){
						LoggerFactory.getLogger(CcPath.class).error(ex.toString());
						JOptionPane.showMessageDialog(null, String.format("Could not load example (%s)",ex.toString()),"Error",  JOptionPane.WARNING_MESSAGE);
					}
	            }
	      });

		  networkTree.addMouseListener ( new MouseAdapter ()
			{
				public void mousePressed ( MouseEvent e )
				{
					if ( SwingUtilities.isRightMouseButton ( e ) )
					{
						if ( networkTree.getSelectionCount() > 0){
							TreePath path = networkTree.getPathForLocation ( e.getX (), e.getY () );

							Rectangle pathBounds = networkTree.getUI ().getPathBounds ( networkTree, path );
							if ( pathBounds != null && pathBounds.contains ( e.getX (), e.getY () ) )
							{
								JPopupMenu menu = new JPopupMenu ();
								JMenuItem item = new JMenuItem ( "Add selected networks to selection list" );
								item.addActionListener(new ActionListener(){
									@Override
									public void actionPerformed(ActionEvent e) {
										NetworkHierachyTreeNode selected = networkTree.getSelectedNode();
										operations.addNetworks(new ArrayList<NetworkHierachyNode>(selected.getOperatorable().getNetworks()),(NetworkOperatorTreeNode)operations.getModel().getRoot() );

									}
								});
								menu.add (item  );
								menu.show ( networkTree, pathBounds.x, pathBounds.y + pathBounds.height );
							}
						}
					}
				}
			} );
		  operations.getModel().addTreeModelListener(new TreeModelListener(){

			 private void change(TreeModelEvent e){
				 //check if at least one network
				 DefaultTreeModel model = (DefaultTreeModel) e.getSource();
				 Stack<NetworkOperatorTreeNode> stack = new Stack<NetworkOperatorTreeNode>();
				 stack.add((NetworkOperatorTreeNode)model.getRoot());
				 boolean canProduce = false;
				 while(!stack.isEmpty()){
					 NetworkOperatorTreeNode top = stack.pop();
					 if ( top.getOperatorable() != null && top.getOperatorable().getOperator() instanceof ReadNetwork) {
						 canProduce = true;
						 break;
					 }
					 if ( top.getChildCount() > 0){
						 for(int i = 0 ; i< top.getChildCount();i++){
							 stack.add((NetworkOperatorTreeNode) top.getChildAt(i));
						 }
					 }
				 }
				 if ( canProduce){
					 okButton.setEnabled(true);
				 }else{
					 okButton.setEnabled(false);
				 }
			 }
			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				change(e);
			}

			@Override
			public void treeNodesInserted(TreeModelEvent e) {
				change(e);
				
			}

			@Override
			public void treeNodesRemoved(TreeModelEvent e) {
				change(e);
				
			}

			@Override
			public void treeStructureChanged(TreeModelEvent e) {
				change(e);
				
			}
			  
		  });
		  
		  for(final Class<? extends GeneralOperation> possibleOperation : possibleOperations){

			  JButton operation = new JButton( possibleOperation.getSimpleName());
			  if ( possibleOperation == Union.class){
				  operation.setToolTipText("Adds the Union operation");
			  }else if ( possibleOperation == Intersect.class){
				  operation.setToolTipText("Adds the Intersect operation");
			  }else if ( possibleOperation == SupportFilter.class){
				  operation.setToolTipText("Adds the Support Filter  operation");
			  }else if ( possibleOperation == Difference.class){
				  operation.setToolTipText("Adds the difference operation ");
			  }else if ( possibleOperation == GeneSetFilter.class){
				  operation.setToolTipText("Adds the Gene Set Filter operation");
			  }
			  operationsView.add(operation,"grow,center, wrap");
			
			  operation.addActionListener(new ActionListener(){

				  @Override
				  public void actionPerformed(ActionEvent e) {
					 try {
						final GeneralOperation op = possibleOperation.newInstance();
					
						Integer taxId = null;
						if ( op.getClass().equals(GeneSetFilter.class)){
							final JDialog dialog = new JDialog(new JFrame(),ModalityType.APPLICATION_MODAL);
							dialog.setLayout(new MigLayout());
							final JTextArea text = new JTextArea();
							JScrollPane sPane = new JScrollPane(text);
							dialog.setResizable(false);
							dialog.add(new JLabel("<html>Insert Ensembl gene IDs of interest (e.g. ENSG00000102974, ENSG00000141510).<br>The edges which do not containing one of the gene IDs of interest will be filtered.</html>"),"wrap");
							dialog.add(sPane, "width 200, height 200, span 2,grow, wrap");
							
							JButton okButton = new JButton("Ok");
							final Set<Entity> ret = new HashSet<Entity>();
							okButton.addActionListener(new ActionListener(){

								@Override
								public void actionPerformed(ActionEvent e) {
									
									for(String entity : text.getText().split("\\n|,")){
										if ( entity.trim().length() > 0){
											ret.add(new Entity(entity));
										}
									}
									dialog.dispose();
								}
								
							});
							JButton exampleButton = new JButton("Example (Human KEGG Leukemia pathway genes)");
							exampleButton.addActionListener(new ActionListener(){

								@Override
								public void actionPerformed(ActionEvent e) {
									InputStream is = CroCoLogger.class.getClassLoader().getResourceAsStream("ChronicMyeloidLeukemia.human");
									
									//URL url = ClassLoader.getSystemResource("ChronicMyeloidLeukemia.human");
									try {
										StringBuffer buffer = new StringBuffer();
										int c = -1;
										while( (c  = is.read()) != -1){
											buffer.append((char)c);
										}
										
										text.setText(new String(buffer));
									} catch (IOException e1) {
										throw new RuntimeException(e1);
									} 
								}
								
							});
							dialog.add(exampleButton );
							dialog.add(okButton);
							dialog.pack();
							dialog.setVisible(true);
							if ( ret.size() == 0) return;
							op.setInput(GeneSetFilter.genes, ret);
							op.setInput(GeneSetFilter.filterType, FilterType.OnSideFilter);
							
						}else if ( op.getClass().equals(SupportFilter.class)){
							String ret = (String)JOptionPane.showInputDialog(
				                    null,
				                    "Support Filter","Filter",
				                    JOptionPane.PLAIN_MESSAGE,
				                    null,
				                    null,
				                    "10");
							Integer filter = null;
							try{
								filter = Integer.valueOf(ret);
							}catch(NumberFormatException ex){
								JOptionPane.showMessageDialog(null, String.format("%s is not a number.",ret));
								return;
							}
							op.setInput(SupportFilter.Support, filter);
						}
						NetworkOperationNode parent = ((NetworkOperatorTreeNode)operations.getModel().getRoot()).getOperatorable();
						
						NetworkOperationNode node = new NetworkOperationNode(parent,taxId,op);
						operations.addNode(node);
					} catch (InstantiationException e1) {
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						e1.printStackTrace();
					}

				  }

			  });

			  


		  }
		  
	
		  
		  

		  okButton.addActionListener(new ActionListener(){
			  
			@Override
			public void actionPerformed(ActionEvent arg) {
				
				final List<NetworkSummary> summaries = new ArrayList<NetworkSummary>();
				
				class NetworkSummaryTask extends AbstractTask{
					@Override
					public void run(TaskMonitor taskMonitor) throws Exception {
						taskMonitor.setProgress(1); 
						taskMonitor.setStatusMessage("Show network summary dialog");
						Thread thread = new Thread(){
							@Override
							public void run() {
								new NetworkSummaryDialog(null, summaries); 
							 }
						};
						thread.start();
					}
					
				}
				
				class NetworkProcessTask extends AbstractTask {
					private ProgressInformation pi;
					private NetworkOperationNode root;
					private Network network = null;
					
					public NetworkProcessTask(final NetworkOperationNode root){
						this.root = root;
						this.pi = new ProgressInformation(OperationUtil.getNumberOfOperations(root));
							
					}
					@Override
					public void cancel() {
						super.cancel();
						
						pi.setKill(true);
						
					}
					@Override
					public void run(final TaskMonitor taskMonitor)   {
						taskMonitor.setTitle("Process");
						taskMonitor.setProgress(0);
						
						
						CroCoLogger.getLogger().debug("Register monitor");
						final int k = OperationUtil.getNumberOfOperations(root);
						
						pi.addListener(new ProgressListener(){
							int n = 0;
							@Override
							public void update(final GeneralOperation operation) {
								n+=1;
								taskMonitor.setProgress((double)n/(double)k);
								if ( ReadNetwork.class.isInstance(operation)){
									taskMonitor.setStatusMessage("Read network:" + operation.getParameter(ReadNetwork.NetworkHierachyNode).getName());
								}else{
									taskMonitor.setStatusMessage(operation.getClass().getSimpleName());
								}
								
							}
						});
						try{
						    network = OperationUtil.process(service,root,pi);
						}catch(Exception e){
							LoggerFactory.getLogger(CcPath.class).error("Cannot process",e);
						}
						if ( cancelled){
							LoggerFactory.getLogger(CcPath.class).debug("Operation stopped");
							return; 
						}
						if ( network == null){
							LoggerFactory.getLogger(CcPath.class).error("No network generated");
							return;
						}
						summaries.add(network.getNetworkSummary());
						CytoscapeTransformer transformer = new CytoscapeTransformer(context);

						LoggerFactory.getLogger(CcPath.class).debug("Processing done.");
						
					
						CyNetwork cytoNetwork = transformer.convert(network);

						CyNetworkManager manager = ServiceUtil.getService(context,CyNetworkManager.class,null);
						manager.addNetwork(cytoNetwork);

						// Create a new network view
						CyNetworkViewFactory networkViewFactory = ServiceUtil.getService(context, CyNetworkViewFactory.class,null);
						CyNetworkView myView = networkViewFactory.createNetworkView(cytoNetwork);

						// Add view to Cytoscape
						CyNetworkViewManager networkViewManager = ServiceUtil.getService(context, CyNetworkViewManager.class,null);
						 networkViewManager.addNetworkView(myView);
						CroCoLogger.getLogger().info("stop");   
						
						
						   
						taskMonitor.setProgress(1.0);
						
						
						//new NetworkSummaryDialog(null,network.getNetworkSummary());
						
					}
					
				}

				
				//check 
				Stack<NetworkOperatorTreeNode> stack = new Stack<NetworkOperatorTreeNode>();
				final NetworkOperatorTreeNode root = (NetworkOperatorTreeNode)operations.getModel().getRoot();
				
				int action = JOptionPane.YES_OPTION;
				stack.add(root);
				while(!stack.isEmpty()){
					NetworkOperatorTreeNode top = stack.pop();
					if ( top.getChildCount() > MAX_BEFORE_WARN) {
						 action = JOptionPane.showOptionDialog(null,  String.format("The node %s has %s children. This may leads to an out of memory exception. Do you want to process?",top.toString(),top.getChildCount()), "",
							        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,null,null,null
							       );
					
						
						
					}
					for(int  i  =  0 ; i< top.getChildCount();i++){
						NetworkOperatorTreeNode child = (NetworkOperatorTreeNode) top.getChildAt(i);
						stack.add(child);
					}
				}
				
			
				if ( action != JOptionPane.YES_OPTION ) return;
				DialogTaskManager dialogTaskManager = ServiceUtil.getService(context, DialogTaskManager.class,null);
				
				AbstractTaskFactory factory = new  AbstractTaskFactory(){

					@Override
					public TaskIterator createTaskIterator() {
						AbstractTask[] tasks = new AbstractTask[root.getChildCount()+1];
						for(int i = 0 ; i < root.getChildCount(); i++){
							NetworkOperatorTreeNode node = (NetworkOperatorTreeNode)root.getChildAt(i);
							tasks[i] =new  NetworkProcessTask(node.getOperatorable());
							
						}
						tasks[tasks.length-1] = new NetworkSummaryTask();
						return new TaskIterator(tasks);
					}
					
				};
				//start task
				dialogTaskManager.execute(factory.createTaskIterator() );
				
				
			}
			  
		  });
		  
		  operations.addTreeSelectionListener(new TreeSelectionListener(){

			  @Override
			  public void valueChanged(TreeSelectionEvent a) {
				  NetworkOperationNode node = ((NetworkOperatorTreeNode) a.getPath().getLastPathComponent()).getOperatorable();
				  if ( node.getOperator() instanceof ReadNetwork){
					
					  ReadNetwork r = (ReadNetwork) node.getOperator();
					  NetworkHierachyNode n =  r.getParameter(ReadNetwork.NetworkHierachyNode);
					  showNetworkInfo(networkInfo, image, n);
				  }
					

			  }

		  });
		 
		  networkTree.addTreeSelectionListener(new TreeSelectionListener(){

			  @Override
			  public void valueChanged(TreeSelectionEvent a) {
				  NetworkHierachyTreeNode node = (NetworkHierachyTreeNode) a.getPath().getLastPathComponent();
				
				  if ( node.getOperatorable().getNetworks().size() == 1)
				      showNetworkInfo(networkInfo, image,  node.getOperatorable().getNetworks().iterator().next());
			  }

		  });
		  
		// return view;
	 }
	 private void showNetworkInfo(NetworkInfoList networkInfo, JLabel image, NetworkHierachyNode nh  ){
         
		 networkInfo.update(nh);
		 Image img = null;
		try {
			img = service.getRenderedNetwork(nh.getGroupId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if ( img != null){
		  img = img.getScaledInstance(360, 360, Image.SCALE_SMOOTH);
			
		 image.setIcon(new ImageIcon(img));
		}
	 }
	@Override
	public TaskIterator createTaskIterator(Object arg0) {
		return null;
	}

}
