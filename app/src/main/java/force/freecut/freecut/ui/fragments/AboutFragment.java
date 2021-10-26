package force.freecut.freecut.ui.fragments;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Locale;

import force.freecut.freecut.R;
import force.freecut.freecut.utils.ShareUtils;
import force.freecut.freecut.utils.SharedPrefUtil;
import force.freecut.freecut.view_models.MainViewPagerSwipingViewModel;

import static force.freecut.freecut.utils.Constants.LOCALE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AboutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AboutFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private View mChangeLanguage;
    private TextView mCurrentLanguage;
    private TextView mWebsite;
    private TextView mAppleStore;
    private TextView mGooglePlay;
    private MainViewPagerSwipingViewModel mMainViewPagerSwipingViewModel;

    public AboutFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AboutFragment.
     */
    public static AboutFragment newInstance(String param1, String param2) {
        AboutFragment fragment = new AboutFragment();
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
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        mMainViewPagerSwipingViewModel = ViewModelProviders.of(getActivity())
                .get(MainViewPagerSwipingViewModel.class);
        mCurrentLanguage = view.findViewById(R.id.text_language);
        mWebsite = view.findViewById(R.id.web_link);
        mAppleStore = view.findViewById(R.id.apple_store);
        mGooglePlay = view.findViewById(R.id.google_store);
        String language = SharedPrefUtil.getInstance(getActivity()).read(LOCALE, Locale.getDefault().getLanguage());
        if (language.equals("ar")){
            mCurrentLanguage.setText(getText(R.string.arabic));
        } else {
            mCurrentLanguage.setText(getText(R.string.english));
        }
        mChangeLanguage = view.findViewById(R.id.view_changeLanguage);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(inflater.inflate(R.layout.dialog_language_change, null));
        final AlertDialog dialogChangeLanguage = builder.create();
        mChangeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogChangeLanguage.show();

                RadioButton buttonArabic = dialogChangeLanguage.findViewById(R.id.button_arabic);
                RadioButton buttonEnglish = dialogChangeLanguage.findViewById(R.id.button_english);

                buttonArabic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogChangeLanguage.dismiss();
                        SharedPrefUtil.getInstance(getActivity()).write(LOCALE, "ar");
                        //mBottomNavigationViewModel.setSelectedItem(R.id.navigation_trim);
                        getActivity().recreate();
                    }
                });

                buttonEnglish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogChangeLanguage.dismiss();
                        SharedPrefUtil.getInstance(getActivity()).write(LOCALE, "en");
                        //mBottomNavigationViewModel.setSelectedItem(R.id.navigation_trim);
                        getActivity().recreate();
                    }
                });
            }
        });

        mWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareUtils.OpenLink(getActivity(),"https://www.forcetouches.com/");
            }
        });

        mAppleStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareUtils.OpenLink(getActivity(),"https://itunes.apple.com/sa/developer/ibrahim-alomar/id1120890963?mt=8");
            }
        });

        mGooglePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareUtils.OpenLink(getActivity(),"https://play.google.com/store/apps/developer?id=Force+Touches");
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        mMainViewPagerSwipingViewModel.setMainViewPagerSwiping(true);
        super.onResume();
    }
}