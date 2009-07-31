package edu.uncc.parsets.gui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.util.PSLogging;
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

public class VersionCheck extends Thread {

	private static final String VERSIONURL = "http://data.eagereyes.org/parsets/version.php?current="+ParallelSets.VERSION+"&os=";
	
	private static final String MESSAGE1 = "A new version of this program is available:";
	
	private static final String MESSAGE2 = "Do you want to upgrade?";
	
	private static final String TITLE = "New Version!";
	
	private JFrame frame;
	
	public VersionCheck(JFrame f) {
		frame = f;
		setDaemon(true);
	}
	
	@Override
	public void run() {
		BufferedReader reader = null;
		try {
			URL url = new URL(VERSIONURL+AbstractOS.getCurrentOS().shortName());
			InputStream stream = url.openStream();

			reader = new BufferedReader(new InputStreamReader(stream));
			JSONParser p = new JSONParser();
			JSONObject versionInfo = (JSONObject) p.parse(reader);
			String latestVersion = (String) versionInfo.get("latest");
			String versionParts[] = latestVersion.split("\\.");
			int majorVersion = Integer.parseInt(versionParts[0]);
			int minorVersion = Integer.parseInt(versionParts[1]);
			if (majorVersion > ParallelSets.MAJOR_VERSION ||
				(majorVersion == ParallelSets.MAJOR_VERSION && minorVersion > ParallelSets.MINOR_VERSION)) {
				JSONArray versions = (JSONArray) versionInfo.get("versions");
				StringBuilder versionsHTML = new StringBuilder("<html>");
				for (Object v : versions) {
					JSONObject version = (JSONObject) v;
					versionsHTML.append("<p><b>Version ");
					versionsHTML.append(version.get("version")+"</b> (released ");
					versionsHTML.append(version.get("release_date")+")</p>");
					versionsHTML.append(version.get("notes"));
				}
				versionsHTML.append("</html>");
				JTextPane textPane = new JTextPane();
				textPane.setContentType("text/html");
				textPane.setText(versionsHTML.toString());
				textPane.setEditable(false);
				Box b = new Box(BoxLayout.Y_AXIS);
				b.add(new JLabel(MESSAGE1));
				b.add(Box.createVerticalStrut(5));
				JScrollPane scrollPane = new JScrollPane(textPane);
				scrollPane.setPreferredSize(new Dimension(300, 300));
				b.add(scrollPane);
				b.add(new JLabel(MESSAGE2));
				int response = JOptionPane.showConfirmDialog(frame, b, TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
				if (response == 0)
					Desktop.getDesktop().browse(new URI(ParallelSets.WEBSITE));
			}
		} catch (RuntimeException e) { // to make FindBugs happy
			PSLogging.logger.info("Could not check for new version.", e);
		} catch (Exception e) {
			PSLogging.logger.info("Could not check for new version.", e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					PSLogging.logger.warn("Error closing stream.", e);
				}
		}
	}
}
