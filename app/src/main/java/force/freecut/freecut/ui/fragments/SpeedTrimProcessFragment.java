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
import static force.freecut.freecut.utils.Constants.SEGMENT_TIME;
import static force.freecut.freecut.utils.Constants.VIDEO_PATH;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SpeedTrimProcessFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SpeedTrimProcessFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = SpeedTrimProcessFragment.class.getSimpleName();

    private String mParam1;
    private String mParam2;

    TextView mProgress;
    TextView mPercentage;
    ProgressBar mProgressBar;

    public SpeedTrimProcessFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SpeedTrimProcessFragment.
     */
    public static SpeedTrimProcessFragment newInstance(String param1, String param2) {
        SpeedTrimProcessFragment fragment = new SpeedTrimProcessFragment();
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
        View view = inflater.inflate(R.layout.fragment_speed_trim_process, container, false);
        mProgress = view.findViewById(R.id.progress);
        mProgressBar = view.findViewById(R.id.progressBar);
        mPercentage = view.findViewById(R.id.percentage);

        TrimViewModel trimViewModel = ViewModelProviders.of(getActivity()).get(TrimViewModel.class);
        trimViewModel.getTrimBundle().observe(this, new Observer<Bundle>() {
            @Override
            public void onChanged(Bundle bundle) {
                final String[] commandTrim = {"-i", bundle.getString(VIDEO_PATH),
                        "-codec:a", "copy", "-f", "segment", "-segment_time",
                        bundle.getString(SEGMENT_TIME), "-codec:v", "copy",
                        "-map", "0", bundle.getString(FILE_PATH)};
                long executionId = FFmpeg.executeAsync(commandTrim, new ExecuteCallback() {
                    @Override
                    public void apply(long executionId, int returnCode) {
                        Log.d(TAG, "start");
                        if (returnCode == RETURN_CODE_SUCCESS) {
                            Log.d(TAG, "success");
                            if (getActivity() != null) {
                                mProgressBar.setProgress(100);
                                mPercentage.setText("100 %");
                                loadFragment(getActivity().getSupportFragmentManager(),
                                        SuccessFragment2.newInstance(null, null), false);
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