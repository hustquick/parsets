package edu.uncc.parsets.parsets;

import java.awt.Color;
import java.awt.FontMetrics;

import javax.media.opengl.GL;

import com.sun.opengl.util.j2d.TextRenderer;

import edu.uncc.parsets.data.CategoryHandle;
import edu.uncc.parsets.data.CategoryTree;
import edu.uncc.parsets.util.AnimatableProperty;
import edu.uncc.parsets.util.ColorBrewer;

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
	private float width;
	private int color;
	private boolean topLevel = false;
	private boolean visible = true;

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

	public float layout(int x, int totalWidth, CategoryTree dataTree) {
		if (active)
			newLeftX = x;
		else
			leftX.setValue(x);
		width = (dataTree.getFilteredFrequency(category) * (float) totalWidth);

		topIndexPoint = 0;
		bottomIndexPoint = 0;

		return width;
	}

	public void display(GL gl, TextRenderer catFont, FontMetrics catFontMetrics,
			int topY, int barHeight) {

		if (active) {		
			if (topLevel)
				ColorBrewer.setColor(color, false, 1f, gl);
			else
				gl.glColor4f(1f, 1f, 1f, 1f);

			gl.glBegin(GL.GL_QUADS);
			gl.glVertex2f(leftX.getValue(), topY);
			gl.glVertex2f(leftX.getValue(), topY - barHeight);
			gl.glVertex2f(leftX.getValue() + width, topY - barHeight);
			gl.glVertex2f(leftX.getValue() + width, topY);
			gl.glEnd();
		}
		
		if (topLevel)
			ColorBrewer.setColor(color, true, gl);
		else
			gl.glColor3f(.3f, .3f, .3f);

		gl.glLineWidth(2);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2f(leftX.getValue(), topY - barHeight);
		gl.glVertex2f(leftX.getValue() + width, topY - barHeight);
		gl.glEnd();
		gl.glLineWidth(1);
				
		String label = category.getName();

		int labelWidth = catFontMetrics.stringWidth(label);
		
		if (labelWidth > width) {
			while (labelWidth > width && label.length() > 0) {
				label = label.substring(0, label.length() - 1);
				labelWidth = catFontMetrics.stringWidth(label+"\u2026"); // unicode code for ellipsis
			}
			if (label.length() > 0)
				label = label.concat("\u2026"); // unicode code for ellipsis
			else if (catFontMetrics.stringWidth(category.getName().substring(0, 1)) < width)
				label = category.getName().substring(0, 1);
		}
		
		catFont.begin3DRendering();
		catFont.setColor(Color.BLACK);
		if (label.length() > 0)
			catFont.draw(label, (int) leftX.getValue() + 3, topY - catFontMetrics.getAscent() - 1);
		catFont.end3DRendering();

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

	public int getWidth() {
		return (int)width;
	}
	
	public boolean containsX(int x) {
		return (x >= leftX.getValue()) && (x < leftX.getValue()+width);
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

	public float getOutRibbonY() {
		return axis.getBarY() - axis.getBarHeight() - 1;
	}

	public float getInRibbonY() {
		return getOutRibbonY() + 1;
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
}
