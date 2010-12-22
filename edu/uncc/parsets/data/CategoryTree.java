package edu.uncc.parsets.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

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

public class CategoryTree implements Iterable<List<CategoryNode>> {

	private ArrayList<List<CategoryNode>> levels;

	private ArrayList<TreeMap<CategoryHandle, List<CategoryNode>>> catGroups;

	private CategoryNode rootNode;
	
	public CategoryTree(int numLevels) {
		levels = new ArrayList<List<CategoryNode>>(numLevels);
		catGroups = new ArrayList<TreeMap<CategoryHandle,List<CategoryNode>>>(numLevels);
		for (int i = 0; i < numLevels; i++) {
			levels.add(new ArrayList<CategoryNode>());
			catGroups.add(new TreeMap<CategoryHandle, List<CategoryNode>>());
		}
	}
	
	public void addtoLevel(int level, CategoryNode node) {
		List<CategoryNode> levelList = levels.get(level);
		levelList.add(node);
		TreeMap<CategoryHandle, List<CategoryNode>> group = catGroups.get(level);
		if (level == 0)
			rootNode = node;
		else {
			List<CategoryNode> catList = group.get(node.getToCategory());
			if (catList == null) {
				catList = new ArrayList<CategoryNode>();
				group.put(node.getToCategory(), catList);
			}
			catList.add(node);
		}
	}
	
	public List<CategoryNode> getLevelList(int level) {
		return levels.get(level);
	}

	public List<CategoryNode> getNodesForCategory(int level, CategoryHandle category) {
		return catGroups.get(level).get(category);
	}
	
	public Iterator<List<CategoryNode>> iterator() {
		return levels.iterator();
	}

	public CategoryNode getRootNode() {
		return rootNode;
	}
	
	public void print() {
		rootNode.print();
	}
	
	public float getFilteredFrequency(CategoryHandle cat) {
		
		float total = 0;
		
		List<CategoryNode> nodes = getLevelList(levels.size()-1);

		for(CategoryNode node : nodes) {
			if (node.isVisible()) 
				total += node.getCount();
		}

		return (float)getFilteredCount(cat)/total;
	}

	public int getFilteredCount(CategoryHandle cat) {
		
		int count = 0;

		for (int i=0; i<levels.size(); i++) {
			List<CategoryNode> nodes = getNodesForCategory(i, cat);
			if (nodes != null)
				for (CategoryNode node : nodes)
					if (node.isVisible()) 
						count += node.getCount();
		}
		
		return count;
	}
	
	public int getFilteredTotal() {
		int total = 0;
		
		List<CategoryNode> nodes = getLevelList(levels.size()-1);

		for(CategoryNode node : nodes) {
			if (node.isVisible()) 
				total += node.getCount();
		}
		
		return total;

	}
	
}
