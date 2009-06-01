package edu.uncc.parsets.data.old;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
 * Data set class that stores tabular data for conversion into the data cube.
 * This is a legacy class that will be rewritten soon. 
 */
public class CSVDataSet implements Iterable<DataDimension> {

	// * List of dimensions, each dimension listed only once.
	protected List<DataDimension> dimensions = new Vector<DataDimension>();

	// * Map the dimension key and/or name to the dimension. Dimensions can be
	// included more than once under different names here.
	protected Map<String, DataDimension> dimensionsmap = new HashMap<String, DataDimension>();

	protected String filename;
	
	MetaData metaData;

	public CSVDataSet(String filename) {
		this.filename = filename;
	}

	public DataDimension addDimension(String key, int estimatedNumRecords) {
		DataDimension dim = new DataDimension(key, estimatedNumRecords);
		dimensions.add(dim);
		dimensionsmap.put(key, dim);
		return dim;
	}

	public DataDimension getDimension(String name) {
		
		if (dimensionsmap.get(name) == null) {
			
			// try to find the name as a label
			
			for (DataDimension dim : dimensions) {
				
				if (dim.getName().equals(name)) {
					
					return dim;
					
				}
				
			}
			
			return null;
			
		}
		
		return dimensionsmap.get(name);
	}

	public DataDimension getDimension(int index) {
		return dimensions.get(index);
	}

	/**
	 * find the index for a given dimension
	 */
	public int getDimensionIndex(DataDimension dim) {
		int index = -1;
		for (int i = 0; i < dimensions.size(); i++)
			if (dimensions.get(i) == dim)
				index = i;
		return index;
	}

	/**
	 * find the index for a given dimension name
	 */
	public int getDimensionIndex(String dimName) {
		return getDimensionIndex(getDimension(dimName));
	}
	
	public int getNumDimensions() {
		return dimensionsmap.size();
	}

	public Iterator<DataDimension> iterator() {
		return dimensions.iterator();
	}

	public String getName() {
		if (metaData != null)
			return metaData.getName();
		else
			return filename;
	}
	
	public void setName(String newName) {
		if (metaData != null)
			metaData.setName(newName);
	}
	
	public String getFilename() {
		return filename;
	}

	public String getFileBaseName() {
		return (new File(filename)).getName();
	}
	
	public int getNumRecords(int i) {
		int numRecords = dimensions.get(i).getNumRecords();
		return numRecords;
	}

	/**
	 * Export dataset as ARFF file. Only categorical dimensions are exported, for easier reimport and Bayesianization.
	 * 
	 * @param exportFilename
	 * @param meta
	 */
	public void exportARFF(String exportFilename) {
		try {
			PrintStream w = new PrintStream(exportFilename);
			// PrintStream w = System.out;
			w.println("% ARFF File created by DataSet.exportARFF() from file " + filename);
			w.println();
			String name = new File(filename).getName();
			if (name.lastIndexOf(".") > 0)
				name = name.substring(0, name.lastIndexOf("."));
			w.println("@Relation " + name);
			w.println();
			int numCategorical = 0;
			boolean isCategorical[] = new boolean[dimensions.size()];
			int catIndex = 0;
			int lastCategorical = 0;
			for (DataDimension dim : dimensions) {
				if (dim.getDataType() == DataType.categorical) {
					w.print("@Attribute " + dim.dimensionKey + " {");
					for (int i = 0; i < dim.getNumCategories(); i++) {
//						String[] catNameParts = meta.getCategoryLabel(dim.dimensionKey, dim.getCategoryName(i)).split(" ");
//						StringBuffer cb = new StringBuffer();
//						for (int j = 0; j < catNameParts.length; j++)
//							cb.append(catNameParts[j]);						
						w.print("\""+dim.getCategoryName(i)+"\"");
						if (i < dim.getNumCategories() - 1)
							w.print(", ");
					}
					w.println("}");
					numCategorical++;
					isCategorical[catIndex] = true;
					lastCategorical = catIndex;
				} else
					isCategorical[catIndex] = false;
				catIndex++;
			}
			w.println();
			w.println("@Data");
			float[][] data = new float[dimensions.size()][];
			for (int i = 0; i < dimensions.size(); i++)
					data[i] = dimensions.get(i).values;
			for (int row = 0; row < data[0].length; row++) {
				for (int column = 0; column < data.length; column++) {
					if (isCategorical[column]) {
						
						if (dimensions.get(column).getCategoryName((int)data[column][row]).length()==0) {
							w.print("?");
						}
						
						else { 
							w.print(dimensions.get(column).getCategoryName((int)data[column][row]));
						}
						if (column < lastCategorical)
							w.print(",");
					}
				}
				w.println();
			}
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
		for (DataDimension d : dimensions) {
			DimensionMetaData dimMeta = metaData.getDimensionMetaData(d.getKey());
			if (dimMeta != null)
				d.setMetaData(dimMeta);
		}
	}

	public MetaData getMetaData() {
		return metaData;
	}

}
