package force.freecut.freecut.ui.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import force.freecut.freecut.R;
import force.freecut.freecut.adapters.OutputVideosAdapter;
import force.freecut.freecut.view_models.TrimViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SpeedTrimProcessFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SpeedTrimProcessFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = SpeedTrimProcessFragment.class.getSimpleName();

    private String mParam1;
    private String mParam2;

    private VideoView mVideoView;
    private AppCompatSeekBar mVideoSeekBar;
    private ImageView mIcVideoControl;
    private ImageView mVoiceControl;
    private TextView mVideoName;
    private RecyclerView mOutputVideos;
    private OutputVideosAdapter mVideosAdapter;

    private Handler updateHandler = new Handler();
    private boolean mVideoControlsVisible = false;
    private TextView mVideoTime;
    private boolean mBlockSeekBar = true;
    private boolean mVideoMuted = false;
    private MediaPlayer mMediaPlayer;

    private Runnable updateVideoTime = new Runnable() {
        @Override
        public void run() {
            long currentPosition = mVideoView.getCurrentPosition();
            mVideoSeekBar.setProgress((int) currentPosition);
            mVideoTime.setText(String.format(Locale.ENGLISH, "%s / %s",
                    getVideoTime((int) currentPosition / 1000),
                    getVideoTime(mVideoView.getDuration() / 1000)));
            updateHandler.postDelayed(this, 100);
        }
    };

    public SpeedTrimProcessFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SpeedTrimProcessFragment.
     */
    public static SpeedTrimProcessFragment newInstance(String param1, String param2) {
        SpeedTrimProcessFragment fragment = new SpeedTrimProcessFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_speed_trim_process, container, false);

        mVideoView = view.findViewById(R.id.videoView);
        mVideoSeekBar = view.findViewById(R.id.videoSeekBar);
        mIcVideoControl = view.findViewById(R.id.icVideoControl);
        mVoiceControl = view.findViewById(R.id.voiceControl);
        mOutputVideos = view.findViewById(R.id.rv_videos);
        mVideoName = view.findViewById(R.id.videoName);
        mVideoTime = view.findViewById(R.id.videoTime);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mOutputVideos.setLayoutManager(layoutManager);
        mOutputVideos.setHasFixedSize(true);

        TrimViewModel trimViewModel = ViewModelProviders.of(getActivity()).get(TrimViewModel.class);
        trimViewModel.getTrimBundle().observe(this, new Observer<Bundle>() {
            @Override
            public void onChanged(Bundle bundle) {
//                String file =
//                        new File(directory, mVideoName + "-st-" +
//                                mNumberOfSeconds.getText().toString() + "-" + "%02d" + ".mp4")
//                                .getAbsolutePath();
//                final String[] commandTrim = {"-i", bundle.getString(VIDEO_PATH),
//                        "-codec:a", "copy", "-f", "segment", "-segment_time",
//                        bundle.getString(SEGMENT_TIME), "-codec:v", "copy",
//                        "-map", "0", bundle.getString(FILE_PATH)};
//                long executionId = FFmpeg.executeAsync(commandTrim, new ExecuteCallback() {
//                    @Override
//                    public void apply(long executionId, int returnCode) {
//                        if (returnCode == RETURN_CODE_SUCCESS) {
//
//                        }
//                    }
//                });
            }
        });

        return view;
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