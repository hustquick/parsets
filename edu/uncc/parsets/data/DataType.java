package edu.uncc.parsets.data;

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
 * The type of data per dimension. Each DimensionDescriptor know its data type, so we can treat it
 * correctly.
 * 
 */
public enum DataType {

	/**
	 * Categorical data. The values are read and treated as strings, and there are only a few of them.
	 * There is no inherent ordering.
	 */
	categorical,

	/**
	 * Numerical data. This type of data is continuous, and there are many different values. The data
	 * is treated as floats internally, even if they really are integers, to keep things simpler.
	 */
	numerical,

	/**
	 * Textual data. This is similar to categorical, but there are more strings and we treat them as
	 * text, so there is a possible ordering.
	 */
	textual;
	
	public static final String dataTypeNames[] = {"Categories", "Numbers", "Text"};
	
	public static DataType typeFromString(String typeString) {
		if (typeString.equals("categorical"))
			return categorical;
		else if (typeString.equals("int") || typeString.equals("float"))
			return numerical;
		else if (typeString.equals("text"))
			return textual;
		else
			return null;
	}
}
