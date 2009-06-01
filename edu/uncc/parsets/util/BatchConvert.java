package edu.uncc.parsets.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.uncc.parsets.data.DataSet;
import edu.uncc.parsets.data.JSONExport;
import edu.uncc.parsets.data.LocalDB;
import edu.uncc.parsets.data.LocalDBDataSet;
import edu.uncc.parsets.data.old.CSVDataSet;
import edu.uncc.parsets.gui.DataWizard;

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

public class BatchConvert {

	private static final String TEMPDBFILENAME = "temp.db";

	private static final String USAGE = "Usage: convert <srcdir> <destdir>";

	public static final String BASEURL = "http://data.eagereyes.org/";
	
	public static void batchConvert(String args[]) {
		if (args.length != 3 || !args[0].equals("convert")) {
			System.err.println(USAGE);
			return;
		}
		File tempDBFile = new File(TEMPDBFILENAME);
		if (tempDBFile.exists())
			if (!tempDBFile.delete())
				System.err.println("Could not delete temporary database "+TEMPDBFILENAME);
		
		File srcDir = new File(args[1]);
		File dstDir = new File(args[2]);
		
		LocalDB tempDB = new LocalDB(TEMPDBFILENAME);
		
		List<File> csvFiles = scanDir(srcDir, dstDir);
		for (File f : csvFiles) {
			CSVDataSet csvData = DataWizard.parseCSVFile(f.getPath(), null);
			String newPath = rebase(f.getPath(), dstDir.getPath())+".json.gz";
			csvData.getMetaData().setURL(BASEURL+newPath);
			String handle = tempDB.addLocalDBDataSet(csvData);
			DataSet localDS = tempDB.getDataSet(handle);
			JSONExport.exportDataSet((LocalDBDataSet)localDS, newPath);
			System.out.println(f.getPath()+" => "+localDS.getURL());
		}
		
		String indexName = JSONExport.exportDBIndex(tempDB, dstDir.getPath()+File.separatorChar+"index");
		System.out.println("Index: "+indexName);
		System.out.println("Done.");
		
		if (!tempDBFile.delete())
			System.err.println("Could not delete temporary database "+TEMPDBFILENAME);
	}

	private static String rebase(String original, String newBase) {
		StringBuilder newPath = new StringBuilder(newBase);
		newPath.append(original.substring(original.indexOf(File.separatorChar), original.lastIndexOf(".")));
		return newPath.toString();
	}

	private static List<File> scanDir(File srcDir, File dstDir) {
		if (!dstDir.exists())
			if (!dstDir.mkdir())
				System.err.println("Could not create directory "+dstDir.getPath());
		List<File> files = new ArrayList<File>();
		for (File f : srcDir.listFiles()) {
			if (f.isDirectory()) {
				if (!f.getName().startsWith(".")) {
					File newDest = new File(dstDir.getPath()+File.separatorChar+f.getName());
					files.addAll(scanDir(f, newDest));
				}
			} else if (f.getName().endsWith(".csv")) {
				files.add(f);
//				System.out.println(f.getPath());
			}
		}
		return files;
	}
	
}
