package edu.uncc.parsets.data.old;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import edu.uncc.parsets.data.DataType;
import edu.uncc.parsets.util.PSLogging;

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
 * Parse metadata in XML format. This is currently only for bulk conversion.
 */
public class MetaDataParser {

	private CSVDataSet dataSet;

	private class MetaXMLHandler extends DefaultHandler {
		private String categoryKey;
		
		private StringBuilder characters = new StringBuilder();
		
		private DataDimension currentDimension;

		public void startElement(String uri, String localName, String qName, Attributes atts) {
			if (qName.equals("cat")) {
				categoryKey = atts.getValue("id").trim();
				characters.setLength(0);
				
			} else if (qName.equals("dim")) {
				String dimKey = atts.getValue("id").trim();
				DataType type = DataType.categorical;
				String typeString = atts.getValue("type");
				if (typeString != null)
					type = DataType.typeFromString(typeString);
				
				currentDimension = new DataDimension(dimKey, type);
				dataSet.addDimension(dimKey, currentDimension);

			} else if (qName.equals("name")) {
				characters.setLength(0);

			} else if (qName.equals("desc")) {
				characters.setLength(0);
				
			} else if (qName.equals("meta")) {
				if (atts.getValue("name") != null)
					dataSet.setName(atts.getValue("name"));
				if (atts.getValue("section") != null)
					dataSet.setSection(atts.getValue("section"));
				if (atts.getValue("source") != null)
					dataSet.setSource(atts.getValue("source"));
				if (atts.getValue("srcURL") != null)
					dataSet.setSourceURL(atts.getValue("srcURL"));
			} else if (qName.equals("user")) {
				// ignore for now		
			} else {
				PSLogging.logger.warn("Unknown tag encountered: "+qName);
			}
		}
		
		public void endElement(String uri, String localName, String qName) {
			if (qName.equals("cat")) {
				currentDimension.addCategory(categoryKey, characters.toString().trim());

			} else if (qName.equals("name")) {
				currentDimension.setName(characters.toString().trim());

			} else if (qName.equals("desc")) {
				// ignore
			}
		}
		
		public void characters(char[] ch, int start, int length) {
			characters.append(ch, start, length);
		}
	}
	
	
	public boolean parse(CSVDataSet csvDataSet, String filename) {
		dataSet = csvDataSet;
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(filename, new MetaXMLHandler());
		} catch (Exception e) {
			PSLogging.logger.error("Error parsing metadata file "+filename, e);
			return false;
		}
		return true;
	}
}
