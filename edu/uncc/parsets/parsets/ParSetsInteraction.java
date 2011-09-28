package edu.uncc.parsets.parsets;

import java.awt.event.MouseEvent;

import java.util.ArrayList;

import javax.swing.event.MouseInputAdapter;

import edu.uncc.parsets.data.CategoryHandle;
import edu.uncc.parsets.data.CategoryNode;
import edu.uncc.parsets.data.DimensionHandle;
import edu.uncc.parsets.data.LocalDBDataSet;
import edu.uncc.parsets.parsets.CategoricalAxis.ButtonAction;
import edu.uncc.parsets.util.AnimatableProperty;

// new import
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
public class ParSetsInteraction extends MouseInputAdapter {

    private CategoryBar activeCategoryBar = null;
    private CategoricalAxis activeAxis = null;
    private int deltaMouseX;
    private ParSetsView view;
    VisualConnection selectedRibbon = null;
    
    // new component
    private JPopupMenu popmenu = new JPopupMenu();
    private JMenuItem tableOp1 = new JMenuItem("Create Table");


    public ParSetsInteraction(ParSetsView parSetsView) {
        view = parSetsView;
        
        // don't know if this should go here, new stuff
        tableOp1.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent a){
        		executeTable();
        	}});       
        popmenu.add(tableOp1);

    }

    @Override
    public final void mousePressed(MouseEvent e) {
        if (activeCategoryBar != null) {
            deltaMouseX = e.getX() - activeCategoryBar.getLeftX();
        }
        
        // new stuff
        if(e.getButton() == MouseEvent.BUTTON3 && selectedRibbon != null)
        	popmenu.show(e.getComponent(), e.getX(), e.getY());

        
    }
   

    @Override
    public final void mouseReleased(MouseEvent e) {
        if (activeAxis != null) {
            AnimatableProperty.beginAnimations(.33f, 1, AnimatableProperty.SpeedProfile.linearInSlowOut, view);
            activeAxis.setActive(false);
            if (activeAxis.getButtonAction() != ButtonAction.None) {
                activeAxis.sort(view.getDataTree(), view.getConnectionTree(), activeAxis.getButtonAction());
            }
            activeAxis = null;
            view.layoutAxes();
            AnimatableProperty.commitAnimations();
        } else {
            if (selectedRibbon != null) {
                if (e.getClickCount() == 1) {
                    fireSelectionChangeEvent(SelectionChangeEvent.SELECTION_CHANGE);
                }
            }
        }
        if (activeCategoryBar != null) {
            AnimatableProperty.beginAnimations(.33f, 1, AnimatableProperty.SpeedProfile.linearInSlowOut, view);
            activeCategoryBar.setActive(false);
            activeCategoryBar = null;
            AnimatableProperty.commitAnimations();
        }
        view.repaint();
    }

    @Override
    public final void mouseDragged(MouseEvent e) {
        view.clearTooltip();
        if (activeCategoryBar != null) {
            activeCategoryBar.setLeftX(e.getX() - deltaMouseX);
            int index = activeAxis.moveCategoryBar(e.getX(), activeCategoryBar);
            if (index != -1) {
                // animation leads to odd layout issues.
//				AnimatableProperty.beginAnimations(2, 1, AnimatableProperty.SpeedProfile.linearInSlowOut, view);
                view.moveCategory(activeAxis, activeCategoryBar.getCategory(), index);
                view.layoutAxes();
//				AnimatableProperty.commitAnimations();
            }
            view.repaint();
        } else if (activeAxis != null) {
            activeAxis.setBarY(e.getY());

            int index = view.getAxisPosition(e.getY(), activeAxis);
            if (index != -1) {
                view.moveAxis(index, activeAxis);
                view.layoutAxes();
            }

            view.repaint();
        }
    }

    @Override
    public final void mouseMoved(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        view.clearTooltip();

        if (activeCategoryBar != null) {
            activeCategoryBar.setActive(false);
        }

        if (activeAxis != null) {
            activeAxis.setActive(false);
        }

        activeCategoryBar = null;
        activeAxis = null;
        for (VisualAxis va : view.getAxes()) {
            if (va.containsY(mouseY)) {
                activeAxis = (CategoricalAxis) va;
                activeAxis.setActive(true);
                activeCategoryBar = va.findBar(mouseX, mouseY);
            }
        }
        if (activeCategoryBar != null) {
            activeCategoryBar.setActive(true);

            String s = activeCategoryBar.getCategory().getName() + "\n";
            s += view.getDataTree().getFilteredCount(activeCategoryBar.getCategory()) + ", ";
            s += (int) (view.getDataTree().getFilteredFrequency(activeCategoryBar.getCategory()) * 100) + "%";
            view.setTooltip(s, mouseX, mouseY);

            view.getConnectionTree().selectCategory(activeCategoryBar.getCategory());
        } else if (activeAxis == null) {
            selectedRibbon = view.getConnectionTree().getRibbon(mouseX, mouseY, view.getDataTree());
            if (selectedRibbon != null) {
                view.setTooltip(selectedRibbon.getTooltip(view.getDataTree().getFilteredTotal()),
                        mouseX, mouseY);
            }
        } else {
            view.getConnectionTree().clearSelection();
        }

        // TODO: Change cursor according to type of movement possible
        // requires hand-drawn cursors, standard types don't seem to include
        // up-down or left-right movement.

        view.repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        view.setMouseInDisplay(true);
        view.repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        view.setMouseInDisplay(false);
        view.clearTooltip();
        view.getConnectionTree().clearSelection();
        view.repaint();
    }

    private void fireSelectionChangeEvent(String selectionType) {
        CategoryNode node = selectedRibbon.getNode();
        ArrayList<CategoryHandle> cats = new ArrayList<CategoryHandle>();

        while (node.getParent() != null) {
            cats.add(0, node.getToCategory());
            node = node.getParent();
        }
        view.getController().setSelected(new SelectionChangeEvent(selectionType, cats, null));
    }
    
    
    private void executeTable()
    {
    	
    	if(selectedRibbon != null){
    		CategoryNode node = selectedRibbon.getNode();
    		LocalDBDataSet datab = node.getToCategory().getDimension().getLocalDataSet();
    		ArrayList<DimensionHandle> dims = datab.getDimensions();

    		ArrayList<CategoryHandle> cats = new ArrayList<CategoryHandle>();
    		int row = 0;
    		int col = 0;
    		while (node.getParent() != null) {			
    			cats.add(0, node.getToCategory());
    			node = node.getParent();			
    		}

	
    			
    		String query = "select * from " + datab.getHandle() + "_dims where ";
    		for(CategoryHandle c : cats){ 			
    			query += c.getDimension().getHandle() + " = " + c.getCategoryNum() + " and ";
    			
    		}
    		query = query.substring(0, query.length()-5);
    		System.err.print(query);

    		System.out.println(datab.getHandle());
    		
    		try{
    		Statement stmt = datab.getDB().createStatement(DBAccess.FORREADING);
    		ResultSet rs = stmt.executeQuery(query);
    		col = rs.getMetaData().getColumnCount();
    		while(rs.next()){
    			row++;
    			
    			
    		}
    		}
    		catch(SQLException e) {
    			e.printStackTrace();
    		} finally {
    			datab.getDB().releaseReadLock();
    		}
    		  		
    	
    		String[][] results = new String[row][col]; 
    		System.out.println("columns" + col + " size of dimensions " + dims.size());
    		
    		try{
    		Statement stmt = datab.getDB().createStatement(DBAccess.FORREADING);
    		ResultSet rs = stmt.executeQuery(query);
    		int rowcounter = 0;
    		while(rs.next()){
    			for(int i = 1; i <= col; i ++){
    				String temp = rs.getString(i);
    				if(i == 2)
    					results[rowcounter][dims.size()] = temp;
    				else if(i > 2){
    					results[rowcounter][i-3] = dims.get(i-3).num2Handle(Integer.parseInt(temp)).getHandle();
    				}
    				
    			}
    			rowcounter++;
    			
    		}
    		}
    		catch(SQLException e) {
    			e.printStackTrace();
    		} finally {
    			datab.getDB().releaseReadLock();
    		}
    		
    		
    		String[] columnNames = new String[dims.size()+1];
    		int counter = 0;
    		for(DimensionHandle handle : dims){
    			columnNames[counter] = handle.getName();
    			counter++;
    		}
    		columnNames[dims.size()] = "Count";
    		
    		view.addTable(columnNames, results);

    	}
    	
    	
    }
    


}

