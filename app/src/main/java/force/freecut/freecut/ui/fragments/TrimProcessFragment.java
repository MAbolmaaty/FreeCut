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

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.FFprobe;
import com.arthenica.mobileffmpeg.MediaInformation;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import force.freecut.freecut.Data.TrimVideoModel;
import force.freecut.freecut.R;
import force.freecut.freecut.adapters.TrimmedVideosAdapter;
import force.freecut.freecut.view_models.ToolbarViewModel;
import force.freecut.freecut.view_models.TrimViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrimProcessFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrimProcessFragment extends Fragment {

    private static final String TAG = TrimProcessFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
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
    private long mFFmpegTrimProcessId;
    private int mLastClickedVideo;
    private TrimVideoModel[] mTrimVideoModels;
    private boolean mPaused;
    private boolean mTrimmingComplete;
    private RecyclerView mOutputVideos;
    private TrimmedVideosAdapter mTrimmedVideosAdapter;
    private Handler mUpdateVideoTimeHandler = new Handler();
    private Handler mHideVideoControlsHandler = new Handler();
    private ToolbarViewModel mToolbarViewModel;

    private int mProgressPerVideo;
    private int fps;

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

    public TrimProcessFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TrimProcessFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TrimProcessFragment newInstance(String param1, String param2) {
        TrimProcessFragment fragment = new TrimProcessFragment();
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
        View view = inflater.inflate(R.layout.fragment_trim_process, container, false);

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                RecyclerView.VERTICAL, false);
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

                        int numberOfVideos = (int) Math.ceil((double) videoDuration
                                / bundle.getInt(SEGMENT_TIME));

                        showVideoControls();
                        controlVideo();
                        controlVideoSeekbar();
                        controlVideoVoice();

                        mVideoSeekBar.setProgress(0);
                        mVideoSeekBar.setMax(mVideoView.getDuration());
                        mVideoTime.setText(String.format(Locale.ENGLISH, "%s / %s",
                                getVideoTime(0),
                                getVideoTime(videoDuration)));

                        // Calculate video fps
                        MediaInformation info = FFprobe.getMediaInformation(bundle.getString(VIDEO_PATH));
                        try {
                            JSONArray array = info.getAllProperties().getJSONArray("streams");
                            JSONObject object = array.getJSONObject(0);
                            int videoFrames = Integer.parseInt(object.getString("nb_frames"));
                            fps = videoFrames / videoDuration;
                            mProgressPerVideo = fps * bundle.getInt(SEGMENT_TIME);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        mTrimVideoModels = new TrimVideoModel[numberOfVideos];

                        for (int i = 0; i < numberOfVideos; i++) {
                            mTrimVideoModels[i] = new TrimVideoModel(null,
                                    String.format(Locale.ENGLISH, "video-%02d", i + 1),
                                    "", getString(R.string.waiting), 0,
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

                        mOutputVideos.setAdapter(mTrimmedVideosAdapter);

                        trim(bundle.getString(STORAGE_DIRECTORY), bundle.getString(VIDEO_PATH),
                                bundle.getInt(SEGMENT_TIME), videoDuration,
                                0, 1);
                    }
                });

                mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
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
    public void onDestroyView() {
        FFmpeg.cancel(mFFmpegTrimProcessId);
        super.onDestroyView();
    }

    private void trim(String storageDirectory, String videoPath,
                      int segmentTime, int videoDuration, int start, int counter) {
        if (start >= videoDuration) {
            File directory = new File(storageDirectory);
            File[] files = directory.listFiles();
            if (files != null && files.length > 1) {
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
            }
            mTrimmingComplete = true;
            return;
        }

        if (segmentTime + start > videoDuration) {
            mProgressPerVideo = fps * (videoDuration - start);
        }

        String name = String.format(Locale.ENGLISH, "video-%02d", counter);
        File file =
                new File(storageDirectory, name + ".mp4");

        String path = file.getAbsolutePath();

        final String[] trim = {
                "-ss", String.valueOf(start),
                "-i", videoPath,
                "-t", String.valueOf(segmentTime),
                "-c:v", "libx264",
                "-crf", "23",
                path};

        mTrimVideoModels[counter - 1].setTrimmingStatus(getString(R.string.trimming));
        mTrimmedVideosAdapter.notifyItemChanged(counter - 1);

        mFFmpegTrimProcessId = FFmpeg.executeAsync(trim, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    mTrimVideoModels[counter - 1].setVideoFile(file);
                    mTrimVideoModels[counter - 1].setProgress(100);
                    mTrimVideoModels[counter - 1].setVideoDuration(getVideoDuration(file));
                    mTrimmedVideosAdapter.notifyItemChanged(counter - 1);
                    trim(storageDirectory, videoPath,
                            segmentTime, videoDuration, start + segmentTime,
                            counter + 1);
                }
            }
        });

        Config.enableStatisticsCallback(new StatisticsCallback() {
            @Override
            public void apply(Statistics statistics) {
                double progress =
                        ((double) statistics.getVideoFrameNumber()
                                / mProgressPerVideo) * 100;

                if ((int) progress <= 100) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if ((TrimmedVideosAdapter.TrimmingVideoViewHolder)
                                        (mOutputVideos.findViewHolderForAdapterPosition(counter - 1))
                                        != null) {
                                    ((TrimmedVideosAdapter.TrimmingVideoViewHolder)
                                            mOutputVideos
                                                    .findViewHolderForAdapterPosition(counter - 1))
                                            .updateProgress((int) progress);
                                }
                            }
                        });
                    }
                }
            }
        });
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

    private void controlVideo() {
        mIcVideoControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIcVideoControl.getAlpha() == 0) {
                    mBlockSeekBar = false;
                    mVideoSeekBar.animate().alpha(1);
                    mViewShadow.animate().alpha(1);
                    mIcVideoControl.animate().alpha(1);
                    mVoiceControl.setClickable(true);
                    mVoiceControl.animate().alpha(1);
                    mVideoTime.animate().alpha(1);
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
                    mViewShadow.setAlpha(0);
                    mIcVideoControl.setAlpha((float) 0);
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
                    mViewShadow.animate().alpha(0);
                    mVideoTime.animate().alpha(0);
                    mVoiceControl.setClickable(false);
                    mVoiceControl.animate().alpha(0);
                    mVideoControlsVisible = false;
                } else {
                    mBlockSeekBar = false;
                    mVideoSeekBar.animate().alpha(1);
                    mIcVideoControl.animate().alpha(1);
                    mViewShadow.animate().alpha(1);
                    mVideoTime.animate().alpha(1);
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
}