package de.lmu.ifi.bio.croco.cyto.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import de.lmu.ifi.bio.croco.operation.GeneralOperation;

public class OperationsTable extends JTable {
	
	class OperationModel extends AbstractTableModel{
		
		private static final long serialVersionUID = 1L;
		List<JButton> possibleOperations = null;
		public OperationModel(List<JButton> possibleOperations) {
			this.possibleOperations = possibleOperations;
			
		}
		@Override
		public Class<?> getColumnClass(int columnIndex) {
		      return JButton.class;
		}
		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			return possibleOperations.size();
		}

	    @Override
	    public String getColumnName(int column) {
	        return "Operation";
	     }
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return possibleOperations.get(rowIndex);
			
		}
		
	}
	//class OperationRendered im

	class Renderer implements TableCellRenderer{

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
		
			return (JButton)value;
		}
		
	}
	
	public OperationsTable( List<JButton> possibleOperations){
		this.setModel(new OperationModel(possibleOperations));
		this.setDefaultRenderer(JButton.class, new Renderer() );
		
	}
}
