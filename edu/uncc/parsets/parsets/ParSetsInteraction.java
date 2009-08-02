package edu.uncc.parsets.parsets;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

import edu.uncc.parsets.util.AnimatableProperty;

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
	
	public ParSetsInteraction(ParSetsView parSetsView) {
		view = parSetsView;
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
			activeAxis.setHandleActive(false);
			activeAxis = null;
			view.layout();
			AnimatableProperty.commitAnimations();
		}
		if (activeCategoryBar != null) {
			AnimatableProperty.beginAnimations(.33f, 1, AnimatableProperty.SpeedProfile.linearInSlowOut, view);
			activeCategoryBar.setActive(false);
			activeCategoryBar = null;
			AnimatableProperty.commitAnimations();
		}
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
			
			int index = view.getAxisPosition(e.getY(), activeAxis);
			if (index != -1) {
				System.err.println("new Index! "+index);
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
			activeAxis.setHandleActive(false);

		activeCategoryBar = null;
		activeAxis = null;
		for (VisualAxis va : view.getAxes()) {
			if (va.containsY(mouseY)) {
				activeAxis = (CategoricalAxis)va;
				activeAxis.setHandleActive(true);
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
		} else {
			String s = view.getConnectionTree().highlightRibbon(mouseX, mouseY, view.getDataTree());
			if (s != null) 
				view.setTooltip(s, mouseX, mouseY);
			
		}

		// TODO: Change cursor according to type of movement possible
		// requires hand-drawn cursors, standard types don't seem to include
		// up-down or left-right movement.
		
		view.repaint();
	}
}
