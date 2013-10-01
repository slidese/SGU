package org.developerworks.android;
public abstract class FeedParserFactory {
	static String feedUrl = "http://www.androidster.com/android_news.rss";
	
	public static FeedParser getParser(String rss){
		return getParser(ParserType.ANDROID_SAX, rss);
	}
	
	public static FeedParser getParser(ParserType type, String rss){
		switch (type){
			case SAX:
				return new SaxFeedParser(feedUrl);
			case DOM:
				return new DomFeedParser(feedUrl);
			case ANDROID_SAX:
				return new AndroidSaxFeedParser(feedUrl, rss);
			case XML_PULL:
				return new XmlPullFeedParser(feedUrl);
			default: return null;
		}
	}
}
