package edu.uncc.parsets.parsets;

import java.awt.Graphics2D;
import java.awt.Polygon;

import edu.uncc.parsets.data.CategoryNode;
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

public class BasicRibbon extends VisualConnection implements Comparable<BasicRibbon> {
	
	private CategoryBar upperBar, lowerBar;
	
	private AnimatableProperty upperOffset = new AnimatableProperty();
	private AnimatableProperty lowerOffset = new AnimatableProperty();
	
	public BasicRibbon(VisualConnection parent, CategoryNode categoryNode, CategoricalAxis upperAxis, CategoricalAxis lowerAxis) {
		super(parent);
		node = categoryNode;

		upperOffset.setDontAnimate(true);
		lowerOffset.setDontAnimate(true);
		
		this.upperBar = upperAxis.getCategoryBar(node.getParent().getToCategory());
		this.lowerBar = lowerAxis.getCategoryBar(node.getToCategory());
	}
	
	public BasicRibbon(CategoryNode categoryNode, CategoricalAxis upperAxis, CategoricalAxis lowerAxis) {
		
		node = categoryNode;

		upperOffset.setDontAnimate(true);
		lowerOffset.setDontAnimate(true);

		this.upperBar = upperAxis.getCategoryBar(node.getParent().getToCategory());
		this.lowerBar = lowerAxis.getCategoryBar(node.getToCategory());
		
	}

	public int compareTo(BasicRibbon o) {
		return node.compareTo(o.node);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BasicRibbon)
			return this.getNode().equals(((BasicRibbon) o).getNode());
		else
			return false;
	}

	@Override
	public int hashCode() {
		assert false : "hashCode not designed";
		return 42; // any arbitrary constant will do
	}
	
	public String toString() {
		return node.toString();
	}
	
	public String getTooltip(int filteredTotal) { 
		return node.getTooltipText(filteredTotal);
	}
	
	public void layout(float parentWidth) {

		if (node == null || node.getCount() == 0) {
			width = 0;
		} else {
			width = parentWidth * node.getRatio();
			
			upperOffset.setValue(upperBar.getBottomIndexPoint());
			upperBar.setBottomIndexPoint(upperOffset.getValue() + width);

			lowerOffset.setValue(lowerBar.getTopIndexPoint());
			lowerBar.setTopIndexPoint(lowerOffset.getValue() + width);
		}
	}
	
	public void display(Graphics2D g, float alpha) {
		
		if (width == 0 || isSelected)
			return;

		ColorBrewer.setColor(colorBrewerIndex, false, alpha, g);
		if (width >= 1) {
			int xPoints[] = new int[4];
			int yPoints[] = new int[4];
			
			xPoints[0] = upperBar.getLeftX() + (int)upperOffset.getValue(); yPoints[0] = upperBar.getOutRibbonY();
			xPoints[1] = upperBar.getLeftX() + (int)upperOffset.getValue() + (int)Math.round(width); yPoints[1] = upperBar.getOutRibbonY();
			xPoints[2] = lowerBar.getLeftX() + (int)lowerOffset.getValue() + (int)Math.round(width); yPoints[2] = lowerBar.getInRibbonY();
			xPoints[3] = lowerBar.getLeftX() + (int)lowerOffset.getValue(); yPoints[3] = lowerBar.getInRibbonY();

			g.fillPolygon(xPoints, yPoints, 4);
		} else {
			g.drawLine(upperBar.getLeftX() + (int)upperOffset.getValue(), upperBar.getOutRibbonY(), lowerBar.getLeftX() + (int)lowerOffset.getValue() + (int)width, lowerBar.getInRibbonY());
		}
	}
	
	public void displaySelected(Graphics2D g) {
		
		if (width == 0)
			return;
	
		if (width >= 1) {
			ColorBrewer.setColor(colorBrewerIndex, false, g);
			int xPoints[] = new int[4];
			int yPoints[] = new int[4];

			xPoints[0] = upperBar.getLeftX() + (int)upperOffset.getValue(); yPoints[0] = upperBar.getOutRibbonY();
			xPoints[1] = upperBar.getLeftX() + (int)upperOffset.getValue() + (int)Math.round(width); yPoints[1] = upperBar.getOutRibbonY();
			xPoints[2] = lowerBar.getLeftX() + (int)lowerOffset.getValue() + (int)Math.round(width); yPoints[2] = lowerBar.getInRibbonY();
			xPoints[3] = lowerBar.getLeftX() + (int)lowerOffset.getValue(); yPoints[3] = lowerBar.getInRibbonY();
			g.fillPolygon(xPoints, yPoints, 4);
				
			ColorBrewer.setColor(colorBrewerIndex, true, .8f, g);
			g.drawPolygon(xPoints, yPoints, 4);
		} else {
			ColorBrewer.setColor(colorBrewerIndex, true, g);
			g.drawLine(upperBar.getLeftX() + (int)upperOffset.getValue(), upperBar.getOutRibbonY(), lowerBar.getLeftX() + (int)lowerOffset.getValue() + (int)width, lowerBar.getInRibbonY());
		}
	}
	
	/**
	 * @param colorBrewerIndex the colorBrewerIndex to set
	 */
	public void setColorBrewerIndex(int colorBrewerIndex) {
		this.colorBrewerIndex = colorBrewerIndex;
	}
	
	public boolean contains(int x, int y) {
		
		Polygon poly = new Polygon();
		
		poly.addPoint((int)(upperBar.getLeftX() + upperOffset.getValue()), (int)upperBar.getOutRibbonY());
		poly.addPoint((int)(upperBar.getLeftX() + upperOffset.getValue() + width), (int)upperBar.getOutRibbonY());
		poly.addPoint((int)(lowerBar.getLeftX() + lowerOffset.getValue() + width), (int)lowerBar.getInRibbonY());
		poly.addPoint((int)(lowerBar.getLeftX() + lowerOffset.getValue()), (int)lowerBar.getInRibbonY());

		return poly.contains(x, y);
	}	
	
}
