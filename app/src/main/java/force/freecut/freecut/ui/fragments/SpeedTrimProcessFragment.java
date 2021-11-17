package force.freecut.freecut.ui.fragments;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static force.freecut.freecut.utils.Constants.MAIN_VIDEO;
import static force.freecut.freecut.utils.Constants.SEGMENT_TIME;
import static force.freecut.freecut.utils.Constants.STORAGE_DIRECTORY;
import static force.freecut.freecut.utils.Constants.TRIMMED_VIDEO;
import static force.freecut.freecut.utils.Constants.VIDEO_NAME;
import static force.freecut.freecut.utils.Constants.VIDEO_PATH;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
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
import com.todkars.shimmer.ShimmerRecyclerView;

import java.io.File;
import java.util.Locale;

import force.freecut.freecut.Data.TrimVideoModel;
import force.freecut.freecut.R;
import force.freecut.freecut.adapters.TrimmedVideosAdapter;
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
    private long mFFmpegSpeedTrimProcessId;
    private long mFFmpegSeekToZeroProcessId;
    private int mLastClickedVideo;
    private TrimVideoModel[] mTrimVideoModels;
    private boolean mPaused;
    private boolean mTrimmingComplete;
    private RecyclerView mOutputVideos;
    private ShimmerRecyclerView mShimmerRecyclerView;
    private TrimmedVideosAdapter mTrimmedVideosAdapter;
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
            mUpdateVideoTimeHandler.postDelayed(this, 10);
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
        View view = inflater.inflate(R.layout.fragment_speed_trim_process,
                container, false);

        mVideoView = view.findViewById(R.id.videoView);
        mVideoViewBackground = view.findViewById(R.id.videoViewBackground);
        mViewShadow = view.findViewById(R.id.shadow);
        mVideoSeekBar = view.findViewById(R.id.videoSeekBar);
        mIcVideoControl = view.findViewById(R.id.icVideoControl);
        mVoiceControl = view.findViewById(R.id.voiceControl);
        mOutputVideos = view.findViewById(R.id.rv_videos);
        mShimmerRecyclerView = view.findViewById(R.id.shimmer_recycler_view);
        mVideoName = view.findViewById(R.id.videoName);
        mVideoTime = view.findViewById(R.id.videoTime);

        mLastClickedVideo = -1;

        mToolbarViewModel = ViewModelProviders.of(getActivity()).get(ToolbarViewModel.class);
        mToolbarViewModel.setToolbarTitle(getString(R.string.speed_trim));
        mToolbarViewModel.showBackButton(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                RecyclerView.VERTICAL, false);
        mOutputVideos.setLayoutManager(layoutManager);
        mOutputVideos.setHasFixedSize(true);

        mOutputVideos.setVisibility(View.GONE);
        mShimmerRecyclerView.showShimmer();

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
                        mMediaPlayer = mp;

                        if (mPaused)
                            return;

                        int videoDuration = mVideoView.getDuration() / 1000;
                        mUpdateVideoTimeHandler.postDelayed(mUpdateVideoTimeRunnable, 10);
                        mVoiceControl.setImageResource(R.drawable.ic_speaker);
                        mVideoMuted = false;
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

                        showVideoControls();
                        controlVideo();
                        controlVideoSeekbar();
                        controlVideoVoice();

                        mVideoSeekBar.setProgress(0);
                        mVideoSeekBar.setMax(mVideoView.getDuration());
                        mVideoTime.setText(String.format(Locale.ENGLISH, "%s / %s",
                                getVideoTime(0),
                                getVideoTime(videoDuration)));

                        speedTrim(bundle.getString(STORAGE_DIRECTORY), bundle.getString(VIDEO_PATH),
                                bundle.getInt(SEGMENT_TIME));
                    }
                });

                mVideoView.setOnCompletionListener(mp -> {
                    mUpdateVideoTimeHandler.removeCallbacks(mUpdateVideoTimeRunnable);
                    mBlockSeekBar = false;
                    mVideoSeekBar.animate().alpha(1);
                    mViewShadow.animate().alpha(1);
                    mIcVideoControl.animate().alpha(1);
                    mVoiceControl.setClickable(true);
                    mVoiceControl.animate().alpha(1);
                    mVideoTime.animate().alpha(1);
                    mVideoControlsVisible = true;
                    mIcVideoControl.setImageResource(R.drawable.ic_play);
                    if (mLastClickedVideo != -1) {
                        mTrimVideoModels[mLastClickedVideo].setVideoMode(TrimVideoModel.Mode.PAUSE);
                        mTrimmedVideosAdapter.notifyItemChanged(mLastClickedVideo);
                    }
                });

            }
        });

        return view;
    }

    @Override
    public void onPause() {
        mPaused = true;
        mVideoView.pause();
        mBlockSeekBar = false;
        mVideoSeekBar.animate().alpha(1);
        mViewShadow.animate().alpha(1);
        mIcVideoControl.animate().alpha(1);
        mVoiceControl.setClickable(true);
        mVoiceControl.animate().alpha(1);
        mVideoTime.animate().alpha(1);
        mVideoControlsVisible = true;
        mIcVideoControl.setImageResource(R.drawable.ic_play);
        mVoiceControl.setImageResource(R.drawable.ic_speaker);
        if (mLastClickedVideo != -1) {
            mTrimVideoModels[mLastClickedVideo].setVideoMode(TrimVideoModel.Mode.PAUSE);
            mTrimmedVideosAdapter.notifyItemChanged(mLastClickedVideo);
        }
        mUpdateVideoTimeHandler.removeCallbacks(mUpdateVideoTimeRunnable);
        mHideVideoControlsHandler.removeCallbacks(mHideVideoControlsRunnable);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        FFmpeg.cancel(mFFmpegSpeedTrimProcessId);
        FFmpeg.cancel(mFFmpegSeekToZeroProcessId);
        super.onDestroy();
    }

    private void speedTrim(String storageDirectory, String videoPath, int segmentTime) {

        File initialStorageDirectory =
                new File(storageDirectory, "initial/");

        if (initialStorageDirectory.exists()) {
            for (File video : initialStorageDirectory.listFiles()) {
                video.delete();
            }
        }

        initialStorageDirectory.mkdirs();

        File initialFile =
                new File(initialStorageDirectory, "%02d.mp4");

        final String[] speedTrim = {
                "-i", videoPath,
                "-codec:a", "copy",
                "-f", "segment", "-segment_time", String.valueOf(segmentTime),
                "-codec:v", "copy",
                "-map", "0",
                initialFile.getAbsolutePath()};

        mFFmpegSpeedTrimProcessId = FFmpeg.executeAsync(speedTrim, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d(TAG, "speed trim success");
                    seekVideoToZero(initialStorageDirectory, new File(storageDirectory),
                            1);
                } else {
                    Log.d(TAG, "speed trim fail");
                }
            }
        });
    }

    private void seekVideoToZero(File initialStorageDirectory,
                                 File destinationStorageDirectory,
                                 int counter){

        if (initialStorageDirectory.listFiles().length == 0){
            initialStorageDirectory.delete();
            mTrimVideoModels = new TrimVideoModel[destinationStorageDirectory.listFiles().length];
            for (int i = 0; i <destinationStorageDirectory.listFiles().length ; i++) {
                mTrimVideoModels[i] =
                        new TrimVideoModel(destinationStorageDirectory.listFiles()[i],
                                destinationStorageDirectory.listFiles()[i].getName(),
                                getVideoDuration(destinationStorageDirectory.listFiles()[i]),
                                getString(R.string.trimming), 100,
                                TrimVideoModel.Mode.PAUSE);
            }

            mTrimmedVideosAdapter = new TrimmedVideosAdapter(getActivity(), mTrimVideoModels,
                    new TrimmedVideosAdapter.VideoPlayClickListener() {
                        @Override
                        public void onPlayClickListener(int videoClicked) {
                            mVideoView.setTag(TRIMMED_VIDEO);
                            mVideoView.setVideoPath(mTrimVideoModels[videoClicked]
                                    .getVideoFile().getAbsolutePath());
                            mPaused = false;
                            mVideoName.setText(mTrimVideoModels[videoClicked]
                                    .getVideoName());

                            if (mLastClickedVideo != -1) {
                                mTrimVideoModels[mLastClickedVideo]
                                        .setVideoMode(TrimVideoModel.Mode.PAUSE);
                                mTrimmedVideosAdapter.notifyItemChanged(mLastClickedVideo);
                            }

                            mLastClickedVideo = videoClicked;
                            mTrimVideoModels[videoClicked]
                                    .setVideoMode(TrimVideoModel.Mode.PLAY);
                            mTrimmedVideosAdapter.notifyItemChanged(videoClicked);
                        }
                    }, new TrimmedVideosAdapter.VideoShareClickListener() {
                @Override
                public void onShareClickListener(int videoClicked) {
                    if (mTrimmingComplete)
                        shareVideo(mTrimVideoModels[videoClicked].getVideoFile()
                                .getAbsolutePath());
                }
            });

            mShimmerRecyclerView.hideShimmer();
            mOutputVideos.setVisibility(View.VISIBLE);
            mShimmerRecyclerView.setVisibility(View.GONE);
            mOutputVideos.setAdapter(mTrimmedVideosAdapter);
            mTrimmingComplete = true;
            return;
        }

        String name = String.format(Locale.ENGLISH, "video-%02d",
                counter);
        File file =
                new File(destinationStorageDirectory, name + ".mp4");

        final String[] seekToZero = {
                "-ss", "0",
                "-i", initialStorageDirectory.listFiles()[0].getAbsolutePath(),
                "-c:v", "copy",
                "-c:a", "copy",
                file.getAbsolutePath()};

        mFFmpegSeekToZeroProcessId = FFmpeg.executeAsync(seekToZero, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d(TAG, "seek to zero success");
                    initialStorageDirectory.listFiles()[0].delete();
                    seekVideoToZero(initialStorageDirectory, destinationStorageDirectory,
                            counter+1);
                } else {
                    Log.d(TAG, "seek to zero failed");
                }
            }
        });
    }

    private void controlVideo() {
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
                    mViewShadow.animate().alpha(1);
                    mVideoControlsVisible = true;
                    mHideVideoControlsHandler.postDelayed(mHideVideoControlsRunnable,
                            3000);
                    return;
                }
                if (mVideoView.isPlaying()) {
                    mIcVideoControl.setImageResource(R.drawable.ic_play);
                    mVideoView.pause();
                    mUpdateVideoTimeHandler.removeCallbacks(mUpdateVideoTimeRunnable);
                    if (mLastClickedVideo != -1) {
                        mTrimVideoModels[mLastClickedVideo].setVideoMode(TrimVideoModel.Mode.PAUSE);
                        mTrimmedVideosAdapter.notifyItemChanged(mLastClickedVideo);
                    }
                } else {
                    mIcVideoControl.setImageResource(R.drawable.ic_pause);
                    mVideoView.start();
                    mUpdateVideoTimeHandler.postDelayed(mUpdateVideoTimeRunnable, 10);
                    mBlockSeekBar = true;
                    mVideoSeekBar.setAlpha(0);
                    mIcVideoControl.setAlpha((float) 0);
                    mViewShadow.setAlpha(0);
                    mVideoTime.setAlpha(0);
                    mVoiceControl.setClickable(false);
                    mVoiceControl.setAlpha((float) 0);
                    mVideoControlsVisible = false;
                    if (mLastClickedVideo != -1) {
                        mTrimVideoModels[mLastClickedVideo].setVideoMode(TrimVideoModel.Mode.PLAY);
                        mTrimmedVideosAdapter.notifyItemChanged(mLastClickedVideo);
                    }
                }
            }
        });
    }

    private void showVideoControls() {
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mVideoControlsVisible) {
                    mVideoSeekBar.animate().alpha(0);
                    mBlockSeekBar = true;
                    mIcVideoControl.animate().alpha(0);
                    mVideoTime.animate().alpha(0);
                    mViewShadow.animate().alpha(0);
                    mVoiceControl.setClickable(false);
                    mVoiceControl.animate().alpha(0);
                    mVideoControlsVisible = false;
                } else {
                    mBlockSeekBar = false;
                    mVideoSeekBar.animate().alpha(1);
                    mIcVideoControl.animate().alpha(1);
                    mVideoTime.animate().alpha(1);
                    mViewShadow.animate().alpha(1);
                    mVoiceControl.setClickable(true);
                    mVoiceControl.animate().alpha(1);
                    mVideoControlsVisible = true;
                    mHideVideoControlsHandler.postDelayed(mHideVideoControlsRunnable,
                            3000);
                }
                return false;
            }
        });
    }

    private void controlVideoSeekbar() {
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
                mVideoView.pause();
                mHideVideoControlsHandler.removeCallbacks(mHideVideoControlsRunnable);
                mUpdateVideoTimeHandler.removeCallbacks(mUpdateVideoTimeRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mUpdateVideoTimeHandler.postDelayed(mUpdateVideoTimeRunnable, 10);
                mVideoView.start();
                mHideVideoControlsHandler.postDelayed(mHideVideoControlsRunnable,
                        3000);
                mIcVideoControl.setImageResource(R.drawable.ic_pause);
                if (mLastClickedVideo != -1) {
                    mTrimVideoModels[mLastClickedVideo].setVideoMode(TrimVideoModel.Mode.PLAY);
                    mTrimmedVideosAdapter.notifyItemChanged(mLastClickedVideo);
                }
            }
        });

        mVideoSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mBlockSeekBar;
            }
        });
    }

    private void controlVideoVoice() {
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

    private String getVideoDuration(File videoFile) {
        if (videoFile == null)
            return "";
        String videoPath = videoFile.getAbsolutePath();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        String time =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int seconds = Integer.parseInt(time) / 1000;
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