package force.freecut.freecut.ui.fragments;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import static force.freecut.freecut.utils.Constants.MAIN_VIDEO;
import static force.freecut.freecut.utils.Constants.SEGMENT_TIME;
import static force.freecut.freecut.utils.Constants.STORAGE_DIRECTORY;
import static force.freecut.freecut.utils.Constants.TRIMMED_VIDEO;
import static force.freecut.freecut.utils.Constants.VIDEO_NAME;
import static force.freecut.freecut.utils.Constants.VIDEO_PATH;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.FFprobe;
import com.arthenica.mobileffmpeg.MediaInformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

import force.freecut.freecut.Data.TrimmedVideo;
import force.freecut.freecut.R;
import force.freecut.freecut.adapters.OutputVideosAdapter;
import force.freecut.freecut.view_models.ToolbarViewModel;
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
    private View mVideoViewBackground;
    private View mViewShadow;
    private AppCompatSeekBar mVideoSeekBar;
    private ImageView mIcVideoControl;
    private ImageView mVoiceControl;
    private TextView mVideoName;
    private boolean mVideoControlsVisible = false;
    private TextView mVideoTime;
    private boolean mBlockSeekBar = true;
    private boolean mVideoMuted = false;
    private MediaPlayer mMediaPlayer;
    private long mFFmpegProcessId;
    private int mLastClickedVideo;
    private TrimmedVideo[] mTrimmedVideos;
    private boolean mPaused;
    private boolean mTrimmingComplete;
    private RecyclerView mOutputVideos;
    private OutputVideosAdapter mVideosAdapter;
    private Handler mUpdateVideoTimeHandler = new Handler();
    private Handler mHideVideoControlsHandler = new Handler();
    private ToolbarViewModel mToolbarViewModel;

    private Runnable mUpdateVideoTimeRunnable = new Runnable() {
        @Override
        public void run() {
            long currentPosition = mVideoView.getCurrentPosition();
            mVideoSeekBar.setProgress((int) currentPosition);
            mVideoTime.setText(String.format(Locale.ENGLISH, "%s / %s",
                    getVideoTime((int) currentPosition / 1000),
                    getVideoTime(mVideoView.getDuration() / 1000)));
            mUpdateVideoTimeHandler.postDelayed(this, 100);
        }
    };

    private Runnable mHideVideoControlsRunnable = new Runnable() {
        @Override
        public void run() {
            if (mVideoView.isPlaying() && mVideoControlsVisible) {
                if (mVideoView.isPlaying() && mVideoControlsVisible) {
                    mVideoSeekBar.animate().alpha(0);
                    mBlockSeekBar = true;
                    mIcVideoControl.animate().alpha(0);
                    mViewShadow.animate().alpha(0);
                    mVideoTime.animate().alpha(0);
                    mVoiceControl.setClickable(false);
                    mVoiceControl.animate().alpha(0);
                    mVideoControlsVisible = false;
                }
            }
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
        mVideoViewBackground = view.findViewById(R.id.videoViewBackground);
        mViewShadow = view.findViewById(R.id.shadow);
        mVideoSeekBar = view.findViewById(R.id.videoSeekBar);
        mIcVideoControl = view.findViewById(R.id.icVideoControl);
        mVoiceControl = view.findViewById(R.id.voiceControl);
        mOutputVideos = view.findViewById(R.id.rv_videos);
        mVideoName = view.findViewById(R.id.videoName);
        mVideoTime = view.findViewById(R.id.videoTime);

        mLastClickedVideo = -1;

        mToolbarViewModel = ViewModelProviders.of(getActivity()).get(ToolbarViewModel.class);
        mToolbarViewModel.showBackButton(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mOutputVideos.setLayoutManager(layoutManager);
        mOutputVideos.setHasFixedSize(true);

        TrimViewModel trimViewModel = ViewModelProviders.of(getActivity()).get(TrimViewModel.class);
        trimViewModel.getTrimBundle().observe(this, new Observer<Bundle>() {
            @Override
            public void onChanged(Bundle bundle) {
                mVideoView.setVideoPath(bundle.getString(VIDEO_PATH));
                mVideoView.start();
                mVideoView.setVisibility(View.VISIBLE);
                mVideoViewBackground.setVisibility(View.VISIBLE);
                mVideoView.requestFocus();
                mVideoName.setText(bundle.getString(VIDEO_NAME));
                mVideoView.setTag(MAIN_VIDEO);

                mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        int videoDuration = mVideoView.getDuration() / 1000;
                        mUpdateVideoTimeHandler.postDelayed(mUpdateVideoTimeRunnable, 100);
                        if (mVideoView.getTag().equals(TRIMMED_VIDEO)) {

                            mIcVideoControl.setImageResource(R.drawable.ic_pause);
                            mVideoView.start();
                            mBlockSeekBar = true;
                            mVideoSeekBar.setAlpha(0);
                            mViewShadow.setAlpha(0);
                            mIcVideoControl.setAlpha((float) 0);
                            mVideoTime.setAlpha(0);
                            mVoiceControl.setClickable(false);
                            mVoiceControl.setAlpha((float) 0);
                            mVideoControlsVisible = false;
                            mVideoSeekBar.setProgress(0);
                            mVideoSeekBar.setMax(mVideoView.getDuration());
                            mVideoTime.setText(String.format(Locale.ENGLISH, "%s / %s",
                                    getVideoTime(0),
                                    getVideoTime(videoDuration)));
                            return;
                        }

                        if (mPaused)
                            return;

                        int numberOfVideos = (int) Math.ceil((double) videoDuration
                                / bundle.getInt(SEGMENT_TIME));

                        showVideoControls();
                        controlVideo();
                        controlVideoSeekbar();
                        controlVideoVoice();

                        mMediaPlayer = mp;
                        mVideoSeekBar.setProgress(0);
                        mVideoSeekBar.setMax(mVideoView.getDuration());
                        mVideoTime.setText(String.format(Locale.ENGLISH, "%s / %s",
                                getVideoTime(0),
                                getVideoTime(videoDuration)));

                        mTrimmedVideos = new TrimmedVideo[numberOfVideos];

                        for (int i = 0; i < numberOfVideos; i++) {
                            mTrimmedVideos[i] = new TrimmedVideo(null,
                                    String.format(Locale.ENGLISH, "video-%02d", i + 1),
                                    "", getString(R.string.waiting), 0,
                                    TrimmedVideo.Mode.PAUSE);
                        }

                        mVideosAdapter = new OutputVideosAdapter(getActivity(), mTrimmedVideos,
                                new OutputVideosAdapter.VideoPlayClickListener() {
                                    @Override
                                    public void onPlayClickListener(int videoClicked) {
                                        mVideoView.setTag(TRIMMED_VIDEO);
                                        mVideoView.setVideoPath(mTrimmedVideos[videoClicked]
                                                .getVideoFile().getAbsolutePath());
                                        mVideoName.setText(mTrimmedVideos[videoClicked]
                                                .getVideoName());

                                        if (mLastClickedVideo != -1) {
                                            mTrimmedVideos[mLastClickedVideo]
                                                    .setVideoMode(TrimmedVideo.Mode.PAUSE);
                                            mVideosAdapter.notifyItemChanged(mLastClickedVideo);
                                        }

                                        mLastClickedVideo = videoClicked;
                                        mTrimmedVideos[videoClicked]
                                                .setVideoMode(TrimmedVideo.Mode.PLAY);
                                        mVideosAdapter.notifyItemChanged(videoClicked);
                                    }
                                }, new OutputVideosAdapter.VideoShareClickListener() {
                            @Override
                            public void onShareClickListener(int videoClicked) {
                                if (mTrimmingComplete)
                                    shareVideo(mTrimmedVideos[videoClicked].getVideoFile()
                                            .getAbsolutePath());
                            }
                        });

                        mOutputVideos.setAdapter(mVideosAdapter);

                        speedTrim(bundle.getString(STORAGE_DIRECTORY), bundle.getString(VIDEO_PATH),
                                bundle.getInt(SEGMENT_TIME));
                    }
                });

            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        FFmpeg.cancel(mFFmpegProcessId);
        super.onDestroy();
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

    private void speedTrim(String storageDirectory, String videoPath, int segmentTime){

        File file =
                new File(storageDirectory, "video-%02d.mp4");

        String path = file.getAbsolutePath();

        final String[] speedTrim = {
                "-i", videoPath,
                "-codec:a", "copy",
                "-f", "segment", "-segment_time", String.valueOf(segmentTime),
                "-codec:v", "copy",
                "-map", "0", path};

        long executionId = FFmpeg.executeAsync(speedTrim, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {

                } else {

                }
            }
        });
    }

    private void controlVideo(){
        mIcVideoControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIcVideoControl.getAlpha() == 0) {
                    mBlockSeekBar = false;
                    mVideoSeekBar.animate().alpha(1);
                    mIcVideoControl.animate().alpha(1);
                    mVoiceControl.setClickable(true);
                    mVoiceControl.animate().alpha(1);
                    mVideoTime.animate().alpha(1);
                    mVideoControlsVisible = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mVideoView.isPlaying() && mVideoControlsVisible) {
                                mVideoSeekBar.animate().alpha(0);
                                mBlockSeekBar = true;
                                mIcVideoControl.animate().alpha(0);
                                mVideoTime.animate().alpha(0);
                                mVoiceControl.setClickable(false);
                                mVoiceControl.animate().alpha(0);
                                mVideoControlsVisible = false;
                            }
                        }
                    }, 3000);
                    return;
                }
                if (mVideoView.isPlaying()) {
                    mIcVideoControl.setImageResource(R.drawable.ic_play);
                    mVideoView.pause();
                } else {
                    mIcVideoControl.setImageResource(R.drawable.ic_pause);
                    mVideoView.start();
                    mBlockSeekBar = true;
                    mVideoSeekBar.setAlpha(0);
                    mIcVideoControl.setAlpha((float) 0);
                    mVideoTime.setAlpha(0);
                    mVoiceControl.setClickable(false);
                    mVoiceControl.setAlpha((float) 0);
                    mVideoControlsVisible = false;
                }
            }
        });
    }

    private void showVideoControls(){
        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVideoControlsVisible) {
                    mVideoSeekBar.animate().alpha(0);
                    mBlockSeekBar = true;
                    mIcVideoControl.animate().alpha(0);
                    mVideoTime.animate().alpha(0);
                    mVoiceControl.setClickable(false);
                    mVoiceControl.animate().alpha(0);
                    mVideoControlsVisible = false;
                } else {
                    mBlockSeekBar = false;
                    mVideoSeekBar.animate().alpha(1);
                    mIcVideoControl.animate().alpha(1);
                    mVideoTime.animate().alpha(1);
                    mVoiceControl.setClickable(true);
                    mVoiceControl.animate().alpha(1);
                    mVideoControlsVisible = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mVideoView.isPlaying() && mVideoControlsVisible) {
                                mVideoSeekBar.animate().alpha(0);
                                mBlockSeekBar = true;
                                mIcVideoControl.animate().alpha(0);
                                mVideoTime.animate().alpha(0);
                                mVoiceControl.setClickable(false);
                                mVoiceControl.animate().alpha(0);
                                mVideoControlsVisible = false;
                            }
                        }
                    }, 3000);
                }
            }
        });
    }

    private void controlVideoSeekbar(){
        mVideoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mVideoView.seekTo(progress);
                    mVideoTime.setText(String.format(Locale.ENGLISH, "%s / %s",
                            getVideoTime(progress / 1000),
                            getVideoTime(mVideoView.getDuration() / 1000)));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mVideoSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mBlockSeekBar;
            }
        });
    }

    private void controlVideoVoice(){
        mVoiceControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoMuted) {
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                    mVoiceControl.setImageResource(R.drawable.ic_speaker);
                    mVideoMuted = false;
                } else {
                    mMediaPlayer.setVolume(0, 0);
                    mVoiceControl.setImageResource(R.drawable.ic_silent);
                    mVideoMuted = true;
                }
            }
        });
    }

    private void shareVideo(String videoPath) {
        Uri uri = FileProvider.getUriForFile(getActivity(),
                getActivity().getApplicationContext()
                        .getPackageName() + ".provider", new File(videoPath));

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        getActivity().startActivity(Intent.createChooser(shareIntent, ""));
    }
}