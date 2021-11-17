package force.freecut.freecut.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.Locale;

import force.freecut.freecut.Data.TrimVideoModel;
import force.freecut.freecut.R;

public class TrimmedVideosAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final String TAG = TrimmedVideosAdapter.class.getSimpleName();

    private Context mContext;
    private TrimVideoModel[] mTrimVideoModels;
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

    public TrimmedVideosAdapter(Context context, TrimVideoModel[] trimVideoModels,
                                VideoPlayClickListener videoPlayClickListener,
                                VideoShareClickListener videoShareClickListener) {
        mContext = context;
        mTrimVideoModels = trimVideoModels;
        mVideoPlayClickListener = videoPlayClickListener;
        mVideoShareClickListener = videoShareClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mTrimVideoModels[position].getProgress() == 100){
            return TRIMMED;
        } else {
            return TRIMMING;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForTrimmingVideo = R.layout.list_item_trimming_video;
        int layoutIdForTrimmedVideo = R.layout.list_item_trimmed_video;
        LayoutInflater inflater = LayoutInflater.from(context);

        View viewForTrimmingVideo =
                inflater.inflate(layoutIdForTrimmingVideo, parent, false);

        View viewForTrimmedVideo =
                inflater.inflate(layoutIdForTrimmedVideo, parent, false);

        switch (viewType){
            case TRIMMING:
                TrimmingVideoViewHolder viewHolderTrimmingVideo =
                        new TrimmingVideoViewHolder(viewForTrimmingVideo);
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
                TrimmingVideoViewHolder trimmingVideoViewHolder = (TrimmingVideoViewHolder) holder;
                trimmingVideoViewHolder.mVideoName.setText(mTrimVideoModels[position].getVideoName());
                trimmingVideoViewHolder.mVideoProgressStatus.setText(mTrimVideoModels[position]
                        .getTrimmingStatus());
                trimmingVideoViewHolder.mProgressPercentage
                        .setText(String.format(Locale.ENGLISH,"%d%%",
                                mTrimVideoModels[position].getProgress()));
                trimmingVideoViewHolder.mVideoProgress
                        .setProgress(mTrimVideoModels[position].getProgress());
                break;
            case TRIMMED:
                TrimmedVideoViewHolder trimmedVideoViewHolder = (TrimmedVideoViewHolder) holder;
                trimmedVideoViewHolder.mVideoName.setText(mTrimVideoModels[position].getVideoName());
                Glide.with(mContext).load(mTrimVideoModels[position].getVideoFile()).fitCenter()
                        .into(trimmedVideoViewHolder.mVideoThumbnail);
                trimmedVideoViewHolder.mVideoTime
                        .setText(mTrimVideoModels[position].getVideoDuration());
                if (mTrimVideoModels[position].getVideoMode() == TrimVideoModel.Mode.PLAY) {
                    trimmedVideoViewHolder.mVideoMode.setImageResource(R.drawable.ic_pause);
                    trimmedVideoViewHolder.mPlayIndicator.setAlpha(1);
                }
                if (mTrimVideoModels[position].getVideoMode() == TrimVideoModel.Mode.PAUSE) {
                    trimmedVideoViewHolder.mVideoMode.setImageResource(R.drawable.ic_play);
                    trimmedVideoViewHolder.mPlayIndicator.setAlpha(0);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mTrimVideoModels.length;
    }

    public class TrimmingVideoViewHolder extends RecyclerView.ViewHolder{

        private TextView mVideoName;
        private ProgressBar mVideoProgress;
        private TextView mProgressPercentage;
        private TextView mVideoProgressStatus;

        public TrimmingVideoViewHolder(@NonNull View itemView) {
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
            mShare = itemView.findViewById(R.id.ic_videoShare);
            mVideoMode = itemView.findViewById(R.id.ic_videoMode);
            mPlayIndicator = itemView.findViewById(R.id.playIndicator);

            mVideoMode.setOnClickListener(v ->
                    mVideoPlayClickListener.onPlayClickListener(getAdapterPosition()));

            mShare.setOnClickListener(v ->
                    mVideoShareClickListener.onShareClickListener(getAdapterPosition()));
        }
    }
}
