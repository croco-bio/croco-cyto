package de.lmu.ifi.bio.croco.cyto.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

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
import de.lmu.ifi.bio.croco.cyto.util.QueryServiceWrapper;

/**
 * Ortholog mapping selection dialogs for a given species.
 * @author pesch
 *
 */
public class OrthologMappingSelection extends JDialog{

	private static final long serialVersionUID = 1L;
	
	//species of interest
	private Species selectedSpecies= null;
	
	//select ortholog mapping
	private OrthologMappingInformation selectedMapping = null;
	
	//exit event
	private int buttonCode= JOptionPane.CANCEL_OPTION;
	
	class OrthologMappingInformationModel extends DefaultComboBoxModel{
		private static final long serialVersionUID = 1L;
		private HashSet<Species> filterData;
		private List<OrthologMappingInformation> data;
		boolean filter;
		
		public OrthologMappingInformationModel(List<OrthologMappingInformation> data, List<Species> filterData, boolean filter){
			super(new Vector<OrthologMappingInformation>(data));
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
				if ( filter && 
						(data.get(i).getSpecies1().equals(selectedSpecies) && !filterData.contains(data.get(i).getSpecies2()) ) ||
						(data.get(i).getSpecies2().equals(selectedSpecies) && !filterData.contains(data.get(i).getSpecies1()) )
						)  continue;
				this.insertElementAt(data.get(i),j++);
			}
			this.setSelectedItem(this.getElementAt(0));
		}
	}
	
	class SpeciesRenderer implements  ListCellRenderer{


		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			String[] tokens = ((Species)value).getName().split(" ");
			StringBuffer name = new StringBuffer();
			for(int i = 1; i <tokens.length ; i++){
				name.append(tokens[i].toLowerCase() + " ");
			}
			return new JLabel(tokens[0].substring(0,1).toUpperCase() + ". " + name.toString().trim() + " (taxonomy id:" + ((Species)value).getTaxId() + ")");
		}
		
	}
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
	/**
	 * @return the selected mapping
	 */
	public OrthologMappingInformation getSelectedMapping() {
		return selectedMapping;
	}

	
	public OrthologMappingSelection( Species selectedSpecies){
		super((JFrame)null,"Ortholog Mapping Selection",true);
		this.selectedSpecies = selectedSpecies;
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
		OrthologMappingSelection s = new OrthologMappingSelection(Species.Mouse);
		s.showDialog();
	
	}

	/**
	 * Queries available ortholog mappings from the query service
	 * @return list of OrthologMappingInformation objects
	 * @throws Exception
	 */
	private List<OrthologMappingInformation> getPossibleMappings() throws Exception{
		final QueryService service = QueryServiceWrapper.getInstance().getService();
		
		List<OrthologMappingInformation> orthologMappings = service.getOrthologMappingInformation(null, selectedSpecies, null);
		return orthologMappings;
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

		panel.add(new JLabel("From"));
		
		final JComboBox fromSpecies = new JComboBox(new Species[]{selectedSpecies});
	
		fromSpecies.setEnabled(false);
		fromSpecies.setRenderer(new SpeciesRenderer());
		panel.add(fromSpecies,"span 2,grow, wrap");


		panel.add(new JLabel("To"));
		final JComboBox toSpecies = new JComboBox();
		try{
			toSpecies.setModel(new OrthologMappingInformationModel(getPossibleMappings(),Species.knownSpecies,true));
		}catch(Exception e){
			throw new RuntimeException("Error from queryservice",e);
		}
		
		selectedMapping = (OrthologMappingInformation) toSpecies.getSelectedItem();
		panel.add(toSpecies,"wrap");
		toSpecies.setRenderer(new OrthologMappingInformationRenderer());
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
				if(selectedMapping == null ){
					JOptionPane.showMessageDialog(OrthologMappingSelection.this,
							"You did not select an ortholog mapping.",
							"Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				buttonCode = JOptionPane.OK_OPTION;

				OrthologMappingSelection.this.dispose();
			}

		});


		onlyModel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				((OrthologMappingInformationModel)toSpecies.getModel()).setFilter(!onlyModel.isSelected());
				OrthologMappingSelection.this.pack();
			}
		});

		toSpecies.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedMapping = (OrthologMappingInformation) toSpecies.getSelectedItem();
			}
		});
	
	}
}
