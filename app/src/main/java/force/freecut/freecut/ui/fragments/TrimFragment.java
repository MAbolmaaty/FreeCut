package force.freecut.freecut.ui.fragments;

import static android.app.Activity.RESULT_OK;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static force.freecut.freecut.ui.activities.MainActivity.loadFragment;
import static force.freecut.freecut.utils.Constants.FILE_PATH;
import static force.freecut.freecut.utils.Constants.STORAGE_DIRECTORY;
import static force.freecut.freecut.utils.Constants.TRIM_NUMBER_OF_SECONDS;
import static force.freecut.freecut.utils.Constants.TRIM_VIDEO_PATH;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

import force.freecut.freecut.Data.TinyDB;
import force.freecut.freecut.Data.Utils;
import force.freecut.freecut.R;
import force.freecut.freecut.helper.PreferenceHelper;
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
    Button textCut;
    Uri mSelectedVideoUri;
    EditText seconds;
    String mypath;
    // TinyDB : Implementation for Shared Preferences
    TinyDB tinydb;
    File dest;
    String filePrefix;
    String mylink = "https://www.google.com/";
    ImageView placeholder;
    ImageView mBanner;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    private ScrollView mRootView;
    String saldo = "";

    private PreferenceHelper helpers;

    private TrimViewModel mTrimViewModel;
    private ImageView mIconNoVideo;
    private TextView mTextNoVideo;
    private Toast mToast;

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
        mTrimViewModel = ViewModelProviders.of(getActivity()).get(TrimViewModel.class);

        mBanner = view.findViewById(R.id.banner);
        mIconNoVideo = view.findViewById(R.id.imageView_noVideo);
        mTextNoVideo = view.findViewById(R.id.textView_noVideo);

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
        adView.loadAd(adRequest);

        mRootView = view.findViewById(R.id.rootView);

        // Initialize SharedPreference
        tinydb = new TinyDB(getActivity());

        placeholder = view.findViewById(R.id.view_videoView);

        textCut = view.findViewById(R.id.trim);

        seconds = view.findViewById(R.id.numberOfSeconds);

        new GetDataSync().execute();

        textCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(TAG, "Trim Button Clicked");
//                int numOfSeconds = Integer.parseInt(seconds.getText().toString());
//                int hours = numOfSeconds / 3600;
//                int minutes = (numOfSeconds % 3600) / 60;
//                int secondsIn = numOfSeconds % 60;
//                String s = String.format("%02d:%02d:%02d", hours, minutes, secondsIn);
//                Log.d(TAG, s);
                if (mToast != null)
                    mToast.cancel();

                if (mSelectedVideoUri == null) {
                    placeholder.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.shake50));
                    mIconNoVideo.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.shake50));
                    mTextNoVideo.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.shake50));
                    return;
                }

                if (seconds.getText().toString().isEmpty()) {
                    seconds.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.shake50));
                    mToast = Toast.makeText(getActivity(),
                            getString(R.string.specify_number_of_seconds), Toast.LENGTH_SHORT);
                    mToast.show();
                    return;
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(inflater.inflate(R.layout.dialog_trim_type, null));
                final AlertDialog dialog = builder.create();
                dialog.show();

                File file = new File(mSelectedVideoUri.toString());

                long file_size = Integer.parseInt(String.valueOf(file.length()));
                if (Utils.getInternalAvailableSpace() > file_size) {
                    InputMethodManager imm =
                            (InputMethodManager) getActivity()
                                    .getSystemService(Context.INPUT_METHOD_SERVICE);

                    imm.hideSoftInputFromWindow(seconds.getWindowToken(), 0);
                } else {
                    Toast.makeText(getActivity(),
                            "You Don't have free space in your internal memory",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                Button speedTrim = dialog.findViewById(R.id.speedTrim);
                Button trim = dialog.findViewById(R.id.trim);

                speedTrim.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                            // mVideoPath : /storage/emulated/0/video.mp4
                            // fileExtn_mp4 : .mp4
                        //fileExtn_mp4 = mVideoPath.substring(mVideoPath.lastIndexOf("."));
                            // mVideoName : video.mp4
                        //mypath = createDirectory(mVideoName, seconds.getText().toString());
                            // mypath : /storage/emulated/0/FreeCut/FreeCut-video.mp4-seconds-1
                        //filePrefix = "cut" + seconds.getText().toString() + fileno + "%02d";
                            // filePrefix : cut[seconds]1%02d
                        //dest = new File(mypath, filePrefix + fileExtn_mp4);
                        //filePath = dest.getAbsolutePath();
                            // filePath directory : /storage/emulated/0/FreeCut/
                            // FreeCut-videoplayback.mp4-100-2/cut1001%02d.mp4
                        // new storage directory
                        String directory = createStorageDirectory(mVideoName);
                        String file =
                                new File(directory,"speed-trim" + "-" + seconds.getText().toString()
                                        + "-" + "%02d" + ".mp4").getAbsolutePath();
                        Bundle bundle = new Bundle();
                        bundle.putString(TRIM_VIDEO_PATH, mVideoPath);
                        bundle.putString(TRIM_NUMBER_OF_SECONDS, seconds.getText().toString());
                        bundle.putString(STORAGE_DIRECTORY, directory);
                        bundle.putString(FILE_PATH, file);
                        mTrimViewModel.setTrimBundle(bundle);
                        //tinydb.putString("cut", mypath);
                        loadFragment(getActivity().getSupportFragmentManager(),
                                TrimProcessFragment.newInstance(null, null), false);
                    }
                });

                trim.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "Trim Button Clicked");
                        dialog.dismiss();
                        // mVideoPath : /storage/emulated/0/videoplayback.mp4
                        fileExtn_mp4 = mVideoPath.substring(mVideoPath.lastIndexOf("."));

                        //String : /storage/emulated/0/FreeCut/FreeCut-videoplayback.mp4-66-1
                        mypath = createDirectory(new File(mVideoPath).getName(),
                                seconds.getText().toString());

                        //String : cut1001%02d
                        filePrefix = "cut" + seconds.getText().toString() + fileno + "%02d";

                        dest = new File(mypath, filePrefix + fileExtn_mp4);

                        //String : /storage/emulated/0/FreeCut/FreeCut-videoplayback.mp4-100-2/cut1001%02d.mp4
                        filePath = dest.getAbsolutePath();

                        trimErrorCode();

                    }
                });
            }
        });

        /**
         * Pick up video file
         * */
        placeholder.setOnClickListener(new View.OnClickListener() {
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
    public void onResume() {
        super.onResume();
        //mToolbarViewModel.setToolbarTitle(getString(R.string.trim));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
            if (resultCode == RESULT_OK) {
                // data.getData : uri : Ex : content://media/external/video/media/100044
                mSelectedVideoUri = data.getData();
                // Video Path : String : Ex : /storage/emulated/0/video.mp4
                mVideoPath = Utils.getRealPathFromURI_API19(getActivity(), mSelectedVideoUri);
                String[] pathSegments = mVideoPath.split("/");
                // Video Name : String : Ex : video.mp4
                mVideoName = pathSegments[pathSegments.length - 1];
                placeholder.setBackgroundResource(0);
                mIconNoVideo.setVisibility(View.GONE);
                mTextNoVideo.setVisibility(View.GONE);
                Glide.with(getActivity()).load(mSelectedVideoUri).fitCenter().into(placeholder);
            }
        }
    }

    /**
     * */
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
                    Log.d("Em-FFMPEG", "TrimFragment : Trim Error Command Success");
                    String[] CutCommand = {"-i", mVideoPath, "-y", "-acodec", "copy",
                            "-f", "segment", "-segment_time", seconds.getText().toString(),
                            "-vcodec", "copy", "-reset_timestamps", "1", "-map", "0", filePath};
                    FFmpeg.executeAsync(CutCommand, new ExecuteCallback() {
                        @Override
                        public void apply(long executionId, int returnCode) {
                            tinydb.putString("seconds", seconds.getText().toString());
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
                } else {
                    Log.d("Em-FFMPEG", "TrimFragment : Trim Error Command failed " + returnCode);
                    filePrefix = "cut" + seconds.getText().toString() + fileno + "%03d";
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
                                    seconds.getText().toString(), "-vcodec", "copy",
                                    "-reset_timestamps", "1", "-map", "0", filePath};

                            FFmpeg.executeAsync(CutCommand, new ExecuteCallback() {
                                @Override
                                public void apply(long executionId, int returnCode) {
                                    encodeFile.delete();

                                    tinydb.putString("seconds", seconds.getText().toString());
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

    private String createStorageDirectory(String name){
        File file = new File(Environment.getExternalStorageDirectory(),
                "FreeCut/" + name + "/");
        if (file.mkdirs()){
            Log.d(TAG, "File created");
        } else {
            Log.d(TAG, "File not created");
        }
        return file.getAbsolutePath();
    }
}