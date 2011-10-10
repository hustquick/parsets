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
	private String query = "";	
	private boolean isOnCategoryBar = false;
		
	
	
	public TableWindow(VisualConnection selectedRibbon, boolean isOnBar){
		currentRibbon = selectedRibbon;
		isOnCategoryBar = isOnBar;
		initialize();
	}
	
	
	public void initialize(){
		
		Vector<Vector<String>> dataVector = new Vector<Vector<String>>();
		Vector<String> columnVector = new Vector<String>();
		final ArrayList<String[]> csvArray = new ArrayList<String[]>();
		
		if(currentRibbon != null){
			CategoryNode currentNode = currentRibbon.getNode();
			LocalDBDataSet currentDataSet = currentNode.getToCategory().getDimension().getLocalDataSet();
			ArrayList<DimensionHandle> tempDims = currentDataSet.getDimensions();
			ArrayList<DimensionHandle> dimensionList = new ArrayList<DimensionHandle>();
					
			// fill dimension list with categorical dimensions only
			for (DimensionHandle d : tempDims){
					if(d.getDataType() == DataType.categorical)
						dimensionList.add(0, d);
			}
			
			
			ArrayList<CategoryHandle> categoryList = new ArrayList<CategoryHandle>();
			// fill category list for sql query
			if(!isOnCategoryBar){
				while(currentNode.getParent() != null){
					categoryList.add(0, currentNode.getToCategory());
					currentNode = currentNode.getParent();
				}
			}
			else{
				categoryList.add(0, currentNode.getToCategory());
			}

			
			// build the sql query string
    		query += "select * from " + currentDataSet.getHandle() + "_dims where ";
    		for(CategoryHandle c : categoryList){ 			
    			query += c.getDimension().getHandle() + " = " + c.getCategoryNum() + " and ";
    			System.out.println("dimension is " + c.getDimension().getHandle() + " and value is " + c.getCategoryNum());
    			
    		}
    		query = query.substring(0, query.length()-5);
    		
    		int col = 0;

    		
    		try{
    			Statement stmt = currentDataSet.getDB().createStatement(DBAccess.FORREADING);
    			ResultSet rs = stmt.executeQuery(query);
    			ResultSetMetaData meta = rs.getMetaData();
    			col = meta.getColumnCount();
    			
    			// fill column vector
    			columnVector.add("Count");
        		for(int i = 1; i <= col; i++){
        			if(i >= 2){
            			String temp = meta.getColumnName(i);
            			for(DimensionHandle d : dimensionList){
            				if(d.getHandle().equals(temp)){
            					columnVector.add(d.getName());
            				}
            			}
        			}

        		}
    			
    			
    			while(rs.next()){
    				Vector<String> tempVector = new Vector<String>();
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
    		int csvcounter = 0;
			for(Vector<String> v : dataVector){
				 Vector<String> temp = v;
				String[] temp2 = new String[temp.size()];
				for(Object z : temp){
					temp2[csvcounter] = z.toString();
					csvcounter++;
				}
				csvArray.add(temp2);
				csvcounter = 0;
			}		
		}
		
		
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
	
	
	public void exportCSVFile(String filename, ArrayList<String[]> exportlist){
		
		try{
			 CSVWriter writer = new CSVWriter(new FileWriter(filename), ',');
			 for(String[] temp: exportlist){
				 writer.writeNext(temp);
			 }
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


