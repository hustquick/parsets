package edu.uncc.parsets.parsets;

import java.awt.Color;
import java.util.ArrayList;

import javax.media.opengl.GL;

import edu.uncc.parsets.data.CategoryNode;

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
 * The generic VisualConnection class, which connects two VisualAxes in a form
 * determined by their respective data types.
 */
public class VisualConnection {
	
	protected ArrayList<VisualConnection> children;
	protected VisualConnection parent;
	protected CategoryNode node;
	
	protected float width;
	protected int colorBrewerIndex;	
	protected boolean isSelected = false;
	
	// Flags for the bundled layout traversal.
	
	protected boolean isLaidOut = false;
	protected boolean isComplete = false;
	
	public VisualConnection() {
		parent = null;
		children = new ArrayList<VisualConnection>();
	}
	
	public VisualConnection(VisualAxis upperAxis, VisualAxis lowerAxis) {
		
	}
	
	public VisualConnection(VisualConnection parent) {
		this.parent = parent;
		children = new ArrayList<VisualConnection>();
	}

	/**
	 * Initialize the visual parameters for this element.
	 */
	public void layout() {
		
	}
	
	
	/**
	 * Initialize the visual parameters for this element.
	 */
	public void layout(float parentWidth) {
		
	}
	
	/**
	 * Paints the element to screen.
	 * 
	 * @param gl: The graphics instance to paint to.
	 */
	public void display(GL gl) {
		
	}
	
	/**
	 * Sets the colors of this element based on their value in the selected dimension.  This is 
	 * the topmost, or first-added, dimension by default, but should be changeable eventually.
	 * @param selectedDimensionIndex: Index in the dataset of the selected dimension.
	 */
	public void setColors(int selectedDimensionIndex) {
		
	}
	
	/**
	 * Clear the layout.
	 */
	public void clear() {
		
	}

//	protected void handleMouseEvent(MouseEvent me) {
//		
//	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(float width) {
		this.width = width;
	}



	/**
	 * @return the colorBrewerIndex
	 */
	public int getColorBrewerIndex() {
		return colorBrewerIndex;
	}

	/**
	 * @param colorBrewerIndex the colorBrewerIndex to set
	 */
	public void setColorBrewerIndex(int colorBrewerIndex) {
		this.colorBrewerIndex = colorBrewerIndex;
	}

	public boolean contains(int x, int y) {
		
		return false;
		
	}
	
	public void displaySelected(GL gl) {
		
	}

	/**
	 * @return the isSelected
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * @param isSelected the isSelected to set
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
//	public void updateFilter(DataFilter filter) {
//		
//	}

	public boolean isLaidOut() {
		return isLaidOut;
	}

	public void setLaidOut(boolean isLaidOut) {
		this.isLaidOut = isLaidOut;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}
	
	public Color desaturate(Color c) {
		
		float hsb[] = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
		
		Color desat = Color.getHSBColor(hsb[0], hsb[1] - 0.1f, hsb[2]);
		
		Color finalColor = new Color(desat.getRed(), desat.getGreen(), desat.getBlue(), c.getAlpha());
		
		return finalColor;
		
	}
	
	public Color saturate(Color c) {
		
		float hsb[] = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
		
		Color sat = Color.getHSBColor(hsb[0], hsb[1] + 0.1f, hsb[2]);
		
		Color finalColor = new Color(sat.getRed(), sat.getGreen(), sat.getBlue(), c.getAlpha());
		
		return finalColor;
		
	}

	/**
	 * @return the width
	 */
	public float getWidth() {
		return width;
	}
	
	public VisualConnection addChild(VisualConnection child) {
		children.add(child);
		return child;
	}
	
	public VisualConnection getParent() {
		return parent;
	}
	
	public void setParent(VisualConnection parent) {
		this.parent = parent;
	}
	
	public ArrayList<VisualConnection> getChildren() {
		return children;
	}

	/**
	 * @return the node
	 */
	public CategoryNode getNode() {
		return node;
	}

	/**
	 * @param node the node to set
	 */
	public void setNode(CategoryNode node) {
		this.node = node;
	}
	
	public String getTooltip(int filteredTotal) {
		return "";
	}
	
}
