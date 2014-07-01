package org.developerworks.android;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DomFeedParser extends BaseFeedParser {

	protected DomFeedParser(String feedUrl) {
		super(feedUrl, null);
	}

	public List<Message> parse() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		List<Message> messages = new ArrayList<Message>();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(this.getInputStream());
			Element root = dom.getDocumentElement();
			NodeList items = root.getElementsByTagName(ITEM);
			for (int i=0;i<items.getLength();i++){
				Message message = new Message();
				Node item = items.item(i);
				NodeList properties = item.getChildNodes();
				for (int j=0;j<properties.getLength();j++){
					Node property = properties.item(j);
					String name = property.getNodeName();
					if (name.equalsIgnoreCase(TITLE)){
						message.setTitle(property.getFirstChild().getNodeValue());
					} else if (name.equalsIgnoreCase(LINK)){
						message.setLink(property.getFirstChild().getNodeValue());
					} else if (name.equalsIgnoreCase(DESCRIPTION)){
						StringBuilder text = new StringBuilder();
						NodeList chars = property.getChildNodes();
						for (int k=0;k<chars.getLength();k++){
							text.append(chars.item(k).getNodeValue());
						}
						message.setDescription(text.toString());
					} else if (name.equalsIgnoreCase(PUB_DATE)){
						message.setDate(property.getFirstChild().getNodeValue());
					}
				}
				messages.add(message);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		return messages;
	}
}
