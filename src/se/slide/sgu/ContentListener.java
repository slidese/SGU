package se.slide.sgu;

import se.slide.sgu.model.Content;

public interface ContentListener {
    public void playContent(Content content);
    public int getMode();
}
