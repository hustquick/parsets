package edu.uncc.parsets.data.old;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.uncc.parsets.data.DataType;
import edu.uncc.parsets.data.old.MetaData.DimensionMetaData;

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
 * Parser for XML file that describes categories. Currently only used for bulk
 * conversion of data sets.
 */
public class MetaDataParser extends DefaultHandler {


	MetaData metaData;
	
	private DimensionMetaData currentDimension;

	private String categoryName;
	
	private StringBuffer characters = new StringBuffer();
	
	private String fileName;
	
	public MetaData parse(String filename) {
		try {
			fileName = filename;
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(filename, this);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return metaData;
	}
	
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		if (qName.equals("cat")) {
			categoryName = atts.getValue("id").trim();
			characters.setLength(0);
			
		} else if (qName.equals("dim")) {
			String dimKey = atts.getValue("id").trim();
			DataType type = DataType.categorical;
			String typeString = atts.getValue("type");
			if (typeString != null)
				type = DataType.typeFromString(typeString);
			String dateFormat = atts.getValue("format");
			currentDimension = new MetaData.DimensionMetaData(type, dateFormat);
			metaData.addDimension(dimKey, currentDimension);

		} else if (qName.equals("name")) {
			characters.setLength(0);

		} else if (qName.equals("desc")) {
			characters.setLength(0);
			
		} else if (qName.equals("meta")) {
			metaData = new MetaData();
			if (atts.getValue("name") != null)
				metaData.setName(atts.getValue("name"));
			else {
				String name = (new File(fileName)).getName();
				int lastPeriod = name.lastIndexOf('.');
				if (lastPeriod > 0)
					name = name.substring(0, lastPeriod);
				metaData.setName(name);
			}
			if (atts.getValue("section") != null)
				metaData.setSection(atts.getValue("section"));
			if (atts.getValue("source") != null)
				metaData.setSource(atts.getValue("source"));
			if (atts.getValue("srcURL") != null)
				metaData.setSrcURL(atts.getValue("srcURL"));
		} else if (qName.equals("user")) {
			// ignore for now
		
		} else {
			System.err.println("Unknown tag '"+qName+"', aborting.");
			metaData = null;
		}
	}
	
	public void endElement(String uri, String localName, String qName) {
		if (qName.equals("cat")) {
			currentDimension.addCategory(categoryName, characters.toString().trim());

		} else if (qName.equals("name")) {
			currentDimension.setName(characters.toString().trim());

		} else if (qName.equals("desc")) {
			currentDimension.setDescription(characters.toString().trim());
		}
	}
	
	public void characters(char[] ch, int start, int length) {
		characters.append(String.copyValueOf(ch, start, length));
	}

}
