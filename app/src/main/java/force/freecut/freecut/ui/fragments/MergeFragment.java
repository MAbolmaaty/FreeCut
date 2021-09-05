package force.freecut.freecut.ui.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.bumptech.glide.Glide;
import com.erikagtierrez.multiple_media_picker.Gallery;
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
import java.util.List;

import force.freecut.freecut.Data.TinyDB;
import force.freecut.freecut.Data.Utils;
import force.freecut.freecut.Data.VideoItem;
import force.freecut.freecut.R;
import force.freecut.freecut.adapters.VideosAdapter;
import force.freecut.freecut.utils.interfaces.VideoItemDeleteHandler;
import force.freecut.freecut.view_models.MergeViewModel;
import force.freecut.freecut.view_models.ToolbarViewModel;

import static android.app.Activity.RESULT_OK;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static force.freecut.freecut.ui.activities.MainActivity.loadFragment;
import static force.freecut.freecut.utils.Constants.VIDEOS_MERGE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MergeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MergeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RecyclerView mRecyclerView;
    private VideosAdapter mVideosAdapter;
    List<VideoItem> mListVideos;
    String mergeFilePath;
    String All = "";
    private ProgressDialog mProgressDialog;
    static final int OPEN_MEDIA_PICKER = 1;
    String[] extensions;
    String[] links;
    int ind;
    long totalbytes = 0;
    TinyDB tinydb;
    VideoItem mVideoItem;
    View mMerge;
    View mImport;
    TextView mNoVideos;
    String mylink = "https://www.google.com/";
    private MergeViewModel mMergeViewModel;
    ImageView mBanner;
    String saldo = "";

    private ToolbarViewModel mToolbarViewModel;

    public MergeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MergeFragment.
     */
    public static MergeFragment newInstance(String param1, String param2) {
        MergeFragment fragment = new MergeFragment();
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
        View view = inflater.inflate(R.layout.fragment_merge, container, false);
        mBanner = view.findViewById(R.id.banner);
        mMergeViewModel = ViewModelProviders.of(getActivity()).get(MergeViewModel.class);
        mToolbarViewModel = ViewModelProviders.of(getActivity()).get(ToolbarViewModel.class);

        MobileAds.initialize(getActivity(), "ca-app-pub-6503532425142366~2924525320");

        AdView adView1 = view.findViewById(R.id.adView22);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView1.loadAd(adRequest);

        mRecyclerView = view.findViewById(R.id.recyclerView);
        mMerge = view.findViewById(R.id.view_merge);
        mImport = view.findViewById(R.id.view_import);
        mNoVideos = view.findViewById(R.id.noVideos);
        tinydb = new TinyDB(getActivity());
        mergeFilePath = new File(Environment.getExternalStorageDirectory(), "FreeCut").getAbsolutePath();
        mListVideos = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

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

        mToolbarViewModel.listenForDeleteAllMergeVideos().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean deleteAll) {
                if (deleteAll && mListVideos.size() > 0){
                    mListVideos.clear();
                    mVideosAdapter.notifyDataSetChanged();
                    mNoVideos.setVisibility(View.VISIBLE);
                }
            }
        });

        mMerge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalbytes = 0;
                for (int i = 0; i < mListVideos.size(); i++) {
                    File file = new File(mListVideos.get(i).getImageLink());
                    totalbytes += Integer.parseInt(String.valueOf(file.length()));
                }
                if (mListVideos.isEmpty() || mListVideos.size() == 1) {
                    Toast.makeText(getActivity(), getString(R.string.select_videos_for_merge), Toast.LENGTH_SHORT).show();
                    mNoVideos.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.shake50));
                } else if (Utils.getInternalAvailableSpace() < totalbytes) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(true);
                    builder.setTitle("Free Cut");
                    builder.setMessage("You Don't have free space in your internal memory");
                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    extensions = new String[mListVideos.size()];
                    links = new String[mListVideos.size()];
                    for (int i = 0; i < mListVideos.size(); i++) {
                        extensions[i] = mListVideos.get(i).getImageLink().substring(mListVideos.get(i).getImageLink().lastIndexOf("."));
                        links[i] = mListVideos.get(i).getImageLink();
                    }
                    Bundle mergeBundle = new Bundle();
                    mergeBundle.putStringArray("link", links);
                    mergeBundle.putString("merge", mergeFilePath);
                    mergeBundle.putString("state", "0");
                    mMergeViewModel.setMergeBundle(mergeBundle);
                    loadFragment(getActivity().getSupportFragmentManager(),
                            MergeProcessFragment.newInstance(null, null),
                            false);
                }
            }
        });

        mImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Gallery.class);
                intent.putExtra("title", "Select media");
                // Mode 1 for both images and videos selection, 2 for images only and 3 for videos!
                intent.putExtra("mode", 3);
                intent.putExtra("maxSelection", 100);
                // Optional
                startActivityForResult(intent, OPEN_MEDIA_PICKER);
            }
        });

        mNoVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Gallery.class);
                intent.putExtra("title", "Select media");
                // Mode 1 for both images and videos selection, 2 for images only and 3 for videos!
                intent.putExtra("mode", 3);
                intent.putExtra("maxSelection", 100);
                // Optional
                startActivityForResult(intent, OPEN_MEDIA_PICKER);
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_MEDIA_PICKER) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK && data != null) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setTitle(getString(R.string.preparing));

                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage(getString(R.string.waiting));
                //mProgressDialog.show();
                final ArrayList<String> selectionResult = data.getStringArrayListExtra("result");
                ind = 0;
                for (final String item : selectionResult) {

                    /*final String[] command = {"-i", item};
                    FFmpeg.executeAsync(command, new ExecuteCallback() {
                        @Override
                        public void apply(long executionId, int returnCode) {
                            if (returnCode == RETURN_CODE_SUCCESS) {
                                ind = ind + 1;*/
                                /*if (All.contains("Audio:") && All.contains("Video:") && !All.contains("Invalid data found when processing input")) {
                                    */mVideoItem = new VideoItem();
                                    mVideoItem.setItem_name(new File(item).getName());
                                    mVideoItem.setImageLink(item);
                                    mListVideos.add(mVideoItem);
                                    All = "";
                                /*} else {
                                    All = "";
                                }*/

                                    mVideosAdapter = new VideosAdapter(getActivity(), mListVideos,
                                            VIDEOS_MERGE, null, new VideoItemDeleteHandler() {
                                        @Override
                                        public void onClick(int position) {
                                            mListVideos.remove(position);
                                            if (mListVideos.size() < 1) {
                                                mNoVideos.setVisibility(View.VISIBLE);
                                            }
                                            mVideosAdapter.notifyDataSetChanged();
                                        }
                                    });
                                    mRecyclerView.setAdapter(mVideosAdapter);
                                    mProgressDialog.dismiss();
                                    mNoVideos.setVisibility(View.GONE);
                                    mImport.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.grey));
                                    mMerge.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.orange));

                           /* }
                        }
                    });*/
                }
            }
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