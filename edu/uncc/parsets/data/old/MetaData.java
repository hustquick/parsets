
package edu.uncc.parsets.data.old;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.uncc.parsets.data.DataType;

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
 * MetaData stores the meta information about a dataset as read from the XML
 * file. The role of MetaData is mostly to provide that information to the data
 * parser, but also to hierarchically organize the data dimensions and
 * categories.
 * 
 * Legacy class, will be folded into CSVDataSet/DataDimension soon.
 */
public class MetaData {

	/**
	 * DimensionMetaData contains the information for one data dimension.
	 */	
	public static class DimensionMetaData {

		protected DataType type;

		protected String fullname;

		protected String description;

		protected Map<String, String> categoryNames = new TreeMap<String, String>();

		protected List<String> categoryKeys = new ArrayList<String>();
		
		protected String dateFormat;
		
		public DimensionMetaData(DataType dataType, String dateFormatStr) {
			type = dataType;
			dateFormat = dateFormatStr;
		}
		
		public void setName(String newName) {
			fullname = newName;
		}
		
		public void setDescription(String newDescription) {
			description = newDescription;
		}
		
		public DataType getType() {
			return type;
		}

		public String getName() {
			return fullname;
		}

		public int getNumCategories() {
			return categoryNames.size();
		}
		
		//* Looks up the long category name given the category key
		public String getCategoryLabel(String catKey) {
			
			// If the metadata has no long name for this key, just 
			//	treat the category key as a label.
			
			if (categoryNames.get(catKey) == null) {
				return catKey;
			}
			
			return categoryNames.get(catKey);
		}
		
		//* Looks up the category key given the long category name
		public String getCategoryKey(String catName) {
			
			if (categoryNames.size() == 0) {
				return catName;
			}
			
			for (Map.Entry<String, String> e : categoryNames.entrySet()) {
				if (e.getValue().equals(catName))
					return e.getKey();
				
			}
			return "";
		}
		
		public String getDateFormat() {
			return dateFormat;
		}

		public void addCategory(String catKey, String catName) {
			categoryNames.put(catKey, catName);
			categoryKeys.add(catKey);
		}
		
		public int getCategoryIndex(String catKey) {
			return categoryKeys.indexOf(catKey);
		}
	}

	//* Map data dimension keys to DimensionMetaData
	private Map<String, DimensionMetaData> dimensionmeta = new TreeMap<String, DimensionMetaData>();

	//* The name of the dataset, or the filename if nothing specified in the XML file.
	String name;

	private String section = "Misc";
	
	private String url;

	private String source = "";
	
	private String srcURL = "";
	
	/**
	 * Returns the datatype for the dimension key (i.e., the name of the
	 * dimension as it appears in the first line of the CSV file).
	 * 
	 * @param key
	 *            The name of the dimension from the data file
	 * @return the type of the dimension, or categorical if none was specified
	 */

	public DataType getDataTypeForKey(String key) {
		DimensionMetaData dmd = dimensionmeta.get(key);
		if (dmd != null)
			return dmd.getType();
		else
			return DataType.categorical;
	}

	/**
	 * Looks up the full name of a dimension by its key (the name of the
	 * dimension as it appears in the first line of the CSV file).
	 * 
	 * @param key
	 *            The name for the dimension from the data file
	 * @return the full name of the dimension, or the key if no full name was
	 *         found
	 */
	public String getFullName(String key) {
		DimensionMetaData dmd = dimensionmeta.get(key);
		if (dmd != null)
			return dmd.getName();
		else
			return key;
	}

	public int getNumberOfCategories(String dimKey) {
		return dimensionmeta.get(dimKey).getNumCategories();
	}
	
	public DimensionMetaData getDimensionMetaData(String dimName) {
		return dimensionmeta.get(dimName);
	}

	public void addDimension(String dimKey, DimensionMetaData newDimension) {
		dimensionmeta.put(dimKey, newDimension);
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

	public void setSource(String newSource) {
		source = newSource;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSrcURL(String newURL) {
		srcURL = newURL;
	}
	
	public String getSrcURL() {
		return srcURL;
	}
}
