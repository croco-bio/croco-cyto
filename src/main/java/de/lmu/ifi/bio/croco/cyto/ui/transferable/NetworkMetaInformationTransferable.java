package de.lmu.ifi.bio.croco.cyto.ui.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.bio.croco.data.NetworkMetaInformation;


public class NetworkMetaInformationTransferable implements Transferable {
	private List<NetworkMetaInformation> nodes;
	final public static DataFlavor INFO_FLAVOR =new DataFlavor(NetworkMetaInformation.class, "NetworkMetaInformation");

	static DataFlavor flavors[] = {INFO_FLAVOR };
	public NetworkMetaInformationTransferable(List<NetworkMetaInformation> nodes) throws ClassNotFoundException {
		this.nodes = new ArrayList<NetworkMetaInformation>(nodes);
		
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