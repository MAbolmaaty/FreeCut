package force.freecut.freecut.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapEncoder;
import com.bumptech.glide.load.resource.bitmap.StreamBitmapDecoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;

import java.io.File;
import java.util.List;

import force.freecut.freecut.Data.VideoItem;
import force.freecut.freecut.R;
import force.freecut.freecut.utils.interfaces.VideoItemClickHandler;
import force.freecut.freecut.utils.interfaces.VideoItemDeleteHandler;
import force.freecut.freecut.videos.PoolingResourceDecoder;
import force.freecut.freecut.videos.VideoThumbnailBitmapModelLoader;

import static force.freecut.freecut.utils.Constants.VIDEOS_MERGE;
import static force.freecut.freecut.utils.Constants.VIDEOS_TRIM;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.AdapterViewHolder> {

    private Context mContext;
    private List<VideoItem> mListVideos;
    GenericRequestBuilder<File, ?, ?, Bitmap> thumbLoader ;
    private int mVideosType;
    private VideoItemClickHandler mClickHandler;
    private VideoItemDeleteHandler mDeleteHandler;

    public VideosAdapter(Context context, List<VideoItem> listVideos, int videosType,
                         VideoItemClickHandler clickHandler, VideoItemDeleteHandler deleteHandler) {
        mContext = context;
        mListVideos = listVideos;
        thumbLoader = Glide
                .with(context)
                .using(new VideoThumbnailBitmapModelLoader(), Bitmap.class)
                .from(File.class)
                .as(Bitmap.class)
                .override(50,50)
                .placeholder(R.drawable.video)
                .decoder(new PoolingResourceDecoder(context))
                .diskCacheStrategy(DiskCacheStrategy.RESULT) // change to NONE and remove next two lines to disable caching
                .encoder(new BitmapEncoder(Bitmap.CompressFormat.JPEG, 75)) // for cache
                .cacheDecoder(new FileToStreamDecoder<>(new StreamBitmapDecoder(context)));
        mVideosType = videosType;
        mClickHandler = clickHandler;
        mDeleteHandler = deleteHandler;
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_video, parent, false);
        switch (mVideosType){
            case VIDEOS_TRIM:
                view.findViewById(R.id.videoDuration).setVisibility(View.VISIBLE);
                view.findViewById(R.id.iconPlay).setVisibility(View.VISIBLE);
                break;
            case VIDEOS_MERGE:
                view.findViewById(R.id.delete).setVisibility(View.VISIBLE);
                break;

        }
        return new AdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewHolder holder, int position) {
        holder.mVideoName.setText(mListVideos.get(position).getItem_name());
        holder.mVideoDuration.setText(mListVideos.get(position).getTime());
        File video = new File(mListVideos.get(position).getImageLink());
        if (video.exists()){
            thumbLoader.load(video).into(holder.mVideoThumbnail);
        }
    }

    @Override
    public int getItemCount() {
        return mListVideos.size();
    }

    public class AdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView mVideoThumbnail;
        TextView mVideoName;
        TextView mVideoDuration;
        ImageView mDelete;

        public AdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mVideoThumbnail = itemView.findViewById(R.id.videoThumbnail);
            mVideoName = itemView.findViewById(R.id.videoName);
            mVideoDuration = itemView.findViewById(R.id.videoDuration);
            mDelete = itemView.findViewById(R.id.delete);
            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDeleteHandler.onClick(getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View v) {
            mClickHandler.onClick(getAdapterPosition());
        }
    }
}
