package force.freecut.freecut.videos;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;

import java.io.File;

/**
 * Created by MohamedDev on 5/22/2017.
 */

class VideoThumbnailDataFetcher implements DataFetcher<Bitmap> {
    private final File model;
    private int kind;
    public VideoThumbnailDataFetcher(File model, int kind) {
        this.model = model;
        this.kind = kind;
    }
    @Override
    public Bitmap loadData(Priority priority) throws Exception {
        String path = model.getAbsolutePath();
        return ThumbnailUtils.createVideoThumbnail(path, kind);
    }
    @Override
    public void cleanup() {
        // stateless, no cleanup needed
    }
    @Override
    public String getId() {
        return model.getAbsolutePath() + "+" + kindString(kind);
    }
    private String kindString(int kind) {
        switch(kind) {
            case MediaStore.Video.Thumbnails.MINI_KIND: return "MINI_KIND";
            case MediaStore.Video.Thumbnails.MICRO_KIND: return "MICRO_KIND";
            default: return "UNKNOWN_KIND(" + kind + ")";
        }
    }
    @Override
    public void cancel() {
        // cannot cancel
    }
}
