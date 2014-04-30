package de.lmu.ifi.bio.croco.cyto.ui;

import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.slf4j.LoggerFactory;

import de.lmu.ifi.bio.crco.util.CroCoLogger;
import de.lmu.ifi.bio.crco.util.Pair;

public class Help extends JDialog {
	
	private static final long serialVersionUID = 1L;
	public Help(Frame frame){
		super(frame,"Help",true);
	}
	public static void main(String[] args) throws IOException{
		Help help = new Help(null);
		help.init();
		
		help.setVisible(true);
	}
	private List<Pair<String,String>> helpContent;
	private void readHelpData() throws IOException{
		helpContent= new ArrayList<Pair<String,String>>();
		
		InputStream helpStream = CroCoLogger.class.getClassLoader().getResourceAsStream("help");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(helpStream));
		String line = null;
		Set<Integer> ids = new HashSet<Integer>();
		Pattern pattern = Pattern.compile("\\w+.(\\d+).\\w+");
		HashMap<String,String> fileContent = new HashMap<String,String>();
		while (( line = br.readLine())!=null){
			if ( line.indexOf("=") == -1 ) continue;
			String id = line.substring(0,line.indexOf("="));
			String content =  line.substring(line.indexOf("=")+1);
			fileContent.put(id, content);
			Matcher matcher = pattern.matcher(id);
			if ( matcher.find()){
				ids.add(Integer.valueOf(matcher.group(1)));
			}
		}
		br.close();
		for(int id : ids){
			String text = fileContent.get("Help." + id + ".Text");
			String image = fileContent.get("Help." + id + ".Image");
			
			helpContent.add(new Pair<String,String>(text,image));
		}
	}
	private int index = 0;
	private void showHelpInformation(){
		if ( helpContent.size() >0  ){
			description.setText(helpContent.get(index).getFirst());
			InputStream is = CroCoLogger.class.getClassLoader().getResourceAsStream(helpContent.get(index).getSecond());
			//if ( is == null) return;
			try{
				Image startUpImage = ImageIO.read(is);
			
				imgStartUplnl.setIcon(new ImageIcon(startUpImage));
			}catch(IOException e){
				LoggerFactory.getLogger(Help.class).error("Cannot find read image " +helpContent.get(index).getSecond() );
			}
		}
		
	}
	JLabel description = new JLabel();
	JLabel imgStartUplnl = new JLabel();
	public void init() {
		try{
			readHelpData();
		}catch(Exception e){
			LoggerFactory.getLogger(Help.class).error("Cannot read help data");
			return;
		}
		this.setTitle("Help");
		JPanel panel = new JPanel();
		this.add(panel);
		panel.setLayout(new MigLayout()); 
		
		
		showHelpInformation();
		
		
		panel.add(description,"align center, wrap");
		panel.add(imgStartUplnl,"align center,height 400,wrap");
		JButton next = new JButton("Next");

		panel.add(next,"align right,wrap");
		next.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				index = (index+1)%helpContent.size();
				
				showHelpInformation();
				
			}
			
		});
		this.add(panel);
		this.pack();
	}
}
