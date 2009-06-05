package edu.uncc.parsets.util.osabstraction;

import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import javax.swing.JFileChooser;

import com.sun.jna.Platform;

import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.data.LocalDB;
import edu.uncc.parsets.gui.DBTab.CSVFileFilter;
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

public abstract class AbstractOS {

	protected static boolean javaStore = false;
	
	static {
		javaStore = System.getProperty("parsets.javastore", "false").equalsIgnoreCase("true");
	}

	
	private static AbstractOS currentOS;

	protected AbstractOS() {
	}	
	
	/**
	 * Install the DB when running as a regular application.
	 * @param dbFile the location where to install the database
	 */
	protected void installRegular(File dbFile) {
		
	}

	/**
	 * Install the DB when running from the JavaStore. The difference is that
	 * NIO classes can't be used, since the db is read from the jar file.
	 * @param dbFile the location where to install the database
	 */
	protected void installJavaStore(File dbFile) {
		PSLogging.logger.info("Installing new database at " + dbFile.getAbsolutePath());
		File parentDir = dbFile.getParentFile();
		if (!parentDir.exists())
			if (parentDir.mkdir() == false)
				PSLogging.logger.fatal("Could not create parent directory");
		
		PSLogging.logger.info("Source file: " + this.getClass().getResource("/"+LocalDB.LOCALDBFILENAME));
	    InputStream fromStream = null;
	    FileOutputStream toStream = null;
		try {
			fromStream = this.getClass().getResourceAsStream("/"+LocalDB.LOCALDBFILENAME);
			toStream = new FileOutputStream(dbFile);
		    byte[] buffer = new byte[4096*4];
		    int bytesRead;

		    while ((bytesRead = fromStream.read(buffer)) != -1)
		    	toStream.write(buffer, 0, bytesRead);
		} catch (Exception e) {
			PSLogging.logger.error("Error installing DB", e);
		} finally {
			if (fromStream != null) {
				try {
					fromStream.close();
				} catch (IOException e) {
				}
			}
			if (toStream != null) {
				try {
					toStream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public String getLocalDBPath(String dbFileName) {
		String dbPath = getLocalDBDir()+File.separatorChar+ParallelSets.PROGRAMNAME+File.separatorChar+dbFileName;
		File dbFile = new File(dbPath);
		if (!dbFile.exists()) {
			if (javaStore)
				installJavaStore(dbFile);
			else
				installRegular(dbFile);
		}
		return dbPath;
	}
	
	public abstract String getLocalDBDir();

	/**
	 * Call determineOS before calling this function.
	 * 
	 * @return The current OS object, or null if not supported.
	 */
	public static AbstractOS getCurrentOS() {
		return currentOS;
	}
	
	public static void determineOS() {
		if (Platform.isMac())
			currentOS = new MacOSX();
		else if (Platform.isWindows())
			currentOS = new Windows();
		else if (Platform.isLinux()) // may eventually include more Unices
			currentOS = new Linux();
		else {
			PSLogging.logger.fatal("OS not supported.");
		}
	}
	
	// Based on code from
	// http://stackoverflow.com/questions/106770/standard-concise-way-to-copy-a-file-in-java
	public static void copyFileNIO(File sourceFile, File destFile) {
		PSLogging.logger.info("Copying "+sourceFile.getAbsolutePath()+" to "+destFile.getAbsolutePath());
		if (!destFile.exists()) {
			try {
				destFile.createNewFile();
			} catch (IOException e) {
				PSLogging.logger.error("Could not create destination file.", e);
			}
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} catch (FileNotFoundException e) {
			PSLogging.logger.error("Source file not found.", e);
		} catch (IOException e) {
			PSLogging.logger.error("IO Error during copy.", e);
		} finally {
			if (source != null) {
				try {
					source.close();
				} catch (IOException e) {
					PSLogging.logger.error("Error closing source file.", e);
				}
			}
			if (destination != null) {
				try {
					destination.close();
				} catch (IOException e) {
					PSLogging.logger.error("Error closing destination file.", e);
				}
			}
		}
	}

	public String openDialog(Frame frame, CSVFileFilter fileFilter) {
		// TODO: start in user's home directory
	    JFileChooser fileChooser = new JFileChooser(new File("."));
	    if (fileFilter != null)
	    	fileChooser.setFileFilter(fileFilter);
	    if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
	    	return fileChooser.getSelectedFile().getAbsolutePath();
	    else
	    	return null;
	}

	public abstract String shortName();
	
}
