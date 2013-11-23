package se.slide.sgu;

import se.slide.sgu.model.Content;

public interface ContentListener {
    public void playContent(Content content);
    public int getSavedStateMode();
    public boolean getSavedStateIsPlaying();
    public boolean getSavedStateIsPaused();
    public String getSavedStateMp3();
    public void showContentDetails(Content content);
    public Content getCurrentTrack();
    public boolean isPlaying();
    public boolean isPaused();
}
