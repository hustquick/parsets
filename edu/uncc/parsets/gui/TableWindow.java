package edu.uncc.parsets.gui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import au.com.bytecode.opencsv.CSVWriter;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

import edu.uncc.parsets.data.CategoryHandle;
import edu.uncc.parsets.data.CategoryNode;
import edu.uncc.parsets.data.DataType;
import edu.uncc.parsets.data.DimensionHandle;
import edu.uncc.parsets.data.LocalDBDataSet;
import edu.uncc.parsets.data.LocalDB.DBAccess;
import edu.uncc.parsets.parsets.VisualConnection;
import edu.uncc.parsets.util.PSLogging;
import edu.uncc.parsets.util.osabstraction.AbstractOS;

public class TableWindow extends JFrame{
	
	private VisualConnection currentRibbon = null;
	private String[] columnNames;
	private String[][] tableData;
	private String query = "";
	private ArrayList<DimensionHandle> dimensionList;
	private ArrayList<CategoryHandle> categoryList = new ArrayList<CategoryHandle>();
	private String[] csvArray;
	private CategoryNode currentNode;
	private LocalDBDataSet currentDataSet;
	private String[] csvlist;
	
	private Vector<Object> dataVector = new Vector<Object>();
	private Vector<Object> columnVector = new Vector<Object>();
 	
	
	
	public TableWindow(VisualConnection selectedRibbon){
		currentRibbon = selectedRibbon;
	//	FillTable();
		fillTable2();
		initiateFrame();
	}
	
	
	private void initiateFrame(){
		
		final JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());
		JButton export = new JButton("Export to CSV File");
        export.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = AbstractOS.getCurrentOS().showDialog(frame, new CSVFileNameFilter(), FileDialog.SAVE);
                if (fileName != null) {
                    exportCSVFile(fileName, csvArray);
                }
            }
        });
			
			
		
        JTable table = new JTable(dataVector, columnVector){
        	  public boolean isCellEditable(int row, int column){
        		    return false;}};
		JScrollPane scrollPane = new JScrollPane(table);
		frame.add(scrollPane);
		frame.pack();
		frame.add(export, BorderLayout.SOUTH);
		frame.setVisible(true);
		scrollPane.setVisible(true);
		
		
	}
	
	private void fillTable2(){
		
		if(currentRibbon != null){
			currentNode = currentRibbon.getNode();
			currentDataSet = currentNode.getToCategory().getDimension().getLocalDataSet();
			ArrayList<DimensionHandle> tempDims = currentDataSet.getDimensions();
			dimensionList = new ArrayList<DimensionHandle>();
					
			// fill dimension list with categorical dimensions only
			for (DimensionHandle d : tempDims){
					if(d.getDataType() == DataType.categorical)
						dimensionList.add(0, d);
			}
			
			// fill category list for sql query
			while(currentNode.getParent() != null){
				categoryList.add(0, currentNode.getToCategory());
				currentNode = currentNode.getParent();
			}
			
			// build the sql query string
			query = "";
    		query += "select * from " + currentDataSet.getHandle() + "_dims where ";
    		for(CategoryHandle c : categoryList){ 			
    			query += c.getDimension().getHandle() + " = " + c.getCategoryNum() + " and ";
    			
    		}
    		query = query.substring(0, query.length()-5);
    		
    		int col = 0;
    		
    		
    		try{
    			Statement stmt = currentDataSet.getDB().createStatement(DBAccess.FORREADING);
    			ResultSet rs = stmt.executeQuery(query);
    			ResultSetMetaData meta = rs.getMetaData();
    			col = meta.getColumnCount();
    			
    			// fill column vector
        		for(int i = 1; i <= col; i++){
        			if(i >= 2){
            			String temp = meta.getColumnName(i);
            			columnVector.add(temp);
        			}

        		}
    			
    			
    			while(rs.next()){
    				Vector<Object> tempVector = new Vector<Object>();
    				for(int i = 1; i<=col; i++){
    					String temp = rs.getString(i);
    					if(i == 2){
    						tempVector.add(temp);
    					}
    					if(i >= 2){
    						String colName = meta.getColumnName(i);
    						for(DimensionHandle d : dimensionList){
    							if(d.getHandle().equals(colName)){    							
    								tempVector.add(d.num2Handle(Integer.parseInt(temp)).getName());
    							}
    						}
    						
    					}
    				}
    				
    				
    				dataVector.add(tempVector);
    			}
    		}
    		catch(SQLException e) {
    			e.printStackTrace();
    		} finally {
    			currentDataSet.getDB().releaseReadLock();		
    		}
    		
  		  		
    		// populate array for csv printing
    		csvArray = new String[dataVector.size()*(col-1)];
    		System.out.println(csvArray.length);
    		int csvcounter = 0;
			for(Object v : dataVector){
				Vector temp = (Vector)v;
				for(Object z : temp){
					csvArray[csvcounter] = z.toString();
					csvcounter++;
				}
			}		
		}
	}	
	
	
	
	private void FillTable(){
		
		if(currentRibbon != null){
			currentNode = currentRibbon.getNode();
			currentDataSet = currentNode.getToCategory().getDimension().getLocalDataSet();
	//		ArrayList<DimensionHandle> tempDims = currentDataSet.getDimensions();
	//		dimensionList = new ArrayList<DimensionHandle>();
	//		for (DimensionHandle d : tempDims){
	//			if(d.getDataType() == DataType.categorical)
	//				dimensionList.add(0, d);
	//		}
			
	//		for(DimensionHandle d2: dimensionList)
	//			System.err.println(d2.getHandle());
			
			dimensionList = currentDataSet.getDimensions();
			
			// populate category List
			while(currentNode.getParent() != null){
				categoryList.add(0, currentNode.getToCategory());
				currentNode = currentNode.getParent();
			}
			
			// build the sql query string
    		query += "select * from " + currentDataSet.getHandle() + "_dims where ";
    		for(CategoryHandle c : categoryList){ 			
    			query += c.getDimension().getHandle() + " = " + c.getCategoryNum() + " and ";
    			
    		}
    		query = query.substring(0, query.length()-5);
    		System.err.print(query);
    		
    		// get row and column count
    		int row = 0;
    		int col = 0;
    		try{
    		Statement stmt = currentDataSet.getDB().createStatement(DBAccess.FORREADING);
    		ResultSet rs = stmt.executeQuery(query);
    		col = rs.getMetaData().getColumnCount();
    		while(rs.next()){
    			row++;  			
    		}
    		}
    		catch(SQLException e) {
    			e.printStackTrace();
    		} finally {
    			currentDataSet.getDB().releaseReadLock();
    		}
    		
    		// populate the 2 dim String array
    		tableData = new String[row][col]; 
    		System.out.println("columns" + col + " size of dimensions " + dimensionList.size());
    		
    		try{
    		Statement stmt = currentDataSet.getDB().createStatement(DBAccess.FORREADING);
    		ResultSet rs = stmt.executeQuery(query);
    		int rowcounter = 0;
    		while(rs.next()){
    			for(int i = 1; i <= col; i ++){
    				String temp = rs.getString(i);
    				if(i == 2)
    					tableData[rowcounter][dimensionList.size()] = temp;
    				else if(i > 2){
    					tableData[rowcounter][i-3] = dimensionList.get(i-3).num2Handle(Integer.parseInt(temp)).getHandle();
    				}
    				
    			}
    			rowcounter++;		
    		}
    		}
    		catch(SQLException e) {
    			e.printStackTrace();
    		} finally {
    			currentDataSet.getDB().releaseReadLock();
    		}
    		
    		// populate export csv list
    		int csvcounter = 0;
    		csvlist = new String[tableData.length*(col-1)];
    		for(int i = 0; i < tableData.length; i++){
    			for(int j = 0; j < (col-1); j++){
    				csvlist[csvcounter] = tableData[i][j];
    				csvcounter++;
    			}
    		}
			
    		columnNames = new String[dimensionList.size()+1];
    		int counter = 0;
    		for(DimensionHandle handle : dimensionList){
    			columnNames[counter] = handle.getName();
    			counter++;
    		}
    		columnNames[dimensionList.size()] = "Count";

			
		}
	}
	
	

	
	public void exportCSVFile(String filename, String[] exportlist){
		
		try{
			 CSVWriter writer = new CSVWriter(new FileWriter(filename), ',');
			 writer.writeNext(exportlist);
			 writer.close();
			
			
		}catch (FileNotFoundException e) {
			PSLogging.logger.error("Error exporting CSV", e);
		} catch (IOException e) {
			PSLogging.logger.error("Error exporting CSV", e);
		}
		
	}
	

	
	
	private static class CSVFileNameFilter extends CombinedFileNameFilter {

	    @Override
	    public String getDescription() {
	        return "CSV Files";
	    }

	    @Override
	    public String getExtension() {
	        return ".csv";
	    }
	}

}


