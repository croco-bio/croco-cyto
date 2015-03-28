package de.lmu.ifi.bio.croco.cyto.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.lmu.ifi.bio.crco.connector.QueryService;
import de.lmu.ifi.bio.crco.data.NetworkHierachyNode;
import de.lmu.ifi.bio.crco.data.Option;
import de.lmu.ifi.bio.crco.data.Species;
import de.lmu.ifi.bio.crco.util.Pair;

public class NetworkInfoList extends JTable {
    QueryService service;
	class TableModel extends AbstractTableModel{
	
		private static final long serialVersionUID = 1L;
		List<Pair<String,String>> data = new ArrayList<Pair<String,String>>();
		
		
		
		public TableModel(NetworkHierachyNode node) {
			if ( node != null){
				
				try{
					
					
					
					data.add(new Pair<String,String>("Network name", node.getName()));
					data.add(new Pair<String,String>("Species", Species.getSpecies(node.getTaxId()).getName()));
					data.add(new Pair<String,String>("Network ID",node.getGroupId() + ""));
                    
					data.add(new Pair<String,String>("Number of interactions",""+service.getNumberOfEdges(node.getGroupId())));
                    
					
					for(Entry<Option, String> option : node.getOptions().entrySet()){
						data.add(new Pair<String,String>(option.getKey().description, option.getValue()));
					}
					
					
				}catch(Exception e){
					throw new RuntimeException(e);
				}
			}
			
			
		}
		
		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return 2;
		}

		@Override
		public int getRowCount() {
			return data.size();
		}

	    @Override
	    public String getColumnName(int column) {
	         if ( column == 0){
	        	 return "Option";
	         }else{
	        	 return "Value";
	         }
	     }
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0 ){
				return data.get(rowIndex).getFirst();
			}else{
				return data.get(rowIndex).getSecond();
			}
			
		}
		
	}
	
	private static final long serialVersionUID = 1L;

	public NetworkInfoList(QueryService service) {
		super();
		this.service = service;
	}

	public void update(NetworkHierachyNode node){
		
		this.setModel(new TableModel(node));
		
	}



}
