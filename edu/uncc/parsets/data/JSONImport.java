package edu.uncc.parsets.data;
import java.io.IOException;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.data.LocalDB.DBAccess;

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
 * Import of data set from JSON format created by {@link JSONExport}.
 */
public class JSONImport {

	private static final class TableIndexInfo {
		String name;
		String tableName;
		List<String> columnDefs = new ArrayList<String>();

		public TableIndexInfo(String n) {
			name = n;
		}
	}
	
	private static final class InsertMetaData {
		PreparedStatement stmt;
		int types[];
		boolean rollback = false;
		public InsertMetaData(int numColumns) {
			types = new int[numColumns];
		}
	}

	private static final class JSONStreamer implements ContentHandler {

		private static enum Mode {start, version, type, tables, indices, data}
		
		private static enum SubMode {none, tableName, indexName, columns, headerColumns, dataColumns}
		
		Mode mode = Mode.start;
		
		SubMode subMode = SubMode.none;
		
		private int version;

		private String type;

		private TableIndexInfo tableInfo;

		private Statement dbStmt;

		private String tableName;
		
		private InsertMetaData insertMeta;
		
		List<Object> columns = new ArrayList<Object>();

		private LocalDB localDB;

		boolean rollback = false;
		
		public JSONStreamer(LocalDB db, Statement stmt) {
			dbStmt = stmt;
			localDB = db;
		}

		@Override
		public void startJSON() throws ParseException, IOException {
		}

		@Override
		public void endJSON() throws ParseException, IOException {
		}

		@Override
		public boolean startArray() throws ParseException, IOException {
			return true;
		}

		@Override
		public boolean endArray() throws ParseException, IOException {
			if (mode == Mode.data && columns.size() > 0) {
				if (subMode == SubMode.headerColumns) {
					insertMeta = JSONImport.prepareInsert(localDB, tableName, columns);
					rollback |= insertMeta.rollback;
					columns.clear();
					subMode = SubMode.dataColumns;
				} else {
					JSONImport.addRow(insertMeta, columns);
					rollback |= insertMeta.rollback;
					columns.clear();
				}
			}
			return true;
		}

		@Override
		public boolean startObject() throws ParseException, IOException {
			return true;
		}

		@Override
		public boolean startObjectEntry(String s) throws ParseException, IOException {
			if (mode == Mode.data) {
				tableName = s;
				subMode = SubMode.headerColumns;
				columns.clear();
				tableInfo = null;
			} else {
				for (Mode m : Mode.values())
					if (s.equals(m.name()))
						mode = m;
	
				subMode = SubMode.none;
				for (SubMode m : SubMode.values())
					if (s.equals(m.name()))
						subMode = m;
			}
			return true;
		}

		@Override
		public boolean endObjectEntry() throws ParseException, IOException {
			if (insertMeta != null) {
				JSONImport.writeData(localDB, insertMeta);
				rollback |= insertMeta.rollback;
				insertMeta = null;
			}
			return true;
		}

		@Override
		public boolean endObject() throws ParseException, IOException {
			if (tableInfo != null) {
				if (mode == Mode.tables)
					JSONImport.createTable(dbStmt, tableInfo);
				else if (mode == Mode.indices)
					JSONImport.createIndex(dbStmt, tableInfo);
				tableInfo = null;
			}
			return true;
		}

		@Override
		public boolean primitive(Object o) throws ParseException, IOException {
			switch(mode) {
			case tables:
				switch(subMode) {
				case tableName:
					tableInfo = new TableIndexInfo((String)o);
					break;
				case columns:
					tableInfo.columnDefs.add((String)o);
					break;
				}
				break;
			case indices:
				switch(subMode) {
				case indexName:
					tableInfo = new TableIndexInfo((String) o);
					break;
				case tableName:
					tableInfo.tableName = (String) o;
					break;
				case columns:
					tableInfo.columnDefs.add((String)o);
					break;
				}
				break;
			case version:
				version = ((Long) o).intValue();
				break;
			case type:
				type = (String) o;
				if (!type.equals("cube") || version > JSONExport.JSONMAXCOMPATIBLEVERSION)
					ParallelSets.logger.error("Cannot import type "+type+", version "+version);
				break;
			case data:
				columns.add(o);
			}

			return true;
		}		
	}

	
	public static void importDataSet(LocalDB db, Reader fileReader) {
		Statement stmt = null;
		try {
			stmt = db.createStatement(DBAccess.FORWRITING);
			JSONParser parser = new JSONParser();
			stmt.executeUpdate("BEGIN TRANSACTION");
			JSONStreamer streamer = new JSONStreamer(db, stmt);
			parser.parse(fileReader, streamer);
			if (streamer.rollback)
				stmt.executeUpdate("ROLLBACK");
			else
				stmt.executeUpdate("COMMIT");
		} catch (Exception e) {
			ParallelSets.logger.error("Error importing dataset", e);
			try {
				stmt.executeUpdate("ROLLBACK");
			} catch (SQLException e1) {
				ParallelSets.logger.error("Error rolling back changes after error", e1);
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// ...
				}
			db.releaseWriteLock();
		}
	}
	
	private static InsertMetaData prepareInsert(LocalDB db, String tableName, List<Object> columns) {
		StringBuilder sql = new StringBuilder("INSERT INTO "+tableName+" VALUES (");
		InsertMetaData meta = new InsertMetaData(columns.size());
		int columnNum = 0;
		for (Iterator<Object> c = columns.iterator(); c.hasNext(); ) {
			String type = (String) c.next();
			if (type.equals("INTEGER"))
				meta.types[columnNum] = Types.INTEGER;
			else if (type.equals("REAL"))
				meta.types[columnNum] = Types.FLOAT;
			else if (type.equals("TEXT"))
				meta.types[columnNum] = Types.VARCHAR;
			else
				ParallelSets.logger.error("Unknown column type "+type);
			columnNum++;

			if (c.hasNext())
				sql.append("?, ");
			else
				sql.append("?);");
		}
		try {
			meta.stmt = db.prepareStatement(sql.toString(), DBAccess.FORWRITING);
		} catch (SQLException e) {
			ParallelSets.logger.error("Error creating prepared statement for table "+tableName+": "+sql, e);
			meta.rollback = true;
		}
		return meta;
	}
	
	private static void addRow(InsertMetaData meta, List<Object> data) {
		int columnNum = 1;
		try {
			for (Object o : data) {
				switch(meta.types[columnNum-1]) {
				case Types.INTEGER:
					meta.stmt.setLong(columnNum, (Long) o);
					break;
				case Types.REAL:
					meta.stmt.setDouble(columnNum, (Double) o);
					break;
				case Types.VARCHAR:
					meta.stmt.setString(columnNum, (String) o);
				}
				columnNum++;
			}
			meta.stmt.addBatch();
		} catch (SQLException e) {
			ParallelSets.logger.error("Error adding data to prepared statement", e);
			meta.rollback = true;
		}
	}
	
	public static void writeData(LocalDB db, InsertMetaData meta) {
		try {
			meta.stmt.executeBatch();
		} catch (SQLException e) {
			ParallelSets.logger.error("Error writing data", e);
			meta.rollback = true;
		} finally {
			try {
				meta.stmt.close();
			} catch (SQLException e) {
				ParallelSets.logger.error("Error releasing statement", e);
			}
			db.releaseWriteLock();
		}
	}

	private static void createIndex(Statement stmt, TableIndexInfo info) {
		StringBuilder sql = new StringBuilder("CREATE INDEX "+info.name+" on "+info.tableName+" (");
		for (Iterator<String> columnIt = info.columnDefs.iterator(); columnIt.hasNext(); ) {
			sql.append(columnIt.next());
			if (columnIt.hasNext())
				sql.append(", ");
		}
		sql.append(");");
		try {
			stmt.executeUpdate(sql.toString());
		} catch (SQLException e) {
			ParallelSets.logger.error("Failed to create index "+info.name+" on "+info.tableName, e);
		}
	}

	private static void createTable(Statement stmt, TableIndexInfo info) {
		StringBuilder sql = new StringBuilder("CREATE TABLE "+info.name+" (");
		for (Iterator<String> columnIt = info.columnDefs.iterator(); columnIt.hasNext(); ) {
			sql.append(columnIt.next()); // field name
			sql.append(' ');
			sql.append(columnIt.next()); // field type
			if (columnIt.hasNext())
				sql.append(", ");
		}
		sql.append(");");
		try {
			stmt.executeUpdate(sql.toString());
		} catch (SQLException e) {
			ParallelSets.logger.error("Failed to create table "+info.name, e);
		}
	}
		
}
