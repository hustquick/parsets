package edu.uncc.parsets.data.old;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
 * A dimension contains the data of one dimension in the data, for all the
 * records. In addition to the single values, there are also some statistics
 * like the number of different values, occurrence counts, etc. This is a
 * legacy class that needs to be rewritten.
 * 
 */
public class DataDimension {

	// * This dimension's datatype - usually determined by the corresponding
	// metadata entry
	protected DataType datatype;

	// * The key (internal name) for this dimension
	protected String dimensionKey;

	DimensionMetaData metaData;

	// * The map of category keys to integers, for faster lookup when parsing
	protected Map<String, Integer> categoryMap = new TreeMap<String, Integer>();

	protected ArrayList<String> categories = new ArrayList<String>();

	/**
	 * The list of values, while the data is being read in. This list is
	 * destroyed when the data is copied into the array.
	 */
	protected List<Float> valuesList;

	protected float[] values;

	private int[] occurrenceCounts;

	private float minimum;
	private float maximum;

	private int nUnknowns;

	protected SimpleDateFormat dateFormat;

	private float[] frequencies;

	public DataDimension(String key, int estimatedNumRecords) {
		dimensionKey = key;
		valuesList = new ArrayList<Float>(estimatedNumRecords);
		nUnknowns = 0;
	}

	/**
	 * AddDataItem adds a new value to the list, creating a new integer mapping
	 * if necessary.
	 * 
	 * @param key
	 *            The item key of the new value
	 */
	public void addDataItem(String key) {

		if (key == null || key.length() == 0) {
			key = "<empty>";
			nUnknowns++;
		}

		Integer catNum = categoryMap.get(key);
		if (catNum != null) {
			valuesList.add((float) catNum);
		} else {
			int newKey = categoryMap.size();
			// if ((metaData != null) && (metaData.getCategoryIndex(key) >= 0))
			// newKey = metaData.getCategoryIndex(key);
			categoryMap.put(key, newKey);
			valuesList.add((float) newKey);
			categories.add(key);
		}
	}

	public void addTextData(String text) {

		if (text.length() == 0) {
			text = "<empty>";
			nUnknowns++;
		}

		Integer key = categoryMap.get(text);
		if (key != null)
			valuesList.add((float) key);
		else {
			int newKey = categoryMap.size();
			categoryMap.put(text, newKey);
			valuesList.add((float) newKey);
			categories.add(text);
		}
	}

	/**
	 * Add a data item as a number. The corresponding mappings must be set with
	 * addMapping()
	 * 
	 * @param val
	 *            the number to add
	 */
	public void addDataItem(int val) {
		valuesList.add((float) val);
		addMapping(val, Integer.toString(val));
	}

	/**
	 * Add a data item as a float. The corresponding mappings must be set with
	 * addMapping()
	 * 
	 * @param val
	 *            the number to add
	 */
	public void addDataItem(float val) {

		valuesList.add(val);
		addMapping((int) val, Float.toString(val));

		if (val == Float.MAX_VALUE) {
			nUnknowns++;
		}
	}

	public void addDate(String dateString) {
		try {
			getDateFormat().getCalendar().setTime(
					getDateFormat().parse(dateString));
			valuesList
					.add((float) dateFormat.getCalendar().getTimeInMillis() / 1000);
			// categoryMap.put((int)dateFormat.getCalendar().getTimeInMillis() /
			// 1000, dateString);

		} catch (ParseException e) {

			if (dateString.indexOf("XX") != -1) {
				valuesList.add(Float.MAX_VALUE);
				// categoryMap.put((int)Float.MAX_VALUE, "??");
				nUnknowns++;
			}

			else {

				System.err.println("Could not use pattern "
						+ dateFormat.toPattern()
						+ " to parse date from string: " + dateString);
				e.printStackTrace();

			}
		}
	}

	/**
	 * Add a new mapping from numeric code to String key.
	 * 
	 */
	public void addMapping(int val, String key) {
		categoryMap.put(key, val);
	}

	public void initializeDimension() {

		minimum = Float.MAX_VALUE;
		maximum = Float.MIN_VALUE;

		values = new float[valuesList.size()];

		Iterator<Float> it = valuesList.iterator();
		// HashMap<Float, Integer> occurrences = new HashMap<Float, Integer>();
		//		
		for (int i = 0; i < values.length; i++) {
			values[i] = it.next();
			// occurrences.put(values[i], null);
		}

		valuesList = null;
		// System.err.println("initializing "+getName());
		if (datatype == DataType.categorical) {

			int numValues = categoryMap.size();
			// if (metaData != null)
			// if (metaData.getNumCategories() > numValues)
			// numValues = metaData.getNumCategories();

			occurrenceCounts = new int[numValues];
			for (float i : values)
				occurrenceCounts[(int) i]++;

			frequencies = new float[numValues];
			for (int i = 0; i < occurrenceCounts.length; i++) {
				frequencies[i] = (float) occurrenceCounts[i]
						/ (float) values.length;
			}

		}

		else if (datatype == DataType.numerical) {

			for (float i : values) {

				if (i > maximum && i != Float.MAX_VALUE) {
					maximum = i;
				}

				if (i < minimum) {
					minimum = i;
				}

			}

		}
	}

	/**
	 * The dimension's display name. Returns the key for the moment, but this
	 * needs to be retrieved from the metadata later.
	 * 
	 * @return
	 */
	public String getName() {
		if ((metaData != null) && (metaData.getName() != null))
			return metaData.getName();
		else
			return dimensionKey;
	}

	public String getKey() {
		return dimensionKey;
	}

	public String toString() {
		return dimensionKey;
	}

	public float[] getValues() {
		return values;
	}

	public int getNumRecords() {
		return values.length;
	}

	public int[] getOccurenceCounts() {
		return occurrenceCounts;
	}

	public float[] getCategoryFrequencies() {
		return frequencies;
	}

	public String getCategoryName(int categoryValue) {
//		if (metaData != null)
//			return metaData.getCategoryLabel(categories.get(categoryValue));
//		else {
			if (datatype != DataType.textual)
				return categories.get(categoryValue);
			else {
				for (Map.Entry<String, Integer> e : categoryMap.entrySet()) {
					if (e.getValue() == categoryValue)
						return e.getKey();
				}
				return null;
			}
//		}
	}

	public void setCategoryName(int categoryValue, String name) {
		metaData.categoryNames.put(categories.get(categoryValue), name);
	}

	public String getCategoryLabel(int categoryValue) {
		if (metaData != null)
			return metaData.getCategoryLabel(categories.get(categoryValue));
		else
			return categories.get(categoryValue);
	}

	public int getNumCategories() {
		return categories.size();
	}

	public DataType getDataType() {
		return datatype;
	}

	public void setDataType(DataType type) {
		datatype = type;
	}

	/**
	 * @return the minimum
	 */
	public float getMinimum() {
		return minimum;
	}

	/**
	 * @return the maximum
	 */
	public float getMaximum() {
		return maximum;
	}

	public float getCategoryValue(String categoryName) {

		Integer value = categoryMap.get(categoryName);
		if (value == null) {
			value = categoryMap.get(metaData.getCategoryKey(categoryName));
		}

		if (value != null)
			return value.floatValue();
		else
			return Float.NaN;
	}

	/**
	 * @return the dateFormat
	 */
	public SimpleDateFormat getDateFormat() {
		if (dateFormat == null)
			dateFormat = new SimpleDateFormat(metaData.getDateFormat());
		return dateFormat;
	}

	/**
	 * @param dateFormat
	 *            the dateFormat to set
	 */
	public void setDateFormat(SimpleDateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	public int getCategoryCount(int categoryValue) {
		return occurrenceCounts[categoryValue];
	}

	public int getNumberOfUnknownValues() {
		return nUnknowns;
	}

	public void setMetaData(DimensionMetaData dimensionMetaData) {
		metaData = dimensionMetaData;
		datatype = metaData.getType();
	}

	public DimensionMetaData getMetaData() {
		return metaData;
	}

}
