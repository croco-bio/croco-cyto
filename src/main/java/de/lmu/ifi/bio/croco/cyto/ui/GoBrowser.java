package de.lmu.ifi.bio.croco.cyto.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import net.miginfocom.swing.MigLayout;
import de.lmu.ifi.bio.crco.connector.LocalService;
import de.lmu.ifi.bio.crco.connector.QueryService;
import de.lmu.ifi.bio.crco.data.ContextTreeNode;
import de.lmu.ifi.bio.crco.util.CroCoLogger;
import de.lmu.ifi.bio.croco.cyto.ui.GOBrowserTree.GoNode;

public class GoBrowser extends JDialog {
    QueryService service;
    
	public static void main(String[] main ) throws Exception{
		GoBrowser b = new GoBrowser(null,null,new LocalService());
		b.showDialog();
	}
	
	public ContextTreeNode showDialog() throws Exception{
		init();
		if ( action == 1 ) return selected;
		return null;
	}
	private int action = 0;
	private ContextTreeNode selected;
	public void init() throws Exception{
		this.setLayout(new MigLayout());
		 final GOBrowserTree browser = new GOBrowserTree(context,service);
		 
		  JScrollPane scrp = new JScrollPane(browser);
		  scrp.setPreferredSize(new Dimension(890,570));
		 this.add(scrp,"width 500, height 500,wrap, span 2");
		 
		 final JTextField txt = new JTextField();
		 if( context != null){
			 selected = context;
			 txt.setText(context.getDescription());
		 }
		 txt.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
			    updata();
			  }
			  public void removeUpdate(DocumentEvent e) {
				  updata();
			  }
			  public void insertUpdate(DocumentEvent e) {
				  updata();
			  }

			  public void updata() {
				  if ( txt.getText().length() > 0){
					  try {
						  List<ContextTreeNode> nodes = service.getContextTreeNodes(txt.getText());
						  browser.setData(nodes);
					  }catch(Exception e){
						  throw new RuntimeException(e);
					  }
				  }else{
					  try {
						browser.init(null);
					} catch (Exception e) {
						 throw new RuntimeException(e);
					}
				  }
			  }
			});
		 
		  
		  browser.addTreeSelectionListener(new TreeSelectionListener(){

			  @Override
			  public void valueChanged(TreeSelectionEvent a) {
				  GoNode node = (GoNode) a.getPath().getLastPathComponent();
				  selected = node.getData();
					

			  }

		  });
		 
		 this.add(new JLabel("GO-Term:"));
		 this.add(txt,"grow, wrap");
		 
		 JButton noC = new JButton("Use entire network");
		 noC.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
			 
		 });
		 
		 this.add(noC);
		 JButton ok = new JButton("Ok");
		 ok.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				action = 1;
				setVisible(false);
			}
			 
		 });
		 
		 this.add(ok,"align right");
		 
		 this.pack();
		 this.setVisible(true);
	}
	private ContextTreeNode context;
	public GoBrowser(JFrame frame, ContextTreeNode context, QueryService service) throws Exception{
		super(frame,"Gene Ontology Browser",true);
		CroCoLogger.getLogger().debug("Given default context:" + context);
		this.context = context;
		this.service = service;
	}
	
	
}
