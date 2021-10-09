package force.freecut.freecut.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import force.freecut.freecut.Data.TrimmedVideo;
import force.freecut.freecut.R;

public class OutputVideosAdapter
        extends RecyclerView.Adapter<OutputVideosAdapter.OutputVideoViewHolder>{

    private static final String TAG = OutputVideosAdapter.class.getSimpleName();

    private Context mContext;
    private int mNumberOfVideos;
    private TrimmedVideo [] mTrimmedVideos;

    public OutputVideosAdapter(Context context, int numberOfVideos) {
        mContext = context;
        mNumberOfVideos = numberOfVideos;
        mTrimmedVideos = new TrimmedVideo[mNumberOfVideos];
        for (int i = 0 ; i < mNumberOfVideos ; i++){
            mTrimmedVideos[i] = new TrimmedVideo(null, 0);
        }
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
        holder.mVideoProgress.setProgress(mTrimmedVideos[position].getTrimProgress(), true);
        holder.mProgressPercentage.setText(String.format(Locale.ENGLISH,"%d%%" ,
                mTrimmedVideos[position].getTrimProgress()));

        if (mTrimmedVideos[position].getTrimProgress() > 0){
            holder.mVideoName.setText(mContext.getString(R.string.trimming));
        }

        if (mTrimmedVideos[position].getVideoFile() != null) {
            holder.mVideoName.setText(mTrimmedVideos[position].getVideoFile().getName());
            Glide.with(mContext).load(mTrimmedVideos[position].getVideoFile()).fitCenter()
                    .into(holder.mVideoThumbnail);
        }
    }

    @Override
    public int getItemCount() {
        return mNumberOfVideos;
    }

    public class OutputVideoViewHolder extends RecyclerView.ViewHolder{

        private ImageView mVideoThumbnail;
        private TextView mVideoName;
        private ProgressBar mVideoProgress;
        private TextView mProgressPercentage;

        public OutputVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            mVideoThumbnail = itemView.findViewById(R.id.videoThumbnail);
            mVideoProgress = itemView.findViewById(R.id.videoProgressBar);
            mVideoName = itemView.findViewById(R.id.videoName);
            mProgressPercentage = itemView.findViewById(R.id.progressPercentage);
        }

        public void updateProgressBar(int progress){
            mVideoProgress.setProgress(progress);

        }

        public void updatePercentage(int progress){
            mProgressPercentage.setText(String.format(Locale.ENGLISH,"%d%%" , progress));
        }
    }

    public void setVideoProgress(int position, int progress){
        mTrimmedVideos[position].setTrimProgress(progress);
        notifyItemChanged(position);
    }

    public void setVideoPath(int position, File file){
        mTrimmedVideos[position].setVideoFile(file);
        notifyItemChanged(position);
    }
}
