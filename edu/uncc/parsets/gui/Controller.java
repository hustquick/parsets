package edu.uncc.parsets.gui;

import java.util.ArrayList;

import edu.uncc.parsets.data.CategoryHandle;
import edu.uncc.parsets.data.DataSet;
import edu.uncc.parsets.parsets.ParSetsView;
import genosets.interaction.SelectedDimListener;
import genosets.interaction.SelectedDimensionChangeEvent;

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
 * The controller mostly notifies a variety of listeners of changes to the
 * database, also lets other objects talk to the view. This will need to be
 * re-thought when we have multiple views, especially when those can be of
 * different types.
 */
public class Controller {

	private ArrayList<DataSetListener> dsListeners = new ArrayList<DataSetListener>();
	private ArrayList<SelectedDimListener> selectionListeners = new ArrayList<SelectedDimListener>();
	public ParSetsView parSetsView;
	private ArrayList<CategoryHandle> filteredCategories = new ArrayList<CategoryHandle>();
	private boolean spawnSelected = false;
	private SideBar sideBar; //TODO: Gross
	
	public DBTab dbTab;
	
	public Controller() {
		
	}
	
	public void addDataSetListener(DataSetListener l) {
		dsListeners.add(l);
	}

	public void removeDataSetListener(DataSetListener l) {
		dsListeners.remove(l);
	}

	public void setDataSet(DataSet data) {
		for (DataSetListener l : dsListeners)
			l.setDataSet(data);
	}
	
	public void addSelectedDimListener(SelectedDimListener listener){
		selectionListeners.add(listener);
	}
	
	public void removeSelectedDimListener(SelectedDimListener listener){
		selectionListeners.remove(listener);
	}
	
	public void notifySelectedDimListeners(SelectedDimensionChangeEvent e){
		for (SelectedDimListener l : selectionListeners) {
			l.updateParentSelection(e);
		}	
	}
	
	public void addFilteredCategory(CategoryHandle category){
		filteredCategories.add(category);
	}
	
	public void removeFilteredCategory(CategoryHandle category){
		filteredCategories.remove(category);
	}
	
	public ArrayList<CategoryHandle> getFilteredCategories(){
		return this.filteredCategories;
	}

	public boolean isSpawnSelected() {
		return spawnSelected;
	}

	public void setSpawnSelected(boolean spawnSelected) {
		this.spawnSelected = spawnSelected;
	}

	public SideBar getSideBar() {
		return sideBar;
	}

	public void setSideBar(SideBar sideBar) {
		this.sideBar = sideBar;
	}
}
