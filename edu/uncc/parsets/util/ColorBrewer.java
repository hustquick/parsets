package edu.uncc.parsets.util;

import javax.media.opengl.GL;

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
 * Colors to be used for ribbons. Based on a color scheme from
 * http://colorbrewer.org/
 */
public class ColorBrewer {
	
	public final static float colors[][] = {
			{ 141f / 255f, 211f / 255f, 199f / 255f },
			{ 190f / 255f, 186f / 255f, 218f / 255f },
			{ 251f / 255f, 128f / 255f, 114f / 255f },
			{ 128f / 255f, 177f / 255f, 211f / 255f },
			{ 253f / 255f, 180f / 255f, 98f / 255f },
			{ 179f / 255f, 222f / 255f, 105f / 255f },
			{ 252f / 255f, 205f / 255f, 229f / 255f },
			{ 217f / 255f, 217f / 255f, 217f / 255f },
			{ 188f / 255f, 128f / 255f, 189f / 255f },
			{ 204f / 255f, 235f / 255f, 197f / 255f },
			{ 255f / 255f, 237f / 255f, 111f / 255f },
			{ 255f / 255f, 255f / 255f, 179f / 255f }};

	public static void setColor(int colorNum, boolean darker, GL gl) {
		colorNum = colorNum % colors.length;
		if (darker)
			gl.glColor3f(colors[colorNum][0]*.75f, colors[colorNum][1]*.75f, colors[colorNum][2]*.75f);
		else
			gl.glColor3f(colors[colorNum][0], colors[colorNum][1], colors[colorNum][2]);
	}
	
	public static void setColor(int colorNum, boolean darker, float alpha, GL gl) {
		colorNum = colorNum % colors.length;
		if (darker)
			gl.glColor4f(colors[colorNum][0]*.75f, colors[colorNum][1]*.75f, colors[colorNum][2]*.75f, alpha);
		else
			gl.glColor4f(colors[colorNum][0], colors[colorNum][1], colors[colorNum][2], alpha);
	}
	
}
