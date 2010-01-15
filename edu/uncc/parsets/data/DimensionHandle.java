package edu.uncc.parsets.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.uncc.parsets.data.LocalDB.DBAccess;
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

public class DimensionHandle implements Iterable<CategoryHandle> {

	protected String name; //Printable name
	protected String handle; //Database attribute name
	protected DataType dataType;

	protected List<CategoryHandle> categories;
	protected DataSet dataSet;
	protected int num;
	
	public DimensionHandle(String name, String handle, DataType dataType, int dimNum, DataSet dataSet) {
		this.name = name;
		this.handle = handle;
		this.dataType = dataType;
		num = dimNum;
		this.dataSet = dataSet;
	}
	
	public String getName() {
		return name;
	}
	
	protected String getHandle() {
		return handle;
	}

	public DataType getDataType() {
		return dataType;
	}

	protected int getNum() {
		return num;
	}

	public Iterator<CategoryHandle> iterator() {
		if (categories == null)
			loadCategories();
		return categories.iterator();
	}

	private void loadCategories() {
		categories = new ArrayList<CategoryHandle>();
		try {
			Statement stmt = ((LocalDBDataSet)dataSet).getDB().createStatement(DBAccess.FORREADING);
			ResultSet rs = stmt.executeQuery("select name, handle, number, count from Admin_Categories where dataSet='"+dataSet.getHandle()+"' and dimension='"+handle+"';");
			while (rs.next())
				categories.add(new CategoryHandle(rs.getString(1), rs.getString(2), rs.getInt(3), this, rs.getInt(4)));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			((LocalDBDataSet)dataSet).getDB().releaseReadLock();
		}
	}

	/**
	 * Finds the category corresponding to a given number in this dimension. If not found, returns null.
	 * 
	 * @param num The number of the category, from the database
	 * @return The category handle or null
	 */
	protected CategoryHandle num2Handle(int num) {
		if (categories == null)
			loadCategories();
		for (CategoryHandle h : categories)
			if (h.getCategoryNum() == num)
				return h;
		return null;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	public DataSet getDataSet() {
		return dataSet;
	}

	public List<CategoryHandle> getCategories() {
		
		if (categories == null)
			loadCategories();
		
		if (dataType == DataType.categorical) 
			return categories;
		
		else {
			PSLogging.logger.error("Trying to get categories for numerical dimension" + getName());
			return null;
		}
			
	}
	
}
