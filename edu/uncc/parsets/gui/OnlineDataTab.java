package edu.uncc.parsets.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.json.simple.parser.JSONParser;

import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.data.DataSet;
import edu.uncc.parsets.data.DownloadManager;
import edu.uncc.parsets.data.JSONExport;
import edu.uncc.parsets.data.LocalDB;
import edu.uncc.parsets.data.OnlineDataSet;
import edu.uncc.parsets.util.BatchConvert;
import edu.uncc.parsets.util.PSLogging;

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
public class OnlineDataTab extends JPanel implements Runnable, ComponentListener, ActionListener {

	public static final String TABTITLE = "Online Data";
	
	public static final String INDEXURL = BatchConvert.BASEURL+"psdata/index.json.gz";
	
	private List<DataSet> filteredDataSets;

	private GroupedDataSetList onlineDBList;

	private boolean noData = true;

	private JButton downloadButton;

	private JFrame frame;

	private DownloadManager downloadManager;

	private JProgressBar progressBar;

	private ArrayList<OnlineDataSet> onlineDSList;
	
	public OnlineDataTab(JFrame f) {
		super(new MigLayout("fillx, wrap 1, insets 0","[]", "[fill, grow]r[]r[]r"));
		setOpaque(false);
		frame = f;
		downloadManager = new DownloadManager(this);
		
		add(makeDataSetList(), "growx");
		addButtons();
		addComponentListener(this);
		LocalDB.getDefaultDB().addRescanListener(this);
	}

	private JComponent makeDataSetList() {
		Box b = new Box(BoxLayout.Y_AXIS);
		b.setBorder(BorderFactory.createTitledBorder("Online Data Sets"));
		onlineDBList = new GroupedDataSetList();
		onlineDBList.addDSListener(new GroupedDataSetList.DSListener() {
			public void selectDataSet(DataSet ds) {
				downloadButton.setEnabled(ds != null && !downloadManager.isDSqueued(ds));
			}

			public void openDataSet(DataSet ds) {
			}
		});
		JScrollPane scrollPane = new JScrollPane(onlineDBList);
		b.add(scrollPane);
		return b;
	}

	private void addButtons() {
		downloadButton = new JButton("Download to Local DB");
		downloadButton.setEnabled(false);
		downloadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				downloadManager.queueDownload((OnlineDataSet) onlineDBList.getSelectedDataSet());
				downloadButton.setEnabled(false);
			}
		});
		add(downloadButton, "center");
		add(progressBar = new JProgressBar(), "center");
		progressBar.setVisible(false);
	}
	
	@SuppressWarnings("unchecked")
	public void run() {
		downloadManager.start();
		BufferedReader reader = null;
		try {
			URL url = new URL(INDEXURL);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();

			InputStream stream = new GZIPInputStream(connection.getInputStream());
			reader = new BufferedReader(new InputStreamReader(stream));
			Map<String, Map<String, Object>> index = (Map<String, Map<String, Object>>)new JSONParser().parse(reader);
			stream.close();
			Map<String, Object> dsList = index.get(JSONExport.DATASETSKEY);
			onlineDSList = new ArrayList<OnlineDataSet>();
			for (Map.Entry<String, Object> e : dsList.entrySet()) {
				OnlineDataSet ds = new OnlineDataSet(e.getKey(), (Map<String, Object>)e.getValue());
				onlineDSList.add(ds);
			}
			filteredDataSets = filterDataSets(onlineDSList);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					onlineDBList.setSectionData(LocalDB.groupIntoSections(filteredDataSets.iterator()));
				}
			});
		} catch (Exception e) {
			PSLogging.logger.info("Could not download online data index.", e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					PSLogging.logger.warn("Error closing stream.", e);
				}
		}

	}

	private List<DataSet> filterDataSets(ArrayList<OnlineDataSet> onlineDSs) {
		ArrayList<DataSet> dsList = new ArrayList<DataSet>();
		for (OnlineDataSet ds : onlineDSs)
			if (!LocalDB.getDefaultDB().containsHandle(ds.getHandle()))
				dsList.add(ds);

		return dsList;
	}

	public void activateProgressBar(boolean running) {
		progressBar.setIndeterminate(running);
		progressBar.setVisible(running);
	}
	
	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
		if (noData) {
			Thread onlineCheck = new Thread(this);
			onlineCheck.setDaemon(true);
			onlineCheck.start();
			noData = false;
		}
	}

	public void componentShown(ComponentEvent e) {
		if (ParallelSets.isInstalled()) {
			VersionCheck vc = new VersionCheck(frame);
			vc.start();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (onlineDSList != null) {
			filteredDataSets = filterDataSets(onlineDSList);
			onlineDBList.setSectionData(LocalDB.groupIntoSections(filteredDataSets.iterator()));
		}
	}
	
}
