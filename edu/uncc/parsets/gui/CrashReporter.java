package edu.uncc.parsets.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import edu.uncc.parsets.ParallelSets;

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

public class CrashReporter extends AppenderSkeleton {

	private static final String MESSAGE = 
		"Parallel Sets has encountered an error that it cannot recover from.\n" +
		"Crash reports help us improve the program. They contain no personal data.\n" +
		"Do you want to submit a crash report? \n";

	private static final String TITLE = "Fatal Error Occurred";

	private static final String REPORTURL = "http://data.eagereyes.org/parsets/postCrashReport.php";
	
	private static final String RESULTTITLE = "Report Submitted!";
	
	private static final String RESULTMSG =
		"Your report has been submitted.\n" +
		"Your reference code is ";
	
	
	private JFrame frame;

	public CrashReporter(JFrame mainFrame) {
		frame = mainFrame;
	}
	
	@Override
	protected void append(LoggingEvent e) {
		if (e.getLevel() == Level.FATAL) {
			int choice = JOptionPane.showConfirmDialog(frame, MESSAGE,
					TITLE, JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_OPTION);
			if (choice == 0) {
				String logFile = ParallelSets.getLogFileAsString();
				HashMap<String, String> data = new HashMap<String, String>();
				data.put("data", logFile);
				String result[] = postRequest(REPORTURL, data);
				JOptionPane.showMessageDialog(frame, RESULTMSG+result[0].substring(0, 6), RESULTTITLE, JOptionPane.INFORMATION_MESSAGE);
			}
			frame.dispose();
		}
	}

	public static String[] postRequest(String urlString, Map<String, String> params) {
		try {
			URL url = new URL(urlString);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			
			PrintWriter out = new PrintWriter(connection.getOutputStream());
			StringBuffer data = new StringBuffer();
			boolean first = true;
			for (Entry<String, String> p : params.entrySet()) {
				if (first)
					first = false;
				else
					data.append("&");
				data.append(p.getKey()+"="+URLEncoder.encode(p.getValue(), "UTF-8"));
			}
			out.println(data);
			out.close();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			Vector<String> lines = new Vector<String>();
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
			in.close();
			String result[] = new String[lines.size()];
			return lines.toArray(result);
		} catch (Exception e) {
			ParallelSets.logger.error("Error submitting POST request.", e);
			return null;
		}
	}
	
	public void close() {

	}

	public boolean requiresLayout() {
		return false;
	}

}
