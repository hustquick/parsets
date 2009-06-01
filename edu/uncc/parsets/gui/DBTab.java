package edu.uncc.parsets.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;
import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.data.DataSet;
import edu.uncc.parsets.data.JSONExport;
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

	public static class CSVFileFilter extends FileFilter implements FilenameFilter {
		@Override
		public boolean accept(File f) {
			return f.getName().endsWith(".csv") || f.isDirectory();
		}

		@Override
		public String getDescription() {
			return "CSV Files";
		}

		public boolean accept(File dir, String name) {
			return name.endsWith(".csv");
		}
	}
	
	private GroupedDataSetList localDBList;
	private JButton openButton;
	private JButton deleteButton;
	private Controller controller;
	
	public DBTab(JFrame frame, Controller controller) {
		super(new MigLayout("fillx, wrap 2, insets 0", "[]0[]", "[grow, fill]r[]r[]r[]r"));
		this.controller = controller;
		
		setOpaque(false);
		add(makeDataSetList(), "span 2, growx");
		addButtons(frame);
		LocalDB.getDefaultDB().addRescanListener(this);
	}

	private JComponent makeDataSetList() {
		Box b = new Box(BoxLayout.Y_AXIS);
		b.setBorder(BorderFactory.createTitledBorder("Local Data Sets"));
		localDBList = new GroupedDataSetList(LocalDB.getDefaultDB().getSections());
		localDBList.addDSListener(new GroupedDataSetList.DSListener() {
			public void selectDataSet(DataSet ds) {
				openButton.setEnabled(ds != null);
				deleteButton.setEnabled(ds != null);
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
				((LocalDBDataSet)localDBList.getSelectedDataSet()).deleteFromDB();
			}
		});
		add(deleteButton, "center");
		
		openButton = new JButton("Open");
		openButton.setEnabled(false);
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (localDBList.getSelectedDataSet() != null)
					openDBDataSet(localDBList.getSelectedDataSet());
			}
		});
		add(openButton, "center");

		JButton importFileButton = new JButton("Import CSV File ...");
		importFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		    	new DataWizard();
			}
		});
		add(importFileButton, "span 2, center");

		if (!ParallelSets.isInstalled()) {
			JButton exportJSONButton = new JButton(">JSON");
			exportJSONButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fileChooser = new JFileChooser(new File("."));
					if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
						JSONExport.exportDataSet((LocalDBDataSet) localDBList.getSelectedDataSet(), fileChooser.getSelectedFile().getAbsolutePath());
				}
			});
			add(exportJSONButton, "center");
			JButton exportIndexButton = new JButton(">Index");
			exportIndexButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JSONExport.exportDBIndex(LocalDB.getDefaultDB(), "index");
				}
			});
			add(exportIndexButton, "center");
		}
	}

	public void actionPerformed(ActionEvent e) {
		localDBList.setSectionData(LocalDB.getDefaultDB().getSections());
	}	
	
	public void openDBDataSet(DataSet dataSet) {
		controller.setDataSet(dataSet);
	}
}
