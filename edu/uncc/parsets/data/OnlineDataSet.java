package edu.uncc.parsets.data;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

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
 * Minimal wrapper around parsed JSON data to represent online datasets that
 * can be downloaded.
 */
public class OnlineDataSet extends DataSet {

	private Map<String, Object> jsonData;
	private String handle;
		
	public OnlineDataSet(String dsHandle, Map<String, Object> jsonObject) {
		jsonData = jsonObject;
		handle = dsHandle;
		name = (String)jsonData.get("name");
	}

	@Override
	public String getHandle() {
		return handle;
	}
	
	@Override
	public DefaultMutableTreeNode getCategoricalDimensionsAsTree() {
		return null;
	}

	@Override
	public int getCount(CategoryHandle[] categories) {
		return 0;
	}

	@Override
	public int getNumCategoricalDimensions() {
		return ((Long)jsonData.get("categorical")).intValue();
	}

	@Override
	public int getNumDimensions() {
		return getNumCategoricalDimensions()+getNumNumericDimensions();
	}

	@Override
	public int getNumNumericDimensions() {
		return ((Long)jsonData.get("numerical")).intValue();
	}

	@Override
	public int getNumRecords() {
		return ((Long)jsonData.get("items")).intValue();
	}

	@Override
	public DimensionHandle[] getNumericDimensions() {
		return null;
	}

	@Override
	public String getSection() {
		return (String)jsonData.get("section");
	}

	public String getSource() {
		return (String)jsonData.get("source");
	}

	public String getSrcURL() {
		return (String)jsonData.get("srcURL");
	}

	@Override
	public float getSum(CategoryHandle[] categories,
			DimensionHandle numericalDim) {
		return 0;
	}

	@Override
	public CategoryTree getTree(List<DimensionHandle> dimensions) {
		return null;
	}

	@Override
	public String getURL() {
		return (String)jsonData.get("url");
	}

	@Override
	public Iterator<DimensionHandle> iterator() {
		return null;
	}

}
