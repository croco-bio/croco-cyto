package de.lmu.ifi.bio.croco.cyto.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;

import de.lmu.ifi.bio.crco.data.Species;

public class SpeciesSelection extends JDialog{

	private static final long serialVersionUID = 1L;
	public SpeciesSelection(JFrame frame){
		super(frame,"Species selection",true);
		init();
		this.pack();
		
		this.setVisible(true);
	}
	private Species selectedSpecies = null;
	public Species getSelectedSpecies() {
		return selectedSpecies;
	}
	public void init(){
		this.setLayout(new FlowLayout());
		
		Vector<Species> species = new Vector<Species>();
		

		
		for(Species specie : Species.knownSpecies){
			species.add(specie);
		}
	
		final JComboBox selection  =new JComboBox(species);
		selection.setSelectedIndex(-1);  
		selection.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				selectedSpecies = (Species)selection.getSelectedItem();
				SpeciesSelection.this.dispose();
			}
			
		});
		
		this.add(selection);
	//	this.add(panel);
	}
}
