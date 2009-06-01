package edu.uncc.parsets.gui;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.data.LocalDB;
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

	private static final String VERSIONURL = "http://data.eagereyes.org/parsets/currentversion.txt?version="+ParallelSets.VERSION+"&os=";
	
	private static final String MESSAGE1 = "A new version of the program is available\n" +
			"(V";
	
	private static final String MESSAGE2 = ", you have V"+ParallelSets.VERSION+").\n" +
			"Do you want to be taken to the website?";
	
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
			String version = reader.readLine();
			if (version != null && !version.equals(LocalDB.getDefaultDB().getSetting(LocalDB.LAST_VERSION_SEEN_KEY))) {
				LocalDB.getDefaultDB().storeSetting(LocalDB.LAST_VERSION_SEEN_KEY, version);
				String versionParts[] = version.split("\\.");
				if (Integer.parseInt(versionParts[0]) > ParallelSets.MAJOR_VERSION ||
						versionParts[1].compareTo(ParallelSets.MINOR_VERSION) > 0) {
					int response = JOptionPane.showConfirmDialog(frame, MESSAGE1+version+MESSAGE2, TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
					if (response == 0)
						Desktop.getDesktop().browse(new URI(ParallelSets.WEBSITE));
				}
			}
		} catch (Exception e) {
			ParallelSets.logger.info("Could not check for new version.", e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					ParallelSets.logger.warn("Error closing stream.", e);
				}
		}
	}
}
