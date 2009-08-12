package edu.uncc.parsets.data.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;
import edu.uncc.parsets.data.LocalDB;
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

public class CSVParser {

	private String csvFileName;

	private CSVParserListener callBack;

	private CSVDataSet dataSet;

	private char separator = ';';

	private String[] columns;

	private CSVReader parser;
		
	public CSVParser(String fileName, CSVParserListener receiver) {
		csvFileName = fileName;
		callBack = receiver;
		dataSet = new CSVDataSet(fileName);
	}

	/**
	 * Call the parser to read in a dataset, and parse the XML metadata file if
	 * it exists. The parser runs in a separate thread, which is returned. The
	 * caller can wait for the thread to finish using {@link Thread#join()},
	 * or provide a callback that receives progress notifications.
	 */
	public Thread analyzeCSVFile() {
		MetaDataParser mp = new MetaDataParser();
		String metafilename = csvFileName.substring(0, csvFileName.lastIndexOf('.'))
				+ ".xml";
		if (new File(metafilename).exists()) {
			mp.parse(dataSet, metafilename);
		} else {
			if (metafilename.contains("_")) {
				metafilename = metafilename.substring(0, metafilename.lastIndexOf("_"))+".xml";
				if (new File(metafilename).exists()) {
					mp.parse(dataSet, metafilename);
					String name = new File(csvFileName).getName();
					name = name.substring(0, name.lastIndexOf('.'));
					name = name.replace('_', ' ');
					dataSet.setName(name);
				}
			}
		}
		Thread t = new Thread() {
			public void run() {
				analyzeFile();
			}
		};
		t.start();
		return t;
	}
	
	private void analyzeFile() {
		
		float numLinesEstimate = 1000;

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(csvFileName));
			String firstLine = reader.readLine();
			// guess separator char
			int numCommas = 0;
			int numSemicolons = 0;
			if (firstLine != null) {
				for (int i = 0; i < firstLine.length(); i++) {
					char c = firstLine.charAt(i);
					if (c == ',')
						numCommas++;
					else if (c == ';')
						numSemicolons++;
				}
				if (numCommas > numSemicolons)
					separator = ',';
			}
			CSVReader parser = new CSVReader(new FileReader(csvFileName), separator);
			String[] headerLine = parser.readNext();
			for (String columnName : headerLine)
				dataSet.instantiateDimension(columnName);

			int numColumns = headerLine.length;
			
			if (firstLine != null) {
				int numBytes = firstLine.length()+1;
				int numLines = 1;
				String columns[];
				while (((columns = parser.readNext()) != null) && (numLines < 100)) {
					if (columns.length != numColumns) {
						PSLogging.logger.error("Found "+columns.length+" columns instead of "+numColumns+" in line "+numLines);
						if (callBack != null)
							callBack.errorWrongNumberOfColumns(numColumns, columns.length, numLines);
						return;
					}
					numLines++;
					for (int i = 0; i < columns.length; i++) {
						numBytes += columns[i].length()+1;
						dataSet.getDimension(i).addValue(columns[i]);
					}
					dataSet.setNumRecords(numLines);
				}
				File f = new File(csvFileName);
				numLinesEstimate = (int) (f.length() / numBytes) * numLines;
				numLinesEstimate /= 100f; // to scale from 0 to 100

				while (columns != null) {
					if (columns.length != numColumns) {
						PSLogging.logger.error("Found "+columns.length+" columns instead of "+numColumns+" in line "+numLines);
						if (callBack != null)
							callBack.errorWrongNumberOfColumns(numColumns, columns.length, numLines);
						return;
					}
					numLines++;
					if ((numLines & 0xff) == 0 && callBack != null)
						callBack.setProgress((int)(numLines/numLinesEstimate));
					for (int i = 0; i < columns.length; i++)
						dataSet.getDimension(i).addValue(columns[i]);
					dataSet.setNumRecords(numLines);
					columns = parser.readNext();
				}
			}
		} catch (FileNotFoundException e) {
			PSLogging.logger.error("File not found: "+csvFileName, e);
			if (callBack != null)
				callBack.errorFileNotFound(csvFileName);
		} catch (IOException e) {
			PSLogging.logger.error("IOException while reading file: "+csvFileName, e);
			if (callBack != null)
				callBack.errorReadingFile(csvFileName);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				PSLogging.logger.error("IOException while closing file: "+csvFileName, e);
			}
		}
		if (callBack != null)
			callBack.setDataSet(dataSet);
	}
	
	public void streamToDB(LocalDB db) {
		try {
			parser = new CSVReader(new FileReader(csvFileName), separator);
			columns = parser.readNext();
			db.addLocalDBDataSet(dataSet, this);
			if (callBack != null)
				callBack.importDone();
		} catch (Exception e) {
			PSLogging.logger.error("Error streaming data", e);
			if (callBack != null)
				callBack.errorReadingFile(csvFileName);
		}
	}

	public float[] readNextLine() {
		try {
			columns = parser.readNext();
			if (columns != null) {
				float values[] = new float[columns.length];
				for (int i = 0; i < columns.length; i++) {
					switch (dataSet.getDimension(i).getDataType()) {
					case categorical:
						values[i] = dataSet.getDimension(i).getNumForKey(columns[i]);
						break;
					case numerical:
						values[i] = Float.valueOf(columns[i]);
						break;
					default:
						values[i] = 0;
						break;
					}
					return values;
				}
			}
		} catch (Exception e) {
			PSLogging.logger.error("Error reading line", e);
		}
		return null;
	}
	
	public CSVDataSet getDataSet() {
		return dataSet;
	}
}
