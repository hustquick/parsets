package edu.uncc.parsets.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.uncc.parsets.data.DataSet;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * Copyright (c) 2009, Robert Kosara, Caroline Ziemkiewicz,
 *                     and others (see Authors.txt for full list)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of UNC Charlotte nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *      
 * THIS SOFTWARE IS PROVIDED BY ITS AUTHORS ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * List that shows group headings, and is specific to DataSet. This is a very
 * simple adaptation that breaks a lot of JList's functionality.
 * 
 * Only use {@link #setSectionData(Map)} to set the data for the list, and
 * {@link #getSelectedDataSet()} and the {@link DSListener} class to interact
 * with this class.
 * 
 * Also, this currently only works with {@link ListSelectionModel#SINGLE_SELECTION},
 * as the functions that would handle multiple selection are not there yet.
 *
 */
@SuppressWarnings("serial")
public class GroupedDataSetList extends JList implements ListSelectionListener, MouseListener {

	public static interface DSListener {
		/** 
		 * Called when the user clicks somewhere in the list. The argument is
		 * null if the user clicked on a heading.
		 * 
		 * @param ds The selected {@link DataSet} or null
		 */
		public void selectDataSet(DataSet ds);

		/**
		 * Called when the user double-clicks on a dataset. This is only
		 * called for actual {@link DataSet}s, never with null.
		 * 
		 * @param ds The dataset to be opened
		 */
		public void openDataSet(DataSet ds);
	}
	
	private static final int CELLWIDTH = 180;
	
	private Object dataSets[];
	
	private List<DSListener> dsListeners = new Vector<DSListener>();
	
	private static class DBTableCellRenderer implements ListCellRenderer {

		private static final Font TITLELABELFONT = new Font("Sans-Serif", Font.PLAIN, 14);
		private static final Font TITLEBOLDFONT = new Font("Sans-Serif", Font.BOLD, 14);
		private static final Font DETAILSLABELFONT = new Font("Sans-Serif", Font.PLAIN, 10);
		private static final Font DETAILSBOLDFONT = new Font("Sans-Serif", Font.BOLD, 10);
		private static final Color STRIPECOLOR = new Color(230, 230, 255);

		private static final Font SECTIONLABELFONT = new Font("Sans-Serif", Font.BOLD, 14);

		
		private JLabel titleLabel;
		private JLabel detailsLabel;
		private JLabel sectionLabel;
		
		private JPanel dsPanel;
		private JPanel labelPanel;

		protected static final Color GRADIENT_TOP = Color.WHITE;
		protected static final Color GRADIENT_BOTTOM = new Color(0xeeeeee); //new Color(0xF9E198);
		
		public DBTableCellRenderer() {
			dsPanel = new JPanel();
			dsPanel.setLayout(new GridLayout(2, 1));
			dsPanel.setOpaque(true);
			dsPanel.add(titleLabel = new JLabel("DataSet Name"));
			titleLabel.setFont(TITLELABELFONT);
			dsPanel.add(detailsLabel = new JLabel("DataSet Details"));
			detailsLabel.setFont(DETAILSLABELFONT);
			
			labelPanel = new JPanel() {
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					GradientPaint gradientPaint = new GradientPaint(0, 0,
							GRADIENT_TOP, 0, getHeight(), GRADIENT_BOTTOM);
					Graphics2D g2 = (Graphics2D) g;
					g2.setPaint(gradientPaint);
					g2.fillRect(0, 0, getWidth(), getHeight()-1);
				}
			};
			labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
			labelPanel.setOpaque(true);
			labelPanel.setBackground(Color.WHITE);
			labelPanel.add(Box.createVerticalStrut(5));
			labelPanel.add(sectionLabel = new JLabel("Heading"));
			labelPanel.add(Box.createVerticalStrut(5));
			sectionLabel.setFont(SECTIONLABELFONT);
		}
		
//		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			if (value instanceof DataSet) {
				if (isSelected) {
					dsPanel.setBackground(list.getSelectionBackground());
					titleLabel.setForeground(list.getSelectionForeground());
					titleLabel.setFont(TITLEBOLDFONT);
					detailsLabel.setForeground(list.getSelectionForeground());
					detailsLabel.setFont(DETAILSBOLDFONT);
				} else {
					boolean stripe = (index & 1) == 1;
					Color bg = (stripe?STRIPECOLOR:Color.WHITE);
					dsPanel.setBackground(bg);
					titleLabel.setForeground(list.getForeground());
					titleLabel.setFont(TITLELABELFONT);
					detailsLabel.setForeground(list.getForeground());
					detailsLabel.setFont(DETAILSLABELFONT);
				}

				DataSet ds = (DataSet) value;
				titleLabel.setText(ds.getName());
				
				StringBuilder sb = new StringBuilder();
				sb.append(ds.getNumDimensions()+" dimensions, ");
				sb.append(ds.getNumRecords()+" items");
				detailsLabel.setText(sb.toString());
				return dsPanel;
			} else {
				String section = ((SectionLabel) value).getName();
				sectionLabel.setText(section);
				return labelPanel;
			}
		}
	}
	
	private static class SectionLabel {
		private String name;
		
		public SectionLabel(String sectionName) {
			name = sectionName;
		}
		
		public String getName() {
			return name;
		}
	}
	
	public GroupedDataSetList() {
		super();
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setCellRenderer(new DBTableCellRenderer());
		setFixedCellWidth(CELLWIDTH);
		addListSelectionListener(this);
		addMouseListener(this);
	}
	
	public GroupedDataSetList(Map<String, List<DataSet>> sections) {
		this();
		setSectionData(sections);
	}
	
	public void addDSListener(DSListener listener) {
		dsListeners.add(listener);
	}
	
	public void removeDSListener(DSListener listener) {
		dsListeners.remove(listener);
	}
	
	public void setSectionData(Map<String, List<DataSet>> sections) {
		String[] sectionNames = new String[sections.size()];
		sections.keySet().toArray(sectionNames);
		Arrays.sort(sectionNames);
		int numDataSets = 0;
		for (List<DataSet> dsList : sections.values())
			numDataSets += dsList.size();
		dataSets = new Object[numDataSets+sectionNames.length];
		int index = 0;
		for (String section : sectionNames) {
			dataSets[index++] = new SectionLabel(section);
			for (DataSet ds : sections.get(section))
				dataSets[index++] = ds;
		}
		setListData(dataSets);
	}

	/**
	 * Returns true if the item at the given index is a {@link DataSet},
	 * false if it is a heading.
	 * 
	 * @param index the list index to check
	 * @return true if item at index is a {@link DataSet}
	 */
	private boolean isIndexActive(int index) {
		if (index >= 0)
			return dataSets[index] instanceof DataSet;
		else
			return false;
	}

	/**
	 * calls {@link #getDataSet(int)} for the currently selected item.
	 * 
	 * @return The selected {@link DataSet} or null
	 */
	public DataSet getSelectedDataSet() {
		return getDataSet(getSelectedIndex());
	}

	/**
	 * Returns the {@link DataSet} at the given index, or null if
	 * that item is a heading.
	 * 
	 * @param index The index of the item to return
	 * @return The {@link DataSet} at the index or null.
	 */
	private DataSet getDataSet(int index) {
		if (isIndexActive(index))
			return (DataSet) dataSets[index];
		else
			return null;
	}

	public void valueChanged(ListSelectionEvent e) {
		DataSet ds = getSelectedDataSet();
		for (DSListener listener : dsListeners)
			listener.selectDataSet(ds);
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			int index = locationToIndex(e.getPoint());
			if (isIndexActive(index)) {
				ensureIndexIsVisible(index);
				DataSet ds = getDataSet(index);
				if (ds != null)
					for (DSListener listener : dsListeners)
						listener.openDataSet(ds);
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}
}
