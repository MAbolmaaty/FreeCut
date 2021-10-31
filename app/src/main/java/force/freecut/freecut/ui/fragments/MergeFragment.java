package force.freecut.freecut.ui.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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
import java.lang.reflect.Array;
import java.lang.reflect.Method;
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
import force.freecut.freecut.view_models.MainViewPagerSwipingViewModel;
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

    private static final String TAG = MergeFragment.class.getSimpleName();

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
    String mylink = "https://www.google.com/";
    private MergeViewModel mMergeViewModel;
    ImageView mBanner;
    String saldo = "";

    private Button mOpenGallery;
    private ToolbarViewModel mToolbarViewModel;
    private MainViewPagerSwipingViewModel mMainViewPagerSwipingViewModel;

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

        mMainViewPagerSwipingViewModel = ViewModelProviders.of(getActivity())
                .get(MainViewPagerSwipingViewModel.class);
        mBanner = view.findViewById(R.id.banner);
        mOpenGallery = view.findViewById(R.id.openGallery);

        mMergeViewModel = ViewModelProviders.of(getActivity()).get(MergeViewModel.class);
        mToolbarViewModel = ViewModelProviders.of(getActivity()).get(ToolbarViewModel.class);

        MobileAds.initialize(getActivity(), "ca-app-pub-6503532425142366~2924525320");

        AdView adView1 = view.findViewById(R.id.adView22);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView1.loadAd(adRequest);

        mRecyclerView = view.findViewById(R.id.recyclerView);
        mMerge = view.findViewById(R.id.view_merge);
        mImport = view.findViewById(R.id.view_import);
        tinydb = new TinyDB(getActivity());
        mergeFilePath = new File(Environment.getExternalStorageDirectory(),
                "FreeCut").getAbsolutePath();
        mListVideos = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                RecyclerView.VERTICAL, false);
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

        //new GetDataSync().execute();

//        mToolbarViewModel.listenForDeleteAllMergeVideos()
//                .observe(this, new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean deleteAll) {
//                if (deleteAll && mListVideos.size() > 0){
//                    mListVideos.clear();
//                    mVideosAdapter.notifyDataSetChanged();
//                }
//            }
//        });

        mMerge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalbytes = 0;
                for (int i = 0; i < mListVideos.size(); i++) {
                    File file = new File(mListVideos.get(i).getImageLink());
                    totalbytes += Integer.parseInt(String.valueOf(file.length()));
                }
                if (mListVideos.isEmpty() || mListVideos.size() == 1) {
                    Toast.makeText(getActivity(), getString(R.string.select_videos_for_merge),
                            Toast.LENGTH_SHORT).show();
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

        mOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), Gallery.class);
//                intent.putExtra("title", "Select media");
//                // Mode 1 for both images and videos selection, 2 for images only and 3 for videos!
//                intent.putExtra("mode", 3);
//                intent.putExtra("maxSelection", 100);
//                // Optional
//                startActivityForResult(intent, OPEN_MEDIA_PICKER);
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("video/mp4");
                startActivityForResult(intent, OPEN_MEDIA_PICKER);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        mMainViewPagerSwipingViewModel.setMainViewPagerSwiping(true);
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_MEDIA_PICKER) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    for (int i = 0 ; i < clipData.getItemCount() ; i++){
                        ClipData.Item videoItem = clipData.getItemAt(i);
                        Uri videoURI = videoItem.getUri();
                        String filePath = getPath(getActivity(), videoURI);
                        mVideoItem = new VideoItem();
                        mVideoItem.setItem_name(new File(filePath).getName());
                        mVideoItem.setImageLink(filePath);
                        mListVideos.add(mVideoItem);
                        All = "";
                        mVideosAdapter = new VideosAdapter(getActivity(), mListVideos,
                                VIDEOS_MERGE, null, new VideoItemDeleteHandler() {
                            @Override
                            public void onClick(int position) {
                                mListVideos.remove(position);
                                mVideosAdapter.notifyDataSetChanged();
                            }
                        });
                        mRecyclerView.setAdapter(mVideosAdapter);
                        mRecyclerView.setVisibility(View.VISIBLE);

                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.select_multiple_videos),
                            Toast.LENGTH_SHORT).show();
                }
//                mProgressDialog = new ProgressDialog(getActivity());
//                mProgressDialog.setTitle(getString(R.string.preparing));
//
//                mProgressDialog.setCancelable(false);
//                mProgressDialog.setMessage(getString(R.string.waiting));
//                //mProgressDialog.show();
//                final ArrayList<String> selectionResult = data.getStringArrayListExtra("result");
//                ind = 0;
//                for (final String item : selectionResult) {
//
//                    /*final String[] command = {"-i", item};
//                    FFmpeg.executeAsync(command, new ExecuteCallback() {
//                        @Override
//                        public void apply(long executionId, int returnCode) {
//                            if (returnCode == RETURN_CODE_SUCCESS) {
//                                ind = ind + 1;*/
//                                /*if (All.contains("Audio:") && All.contains("Video:") && !All.contains("Invalid data found when processing input")) {
//                                    */mVideoItem = new VideoItem();
//                                    mVideoItem.setItem_name(new File(item).getName());
//                                    mVideoItem.setImageLink(item);
//                                    mListVideos.add(mVideoItem);
//                                    All = "";
//                                /*} else {
//                                    All = "";
//                                }*/
//
//                                    mVideosAdapter = new VideosAdapter(getActivity(), mListVideos,
//                                            VIDEOS_MERGE, null, new VideoItemDeleteHandler() {
//                                        @Override
//                                        public void onClick(int position) {
//                                            mListVideos.remove(position);
//                                            mVideosAdapter.notifyDataSetChanged();
//                                        }
//                                    });
//                                    mRecyclerView.setAdapter(mVideosAdapter);
//                                    mProgressDialog.dismiss();
//                                    mImport.setBackgroundTintList(ContextCompat.
//                                            getColorStateList(getActivity(), R.color.grey));
//                                    mMerge.setBackgroundTintList(ContextCompat.
//                                            getColorStateList(getActivity(), R.color.orange));
//
//                           /* }
//                        }
//                    });*/
//                }
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

    private String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider

            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    // Below logic is how External Storage provider build URI for documents
                    // Based on http://stackoverflow.com/questions/28605278/android-5-sd-card-label and https://gist.github.com/prasad321/9852037
                    StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

                    try {
                        Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
                        Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
                        Method getUuid = storageVolumeClazz.getMethod("getUuid");
                        Method getState = storageVolumeClazz.getMethod("getState");
                        Method getPath = storageVolumeClazz.getMethod("getPath");
                        Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
                        Method isEmulated = storageVolumeClazz.getMethod("isEmulated");

                        Object result = getVolumeList.invoke(mStorageManager);

                        final int length = Array.getLength(result);
                        for (int i = 0; i < length; i++) {
                            Object storageVolumeElement = Array.get(result, i);
                            //String uuid = (String) getUuid.invoke(storageVolumeElement);

                            final boolean mounted = Environment.MEDIA_MOUNTED.equals(getState.invoke(storageVolumeElement))
                                    || Environment.MEDIA_MOUNTED_READ_ONLY.equals(getState.invoke(storageVolumeElement));

                            //if the media is not mounted, we need not get the volume details
                            if (!mounted) continue;

                            //Primary storage is already handled.
                            if ((Boolean) isPrimary.invoke(storageVolumeElement) && (Boolean) isEmulated.invoke(storageVolumeElement))
                                continue;

                            String uuid = (String) getUuid.invoke(storageVolumeElement);

                            if (uuid != null && uuid.equals(type)) {
                                String res = getPath.invoke(storageVolumeElement) + "/" + split[1];
                                return res;
                            }
                        }
                    } catch (Exception ex) {
                    }
                }
            }


            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                }
                String[] contentUriPrefixesToTry = new String[]{
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads",
                        "content://downloads/all_downloads"
                };

                for (String contentUriPrefix : contentUriPrefixesToTry) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
                    try {
                        String path = getDataColumn(context, contentUri, null, null);
                        if (path != null) {
                            return path;
                        }
                    } catch (Exception e) {
                    }
                }

                return null;
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

//            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri.
     */
    private String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}