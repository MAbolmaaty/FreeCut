package force.freecut.freecut.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import force.freecut.freecut.Data.VideoItem;
import force.freecut.freecut.R;
import force.freecut.freecut.adapters.VideosAdapter;
import force.freecut.freecut.utils.interfaces.VideoItemClickHandler;
import force.freecut.freecut.view_models.ToolbarViewModel;
import force.freecut.freecut.view_models.VideosViewModel;

import static force.freecut.freecut.ui.activities.MainActivity.loadFragment;
import static force.freecut.freecut.utils.Constants.VIDEOS_TRIM;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideosFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private VideosAdapter mVideosAdapter;
    private RecyclerView mRecyclerViewVideos;
    private ToolbarViewModel mViewModelToolbar;

    String mylink = "";
    private AdView mAdView1;
    ImageView mBanner;
    ImageView imageBack;
    String saldo = "";

    public VideosFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VideosFragment.
     */
    public static VideosFragment newInstance(String param1, String param2) {
        VideosFragment fragment = new VideosFragment();
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
        View view = inflater.inflate(R.layout.fragment_videos, container, false);
        final VideosViewModel videosViewModel = ViewModelProviders.of(getActivity()).get(VideosViewModel.class);
        mViewModelToolbar = ViewModelProviders.of(getActivity()).get(ToolbarViewModel.class);
        mViewModelToolbar.showBackHomeButton(true);
        mViewModelToolbar.showBackButton(false);
        mRecyclerViewVideos = view.findViewById(R.id.recyclerView);
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

        MobileAds.initialize(getActivity(), "ca-app-pub-6503532425142366~2924525320");
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView1.loadAd(adRequest);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        mRecyclerViewVideos.setLayoutManager(layoutManager);
        mRecyclerViewVideos.setHasFixedSize(true);
        videosViewModel.getVideos().observe(this, new Observer<ArrayList<VideoItem>>() {
            @Override
            public void onChanged(final ArrayList<VideoItem> videoItems) {
                mViewModelToolbar.setToolbarTitle(getString(R.string.videos) + " ( " + videoItems.size() +" )");
                mVideosAdapter = new VideosAdapter(getActivity(), videoItems, VIDEOS_TRIM, new VideoItemClickHandler() {
                    @Override
                    public void onClick(int position) {
                        videosViewModel.setVideoLink(videoItems.get(position).getImageLink());
                        videosViewModel.setVideoNumber(String.valueOf(position + 1));
                        loadFragment(getActivity().getSupportFragmentManager(),
                                VideoShowFragment.newInstance(null, null),
                                true);
                    }
                }, null);
                mRecyclerViewVideos.setAdapter(mVideosAdapter);
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