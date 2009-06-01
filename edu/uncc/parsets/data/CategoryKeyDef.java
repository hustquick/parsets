package edu.uncc.parsets.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class CategoryKeyDef {

	private static final long bitMasks[] = {
			0x0000, 0x0001, 0x0003, 0x0007, 0x000f, 0x001f, 0x003f, 0x007f, 0x00ff,
				    0x01ff, 0x03ff, 0x07ff, 0x0fff, 0x1fff, 0x3fff, 0x7fff, 0xffff
	};
	
	private int leftShiftAmounts[];
	private long masks[];
	
	public CategoryKeyDef(int numCategories[]) {
		leftShiftAmounts = new int[numCategories.length];
		masks = new long[numCategories.length];
		
		int leftShift = 0;
		for (int i = 0; i < numCategories.length; i++) {
			int bits = numBits(numCategories[i]+1);
			leftShiftAmounts[i] = leftShift;
			masks[i] = bitMasks[bits] << leftShift;
			leftShift += bits;
		}
	}

	public CategoryKeyDef(List<Integer> leftShifts, List<Integer> maskList) {
		leftShiftAmounts = new int[leftShifts.size()];
		masks = new long[leftShifts.size()];
		Iterator<Integer> ls = leftShifts.iterator();
		Iterator<Integer> ml = maskList.iterator();
		for (int i = 0; i < leftShiftAmounts.length; i++) {
			leftShiftAmounts[i] = ls.next();
			masks[i] = ((long)ml.next()) << leftShiftAmounts[i];
		}
	}
		
	public String maskToString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < masks.length; i++) {
			sb.append(Long.toHexString(masks[i] >> leftShiftAmounts[i]));
			if (i < masks.length-1)
				sb.append(':');
		}
		return sb.toString();
	}
		
	public static int numBits(int num) {
		int bits = 0;
		while (num > 0) {
			bits++;
			num >>= 1;
		}
		return bits;
	}
	
	public int getLeftShift(int dimNum) {
		return leftShiftAmounts[dimNum];
	}
	
	public long getBitMask(int dimNum) {
		return masks[dimNum];
	}
	
	public long getBitMask(List<DimensionHandle> dimensions) {
		long mask = 0L;
		for (DimensionHandle dim : dimensions)
			mask |= masks[dim.getNum()];
		return mask;
	}
	
	public int getNumComponents() {
		return masks.length;
	}

	public List<List<CategoryNode>> groupBy(List<DimensionHandle> dimensions, List<CategoryNode> cats) {
		long mask = getBitMask(dimensions);
		List<List<CategoryNode>> groups = new ArrayList<List<CategoryNode>>();
		for (CategoryNode cc : cats) {
			List<CategoryNode> useList = null;
			for (List<CategoryNode> ccList : groups) {
				if (ccList.get(0).getKey().equals(cc.getKey(), mask)) {
					useList = ccList;
					break;
				}
			}
			if (useList == null) {
				useList = new ArrayList<CategoryNode>();
				groups.add(useList);
			}
			useList.add(cc);
		}
		return groups;
	}
	
}
