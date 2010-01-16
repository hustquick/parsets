package edu.uncc.parsets.gui;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class GenoSetsPopup extends JPopupMenu{
	private static final long serialVersionUID = 1L;
	
	JMenuItem item1;
	
	public GenoSetsPopup(){
		init();
	}
	
	private void init(){
		item1 = new JMenuItem("item1");
		this.add(item1);
	}

}
