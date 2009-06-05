package edu.uncc.parsets.util.osabstraction;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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

// http://developer.apple.com/documentation/Java/Conceptual/Java14Development/07-NativePlatformIntegration/NativePlatformIntegration.html
// http://developer.apple.com/technotes/tn2007/tn2196.html
public class MacOSX extends AbstractOS {

	// based on http://www.velocityreviews.com/forums/t151722-class-loading-on-different-opperating-systems.html
	// catches methods for com.apple.eawt.ApplicationListener
	// http://developer.apple.com/documentation/Java/Reference/1.4.2/appledoc/api/com/apple/eawt/ApplicationListener.html
	private static final class ApplicationAdapter implements InvocationHandler {
		@SuppressWarnings("unchecked")
		public Object invoke(Object proxy, Method method, Object[] args) {
//			System.err.println("Call to "+method.getName());
			if (method.getName().equals("handleAbout")) {
				// TODO: show About box
			}
			if (method.getName().equals("handleAbout") || method.getName().equals("handleQuit")) {
				try {
					Class handlerClass = Class.forName("com.apple.eawt.ApplicationEvent");
					Method setHandled = handlerClass.getMethod("setHandled", boolean.class);
					setHandled.invoke(args[0], Boolean.TRUE);
				} catch (Exception e) {
					System.err.println(e);
				}
			}
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public MacOSX() {
		// this is only for testing the ugly metal LAF to check the vertical label code
//		try {
//			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
//		} catch (Exception e) {
//			// bite me
//		}
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		try {
			Class lc = Class.forName("com.apple.eawt.ApplicationListener");
			Object listener = Proxy.newProxyInstance(this.getClass().getClassLoader(),
					new Class[] { lc }, new ApplicationAdapter());

			Class appc = Class.forName("com.apple.eawt.Application");
			Object app = appc.newInstance();
			Method m = appc.getMethod("addApplicationListener", lc);
			m.invoke(app, listener);
		} catch (Exception e) {
			PSLogging.logger.warn("Could not register Mac OS X application event handler", e);
		}
	}

	/**
	 * Installs into "~/Library/Application Support/Parallel Sets".
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void installRegular(File dbFile) {
		PSLogging.logger.info("Installing new database at "
				+ dbFile.getAbsolutePath());
		try {
			Class fileManagerClass = Class.forName("com.apple.eio.FileManager");
			Method getPathToAppBundle = fileManagerClass
					.getMethod("getPathToApplicationBundle");
			String srcPath = ((String) getPathToAppBundle.invoke(null))
					+ "/Contents/Resources/Java/" + LocalDB.LOCALDBFILENAME;
			File parentDir = dbFile.getParentFile();
			if (!parentDir.exists())
				if (parentDir.mkdir() == false)
					PSLogging.logger
							.fatal("Could not create parent directory");
			PSLogging.logger.info("Source file: " + srcPath);
			File localDBFile = new File(srcPath);
			copyFileNIO(localDBFile, dbFile);
		} catch (Exception e) {
			PSLogging.logger.fatal("Could not locate source DB file.", e);
		}
	}

	@Override
	public String openDialog(Frame frame, CSVFileFilter fileFilter) {
		FileDialog fd = new FileDialog(frame);
		if (fileFilter != null)
			fd.setFilenameFilter(fileFilter);
		fd.setDirectory(getDocsDir());
		fd.setVisible(true);
		if (fd.getFile() != null)
			return fd.getDirectory() + fd.getFile();
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	private static final Class fmParamTypes[] = { Short.TYPE, Integer.TYPE };
	@SuppressWarnings("unchecked")
	private static final Class os2iParamTypes[] = { String.class };

	// Based on code from
	// http://stackoverflow.com/questions/567874/how-do-i-find-the-users-documents-folder-with-java-in-os-x
	@Override
	public String getLocalDBDir() {
		return findFolder("asup");
	}

	public String getDocsDir() {
		return findFolder("docs");
	}

	@SuppressWarnings("unchecked")
	private String findFolder(String type) {
		String path = null;
		try {
			Class fileManagerClass = Class.forName("com.apple.eio.FileManager");
			Method oSTypeToInt = fileManagerClass.getMethod("OSTypeToInt",
					os2iParamTypes);
			Method findFolder = fileManagerClass.getMethod("findFolder",
					fmParamTypes);
			short kUserDomain = fileManagerClass.getField("kUserDomain")
					.getShort(null);
			int kDDInt = (Integer) oSTypeToInt.invoke(null, type);
			path = (String) findFolder.invoke(null, kUserDomain, kDDInt);
		} catch (Exception e) {
			PSLogging.logger.fatal("Error determining user folder " + type,
					e);
		}
		return path;
	}

	@Override
	public String shortName() {
		return "mac";
	}
	
	@Override
	public boolean isMacOSX() {
		return true;
	}
}
