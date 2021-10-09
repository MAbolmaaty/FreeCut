package force.freecut.freecut.ui.fragments;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static force.freecut.freecut.utils.Constants.SEGMENT_TIME;
import static force.freecut.freecut.utils.Constants.STORAGE_DIRECTORY;
import static force.freecut.freecut.utils.Constants.VIDEO_DURATION;
import static force.freecut.freecut.utils.Constants.VIDEO_NAME;
import static force.freecut.freecut.utils.Constants.VIDEO_PATH;
import static force.freecut.freecut.utils.Constants.VIDEO_URI;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.widget.AppCompatSeekBar;
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

import force.freecut.freecut.R;
import force.freecut.freecut.adapters.OutputVideosAdapter;
import force.freecut.freecut.view_models.TrimViewModel;
import force.freecut.freecut.view_models.VideoStatisticsViewModel;

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
    private AppCompatSeekBar mVideoSeekBar;
    private ImageView mIcVideoControl;
    private TextView mVideoName;
    private RecyclerView mOutputVideos;
    private OutputVideosAdapter mVideosAdapter;
    private int mProgressPerVideo;
    private int fps;
    private VideoStatisticsViewModel mVideoStatisticsViewModel;
    private Handler updateHandler = new Handler();
    private boolean mVideoControlsVisible = false;

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
        mVideoSeekBar = view.findViewById(R.id.videoSeekBar);
        mIcVideoControl = view.findViewById(R.id.ic_video_control);
        mOutputVideos = view.findViewById(R.id.rv_videos);
        mVideoName = view.findViewById(R.id.videoName);

        mVideoStatisticsViewModel =
                ViewModelProviders.of(this).get(VideoStatisticsViewModel.class);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mOutputVideos.setLayoutManager(layoutManager);
        mOutputVideos.setHasFixedSize(true);

        TrimViewModel trimViewModel = ViewModelProviders.of(getActivity()).get(TrimViewModel.class);
        trimViewModel.getTrimBundle().observe(this, new Observer<Bundle>() {
            @Override
            public void onChanged(Bundle bundle) {
                mVideoName.setText(bundle.getString(VIDEO_NAME));
                int numberOfVideos = (int) Math.ceil((double) bundle.getInt(VIDEO_DURATION)
                        / bundle.getInt(SEGMENT_TIME));

                mVideosAdapter = new OutputVideosAdapter(getActivity(), numberOfVideos);
                mOutputVideos.setAdapter(mVideosAdapter);

                MediaInformation info = FFprobe.getMediaInformation(bundle.getString(VIDEO_PATH));
                try {
                    JSONArray array = info.getAllProperties().getJSONArray("streams");
                    JSONObject object = array.getJSONObject(0);
                    int videoFrames = Integer.parseInt(object.getString("nb_frames"));
                    fps = videoFrames / bundle.getInt(VIDEO_DURATION);
                    mProgressPerVideo = fps * bundle.getInt(SEGMENT_TIME);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                trim(bundle.getString(STORAGE_DIRECTORY), bundle.getString(VIDEO_PATH),
                        bundle.getInt(SEGMENT_TIME), bundle.getInt(VIDEO_DURATION),
                        0, 1);

                mVideoView.setVideoURI(Uri.parse(bundle.getString(VIDEO_URI)));
                mVideoView.start();
                mVideoView.setVisibility(View.VISIBLE);
                mVideoView.requestFocus();

                mIcVideoControl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mVideoView.isPlaying()) {
                            mIcVideoControl.setImageResource(R.drawable.ic_pause);
                            //mIcVideoControl.setVisibility(View.VISIBLE);
                            mVideoView.pause();
                        } else {
                            mIcVideoControl.setImageResource(R.drawable.ic_play);
                            //mIcVideoControl.setVisibility(View.VISIBLE);
//                            mVideoView.setClickable(false);
//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mIcVideoControl.setVisibility(View.INVISIBLE);
//                                    mVideoView.setClickable(true);
//                                }
//                            }, 1200);
                            mVideoView.start();
                            mVideoSeekBar.setVisibility(View.INVISIBLE);
                            mIcVideoControl.setVisibility(View.INVISIBLE);
                            mVideoControlsVisible = false;
                        }
                    }
                });

                mVideoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mVideoControlsVisible) {
                            mVideoSeekBar.setVisibility(View.INVISIBLE);
                            mIcVideoControl.setVisibility(View.INVISIBLE);
                            mVideoControlsVisible = false;
                        } else {
                            mVideoSeekBar.setVisibility(View.VISIBLE);
                            mIcVideoControl.setVisibility(View.VISIBLE);
                            mVideoControlsVisible = true;
                        }
                    }
                });

                mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mVideoSeekBar.setProgress(0);
                        mVideoSeekBar.setMax(mVideoView.getDuration());
                        updateHandler.postDelayed(updateVideoTime, 100);
                    }
                });

                mVideoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mVideoView.seekTo(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            }
        });
        return view;
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
            return;
        }

        if (segmentTime + start > videoDuration) {
            mProgressPerVideo = fps * (videoDuration - start);
        }

        String name = String.format(Locale.ENGLISH, "trim-%02d", counter);
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

        FFmpeg.executeAsync(trim, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    mVideosAdapter.setVideoProgress(counter - 1, 100);
                    mVideosAdapter.setVideoPath(counter - 1, file);
                    mVideoStatisticsViewModel.setVideoStatisticsStatus(false);
                    trim(storageDirectory, videoPath,
                            segmentTime, videoDuration, start + segmentTime,
                            counter + 1);
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mVideoStatisticsViewModel.setVideoStatisticsStatus(true);
            }
        }, 500);


        mVideoStatisticsViewModel.getVideoStatisticsStatus().observe(TrimProcessFragment.this,
                new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean status) {
                        Config.enableStatisticsCallback(new StatisticsCallback() {
                            @Override
                            public void apply(Statistics statistics) {

                                double progress =
                                        ((double) statistics.getVideoFrameNumber()
                                                / mProgressPerVideo) * 100;

                                if ((int) progress == 3) {
                                    mVideosAdapter.setVideoProgress(counter - 1, (int) progress);
                                }

                                if (status && (int) progress <= 100) {
                                    if ((OutputVideosAdapter.OutputVideoViewHolder)
                                            (mOutputVideos.findViewHolderForAdapterPosition(counter - 1)) != null) {
                                        ((OutputVideosAdapter.OutputVideoViewHolder)
                                                mOutputVideos.findViewHolderForAdapterPosition(counter - 1))
                                                .updateProgressBar((int) progress);
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((OutputVideosAdapter.OutputVideoViewHolder)
                                                        mOutputVideos.findViewHolderForAdapterPosition(counter - 1))
                                                        .updatePercentage((int) progress);
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                });
    }

    private Runnable updateVideoTime = new Runnable() {
        @Override
        public void run() {
            long currentPosition = mVideoView.getCurrentPosition();
            mVideoSeekBar.setProgress((int) currentPosition);
            updateHandler.postDelayed(this, 100);
        }
    };
}