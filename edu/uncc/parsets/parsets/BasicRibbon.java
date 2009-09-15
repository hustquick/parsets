package edu.uncc.parsets.parsets;

import java.awt.Polygon;

import javax.media.opengl.GL;

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
	
	public void display(GL gl, float alpha) {
		
		if (width == 0 || isSelected)
			return;

		ColorBrewer.setColor(colorBrewerIndex, false, alpha, gl);
		if (width >= 1) {
			gl.glBegin(GL.GL_QUADS);
				gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue(), upperBar.getOutRibbonY());
				gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue() + width, upperBar.getOutRibbonY());
				gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue() + width, lowerBar.getInRibbonY());
				gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue(), lowerBar.getInRibbonY());
			gl.glEnd();
		} else {
			gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue(), upperBar.getOutRibbonY());
			gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue() + (int)width, lowerBar.getInRibbonY());
			gl.glEnd();
		}
	}
	
	public void displaySelected(GL gl) {
		
		if (width == 0)
			return;
	
		if (width >= 1) {
			ColorBrewer.setColor(colorBrewerIndex, false, 1f, gl);
			gl.glBegin(GL.GL_QUADS);
				gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue(), upperBar.getOutRibbonY());
				gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue() + width, upperBar.getOutRibbonY());
				gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue() + width, lowerBar.getInRibbonY());
				gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue(), lowerBar.getInRibbonY());
			gl.glEnd();
			
			ColorBrewer.setColor(colorBrewerIndex, true, .8f, gl);
			gl.glBegin(GL.GL_LINE_LOOP);
				gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue(), upperBar.getOutRibbonY());
				gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue() + width, upperBar.getOutRibbonY());
				gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue() + width, lowerBar.getInRibbonY());
				gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue(), lowerBar.getInRibbonY());
			gl.glEnd();
		} else {
			ColorBrewer.setColor(colorBrewerIndex, true, 1f, gl);
			gl.glEnable(GL.GL_LINE_SMOOTH);
			gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue(), upperBar.getOutRibbonY());
			gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue() + (int)width, lowerBar.getInRibbonY());
			gl.glEnd();
			gl.glDisable(GL.GL_LINE_SMOOTH);
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
