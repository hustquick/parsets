package edu.uncc.parsets.data;

import java.util.Collection;

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
 * This class encapsulates a category key, which describes a combination of
 * categories that select a particular subset of the data. If all dimensions
 * have values, a unique category combination is selected. If values are not
 * filled in, the undefined dimensions are aggregated.
 *
 */
public class CategoryKey implements Comparable<CategoryKey> {

	private long key;
	
	private CategoryKeyDef keyDef;
	
	public CategoryKey(long key, CategoryKeyDef keyDef) {
		this.key = key;
		this.keyDef = keyDef;
	}
	
	public CategoryKey(Collection<CategoryHandle> categories, CategoryKeyDef keyDef) {
		this.keyDef = keyDef;
		key = 0L;
		for (CategoryHandle cat : categories)
			key |= cat.getKey().key << keyDef.getLeftShift(cat.getDimension().getNum());
	}

	public CategoryKey(int categories[], CategoryKeyDef keyDef) {
		this.keyDef = keyDef;
		key = 0;
		for (int i = 0; i < categories.length; i++)
			key |= categories[i] << keyDef.getLeftShift(i);			
	}
	
	public CategoryKey(CategoryKey parentKey, CategoryHandle toCategory) {
		keyDef = parentKey.keyDef;
		key = parentKey.key | toCategory.getKey().key;
	}

	/**
	 * Compare two keys for equality of the bits specified in the bit mask.
	 * 
	 * @param other Key to compare with
	 * @param mask Bitmask to apply. Bits that are 1 here will be compared, bits that are 0 will be ignored.
	 * @return true if the masked bits are equal, false otherwise
	 */
	public boolean equals(CategoryKey other, long mask) {
		return (key & mask) == (other.key & mask);
	}
	
	public String toString() {
		if (keyDef == null)
			return "=NULL=";
		else {
			StringBuffer sb = new StringBuffer();
			int numComponents = keyDef.getNumComponents();
			for (int i = 0; i < numComponents; i++) {
				sb.append(Long.toHexString((key & keyDef.getBitMask(i)) >> keyDef.getLeftShift(i)));
				if (i < numComponents-1)
					sb.append(':');
			}
			return sb.toString();
		}
	}

	public CategoryKeyDef getKeyDef() {
		return keyDef;
	}
	
	public int compareTo(CategoryKey o) {
		long diff = key-o.key;
		if (diff > 0L)
			return 1;
		else if (diff < 0L)
			return -1;
		else
			return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CategoryKey)
			return compareTo((CategoryKey)o) == 0;
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		assert false : "hashCode not designed";
		return 42; // any arbitrary constant will do
	}
	
	/**
	 * Get this key as a long. Only to be used by DB code, will change when/if
	 * we switch to BigInteger for the numeric key.
	 * 
	 * @return the key value as a long
	 */
	protected long getKeyValue() {
		return key;
	}

}
