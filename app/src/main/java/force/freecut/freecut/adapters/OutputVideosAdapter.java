package force.freecut.freecut.adapters;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Locale;

import force.freecut.freecut.Data.TrimmedVideo;
import force.freecut.freecut.R;

public class OutputVideosAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final String TAG = OutputVideosAdapter.class.getSimpleName();

    private Context mContext;
    private TrimmedVideo [] mTrimmedVideos;
    private VideoPlayClickListener mVideoPlayClickListener;
    private VideoShareClickListener mVideoShareClickListener;
    private static final int TRIMMING = 0;
    private static final int TRIMMED = 1;

    public interface VideoPlayClickListener{
        void onPlayClickListener(int videoClicked);
    }

    public interface VideoShareClickListener{
        void onShareClickListener(int videoClicked);
    }

    public OutputVideosAdapter(Context context, TrimmedVideo [] trimmedVideos,
                               VideoPlayClickListener videoPlayClickListener,
                               VideoShareClickListener videoShareClickListener) {
        mContext = context;
        mTrimmedVideos = trimmedVideos;
        mVideoPlayClickListener = videoPlayClickListener;
        mVideoShareClickListener = videoShareClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mTrimmedVideos[position].getProgress() == 100){
            return TRIMMED;
        } else {
            return TRIMMING;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForTrimmingVideo = R.layout.list_item_output_video;
        int layoutIdForTrimmedVideo = R.layout.list_item_trimmed_video;
        LayoutInflater inflater = LayoutInflater.from(context);

        View viewForTrimmingVideo =
                inflater.inflate(layoutIdForTrimmingVideo, parent, false);

        View viewForTrimmedVideo =
                inflater.inflate(layoutIdForTrimmedVideo, parent, false);

        switch (viewType){
            case TRIMMING:
                OutputVideoViewHolder viewHolderTrimmingVideo =
                        new OutputVideoViewHolder(viewForTrimmingVideo);
                return viewHolderTrimmingVideo;
            case TRIMMED:
                TrimmedVideoViewHolder trimmedVideoViewHolder =
                        new TrimmedVideoViewHolder(viewForTrimmedVideo);
                return trimmedVideoViewHolder;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case TRIMMING:
                OutputVideoViewHolder outputVideoViewHolder = (OutputVideoViewHolder)holder;
                outputVideoViewHolder.mVideoName.setText(mTrimmedVideos[position].getVideoName());
                outputVideoViewHolder.mVideoProgressStatus.setText(mTrimmedVideos[position]
                        .getTrimmingStatus());
                outputVideoViewHolder.mProgressPercentage
                        .setText(String.format(Locale.ENGLISH,"%d%%",
                                mTrimmedVideos[position].getProgress()));
                outputVideoViewHolder.mVideoProgress
                        .setProgress(mTrimmedVideos[position].getProgress());
                break;
            case TRIMMED:
                TrimmedVideoViewHolder trimmedVideoViewHolder = (TrimmedVideoViewHolder) holder;
                trimmedVideoViewHolder.mVideoName.setText(mTrimmedVideos[position].getVideoName());
//                trimmedVideoViewHolder.mIcPlayVideo
//                        .setImageResource(mTrimmedVideos[position].getVideoStatus());
                Glide.with(mContext).load(mTrimmedVideos[position].getVideoFile()).fitCenter()
                        .into(trimmedVideoViewHolder.mVideoThumbnail);
//                trimmedVideoViewHolder.mVideoTime
//                        .setText(getVideoDuration(mTrimmedVideos[position].getVideoFile()));
                break;
        }
//        switch (mTrimmedVideos[position].getVideoStatus()){
//            case R.drawable.ic_play:
//                holder.mPlayIndicator.setAlpha(0);
//                break;
//            case R.drawable.ic_pause:
//                holder.mPlayIndicator.setAlpha(1);
//                break;
//        }
    }

    @Override
    public int getItemCount() {
        return mTrimmedVideos.length;
    }

    public class OutputVideoViewHolder extends RecyclerView.ViewHolder{

        private TextView mVideoName;
        private ProgressBar mVideoProgress;
        private TextView mProgressPercentage;
        private TextView mVideoProgressStatus;

        public OutputVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            mVideoProgress = itemView.findViewById(R.id.videoProgressBar);
            mVideoName = itemView.findViewById(R.id.videoName);
            mProgressPercentage = itemView.findViewById(R.id.progressPercentage);
            mVideoProgressStatus = itemView.findViewById(R.id.videoProgressStatus);
        }

        public void updateProgress(int progress){
            mProgressPercentage.setText(String.format(Locale.ENGLISH,"%d%%" , progress));
            mVideoProgress.setProgress(progress);
        }
    }

    public class TrimmedVideoViewHolder extends RecyclerView.ViewHolder{

        private ImageView mVideoThumbnail;
        private TextView mVideoName;
        private TextView mVideoTime;
        private ImageView mShare;
        private ImageView mVideoMode;
        private View mPlayIndicator;

        public TrimmedVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            mVideoThumbnail = itemView.findViewById(R.id.videoThumbnail);
            mVideoName = itemView.findViewById(R.id.videoName);
            mVideoTime = itemView.findViewById(R.id.videoTime);
            mShare = itemView.findViewById(R.id.ic_share);
            mVideoMode = itemView.findViewById(R.id.ic_videoMode);
            mPlayIndicator = itemView.findViewById(R.id.playIndicator);

            mVideoMode.setOnClickListener(v ->
                    mVideoPlayClickListener.onPlayClickListener(getAdapterPosition()));

            mShare.setOnClickListener(v ->
                    mVideoShareClickListener.onShareClickListener(getAdapterPosition()));
        }
    }

    private String getVideoDuration(File videoFile) {
        if (videoFile == null)
            return "";
        String videoPath = videoFile.getAbsolutePath();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        String time =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int seconds =  Integer.parseInt(time) / 1000;
        return getVideoTime(seconds);
    }

    private String getVideoTime(int seconds) {
        long second = seconds % 60;
        long minute = (seconds / 60) % 60;
        long hour = (seconds / (60 * 60)) % 24;

        if (hour > 0)
            return String.format(Locale.ENGLISH, "%d:%d:%02d", hour, minute, second);
        else
            return String.format(Locale.ENGLISH, "%d:%02d", minute, second);
    }
}
