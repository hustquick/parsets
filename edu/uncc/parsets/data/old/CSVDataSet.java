package edu.uncc.parsets.data.old;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.uncc.parsets.data.DataType;


public class CSVDataSet implements Iterable<DataDimension> {

	// List of dimensions, in order of appearance in data file
	protected List<DataDimension> dimensions = new ArrayList<DataDimension>();

	// Map the dimension key and/or name to the dimension. Dimensions can be
	// included more than once under different names here.
	protected Map<String, DataDimension> dimensionsmap = new HashMap<String, DataDimension>();

	protected String filename;
	
	// The name of the dataset, or the filename if nothing specified in the XML file.
	private String name;

	private String section = "Misc";
	
	private String url;

	private String sourceName = "";

	private String sourceURL = "";

	private int numRecords = 0;
	
	public CSVDataSet(String filename) {
		this.filename = filename;
		name = (new File(filename)).getName();
		int lastPeriod = name.lastIndexOf('.');
		if (lastPeriod > 0)
			name = name.substring(0, lastPeriod);
	}

	public DataDimension addDimension(String key, DataDimension dim) {
		dimensionsmap.put(key, dim);
		return dim;
	}

	public void instantiateDimension(String key) {
		if (dimensionsmap.containsKey(key))
			dimensions.add(dimensionsmap.get(key));
		else {
			DataDimension dim = new DataDimension(key, DataType.numerical);
			dimensions.add(dim);
		}
	}
	
	public DataDimension getDimension(int index) {
		return dimensions.get(index);
	}

	public int getNumDimensions() {
		return dimensions.size();
	}

	public Iterator<DataDimension> iterator() {
		return dimensions.iterator();
	}

	
	public String getFilename() {
		return filename;
	}

	public String getFileBaseName() {
		return (new File(filename)).getName();
	}

	public String getName() {
		return name;
	}
	
	public void setName(String newName) {
		name = newName;
	}

	public void setSection(String value) {
		section = value;
	}
	
	public String getSection() {
		return section;
	}
	
	public void setURL(String newURL) {
		url = newURL;
	}
	
	public String getURL() {
		return url;
	}
	
	public void setSource(String text) {
		sourceName = text;
	}

	public String getSource() {
		return sourceName;
	}
	
	public void setSourceURL(String text) {
		sourceURL = text;
	}
	
	public String getSourceURL() {
		return sourceURL;
	}
	
	public void setNumRecords(int newNum) {
		numRecords = newNum;
	}
	
	public int getNumRecords() {
		return numRecords;
	}
}
