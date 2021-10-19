package force.freecut.freecut.adapters;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.util.Log;
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
        extends RecyclerView.Adapter<OutputVideosAdapter.OutputVideoViewHolder>{

    private static final String TAG = OutputVideosAdapter.class.getSimpleName();

    private Context mContext;
    private TrimmedVideo [] mTrimmedVideos;
    private VideoPlayClickListener mVideoPlayClickListener;
    private VideoShareClickListener mVideoShareClickListener;

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

    @NonNull
    @Override
    public OutputVideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.list_item_output_video;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);
        OutputVideoViewHolder viewHolder = new OutputVideoViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull OutputVideoViewHolder holder, int position) {
        holder.mVideoName.setText(mTrimmedVideos[position].getVideoName());
        holder.mVideoTime.setAlpha(mTrimmedVideos[position].getOptionsVisibility());
        holder.mVideoProgressStatus.setText(mTrimmedVideos[position].getTrimmingStatus());
        holder.mVideoProgress.setAlpha(mTrimmedVideos[position].getProgressVisibility());
        holder.mProgressPercentage.setAlpha(mTrimmedVideos[position].getProgressVisibility());
        holder.mVideoProgressStatus.setAlpha(mTrimmedVideos[position].getProgressVisibility());
        holder.mProgressPercentage.setText(String.format(Locale.ENGLISH,"%d%%",
                mTrimmedVideos[position].getProgress()));
        holder.mVideoProgress.setProgress(mTrimmedVideos[position].getProgress());
        holder.mIcShare.setAlpha(mTrimmedVideos[position].getOptionsVisibility());
        holder.mIcPlayVideo.setAlpha(mTrimmedVideos[position].getOptionsVisibility());
        holder.mIcPlayVideo.setImageResource(mTrimmedVideos[position].getVideoStatus());
        Glide.with(mContext).load(mTrimmedVideos[position].getVideoFile()).fitCenter()
                .into(holder.mVideoThumbnail);
        holder.mVideoTime.setText(getVideoDuration(mTrimmedVideos[position].getVideoFile()));
        switch (mTrimmedVideos[position].getVideoStatus()){
            case R.drawable.ic_play:
                holder.mPlayIndicator.setAlpha(0);
                break;
            case R.drawable.ic_pause:
                holder.mPlayIndicator.setAlpha(1);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mTrimmedVideos.length;
    }

    public class OutputVideoViewHolder extends RecyclerView.ViewHolder{

        private ImageView mVideoThumbnail;
        private TextView mVideoName;
        private TextView mVideoTime;
        private ProgressBar mVideoProgress;
        private TextView mProgressPercentage;
        private TextView mVideoProgressStatus;
        private ImageView mIcShare;
        private ImageView mIcPlayVideo;
        private View mPlayIndicator;

        public OutputVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            mVideoThumbnail = itemView.findViewById(R.id.videoThumbnail);
            mVideoProgress = itemView.findViewById(R.id.videoProgressBar);
            mVideoName = itemView.findViewById(R.id.videoName);
            mVideoTime = itemView.findViewById(R.id.videoTime);
            mProgressPercentage = itemView.findViewById(R.id.progressPercentage);
            mVideoProgressStatus = itemView.findViewById(R.id.videoProgressStatus);
            mIcShare = itemView.findViewById(R.id.ic_share);
            mIcPlayVideo = itemView.findViewById(R.id.ic_playVideo);
            mPlayIndicator = itemView.findViewById(R.id.playIndicator);

            mIcPlayVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mVideoPlayClickListener.onPlayClickListener(getAdapterPosition());
                }
            });

            mIcShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mVideoShareClickListener.onShareClickListener(getAdapterPosition());
                }
            });
        }

        public void updateProgress(int progress){
            mProgressPercentage.setText(String.format(Locale.ENGLISH,"%d%%" , progress));
            mVideoProgress.setProgress(progress);
        }
    }

    public void setTrimmedVideo(int position, TrimmedVideo trimmedVideo){
        mTrimmedVideos[position] = trimmedVideo;
        notifyItemChanged(position);
    }

    public void setTrimmedVideoStatus(int position, int status){
        mTrimmedVideos[position].setVideoStatus(status);
        notifyItemChanged(position);
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
