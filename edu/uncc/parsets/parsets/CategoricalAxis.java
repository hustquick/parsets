package edu.uncc.parsets.parsets;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import edu.uncc.parsets.data.CategoryHandle;
import edu.uncc.parsets.data.CategoryTree;
import edu.uncc.parsets.data.DimensionHandle;
import edu.uncc.parsets.util.AnimatableProperty;
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

public class CategoricalAxis extends VisualAxis {

	public static enum ButtonAction {None, Alpha_Asc, Alpha_Desc, Size_Asc, Size_Desc};
	
	@SuppressWarnings("serial")
	public static class CategoryBarAlphaComparator implements Comparator<CategoryBar>, Serializable {
		
		boolean asc;
		
		public CategoryBarAlphaComparator(boolean ascending) {
			asc = ascending;
		}
		
		@Override
		public int compare(CategoryBar o1, CategoryBar o2) {
			if (asc)
				return o1.getCategory().getName().compareTo(o2.getCategory().getName());
			else
				return -o1.getCategory().getName().compareTo(o2.getCategory().getName());
		}
	}

	public static class CategoryBarSizeComparator implements Comparator<CategoryBar> {
		
		boolean asc;
		CategoryTree tree;
		
		public CategoryBarSizeComparator(boolean ascending, CategoryTree ctree) {
			asc = ascending;
			tree = ctree;
		}
		
		@Override
		public int compare(CategoryBar o1, CategoryBar o2) {
			if (asc)
				return tree.getFilteredCount(o1.getCategory())-tree.getFilteredCount(o2.getCategory());
			else
				return tree.getFilteredCount(o2.getCategory())-tree.getFilteredCount(o1.getCategory());
		}
	};
	
	public static class Buttons {
		private static final String DESCENDING = "« ";
		private static final String ASCENDING = " »";
		private static final String ALPHA = "alpha";
		private static final String SIZE = "size";
		public static final int SPACING = 20;
		private static final Color LIGHT_COLOR = Color.LIGHT_GRAY;
		private static final Color DARK_COLOR = Color.DARK_GRAY;

		ButtonAction activeButton = ButtonAction.None;
		
		int alphaButtonsX;
		int alphaWidth;
		
		int sizeButtonsX;
		int sizeWidth;
		
		int arrowWidth;
		
		public Buttons(FontMetrics fm) {
			alphaWidth = fm.stringWidth(ALPHA);
			sizeWidth = fm.stringWidth(SIZE);
			arrowWidth = fm.stringWidth(ASCENDING);
		}

		public void display(Graphics2D g, Font navigationFont, int x, int y) {
			alphaButtonsX = x;

			g.setFont(navigationFont);
			
			if (activeButton == ButtonAction.Alpha_Desc)
				g.setColor(DARK_COLOR);
			else
				g.setColor(LIGHT_COLOR);
			g.drawString(DESCENDING, x, y);
			x += arrowWidth;
			
			if (activeButton == ButtonAction.Alpha_Asc || activeButton == ButtonAction.Alpha_Desc)
				g.setColor(DARK_COLOR);
			else
				g.setColor(LIGHT_COLOR);
			g.drawString(ALPHA, x, y);
			x += alphaWidth;
			
			if (activeButton == ButtonAction.Alpha_Asc)
				g.setColor(DARK_COLOR);
			else
				g.setColor(LIGHT_COLOR);
			g.drawString(ASCENDING, x, y);
			x += arrowWidth+SPACING;
			
			sizeButtonsX = x;
			
			if (activeButton == ButtonAction.Size_Desc)
				g.setColor(DARK_COLOR);
			else
				g.setColor(LIGHT_COLOR);
			g.drawString(DESCENDING, x, y);
			x += arrowWidth;
			
			if (activeButton == ButtonAction.Size_Asc || activeButton == ButtonAction.Size_Desc)
				g.setColor(DARK_COLOR);
			else
				g.setColor(LIGHT_COLOR);
			g.drawString(SIZE, x, y);
			x += sizeWidth;
			
			if (activeButton == ButtonAction.Size_Asc)
				g.setColor(DARK_COLOR);
			else
				g.setColor(LIGHT_COLOR);
			g.drawString(ASCENDING, x, y);
			
		}
		
		public void mouseOver(int x) {
			activeButton = ButtonAction.None;
			if (x >= alphaButtonsX && x <= alphaButtonsX+alphaWidth+2*arrowWidth) {
				if (x < alphaButtonsX+alphaWidth/2+arrowWidth)
					activeButton = ButtonAction.Alpha_Desc;
				else
					activeButton = ButtonAction.Alpha_Asc;
			} else if (x >= sizeButtonsX && x <= sizeButtonsX+alphaWidth+2*arrowWidth) {
				if (x < sizeButtonsX+sizeWidth/2+arrowWidth)
					activeButton = ButtonAction.Size_Desc;
				else
					activeButton = ButtonAction.Size_Asc;
			}
		}
	
		public ButtonAction getActiveButton() {
			return activeButton;
		}

		public void setActiveButton(ButtonAction newBtn) {
			activeButton = newBtn;
		}
	}
	
	private DimensionHandle dimension;
	
	private ArrayList<CategoryBar> bars = new ArrayList<CategoryBar>();

	private int xOffset;

	private int barHeight;
	private int textWidth;
	private int textHeight;

	private boolean isActive = false;

	private float newBarY = -1;

	private int displayWidth;

	private boolean handleActive;

	private static Buttons buttons;
	
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
	public void layout(int y, int xOffset, int width, int gap, int barHeight, CategoryTree dataTree, BarState currentState) {
		if (isActive)
			newBarY = y + barHeight;
		else
			barY.setValue(y + barHeight);
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
				x += bar.layout((int)x, width, dataTree, currentState);
				x += gapPer;
			}
		}
		this.barHeight = barHeight;
	}
	
	@Override
	public void paint(Graphics2D g, Font dimFont, FontMetrics dimFontMetrics,
			Font catFont, FontMetrics catFontMetrics) {
		
		if (textWidth == 0) {
			textWidth = dimFontMetrics.stringWidth(dimension.getName());
			textHeight = dimFontMetrics.getAscent();
		}
		
		if (isActive) {
			g.setColor(new Color(1, 1, 1, .5f));
			g.fillRect(xOffset, (int)barY.getValue() - textHeight - 3, displayWidth, 2 * textHeight + 3);
			g.setColor(Color.BLACK);
		} else
			g.setColor(Color.LIGHT_GRAY.darker());

		g.setFont(dimFont);
		g.drawString(dimension.getName(), (int)xOffset, (int)barY.getValue()-5);

		if (handleActive) {
			if (buttons == null)
				buttons = new Buttons(catFontMetrics);
			buttons.display(g, catFont, (int)xOffset+textWidth+2*Buttons.SPACING,
					(int)barY.getValue()-textHeight/2+catFontMetrics.getDescent());
		}
		
		for (CategoryBar bar : bars)
			if (bar.isVisible())
				bar.paint(g, catFont, catFontMetrics, (int)barY.getValue(), barHeight);
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
		return (y > barY.getValue() - textHeight - 5) && (y < barY.getValue() + textHeight + 1);
	}
	
	public int getBarHeight() {
		return barHeight;
	}
	
	@Override
	public CategoryBar findBar(int x, int y) {
		handleActive = false;
		if (y > barY.getValue()) {
			for (CategoryBar bar : bars)
				if (bar.containsX(x))
					return bar;
		} else
			if (buttons != null)
				buttons.mouseOver(x);
		handleActive = true;
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

	public void setActive(boolean newActive) {
		isActive = newActive;
		handleActive = false;
		if (newActive)
			newBarY = -1;
		else if (newBarY >= 0)
			barY.setValue(newBarY);		
	}
	
	public ButtonAction getButtonAction() {
		if (buttons != null)
			return buttons.getActiveButton();
		else
			return ButtonAction.None;
	}

	@Override
	public void setBarY(float value) {
		if (buttons != null)
			buttons.setActiveButton(ButtonAction.None);
		super.setBarY(value);
	}
	
	public void sort(CategoryTree categoryTree, VisualConnectionTree visualConnectionTree, ButtonAction axisCommand) {
		Comparator<CategoryBar> cbComparator = null;
		switch(axisCommand) {
		case Alpha_Asc:
			cbComparator = new CategoryBarAlphaComparator(true);
			break;
		case Alpha_Desc:
			cbComparator = new CategoryBarAlphaComparator(false);
			break;
		case Size_Asc:
			cbComparator = new CategoryBarSizeComparator(true, categoryTree);
			break;
		case Size_Desc:
			cbComparator = new CategoryBarSizeComparator(false, categoryTree);
			break;
		}
		CategoryBar barsArray[] = bars.toArray(new CategoryBar[bars.size()]);
		Arrays.sort(barsArray, cbComparator);
		bars.clear();
		for (CategoryBar b : barsArray)
			bars.add(b);
		visualConnectionTree.orderChildren(dimension, getCategoryOrder());
	}

	public int visibleBars(){
		int count = 0;
		for(CategoryBar  b : bars){
			if(b.isVisible())
				count++;
		}
		
		return count;
	}
	
}
