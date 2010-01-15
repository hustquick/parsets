package edu.uncc.parsets.parsets;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.MouseInputAdapter;

import edu.uncc.parsets.data.CategoryHandle;
import edu.uncc.parsets.data.CategoryNode;
import edu.uncc.parsets.data.GenoSetsDataSet;
import edu.uncc.parsets.data.GenoSetsDimensionHandle;
import edu.uncc.parsets.gui.Controller;
import edu.uncc.parsets.parsets.CategoricalAxis.ButtonAction;
import edu.uncc.parsets.util.AnimatableProperty;
import genosetsdb.MultipleViewController;
import genosetsdb.interaction.SelectedDimension;
import genosetsdb.interaction.SelectedDimensionChangeEvent;

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
	private VisualConnection selectedRibbon = null;
	private Controller controller;
	
	public ParSetsInteraction(ParSetsView parSetsView) {
		view = parSetsView;
		controller = view.getController();
	}
	
	@Override
	public final void mousePressed(MouseEvent e) {
		if (activeCategoryBar != null) {
			deltaMouseX = e.getX()-activeCategoryBar.getLeftX();
		}
	}

	@Override
	public final void mouseReleased(MouseEvent e) {
		if (activeAxis != null) {
			AnimatableProperty.beginAnimations(.33f, 1, AnimatableProperty.SpeedProfile.linearInSlowOut, view);
			activeAxis.setActive(false);
			if (activeAxis.getButtonAction() != ButtonAction.None)
				activeAxis.sort(view.getDataTree(), view.getConnectionTree(), activeAxis.getButtonAction());
			activeAxis = null;
			view.layout();
			AnimatableProperty.commitAnimations();
		}else{
			if(selectedRibbon != null){
				if(e.getButton() == MouseEvent.BUTTON3){
					//GenoSetsPopup menu = new GenoSetsPopup();
					//menu.show(e.getComponent(), e.getX(), e.getY());
					//notifyDependants();
					createDependant();
				}else{
					System.out.println("notifying dependents");
					notifyDependants();
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
			activeCategoryBar.setLeftX(e.getX()-deltaMouseX);
			int index = activeAxis.moveCategoryBar(e.getX(), activeCategoryBar);
			if (index != -1) {
				// animation leads to odd layout issues.
//				AnimatableProperty.beginAnimations(2, 1, AnimatableProperty.SpeedProfile.linearInSlowOut, view);
				view.moveCategory(activeAxis, activeCategoryBar.getCategory(), index);
				view.layout();
//				AnimatableProperty.commitAnimations();
			}
			view.repaint();
		} else if (activeAxis != null) {
			activeAxis.setBarY(view.getHeight() - e.getY());
			
			int index = view.getAxisPosition(view.getHeight() - e.getY(), activeAxis);
			if (index != -1) {
				view.moveAxis(index, activeAxis);
				view.layout();
			}
			
			view.repaint();
		}
	}

	@Override
	public final void mouseMoved(MouseEvent e) {
		int mouseX = e.getX();
		int mouseY = view.getHeight()-e.getY();
		
		view.clearTooltip();

		if (activeCategoryBar != null)
			activeCategoryBar.setActive(false);

		if (activeAxis != null)
			activeAxis.setActive(false);

		activeCategoryBar = null;
		activeAxis = null;
		for (VisualAxis va : view.getAxes()) {
			if (va.containsY(mouseY)) {
				activeAxis = (CategoricalAxis)va;
				activeAxis.setActive(true);
				activeCategoryBar = va.findBar(mouseX, mouseY);
			}
		}
		if (activeCategoryBar != null) {			
			activeCategoryBar.setActive(true);
			
			String s = activeCategoryBar.getCategory().getName() + "\n";
			s += view.getDataTree().getFilteredCount(activeCategoryBar.getCategory()) + ", ";
			s += (int)(view.getDataTree().getFilteredFrequency(activeCategoryBar.getCategory()) * 100) + "%";
			view.setTooltip(s, mouseX, mouseY);		
			view.getConnectionTree().selectCategory(activeCategoryBar.getCategory());
		} else if (activeAxis == null) {
			selectedRibbon = null;
			selectedRibbon = view.getConnectionTree().getAndHighlightRibbon(mouseX, mouseY, view.getDataTree());
			if (selectedRibbon != null){ 
				view.setTooltip(selectedRibbon.getTooltip(view.getDataTree().getFilteredTotal()), 
						mouseX, mouseY);
			}
		} else
			view.getConnectionTree().clearSelection();


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
	
	public void createDependant(){
		MultipleViewController childVisController = new MultipleViewController(createSelectEvent());
		controller.addSelectedDimListener(childVisController);
	}
	
	public void notifyDependants(){
		controller.notifySelectedDimListeners(createSelectEvent());
	}
	
	private SelectedDimensionChangeEvent createSelectEvent(){
		//Get the path to the selected category node and add all category, value pairs and add to list
		List<SelectedDimension> selectedList = new LinkedList();
		CategoryNode node = selectedRibbon.getNode();
		GenoSetsDataSet data = (GenoSetsDataSet) node.getToCategory().getDimension().getDataSet();
		ArrayList<CategoryHandle> list = node.getNodePath();
		for (CategoryHandle catHandle : list) {
			GenoSetsDimensionHandle dh = (GenoSetsDimensionHandle)catHandle.getDimension();
			SelectedDimension selected = new SelectedDimension(dh.getParentTable(), dh.getName(), dh.getPropertyName(), catHandle.getName());
			selectedList.add(selected);
		}
		
		return new SelectedDimensionChangeEvent(this, data.createCriteria(), selectedList, data.getFactClass());
	}
}
