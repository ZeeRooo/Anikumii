package com.zeerooo.anikumii.activities;

import android.animation.Animator;
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
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.zeerooo.anikumii.BuildConfig;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiBottomSheetDialog;
import com.zeerooo.anikumii.anikumiiparts.glide.AnikumiiGlideModule;
import com.zeerooo.anikumii.services.AccountsService;
import com.zeerooo.anikumii.services.NotificationService;
import com.zeerooo.anikumii.services.UpdateService;

import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity {
    private static boolean reloadMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setSupportActionBar(findViewById(R.id.toolbar));

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
        if (reloadMain) {
            startActivity(new Intent(this, MainActivity.class).putExtra("reloadMain", reloadMain));
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
        private TextView malName;
        private ImageView malAvatarView;
        private boolean sync = true;
        private Preference logOut, syncAccounts;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName("ZeeRooo@Anikumii!!");
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
            setPreferencesFromResource(R.xml.pref_account, rootKey);

            final View rootView = getActivity().findViewById(R.id.pref_accLayout);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        v.removeOnLayoutChangeListener(this);
                        final Animator anim = ViewAnimationUtils.createCircularReveal(v, (left + right) / 2, (bottom + top) / 2, 0, (char) Math.hypot(right, bottom));
                        anim.setInterpolator(new AccelerateDecelerateInterpolator());
                        anim.setDuration(350);
                        anim.start();
                    }
                });
            }
            rootView.setVisibility(View.VISIBLE);

            findPreference("pref_account_login").setOnPreferenceClickListener(this);
            logOut = findPreference("pref_account_logOut");
            syncAccounts = findPreference("pref_updateAccounts");

            if (!getPreferenceManager().getSharedPreferences().getString("malUserName", "").equals("")) {
                logOut.setOnPreferenceClickListener(this);
                syncAccounts.setOnPreferenceClickListener(this);
            } else {
                logOut.setVisible(false);
                syncAccounts.setVisible(false);
            }
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
                final String malAvatar = getPreferenceManager().getSharedPreferences().getString("malUserAvatar", null);
                malAvatarView = getActivity().findViewById(R.id.pref_malAvatar);
                if (malAvatar != null)
                    Glide.with(getActivity()).load(malAvatar).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop()).into(malAvatarView);
                malName = getActivity().findViewById(R.id.pref_malName);
                malName.setText(getPreferenceManager().getSharedPreferences().getString("malUserName", getString(R.string.warning_not_logged)));
            }
        }

        @Override
        public boolean onPreferenceClick(final Preference preference) {
            switch (preference.getKey()) {
                case "pref_account_login":
                    new AnikumiiBottomSheetDialog(getActivity()).loginDialog();
                    sync = true;
                    break;
                case "pref_account_logOut":
                    getPreferenceManager().getSharedPreferences().edit().remove("malUserName").apply();
                    getPreferenceManager().getSharedPreferences().edit().remove("malUserAvatar").apply();
                    getPreferenceManager().getSharedPreferences().edit().remove("malCookie").apply();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        CookieManager.getInstance().removeAllCookies(null);
                        CookieManager.getInstance().flush();
                    } else {
                        final CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(getActivity());
                        cookieSyncManager.startSync();
                        final CookieManager cookieManager = CookieManager.getInstance();
                        cookieManager.removeAllCookie();
                        cookieManager.removeSessionCookie();
                        cookieSyncManager.stopSync();
                        cookieSyncManager.sync();
                    }

                    logOut.setVisible(false);
                    syncAccounts.setVisible(false);
                    malName.setText(getString(R.string.app_name));
                    malAvatarView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_launcher_foreground));
                    reloadMain = true;
                    break;
                case "pref_updateAccounts":
                    getActivity().startService(new Intent(getActivity(), AccountsService.class).putExtra("startMain", true));
                    reloadMain = true;
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
            workManager.enqueue(new PeriodicWorkRequest.Builder(NotificationService.class, Integer.valueOf(notifInterval), TimeUnit.HOURS)
                    .addTag("notification_work")
                    .setConstraints(new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build());
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case "notifInterval":
                    workManager.cancelAllWorkByTag("notification_work");
                    notificationStuff(newValue.toString());
                    break;
                case "enableNotif":
                    if ((boolean) newValue)
                        notificationStuff(preference.getSharedPreferences().getString("notifInterval", "3"));
                    else
                        workManager.cancelAllWork();

                    break;
            }
            return true;
        }
    }

    public static class AdvancedFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
        private Preference serverPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName("ZeeRooo@Anikumii!!");
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
            setPreferencesFromResource(R.xml.pref_advanced, rootKey);

            serverPreference = findPreference("defaultServer");
            serverPreference.setOnPreferenceClickListener(this);
            serverPreference.setSummary(getString(R.string.defaultServerSummary, getPreferenceManager().getSharedPreferences().getString("defaultServer", "Zippyshare")));

            Preference columns;
            columns = findPreference("gridColumns");
            columns.setOnPreferenceClickListener(this);
            columns.setSummary(getString(R.string.columnsSummary, getPreferenceManager().getSharedPreferences().getInt("gridColumnsPortrait", Math.round((float) getResources().getDisplayMetrics().widthPixels / 300)), getPreferenceManager().getSharedPreferences().getInt("gridColumnsLandscape", Math.round((float) getResources().getDisplayMetrics().heightPixels / 300))));

            findPreference("updater_now").setOnPreferenceClickListener(this);
            final Preference updater = findPreference("updater_enable");
            if (BuildConfig.VERSION_NAME.endsWith("github"))
                updater.setOnPreferenceChangeListener((Preference preference, Object newValue) -> {
                    if ((boolean) newValue) {
                        WorkManager.getInstance().enqueue(new PeriodicWorkRequest.Builder(UpdateService.class, 1, TimeUnit.DAYS)
                                .addTag("weekly_updater_work")
                                .setConstraints(new Constraints.Builder()
                                        .setRequiredNetworkType(NetworkType.UNMETERED)
                                        .build())
                                .build());
                    } else {
                        WorkManager.getInstance().cancelAllWorkByTag("weekly_updater_work");
                    }
                    return true;
                });
            else
                updater.setVisible(false);
        }


        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case "defaultServer":
                    final AnikumiiBottomSheetDialog defaultServer = new AnikumiiBottomSheetDialog(getActivity());
                    defaultServer.serverDialog(preference.getSharedPreferences().getString("defaultServer", "Zippyshare")).setOnCheckedChangeListener((ChipGroup group, int checkedId) -> {
                        if (checkedId != -1) {
                            preference.getSharedPreferences().edit().putString("defaultServer", ((Chip) group.findViewById(checkedId)).getText().toString()).apply();
                            serverPreference.setSummary(getString(R.string.defaultServerSummary, ((Chip) group.findViewById(checkedId)).getText().toString()));

                            defaultServer.dismiss();
                        }
                    });
                    break;
                case "gridColumns":
                    final AnikumiiBottomSheetDialog columnsDialog = new AnikumiiBottomSheetDialog(getActivity());
                    final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_column, null);

                    final NumberPicker portraitNumberPicker = dialogView.findViewById(R.id.numberPickerColumnsPortrait);
                    portraitNumberPicker.setMinValue(1);
                    portraitNumberPicker.setMaxValue(Math.round((float) getResources().getDisplayMetrics().widthPixels / 300));

                    final NumberPicker landscapeNumberPicker = dialogView.findViewById(R.id.numberPickerColumnsLandscape);
                    landscapeNumberPicker.setMinValue(1);
                    landscapeNumberPicker.setMaxValue(Math.round((float) getResources().getDisplayMetrics().heightPixels / 300));

                    final MaterialButton positiveButton = dialogView.findViewById(R.id.column_positive_button);
                    positiveButton.setOnClickListener(v -> {
                        getPreferenceManager().getSharedPreferences().edit().putInt("gridColumnsPortrait", portraitNumberPicker.getValue()).apply();
                        getPreferenceManager().getSharedPreferences().edit().putInt("gridColumnsLandscape", landscapeNumberPicker.getValue()).apply();
                        findPreference("gridColumns").setSummary(getString(R.string.columnsSummary, portraitNumberPicker.getValue(), landscapeNumberPicker.getValue()));
                        reloadMain = true;
                        columnsDialog.dismiss();
                    });

                    final AppCompatButton negativeButton = dialogView.findViewById(R.id.column_negative_button);
                    negativeButton.setOnClickListener(v -> {
                        // getPreferenceManager().getSharedPreferences().edit().putInt("gridColumnsPortrait", portraitNumberPicker.getValue()).apply();
                        getPreferenceManager().getSharedPreferences().edit().remove("gridColumnsPortrait").apply();
                        getPreferenceManager().getSharedPreferences().edit().remove("gridColumnsLandscape").apply();
                        findPreference("gridColumns").setSummary(getString(R.string.columnsSummary, getPreferenceManager().getSharedPreferences().getInt("gridColumnsPortrait", Math.round((float) getResources().getDisplayMetrics().widthPixels / 400)), getPreferenceManager().getSharedPreferences().getInt("gridColumnsLandscape", Math.round((float) getResources().getDisplayMetrics().heightPixels / 400))));
                        columnsDialog.dismiss();
                        reloadMain = true;
                    });

                    columnsDialog.initialize(getString(R.string.columnsTitle), dialogView, R.color.colorPrimary);
                    break;
                case "updater_now":
                    WorkManager.getInstance().enqueue(new OneTimeWorkRequest.Builder(UpdateService.class)
                            .setInputData(new Data.Builder()
                                    .putBoolean("triggered", true)
                                    .build())
                            .build());
                    break;
            }
            return true;
        }
    }

    public static class AboutFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_about, rootKey);
        }
    }
}