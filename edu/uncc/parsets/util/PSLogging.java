package edu.uncc.parsets.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import javax.swing.JFrame;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.gui.CrashReporter;
import edu.uncc.parsets.gui.MainWindow;

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

public class PSLogging {

	private static final String LOGFILEBASE = "parsets";

	private static final String LOGFILEEXTENSION = ".log";
	
	public static Logger logger = Logger.getLogger("ParSets");

	// the default level is DEBUG, which is the second-lowest level (only TRACE is lower)
	// may consider setting it to WARN for later release builds
	// Set to Level.OFF to turn logging off.
	public static final Level DEFAULTLOGLEVEL = Level.DEBUG;

	private static File logFile;
	
	public static void init(JFrame frame, Level logLevel) {
		BasicConfigurator.configure();
		try {
			logFile = File.createTempFile(LOGFILEBASE, LOGFILEEXTENSION);
			BasicConfigurator.configure(new FileAppender(new SimpleLayout(), logFile.getAbsolutePath()));
		} catch (IOException e) {
			// probably won't be seen, only other logging outlet is console
			logger.error("Could not open log file.", e);
		}

		if (ParallelSets.isInstalled() && frame != null)
			BasicConfigurator.configure(new CrashReporter(frame));
		
		logger.setLevel(logLevel);
		
		// delete log files from previous runs
		// deletes files that are more than 24 hours old
		File oldLogFiles[] = logFile.getParentFile().listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith(LOGFILEBASE) && name.endsWith(LOGFILEEXTENSION);
			}
		});
		for (File f : oldLogFiles)
			if (logFile.lastModified()-f.lastModified() > 24*3600*1000)
				if (!f.delete())
					logger.info("Old logfile "+f.getAbsolutePath()+" could not be deleted.");

		logSystemInfo();
	}
	
	private static void logSystemInfo() {
		logger.info(MainWindow.WINDOWTITLE);
		logger.info(new Date());
		logProperty("java.vendor");
		logProperty("java.version");
		logProperty("java.vm.info");
		logProperty("java.vm.name");
		logProperty("java.vm.specification.name");
		logProperty("java.vm.specification.vendor");
		logProperty("java.vm.specification.version");
		logProperty("java.vm.vendor");
		logProperty("java.vm.version");
		logProperty("os.arch");
		logProperty("os.name");
		logProperty("os.version");
	}
	
	private static void logProperty(String propertyKey) {
		logger.info(propertyKey+" = "+System.getProperty(propertyKey));
	}
	
	public static String getLogFileAsString() {
		try {
			final BufferedInputStream bis = new BufferedInputStream( 
					new FileInputStream(logFile));
			final byte [] bytes = new byte[(int) logFile.length()];
			bis.read(bytes);
			bis.close();
			return new String(bytes);
		} catch (Exception e) {
			logger.error("Error reading in log file.", e);
		}
		return null;
	}

}
