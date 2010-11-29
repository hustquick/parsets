package edu.uncc.parsets;

import javax.swing.SwingUtilities;

import org.apache.log4j.Level;

import edu.uncc.parsets.gui.MainWindow;
import edu.uncc.parsets.util.BatchConvert;
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

/**
 * Parallel Sets is a visualization technique for categorical data. This class
 * sets up everything to run the program. 
 */
public class ParallelSets {

	public static final int MAJOR_VERSION = 2;

	public static final int MINOR_VERSION = 1;

	public static final String VERSION = MAJOR_VERSION+"."+MINOR_VERSION;
	
	public static final String PROGRAMNAME = "Parallel Sets";
	
	public static final String WEBSITE = "http://eagereyes.org/parsets/";	
	
	/** If true, the program is run installed by a user, and needs to act like that. That includes accessing
	 * the installed version of the database, showing a crash reporter dialog when the program crashes, etc.
	 * If false, it's the development version using its local database.
	 * 
	 * There are currently two properties that can be set to "true" on the commandline to switch
	 * this to true: <tt>parsets.use_installed_db</tt> and <tt>parsets.installed</tt>. The former
	 * will be deprecated eventually.
	 */
	protected static boolean installed = false;
	
	static {
		installed = System.getProperty("parsets.use_installed_db", "false").equalsIgnoreCase("true") ||
				System.getProperty("parsets.installed", "false").equalsIgnoreCase("true");
	}
	
	public static void main(String[] args) {
		AbstractOS.determineOS();
		
		if (args == null || args.length == 0)
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					new MainWindow();
				}
			});
		else {
			installed = false;
			PSLogging.init(null, Level.ERROR);
			BatchConvert.batchConvert(args);
		}
			
	}

	
	public static boolean isInstalled() {
		return installed;
	}
	
}
