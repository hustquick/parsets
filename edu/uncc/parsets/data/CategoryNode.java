package edu.uncc.parsets.data;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

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

public class CategoryNode implements Iterable<CategoryNode>, Comparable<CategoryNode> {

	private int count;
	
	private float ratio;
	
	private CategoryHandle toCategory;
	
	private boolean visible;
	
	private CategoryNode parent;
	
	private ArrayList<CategoryNode> children = new ArrayList<CategoryNode>();

	private String pathName;
	
	protected CategoryNode(CategoryNode parent, CategoryHandle toCategory, int count) {
		this.parent = parent;
		this.toCategory = toCategory;
		this.count = count;
		if (parent != null) {
			parent.addChild(this);
			pathName = parent.pathName+"/"+toCategory.getName();
		} else
			pathName = "//";
		visible = true;
	}
	
	public int getCount() {
		return count;
	}

	public float getRatio() {
		return ratio;
	}
	
	public String getTooltipText(int filteredTotal) {		
		CategoryNode node = this;
		ArrayList<CategoryHandle> cats = new ArrayList<CategoryHandle>();
		
		while (node.getParent() != null) {			
			cats.add(0, node.getToCategory());
			node = node.getParent();			
		}
		String s = "";
		for(CategoryHandle category : cats) {
			s += category.getName();
			if (cats.indexOf(category) < cats.size()-1) {
				s += ", ";
			}
		}
		
		s += "\n" + count + ", ";
		
		float percentage = ((float)count/(float)filteredTotal)*100f;
		
		if (percentage < 1) {			
			NumberFormat f = NumberFormat.getInstance();
			f.setMaximumFractionDigits(2);			
			s += f.format(percentage) + "%";
		}
		else 
			s += (int)percentage + "%";

		return s;
		
	}

	public CategoryHandle getToCategory() {
		return toCategory;
	}
	
	public void addChild(CategoryNode n) {
		children.add(n);
	}

	public Iterator<CategoryNode> iterator() {
		return children.iterator();
	}

	public CategoryNode getParent() {
		return parent;
	}

	public void updateValues() {
		if (children.size() > 0) {
			count = 0;
			for (CategoryNode n : children) {
				if (n.isVisible()) {
					n.updateValues();
					count += n.count;
				}
			}
			for (CategoryNode n : children) {
				if (n.isVisible()) 
					n.ratio = (float)n.count/(float)count;
				
			}			
		}
	}
	
	public void print() {
		System.err.println(pathName+" => "+count);
		for (CategoryNode n : children)
			n.print();
	}

	/**
	 * @return the children
	 */
	public ArrayList<CategoryNode> getChildren() {
		return children;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		for (CategoryNode child : children) {
			child.setVisible(visible);
		}
	}

	@Override
	public int compareTo(CategoryNode o) {
		return pathName.compareTo(o.pathName);
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof CategoryNode) && compareTo((CategoryNode)o) == 0;
	}
	
	@Override
	public int hashCode() {
		assert false : "hashCode not designed";
		return 42; // any arbitrary constant will do
	}
}
