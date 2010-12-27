package edu.uncc.parsets.parsets;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

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

public class Tooltip {

	private int x;
	private int y;
	private String text;
	
	public Tooltip(String text, int x, int y) {
		this.text = text;
		this.x = x;
		this.y = y;
	}
	
	public void display(Graphics2D g, Font tooltipFont, FontMetrics tooltipFontMetrics, int width) {
		
		if (text == null)
			return;
		
		g.setColor(new Color(.8f, .8f, .8f, .8f));

		String tok[] = text.split("\n");
		int maxWidth = 0;
		for (int i=0; i<tok.length; i++) {
			if (tooltipFontMetrics.stringWidth(tok[i]) > maxWidth) 
				maxWidth = tooltipFontMetrics.stringWidth(tok[i]);
		}	
		
		if (x + maxWidth > width) 
			x -= maxWidth;
		
		if (y - 13 < 0) 
			y += 13;

		g.fillRect(x, y + 10, maxWidth + 5, tooltipFontMetrics.getHeight()*tok.length + 3);
		
		g.setFont(tooltipFont);
		g.setColor(Color.BLACK);
		if (text.length() > 0) {
			for (int i = 0; i < tok.length; i++) {
				g.drawString(tok[i], x + 2, y + tooltipFontMetrics.getHeight()*(i+1) + 8);
			}
		}
	}
	
	public void newValues(String newText, int newX, int newY) {
		text = newText;
		x = newX;
		y = newY;
	}
}
