package edu.uncc.parsets.data.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.Semaphore;

import au.com.bytecode.opencsv.CSVReader;

import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.data.DataType;
import edu.uncc.parsets.util.PSLogging;


public class CSVParser extends Thread {

	private String csvFileName;

	private DataSetReceiver callBack;

	private CSVDataSet dataSet;

	private char separator = ';';
	
	public CSVParser(String fileName, DataSetReceiver receiver) {
		csvFileName = fileName;
		callBack = receiver;
		dataSet = new CSVDataSet(fileName);
	}

	public void run() {
		
		boolean hasMetaData = false;
		
		if (csvFileName.lastIndexOf('.') > 0) {
			String metaDataFileName = csvFileName.substring(0, csvFileName.lastIndexOf('.'))+".xml";
			if ((new File(metaDataFileName)).exists()) {
				MetaDataParser metaParser = new MetaDataParser();
				hasMetaData = metaParser.parse(dataSet, metaDataFileName);
			}
		}
		
		float numLinesEstimate = 1000;

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(csvFileName));
			String firstLine = reader.readLine();
			// guess separator char
			int numCommas = 0;
			int numSemicolons = 0;
			for (int i = 0; i < firstLine.length(); i++) {
				char c = firstLine.charAt(i);
				if (c == ',')
					numCommas++;
				else if (c == ';')
					numSemicolons++;
			}
			if (numCommas > numSemicolons)
				separator = ',';

			CSVReader parser = new CSVReader(new FileReader(csvFileName), separator);
			String[] headerLine = parser.readNext();
			for (int i = 0; i < headerLine.length; i++)
				dataSet.instantiateDimension(headerLine[i]);

			int numBytes = firstLine.length()+1;
			int numLines = 1;
			String columns[];
			while (((columns = parser.readNext()) != null) && (numLines < 100)) {
				numLines++;
				for (int i = 0; i < columns.length; i++) {
					numBytes += columns[i].length()+1;
					if (!hasMetaData) {
						dataSet.getDimension(i).addValue(columns[i]);
					}
				}
			}
			File f = new File(csvFileName);
			numLinesEstimate = (int) (f.length() / numBytes) * numLines;
			numLinesEstimate /= 100f; // to scale from 0 to 100
		} catch (FileNotFoundException e) {
			PSLogging.logger.error("File not found: "+csvFileName, e);
		} catch (IOException e) {
			PSLogging.logger.error("IOException while reading file: "+csvFileName, e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				PSLogging.logger.error("IOException while closing file: "+csvFileName, e);
			}
		}
		
		try {
			Reader fileReader = new FileReader(csvFileName);
			CSVReader parser = new CSVReader(fileReader, separator);

			int linenum = 1;

			String[] firstline = parser.readNext();
			String[] line = parser.readNext();


			linenum++;

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
			}

			for (int i = 0; i < dimensions.length; i++)
				// Initializing Dimension in DataDimension
				dimensions[i].initializeDimension();

			callBack.setDataSet(dataSet);

		} catch (FileNotFoundException e) {
			PSLogging.logger.error("File not found: "+csvFileName, e);
		} catch (IOException e) {
			PSLogging.logger.error("IOException while reading file: "+csvFileName, e);
		}
	}

	public CSVDataSet getDataSet() {
		return dataSet;
	}
}
