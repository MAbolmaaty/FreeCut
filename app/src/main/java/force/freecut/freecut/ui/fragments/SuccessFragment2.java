package force.freecut.freecut.ui.fragments;

import static force.freecut.freecut.ui.activities.MainActivity.loadFragment;
import static force.freecut.freecut.utils.Constants.STORAGE_DIRECTORY;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import force.freecut.freecut.Data.Utils;
import force.freecut.freecut.Data.VideoItem;
import force.freecut.freecut.R;
import force.freecut.freecut.view_models.TrimViewModel;
import force.freecut.freecut.view_models.VideosViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SuccessFragment2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SuccessFragment2 extends Fragment {

    private static final String TAG = SuccessFragment2.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button mShowVideos;
    private Button home;
    private TextView mTitle;
    private View mViewProgress;
    private ProgressBar mProgressBar1;
    private ProgressBar mProgressBar2;
    private TextView mFilesStatus;
    private ImageView mBanner;
    private AdView mAdView1;

    private String mylink = "https://www.google.com/";
    private String saldo = "";
    private ArrayList<VideoItem> mylist;

    private VideosViewModel mVideosViewModel;

    public SuccessFragment2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SuccessFragment2.
     */
    // TODO: Rename and change types and number of parameters
    public static SuccessFragment2 newInstance(String param1, String param2) {
        SuccessFragment2 fragment = new SuccessFragment2();
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
        View view = inflater.inflate(R.layout.fragment_success2, container, false);
        mShowVideos = view.findViewById(R.id.showVideos);
        home = view.findViewById(R.id.home);
        mTitle = view.findViewById(R.id.text_message);
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

        mTitle.setText(getString(R.string.saved1));

        mVideosViewModel = ViewModelProviders.of(getActivity()).get(VideosViewModel.class);
        TrimViewModel trimViewModel = ViewModelProviders.of(getActivity()).get(TrimViewModel.class);
        trimViewModel.getTrimBundle().observe(this, new Observer<Bundle>() {
            @Override
            public void onChanged(Bundle bundle) {
                File storageDirectory = new File(bundle.getString(STORAGE_DIRECTORY));
                File[] videos = storageDirectory.listFiles();

                for (int i = 0 ; i < videos.length ; i++){
                    VideoItem videoItem = new VideoItem();
                    videoItem.setImageLink(videos[i].getAbsolutePath());
                    videoItem.setItem_name(videos[i].getName());
                    new SingleMediaScanner(getActivity(), videos[i]);
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(videos[i].getAbsolutePath());
                    String time =
                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    long timeInmillisec = Long.parseLong(time);
                    videoItem.setTime(Utils.milliSecondsToTimer(timeInmillisec));
                    mylist.add(videoItem);
                }
            }
        });

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

        return view;
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

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}