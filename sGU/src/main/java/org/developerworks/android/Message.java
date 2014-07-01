package org.developerworks.android;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message implements Comparable<Message>{
	static SimpleDateFormat FORMATTER = 
		new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
	private String title;
	private URL link;
	private String description;
	private Date date;
	private URL enclosure_url;
	private int enclosure_length;
	private String guid;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title.trim();
	}
	// getters and setters omitted for brevity 
	public URL getLink() {
		return link;
	}
	
	public void setLink(String link) {
		try {
			this.link = new URL(link);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public URL getEnclosureUrl() {
        return enclosure_url;
    }
    
    public void setEnclosureUrl(String enclosure_url) {
        try {
            this.enclosure_url = new URL(enclosure_url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public int getEnclosureLength() {
        return enclosure_length;
    }
    
    public void setEnclosureLength(String enclosure_length) {
        try {
            this.enclosure_length = Integer.valueOf(enclosure_length);
        }
        catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String getGuId() {
        return guid;
    }

    public void setGuId(String guid) {
        this.guid = guid.trim();
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description.trim();
	}

	public String getDate() {
		return FORMATTER.format(this.date);
	}

	public void setDate(String date) {
		// pad the date if necessary
		while (!date.endsWith("00")){
			date += "0";
		}
		try {
			this.date = FORMATTER.parse(date.trim());
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Date getDateObject() {
	    return date;
	}
	
	public Message copy(){
		Message copy = new Message();
		copy.title = title;
		copy.link = link;
		copy.enclosure_url = enclosure_url;
		copy.enclosure_length = enclosure_length;
		copy.description = description;
		copy.date = date;
		copy.guid = guid;
		return copy;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Title: ");
		sb.append(title);
		sb.append('\n');
		sb.append("Date: ");
		sb.append(this.getDate());
		sb.append('\n');
		sb.append("Link: ");
		sb.append(link);
		sb.append('\n');
		sb.append("Enclosure url: ");
        sb.append(enclosure_url);
        sb.append('\n');
        sb.append("Enclosure length: ");
        sb.append(enclosure_length);
        sb.append('\n');
        sb.append("GUID: ");
        sb.append(guid);
        sb.append('\n');
		sb.append("Description: ");
		sb.append(description);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((enclosure_url == null) ? 0 : enclosure_url.hashCode());
		//result = prime * result + ((enclosure_length < 1) ? 0 : enclosure_length)); // This is probably not so good
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (enclosure_url == null) {
            if (other.enclosure_url != null)
                return false;
        } else if (!enclosure_url.equals(other.enclosure_url))
            return false;
		if (enclosure_length != other.enclosure_length)
		    return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (guid == null) {
            if (other.guid != null)
                return false;
        } else if (!guid.equals(other.guid))
            return false;
		return true;
	}

	public int compareTo(Message another) {
		if (another == null) return 1;
		// sort descending, most recent first
		return another.date.compareTo(date);
	}
}
