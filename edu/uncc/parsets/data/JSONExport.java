package edu.uncc.parsets.data;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import org.json.simple.JSONValue;

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
 * Export {@link DataSet}s to a streaming-friendly JSON format.
 */
public class JSONExport {

	/**
	 * The version of the JSON data produced by this class. The idea is to
	 * treat 1000 as 1.000, 1100 as 1.1, etc. Minor revisions that don't break
	 * compatibility in the reader increment the last two digits. A revision
	 * that requires an update of the reader adds 100. A change in the first
	 * digit would mean a complete revamp of the data model.
	 */
	public static final int JSONVERSION = 1000;

	public static final int JSONMAXCOMPATIBLEVERSION = 1099;
	
	public static final String METAKEY = "%meta";

	public static final String DATASETSKEY = "datasets";
	
	public static String exportDataSet(LocalDBDataSet ds, String fileName) {
		Map<String, Object> dataset = new LinkedHashMap<String, Object>();
		Map<String, Object> meta = new LinkedHashMap<String, Object>();
		meta.put("version", JSONExport.JSONVERSION);
		meta.put("type", "cube");
		List<Map<String, Object>> tables = new Vector<Map<String, Object>>();
		meta.put("tables", tables);
		List<Map<String, Object>> indices = new Vector<Map<String, Object>>();
		meta.put("indices", indices);
		dataset.put(METAKEY, meta);
		Map<String, List<List<Object>>> data = new LinkedHashMap<String, List<List<Object>>>();
		dataset.put("data", data);
		try {
			exportTable(data, ds.getDB(), "Admin_Datasets", "handle", ds.getHandle());
			exportTable(data, ds.getDB(), "Admin_Dimensions", "dataset", ds.getHandle());
			exportTable(data, ds.getDB(), "Admin_Categories", "dataset", ds.getHandle());
			PreparedStatement stmt = ds.getDB().prepareStatement("select name, sql, type from sqlite_master where name like \""+ds.getHandle()+"%\";", DBAccess.FORREADING);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				if (rs.getString("type").equals("table")) {
					tables.add(create2JSON(rs.getString("sql")));
					String tableName = rs.getString("name");
					exportTable(data, ds.getDB(), tableName, null, null);
				} else if (rs.getString("sql") != null) {
					indices.add(index2JSON(rs.getString("sql")));
				}
			}
			return writeGZIPFile(fileName, dataset);
		} catch (Exception e) {
			PSLogging.logger.error("Could not export dataset as JSON file.", e);
		} finally {
			ds.getDB().releaseReadLock();
		}
		return null;
	}

	private static void exportTable(Map<String, List<List<Object>>> container, LocalDB db, String tableName, String key, String value) {
		List<List<Object>> data = new ArrayList<List<Object>>();
		try {
			String sql = "select * from "+tableName;
			if (key != null)
				sql += " where "+key+"='"+value+"';";
			else
				sql += ";";
			PreparedStatement stmt = db.prepareStatement(sql, DBAccess.FORREADING);
			ResultSet rs = stmt.executeQuery();
			boolean firstRow = true;
			int numColumns = 0;
			while (rs.next()) {
				if (firstRow) {
					numColumns = rs.getMetaData().getColumnCount();
					List<Object> row = new ArrayList<Object>(numColumns);
					for (int column = 1; column <= numColumns; column++) {
						switch (rs.getMetaData().getColumnType(column)) {
						case Types.INTEGER:
							row.add("INTEGER");
							break;
						case Types.FLOAT:
							row.add("REAL");
							break;
						case Types.NULL: // null can only be an empty text field
						case Types.VARCHAR:
							row.add("TEXT");
							break;
						default:
							PSLogging.logger.error("Encountered unknown column type: "+rs.getMetaData().getColumnType(column)+" in table "+tableName);
						}
					}
					data.add(row);
					firstRow = false;
				}
				List<Object> row = new ArrayList<Object>(numColumns);
				for (int column = 1; column <= numColumns; column++) {
					switch (rs.getMetaData().getColumnType(column)) {
					case Types.INTEGER:
						row.add(rs.getLong(column));
						break;
					case Types.FLOAT:
						row.add(rs.getFloat(column));
						break;
					case Types.VARCHAR:
						row.add(rs.getString(column));
						break;
					case Types.NULL:
						row.add(null);
						break;
					}
				}
				data.add(row);
			}
			container.put(tableName, data);
		} catch (SQLException e) {
			PSLogging.logger.error("Could not query table "+tableName+".", e);
		} finally {
			db.releaseReadLock();
		}
	}	
	
	private static Map<String, Object> create2JSON(String sql) {
		Map<String, Object> json = new LinkedHashMap<String, Object>();
		// CREATE TABLE Admin_Dimensions (dataSet TEXT, name TEXT, handle TEXT, type TEXT, leftShift INTEGER, bitMask INTEGER)
		// CREATE TABLE householdsal_measures (key INTEGER, numpeople REAL, numvehicles REAL, costelectricity REAL, costgas REAL, costwater REAL, costoil REAL, rent REAL, mortgage REAL, mortgage2 REAL, rentaspercentage REAL, employment REAL, experience REAL, totalincome REAL)
		String firstSplit[] = sql.split("\\(");
		// extract table name from "CREATE TABLE <tablename>"
		String create[] = firstSplit[0].split(" ");
		json.put("tableName", create[2]);
		// fields are "<name>", "<type>," pairs, last one ends in ")" instead of comma
		String columnNames[] = firstSplit[1].split(" ");
		List<List<String>> columns = new ArrayList<List<String>>(columnNames.length/2);
		for (int i = 0; i < columnNames.length; i += 2) {
			List<String> pair = new ArrayList<String>(2);
			pair.add(columnNames[i]);
			pair.add(columnNames[i+1].substring(0, columnNames[i+1].length()-1));
			columns.add(pair);
		}
		json.put("columns", columns);
		return json;
	}
	
	private static Map<String, Object> index2JSON(String sql) {
		Map<String, Object> json = new LinkedHashMap<String, Object>();
		String tokens[] = sql.split(" ");
		// CREATE INDEX Admin_Dimensions_Handle on Admin_Dimensions (dataSet)
		// CREATE INDEX Admin_Categories_DSHandle on Admin_Categories (dataSet, dimension)
		// skip "CREATE" and "INDEX"
		json.put("indexName", tokens[2]);
		// skip "on"
		json.put("tableName", tokens[4]);
		// now remove parentheses and commas from rest
		List<String> columns = new ArrayList<String>(tokens.length-5);
		for (int i = 5; i < tokens.length; i++) {
			String column = tokens[i].substring(0, tokens[i].length()-1);
			if (i == 5)
				column = column.substring(1);
			columns.add(column);
		}
		json.put("columns", columns);		
		return json;
	}
	
	public static String exportDBIndex(LocalDB db, String fileName) {
		Map<String, Object> index = new LinkedHashMap<String, Object>();
		Map<String, Object> meta = new LinkedHashMap<String, Object>();
		meta.put("type", "index");
		meta.put("version", JSONExport.JSONVERSION);
		index.put(METAKEY, meta);
		Map<String, Map<String, Object>> dsList = new LinkedHashMap<String, Map<String,Object>>();
		for (DataSet ds : db.getDataSets()) {
			Map<String, Object> dataSet = new LinkedHashMap<String, Object>();
			dataSet.put("name", ds.getName());
			dataSet.put("section", ds.getSection());
			dataSet.put("source", ((LocalDBDataSet)ds).getSource());
			dataSet.put("srcURL", ((LocalDBDataSet)ds).getSrcURL());
			dataSet.put("items", ds.getNumRecords());
			dataSet.put("categorical", ds.getNumCategoricalDimensions());
			dataSet.put("numerical", ds.getNumNumericDimensions());
			dataSet.put("url", ds.getURL());
			dsList.put(ds.getHandle(), dataSet);
		}
		index.put(DATASETSKEY, dsList);
		return writeGZIPFile(fileName, index);
	}
	
	private static String writeGZIPFile(String fileName, Map<String, Object> dataset) {
		if (!fileName.endsWith(".json.gz"))
			fileName += ".json.gz";
		try {
			// found this trick on http://weblogs.java.net/blog/mister__m/archive/2003/12/achieving_bette.html
			GZIPOutputStream outStream = new GZIPOutputStream(new FileOutputStream(fileName)) {
				{
					def.setLevel(Deflater.BEST_COMPRESSION);
				}
			};
			OutputStreamWriter outWriter = new OutputStreamWriter(outStream);
			JSONValue.writeJSONString(dataset, outWriter);
			outWriter.flush();
			outWriter.close();
			return fileName;
		} catch (Exception e) {
			PSLogging.logger.error("Error writing to gzipped file "+fileName+".", e);
		}
		return null;
	}
	
}
