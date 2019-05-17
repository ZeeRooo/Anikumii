package com.zeerooo.anikumii2.activities;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class SettingsActivity extends AppCompatActivity {
    public static boolean loadHeaderInfo, reloadMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        if (loadHeaderInfo)
            startActivity(new Intent(this, MainActivity.class).putExtra("loadHeaderInfo", true));
        if (reloadMain)
            startActivity(new Intent(this, MainActivity.class));
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
        private TextView aflvName, malName;
        private ImageView aflvAvatarView, malAvatarView;
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
            findPreference("nav_malAccount").setOnPreferenceClickListener(this);
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

                String aflvAvatar = getPreferenceManager().getSharedPreferences().getString("UserAvatar", null);
                aflvAvatarView = getActivity().findViewById(R.id.pref_aflvAvatar);
                if (aflvAvatar != null)
                    GlideApp.with(getActivity()).load(aflvAvatar).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop()).into(aflvAvatarView);
                aflvName = getActivity().findViewById(R.id.pref_aflvName);
                aflvName.setText(getPreferenceManager().getSharedPreferences().getString("userName", getString(R.string.warning_not_logged, "AnimeFLV")));
                sync = false;
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
                    AnikumiiDialog anikumiiDialog = new AnikumiiDialog(getActivity());

                    LinearLayout linearLayout = new LinearLayout(getActivity());
                    linearLayout.setOrientation(LinearLayout.VERTICAL);

                    final CheckBox mal = new CheckBox(getActivity());
                    mal.setText(getString(R.string.myanimelist));
                    mal.setTextSize(16);
                    mal.setPadding(15, 25, 0, 15);
                    linearLayout.addView(mal);

                    final CheckBox aflv = new CheckBox(getActivity());
                    aflv.setText(getString(R.string.animeflv));
                    aflv.setTextSize(16);
                    aflv.setPadding(15, 25, 0, 15);
                    linearLayout.addView(aflv);

                    anikumiiDialog.setButton(DialogInterface.BUTTON_POSITIVE, getText(android.R.string.ok), (DialogInterface dialogInterface, int i) -> {
                        if (mal.isChecked()) {
                            getPreferenceManager().getSharedPreferences().edit().remove("MALuserName").apply();
                            getPreferenceManager().getSharedPreferences().edit().remove("MALuserAvatar").apply();
                            getPreferenceManager().getSharedPreferences().edit().remove("mal").apply();
                            malName.setText(getString(R.string.app_name));
                            malAvatarView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_launcher_foreground));
                            loadHeaderInfo = true;
                        }

                        if (aflv.isChecked()) {
                            getPreferenceManager().getSharedPreferences().edit().remove("UserCookie").apply();
                            getPreferenceManager().getSharedPreferences().edit().remove("UserAvatar").apply();
                            getPreferenceManager().getSharedPreferences().edit().remove("userName").apply();
                            aflvName.setText(getString(R.string.app_name));
                            aflvAvatarView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_launcher_foreground));
                            loadHeaderInfo = true;
                        }
                    });
                    anikumiiDialog.addCancelButton();
                    anikumiiDialog.initialize(getString(R.string.log_out), linearLayout);
                    break;
                case "nav_malAccount":
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
            findPreference("enableNotif").setOnPreferenceClickListener(this);
            findPreference("headsUp").setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case "headsUp":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        startActivity(new Intent().setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", getActivity().getPackageName(), null)));
                    break;
                case "enableNotif":
                    notificationStuff(preference.getSharedPreferences().getString("notifInterval", "3600000"));
                    break;
            }
            return true;
        }

        private void notificationStuff(String notifInterval) {
            workManager.cancelAllWork();

            workManager.enqueue(new PeriodicWorkRequest.Builder(NotificationService.class, Integer.valueOf(notifInterval), TimeUnit.MILLISECONDS)
                    .setConstraints(new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .setInputData(new Data.Builder()
                            .putBoolean("updateDb", false)
                            .putBoolean("firstRun", false)
                            .build())
                    .build());
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference.getKey().equals("notifInterval"))
                notificationStuff(newValue.toString());
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

            Preference updateApp;
            updateApp = findPreference("pref_checkUpdates");
            updateApp.setOnPreferenceClickListener(this);
            updateApp.setSummary("Versión actual: " + BuildConfig.VERSION_NAME);

            findPreference("defaultServer").setOnPreferenceClickListener(this);

            Preference updateDb = findPreference("pref_updateDB");
            updateDb.setOnPreferenceClickListener(this);
            updateDb.setSummary(getPreferenceManager().getSharedPreferences().getString("lastDbUpdate", "Nunca actualizada"));

            findPreference("defaultServer").setSummary(getString(R.string.defaultServerSummary, getPreferenceManager().getSharedPreferences().getString("defaultServer", "Zippyshare")));

          //findPreference("generateTxtDB").setOnPreferenceClickListener(this);
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
                case "pref_checkUpdates":
                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://drive.google.com/open?id=19Ry_KW8SDQz7RiQFjyBc8BMq7lsDxFn3")));
                    break;
                case "defaultServer":
                    AnikumiiDialog defaultServer = new AnikumiiDialog(getActivity());

                    ChipGroup chipGroup = new ChipGroup(getActivity());
                    chipGroup.setPadding(30, 30, 30, 30);
                    chipGroup.setSingleSelection(true);

                    Chip streamangoChip = new Chip(getActivity());
                    streamangoChip.setText(getString(R.string.streamango));
                    streamangoChip.setCheckable(true);
                    streamangoChip.setTextColor(Color.BLACK);
                    streamangoChip.setChipBackgroundColor(ColorStateList.valueOf(Color.WHITE));
                    streamangoChip.setChecked(preference.getSharedPreferences().getString("defaultServer", "Zippyshare").equals("Streamango"));
                    chipGroup.addView(streamangoChip);

                    Chip zippyshareChip = new Chip(getActivity());
                    zippyshareChip.setText(getString(R.string.zippyshare));
                    zippyshareChip.setCheckable(true);
                    zippyshareChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#fffdd1")));
                    zippyshareChip.setTextColor(Color.BLACK);
                    zippyshareChip.setChecked(preference.getSharedPreferences().getString("defaultServer", "Zippyshare").equals("Zippyshare"));
                    chipGroup.addView(zippyshareChip);

                    Chip mediafireChip = new Chip(getActivity());
                    mediafireChip.setText(getString(R.string.mediafire));
                    mediafireChip.setCheckable(true);
                    mediafireChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#0077ff")));
                    mediafireChip.setTextColor(Color.BLACK);
                    mediafireChip.setChecked(preference.getSharedPreferences().getString("defaultServer", "Zippyshare").equals("MediaFire"));
                    chipGroup.addView(mediafireChip);

                    chipGroup.setOnCheckedChangeListener((ChipGroup group, int checkedId) -> {
                        if (checkedId != -1) {
                            preference.getSharedPreferences().edit().putString("defaultServer", (String) ((Chip) group.findViewById(checkedId)).getText()).apply();
                            findPreference("defaultServer").setSummary(getString(R.string.defaultServerSummary, (String) ((Chip) group.findViewById(checkedId)).getText()));

                            defaultServer.dismiss();
                        }
                    });

                    defaultServer.initialize(getString(R.string.changeServer), chipGroup);
                    break;
                case "pref_updateDB":
                    WorkManager.getInstance().enqueue(new OneTimeWorkRequest.Builder(NotificationService.class)
                            .setConstraints(new Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build())
                            .setInputData(new Data.Builder()
                                    .putBoolean("updateDb", true)
                                    .build())
                            .build());
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
             /*   case "generateTxtDB":
                    System.out.println("Generating");
                    try {
                        File directory = new File(Environment.getExternalStorageDirectory() + "/Anikumii!!/txt/");
                        if (!directory.exists())
                            directory.mkdirs();

                        File file = new File(directory + "/database.anikumii2");
                        if (!file.exists())
                            file.createNewFile();

                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), "UTF-8"));

                        DataBaseHelper db = new DataBaseHelper(getActivity());
                        Cursor cursor = db.getReadableDatabase().rawQuery("SELECT * FROM AnimesDB ORDER BY TITLE", null);
                        writer.append("<!-- Fuente: AnimeFLV.net ¡Muchas gracias! // Source: AnimeFLV.net -->\n");
                        while (cursor.moveToNext()) {
                            writer.append("<a href=\"").append(cursor.getString(3)).append("\"><p class=\"id\">").append(cursor.getString(5)).append("</p><p class=\"Title\">").append(cursor.getString(1)).append("</p><p class=\"Type\">").append(cursor.getString(2)).append("</p><p class=\"Genres\">").append(cursor.getString(4).replace(";", ",")).append("</p></a>\n");
                            writer.flush();
                        }

                        writer.close();
                        cursor.close();
                        db.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println("Finished");
                    }
                    break;*/
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
