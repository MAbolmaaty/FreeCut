package force.freecut.freecut.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;

import java.util.ArrayList;
import java.util.List;

import force.freecut.freecut.R;

public class OutputVideosAdapter
        extends RecyclerView.Adapter<OutputVideosAdapter.OutputVideoViewHolder>{

    private static final String TAG = OutputVideosAdapter.class.getSimpleName();

    private int mNumberOfVideos;

    private int [] mVideoProgresses;


    public OutputVideosAdapter(int numberOfVideos) {
        mNumberOfVideos = numberOfVideos;
        mVideoProgresses = new int [mNumberOfVideos];
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
        holder.mVideoProgress.setProgress(mVideoProgresses[position]);
//        Config.enableStatisticsCallback(new StatisticsCallback() {
//            @Override
//            public void apply(Statistics statistics) {
//                double progress =
//                        ((double) statistics.getVideoFrameNumber()/1500) * 100;
//                Log.d(TAG, "progress : " + (int)progress);
//                holder.mVideoProgress.setProgress((int)progress);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mNumberOfVideos;
    }

    public class OutputVideoViewHolder extends RecyclerView.ViewHolder{

        ImageView mVideoThumbnail;
        ProgressBar mVideoProgress;
        private int mProgress;

        public OutputVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            mVideoThumbnail = itemView.findViewById(R.id.videoThumbnail);
            mVideoProgress = itemView.findViewById(R.id.videoProgressBar);
        }

        public void setProgress(int progress) {
            mProgress = progress;
        }

        public void updateProgressBar(int progress){
            mVideoProgress.setProgress(progress);
        }
    }

    public void setVideoProgress(int position, int progress){
        mVideoProgresses[position] = progress;
        notifyItemChanged(position);
    }
}
