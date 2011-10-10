package edu.uncc.parsets.parsets;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import edu.uncc.parsets.data.CategoryHandle;
import edu.uncc.parsets.data.CategoryNode;
import edu.uncc.parsets.data.CategoryTree;
import edu.uncc.parsets.data.DimensionHandle;

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

public class VisualConnectionTree {

	private VisualConnection root; 
	private VisualConnection test;
	private RibbonLayoutStyle style = RibbonLayoutStyle.BRANCHING;

	public VisualConnectionTree() {
		super();
		root = new VisualConnection();
	}
	
	public void print(VisualConnection node) {
		System.out.println(node.toString());
		for (VisualConnection child : node.getChildren())
			print(child);
	}

	
	/**
	 * Build a visual tree based on a data tree without any layout information.
	 * 
	 * @param tree The categorical data tree on which to base the visual connections.
	 */
	public void buildConnectionTree(ArrayList<VisualAxis> axes, CategoryTree tree) {
		
		root = new VisualConnection();
		addChildren(root, tree.getRootNode(), axes, 1);
	}	
	
	private void addChildren(VisualConnection parentNode, CategoryNode dataNode, ArrayList<VisualAxis> axes, int childLevel) {

		if (childLevel == 1) {
			
			/* The first level is made up of invisible connections.
			 * These can be thought of as ribbons that represent only
			 * a single category in the first dimension, which then 
			 * branch out to form the lower ribbons.  They're not visible
			 * because they're redundant with the first level category
			 * bars, but they're convenient for treating the ribbons as
			 * a tree. 
			 */
			for (CategoryNode child : dataNode.getChildren()) {
				VisualConnection newChild = parentNode.addChild(new InvisibleConnection(parentNode, child));
				addChildren(newChild, child, axes, childLevel+1);
			}
			
			orderChildren(parentNode, ((CategoricalAxis)axes.get(0)).getCategoryOrder());
		} else if (childLevel <= axes.size()){
			
			// The lower levels are ribbons.
			
			for (CategoryNode child : dataNode.getChildren()) {
				VisualConnection newChild = parentNode.addChild(new BasicRibbon(parentNode, child, 
																(CategoricalAxis)axes.get(childLevel-2), 
																(CategoricalAxis)axes.get(childLevel-1)));

				addChildren(newChild, child, axes, childLevel+1);
			}
			
			orderChildren(parentNode, ((CategoricalAxis)axes.get(childLevel-1)).getCategoryOrder());
		}
	}
	
	private void orderChildren(VisualConnection parentNode,	List<CategoryHandle> categoryOrder) {
		
		ArrayList<VisualConnection> newList = new ArrayList<VisualConnection>();
		
		newList.addAll(parentNode.getChildren());
		
		parentNode.getChildren().clear();
		
		for (CategoryHandle cat : categoryOrder) {
			for (VisualConnection ribbon : newList) {
				if (ribbon.getNode().getToCategory().equals(cat)) {
					parentNode.getChildren().add(ribbon);
				}
			}
		}
	}
	
	public void orderChildren(DimensionHandle dimension, List<CategoryHandle> order) {
		orderChildren(dimension, order, root);
	}

	private void orderChildren(DimensionHandle dimension, List<CategoryHandle> order, VisualConnection node) {
		if (node.getChildren() == null)
			return;
		
		if (!node.getChildren().isEmpty() && node.getChildren().get(0).getNode().getToCategory().getDimension().equals(dimension))
			orderChildren(node, order);
		else
			for (VisualConnection vc : node.getChildren())
				orderChildren(dimension, order, vc);
	}

	
	public void doLayout(int minX, int maxX) {
		if (style == RibbonLayoutStyle.BRANCHING)
			doLayoutBranching(minX, maxX);
		else if (style == RibbonLayoutStyle.BUNDLED)
			doLayoutBundled(minX, maxX);
	}
	

	public void doLayoutBranching(int minX, int maxX) {
		root.setWidth(maxX - minX);
		layoutChildren(root);
		setColors(root);
	}

	public void layoutChildren(VisualConnection connectionNode) {		
		for (VisualConnection child : connectionNode.getChildren())
			if (child.getNode().isVisible()) {
				child.layout(connectionNode.getWidth());
				layoutChildren(child);
			}
	}

	/**
	 * Lays out the ribbons in a bundled style. Basically, this means doing a
	 * series of phased depth-first traversals on each of the top-level category
	 * nodes.
	 * 
	 * @param minX
	 *            The left bound of the display space.
	 * @param maxX
	 *            The right bound of the display space.
	 */
	public void doLayoutBundled(int minX, int maxX) {

		root.setWidth(maxX - minX);

		// Set all the layout and completeness flags to false.
		resetForBundling(root);

		// If there are no connections, return.
		if (!root.getChildren().isEmpty()) {

			// While the root is not set to complete...
			while (!root.isComplete()) {

				// Iterate through each of the invisible nodes, each of
				// which represents a top-level category bar. At each
				// step, lay out the leftmost incomplete branch of the
				// top-level node.
				for (VisualConnection invisible : root.getChildren()) {

					if (!invisible.isComplete())
						layoutBranch(invisible);

					// If this node is complete and is the last of the root's
					// children, then we're done.
					else if (invisible == root.getChildren().get(root.getChildren().size() - 1))
						root.setComplete(true);
				}
			}
		}
		setColors(root);
	}

	/**
	 * Lay out the leftmost incomplete branch of this connection node.
	 * 
	 * @param connectionNode
	 */
	public boolean layoutBranch(VisualConnection connectionNode) {

		// If this node isn't laid out already, lay it out.
		if (!connectionNode.isLaidOut()) {

			connectionNode.layout(connectionNode.getParent().getWidth());
			connectionNode.setLaidOut(true);

			// If this is a leaf, this node is completed.
			if (connectionNode.getChildren().isEmpty()) {
				connectionNode.setComplete(true);
				return true;
			}
		}

		// If this is not a leaf, call layoutBranch on this node's
		// leftmost incomplete child.
		for (VisualConnection child : connectionNode.getChildren())
			if (!child.isComplete())
				if (layoutBranch(child) == false)
					return false;

		// If all of the children are complete, this node is
		// completed.
		connectionNode.setComplete(true);
		
		return true;
	}
	
	public void display(Graphics2D g, float alpha) {
		display(g, root, alpha);
		displaySelected(g, root);
	}
	
	private void display(Graphics2D g, VisualConnection node, float alpha) {
		
		node.paint(g, alpha);
		
		for (VisualConnection child : node.getChildren())
			if (child.getNode().isVisible()) 
				display(g, child, alpha);
	}
	
	private void displaySelected(Graphics2D g, VisualConnection node) {
		if (node.isSelected())
			node.paintSelected(g);
		
		for (VisualConnection child : node.getChildren())
			if (child.getNode().isVisible())
				displaySelected(g, child);
	}

	public void setColors(VisualConnection connectionNode) {
		
		// The top level categorical axis determines the color scheme.
		if (connectionNode.getChildren().isEmpty())
			return;

		if (connectionNode == root) {
			for (VisualConnection childNode : connectionNode.getChildren()) {
				childNode.setColorBrewerIndex(childNode.getNode().getToCategory().getCategoryNum() - 1);
				setColors(childNode);
			}
		} else {
			for (VisualConnection childNode : connectionNode.getChildren()) {
				childNode.setColorBrewerIndex(connectionNode.getColorBrewerIndex());
				setColors(childNode);
			}
		}
	}
	
	public String highlightRibbon(int x, int y, CategoryTree dataTree) {
		clearSelection();
		VisualConnection selectedRibbon = highlightRibbon(x, y, root);
		if (selectedRibbon != null) {
			setSelected(selectedRibbon);
			return selectedRibbon.getTooltip(dataTree.getFilteredTotal());
		}
		return null;
	}
        
        public VisualConnection getRibbon(int x, int y, CategoryTree dataTree) {
		clearSelection();
		VisualConnection selectedRibbon = highlightRibbon(x, y, root);
		if (selectedRibbon != null) {
                        setSelected(selectedRibbon);
			return selectedRibbon;
		}
		return null;
	}
	
	private VisualConnection highlightRibbon(int x, int y, VisualConnection node) {
		
		VisualConnection returnNode = null;
		
		if (node.contains(x, y))
			returnNode = node;
		
		for (VisualConnection child : node.getChildren()) {
			VisualConnection temp = highlightRibbon(x, y, child);
			if (returnNode == null)
				returnNode = temp;
		}

		return returnNode;
	}

	/**
	 * Sets the selection flags on a given visual connection, as well as all its
	 * direct ancestors and descendants.
	 * 
	 * @param connection
	 *            The connection to highlight.
	 */
	public void setSelected(VisualConnection connection) {
		selectUp(connection);
		selectDown(connection);
	}

	private void selectDown(VisualConnection connection) {

		connection.setSelected(true);

		for (VisualConnection child : connection.getChildren())
			selectDown(child);
	}

	private void selectUp(VisualConnection connection) {

		connection.setSelected(true);

		if (!connection.equals(root))
			selectUp(connection.getParent());
	}
	
	public void clearSelection() {
		clearSelection(root);
	}

	private void clearSelection(VisualConnection node) {

		node.setSelected(false);
		
		for (VisualConnection child : node.getChildren()) 
			clearSelection(child);
	}

	public void clearConnections() {
		root = new VisualConnection();
	}
	
	public void resetForBundling(VisualConnection connectionNode) {
		connectionNode.setComplete(false);
		connectionNode.setLaidOut(false);
		
		for (VisualConnection child : connectionNode.getChildren())
			resetForBundling(child);
	}
	
	private void selectCategory(CategoryHandle category, VisualConnection node) {
	
		if (!node.equals(root) && node.getNode().getToCategory().equals(category))
			for (VisualConnection child : node.getChildren())
				selectDown(child);
		else
			for (VisualConnection child : node.getChildren())
				selectCategory(category, child);
	}

	public void selectCategory(CategoryHandle category) {
		clearSelection();
		selectCategory(category, root);
	}
	
	
	// added in for activecategorybar selection instead of one ribbon
	public VisualConnection getCategoryBarNode(CategoryHandle category){
		System.out.println("Category is" + category.getName());
		findCategoryBarNode(category, root);
		return test;
		
	}
	
	public void findCategoryBarNode(CategoryHandle category, VisualConnection node){
		if (!node.equals(root) && node.getNode().getToCategory().equals(category)){
			test = node;
		}
		else
			for (VisualConnection child : node.getChildren())
				findCategoryBarNode(category, child);
	

	}
	
	/**
	 * @param style the style to set
	 */
	public void setStyle(RibbonLayoutStyle style) {
		this.style = style;
	}
	
	public void moveCategory(DimensionHandle dimension, CategoryHandle category, int index) {
		moveCategory(dimension, category, index, root);
	}

	private void moveCategory(DimensionHandle dimension, CategoryHandle category, int index, VisualConnection node) {
		
		if (node.getChildren() == null)
			return;
		
		ArrayList<VisualConnection> children = node.getChildren();
		
		if (!children.isEmpty() && children.get(0).getNode().getToCategory().getDimension().equals(dimension)) {
			
			VisualConnection moveCat = null; 
			
			for (VisualConnection child : children)
				if (child.getNode().getToCategory().equals(category))
					moveCat = child;
			
			if (moveCat != null) {
				children.remove(moveCat);
				if (index < children.size()) 
					children.add(index, moveCat);
				else 
					children.add(moveCat);
			}
		} else {
			for (VisualConnection child : children) {
				moveCategory(dimension, category, index, child);
			}
		}		
	}

}
