package org.developerworks.android;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class BaseFeedParser implements FeedParser {

	// names of the XML tags
	static final String CHANNEL = "channel";
	static final String PUB_DATE = "pubDate";
	static final  String DESCRIPTION = "description";
	static final  String LINK = "link";
	static final  String ENCLOSURE = "enclosure";
	static final  String TITLE = "title";
	static final  String ITEM = "item";
	static final  String GUID = "guid";
	
	private final URL feedUrl;
	private final String rssContent; // If the content has already been downloaded

	protected BaseFeedParser(String feedUrl, String rssContent){
		try {
			this.feedUrl = new URL(feedUrl);
			this.rssContent = rssContent;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	protected InputStream getInputStream() {
		try {
			return feedUrl.openConnection().getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected InputStream getRssContentInputStream() {
	    try {
            return new ByteArrayInputStream(rssContent.getBytes("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
}