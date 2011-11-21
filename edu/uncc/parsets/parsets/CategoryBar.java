package edu.uncc.parsets.parsets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import edu.uncc.parsets.data.CategoryHandle;
import edu.uncc.parsets.data.CategoryTree;
import edu.uncc.parsets.util.AnimatableProperty;
import edu.uncc.parsets.util.ColorBrewer;
import edu.uncc.parsets.parsets.BarState;

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

public class CategoryBar {

	private CategoryHandle category;
	private AnimatableProperty leftX = new AnimatableProperty();
	private AnimatableProperty width = new AnimatableProperty();
	private int color;
	private boolean topLevel = false;
	private boolean visible = true;
	int filteredFrequency;

	Stroke THICKSTROKE = new BasicStroke(2);
	
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Keeps track of the position of incoming and outgoing ribbons.
	 */
	private float topIndexPoint;
	private float bottomIndexPoint;

	private CategoricalAxis axis;

	private boolean active = false;
	private int newLeftX;
	
	public CategoryBar(CategoryHandle cat, int colorNum, CategoricalAxis catAxis) {
		category = cat;
		color = colorNum;
		axis = catAxis;
		topIndexPoint = 0;
		bottomIndexPoint = 0;
	}

	public float layout(int x, int totalWidth, CategoryTree dataTree, BarState currentState) {
		if (active)
			newLeftX = x;
		else
			leftX.setValue(x);
		
		if(currentState == BarState.NORMAL){
			width.setValue(dataTree.getFilteredFrequency(category) * (float) totalWidth);
		}
		else{
			width.setValue(totalWidth/(axis.visibleBars()));
		}

		topIndexPoint = 0;
		bottomIndexPoint = 0;
		filteredFrequency = dataTree.getFilteredCount(category);
		return (float)width.getFutureValue();
	}

	public void paint(Graphics2D g, Font catFont, FontMetrics catFontMetrics,
			int topY, int barHeight) {

		if (active) {		
			if (topLevel)
				ColorBrewer.setColor(color, false, g);
			else
				g.setColor(Color.WHITE);

			g.fillRect((int)leftX.getValue(), topY, (int)width.getValue(), barHeight);
		}
		
		if (topLevel)
			ColorBrewer.setColor(color, true, g);
		else
			g.setColor(new Color(.3f, .3f, .5f));

		Stroke s = g.getStroke();
		g.setStroke(THICKSTROKE);
		g.drawLine((int)leftX.getValue(), topY + barHeight, (int)(leftX.getValue()+width.getValue()), topY + barHeight);
		g.setStroke(s);
				
		String label = category.getName();

		int labelWidth = catFontMetrics.stringWidth(label);
		
		if (labelWidth > width.getValue()) {
			while (labelWidth > width.getValue() && label.length() > 0) {
				label = label.substring(0, label.length() - 1);
				labelWidth = catFontMetrics.stringWidth(label+"...");
			}
			if (label.length() > 0)
				label = label.concat("...");
			else if (catFontMetrics.stringWidth(category.getName().substring(0, 1)) < width.getValue())
				label = category.getName().substring(0, 1);
		}

		g.setFont(catFont);
		g.drawString(label, (int) leftX.getValue(), topY + catFontMetrics.getAscent() + 1);
	}

	public float getTopIndexPoint() {
		return topIndexPoint;
	}

	public void setTopIndexPoint(float topIndexPoint) {
		this.topIndexPoint = topIndexPoint;
	}

	public int getLeftX() {
		return (int)leftX.getValue();
	}

	public int getFutureLeftX(){
		return (int)leftX.getFutureValue();
	}
	
	public int getWidth() {
		return (int)width.getValue();
	}
	
	public int getFutureWidth(){
		return (int)width.getFutureValue();
	}
	
	public boolean containsX(int x) {
		return (x >= leftX.getValue()) && (x < leftX.getValue()+width.getValue());
	}
	
	public CategoryHandle getCategory() {
		return category;
	}

	public float getBottomIndexPoint() {
		return bottomIndexPoint;
	}

	public void setBottomIndexPoint(float bottomIndexPoint) {
		this.bottomIndexPoint = bottomIndexPoint;
	}

	public int getOutRibbonY() {
		return axis.getBarY() + axis.getBarHeight() + 1;
	}

	public int getInRibbonY() {
		return getOutRibbonY() - 1;
	}

	public void setActive(boolean newActive) {
		active = newActive;
		if (newActive)
			newLeftX = -1;
		else if (newLeftX >= 0)
			leftX.setValue(newLeftX);
	}

	public boolean isActive() {
		return active;
	}
	
	public void setTopLevel(boolean topLevel) {
		this.topLevel = topLevel;
	}

	public void setLeftX(int newX) {
		leftX.setValue(newX);
	}
	
	public int getFrequency(){
		return filteredFrequency;
	}
	
}
