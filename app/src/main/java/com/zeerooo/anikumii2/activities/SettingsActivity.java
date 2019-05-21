package com.zeerooo.anikumii2.activities;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.zeerooo.anikumii2.BuildConfig;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiDialog;
import com.zeerooo.anikumii2.anikumiiparts.glide.GlideApp;
import com.zeerooo.anikumii2.services.AccountsService;
import com.zeerooo.anikumii2.services.NotificationService;

import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity {
    public static boolean loadHeaderInfo, reloadMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setSubtitle(BuildConfig.VERSION_NAME);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_fragmentContainer, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sendMail) {
            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "chavesjuan400@gmail.com", null)));
        } else
            onBackPressed();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loadHeaderInfo || reloadMain) {
            startActivity(new Intent(this, MainActivity.class).putExtra("loadHeaderInfo", loadHeaderInfo).putExtra("reloadMain", reloadMain));
            reloadMain = false;
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings, rootKey);

            // set onPreferenceClickListener for a few preferences
            findPreference("pref_notifications").setOnPreferenceClickListener(this);
            findPreference("pref_more").setOnPreferenceClickListener(this);
            findPreference("pref_accounts").setOnPreferenceClickListener(this);
            findPreference("pref_advanced").setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case "pref_notifications":
                    getFragmentManager()
                            .beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.settings_fragmentContainer, new NotificationFragment())
                            .commit();
                    break;
                case "pref_more":
                    getFragmentManager()
                            .beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.settings_fragmentContainer, new AboutFragment())
                            .commit();
                    break;
                case "pref_accounts":
                    getFragmentManager()
                            .beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.settings_fragmentContainer, new AccountFragment())
                            .commit();
                    break;
                case "pref_advanced":
                    getFragmentManager()
                            .beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.settings_fragmentContainer, new AdvancedFragment())
                            .commit();
                    break;
            }
            return true;
        }
    }

    public static class AccountFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
        private TextView tioName, malName;
        private ImageView tioAvatarView, malAvatarView;
        private boolean sync = true;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName("ZeeRooo@Anikumii!!");
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
            setPreferencesFromResource(R.xml.pref_account, rootKey);
            setHasOptionsMenu(true);

            View rootView = getActivity().findViewById(R.id.pref_accLayout);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        v.removeOnLayoutChangeListener(this);
                        Animator anim = ViewAnimationUtils.createCircularReveal(v, (left + right) / 2, (bottom + top) / 2, 0, (char) Math.hypot(right, bottom));
                        anim.setInterpolator(new AccelerateDecelerateInterpolator());
                        anim.setDuration(350);
                        anim.start();
                    }
                });
            }
            rootView.setVisibility(View.VISIBLE);

            findPreference("pref_account_login").setOnPreferenceClickListener(this);
            findPreference("pref_account_logOut").setOnPreferenceClickListener(this);
            findPreference("pref_updateAccounts").setOnPreferenceClickListener(this);
        }

        @Override
        public void onDetach() {
            super.onDetach();
            getActivity().findViewById(R.id.pref_accLayout).setVisibility(View.GONE);
        }

        @Override
        public void onResume() {
            super.onResume();
            if (sync) {
                String malAvatar = getPreferenceManager().getSharedPreferences().getString("MALuserAvatar", null);
                malAvatarView = getActivity().findViewById(R.id.pref_malAvatar);
                if (malAvatar != null)
                    GlideApp.with(getActivity()).load(malAvatar).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop()).into(malAvatarView);
                malName = getActivity().findViewById(R.id.pref_malName);
                malName.setText(getPreferenceManager().getSharedPreferences().getString("MALuserName", getString(R.string.warning_not_logged, "MyAnimeList")));
            }
        }

        @Override
        public boolean onPreferenceClick(final Preference preference) {
            switch (preference.getKey()) {
                case "pref_account_login":
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    sync = true;
                    break;
                case "pref_account_logOut":
                    getPreferenceManager().getSharedPreferences().edit().remove("MALuserName").apply();
                    getPreferenceManager().getSharedPreferences().edit().remove("MALuserAvatar").apply();
                    getPreferenceManager().getSharedPreferences().edit().remove("mal").apply();
                    malName.setText(getString(R.string.app_name));
                    malAvatarView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_launcher_foreground));
                    loadHeaderInfo = true;
                    break;
                case "pref_updateAccounts":
                    getActivity().startService(new Intent(getActivity(), AccountsService.class).putExtra("userInfo", true));
                    loadHeaderInfo = true;
                    break;
            }
            return true;
        }
    }

    public static class NotificationFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

        private WorkManager workManager;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName("ZeeRooo@Anikumii!!");
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
            setPreferencesFromResource(R.xml.pref_notifications, rootKey);
            setHasOptionsMenu(true);

            workManager = WorkManager.getInstance();

            findPreference("notifInterval").setOnPreferenceChangeListener(this);
            findPreference("enableNotif").setOnPreferenceChangeListener(this);
            findPreference("headsUp").setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey().equals("headsUp"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    startActivity(new Intent().setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", getActivity().getPackageName(), null)));
            return true;
        }

        private void notificationStuff(String notifInterval) {
            workManager.enqueue(new PeriodicWorkRequest.Builder(NotificationService.class, Integer.valueOf(notifInterval), TimeUnit.MILLISECONDS)
                    .setConstraints(new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build());
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case "notifInterval":
                    workManager.cancelAllWork();
                    notificationStuff(newValue.toString());
                    break;
                case "enableNotif":
                    if ((boolean) newValue)
                        workManager.enqueue(new PeriodicWorkRequest.Builder(NotificationService.class, Integer.valueOf(preference.getSharedPreferences().getString("notifInterval", "3600000")), TimeUnit.MILLISECONDS)
                                .setConstraints(new Constraints.Builder()
                                        .setRequiredNetworkType(NetworkType.CONNECTED)
                                        .build())
                                .build());
                    else
                        workManager.cancelAllWork();

                    break;
            }
            return true;
        }
    }

    public static class AdvancedFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_advanced, rootKey);
            setHasOptionsMenu(true);
            getPreferenceManager().setSharedPreferencesName("ZeeRooo@Anikumii!!");
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);

            findPreference("defaultServer").setOnPreferenceClickListener(this);
            findPreference("defaultServer").setSummary(getString(R.string.defaultServerSummary, getPreferenceManager().getSharedPreferences().getString("defaultServer", "Zippyshare")));

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                getPreferenceScreen().removePreference(findPreference("enablePip"));

            Preference columns;
            columns = findPreference("gridColumns");
            columns.setOnPreferenceClickListener(this);
            columns.setSummary(getString(R.string.columnsSummary, getPreferenceManager().getSharedPreferences().getInt("gridColumnsPortrait", Math.round((float) getResources().getDisplayMetrics().widthPixels / 300)), getPreferenceManager().getSharedPreferences().getInt("gridColumnsLandscape", Math.round((float) getResources().getDisplayMetrics().heightPixels / 300))));
        }


        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case "defaultServer":
                    AnikumiiDialog defaultServer = new AnikumiiDialog(getActivity());
                    defaultServer.serverDialog(preference.getSharedPreferences().getString("defaultServer", "Zippyshare")).setOnCheckedChangeListener((ChipGroup group, int checkedId) -> {
                        if (checkedId != -1) {
                            preference.getSharedPreferences().edit().putString("defaultServer", (String) ((Chip) group.findViewById(checkedId)).getText()).apply();
                            findPreference("defaultServer").setSummary(getString(R.string.defaultServerSummary, (String) ((Chip) group.findViewById(checkedId)).getText()));

                            defaultServer.dismiss();
                        }
                    });
                    break;
                case "gridColumns":
                    AnikumiiDialog columnsDialog = new AnikumiiDialog(getActivity());

                    View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_column, null);

                    final NumberPicker portraitNumberPicker = dialogView.findViewById(R.id.numberPickerColumnsPortrait);
                    portraitNumberPicker.setMinValue(1);
                    portraitNumberPicker.setMaxValue(Math.round((float) getResources().getDisplayMetrics().widthPixels / 300));

                    final NumberPicker landscapeNumberPicker = dialogView.findViewById(R.id.numberPickerColumnsLandscape);
                    landscapeNumberPicker.setMinValue(1);
                    landscapeNumberPicker.setMaxValue(Math.round((float) getResources().getDisplayMetrics().heightPixels / 300));

                    columnsDialog.setButton(DialogInterface.BUTTON_POSITIVE, getText(android.R.string.ok), (DialogInterface dialogInterface, int i) -> {
                        getPreferenceManager().getSharedPreferences().edit().putInt("gridColumnsPortrait", portraitNumberPicker.getValue()).apply();
                        getPreferenceManager().getSharedPreferences().edit().putInt("gridColumnsLandscape", landscapeNumberPicker.getValue()).apply();
                        findPreference("gridColumns").setSummary(getString(R.string.columnsSummary, portraitNumberPicker.getValue(), landscapeNumberPicker.getValue()));
                        reloadMain = true;
                        columnsDialog.dismiss();
                    });
                    columnsDialog.addCancelButton();
                    columnsDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Reset", (DialogInterface dialogInterface, int i) -> {
                        getPreferenceManager().getSharedPreferences().edit().remove("gridColumnsPortrait").apply();
                        getPreferenceManager().getSharedPreferences().edit().remove("gridColumnsLandscape").apply();
                        findPreference("gridColumns").setSummary(getString(R.string.columnsSummary, getPreferenceManager().getSharedPreferences().getInt("gridColumnsPortrait", Math.round((float) getResources().getDisplayMetrics().widthPixels / 400)), getPreferenceManager().getSharedPreferences().getInt("gridColumnsLandscape", Math.round((float) getResources().getDisplayMetrics().heightPixels / 400))));
                        columnsDialog.dismiss();
                        reloadMain = true;
                    });

                    columnsDialog.initialize(getString(R.string.columnsTitle), dialogView);
                    break;
            }
            return true;
        }
    }

    public static class AboutFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_about, rootKey);
            setHasOptionsMenu(true);
        }
    }
}