package force.freecut.freecut.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import force.freecut.freecut.R;
import force.freecut.freecut.adapters.MainPagerAdapter;
import force.freecut.freecut.view_models.MainViewPagerSwipingViewModel;
import force.freecut.freecut.view_models.ToolbarViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    private static final String TAG = MainFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    BottomNavigationView mBottomNavigationView;
    private ViewPager2 mViewPager;
    private ToolbarViewModel mToolbarViewModel;
    private MainViewPagerSwipingViewModel mMainViewPagerSwipingViewModel;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Main Fragment Created");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        Log.d(TAG, "Main Fragment View Created");
        mToolbarViewModel = ViewModelProviders.of(getActivity()).get(ToolbarViewModel.class);
        mMainViewPagerSwipingViewModel = ViewModelProviders.of(getActivity())
                .get(MainViewPagerSwipingViewModel.class);
        mBottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        mViewPager = view.findViewById(R.id.viewPager);

        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(getChildFragmentManager(),
                getLifecycle());
        mViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        mViewPager.setAdapter(mainPagerAdapter);
        mViewPager.setCurrentItem(2, false);
        mViewPager.setOffscreenPageLimit(3);
        setBottomNavigationItemSelected();
        setViewPagerChangeListener();
        mViewPager.setPageTransformer(new DepthPageTransformer());

        mBottomNavigationView.setSelectedItemId(R.id.navigation_trim);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mBottomNavigationView.getMenu().findItem(R.id.navigation_trim).setChecked(true);
                mViewPager.setCurrentItem(2, false);
                setTrimFragmentToolBar();
            }
        }, 0);

        mMainViewPagerSwipingViewModel.getMainViewPagerSwiping().observe(this,
                swiping -> mViewPager.setUserInputEnabled(swiping));

        return view;
    }

    private void setBottomNavigationItemSelected() {
        mBottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.navigation_trim:
                                mViewPager.setCurrentItem(2);
                                setTrimFragmentToolBar();
                                return true;
                            case R.id.navigation_merge:
                                mViewPager.setCurrentItem(1);
                                setMergeFragmentToolBar();
                                return true;
                            case R.id.navigation_about:
                                mViewPager.setCurrentItem(0);
                                setAboutFragmentToolBar();
                                return true;
//                            case R.id.navigation_my_list:
//                                mViewPager.setCurrentItem(0);
//                                setMyListFragmentToolBar();
//                                return true;
                        }
                        return false;
                    }
                });
    }

    private void setViewPagerChangeListener(){
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position){
//                    case 0:
//                        mBottomNavigationView.setSelectedItemId(R.id.navigation_my_list);
//                        setMyListFragmentToolBar();
//                        break;
                    case 0:
                        mBottomNavigationView.setSelectedItemId(R.id.navigation_about);
                        setAboutFragmentToolBar();
                        break;
                    case 1:
                        mBottomNavigationView.setSelectedItemId(R.id.navigation_merge);
                        setMergeFragmentToolBar();
                        break;
                    case 2:
                        mBottomNavigationView.setSelectedItemId(R.id.navigation_trim);
                        setTrimFragmentToolBar();
                        break;
                }
            }
        });
    }

    private void setTrimFragmentToolBar(){
        mToolbarViewModel.setToolbarTitle(getString(R.string.trim));
        mToolbarViewModel.showDeleteAll(false);
        mToolbarViewModel.showBackHomeButton(false);
    }

    private void setMergeFragmentToolBar(){
        mToolbarViewModel.setToolbarTitle(getString(R.string.merge));
        //mToolbarViewModel.showDeleteAll(true);
        mToolbarViewModel.showBackHomeButton(false);
    }

    private void setAboutFragmentToolBar(){
        mToolbarViewModel.setToolbarTitle(getString(R.string.about_app));
        mToolbarViewModel.showDeleteAll(false);
        mToolbarViewModel.showBackHomeButton(false);
    }

    private void setMyListFragmentToolBar(){
        mToolbarViewModel.setToolbarTitle(getString(R.string.my_list));
        mToolbarViewModel.showDeleteAll(false);
        mToolbarViewModel.showBackHomeButton(false);
    }

    public class DepthPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1f);
                view.setTranslationX(0f);
                view.setTranslationZ(0f);
                view.setScaleX(1f);
                view.setScaleY(1f);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);
                // Move it behind the left page
                view.setTranslationZ(-1f);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }
}