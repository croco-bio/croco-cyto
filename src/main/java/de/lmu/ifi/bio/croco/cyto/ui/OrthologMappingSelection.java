package de.lmu.ifi.bio.croco.cyto.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import net.miginfocom.swing.MigLayout;
import de.lmu.ifi.bio.crco.connector.QueryService;
import de.lmu.ifi.bio.crco.data.Species;
import de.lmu.ifi.bio.crco.operation.ortholog.OrthologMappingInformation;
import de.lmu.ifi.bio.crco.util.Pair;
import de.lmu.ifi.bio.croco.cyto.util.QueryServiceWrapper;

/**
 * Ortholog mapping selection dialogs for a given species.
 * @author pesch
 *
 */
public class OrthologMappingSelection extends JDialog{

	private static final long serialVersionUID = 1L;
	
	//species of interest (may be many networks from different species)
	private Set<Species> selectedSpecies= null;
	
	private List<OrthologMappingInformation> orthologMappings;
	//select ortholog mapping
	//private OrthologMappingInformation selectedMapping = null;
	
	//exit event
	private int buttonCode= JOptionPane.CANCEL_OPTION;
	private Species targetSpecies;
	
	class SpeciesSelectionComboBoxModel extends DefaultComboBoxModel<Species>{
		private static final long serialVersionUID = 1L;
		private HashSet<Species> filterData;
		private List<Species> data;
		boolean filter;
		
		public SpeciesSelectionComboBoxModel(List<Species> data, List<Species> filterData, boolean filter){
			super(new Vector<Species>(data));
			this.data = data;
			this.filterData = new HashSet<Species>(filterData);
			this.filter = filter;
			updataData();
		}
	
		public void setFilter(boolean b){
			filter = b;
			updataData();
		}
		
		public void updataData(){
			this.removeAllElements();
			int j=0;
			
			for(int i= 0 ; i< data.size(); i++){
				if ( filter && !filterData.contains(data.get(i))	)  continue;
				this.insertElementAt(data.get(i),j++);
			}
			this.setSelectedItem(this.getElementAt(0));
		}
	}
	
	class SpeciesRenderer implements  ListCellRenderer{


		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			String[] tokens = ((Species)value).getName().split(" ");
			StringBuffer name = new StringBuffer();
			for(int i = 1; i <tokens.length ; i++){
				name.append(tokens[i].toLowerCase() + " ");
			}
			return new JLabel(tokens[0].substring(0,1).toUpperCase() + ". " + name.toString().trim() + " (taxonomy id:" + ((Species)value).getTaxId() + ")");
		}
		
	}
	/*
	class OrthologMappingInformationRenderer implements ListCellRenderer{
	
		@Override
		public Component getListCellRendererComponent(JList list,Object obj, int index,boolean isSelected, boolean cellHasFocus) {
			OrthologMappingInformation value = (OrthologMappingInformation) obj;
			
			if ( value.getSpecies1().equals(selectedSpecies)){
				String[] tokens = value.getSpecies2().getName().split(" ");
				StringBuffer name = new StringBuffer();
				for(int i = 1; i <tokens.length ; i++){
					name.append(tokens[i].toLowerCase() + " ");
				}
				return new JLabel(tokens[0].substring(0,1).toUpperCase() + ". " + name.toString().trim() + " (taxonomy id:" + value.getSpecies2().getTaxId() + ")");
			}else if ( value.getSpecies2().equals(selectedSpecies)){
				String[] tokens = value.getSpecies1().getName().split(" ");
				StringBuffer name = new StringBuffer();
				for(int i = 1; i <tokens.length ; i++){
					name.append(tokens[i].toLowerCase() + " ");
				}
				return new JLabel(tokens[0].substring(0,1).toUpperCase() + ". " + name.toString().trim() + " (taxonomy id:" + value.getSpecies1().getTaxId() + ")");
			}else{
				throw new RuntimeException(String.format("Strange ortholog mapping (%s) (%s)",value.toString(),selectedSpecies.toString()));
			}
		}

	
	}
	*/
	public OrthologMappingSelection(         List<OrthologMappingInformation> orthologMappings,Set<Species> selectedSpecies){
		super((JFrame)null,"Ortholog Mapping Selection",true);
		this.selectedSpecies = selectedSpecies;
		this.orthologMappings = orthologMappings;
		
	}
	
	public OrthologMappingSelection(         List<OrthologMappingInformation> orthologMappings ,Species ... selectedSpecie){
        super((JFrame)null,"Ortholog Mapping Selection",true);
        this.selectedSpecies = new HashSet<Species>();
        this.orthologMappings = orthologMappings;
        
        for(Species s :selectedSpecie )
            this.selectedSpecies.add(s);
    }
    
	/**
	 * Shows the dialog
	 * @return the return exit either  JOptionPane.CANCEL_OPTION, or JOptionPane.OK_OPTION
	 */
	public int showDialog() {
		init();
		this.pack();
		this.setResizable(false);
		this.setVisible(true);
		return buttonCode;
	}
	public static void main(String[] args) throws Exception{
	    QueryService service = QueryServiceWrapper.getInstance().getService();
	    List<OrthologMappingInformation> mappings = service.getOrthologMappingInformation(null,null,null);
	    
		OrthologMappingSelection s = new OrthologMappingSelection(mappings,Species.Mouse,Species.Fly);
		s.showDialog();
	
	}
	public Species getSelectedTargetSpecies()
	{
	    return targetSpecies;
	}
	/**
	 * Queries available ortholog mappings from the query service
	 * @return list of OrthologMappingInformation objects
	 * @throws Exception
	 */

	private List<Species> findPossibleTargetSpecies( List<OrthologMappingInformation> mappings) {
	    HashSet<Species> selectedSpeciesLookup = new HashSet<Species>(this.selectedSpecies);

	    HashSet<Pair<Species,Species>> speciesPairs = new HashSet<Pair<Species,Species>>();
	    
	    HashSet<Species> allPossibleSpecies = new HashSet<Species>();
	    for(OrthologMappingInformation mapping : mappings)
	    {
	        allPossibleSpecies.add(mapping.getSpecies1());
	        allPossibleSpecies.add(mapping.getSpecies2());
	        
	        speciesPairs.add(new Pair<Species,Species>(mapping.getSpecies1(),mapping.getSpecies2()));
	    }
	    
	    List<Species> ret = new ArrayList<Species>();
	    for(Species possibleTargetSpecies : allPossibleSpecies)
	    {
	        boolean hasMappings = true;
	        for(Species selectedSpecies : selectedSpeciesLookup)
	        {
	            if (selectedSpecies.equals(possibleTargetSpecies)) continue;
	            if (!speciesPairs.contains(new Pair<Species,Species>(selectedSpecies,possibleTargetSpecies)))
	            {
	                hasMappings = false;
	                break;
	            }
	        }
	        if ( hasMappings) ret.add(possibleTargetSpecies);
	    }
	    
	    return ret;
	}
	/**
	 * Sets the layout and registers the event listener.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void init() {

		JPanel panel = new JPanel();
		this.add(panel);
		panel.setLayout(new MigLayout()); 


		JButton ok = new JButton("Ok");
		JButton cancle = new JButton("Cancle");

		final JCheckBox onlyModel = new JCheckBox("Show all available mappings",false);

		
		String lab = "";
		if ( this.selectedSpecies.size() == 1)
		{
		    panel.add(new JLabel("From"));
	        
		    Species sp = this.selectedSpecies.iterator().next();;
		    
		    String[] tokens = sp.getName().split(" ");
            StringBuffer name = new StringBuffer();
            for(int i = 1; i <tokens.length ; i++){
                name.append(tokens[i].toLowerCase() + " ");
            }
            
		    lab = tokens[0].substring(0,1).toUpperCase() + ". " + name.toString().trim() + " (taxonomy id:" + sp.getTaxId() + ")";
		    
		    final JLabel fromSpecies = new JLabel(lab);
	        fromSpecies.setBorder(BorderFactory.createEtchedBorder());
	        fromSpecies.setEnabled(false);
	        panel.add(fromSpecies,"span 2,grow, wrap");
		}

		

		
		
		panel.add(new JLabel("To"));
		final JComboBox toSpecies = new JComboBox();
		try{
		    
		    
		    List<Species> targetSpecies = findPossibleTargetSpecies(orthologMappings);
		    
			toSpecies.setModel(new SpeciesSelectionComboBoxModel(targetSpecies,Species.knownSpecies,true));
			
			this.targetSpecies = this.selectedSpecies.iterator().next();
		}catch(Exception e){
			throw new RuntimeException("Error from queryservice",e);
		}
		
		panel.add(toSpecies,"wrap");
		toSpecies.setRenderer(new SpeciesRenderer());
		toSpecies.setPreferredSize(new Dimension(300,30));

		panel.add(onlyModel,"span 2, wrap");

		panel.add(ok,"span 2,align right");
		cancle.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				buttonCode = JOptionPane.CANCEL_OPTION;
				OrthologMappingSelection.this.dispose();

			}

		});

		ok.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
			    /*
			    if(selectedMapping == null ){
					JOptionPane.showMessageDialog(OrthologMappingSelection.this,
							"You did not select an ortholog mapping.",
							"Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
                */
			    
				buttonCode = JOptionPane.OK_OPTION;

				OrthologMappingSelection.this.dispose();
			}

		});


		onlyModel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				((SpeciesSelectionComboBoxModel)toSpecies.getModel()).setFilter(!onlyModel.isSelected());
				OrthologMappingSelection.this.pack();
			}
		});

		toSpecies.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
		//		selectedMapping = (OrthologMappingInformation) toSpecies.getSelectedItem();
			    
			    targetSpecies = (Species)toSpecies.getSelectedItem();
			}
		});
	
	}
}
