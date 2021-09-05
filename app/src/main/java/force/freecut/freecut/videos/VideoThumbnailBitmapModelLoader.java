package force.freecut.freecut.videos;

import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;

import java.io.File;

/**
 * Created by MohamedDev on 5/22/2017.
 */

public class VideoThumbnailBitmapModelLoader implements ModelLoader<File, Bitmap> {
    @Override
    public DataFetcher<Bitmap> getResourceFetcher(File model, int width, int height) {
        int kind = MediaStore.Video.Thumbnails.MINI_KIND;
        if (width <= 96 && height <= 96) {
            kind = MediaStore.Video.Thumbnails.MICRO_KIND;
        }
        return new VideoThumbnailDataFetcher(model, kind);
    }
}