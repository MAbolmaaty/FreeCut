package force.freecut.freecut.adapters;

import android.content.Context;
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

    public interface VideoPlayClickListener{
        void onPlayClickListener(int videoClicked);
    }

    public OutputVideosAdapter(Context context, TrimmedVideo [] trimmedVideos,
                               VideoPlayClickListener videoPlayClickListener) {
        mContext = context;
        mTrimmedVideos = trimmedVideos;
        mVideoPlayClickListener = videoPlayClickListener;
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
    }

    @Override
    public int getItemCount() {
        return mTrimmedVideos.length;
    }

    public class OutputVideoViewHolder extends RecyclerView.ViewHolder{

        private ImageView mVideoThumbnail;
        private TextView mVideoName;
        private ProgressBar mVideoProgress;
        private TextView mProgressPercentage;
        private TextView mVideoProgressStatus;
        private ImageView mIcShare;
        private ImageView mIcPlayVideo;

        public OutputVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            mVideoThumbnail = itemView.findViewById(R.id.videoThumbnail);
            mVideoProgress = itemView.findViewById(R.id.videoProgressBar);
            mVideoName = itemView.findViewById(R.id.videoName);
            mProgressPercentage = itemView.findViewById(R.id.progressPercentage);
            mVideoProgressStatus = itemView.findViewById(R.id.videoProgressStatus);
            mIcShare = itemView.findViewById(R.id.ic_share);
            mIcPlayVideo = itemView.findViewById(R.id.ic_playVideo);

            mIcPlayVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mVideoPlayClickListener.onPlayClickListener(getAdapterPosition());
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
}
