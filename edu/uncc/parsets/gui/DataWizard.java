package edu.uncc.parsets.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

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
import edu.uncc.parsets.data.LocalDB;
import edu.uncc.parsets.data.old.CSVDataSet;
import edu.uncc.parsets.data.old.CSVParser;
import edu.uncc.parsets.data.old.CSVParserListener;
import edu.uncc.parsets.data.old.DataDimension;
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
public class DataWizard implements CSVParserListener {

	private JTable table;

	CSVDataSet data;

	DataDimension currentDimension;

	private JTable categoriesTable;

	private JComboBox typeCB;

	private JLabel dimKeyLabel;

	private JTextField dimNameTF;

	private JButton closeBtn;

	private JFrame wizardFrame;

	private JTextField nameTF;

	private JProgressBar progressBar;

	private JLabel fileNameLabel;

	private JTextField sourceTF;

	private JTextField srcURLTF;

	private String fileName;

	private JTextField sectionTF;

	private JLabel statusLabel;

	private CSVParser csvParser;

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
				numRows = Math.min(data.getNumRecords()-1, 100);
				return numRows+1;
			} else
				return 0;
		}

		public Object getValueAt(int row, int col) {
			if (row >= numRows)
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
				return currentDimension.getCategoryKey(row);
			else
				return currentDimension.getCategoryName(row);
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
		table = new JTable();
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
	    	statusLabel.setText("Analyzing CSV file ...");
	    	csvParser = new CSVParser(fileName, this);
	    	csvParser.analyzeCSVFile();
	    } else {
	    	wizardFrame.setVisible(false);
	    	wizardFrame.dispose();
	    }
	}

	private JComponent createButtonPanel() {
		JPanel p = new JPanel(new MigLayout("fillx, insets 0"));

		progressBar = new JProgressBar();
		p.add(progressBar, "gapleft 5, width 250, split 2");

		statusLabel = new JLabel();
		p.add(statusLabel, "gapleft 7");
		
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
				statusLabel.setText("Writing data to local database ...");
				new Thread(new Runnable() {
					@Override
					public void run() {
						csvParser.streamToDB(LocalDB.getDefaultDB());
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

		categoriesTable = new JTable();
		JScrollPane scrollPane = new JScrollPane(categoriesTable);
		scrollPane.setOpaque(false);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Categories"));
		p.add(scrollPane, "span 2");

		p.setPreferredSize(new Dimension(250, 1000));

		return p;
	}

	private void setActiveDimension(DataDimension d) {
		currentDimension = d;
		categoriesTable.setModel(new CategoryTableModel());
		dimKeyLabel.setText(d.getKey());
		dimNameTF.setText(d.getName());
		typeCB.setSelectedIndex(d.getDataType().ordinal());
	}

	public void setDataSet(final CSVDataSet data) {
		this.data = data;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				statusLabel.setText("");
				progressBar.setIndeterminate(false);
				nameTF.setText(data.getName());
				sectionTF.setText(data.getSection());
				fileNameLabel.setText(data.getFileBaseName());
				sourceTF.setText(data.getSource());
				srcURLTF.setText(data.getSourceURL());
				DataTableModel m = new DataTableModel();
				table.setModel(m);
				table.getColumnModel().getSelectionModel().addListSelectionListener(m);

				progressBar.setValue(0);
			}
		});
	}

	@Override
	public void setProgress(final int progress) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (progress >= 0) {
					progressBar.setIndeterminate(false);
					progressBar.setValue(progress);
				} else
					progressBar.setIndeterminate(true);
			}
		});
	}

	@Override
	public void importDone() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				wizardFrame.setVisible(false);
			}
		});
	}

	@Override
	public void errorFileNotFound(String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void errorReadingFile(String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void errorWrongNumberOfColumns(int expected, int found, int line) {
		// TODO Auto-generated method stub
		
	}
}
