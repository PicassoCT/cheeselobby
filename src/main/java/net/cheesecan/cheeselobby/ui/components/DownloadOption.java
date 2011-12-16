package net.cheesecan.cheeselobby.ui.components;

/**
 *
 * @author jahziah
 */
public class DownloadOption {

    private String caption;
    private String url;

    public DownloadOption(String caption, String url) {
        this.caption = caption;
        this.url = url;
    }

    @Override
    public String toString() {
        return caption;
    }

    public String getUrl() {
        return url;
    }
}