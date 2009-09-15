package edu.uncc.parsets.data;

import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

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
 * A data source that can produce a {@link CategoryTree}.
 */
public abstract class DataSet implements Iterable<DimensionHandle>, Comparable<DataSet> {

	public abstract Iterator<DimensionHandle> iterator();

	public abstract CategoryTree getTree(List<DimensionHandle> dimensions);
	
	protected String name;
	
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the number of records in this data set. 0 means number unknown
	 * or not applicable.
	 * 
	 * @return number of records or 0
	 */
	public abstract int getNumRecords();
	
	/**
	 * Return the database handle for this data set. This function calls {@link #getName()}
	 * unless overridden. Only use for talking directly to a database.
	 * 
	 * @return the database handle of this data set
	 */
	protected String getHandle() {
		return getName();
	}

	public abstract String getURL();
	
	public abstract int getNumDimensions();
	
	public abstract int getNumCategoricalDimensions();
	
	public abstract int getNumNumericDimensions();
	
	public abstract DimensionHandle[] getNumericDimensions();
	
	public DefaultMutableTreeNode getCategoricalDimensionsAsTree() {
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(name);
		
		for (DimensionHandle d : this)
			if (d.getDataType() == DataType.categorical) {
				
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(d);
				
				for (CategoryHandle cat : d.getCategories())
					node.add(new DefaultMutableTreeNode(cat));
				
				root.add(node);
				
			}
		
		return root;
	}

	public abstract String getSection();
	
	public int compareTo(DataSet o) {
		return name.compareToIgnoreCase(o.name);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof DataSet)
			return compareTo((DataSet)o) == 0;
		else
			return false;
	}

	@Override
	public int hashCode() {
		assert false : "hashCode not designed";
		return 42; // any arbitrary constant will do
	}
}
