package force.freecut.freecut.Data;

import java.io.Serializable;

/**
 * Created by MohamedDev on 2/13/2018.
 */

public class VideoItem implements Serializable {
    private String ImageLink;
    private String Item_name;
    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImageLink() {
        return ImageLink;
    }

    public void setImageLink(String imageLink) {
        ImageLink = imageLink;
    }

    public String getItem_name() {
        return Item_name;
    }

    public void setItem_name(String item_name) {
        Item_name = item_name;
    }
}
