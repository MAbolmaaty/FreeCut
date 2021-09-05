package force.freecut.freecut.ui.fragments;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

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

import bg.devlabs.fullscreenvideoview.FullscreenVideoView;
import force.freecut.freecut.R;
import force.freecut.freecut.view_models.ToolbarViewModel;
import force.freecut.freecut.view_models.VideosViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoShowFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoShowFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    String filepath = "";

    TextView mShare;
    String mylink = "";
    private AdView mAdView1;
    ImageView mBanner;
    String saldo = "";
    private VideosViewModel mVideosViewModel;
    private ToolbarViewModel mViewModelToolbar;

    public VideoShowFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VideoShowFragmentFragment.
     */
    public static VideoShowFragment newInstance(String param1, String param2) {
        VideoShowFragment fragment = new VideoShowFragment();
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
        View view = inflater.inflate(R.layout.fragment_video_show, container, false);
        mVideosViewModel = ViewModelProviders.of(getActivity()).get(VideosViewModel.class);
        mViewModelToolbar = ViewModelProviders.of(getActivity()).get(ToolbarViewModel.class);
        mViewModelToolbar.showBackButton(true);
        mViewModelToolbar.showBackHomeButton(false);

        mShare = view.findViewById(R.id.share);
        mAdView1 = view.findViewById(R.id.adView22);
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

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView1.loadAd(adRequest);

        final FullscreenVideoView fullscreenVideoView = view.findViewById(R.id.fullscreenVideoView);
        mVideosViewModel.getVideoNumber().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String videoNumber) {
                mViewModelToolbar.setToolbarTitle(getString(R.string.video) + " " + videoNumber + getString(R.string.preview));
                mVideosViewModel.getVideoLink().observe(VideoShowFragment.this, new Observer<String>() {
                    @Override
                    public void onChanged(String videoLink) {
                        filepath = videoLink;
                        File videoFile = new File(filepath);
                        fullscreenVideoView.videoFile(videoFile).enableAutoStart();

                        mShare.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                shareVideo(new File(filepath).getName(), filepath);
                            }
                        });
                    }
                });
            }
        });

        return view;
    }

    public void shareVideo(final String title, String path) {
        MediaScannerConnection.scanFile(getActivity(), new String[]{path},
                null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                        shareIntent.setType("video/*");
                        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
                        shareIntent.putExtra(android.content.Intent.EXTRA_TITLE, title);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        getActivity().startActivity(Intent.createChooser(shareIntent, "share Video"));

                    }
                });
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
}