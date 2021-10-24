package force.freecut.freecut.ui.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.Locale;

import force.freecut.freecut.R;
import force.freecut.freecut.ui.fragments.MainFragment;
import force.freecut.freecut.utils.SharedPrefUtil;
import force.freecut.freecut.view_models.ToolbarViewModel;

import static force.freecut.freecut.utils.Constants.LOCALE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    TextView mTitle;
    ImageView mIconDelete;
    TextView mDeleteAll;
    //ImageView mBackHome;
    ImageView mBack;
    private ToolbarViewModel mToolbarViewModel;


    @Override
    protected void attachBaseContext(Context newBase) {
        String language = SharedPrefUtil.getInstance(newBase).read(LOCALE, Locale.getDefault()
                .getLanguage());
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = newBase.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        newBase = newBase.createConfigurationContext(configuration);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set App Language
        String language = SharedPrefUtil.getInstance(this).read(LOCALE, Locale.getDefault()
                .getLanguage());
        Locale locale = new Locale(language);
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        createConfigurationContext(configuration);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkWriteExternalPermission() || !checkReadExternalPermission()) {
            startPermissionActivity();
        }

        Log.d(TAG, "Main Activity Created");

        mTitle = findViewById(R.id.title);
        mIconDelete = findViewById(R.id.icon_delete);
        mDeleteAll = findViewById(R.id.delete);
        //mBackHome = findViewById(R.id.home);
        mBack = findViewById(R.id.back);

        mToolbarViewModel = ViewModelProviders.of(this).get(ToolbarViewModel.class);
        setToolBar();
        loadFragment(getSupportFragmentManager(),
                MainFragment.newInstance(null, null), false);

    }

    private void setToolBar() {
        mToolbarViewModel.getToolbarTitle().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String title) {
                mTitle.setText(title);
            }
        });
        mDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToolbarViewModel.deleteAllMergeVideos(true);
            }
        });
        mToolbarViewModel.deleteAllVisibility().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean visible) {
                if (visible) {
                    mIconDelete.setVisibility(View.VISIBLE);
                    mDeleteAll.setVisibility(View.VISIBLE);
                } else {
                    mIconDelete.setVisibility(View.GONE);
                    mDeleteAll.setVisibility(View.GONE);
                }
            }
        });
        mIconDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToolbarViewModel.deleteAllMergeVideos(true);
            }
        });
//        mToolbarViewModel.backHomeButtonVisibility().observe(this, new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean visible) {
//                if (visible) {
//                    mBackHome.setVisibility(View.VISIBLE);
//                } else {
//                    mBackHome.setVisibility(View.GONE);
//                }
//            }
//        });
//        mBackHome.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadFragment(getSupportFragmentManager(), MainFragment.newInstance(null, null),
//                                false);
//                    }
//                }, 0);
//            }
//        });

        mToolbarViewModel.backButtonVisibility().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean visible) {
                if (visible) {
                    mBack.setVisibility(View.VISIBLE);
                } else {
                    mBack.setVisibility(View.GONE);
                }
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public static void loadFragment(FragmentManager fragmentManager,
                                    Fragment fragment,
                                    boolean addToBackStack) {
        if (addToBackStack) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    private boolean checkWriteExternalPermission() {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = MainActivity.this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkReadExternalPermission() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        int res = MainActivity.this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void startPermissionActivity() {
        Intent intent = new Intent(MainActivity.this, PermissionActivity.class);
        startActivity(intent);
        finish();
    }
}