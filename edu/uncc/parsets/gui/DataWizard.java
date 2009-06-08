package edu.uncc.parsets.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import net.miginfocom.swing.MigLayout;
import edu.uncc.parsets.data.DataType;
import edu.uncc.parsets.data.old.CSVDataSet;
import edu.uncc.parsets.data.old.CSVParser;
import edu.uncc.parsets.data.old.DataDimension;
import edu.uncc.parsets.data.old.DataSetReceiver;
import edu.uncc.parsets.data.old.MetaDataParser;
import edu.uncc.parsets.gui.DBTab.CSVFileFilter;
import edu.uncc.parsets.util.osabstraction.AbstractOS;

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
public class DataWizard implements DataSetReceiver {

	private JTable table;

	CSVDataSet data;

	DataDimension currentDimension;

	private CategoryTableModel categoryModel;

	private JTable categoriesTable;

	private JComboBox typeCB;

	private JLabel dimKeyLabel;

	private JTextField dimNameTF;

	private JButton closeBtn;

	private JFrame wizardFrame;

	private JTextField nameTF;

//	private JLabel statusLabel;

	private JProgressBar progressBar;

	private JLabel fileNameLabel;

	private JTextField sourceTF;

	private JTextField srcURLTF;

	private String fileName;

	private DataTableModel tableModel;

	private JTextField sectionTF;

	class DataTableModel extends AbstractTableModel implements ListSelectionListener {

		int numRows = 0;
		
		public int getColumnCount() {
			if (data != null)
				return data.getNumDimensions();
			else
				return 0;
		}

		public int getRowCount() {
			if (data != null) {
				numRows = Math.min(data.getNumRecords(), 100);
				return numRows+1;
			} else
				return 0;
		}

		public Object getValueAt(int row, int col) {
			if (row == numRows)
				return "...";
			else {
				DataDimension d = data.getDimension(col);
				return d.getValues().get(row);
			}
		}

		public String getColumnName(int i) {
			return data.getDimension(i).getName();
		}

		public void valueChanged(ListSelectionEvent event) {
			if (data != null)
				if (!event.getValueIsAdjusting() && (table.getSelectedColumn() >= 0)) {
					setActiveDimension(data.getDimension(table.getSelectedColumn()));
				}
		}
	};

	class CategoryTableModel extends AbstractTableModel {

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			if (currentDimension != null
					&& currentDimension.getDataType() == DataType.categorical)
				return currentDimension.getNumCategories();
			else
				return 0;
		}

		public Object getValueAt(int row, int col) {
			if (col == 0)
				return currentDimension.getCategoryName(row);
			else
				return currentDimension.getCategoryLabel(row);
		}

		public String getColumnName(int i) {
			if (i == 0)
				return "Key";
			else
				return "Name";
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 1;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			currentDimension.setCategoryName(rowIndex, (String) aValue);
		}
	};

	public DataWizard() {
		tableModel = new DataTableModel();
		table = new JTable(tableModel);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().getSelectionModel().addListSelectionListener(tableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setOpaque(false);

		wizardFrame = new JFrame("CSV Import");
		wizardFrame.setSize(800, 800);
		wizardFrame.add(createDataSetPanel(), BorderLayout.NORTH);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Data"));
		wizardFrame.add(scrollPane, BorderLayout.CENTER);
		wizardFrame.add(createDimensionPanel(), BorderLayout.EAST);
		wizardFrame.add(createButtonPanel(), BorderLayout.SOUTH);

		wizardFrame.setVisible(true);
		
		fileName = AbstractOS.getCurrentOS().showDialog(wizardFrame, new CSVFileFilter(), FileDialog.LOAD);
	    if (fileName != null) {
	    	progressBar.setIndeterminate(true);
	    	parseCSVFile(fileName, this);
	    } else {
	    	wizardFrame.setVisible(false);
	    	wizardFrame.dispose();
	    }
	}

	/**
	 * Call the parser to read in a dataset, and parse the XML metadata file if
	 * it exists. The parser runs in a separate thread, and this function
	 * returns before the data has been parsed if the {@literal callBack} 
	 * parameter is not null. Waits for the parse and returns the
	 * dataset if callBack is null.
	 */
	public static CSVDataSet parseCSVFile(String filename, DataSetReceiver callBack) {
		CSVParser reader = new CSVParser(filename, callBack);
		MetaDataParser mp = new MetaDataParser();
		String metafilename = filename.substring(0, filename.lastIndexOf('.'))
				+ ".xml";
		if (new File(metafilename).exists()) {
			mp.parse(reader.getDataSet(), metafilename);
		} else {
			if (metafilename.contains("_")) {
				metafilename = metafilename.substring(0, metafilename.lastIndexOf("_"))+".xml";
				if (new File(metafilename).exists()) {
					mp.parse(reader.getDataSet(), metafilename);
					String name = new File(filename).getName();
					name = name.substring(0, name.lastIndexOf('.'));
					name = name.replace('_', ' ');
					reader.getDataSet().setName(name);
				}
			}
//			if (metaData == null) {
//				metaData = new MetaData();
//				String name = (new File(filename)).getName();
//				int lastPeriod = name.lastIndexOf('.');
//				if (lastPeriod > 0)
//					name = name.substring(0, lastPeriod);
//				metaData.setName(name);
//			}
		}
		reader.analyzeFile();
		return reader.getDataSet();
	}

	private JComponent createButtonPanel() {
		JPanel p = new JPanel(new MigLayout("fillx, insets 0"));

		progressBar = new JProgressBar();
		p.add(progressBar, "gapleft 5, width 250");

//		statusLabel = new JLabel("Analyzing file ...");
//		p.add(statusLabel, "gapleft 7");
		
		closeBtn = new JButton("Cancel");
		closeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				wizardFrame.setVisible(false);
			}
		});
		p.add(closeBtn, "split 2, right, tag cancel");

		JButton saveBtn = new JButton("Save to DB");
		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				data.setName(nameTF.getText());
				data.setSource(sourceTF.getText());
				data.setSourceURL(srcURLTF.getText());
				data.setSection(sectionTF.getText());
				progressBar.setIndeterminate(true);
				new Thread(new Runnable() {
					@Override
					public void run() {
//						LocalDB.getDefaultDB().addLocalDBDataSet(data);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								wizardFrame.setVisible(false);
							}
						});
					}
				}).start();
			}
		});
		p.add(saveBtn, "tag ok");
		
		return p;
	}

	private JComponent createDataSetPanel() {
		JPanel p = new JPanel(new MigLayout("fill, insets 0 5 0 5, wrap 6"));
		p.setBorder(BorderFactory.createTitledBorder("Dataset"));
		p.add(new JLabel("Name:"), "right");
		p.add(nameTF = new JTextField(), "gap related, width 25sp, growx");
		p.add(new JLabel("Section:"), "right, gap unrelated");
		p.add(sectionTF = new JTextField(), "gap related, width 25sp, growx");
		p.add(new JLabel("File:"), "right, gap unrelated");
		p.add(fileNameLabel = new JLabel(), "gap related, width 25sp");
		p.add(new JLabel("Source:"), "right");
		p.add(sourceTF = new JTextField(), "gap related, width 25sp, growx");
		p.add(new JLabel("URL:"), "right, gap unrelated");
		p.add(srcURLTF = new JTextField(), "gap related, span 3, growx");
		return p;
	}

	private JPanel createDimensionPanel() {
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder("Dimension"));
		p.setLayout(new MigLayout("wrap 2, insets 0 10 0 10, fill", "[right]r[left]", "[]r[]r[]r[]r[grow,fill]r"));

//		p.add(new JCheckBox("Include"), "span 2, left");

		p.add(new JLabel("Key:"));
		dimKeyLabel = new JLabel("");
		p.add(dimKeyLabel, "gap related");
		
		p.add(new JLabel("Name:"));
		dimNameTF = new JTextField(25);
		dimNameTF.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				currentDimension.setName(dimNameTF.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				currentDimension.setName(dimNameTF.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				currentDimension.setName(dimNameTF.getText());
			}
		});
		p.add(dimNameTF, "gap related");
		
		p.add(new JLabel("Type:"));
		DataType activeTypes[] = {DataType.categorical, DataType.numerical};
		typeCB = new JComboBox(activeTypes);
		typeCB.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				currentDimension.setDataType(DataType.values()[typeCB
						.getSelectedIndex()]);
				categoriesTable.revalidate();
			}
		});
		p.add(typeCB, "gap related");

		categoryModel = new CategoryTableModel();
		categoriesTable = new JTable(categoryModel);
		JScrollPane scrollPane = new JScrollPane(categoriesTable);
		scrollPane.setOpaque(false);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Categories"));
		p.add(scrollPane, "span 2");

		p.setPreferredSize(new Dimension(250, 1000));

		return p;
	}

	private void setActiveDimension(DataDimension d) {
		currentDimension = d;
		categoryModel.fireTableDataChanged();
		dimKeyLabel.setText(d.getKey());
		dimNameTF.setText(d.getName());
		typeCB.setSelectedIndex(d.getDataType().ordinal());
	}

	public void setDataSet(CSVDataSet data) {
		this.data = data;
		nameTF.setText(data.getName());
		sectionTF.setText(data.getSection());
		fileNameLabel.setText(data.getFileBaseName());
		sourceTF.setText(data.getSource());
		srcURLTF.setText(data.getSourceURL());
		table.setModel(new DataTableModel());
		progressBar.setIndeterminate(false);
	}
}
