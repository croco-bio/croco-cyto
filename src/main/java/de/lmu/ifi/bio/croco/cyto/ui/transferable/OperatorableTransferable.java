package de.lmu.ifi.bio.croco.cyto.ui.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import de.lmu.ifi.bio.crco.data.NetworkOperationNode;

public class OperatorableTransferable implements Transferable {
	private NetworkOperationNode node;
	final public static DataFlavor INFO_FLAVOR =new DataFlavor(NetworkOperationNode.class, "Operatorable");

	static DataFlavor flavors[] = {INFO_FLAVOR };
	public OperatorableTransferable(NetworkOperationNode node) throws ClassNotFoundException {
		this.node = node;
		
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {

		if(!isDataFlavorSupported(flavor))
			throw new UnsupportedFlavorException(flavor);
		return node;
	}
	public boolean isDataFlavorSupported(DataFlavor df) {
		return df.equals(INFO_FLAVOR);
	}



	/** implements Transferable interface */
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}
}
