package edu.uncc.parsets.data.old;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

import edu.uncc.parsets.data.DataType;

/**
 * A dimension contains the data of one dimension in the data, for all the
 * records. In addition to the single values, there are also some statistics
 * like the number of different values, occurrence counts, etc.
 * 
 * The process for filling a dimension with data is as follows. First, the data
 * is read in without knowing how many values there will be. This is
 * accomplished by calling {@link DataDimension#addDataItem}. Once the file is
 * read in, the function {@link DataDimension#initializeDimension} must be
 * called to process the list into an array, and to compute statistics. Only
 * once this function was called, can those values be retrieved.
 * 
 */
public class DataDimension {

	// * The key (internal name) for this dimension
	private String dimensionKey;

	private DataType type;

	private String name;

	private Map<String, String> categoryNames = new TreeMap<String, String>();

	// * The map of category keys to integers, for faster lookup when parsing
	private Map<String, Integer> categoryMap = new TreeMap<String, Integer>();

	private ArrayList<String> categories = new ArrayList<String>();

	private Set<String> uniqueValues = new TreeSet<String>();

	public DataDimension(String key, DataType dataType) {
		dimensionKey = key;
		type = dataType;
	}

	public void addCategory(String key, String name) {
		categoryNames.put(key, name);
	}
	
	public void addValue(String value) {
		if (uniqueValues.size() < 100)
			uniqueValues.add(value);
		switch(type) {
		case numerical:
			try {
				Float.parseFloat(value);
			} catch (ParseException e) {
				type = DataType.categorical;
			}
			break;
		case categorical:
			if (uniqueValues.size() > 100)
				type = DataType.textual;
			break;
		}
	}
	
	public Set<String> getUniqueValues() {
		return uniqueValues;
	}
	
	public String getName() {
		return name;
	}

	public String getKey() {
		return dimensionKey;
	}

	public String toString() {
		return dimensionKey;
	}

	public String getCategoryName(int categoryValue) {
		if (type != DataType.textual)
			return categories.get(categoryValue);
		else {
			for (Map.Entry<String, Integer> e : categoryMap.entrySet()) {
				if (e.getValue() == categoryValue)
					return e.getKey();
			}
			return null;
		}
	}

	public void setCategoryName(int categoryValue, String name) {
		categoryNames.put(categories.get(categoryValue), name);
	}

	public String getCategoryLabel(int categoryValue) {
		return categories.get(categoryValue);
	}

	public int getNumCategories() {
		return categories.size();
	}

	public DataType getDataType() {
		return type;
	}

	public void setDataType(DataType type) {
		this.type = type;
	}
	
	public void setName(String newName) {
		name = newName;
	}
}
