package edu.uncc.parsets.data.old;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import edu.uncc.parsets.data.DataType;
import edu.uncc.parsets.util.PSLogging;


/**
 * Parse metadata in XML format. This is only for bulk conversion.
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
			characters.append(String.copyValueOf(ch, start, length));
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
