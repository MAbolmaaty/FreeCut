package force.freecut.freecut.videos;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

/**
 * Created by MohamedDev on 5/22/2017.
 */

public class PoolingResourceDecoder implements ResourceDecoder<Bitmap, Bitmap> {
    private final BitmapPool pool;
    public PoolingResourceDecoder(Context context) {
        pool = Glide.get(context).getBitmapPool();
    }
    @Override
    public Resource<Bitmap> decode(Bitmap source, int width, int height) {
        return BitmapResource.obtain(source, pool);
    }
    @Override
    public String getId() {
        return getClass().getName();
    }
}