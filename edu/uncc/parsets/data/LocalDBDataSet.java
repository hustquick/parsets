package edu.uncc.parsets.data;

import edu.uncc.parsets.parsets.SelectionChangeEvent;
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

/**
 * A dataset that is backed by a SQLite database.
 */
public class LocalDBDataSet extends DataSet {

	/**
	 * Whether to write a timestamp to the DB whenever a dataset is opened.
	 * This is not very useful for the Java program, but will be important
	 * on the iPhone.
	 */
	private static final boolean RECORDOPENTIMES = false;
	
	private LocalDB db;
	
	private String dbHandle;
	
	private ArrayList<DimensionHandle> dimHandles;
	
	private int numRecords;

	private String url = "";

	private String section = "Misc";

	private String source = "";
	
	private String srcURL = "";
	
	protected LocalDBDataSet(String dsName, String handle, int count, String remoteURL, String listSection, String dbSource, String dbSrcURL, LocalDB dbHandler) {
		name = dsName;
		dbHandle = handle;
		db = dbHandler;
		numRecords = count;
		if (remoteURL != null)
			url = remoteURL;
		if (listSection != null)
			section = listSection;
		if (dbSource != null)
			source = dbSource;
		if (dbSrcURL != null)
			srcURL = dbSrcURL;
	}
	
	public LocalDB getDB() {
		return db;
	}
	
	@Override
	public int getNumRecords() {
		return numRecords;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public String getURL() {
		return url;
	}
	
	public String getSection() {
		return section;
	}
		
	public String getSource() {
		return source;
	}
	
	public String getSrcURL() {
		return srcURL;
	}
	
	@Override
	public CategoryTree getTree(List<DimensionHandle> dimensions) {
		CategoryTree tree = new CategoryTree(dimensions.size()+1);
		String dimList = dims2String(dimensions);
		StringBuffer query = new StringBuffer("select ");
		query.append(dimList);
		query.append(", sum(count) from "+dbHandle+"_dims group by ");
		query.append(dimList);
		query.append(";");

		try {
			Statement stmt = db.createStatement(DBAccess.FORREADING);
			ResultSet rs = stmt.executeQuery(query.toString());
			CategoryNode thisLine[] = new CategoryNode[dimensions.size()+1];
			CategoryNode previousLine[] = new CategoryNode[dimensions.size()+1];
			CategoryNode root = new CategoryNode(null, null, 0);
			tree.addtoLevel(0, root);
			// this would be much prettier as a recursive function, but the
			// semantics and side effects of the ResultSet make that very
			// annoying
			while (rs.next()) {
				int column = 1;
				CategoryNode previousNode = root;
				for (DimensionHandle dim : dimensions) {
					CategoryHandle cat = dim.num2Handle(rs.getInt(column));
					CategoryNode node = null;
					if (previousLine[column] != null && cat == previousLine[column].getToCategory()) {
						node = previousLine[column];
					} else {
						if (column == dimensions.size())
							node = new CategoryNode(previousNode, cat, rs.getInt(column+1));
						else {
							node = new CategoryNode(previousNode, cat, 0);
							for (int i = column+1; i <= dimensions.size(); i++)
								previousLine[i] = null;
						}
						tree.addtoLevel(column, node);
					}
					previousNode = node;
					thisLine[column] = node;
					
					column++;
				}
				CategoryNode temp[] = thisLine;
				thisLine = previousLine;
				previousLine = temp;
			}
			rs.close();			
		} catch (SQLException e) {
			PSLogging.logger.error("SQL error while creating tree.", e);
		} finally {
			db.releaseReadLock();
		}
		tree.getRootNode().updateValues();
		return tree;
	}

	/**
	 * Makes a string from a list of dimensions, with commas in between. Returns
	 * an empty string for an empty list.
	 * 
	 * @param dimensions List of dimension handles to put in the string
	 * @return SQL-ready list of dimension handles
	 */
	public static String dims2String(List<DimensionHandle> dimensions) {
		StringBuffer dimList = new StringBuffer();
		boolean first = true;
		for (DimensionHandle handle : dimensions) {
			if (first)
				first = false;
			else
				dimList.append(", ");
			dimList.append(handle.getHandle());
		}
		return dimList.toString();
	}

	@Override
	public Iterator<DimensionHandle> iterator() {
		if (dimHandles == null)
			loadDimensions();
		return dimHandles.iterator();
	}
	
	private void loadDimensions() {
		try {
			Statement stmt = db.createStatement(DBAccess.FORREADING);
			dimHandles = new ArrayList<DimensionHandle>();
			ResultSet rs = stmt.executeQuery("select name, handle, type from Admin_Dimensions where dataSet = '"+dbHandle+"';");
			int dimNum = 0;
			while (rs.next()) {
				dimHandles.add(new DimensionHandle(rs.getString(1), rs.getString(2), DataType.typeFromString(rs.getString(3)), dimNum, this));
				dimNum++;
			}
			rs.close();
			if (RECORDOPENTIMES)
				stmt.executeUpdate("update Admin_DataSets set lastOpened=datetime('now') where handle='"+dbHandle+"';");
		} catch (SQLException e) {
			PSLogging.logger.error("SQL error while loading dimensions.", e);
		} finally {
			db.releaseReadLock();
		}
	}
	
	public void deleteFromDB() {
		db.deleteFromDB(dbHandle);
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	protected String getHandle() {
		return dbHandle;
	}

	public int getNumDimensions() {
		if (dimHandles == null)
			loadDimensions();

		return dimHandles.size();
	}
	
	public int getNumCategoricalDimensions() {
		if (dimHandles == null)
			loadDimensions();
		int num = 0;
		for (DimensionHandle d : dimHandles)
			if (d.getDataType() == DataType.categorical) 
				num++;
		return num;
	}
	
	public int getNumNumericDimensions() {
		return getNumDimensions()-getNumCategoricalDimensions();
	}

	public DimensionHandle[] getNumericDimensions() {
		DimensionHandle handles[] = new DimensionHandle[getNumNumericDimensions()];
		int i = 0;
		for (DimensionHandle d : dimHandles)
			if (d.getDataType() != DataType.categorical)
				handles[i++] = d;
		
		return handles;
	}

    public void selectionChanged(SelectionChangeEvent event) {
        
    }
}
