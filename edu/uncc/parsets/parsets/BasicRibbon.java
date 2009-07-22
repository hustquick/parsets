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
		
		this.upperBar = upperAxis.getCategoryBar(node.getParent().getToCategory());
		this.lowerBar = lowerAxis.getCategoryBar(node.getToCategory());
	}
	
	public BasicRibbon(CategoryNode categoryNode, CategoricalAxis upperAxis, CategoricalAxis lowerAxis) {
		
		node = categoryNode;

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
		}
		
		else {
		
			width = parentWidth * node.getRatio();
			
			upperOffset.setValue(upperBar.getBottomIndexPoint());
			upperBar.setBottomIndexPoint(upperOffset.getValue() + width);

			lowerOffset.setValue(lowerBar.getTopIndexPoint());
			lowerBar.setTopIndexPoint(lowerOffset.getValue() + width);

		}

	}
	
	
	public void display(GL gl, float alpha) {
		
		if (width == 0 || isSelected) {
			return;
		}

		ColorBrewer.setColor(colorBrewerIndex, false, alpha, gl);

		if (width >= 1) {
			gl.glEnable(GL.GL_POLYGON_SMOOTH);
			gl.glBegin(GL.GL_QUADS);
				gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue(), upperBar.getOutRibbonY());
				gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue() + width, upperBar.getOutRibbonY());
				gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue() + width, lowerBar.getInRibbonY());
				gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue(), lowerBar.getInRibbonY());
			gl.glEnd();
			gl.glDisable(GL.GL_POLYGON_SMOOTH);
		}
		
		else {
			gl.glEnable(GL.GL_LINE_SMOOTH);
			gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue(), upperBar.getOutRibbonY());
			gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue() + (int)width, lowerBar.getInRibbonY());
			gl.glEnd();
			gl.glDisable(GL.GL_LINE_SMOOTH);
		}		

//		gl.glDisable(GL.GL_BLEND);
		
/*		if (selectionPercentage > 0) {
			
			int lowerY;
			
			if (OLD_Controller.drawStyle == DrawStyle.classic)
				lowerY = lowerAxis.topY;
			else
				lowerY = lowerAxis.bottomY;

			Polygon selectedPoly = new Polygon();
			
			selectedPoly.addPoint(upperBar.getX() + (int)upperOffset, (int)upperAxis.bottomY);
			selectedPoly.addPoint((int)(upperBar.getX() + upperOffset + (width * selectionPercentage)), (int)upperAxis.bottomY);
			selectedPoly.addPoint((int)(lowerBar.getX() + lowerOffset + (width * selectionPercentage)), (int)lowerY);
			selectedPoly.addPoint(lowerBar.getX() + (int)lowerOffset, (int)lowerY);
			
			g.setColor(ColorBrewer.getColor(colorBrewerIndex, 0.9f));
			
			g.fillPolygon(selectedPoly);
			
			g.setColor(ColorBrewer.getColor(colorBrewerIndex, 0.7f).darker());
			
			g.drawPolygon(polygon);
			
		}
		
		*/

	}
	
	public void displaySelected(GL gl) {
		
		if (width == 0) {
			return;
		}
	
		if (width >= 1) {
			ColorBrewer.setColor(colorBrewerIndex, false, 0.9f, gl);
			
			gl.glEnable(GL.GL_POLYGON_SMOOTH);
			gl.glBegin(GL.GL_QUADS);
				gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue(), upperBar.getOutRibbonY());
				gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue() + width, upperBar.getOutRibbonY());
				gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue() + width, lowerBar.getInRibbonY());
				gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue(), lowerBar.getInRibbonY());
			gl.glEnd();
			gl.glDisable(GL.GL_POLYGON_SMOOTH);
			
			ColorBrewer.setColor(colorBrewerIndex, true, 0.9f, gl);
			
			gl.glEnable(GL.GL_LINE_SMOOTH);
			gl.glBegin(GL.GL_LINE_LOOP);
				gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue(), upperBar.getOutRibbonY());
				gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue() + width, upperBar.getOutRibbonY());
				gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue() + width, lowerBar.getInRibbonY());
				gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue(), lowerBar.getInRibbonY());
			gl.glEnd();
			gl.glDisable(GL.GL_LINE_SMOOTH);
			
		}
		
		else {
			ColorBrewer.setColor(colorBrewerIndex, true, 0.9f, gl);
			gl.glEnable(GL.GL_LINE_SMOOTH);
			gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(upperBar.getLeftX() + upperOffset.getValue(), upperBar.getOutRibbonY());
			gl.glVertex2f(lowerBar.getLeftX() + lowerOffset.getValue() + (int)width, lowerBar.getInRibbonY());
			gl.glEnd();
			gl.glDisable(GL.GL_LINE_SMOOTH);
		}
		
	}
	
/*	public void handleMouseEvent(MouseEvent me) {

		if (me.getID() == MouseEvent.MOUSE_MOVED) {

			if (polygon != null) {
			
				if (polygon.contains((double)me.getX(), (double)me.getY())) {
					
					String tooltip = node.toString();
					tooltip += ": " + node.getValue();
					
					OLD_Controller.controller.getLayout().setToolTip(tooltip, me.getX(), me.getY());		
					
				}
			
			}			
		
		}
		
		if (me.getID() == MouseEvent.MOUSE_CLICKED) {

			if (polygon != null) {
			
				if (polygon.contains((double)me.getX(), (double)me.getY())) {
					
					//controller.getLayout().setLayoutInvalid();
					EventService.getEventService().fireSelectedNodeChangeEvent(
							new SelectedNodeChangeEvent(this, node));
					System.err.println("setSelected()" + node.getPointList().size());
					
				}
			
			}			
			
		}

	}
*/

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
//	
//	
//
//	
//	public void setSelectionPercentage(float selectionPercentage) {
//		this.selectionPercentage = selectionPercentage;
//	}
//	
//	public Point getCenterPoint() {
//		
//		Point pt = new Point();
//		
//		pt.x = (int)polygon.getBounds().getCenterX();
//		pt.y = (int)polygon.getBounds().getCenterY();
//		
//		return pt;		
//		
//	}

	
	
}
