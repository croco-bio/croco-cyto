package de.lmu.ifi.bio.croco.cyto.ui.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.bio.crco.data.NetworkHierachyNode;


public class NetworkHierachyNodeTransferable implements Transferable {
	private List<NetworkHierachyNode> nodes;
	final public static DataFlavor INFO_FLAVOR =new DataFlavor(NetworkHierachyNode.class, "NetworkHierachyNode");

	static DataFlavor flavors[] = {INFO_FLAVOR };
	public NetworkHierachyNodeTransferable(List<NetworkHierachyNode> nodes) throws ClassNotFoundException {
		this.nodes = new ArrayList<NetworkHierachyNode>(nodes);
		
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor)
throws UnsupportedFlavorException {

		if(!isDataFlavorSupported(flavor))
			throw new UnsupportedFlavorException(flavor);
		return nodes;
	}
	public boolean isDataFlavorSupported(DataFlavor df) {
		return df.equals(INFO_FLAVOR);
	}



	/** implements Transferable interface */
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}
}