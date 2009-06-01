package edu.uncc.parsets.data.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.Semaphore;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;


import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.data.DataType;
import edu.uncc.parsets.data.old.MetaData.DimensionMetaData;

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
 * Parser for CSV files. Needs to be rewritten to analyze the data file first,
 * then stream the data into the DB.
 */
public class CSVReader implements Runnable {

	private String fileName;

	private MetaData metaData;

	private DataSetReceiver callBack;

	private Semaphore semaphore = new Semaphore(0);

	private CSVDataSet dataSet;

	// TODO: integrate progress bar in DataWizard window, also show progress bar
	// while writing to db (in "indeterminate" mode).
	private boolean showProgressDialog = false;

	private JProgressBar progressBar;

	private JDialog progressDialog;

	private char separator = ';';
	
	/**
	 * Load a dataset given the metadata. This function loads the file
	 * asynchronously and then calls the callback, if one is provided.
	 * If the callback is null, it does a synchronous read. The data set
	 * can then be retrieved using the {@link #getDataSet()} function.
	 * 
	 * @param fileName the CSV file to open
	 * @param metaData the metadata to use while reading the file.
	 * @param receiver the callback to call when loading has finished, or null for sychronous load
	 */
	public void readFile(String fileName, MetaData metaData, DataSetReceiver receiver) {

		this.fileName = fileName;
		this.metaData = metaData;
		if (receiver != null)
			callBack = receiver;
		else {
			callBack = new DataSetReceiver() {
				public void setDataSet(CSVDataSet data) {
					dataSet = data;
					semaphore.release();
				}
			};
			showProgressDialog = false;
		}
		
		Thread worker = new Thread(this);
		worker.start();

		if (receiver == null) {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void run() {

		if (showProgressDialog) {
			progressDialog = new JDialog((JFrame) null, "Opening File ...", false);
			progressBar = new JProgressBar();
			progressBar.setIndeterminate(true);
			progressDialog.add(progressBar);
			progressDialog.setSize(300, 40);
			progressDialog.setVisible(true);
		}
		
		CSVDataSet dataSet = new CSVDataSet(fileName);
		int linenum = 0;

		float numLinesEstimate = 1000;

		BufferedReader file = null;
		
		try {
			file = new BufferedReader(new FileReader(fileName));
			int numBytes = 0;
			int lineNum = 0;
			String line;
			while (((line = file.readLine()) != null) && (lineNum < 10)) {
				lineNum++;
				numBytes += line.length();
			}
			if (line != null) {
				int numCommas = 0;
				int numSemicolons = 0;
				for (int i = 0; i < line.length(); i++) {
					char c = line.charAt(i);
					if (c == ',')
						numCommas++;
					else if (c == ';')
						numSemicolons++;
				}
				if (numCommas > numSemicolons)
					separator = ',';
			}
			File f = new File(fileName);
			numLinesEstimate = (int) (f.length() / numBytes) * 10;
			// System.out.println("Estimated number of lines in data file:
			// "+(int)numLinesEstimate);
			numLinesEstimate /= 100f; // pre-divide so we don't have to
			// multiply by 100 on every line
		} catch (FileNotFoundException e) {
			ParallelSets.logger.error("File not found: "+fileName, e);
		} catch (IOException e) {
			ParallelSets.logger.error("IOException while reading file: "+fileName, e);
		} finally {
			try {
				file.close();
			} catch (IOException e) {
				ParallelSets.logger.error("IOException while closing file: "+fileName, e);
			}
		}

		if (showProgressDialog) {
			progressBar.setMinimum(0);
			progressBar.setMaximum((int) (numLinesEstimate * 100));
			progressBar.setValue(0);
			progressBar.setIndeterminate(false);
		}
		
		try {
			Reader is = new FileReader(fileName);
			au.com.bytecode.opencsv.CSVReader parser = new au.com.bytecode.opencsv.CSVReader(is, separator);

			String[] firstline = parser.readNext();
			linenum++;
			String[] line = parser.readNext();
			DataDimension[] dimensions = new DataDimension[firstline.length];

			for (int i = 0; i < firstline.length; i++) {
				dimensions[i] = dataSet.addDimension(firstline[i], (int) numLinesEstimate);
				DimensionMetaData dimMeta = metaData.getDimensionMetaData(firstline[i]);
				if (dimMeta != null)
					dimensions[i].setDataType(dimMeta.getType());
				else {
					metaData.addDimension(firstline[i], new MetaData.DimensionMetaData(DataType.categorical, null));
					dimensions[i].setDataType(DataType.textual);
				}
			}

			dataSet.setMetaData(metaData);

			linenum++;

			// Adding Values in DataDimension

//			int currentDecade = 0;

			while (line != null) {

				for (int i = 0; i < line.length; i++) {

					switch (dimensions[i].getDataType()) {
					case categorical:
						dimensions[i].addDataItem(line[i]);
						break;

					case numerical:
						try {
							dimensions[i].addDataItem(Float.parseFloat(line[i]));
						} catch (NumberFormatException e) {
							dimensions[i].addDataItem(Float.MAX_VALUE);
						}
						break;

					case textual:
						dimensions[i].addTextData(line[i]);
						break;
					}

				}
				line = parser.readNext();
				linenum++;

				if (showProgressDialog)
					progressBar.setValue(linenum);

//				if ((int) ((float) linenum / numLinesEstimate) > currentDecade) {
//					currentDecade += 10;
//					if (currentDecade < 100)
//						System.out.print(currentDecade + "% ... ");
//					else if (currentDecade == 100)
//						System.out.println("100%");
//				}

			}

			for (int i = 0; i < dimensions.length; i++)
				// Initializing Dimension in DataDimension
				dimensions[i].initializeDimension();

			callBack.setDataSet(dataSet);
			if (showProgressDialog)
				progressDialog.setVisible(false);

		} catch (FileNotFoundException e) {
			ParallelSets.logger.error("File not found: "+fileName, e);
		} catch (IOException e) {
			ParallelSets.logger.error("IOException while reading file: "+fileName, e);
		}
	}

	public CSVDataSet getDataSet() {
		return dataSet;
	}
}
