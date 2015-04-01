package de.lmu.ifi.bio.croco.cyto.layout;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;

import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.undo.UndoSupport;

/**
 * Provides (meta) layout functions
 * @author pesch
 *
 */
public abstract class CroCoLayout extends AbstractLayoutAlgorithm{

	public CroCoLayout(String id, String name, UndoSupport undoSupport) {
		super(id, name, undoSupport);
	}
	class CheckedAdapter extends MouseAdapter{
		public void mousePressed(MouseEvent e)
		{
			JList list = (JList) e.getSource();
			
			int index = list.locationToIndex(e.getPoint());

			if (index != -1) {
				JToggleButton checkbox = ((ListData)list.getModel().getElementAt(index)).checkBox;
				checkbox.setSelected(!checkbox.isSelected());
				list.repaint();
			}
		}
	}
	class ListData<E>{
		public ListData(E data, String name, boolean checkBox){
			this.data = data;
			if ( checkBox)
				this.checkBox = new JCheckBox(name);
			else
				this.checkBox = new JRadioButton(name);
		}
		E data;
		JToggleButton checkBox;
	}
	class CellRenderer implements ListCellRenderer{
		@Override
		public Component getListCellRendererComponent(JList list,Object value, int index, boolean isSelected,boolean cellHasFocus) {
			ListData data = (ListData) value;
			JToggleButton checkbox = (JToggleButton)data.checkBox;
	//		checkbox.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
	//		checkbox.setForeground(isSelected ?  list.getSelectionForeground() :list. getForeground());
	//		checkbox.setEnabled(list.isEnabled());
	//		checkbox.setFont(list.getFont());
	//		checkbox.setFocusPainted(false);
	//		checkbox.setBorderPainted(true);
			//checkbox.setBorder(isSelected ?UIManager.getBorder( "List.focusCellHighlightBorder") : noFocusBorder);
			return checkbox;
		}
	}

}
