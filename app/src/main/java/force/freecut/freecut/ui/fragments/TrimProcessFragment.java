package force.freecut.freecut.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;

import force.freecut.freecut.R;
import force.freecut.freecut.view_models.TrimViewModel;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static force.freecut.freecut.ui.activities.MainActivity.loadFragment;
import static force.freecut.freecut.utils.Constants.FILE_PATH;
import static force.freecut.freecut.utils.Constants.TRIM_NUMBER_OF_SECONDS;
import static force.freecut.freecut.utils.Constants.TRIM_VIDEO_PATH;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrimProcessFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrimProcessFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = TrimProcessFragment.class.getSimpleName();

    private String mParam1;
    private String mParam2;

    TextView mProgress;
    TextView mPercentage;
    ProgressBar mProgressBar;

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
        mProgress = view.findViewById(R.id.progress);
        mProgressBar = view.findViewById(R.id.progressBar);
        mPercentage = view.findViewById(R.id.percentage);

        TrimViewModel trimViewModel = ViewModelProviders.of(getActivity()).get(TrimViewModel.class);
        trimViewModel.getTrimBundle().observe(this, new Observer<Bundle>() {
            @Override
            public void onChanged(Bundle bundle) {
                Log.d(TAG, "File PATH : " + bundle.getString(FILE_PATH));
                final String[] CutCommand = {"-i", bundle.getString(TRIM_VIDEO_PATH),
                        "-y", "-acodec", "copy", "-f", "segment", "-segment_time",
                        bundle.getString(TRIM_NUMBER_OF_SECONDS), "-vcodec", "copy",
                        "-reset_timestamps", "1", "-map", "0", bundle.getString(FILE_PATH)};
                final String[] command = {"-ss", "00:00:00", "-i",
                        bundle.getString(TRIM_VIDEO_PATH), "-c", "copy", "-t", "00:00:10",
                        bundle.getString(FILE_PATH)};
                final String[] command2 = {"-y", "-i", bundle.getString(TRIM_VIDEO_PATH),
                "-ss", "00:00:05", "-t", "00:00:05", "-async", "1", "-strict", "-2",
                        bundle.getString(FILE_PATH)};
                long executionId = FFmpeg.executeAsync(CutCommand, new ExecuteCallback() {
                    @Override
                    public void apply(long executionId, int returnCode) {
                        Log.d(TAG, "start");
                        if (returnCode == RETURN_CODE_SUCCESS) {
                            Log.d(TAG, "success");
                            if (getActivity() != null) {
                                mProgressBar.setProgress(100);
                                mPercentage.setText("100 %");
                                loadFragment(getActivity().getSupportFragmentManager(),
                                        SuccessFragment.newInstance(null, null), false);
                            }
                        } else {
                            Log.d(TAG, "fail");
                        }
                    }
                });
            }
        });

        return view;
    }
}