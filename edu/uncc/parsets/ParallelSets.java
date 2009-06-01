package edu.uncc.parsets;

import java.awt.BorderLayout;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import javax.media.opengl.GLCanvas;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import edu.uncc.parsets.gui.Controller;
import edu.uncc.parsets.gui.CrashReporter;
import edu.uncc.parsets.gui.SideBar;
import edu.uncc.parsets.parsets.ParSetsView;
import edu.uncc.parsets.util.BatchConvert;
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

/**
 * Parallel Sets is a visualization technique for categorical data. This class
 * sets up everything to run the program. 
 */
public class ParallelSets {

	public static final int MAJOR_VERSION = 2;

	public static final String MINOR_VERSION = "0";

	public static final String VERSION = MAJOR_VERSION+"."+MINOR_VERSION;
	
	private static final int WINDOWHEIGHT = 600;

	private static final int WINDOWWIDTH = 900;

	public static final String PROGRAMNAME = "Parallel Sets";
	
	public static final String WINDOWTITLE = PROGRAMNAME+" V"+VERSION;

	public static final String WEBSITE = "http://eagereyes.org/parsets/";	
	
	protected static final String LOGFILEBASE = "parsets";

	protected static final String LOGFILEEXTENSION = ".log";
	
	protected static final String ICONFILE = "/support/parsets-512.gif";

	/** If true, the program is run installed by a user, and needs to act like that. That includes accessing
	 * the installed version of the database, showing a crash reporter dialog when the program crashes, etc.
	 * If false, it's the development version using its local database.
	 * 
	 * There are currently two properties that can be set to "true" on the commandline to switch
	 * this to true: <tt>parsets.use_installed_db</tt> and <tt>parsets.installed</tt>. The former
	 * will be deprecated eventually.
	 */
	protected static boolean installed = false;
	
	private static Controller controller;

	public static Logger logger = Logger.getLogger("ParSets");

	// the default level is DEBUG, which is the second-lowest level (only TRACE is lower)
	// may consider setting it to WARN for later release builds
	// Set to Level.OFF to turn logging off.
	private static Level DEFAULTLOGLEVEL = Level.DEBUG;

	private static File logFile;

	static {
		installed = System.getProperty("parsets.use_installed_db", "false").equalsIgnoreCase("true") ||
				System.getProperty("parsets.installed", "false").equalsIgnoreCase("true");
	}
	
	public static void main(String[] args) {
		AbstractOS.determineOS();
		
		if (args.length == 0)
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					interactiveModeSetup();
				}
			});
		else {
			installed = false;
			DEFAULTLOGLEVEL = Level.ERROR;
			initLogging(null);
			BatchConvert.batchConvert(args);
		}
			
	}

	private static void interactiveModeSetup() {
		controller = new Controller();

		JFrame f = new JFrame(WINDOWTITLE);
		f.setSize(WINDOWWIDTH, WINDOWHEIGHT);		
		f.setIconImage(new ImageIcon(ICONFILE).getImage());
		//f.setLayout(new MigLayout("insets 0,fill", "[min!]0[grow,fill]", "[grow,fill]"));
		f.setLayout(new BorderLayout());
		
		initLogging(f);
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				logger.fatal("Uncaught exception, program terminating.", e);
			}
		});
		
		SideBar sideBar = new SideBar(f, controller);
		f.add(sideBar, BorderLayout.WEST);

		GLCanvas glCanvas = new GLCanvas();
		glCanvas.addGLEventListener(new ParSetsView(glCanvas, controller));
		f.add(glCanvas, BorderLayout.CENTER);
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

	private static void initLogging(JFrame frame) {
		BasicConfigurator.configure();
		try {
			logFile = File.createTempFile(LOGFILEBASE, LOGFILEEXTENSION);
			BasicConfigurator.configure(new FileAppender(new SimpleLayout(), logFile.getAbsolutePath()));
		} catch (IOException e) {
			// probably won't be seen, only other logging outlet is console
			logger.error("Could not open log file.", e);
		}

		if (isInstalled() && frame != null)
			BasicConfigurator.configure(new CrashReporter(frame));
		
		logger.setLevel(DEFAULTLOGLEVEL);
		
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
		
//		logger.info("Current directory: "+new File(".").getAbsolutePath());
	}
	
	private static void logSystemInfo() {
		logger.info(WINDOWTITLE);
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
	
	public static Controller getController() {
		return controller;
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
	
	public static boolean isInstalled() {
		return installed;
	}
	
}
