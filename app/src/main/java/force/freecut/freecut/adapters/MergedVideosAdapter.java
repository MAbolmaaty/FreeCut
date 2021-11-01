package force.freecut.freecut.adapters;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import force.freecut.freecut.Data.MergeVideoModel;

public class MergedVideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = MergedVideosAdapter.class.getSimpleName();

    private Context mContext;
    private MergeVideoModel[] mMergeVideoModels;
    private VideoPlayClickListener mVideoPlayClickListener;
    private VideoShareClickListener mVideoShareClickListener;
    private static final int MERGING = 0;
    private static final int MERGED = 1;
    private static final int LAST_ITEM = 2;

    public interface VideoPlayClickListener {
        void onPlayClickListener(int videoClicked);
    }

    public interface VideoShareClickListener {
        void onShareClickListener(int videoClicked);
    }

    public MergedVideosAdapter(Context context, MergeVideoModel[] mergeVideoModels,
                               VideoPlayClickListener videoPlayClickListener,
                               VideoShareClickListener videoShareClickListener) {
        mContext = context;
        mMergeVideoModels = mergeVideoModels;
        mVideoPlayClickListener = videoPlayClickListener;
        mVideoShareClickListener = videoShareClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mMergeVideoModels[position].getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
