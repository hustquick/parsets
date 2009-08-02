package edu.uncc.parsets.parsets;

import java.awt.Color;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import com.sun.opengl.util.j2d.TextRenderer;

import edu.uncc.parsets.data.CategoryHandle;
import edu.uncc.parsets.data.CategoryTree;
import edu.uncc.parsets.data.DimensionHandle;
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

public class CategoricalAxis extends VisualAxis {

	private DimensionHandle dimension;
	
	private ArrayList<CategoryBar> bars = new ArrayList<CategoryBar>();

	private float xOffset;

	private int barHeight;
	private int textWidth;
	private int textHeight;

	private boolean isHandleActive = false;

	private float newBarY = -1;

	private int displayWidth;

	public CategoricalAxis(DimensionHandle dim) {
		dimension = dim;
		int colorNum = 0;
		for (CategoryHandle cat : dim) {
			bars.add(new CategoryBar(cat, colorNum, this));
			colorNum++;
		}
		barY = new AnimatableProperty();
	}
	
	@Override
	public void layout(int y, int xOffset, int width, int gap, int barHeight, CategoryTree dataTree) {
		if (isHandleActive)
			newBarY = y - barHeight;
		else
			barY.setValue(y - barHeight);
		int visibleSize = 0;
		for (CategoryBar bar : bars) 
			if (bar.isVisible()) 
				visibleSize++;
		
		float gapPer = (float)gap/(float)(visibleSize-1);
		if (visibleSize == 1)
			gapPer = 0;
				
		this.xOffset = xOffset;
		displayWidth = width+gap;
		float x = xOffset;
		for (CategoryBar bar : bars) {
			if (bar.isVisible()) {
				x += bar.layout((int)x, width, dataTree);
				x += gapPer;
			}
		}
		this.barHeight = barHeight;
	}
	
	@Override
	public void display(GL gl, TextRenderer dimFont, FontMetrics dimFontMetrics,
			TextRenderer catFont, FontMetrics catFontMetrics) {
		
		if (textWidth == 0) {
			textWidth = dimFontMetrics.stringWidth(dimension.getName());
			textHeight = dimFontMetrics.getAscent();
		}
		
		if (isHandleActive) {
			gl.glColor4f(1f, 1f, 1f, .5f);
			gl.glBegin(GL.GL_QUADS);
			gl.glVertex2f(xOffset, barY.getValue() + textHeight + 5);
			gl.glVertex2f(xOffset, barY.getValue() - textHeight);
			gl.glVertex2f(xOffset + displayWidth, barY.getValue() - textHeight);
			gl.glVertex2f(xOffset + displayWidth, barY.getValue() + textHeight + 5);
			gl.glEnd();
			dimFont.setColor(Color.black);
		} else
			dimFont.setColor(Color.LIGHT_GRAY.darker());

		dimFont.begin3DRendering();
		dimFont.draw(dimension.getName(), (int)xOffset, (int)barY.getValue()+5);
		dimFont.end3DRendering();
		
		for (CategoryBar bar : bars)
			if (bar.isVisible())
				bar.display(gl, catFont, catFontMetrics, (int)barY.getValue(), barHeight);
	}	
	
	public CategoryBar getCategoryBar(CategoryHandle handle) {

		for (CategoryBar bar : bars)
			if (bar.getCategory() == handle) 
				return bar;	
		
		return null;
		
	}

	/**
	 * @return the dimension
	 */
	public DimensionHandle getDimension() {
		return dimension;
	}
		
	public void setTopLevel(boolean isTopLevel) {
		for (CategoryBar bar : bars)
			bar.setTopLevel(isTopLevel);
	}

	@Override
	public boolean containsY(int y) {
		return (y < barY.getValue() + textHeight + 5) && (y >= barY.getValue() - textHeight-1);
	}
	
	public int getBarHeight() {
		return barHeight;
	}
	
	@Override
	public CategoryBar findBar(int x, int y) {
		if (y < barY.getValue())
			for (CategoryBar bar : bars)
				if (bar.containsX(x))
					return bar;
		return null;
	}
	
	public int moveCategoryBar(int x, CategoryBar activeBar) {
		int activeIndex = bars.indexOf(activeBar);
		int insertIndex = 0;
		while ((insertIndex < bars.size()) && (x > bars.get(insertIndex).getLeftX()))
			insertIndex++;
		if (insertIndex != activeIndex) {
			bars.remove(activeIndex);
			if (activeIndex < insertIndex)
				insertIndex--;
			if (insertIndex > bars.size())
				bars.add(activeBar);
			else
				bars.add(insertIndex, activeBar);
			return insertIndex;
		}
		return -1;
	}
	
	public void removeBar(CategoryHandle handle) {
		getCategoryBar(handle).setVisible(false);
	}

	public void addBar(CategoryHandle category) {
		getCategoryBar(category).setVisible(true);
	}
	
	public List<CategoryHandle> getCategoryOrder() {
		ArrayList<CategoryHandle> cats = new ArrayList<CategoryHandle>();		
		for (CategoryBar bar : bars) {
			cats.add(bar.getCategory());
		}		
		return cats;
	}

	public void setHandleActive(boolean newActive) {
		isHandleActive = newActive;
		if (newActive)
			newBarY = -1;
		else if (newBarY >= 0)
			barY.setValue(newBarY);		
	}

	
}
