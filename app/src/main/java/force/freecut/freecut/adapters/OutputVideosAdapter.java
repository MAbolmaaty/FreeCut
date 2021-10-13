package force.freecut.freecut.adapters;

import android.content.Context;
import android.os.Handler;
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
    private int mNumberOfVideos;
    private TrimmedVideo [] mTrimmedVideos;

    public OutputVideosAdapter(Context context, int numberOfVideos) {
        mContext = context;
        mNumberOfVideos = numberOfVideos;
        mTrimmedVideos = new TrimmedVideo[mNumberOfVideos];
        for (int i = 0 ; i < mNumberOfVideos ; i++){
            mTrimmedVideos[i] = new TrimmedVideo(null, 0,
                    String.format(Locale.ENGLISH, "video-%02d", i+1));
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
        holder.mVideoName.setText(mTrimmedVideos[position].getVideoName());

        if (mTrimmedVideos[position].getTrimProgress() > 0){
            holder.mVideoProgressStatus.setText(mContext.getString(R.string.trimming));
        }

        if (mTrimmedVideos[position].getVideoFile() != null) {
            holder.mVideoProgressStatus.setText("");
            Glide.with(mContext).load(mTrimmedVideos[position].getVideoFile()).fitCenter()
                    .into(holder.mVideoThumbnail);
            hideVideoProgress(holder);
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
        private TextView mVideoProgressStatus;
        private ImageView mIcTikTok;
        private ImageView mIcFacebook;
        private ImageView mIcInstagram;
        private ImageView mIcTwitter;
        private ImageView mIcSnapchat;
        private ImageView mIcWhatsapp;
        private ImageView mIcPlayVideo;

        public OutputVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            mVideoThumbnail = itemView.findViewById(R.id.videoThumbnail);
            mVideoProgress = itemView.findViewById(R.id.videoProgressBar);
            mVideoName = itemView.findViewById(R.id.videoName);
            mProgressPercentage = itemView.findViewById(R.id.progressPercentage);
            mVideoProgressStatus = itemView.findViewById(R.id.videoProgressStatus);
            mIcTikTok = itemView.findViewById(R.id.ic_share);
            mIcFacebook = itemView.findViewById(R.id.ic_facebook);
            mIcInstagram = itemView.findViewById(R.id.ic_instagram);
            mIcTwitter = itemView.findViewById(R.id.ic_twitter);
            mIcSnapchat = itemView.findViewById(R.id.ic_snapchat);
            mIcWhatsapp = itemView.findViewById(R.id.ic_whatsapp);
            mIcPlayVideo = itemView.findViewById(R.id.ic_playVideo);
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
    }

    private void hideVideoProgress(OutputVideoViewHolder holder){
        holder.mVideoProgress.animate().alpha(0).setDuration(500);
        holder.mProgressPercentage.animate().alpha(0).setDuration(500);
        holder.mVideoProgressStatus.animate().alpha(0).setDuration(500);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                holder.mVideoProgress.setVisibility(View.INVISIBLE);
                holder.mProgressPercentage.setVisibility(View.INVISIBLE);
                holder.mVideoProgressStatus.setVisibility(View.INVISIBLE);

                holder.mIcTikTok.setVisibility(View.VISIBLE);
                holder.mIcFacebook.setVisibility(View.VISIBLE);
                holder.mIcInstagram.setVisibility(View.VISIBLE);
                holder.mIcTwitter.setVisibility(View.VISIBLE);
                holder.mIcSnapchat.setVisibility(View.VISIBLE);
                holder.mIcWhatsapp.setVisibility(View.VISIBLE);
                holder.mIcPlayVideo.setVisibility(View.VISIBLE);

                holder.mIcTikTok.animate().alpha(1).setDuration(500);
                holder.mIcFacebook.animate().alpha(1).setDuration(500);
                holder.mIcInstagram.animate().alpha(1).setDuration(500);
                holder.mIcTwitter.animate().alpha(1).setDuration(500);
                holder.mIcSnapchat.animate().alpha(1).setDuration(500);
                holder.mIcWhatsapp.animate().alpha(1).setDuration(500);
                holder.mIcPlayVideo.animate().alpha(1).setDuration(500);
            }
        }, 500);

    }
}
