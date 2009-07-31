package edu.uncc.parsets.data.old;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.uncc.parsets.data.DataType;
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

public class DataDimension {

	// * The key (internal name) for this dimension
	private String dimensionKey;

	private DataType type;

	private String name;

	private Map<String, String> categoryNames = new TreeMap<String, String>();

	// * The map of category keys to integers, for faster lookup when parsing
	private Map<String, Integer> categoryMap = new TreeMap<String, Integer>();

	private ArrayList<String> categoryKeys = new ArrayList<String>();

	private Map<String, Integer> occurrenceCounts = new TreeMap<String, Integer>();

	private List<String> values = new ArrayList<String>(100);

	private boolean hasMetaData = false;

	private String dbHandle;
	
	public DataDimension(String key, DataType dataType) {
		dimensionKey = key;
		type = dataType;
	}

	public void addCategory(String key, String name) {
		categoryNames.put(key, name);
		categoryKeys.add(key);
		categoryMap.put(key, categoryKeys.size());
	}
	
	public void addValue(String value) {
		if (values.size() < 100)
			values.add(value);
		Integer num = occurrenceCounts.get(value);
		if (num == null)
			num = Integer.valueOf(1);
		else
			num = Integer.valueOf(num+1);
		occurrenceCounts.put(value, num);
		switch(type) {
		case numerical:
			try {
				Float.parseFloat(value);
			} catch (Exception e) {
				type = DataType.categorical;
			}
			break;
		case categorical:
			if (occurrenceCounts.size() > 100)
				type = DataType.textual;
			break;
		}
		if (type != DataType.textual)
			if (!hasMetaData)
				if (!categoryMap.containsKey(value))
					addCategory(value, value);
	}

	public int getOccurrenceCount(String key) {
		Integer count = occurrenceCounts.get(key);
		if (count != null)
			return count;
		else {
			PSLogging.logger.warn("No occurrance count found for '"+key+"'");
			return 0;
		}
	}
	
	public List<String> getValues() {
		return values;
	}
	
	public String getName() {
		if (name != null)
			return name;
		else
			return dimensionKey;
	}

	public String getKey() {
		return dimensionKey;
	}

	public String toString() {
		return dimensionKey;
	}

	public String getCategoryName(int categoryValue) {
		if (type != DataType.textual)
			return categoryNames.get(categoryKeys.get(categoryValue));
		else {
			for (Map.Entry<String, Integer> e : categoryMap.entrySet()) {
				if (e.getValue() == categoryValue)
					return e.getKey();
			}
			return null;
		}
	}

	public void setCategoryName(int categoryValue, String name) {
		categoryNames.put(categoryKeys.get(categoryValue), name);
	}

	public String getCategoryKey(int categoryValue) {
		return categoryKeys.get(categoryValue);
	}

	public int getNumCategories() {
		return categoryKeys.size();
	}

	public DataType getDataType() {
		return type;
	}

	public void setDataType(DataType type) {
		this.type = type;
	}
	
	public void setName(String newName) {
		name = newName;
		hasMetaData = true;
	}
	
	public int getNumForKey(String key) {
		// TODO: Debug code that needs to go before release
		if (categoryMap.containsKey(key))
			return categoryMap.get(key);
		else {
			System.err.println("Can't find "+key+" in dimension "+getName());
			return 0;
		}
	}

	public void setHandle(String dimHandle) {
		dbHandle = dimHandle;
	}
	
	public String getHandle() {
		return dbHandle;
	}
}
