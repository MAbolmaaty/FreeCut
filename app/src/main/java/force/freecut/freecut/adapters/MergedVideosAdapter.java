package force.freecut.freecut.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import force.freecut.freecut.Data.MergeVideoModel;
import force.freecut.freecut.R;

public class MergedVideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = MergedVideosAdapter.class.getSimpleName();

    private Context mContext;
    private List<MergeVideoModel> mMergeVideoModels;
    private VideoPlayClickListener mVideoPlayClickListener;
    private VideoShareClickListener mVideoShareClickListener;
    private ButtonMergeClickListener mButtonMergeClickListener;
    public static final int MERGING = 0;
    public static final int MERGED = 1;
    public static final int LAST_ITEM = 2;

    public interface VideoPlayClickListener {
        void onPlayClickListener(int videoClicked);
    }

    public interface VideoShareClickListener {
        void onShareClickListener(int videoClicked);
    }

    public interface ButtonMergeClickListener {
        void onButtonMergeClickListener();
    }

    public MergedVideosAdapter(Context context, List<MergeVideoModel> mergeVideoModels,
                               VideoPlayClickListener videoPlayClickListener,
                               VideoShareClickListener videoShareClickListener,
                               ButtonMergeClickListener buttonMergeClickListener) {
        mContext = context;
        mMergeVideoModels = mergeVideoModels;
        mVideoPlayClickListener = videoPlayClickListener;
        mVideoShareClickListener = videoShareClickListener;
        mButtonMergeClickListener = buttonMergeClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mMergeVideoModels.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForMergingVideo = R.layout.list_item_merging_video;
        int layoutIdForMergedVideo = R.layout.list_item_merged_video;
        int layoutIdForMergeLastItem = R.layout.list_item_merge_last;
        LayoutInflater inflater = LayoutInflater.from(context);

        View viewForMergingVideo =
                inflater.inflate(layoutIdForMergingVideo, parent, false);
        View viewForMergedVideo =
                inflater.inflate(layoutIdForMergedVideo, parent, false);
        View viewForMergeLastItem =
                inflater.inflate(layoutIdForMergeLastItem, parent, false);

        switch (viewType){
            case MERGING:
                MergingVideoViewHolder mergingVideoViewHolder =
                        new MergingVideoViewHolder(viewForMergingVideo);
                return mergingVideoViewHolder;
            case MERGED:
                MergedVideoViewHolder mergedVideoViewHolder =
                        new MergedVideoViewHolder(viewForMergedVideo);
                return mergedVideoViewHolder;
            case LAST_ITEM:
                MergeLastItemViewHolder mergeLastItemViewHolder =
                        new MergeLastItemViewHolder(viewForMergeLastItem);
                return mergeLastItemViewHolder;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case MERGING:
                MergingVideoViewHolder mergingVideoViewHolder = (MergingVideoViewHolder) holder;
                mergingVideoViewHolder.mVideoName.setText(mMergeVideoModels.get(position).getVideoName());
                Glide.with(mContext).load(mMergeVideoModels.get(position).getVideoFile()).fitCenter()
                        .into(mergingVideoViewHolder.mVideoThumbnail);
                mergingVideoViewHolder.mVideoTime
                        .setText(mMergeVideoModels.get(position).getVideoDuration());
                if (mMergeVideoModels.get(position).getVideoMode() == MergeVideoModel.Mode.PLAY){
                    mergingVideoViewHolder.mVideoMode.setImageResource(R.drawable.ic_pause);
                    mergingVideoViewHolder.mPlayIndicator.setAlpha(1);
                }
                if (mMergeVideoModels.get(position).getVideoMode() == MergeVideoModel.Mode.PAUSE){
                    mergingVideoViewHolder.mVideoMode.setImageResource(R.drawable.ic_play);
                    mergingVideoViewHolder.mPlayIndicator.setAlpha(0);
                }
                break;
            case MERGED:
            case LAST_ITEM:
        }
    }

    @Override
    public int getItemCount() {
        return mMergeVideoModels.size();
    }

    public class MergingVideoViewHolder extends RecyclerView.ViewHolder{

        private ImageView mVideoThumbnail;
        private TextView mVideoName;
        private TextView mVideoTime;
        private ImageView mShare;
        private ImageView mVideoMode;
        private View mPlayIndicator;

        public MergingVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            mVideoThumbnail = itemView.findViewById(R.id.videoThumbnail);
            mVideoName = itemView.findViewById(R.id.videoName);
            mVideoTime = itemView.findViewById(R.id.videoTime);
            mShare = itemView.findViewById(R.id.ic_share);
            mVideoMode = itemView.findViewById(R.id.ic_videoMode);
            mPlayIndicator = itemView.findViewById(R.id.playIndicator);

            mVideoMode.setOnClickListener(v ->
                    mVideoPlayClickListener.onPlayClickListener(getAdapterPosition()));
        }
    }

    public class MergedVideoViewHolder extends RecyclerView.ViewHolder{

        public MergedVideoViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class MergeLastItemViewHolder extends RecyclerView.ViewHolder{

        private Button mMergeButton;

        public MergeLastItemViewHolder(@NonNull View itemView) {
            super(itemView);
            mMergeButton = itemView.findViewById(R.id.buttonMerge);

            mMergeButton.setOnClickListener(v ->
                    mButtonMergeClickListener.onButtonMergeClickListener());
        }
    }
}
