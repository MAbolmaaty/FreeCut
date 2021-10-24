package force.freecut.freecut.ui.fragments;

import static android.app.Activity.RESULT_OK;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static force.freecut.freecut.ui.activities.MainActivity.loadFragment;
import static force.freecut.freecut.utils.Constants.PROCESS_SPEED_TRIM;
import static force.freecut.freecut.utils.Constants.PROCESS_TRIM;
import static force.freecut.freecut.utils.Constants.SEGMENT_TIME;
import static force.freecut.freecut.utils.Constants.STORAGE_DIRECTORY;
import static force.freecut.freecut.utils.Constants.VIDEO_NAME;
import static force.freecut.freecut.utils.Constants.VIDEO_PATH;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;

import force.freecut.freecut.Data.TinyDB;
import force.freecut.freecut.Data.TrimmedVideo;
import force.freecut.freecut.Data.Utils;
import force.freecut.freecut.R;
import force.freecut.freecut.helper.PreferenceHelper;
import force.freecut.freecut.view_models.MainViewPagerSwipingViewModel;
import force.freecut.freecut.view_models.ToolbarViewModel;
import force.freecut.freecut.view_models.TrimViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrimFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrimFragment extends Fragment {
    private static final String TAG = TrimFragment.class.getSimpleName();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mVideoPath;
    private String mVideoName;

    private String mParam1;
    private String mParam2;

    boolean fault = false;
    int fileno = 1;
    String filePath, fileExtn_mp4;
    Uri mSelectedVideoUri;
    String mypath;
    // TinyDB : Implementation for Shared Preferences
    TinyDB tinydb;
    File dest;
    String filePrefix;
    String mylink = "https://www.google.com/";

    ImageView mBanner;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    String saldo = "";

    private PreferenceHelper helpers;

    private TrimViewModel mTrimViewModel;
    private Toast mToast;
    private ImageView mIcVideo;
    private Button mOpenGallery;
    private TextView mPickUpTrim;

    private VideoView mVideoView;
    private View mViewShadow;
    private ImageView mIcShare;
    private ImageView mIcRemove;
    private AppCompatSeekBar mVideoSeekBar;
    private ImageView mIcVideoControl;
    private ImageView mVoiceControl;
    private TextView mVideoTime;
    private TextView mTextViewVideoName;
    private TextView mEnterSeconds;
    private EditText mNumberOfSeconds;
    private Button mTrim;
    private Button mSpeedTrim;
    private View mVideoViewBackground;
    private ToolbarViewModel mToolbarViewModel;
    private MainViewPagerSwipingViewModel mMainViewPagerSwipingViewModel;
    private boolean mBlockSeekBar = true;
    private boolean mVideoControlsVisible = false;
    private boolean mVideoMuted = false;
    private MediaPlayer mMediaPlayer;
    private Handler mUpdateVideoTimeHandler = new Handler();
    private Handler mHideVideoControlsHandler = new Handler();

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
                showVideoControls(false);
            }
        }
    };

    public TrimFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TrimFragment.
     */
    public static TrimFragment newInstance(String param1, String param2) {
        TrimFragment fragment = new TrimFragment();
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
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trim, container, false);
        mToolbarViewModel = ViewModelProviders.of(getActivity()).get(ToolbarViewModel.class);
        mToolbarViewModel.showBackButton(false);
        mTrimViewModel = ViewModelProviders.of(getActivity()).get(TrimViewModel.class);
        mMainViewPagerSwipingViewModel = ViewModelProviders.of(getActivity())
                .get(MainViewPagerSwipingViewModel.class);

        mBanner = view.findViewById(R.id.banner);
        mIcVideo = view.findViewById(R.id.icVideo);
        mOpenGallery = view.findViewById(R.id.openGallery);
        mPickUpTrim = view.findViewById(R.id.pickUpTrim);
        mVideoView = view.findViewById(R.id.videoView);
        mVideoViewBackground = view.findViewById(R.id.videoViewBackground);
        mViewShadow = view.findViewById(R.id.shadow);
        mIcShare = view.findViewById(R.id.ic_share);
        mIcRemove = view.findViewById(R.id.ic_remove);
        mVideoSeekBar = view.findViewById(R.id.videoSeekBar);
        mIcVideoControl = view.findViewById(R.id.icVideoControl);
        mVideoTime = view.findViewById(R.id.videoTime);
        mTextViewVideoName = view.findViewById(R.id.videoName);
        mVoiceControl = view.findViewById(R.id.voiceControl);
        mEnterSeconds = view.findViewById(R.id.enterSeconds);
        mNumberOfSeconds = view.findViewById(R.id.numberOfSeconds);
        mTrim = view.findViewById(R.id.trim);
        mSpeedTrim = view.findViewById(R.id.speedTrim);

        mBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = mylink;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        helpers = new PreferenceHelper(getActivity());

        MobileAds.initialize(getActivity(), "ca-app-pub-6503532425142366~2924525320");
        AdView adView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        //adView.loadAd(adRequest);

        // Initialize SharedPreference
        tinydb = new TinyDB(getActivity());

        //new GetDataSync().execute();

        mTrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mToast != null)
                    mToast.cancel();

                if (mNumberOfSeconds.getText().toString().isEmpty()) {
                    mNumberOfSeconds.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                            R.anim.shake50));
                    mToast = Toast.makeText(getActivity(),
                            getString(R.string.specify_number_of_seconds), Toast.LENGTH_SHORT);
                    mToast.show();
                    return;
                }

                File file = new File(mSelectedVideoUri.toString());

                long file_size = Integer.parseInt(String.valueOf(file.length()));
                if (Utils.getInternalAvailableSpace() > file_size) {
                    InputMethodManager imm =
                            (InputMethodManager) getActivity()
                                    .getSystemService(Context.INPUT_METHOD_SERVICE);

                    imm.hideSoftInputFromWindow(mNumberOfSeconds.getWindowToken(), 0);
                } else {
                    Toast.makeText(getActivity(),
                            "You Don't have free space in your internal memory",
                            Toast.LENGTH_SHORT).show();
                    //dialog.dismiss();
                    return;
                }

                String storageDirectory = createStorageDirectory(mVideoName, PROCESS_TRIM,
                        mNumberOfSeconds.getText().toString());

                Bundle bundle = new Bundle();
                bundle.putString(STORAGE_DIRECTORY, storageDirectory);
                bundle.putString(VIDEO_NAME, mVideoName);
                bundle.putString(VIDEO_PATH, mVideoPath);
                bundle.putInt(SEGMENT_TIME,
                        Integer.parseInt(mNumberOfSeconds.getText().toString()));
                mTrimViewModel.setTrimBundle(bundle);
                loadFragment(getActivity().getSupportFragmentManager(),
                        TrimProcessFragment.newInstance(null, null), true);
            }
        });

        mSpeedTrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mToast != null)
                    mToast.cancel();

                if (mNumberOfSeconds.getText().toString().isEmpty()) {
                    mNumberOfSeconds.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                            R.anim.shake50));
                    mToast = Toast.makeText(getActivity(),
                            getString(R.string.specify_number_of_seconds), Toast.LENGTH_SHORT);
                    mToast.show();
                    return;
                }

                File file = new File(mSelectedVideoUri.toString());

                long file_size = Integer.parseInt(String.valueOf(file.length()));
                if (Utils.getInternalAvailableSpace() > file_size) {
                    InputMethodManager imm =
                            (InputMethodManager) getActivity()
                                    .getSystemService(Context.INPUT_METHOD_SERVICE);

                    imm.hideSoftInputFromWindow(mNumberOfSeconds.getWindowToken(), 0);
                } else {
                    Toast.makeText(getActivity(),
                            "You Don't have free space in your internal memory",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String storageDirectory = createStorageDirectory(mVideoName,
                        PROCESS_SPEED_TRIM, mNumberOfSeconds.getText().toString());

                Bundle bundle = new Bundle();
                bundle.putString(STORAGE_DIRECTORY, storageDirectory);
                bundle.putInt(SEGMENT_TIME,
                        Integer.parseInt(mNumberOfSeconds.getText().toString()));
                bundle.putString(VIDEO_NAME, mVideoName);
                bundle.putString(VIDEO_PATH, mVideoPath);
                mTrimViewModel.setTrimBundle(bundle);
                loadFragment(getActivity().getSupportFragmentManager(),
                        SpeedTrimProcessFragment.newInstance(null, null), false);
            }
        });

        /**
         * Pick up video file
         * */
        mOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    String[] mimeTypes = {"video/mp4", "video/x-ms-wmv", "video/x-matroska",
                            "video/3gpp", "video/3gpp2"};
                    // (video/*) : only get video files
                    intent.setType("video/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Video"),
                            REQUEST_TAKE_GALLERY_VIDEO);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
            if (resultCode == RESULT_OK) {
                mSelectedVideoUri = data.getData();
                mVideoPath = Utils.getRealPathFromURI_API19(getActivity(), mSelectedVideoUri);
                String[] pathSegments = mVideoPath.split("/");
                mVideoName = pathSegments[pathSegments.length - 1];
                showPickupVideo(false);
                displayVideo(true);
                prepareVideo(mSelectedVideoUri);
                mMainViewPagerSwipingViewModel.setMainViewPagerSwiping(false);
            }
        }
    }

    @Override
    public void onPause() {
        showPickupVideo(true);
        displayVideo(false);
        mHideVideoControlsHandler.removeCallbacks(mHideVideoControlsRunnable);
        mUpdateVideoTimeHandler.removeCallbacks(mUpdateVideoTimeRunnable);
        super.onPause();
    }

    public String createDirectory(String name, String seconds) {
        int num = 1;
        if (new File(Environment.getExternalStorageDirectory(),
                "FreeCut/FreeCut" + "-" + name + "-" + seconds + "-" + num).exists()) {
            num++;
        }

        File mediaStorageDir3 = new File(Environment.getExternalStorageDirectory(),
                "FreeCut/FreeCut" + "-" + name + "-" + seconds + "-" + num);


        if (!mediaStorageDir3.exists()) {
            if (!mediaStorageDir3.mkdirs()) {
                Log.d("App", "failed to create directory");
            }

        }
        return mediaStorageDir3.getAbsolutePath();

    }

    public class GetDataSync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                getData();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            String imgURL = "http://forcetouches.com/freecutAdmin/images/" + saldo;
            Glide.with(getActivity()).load(imgURL).into(mBanner);
        }

    }

    private void getData() throws IOException, JSONException {
        JSONObject json = readJsonFromUrl("http://www.forcetouches.com/freecutAdmin/getandroidbanners.php");
        try {
            String response = json.getString("banner");
            mylink = json.getString("url");
            saldo = response;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    private void trimErrorCode() {
        String[] errorCommand = {"-loglevel", "error", "-t", "30", "-i",
                mVideoPath, "-f", "null", "-"};
        FFmpeg.executeAsync(errorCommand, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
//                    String[] CutCommand = {"-i", mVideoPath, "-y", "-acodec", "copy",
//                            "-f", "segment", "-segment_time", seconds.getText().toString(),
//                            "-vcodec", "copy", "-reset_timestamps", "1", "-map", "0", filePath};
//                    FFmpeg.executeAsync(CutCommand, new ExecuteCallback() {
//                        @Override
//                        public void apply(long executionId, int returnCode) {
//                            tinydb.putString("seconds", seconds.getText().toString());
//                            tinydb.putString("cut", mypath);
//                            tinydb.putString("start", "0");
//                            tinydb.putString("extension", ".mp4");
//                            tinydb.putInt("begin", 1);
//                            tinydb.putInt("num1", 0);
//                            tinydb.putInt("num2", 0);
//                            tinydb.putString("fault", "true");
//                            tinydb.putString("end", "1");
//                            tinydb.putString("cuts", "1");
//                            tinydb.putString("Main", "0");
//                            Log.d(TAG, "Start CutFragmentOne");
//                            loadFragment(getActivity().getSupportFragmentManager(),
//                                    new CutFragmentOne(),
//                                    false);
//                        }
//                    });


                } else {
                    filePrefix = "cut" + mNumberOfSeconds.getText().toString() + fileno + "%03d";
                    fileExtn_mp4 = ".mp4";
                    dest = new File(mypath, filePrefix + fileExtn_mp4);
                    filePath = dest.getAbsolutePath();
                    final File encodeFile = new File(mypath,
                            new File(mVideoPath).getName() + fileExtn_mp4);
                    final String[] encode = {"-i", mVideoPath, "-c:v", "libx264",
                            "-preset", "ultrafast", encodeFile.getAbsolutePath()};
                    FFmpeg.executeAsync(encode, new ExecuteCallback() {
                        @Override
                        public void apply(long executionId, int returnCode) {
                            final String[] CutCommand = {"-i", encodeFile.getAbsolutePath(),
                                    "-y", "-acodec", "copy", "-f", "segment", "-segment_time",
                                    mNumberOfSeconds.getText().toString(), "-vcodec", "copy",
                                    "-reset_timestamps", "1", "-map", "0", filePath};

                            FFmpeg.executeAsync(CutCommand, new ExecuteCallback() {
                                @Override
                                public void apply(long executionId, int returnCode) {
                                    encodeFile.delete();

                                    tinydb.putString("seconds", mNumberOfSeconds.getText().toString());
                                    tinydb.putString("cut", mypath);
                                    tinydb.putString("start", "0");
                                    tinydb.putString("extension", ".mp4");
                                    tinydb.putInt("begin", 1);
                                    tinydb.putInt("num1", 0);
                                    tinydb.putInt("num2", 0);
                                    tinydb.putString("fault", "true");
                                    tinydb.putString("end", "1");
                                    tinydb.putString("cuts", "1");
                                    tinydb.putString("Main", "0");
                                    loadFragment(getActivity().getSupportFragmentManager(),
                                            new CutFragmentOne(),
                                            false);
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private String createStorageDirectory(String name, String process, String seconds) {
        File file = new File(Environment.getExternalStorageDirectory(),
                "FreeCut/" + name + "/" + process + "/" + "secs-" + seconds + "/");

        if (file.exists()) {
            for (File video : file.listFiles()) {
                video.delete();
            }
        }

        file.mkdirs();

        return file.getAbsolutePath();
    }

    private void showPickupVideo(boolean visible){
        if (visible){
            mIcVideo.setVisibility(View.VISIBLE);
            mOpenGallery.setVisibility(View.VISIBLE);
            mPickUpTrim.setVisibility(View.VISIBLE);
            mMainViewPagerSwipingViewModel.setMainViewPagerSwiping(true);
        } else {
            mIcVideo.setVisibility(View.INVISIBLE);
            mOpenGallery.setVisibility(View.INVISIBLE);
            mPickUpTrim.setVisibility(View.INVISIBLE);
        }
    }

    private void displayVideo(boolean visible) {
        if (visible) {
            mVideoView.setVisibility(View.VISIBLE);
            mVideoViewBackground.setVisibility(View.VISIBLE);
            mVideoSeekBar.setVisibility(View.VISIBLE);
            mViewShadow.setVisibility(View.VISIBLE);
            mIcShare.setVisibility(View.VISIBLE);
            mIcRemove.setVisibility(View.VISIBLE);
            mIcVideoControl.setVisibility(View.VISIBLE);
            mVoiceControl.setVisibility(View.VISIBLE);
            mVideoTime.setVisibility(View.VISIBLE);
            mTextViewVideoName.setVisibility(View.VISIBLE);
            mEnterSeconds.setVisibility(View.VISIBLE);
            mNumberOfSeconds.setVisibility(View.VISIBLE);
            mTrim.setVisibility(View.VISIBLE);
            mSpeedTrim.setVisibility(View.VISIBLE);
        } else {
            mVideoView.setVisibility(View.INVISIBLE);
            mVideoViewBackground.setVisibility(View.INVISIBLE);
            mVideoSeekBar.setVisibility(View.INVISIBLE);
            mViewShadow.setVisibility(View.INVISIBLE);
            mIcShare.setVisibility(View.INVISIBLE);
            mIcRemove.setVisibility(View.INVISIBLE);
            mIcVideoControl.setVisibility(View.INVISIBLE);
            mVoiceControl.setVisibility(View.INVISIBLE);
            mVideoTime.setVisibility(View.INVISIBLE);
            mTextViewVideoName.setVisibility(View.INVISIBLE);
            mEnterSeconds.setVisibility(View.INVISIBLE);
            mNumberOfSeconds.setVisibility(View.INVISIBLE);
            mTrim.setVisibility(View.INVISIBLE);
            mSpeedTrim.setVisibility(View.INVISIBLE);
        }
    }

    private void prepareVideo(Uri videoUri) {
        mVideoView.setVideoURI(videoUri);
        mVideoView.start();
        mVideoView.setVisibility(View.VISIBLE);
        mVideoView.requestFocus();
        mTextViewVideoName.setText(mVideoName);

        mIcShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("video/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
                getActivity().startActivity(Intent.createChooser(shareIntent, ""));
            }
        });

        mIcRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPickupVideo(true);
                displayVideo(false);
            }
        });

        mIcVideoControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIcVideoControl.getAlpha() == 0) {
                    showVideoControls(true);
                    mHideVideoControlsHandler.postDelayed(mHideVideoControlsRunnable,
                            3000);
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
                    mViewShadow.setAlpha(0);
                    mIcShare.setAlpha((float) 0);
                    mIcRemove.setAlpha((float) 0);
                    mIcVideoControl.setAlpha((float) 0);
                    mVideoTime.setAlpha(0);
                    mVoiceControl.setClickable(false);
                    mVoiceControl.setAlpha((float) 0);
                    mVideoControlsVisible = false;
                }
            }
        });

        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVideoControlsVisible) {
                    showVideoControls(false);
                } else {
                    showVideoControls(true);
                    mHideVideoControlsHandler.postDelayed(mHideVideoControlsRunnable,
                            3000);
                }
            }
        });

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer = mp;
                mVideoSeekBar.setProgress(0);
                mVideoSeekBar.setMax(mVideoView.getDuration());

                mVideoTime.setText(String.format(Locale.ENGLISH, "%s / %s",
                        getVideoTime(0),
                        getVideoTime(mVideoView.getDuration() / 1000)));
                mUpdateVideoTimeHandler.postDelayed(mUpdateVideoTimeRunnable, 100);
            }
        });

        mVideoView.setOnCompletionListener(mp -> {
            mBlockSeekBar = false;
            mVideoSeekBar.animate().alpha(1);
            mViewShadow.animate().alpha(1);
            mIcVideoControl.animate().alpha(1);
            mVoiceControl.setClickable(true);
            mVoiceControl.animate().alpha(1);
            mVideoTime.animate().alpha(1);
            mVideoControlsVisible = true;
            mIcVideoControl.setImageResource(R.drawable.ic_play);
        });

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
                mHideVideoControlsHandler.removeCallbacks(mHideVideoControlsRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHideVideoControlsHandler.postDelayed(mHideVideoControlsRunnable, 3000);
            }
        });

        mVideoSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mBlockSeekBar;
            }
        });

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

    private String getVideoTime(int seconds) {
        long second = seconds % 60;
        long minute = (seconds / 60) % 60;
        long hour = (seconds / (60 * 60)) % 24;

        if (hour > 0)
            return String.format(Locale.ENGLISH, "%d:%d:%02d", hour, minute, second);
        else
            return String.format(Locale.ENGLISH, "%d:%02d", minute, second);
    }

    private void showVideoControls(boolean show) {
        if (show) {
            mBlockSeekBar = false;
            mVideoSeekBar.animate().alpha(1);
            mViewShadow.animate().alpha(1);
            mIcShare.animate().alpha(1);
            mIcRemove.animate().alpha(1);
            mIcVideoControl.animate().alpha(1);
            mVoiceControl.setClickable(true);
            mVoiceControl.animate().alpha(1);
            mVideoTime.animate().alpha(1);
            mVideoControlsVisible = true;
        } else {
            mVideoSeekBar.animate().alpha(0);
            mBlockSeekBar = true;
            mIcVideoControl.animate().alpha(0);
            mViewShadow.animate().alpha(0);
            mIcShare.animate().alpha(0);
            mIcRemove.animate().alpha(0);
            mVideoTime.animate().alpha(0);
            mVoiceControl.setClickable(false);
            mVoiceControl.animate().alpha(0);
            mVideoControlsVisible = false;
        }
    }
}