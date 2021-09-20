package force.freecut.freecut.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import force.freecut.freecut.Data.TinyDB;
import force.freecut.freecut.Data.Utils;
import force.freecut.freecut.Data.VideoItem;
import force.freecut.freecut.R;
import force.freecut.freecut.view_models.VideosViewModel;

import static force.freecut.freecut.ui.activities.MainActivity.loadFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SuccessFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SuccessFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = SuccessFragment.class.getSimpleName();

    private String mParam1;
    private String mParam2;

    Button home;
    TextView textMessage;
    private AdView mAdView1;
    ArrayList<VideoItem> mylist;
    TinyDB tinyDB;
    String mylink = "https://www.google.com/";

    private VideosViewModel mVideosViewModel;
    private View mViewProgress;
    private ProgressBar mProgressBar1;
    private ProgressBar mProgressBar2;
    private TextView mFilesStatus;
    private Button mShowVideos;
    ImageView mBanner;
    String saldo = "";


    public SuccessFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SuccessFragment.
     */
    public static SuccessFragment newInstance(String param1, String param2) {
        SuccessFragment fragment = new SuccessFragment();
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

    @SuppressLint("StaticFieldLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_success, container, false);
        mShowVideos = view.findViewById(R.id.showVideos);
        home = view.findViewById(R.id.home);
        textMessage = view.findViewById(R.id.text_message);
        mViewProgress = view.findViewById(R.id.view_progress);
        mProgressBar1 = view.findViewById(R.id.progress1);
        mProgressBar2 = view.findViewById(R.id.progress2);
        mFilesStatus = view.findViewById(R.id.filesStatus);
        mBanner = view.findViewById(R.id.banner);
        mBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = mylink;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        new GetDataSync().execute();

        mylist = new ArrayList<>();
        mVideosViewModel = ViewModelProviders.of(getActivity()).get(VideosViewModel.class);
        tinyDB = new TinyDB(getActivity());

        if (tinyDB.getString("cut").contains(".")) {
            textMessage.setText(getString(R.string.saved1));

            new AsyncTask<Void, Void, String>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    mShowVideos.setEnabled(false);
                    mViewProgress.setVisibility(View.VISIBLE);
                    mProgressBar1.setVisibility(View.VISIBLE);
                    mProgressBar2.setVisibility(View.VISIBLE);
                    mFilesStatus.setText(getString(R.string.waiting));
                }

                @Override
                protected String doInBackground(Void... voids) {
                    File directory = new File(tinyDB.getString("cut"));
                    File[] files = directory.listFiles();
                    if (files != null) {
                        Arrays.sort(files, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                        for (int i = 0; i < files.length; i++) {
                            Log.d(TAG, files[i].getName());
                            File f =
                                    new File(tinyDB.getString("cut") + "/" + files[i].getName());
                            File to = new File(Environment.getExternalStorageDirectory(),
                                    "FreeCut/" + f.getName());
                            int var = 0;
                            while (to.exists()) {
                                var++;
                                to = new File(Environment.getExternalStorageDirectory(),
                                        "FreeCut/" + var + f.getName());
                            }

                            f.renameTo(to);
                            f = to;

                            new SingleMediaScanner(getActivity(), f);

                            VideoItem videoItem = new VideoItem();
                            videoItem.setImageLink(f.getAbsolutePath());
                            videoItem.setItem_name(f.getName());
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(f.getAbsolutePath());
                            String time =
                                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            long timeInmillisec = Long.parseLong(time);
                            videoItem.setTime(Utils.milliSecondsToTimer(timeInmillisec));
                            mylist.add(videoItem);
                        }
                    }
                    directory.delete();
                    return null;
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    mViewProgress.setVisibility(View.GONE);
                    mProgressBar1.setVisibility(View.GONE);
                    mProgressBar2.setVisibility(View.GONE);
                    mFilesStatus.setText(getString(R.string.files_ready));
                    mShowVideos.setEnabled(true);
                }

            }.execute();
        } else {
            File f = new File(tinyDB.getString("mer"));
            VideoItem videoItem = new VideoItem();
            videoItem.setImageLink(f.getAbsolutePath());
            videoItem.setItem_name(f.getName());
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(f.getAbsolutePath());
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInmillisec = Long.parseLong(time);
            videoItem.setTime(Utils.milliSecondsToTimer(timeInmillisec));
            mylist.add(videoItem);

            textMessage.setText(getString(R.string.saved2));

        }

        MobileAds.initialize(getActivity(), "ca-app-pub-6503532425142366~2924525320");

        mAdView1 = view.findViewById(R.id.adView2);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView1.loadAd(adRequest);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(getActivity().getSupportFragmentManager(),
                        MainFragment.newInstance(null, null), false);
            }
        });

        mShowVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideosViewModel.setVideos(mylist);
                loadFragment(getActivity().getSupportFragmentManager(),
                        VideosFragment.newInstance(null, null), false);
            }
        });

        File mediaStorageDir =
                new File(Environment.getExternalStorageDirectory(), "FreeCut Test");

        String pathtest = mediaStorageDir.getAbsolutePath();
        File dir = new File(pathtest);
        if (dir.isDirectory() && dir.exists()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
            dir.delete();
        }

        return view;
    }

    public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

        private MediaScannerConnection mMs;
        private File mFile;

        public SingleMediaScanner(Context context, File f) {
            mFile = f;
            mMs = new MediaScannerConnection(context, this);
            mMs.connect();
        }

        @Override
        public void onMediaScannerConnected() {
            mMs.scanFile(mFile.getAbsolutePath(), null);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            mMs.disconnect();
        }

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
        JSONObject json =
                readJsonFromUrl("http://www.forcetouches.com/freecutAdmin/getandroidbanners.php");
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
}