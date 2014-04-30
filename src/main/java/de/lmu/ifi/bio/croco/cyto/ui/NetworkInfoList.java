package de.lmu.ifi.bio.croco.cyto.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.lmu.ifi.bio.crco.connector.QueryService;
import de.lmu.ifi.bio.crco.data.NetworkHierachyNode;
import de.lmu.ifi.bio.crco.data.Option;
import de.lmu.ifi.bio.crco.data.Species;
import de.lmu.ifi.bio.crco.util.Pair;
import de.lmu.ifi.bio.croco.cyto.util.QueryServiceWrapper;

public class NetworkInfoList extends JTable {

	class TableModel extends AbstractTableModel{
	
		private static final long serialVersionUID = 1L;
		List<Pair<String,String>> data = new ArrayList<Pair<String,String>>();
		
		
		
		public TableModel(NetworkHierachyNode node) {
			if ( node != null){
				
				try{
					QueryService service = QueryServiceWrapper.getInstance().getService();
					
					
					
					data.add(new Pair<String,String>("Network name", node.getName()));
					
					data.add(new Pair<String,String>("Species", Species.getSpecies(node.getTaxId()).getName()));
					
					
					List<Pair<Option, String>> options = service.getNetworkInfo(node.getGroupId());
					for(Pair<Option,String> option : options){
						data.add(new Pair<String,String>(option.getFirst().description, option.getSecond()));
					}
					
					data.add(new Pair<String,String>("Network ID",node.getGroupId() + ""));
					
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

	public NetworkInfoList() {
		super();
	}

	public void update(NetworkHierachyNode node){
		
		this.setModel(new TableModel(node));
		
	}



}
