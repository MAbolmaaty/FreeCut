package force.freecut.freecut.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.apache.commons.lang.StringUtils;

import java.io.File;

import force.freecut.freecut.Data.TinyDB;
import force.freecut.freecut.Data.Utils;
import force.freecut.freecut.R;
import force.freecut.freecut.view_models.MergeViewModel;
import force.freecut.freecut.view_models.ToolbarViewModel;

import static android.content.ContentValues.TAG;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static force.freecut.freecut.ui.activities.MainActivity.loadFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MergeProcessFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MergeProcessFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    String[] array;
    long totalTime = 0;
    String mergeFilePath, state;
    File dest;
    private ProgressBar mProgressBar;
    private TextView mPercentage;
    private TextView mProgress;

    public MergeProcessFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MergeProcessFragment.
     */
    public static MergeProcessFragment newInstance(String param1, String param2) {
        MergeProcessFragment fragment = new MergeProcessFragment();
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
        View view = inflater.inflate(R.layout.fragment_merge_process, container, false);
        MergeViewModel mergeViewModel = ViewModelProviders.of(getActivity()).get(MergeViewModel.class);
        ToolbarViewModel toolbarViewModel = ViewModelProviders.of(getActivity()).get(ToolbarViewModel.class);
        toolbarViewModel.showDeleteAll(false);
        mProgressBar = view.findViewById(R.id.progressBar);
        mProgress = view.findViewById(R.id.progress);
        mPercentage = view.findViewById(R.id.percentage);
        MobileAds.initialize(getActivity(), "ca-app-pub-6503532425142366~2924525320");
        AdView adView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        final TinyDB tinydb = new TinyDB(getActivity());
        mergeViewModel.getMergeBundle().observe(this, new Observer<Bundle>() {
            @Override
            public void onChanged(Bundle bundle) {
                array = bundle.getStringArray("link");
                state = bundle.getString("state");
                mergeFilePath = new File(Environment.getExternalStorageDirectory(),
                        "FreeCut").getAbsolutePath();
                tinydb.putString("cut", "FreeCut");
                mProgress.setText(getString(R.string.your_process_is_executing_now));
                mProgressBar.setProgress(10);
                mPercentage.setText("10 %");
                File dir = new File(mergeFilePath);
                String[] children = null;
                if (dir.isDirectory()) {
                    children = dir.list();
                }
                if (state.equals("0")) {
                    dest = new File(mergeFilePath, "merge" + children.length + 1 + ".mp4" + "");
                } else {
                    dest = new File(mergeFilePath,
                            "merge" + children.length + 1 + array[0].substring(array[0].
                                    lastIndexOf(".")));
                }
                for (int i = 0; i < array.length; i++) {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(getActivity(), Uri.fromFile(new File(array[i])));
                    String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    long timeInMillisec = Long.parseLong(time);
                    totalTime += timeInMillisec;
                    retriever.release();
                }
                tinydb.putString("mer", dest.getAbsolutePath());
                String RestOrder[] = {"-ab", "48000", "-ac",
                        "2", "-ar", "22050", "-s", "480x640", "-vcodec",
                        "libx264", "-crf", "27", "-preset", "ultrafast", dest.getAbsolutePath()};
                int numOfElements = (array.length * 2) + 18;
                String[] commandall = new String[numOfElements];
                commandall[0] = "-y";
                int s = 0;
                for (int i = 1; i < commandall.length; i++) {
                    if (i <= array.length * 2) {
                        if (i % 2 != 0)
                            commandall[i] = "-i";
                        else {
                            commandall[i] = array[s];
                            s++;
                        }
                    } else {
                        commandall[i] = "-filter_complex";
                        ++i;
                        commandall[i] = "";
                        String cout = "";
                        for (int j = 0; j < array.length; j++) {
                            commandall[i] += "[" + j + ":v]scale=480x640,setsar=1[v" + j + "];";
                            cout += "[" + "v" + j + "" + "]" + "[" + j + ":a]";
                            if (j == array.length - 1) {
                                commandall[i] += cout;
                            }
                        }
                        commandall[i] += "concat=n=" + array.length + ":v=1:a=1";
                        for (int k = 0; k < RestOrder.length; k++) {
                            i++;
                            commandall[i] = RestOrder[k];
                        }
                    }
                }
                String[] command4 = commandall;
                long executionId = FFmpeg.executeAsync(command4, new ExecuteCallback() {
                    @Override
                    public void apply(long executionId, int returnCode) {
                        if (returnCode == RETURN_CODE_SUCCESS) {
                            Toast.makeText(getActivity(), "Your file saved in FreeCut collected videos ", Toast.LENGTH_SHORT).show();
                            //createNotification();
                            mProgressBar.setProgress(100);
                            mPercentage.setText("100 %");
                            loadFragment(getActivity().getSupportFragmentManager(),
                                    SuccessFragment.newInstance(null, null),
                                    false);
                        }
                    }
                });
            }
        });

        return view;
    }

    /*public void createNotification() {
        Intent intent = new Intent(getActivity(), SuccessActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(getActivity(), (int) System.currentTimeMillis(), intent, 0);

        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(getActivity())
                .setContentTitle("Your Operation is completed  FreeCut")
                .setContentText("FreeCut").setSmallIcon(R.drawable.icon_small)
                .setContentIntent(pIntent)
                .build();
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        noti.defaults |= Notification.DEFAULT_SOUND;

        notificationManager.notify(0, noti);

    }*/
}