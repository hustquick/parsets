package edu.uncc.parsets.data;

import java.awt.event.ActionListener;
import java.io.File;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.SwingUtilities;

import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.data.old.CSVDataSet;
import edu.uncc.parsets.data.old.DataDimension;
import edu.uncc.parsets.gui.MainWindow;
import edu.uncc.parsets.util.PSLogging;
import edu.uncc.parsets.util.osabstraction.AbstractOS;

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
 * Encapsulate an SQLite database that stores datasets. This class provides
 * the means for thread-safe concurrent read and write access to the database.
 * Any interaction with the database has to go through {@link #createStatement(DBAccess)}
 * or {@link #prepareStatement(String, DBAccess)}, and release the lock acquired
 * this way using {@link #releaseReadLock()} or {@link #releaseWriteLock()}.
 *
 */
public class LocalDB {

	/**
	 * Version of the db schema used. This is recorded in the schema_version
	 * entry in the Admin_Settings table, and will allow us to open old dbs
	 * even when the schema changes.
	 */
	private static final int SCHEMA_VERSION = 1000;
	
	public static final String LOCALDBFILENAME = "local.db";

	private static final LocalDB defaultDB;

	public static final String LAST_VERSION_SEEN_KEY = "last_version_seen";
	
	private final ReentrantReadWriteLock dbLock = new ReentrantReadWriteLock();

	private ArrayList<DataSet> dataSets;

	private Map<String, List<DataSet>> sections;
	
	private Connection db;

	private List<ActionListener> listeners = new Vector<ActionListener>();
	
	static {
		defaultDB = new LocalDB(LOCALDBFILENAME);
	}
	
	public static enum DBAccess {
		FORREADING, FORWRITING
	}
	
	/** 
	 * Simple storage class to hold information for {@link #addLocalDBDataSet}.
	 *
	 */
	private static class CubeValues {
		public int count;
		public int categoryValues[];
		public CubeValues(int values[]) {
			count = 1;
			// Java 6 has Arrays.copyOf() for this purpose ...
			categoryValues = new int[values.length];
			for (int i = 0; i < values.length; i++)
				categoryValues[i] = values[i];
		}
	}
	
	public LocalDB(String filename) {
		try {
			Class.forName("org.sqlite.JDBC");

			String dbFileName = null;

//			PSLogging.logger.fatal("test");
			
			boolean initialized = true;

			if (ParallelSets.isInstalled())
				dbFileName = AbstractOS.getCurrentOS().getLocalDBPath(filename);
			else {
				URL dbURL = this.getClass().getResource("/"+filename);

				if (dbURL == null) {
					dbFileName = filename;
					initialized = false;
				} else
					dbFileName = (new File(dbURL.toURI())).getAbsolutePath();
			}
			
			PSLogging.logger.info("Opening local DB file '"+dbFileName+"'");

	    	db = DriverManager.getConnection("jdbc:sqlite:"+dbFileName);

	    	if (!initialized)
	    		initializeDB();
	    	
		} catch (ClassNotFoundException e) {
			PSLogging.logger.fatal("SQLite could not be initialized.", e);
		} catch (SQLException e) {
			PSLogging.logger.fatal("Error connecting to local DB.", e);
		} catch (URISyntaxException e) {
			PSLogging.logger.fatal("Unable to locate local DB file.", e);
		}
	}
	
	public static LocalDB getDefaultDB() {
		return defaultDB;
	}

	/**
	 * Acquire a Statement for interacting with the database. This method
	 * acquires a lock, which must be released (using {@link #releaseReadLock()}
	 * or {@link #releaseWriteLock())} when the calling function is done.
	 * 
	 * @param accessType the kind of access (reading or writing) the resulting statement will be used for
	 * @return the statement object
	 * @throws SQLException
	 */
	public Statement createStatement(DBAccess accessType) throws SQLException {
		acquireLock(accessType);
		return db.createStatement();
	}

	private void acquireLock(DBAccess accessType) {
		if (accessType == DBAccess.FORREADING)
			dbLock.readLock().lock();
		else
			dbLock.writeLock().lock();
	}
	
	public void releaseReadLock() {
		dbLock.readLock().unlock();
	}
	
	public void releaseWriteLock() {
		dbLock.writeLock().unlock();
	}
	
	public PreparedStatement prepareStatement(String statement, DBAccess accessType) throws SQLException {
		acquireLock(accessType);
		return db.prepareStatement(statement);
	}
	
	public void initializeDB() {
		PSLogging.logger.info("Initializing local DB file.");
		Statement statement;
		try {
			statement = createStatement(DBAccess.FORWRITING);
			ResultSet rs = statement.executeQuery("select name from sqlite_master where type=\"table\";");
			// make list of tables first, as dropping invalidates query result
			List<String> dropTables = new ArrayList<String>();
			while (rs.next()) 
				dropTables.add(rs.getString(1));
			for (String tableName : dropTables)
				statement.executeUpdate("drop table "+tableName+";");
			statement.execute("begin transaction;");
			statement.execute("create table Admin_Settings (key TEXT PRIMARY KEY, value TEXT);");
			statement.execute("insert into Admin_Settings values ('creator', '"+MainWindow.WINDOWTITLE+"');");
			statement.execute("insert into Admin_Settings values ('schema_version', '"+SCHEMA_VERSION+"');");
			statement.execute("insert into Admin_Settings values ('"+LAST_VERSION_SEEN_KEY+"', '"+ParallelSets.VERSION+"');");
			statement.execute("create table Admin_Datasets (name TEXT, handle TEXT PRIMARY KEY, type TEXT, numDimensions INTEGER, numRecords INTEGER, section TEXT, dateAdded TEXT, lastOpened TEXT, dataURL TEXT, source TEXT, srcURL TEXT);");
	        statement.execute("create table Admin_Dimensions (dataSet TEXT, name TEXT, handle TEXT, type TEXT, leftShift INTEGER, bitMask INTEGER);");
	        statement.execute("create table Admin_Categories (dataSet TEXT, dimension TEXT, name TEXT, handle TEXT, number INTEGER, count INTEGER);");
	        statement.execute("create index Admin_Dimensions_Handle on Admin_Dimensions (dataSet);");
	        statement.execute("create index Admin_Categories_DSHandle on Admin_Categories (dataSet, dimension);");
	        statement.execute("commit;");
	        statement.close();
		} catch (SQLException e) {
			PSLogging.logger.error("Error initializing local DB.", e);
		} finally {
			releaseWriteLock();
		}
		rescanDB();
	}

	public LocalDBDataSet[] getDataSets() {
		if (dataSets == null)
			rescanDB();
		LocalDBDataSet ds[] = dataSets.toArray(new LocalDBDataSet[dataSets.size()]);
		Arrays.sort(ds);
		return ds;
	}

	public DataSet getDataSet(String handle) {
		for (DataSet ds : dataSets)
			if (ds.getHandle().equals(handle))
				return ds;
		return null;
	}
	
	public Map<String, List<DataSet>> getSections() {
		if (sections == null) {
			if (dataSets == null)
				rescanDB();
			sections = groupIntoSections(dataSets.iterator());
		}		
		return sections;
	}

	public static Map<String, List<DataSet>> groupIntoSections(Iterator<DataSet> it) {
		TreeMap<String, List<DataSet>> sections = new TreeMap<String, List<DataSet>>();
		while (it.hasNext()) {
			DataSet ds = it.next();
			List<DataSet> list = sections.get(ds.getSection());
			if (list == null) {
				list = new ArrayList<DataSet>();
				sections.put(ds.getSection(), list);
			}
			list.add(ds);
		}
		for (List<DataSet> list : sections.values())
			Collections.sort(list);
		return sections;
	}
	
	/** 
	 * Add a listener to be called whenever {@link #rescanDB()} is run.
	 * @param l The listener to be added
	 */
	public void addRescanListener(ActionListener l) {
		listeners.add(l);
	}
	
	public void removeRescanListener(ActionListener l) {
		listeners.remove(l);
	}
	
	public void rescanDB() {
		dataSets = new ArrayList<DataSet>();
		sections = null;
		try {
			Statement stmt = createStatement(DBAccess.FORREADING);
        	ResultSet rs = stmt.executeQuery("select name, handle, numRecords, dataURL, section, source, srcURL from Admin_DataSets;");
        	while (rs.next()) {
        		LocalDBDataSet ds = new LocalDBDataSet(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), this);
        		dataSets.add(ds);
        	}
        	SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					for (ActionListener l : listeners)
						l.actionPerformed(null);
				}
        	});
		} catch(SQLException e) {
        	PSLogging.logger.error("Error reading list of datasets.", e);
        } finally {
        	releaseReadLock();
        }
	}
	
	public String addLocalDBDataSet(CSVDataSet dataSet) {
		ArrayList<DataDimension> catDims = new ArrayList<DataDimension>(dataSet.getNumDimensions());
		ArrayList<DataDimension> numDims = new ArrayList<DataDimension>(dataSet.getNumDimensions());
		
		int bits = 0;
		BigInteger combinations = BigInteger.ONE;
		for (DataDimension d : dataSet)
			if (d.getDataType() == DataType.categorical) {
				combinations = combinations.multiply(BigInteger.valueOf(d
						.getNumCategories()));
				bits += CategoryKeyDef.numBits(d.getNumCategories() + 1);
				catDims.add(d);
			} else
				numDims.add(d);
		
		PSLogging.logger.info("Storing data set " + dataSet.getName());
		PSLogging.logger.info(dataSet.getNumRecords(0) + " records");
		PSLogging.logger.info(catDims.size() + " (categorical) dimensions");
		PSLogging.logger.info(numDims.size() + " measures");
		PSLogging.logger.info(combinations + " potential combinations ("
				+ combinations.bitLength() + " bits)");
		PSLogging.logger.info("Key length: " + bits + " bits");

		// where is map() when I need it?
		int categories[] = new int[catDims.size()];
		int i = 0;
		for (DataDimension d : catDims) {
			categories[i] = d.getNumCategories();
			i++;
		}
		
		CategoryKeyDef keyDef = new CategoryKeyDef(categories);

		Statement stmt = null;
		String handle = null;
		try {
			stmt = createStatement(DBAccess.FORWRITING);
			stmt.execute("begin transaction;");
			handle = writeAdmin(dataSet, catDims, numDims, keyDef);
			writeData(dataSet.getNumRecords(0), handle, catDims, numDims, keyDef);
			stmt.execute("commit;");
		} catch (SQLException e) {
			PSLogging.logger.error("Could not write dataset to local DB.", e);
			try {
				stmt.execute("rollback;");
			} catch (SQLException e1) {
				PSLogging.logger.error("Could not roll back changes.", e1);
			}
		} finally {
			releaseWriteLock();
			try {
				stmt.close();
			} catch (SQLException e) {
				PSLogging.logger.warn("Could not release DB statement.", e);
			}
		}

		rescanDB();
		
		PSLogging.logger.info("Done.");
		
		return handle;
	}


	private String writeAdmin(CSVDataSet dataSet, ArrayList<DataDimension> catDims, ArrayList<DataDimension> numDims, CategoryKeyDef keyDef) {
		String dsHandle = null;
		PreparedStatement dsStmt = null;
		PreparedStatement dimStmt = null;
		PreparedStatement catStmt = null;
		Statement stmt = null;
		try {
			dsStmt = db.prepareStatement("insert into Admin_DataSets values (?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'), ?, ?, ?);");
			dsStmt.clearBatch();
			dsStmt.setString(1, dataSet.getName());
			dsHandle = name2handle(dataSet.getName());
			dsHandle = makeUniqueHandle(dsHandle, db);
			dsStmt.setString(2, dsHandle);
			dsStmt.setString(3, "cube");
			dsStmt.setInt(4, dataSet.getNumDimensions());
			dsStmt.setInt(5, dataSet.getNumRecords(0));
			dsStmt.setString(6, dataSet.getMetaData().getSection());
			dsStmt.setString(7, dataSet.getMetaData().getURL());
			dsStmt.setString(8, dataSet.getMetaData().getSource());
			dsStmt.setString(9, dataSet.getMetaData().getSrcURL());
			dsStmt.addBatch();
			dsStmt.executeBatch();
			
			dimStmt = db.prepareStatement("insert into Admin_Dimensions values (?, ?, ?, ?, ?, ?);");
			catStmt = db.prepareStatement("insert into Admin_Categories values (?, ?, ?, ?, ?, ?);");
			int dimNum = 0;
			for (DataDimension dim : dataSet) {
				dimStmt.setString(1, dsHandle);
				dimStmt.setString(2, dim.getName());
				String dimHandle = name2handle(dim.getKey());
				dimStmt.setString(3, dimHandle);
				dimStmt.setString(4, dim.getDataType().toString());
				if (dim.getDataType() == DataType.categorical) {
					dimStmt.setInt(5, keyDef.getLeftShift(dimNum));
					dimStmt.setInt(6, (int)(keyDef.getBitMask(dimNum) >> keyDef.getLeftShift(dimNum)));
					dimNum++;
					for (int catNum = 0; catNum < dim.getNumCategories(); catNum++) {
						catStmt.setString(1, dsHandle);
						catStmt.setString(2, dimHandle);
						catStmt.setString(3, dim.getCategoryLabel(catNum));
						catStmt.setString(4, name2handle(dim.getCategoryName(catNum)));
						catStmt.setInt(5, catNum+1);
						catStmt.setInt(6, dim.getCategoryCount(catNum));
						catStmt.addBatch();
					}
				} else {
					dimStmt.setInt(5, 0);
					dimStmt.setInt(6, 0);
				}
				dimStmt.addBatch();
			}
			dimStmt.executeBatch();
			catStmt.executeBatch();
			
			stmt = db.createStatement();
			StringBuffer createSB = new StringBuffer("create table "+dsHandle+"_dims (key INTEGER, count INTEGER");
			for (DataDimension dim : catDims) {
				String handle = name2handle(dim.getKey());
				createSB.append(", "); createSB.append(handle); createSB.append(" INTEGER");
			}
			createSB.append(");");
			stmt.execute(createSB.toString());
			
			if (numDims.size() > 0) {
				createSB = new StringBuffer("create table "+dsHandle+"_measures (key INTEGER");
				for (DataDimension dim : numDims) {
					createSB.append(", "+name2handle(dim.getKey()));
					if (dim.getDataType() == DataType.numerical)
						createSB.append(" REAL");
					else
						createSB.append(" TEXT");
				}		
				createSB.append(");");
				stmt.execute(createSB.toString());
				stmt.execute("create index "+dsHandle+"_measures_key on "+dsHandle+"_measures (key);");
			}
		
		} catch (SQLException e) {
			PSLogging.logger.error("Error writing admin data.", e);
		} finally {
			try {
				if (dsStmt != null)
					dsStmt.close();
			} catch (SQLException e) {
				PSLogging.logger.error("Error closing DB statement 'dsStmt'.", e);
			}
			try {
				if (dimStmt != null)
					dimStmt.close();
			} catch (SQLException e) {
				PSLogging.logger.error("Error closing DB statement 'dimStmt'.", e);
			}
			try {
				if (catStmt != null)
					catStmt.close();
			} catch (SQLException e) {
				PSLogging.logger.error("Error closing DB statement 'catStmt'.", e);
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				PSLogging.logger.error("Error closing DB statement 'stmt'.", e);
			}
		}
		return dsHandle;
	}
	
	private String makeUniqueHandle(String handle, Connection db) {
		int num = 0;
		try {
			PreparedStatement stmt = db.prepareStatement("select count(*) from Admin_Datasets where handle = ?;");
			stmt.setString(1, handle);
			ResultSet rs = stmt.executeQuery();
			while (rs.getInt(1) > 0) {
				num++;
				stmt.setString(1, handle+num);
				rs = stmt.executeQuery();
			}
			stmt.close();
		} catch (SQLException e) {
			PSLogging.logger.error("Error creating unique DB handle.", e);
		}
		if (num == 0)
			return handle;
		else
			return handle+num;
	}

	public static String name2handle(String name) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < name.length(); i++) {
			char c = Character.toLowerCase(name.charAt(i));
			if (Character.getType(c) == Character.LOWERCASE_LETTER || Character.getType(c) == Character.DECIMAL_DIGIT_NUMBER)
				sb.append(c);
		}
		return sb.toString();
	}
	
	public boolean containsHandle(String handle) {
		for (DataSet ds : getDataSets()) {
			if (ds.getHandle().equals(handle))
				return true;
		}
		return false;
	}
	
	private void writeData(int numRecords, String handle, List<DataDimension> catDims, List<DataDimension> numDims, CategoryKeyDef keyDef) {
		int keyArray[] = new int[catDims.size()];
		Map<CategoryKey, CubeValues> cubeValues = new TreeMap<CategoryKey, CubeValues>();
		PreparedStatement measureStmt = null;
		try {
			if (numDims.size() > 0) {
				StringBuffer measureSB = new StringBuffer("insert into "+handle+"_measures values (?");
				for (int i = 0; i < numDims.size(); i++)
					measureSB.append(", ?");
				measureSB.append(");");
				measureStmt = db.prepareStatement(measureSB.toString());
			}
			for (int index = 0; index < numRecords; index++) {
				int dimNum = 0;
				for (DataDimension dim : catDims) {
					keyArray[dimNum] = (int)dim.getValues()[index]+1;
					dimNum++;
				}
				CategoryKey key = new CategoryKey(keyArray, keyDef);
	
				CubeValues values = cubeValues.get(key);
				if (values == null)
					cubeValues.put(key, new CubeValues(keyArray));
				else
					values.count++;
	
				if (numDims.size() > 0) {
					measureStmt.setLong(1, key.getKeyValue());
					int mDimNum = 2;
					for (DataDimension dim : numDims) {
						switch(dim.getDataType()) {
						case numerical:
							measureStmt.setFloat(mDimNum, dim.getValues()[index]);
							break;
						case textual:
							measureStmt.setString(mDimNum, dim.getCategoryName((int)dim.getValues()[index]));
						}
						mDimNum++;
					}
					measureStmt.addBatch();
				}
			}
			if (numDims.size() > 0)
				measureStmt.executeBatch();
		} catch (SQLException e) {
			PSLogging.logger.error("Error writing measures.", e);
		} finally {
			try {
				if (measureStmt != null)
					measureStmt.close();
			} catch (SQLException e) {
				PSLogging.logger.error("Error closing DB statement 'measureStmt'.", e);
			}
		}

		StringBuffer dimsSB = new StringBuffer("insert into "+handle+"_dims values (?, ?");
		for (int i = 0; i < catDims.size(); i++)
			dimsSB.append(", ?");
		dimsSB.append(");");
		PreparedStatement dimsStmt = null;
		try {
			dimsStmt = db.prepareStatement(dimsSB.toString());
			for (Map.Entry<CategoryKey, CubeValues> e : cubeValues.entrySet()) {
				dimsStmt.setLong(1, e.getKey().getKeyValue());
				CubeValues vals = e.getValue();
				dimsStmt.setInt(2, vals.count);
				for (int i = 0; i < vals.categoryValues.length; i++)
					dimsStmt.setInt(i+3, vals.categoryValues[i]);
				dimsStmt.addBatch();
			}
			dimsStmt.executeBatch();
		} catch (SQLException e) {
			PSLogging.logger.error("Error writing measures.", e);
		} finally {
			try {
				if (dimsStmt != null)
					dimsStmt.close();
			} catch (SQLException e) {
				PSLogging.logger.error("Error closing DB statement 'dimsStmt'.", e);
			}
		}
	}

	protected void deleteFromDB(String dbHandle) {
		PSLogging.logger.info("Deleting dataset '"+dbHandle+"' from local DB.");
		try {
			Statement stmt = db.createStatement();
			stmt.execute("begin transaction;");
			stmt.execute("delete from Admin_DataSets where handle = '"+dbHandle+"';");
			stmt.execute("delete from Admin_Dimensions where dataSet = '"+dbHandle+"';");
			stmt.execute("delete from Admin_Categories where dataSet = '"+dbHandle+"';");
			stmt.execute("drop table "+dbHandle+"_dims;");
			stmt.execute("drop table if exists "+dbHandle+"_measures;");
			stmt.execute("commit;");
			stmt.execute("vacuum;");
			stmt.close();
		} catch (SQLException e) {
			PSLogging.logger.error("Could not delete dataset.", e);
		}
		rescanDB();
	}
	
	public String getSetting(String key) {
		try {
			PreparedStatement stmt = prepareStatement("select value from Admin_Settings where key = ?;", DBAccess.FORREADING);
			stmt.setString(1, key);
			ResultSet rs = stmt.executeQuery();
			String value = rs.getString(1);
			stmt.close();
			return value;
		} catch (SQLException e) {
			PSLogging.logger.error("Could not retrieve setting for "+key, e);
		} finally {
			releaseReadLock();
		}
		
		return null;
	}

	public void storeSetting(String key, String value) {
		try {
			PreparedStatement stmt = prepareStatement("update Admin_Settings set value = ? where key = ?;", DBAccess.FORWRITING);
			stmt.setString(1, value);
			stmt.setString(2, key);
			int rows = stmt.executeUpdate();
			if (rows == 0) {
				stmt.close();
				stmt = db.prepareStatement("insert into Admin_Settings values (?, ?);");
				stmt.setString(1, value);
				stmt.setString(2, key);
				stmt.execute();
			}
			stmt.close();
		} catch (Exception e) {
			PSLogging.logger.error("Could not update setting "+key+" to "+value, e);
		} finally {
			releaseWriteLock();
		}
	}
	
}
