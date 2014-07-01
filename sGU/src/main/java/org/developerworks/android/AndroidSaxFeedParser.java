package org.developerworks.android;

import java.util.ArrayList;
import java.util.List;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;

public class AndroidSaxFeedParser extends BaseFeedParser {

	static final String RSS = "rss";
	public AndroidSaxFeedParser(String feedUrl, String rssContent) {
		super(feedUrl, rssContent);
	}

	public List<Message> parse() {
		final Message currentMessage = new Message();
		RootElement root = new RootElement(RSS);
		final List<Message> messages = new ArrayList<Message>();
		Element channel = root.getChild(CHANNEL);
		Element item = channel.getChild(ITEM);
		item.setEndElementListener(new EndElementListener(){
			public void end() {
				messages.add(currentMessage.copy());
			}
		});
		item.getChild(TITLE).setEndTextElementListener(new EndTextElementListener(){
			public void end(String body) {
				currentMessage.setTitle(body);
			}
		});
		item.getChild(LINK).setEndTextElementListener(new EndTextElementListener(){
			public void end(String body) {
				currentMessage.setLink(body);
			}
		});

		// Added enclosure parsing
		item.getChild(ENCLOSURE).setStartElementListener(new StartElementListener() {
            
            @Override
            public void start(Attributes attributes) {
                currentMessage.setEnclosureUrl(attributes.getValue("url"));
                currentMessage.setEnclosureLength(attributes.getValue("length"));
            }
            
        });
		item.getChild(GUID).setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                currentMessage.setGuId(body);
            }
        });
		
		item.getChild(DESCRIPTION).setEndTextElementListener(new EndTextElementListener(){
			public void end(String body) {
				currentMessage.setDescription(body);
			}
		});
		item.getChild(PUB_DATE).setEndTextElementListener(new EndTextElementListener(){
			public void end(String body) {
				currentMessage.setDate(body);
			}
		});
		try {
			//Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
			Xml.parse(this.getRssContentInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return messages;
	}
}
