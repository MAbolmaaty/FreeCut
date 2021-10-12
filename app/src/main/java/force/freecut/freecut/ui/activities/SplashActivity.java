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
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import force.freecut.freecut.Data.TinyDB;
import force.freecut.freecut.R;
import force.freecut.freecut.helper.MyContextWrapper;
import force.freecut.freecut.helper.PreferenceHelper;
import force.freecut.freecut.utils.SharedPrefUtil;

import static force.freecut.freecut.utils.Constants.LOCALE;

public class SplashActivity extends AppCompatActivity {

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkWriteExternalPermission() && checkReadExternalPermission()) {
                        startMainActivity();
                    } else {
                        startPermissionActivity();
                    }

                } else {
                    startMainActivity();
                }
            }
        }, 996);
    }

    private boolean checkWriteExternalPermission() {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = SplashActivity.this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkReadExternalPermission() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        int res = SplashActivity.this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }


    private void startPermissionActivity() {
        Intent intent = new Intent(SplashActivity.this, PermissionActivity.class);
        startActivity(intent);
        finish();
    }

    private void startMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}