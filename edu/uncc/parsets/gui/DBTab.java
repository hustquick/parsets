package edu.uncc.parsets.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;
import edu.uncc.parsets.data.DataSet;
import edu.uncc.parsets.data.LocalDB;
import edu.uncc.parsets.data.LocalDBDataSet;

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

@SuppressWarnings("serial")
public class DBTab extends JPanel implements ActionListener {

	public static final String TABTITLE = "Database";

	public static class CSVFileFilter extends CombinedFileNameFilter {
		@Override
		public String getDescription() {
			return "CSV Files";
		}

		@Override
		public String getExtension() {
			return ".csv";
		}
	}
	
	private GroupedDataSetList localDBList;
	private JButton openButton;
	private JButton deleteButton;
	private Controller controller;
	
	public DBTab(final MainWindow mainWindow, Controller controller) {
		super(new MigLayout("fillx, wrap 2, insets 0", "[]0[]", "[grow, fill]r[]r[]r[]r"));
		this.controller = controller;
		controller.dbTab = this;
		
		setOpaque(false);
		add(makeDataSetList(mainWindow), "span 2, growx");
		addButtons(mainWindow);
		LocalDB.getDefaultDB().addRescanListener(this);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				mainWindow.setDSMenuItemsEnabled(localDBList.getSelectedDataSet() != null);
				super.componentShown(e);
			}
		});
	}

	private JComponent makeDataSetList(final MainWindow mainWindow) {
		Box b = new Box(BoxLayout.Y_AXIS);
		b.setBorder(BorderFactory.createTitledBorder("Local Data Sets"));
		localDBList = new GroupedDataSetList(LocalDB.getDefaultDB().getSections());
		localDBList.addDSListener(new GroupedDataSetList.DSListener() {
			public void selectDataSet(DataSet ds) {
				openButton.setEnabled(ds != null);
				deleteButton.setEnabled(ds != null);
				mainWindow.setDSMenuItemsEnabled(ds != null);
			}

			public void openDataSet(DataSet ds) {
				openDBDataSet(ds);
			}
		});
		JScrollPane scrollPane = new JScrollPane(localDBList);
		b.add(scrollPane);
		return b;
	}

	private void addButtons(final JFrame frame) {
		deleteButton = new JButton("Delete");
		deleteButton.setEnabled(false);
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteSelectedDataSet();
			}
		});
		add(deleteButton, "center");
		
		openButton = new JButton("Open");
		openButton.setEnabled(false);
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openSelectedDataSet();
			}
		});
		add(openButton, "center");
	}

	public void actionPerformed(ActionEvent e) {
		localDBList.setSectionData(LocalDB.getDefaultDB().getSections());
	}	
	
	public void openDBDataSet(DataSet dataSet) {
		controller.setDataSet(dataSet);
	}
	
	public void deleteSelectedDataSet() {
		if (localDBList.getSelectedDataSet() != null)
			((LocalDBDataSet)localDBList.getSelectedDataSet()).deleteFromDB();
	}
	
	public void openSelectedDataSet() {
		if (localDBList.getSelectedDataSet() != null)
			openDBDataSet(localDBList.getSelectedDataSet());
	}
}
