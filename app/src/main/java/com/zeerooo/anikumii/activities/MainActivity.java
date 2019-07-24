package com.zeerooo.anikumii.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.MatrixCursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.widget.NestedScrollView;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager.widget.ViewPager;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.zeerooo.anikumii.Anikumii;
import com.zeerooo.anikumii.BuildConfig;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.adapters.SearchCursorAdapter;
import com.zeerooo.anikumii.adapters.ViewPagerAdapter;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiBottomSheetDialog;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiConnection;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiInputChip;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiSharedPreferences;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiUiHelper;
import com.zeerooo.anikumii.anikumiiparts.glide.GlideApp;
import com.zeerooo.anikumii.fragments.TioAnimeFragment;
import com.zeerooo.anikumii.fragments.TioHentaiFragment;
import com.zeerooo.anikumii.misc.Utils;
import com.zeerooo.anikumii.services.MyAnimeListUpdater;
import com.zeerooo.anikumii.services.NotificationService;
import com.zeerooo.anikumii.services.UpdateService;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by ZeeRooo on 05/01/18
 */

public class MainActivity extends AppCompatActivity {
    private MaterialToolbar searchToolbar = null, mToolbar;
    private MenuItem searchItem;
    private boolean isDisposed, canGoBack, isPopupVisible = true, isLoggedIn;
    private AnikumiiSharedPreferences anikumiiSharedPreferences;
    private String /*userName_str,types,*/ malUserName;
    private MatrixCursor matrixCursor;
    private SearchCursorAdapter searchCursorAdapter;
    private PublishSubject<String> publishSubject;
    private JSONArray jsonArray;
    private AnikumiiInputChip anikumiiInputChip;
    private ViewPagerAdapter viewPagerAdapter;
    private PopupWindow popupWindow;
    private TabLayout tabLayout;
    private MenuItem userMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        anikumiiSharedPreferences = new AnikumiiSharedPreferences(this);

        // Setup the toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(new TioAnimeFragment(anikumiiSharedPreferences), "Anime");
        viewPagerAdapter.addFragment(new TioHentaiFragment(anikumiiSharedPreferences), "Hentai");

        ViewPager viewPager = findViewById(R.id.viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setAdapter(viewPagerAdapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switchActions(item);
            return true;
        });
        bottomNavigationView.setOnNavigationItemReselectedListener(item -> {
            if (((GridLayoutManager) viewPagerAdapter.getCurrentFragment().anikumiiRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition() > 0 && item.getItemId() != R.id.nav_history_animes)
                viewPagerAdapter.getCurrentFragment().anikumiiRecyclerView.smoothScrollToPosition(0);
            else switchActions(item);
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        publishSubject = PublishSubject.create();

        searchToolbar();

        if (getIntent().getAction() != null && getIntent().getAction().equals("shortcut_search")) {
            searchStuff();

            searchToolbar.setVisibility(View.VISIBLE);

            searchItem.expandActionView();
        }

        if (getIntent().getDataString() != null) {
            Anikumii.dominium = Utils.matcher(getIntent().getDataString(), "(https://.*?/)");
            if (Anikumii.dominium.contains("tiohentai")) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tabLayout.selectTab(tabLayout.getTabAt(1));
                        handler.removeCallbacks(this);
                    }
                }, 100);
            }
        }

        malUserName = anikumiiSharedPreferences.getString("malUserName", getString(R.string.app_name));
        isLoggedIn = !malUserName.equals(getString(R.string.app_name));

        if (anikumiiSharedPreferences.getBoolean("firstRun", true)) {
            gridDialog();

            if (BuildConfig.VERSION_NAME.endsWith("github"))
                WorkManager.getInstance().enqueue(new PeriodicWorkRequest.Builder(UpdateService.class, 7, TimeUnit.DAYS)
                        .addTag("weekly_updater_work")
                        .setConstraints(new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.UNMETERED)
                                .build())
                        .build());

            WorkManager.getInstance().enqueue(new PeriodicWorkRequest.Builder(NotificationService.class, 3, TimeUnit.HOURS)
                    .addTag("notification_work")
                    .setConstraints(new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build());

            WorkManager.getInstance().enqueue(new PeriodicWorkRequest.Builder(MyAnimeListUpdater.class, 1, TimeUnit.DAYS)
                    .addTag("myanimelist_updater_work")
                    .setConstraints(new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build());

            anikumiiSharedPreferences.edit().putBoolean("firstRun", false).apply();
        }
    }

    private void gridDialog() {
        AnikumiiBottomSheetDialog columnsDialog = new AnikumiiBottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_column, null);

        dialogView.setBackgroundColor(getResources().getColor(R.color.celestito));

        final NumberPicker portraitNumberPicker = dialogView.findViewById(R.id.numberPickerColumnsPortrait);
        portraitNumberPicker.setMinValue(1);
        portraitNumberPicker.setMaxValue(Math.round((float) getResources().getDisplayMetrics().widthPixels / 300));

        final NumberPicker landscapeNumberPicker = dialogView.findViewById(R.id.numberPickerColumnsLandscape);
        landscapeNumberPicker.setMinValue(1);
        landscapeNumberPicker.setMaxValue(Math.round((float) getResources().getDisplayMetrics().heightPixels / 300));

        Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_main_root_view), "Recuerda que podrás cambiar la relación más adelante en ajustes > avanzado", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(android.R.string.ok, view -> snackbar.dismiss());

        MaterialButton positiveButton = dialogView.findViewById(R.id.column_positive_button);
        positiveButton.setOnClickListener(v -> {
            anikumiiSharedPreferences.edit().putInt("gridColumnsPortrait", portraitNumberPicker.getValue()).apply();
            anikumiiSharedPreferences.edit().putInt("gridColumnsLandscape", landscapeNumberPicker.getValue()).apply();

            viewPagerAdapter.anikumiiMainFragment.refreshGridLayoutManager(portraitNumberPicker.getValue());

            columnsDialog.dismiss();
            snackbar.show();
        });

        AppCompatButton negativeButton = dialogView.findViewById(R.id.column_negative_button);
        negativeButton.setText(android.R.string.cancel);
        negativeButton.setOnClickListener(v -> {
            anikumiiSharedPreferences.edit().remove("gridColumnsPortrait").apply();
            anikumiiSharedPreferences.edit().remove("gridColumnsLandscape").apply();
            columnsDialog.dismiss();
            snackbar.show();
        });

        columnsDialog.initialize("Configura las columnas según la orientación", dialogView, R.color.celestito);
        columnsDialog.findViewById(R.id.dialogTitleTextView).setBackgroundColor(getResources().getColor(R.color.celestito));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (searchToolbar.hasExpandedActionView())
            searchItem.collapseActionView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewPagerAdapter.getCurrentFragment().anikumiiRecyclerView.exit();
    }

    @Override
    public void onBackPressed() {
        if (searchToolbar.hasExpandedActionView()) {
            searchItem.collapseActionView();
        } else if (canGoBack) {
            viewPagerAdapter.getCurrentFragment().reactiveRecyclerView(getString(R.string.latest_episodes), Anikumii.dominium, "article.episode", (byte) 20);
            canGoBack = false;
        } else
            super.onBackPressed();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getBooleanExtra("reloadMain", false)) {
            malUserName = anikumiiSharedPreferences.getString("malUserName", getString(R.string.app_name));
            isLoggedIn = !malUserName.equals(getString(R.string.app_name));
            userStuff();

            onConfigurationChanged(getResources().getConfiguration());
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && (isInMultiWindowMode()/*|| wasMultiWindow*/)) {
            // wasMultiWindow = isInMultiWindowMode();
            userStuff();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.searchAnime:
                searchStuff();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    circleReveal(true);
                else
                    searchToolbar.setVisibility(View.VISIBLE);

                searchItem.expandActionView();
                break;
            case R.id.userAvatar:
                if (!isPopupVisible) {
                    isPopupVisible = true;
                    popupWindow.dismiss();
                    break;
                } else
                    isPopupVisible = false;

                View rootView = getLayoutInflater().inflate(R.layout.overflow_menu, null);

                popupWindow = new PopupWindow(rootView, mToolbar.getWidth() / 2, ListPopupWindow.WRAP_CONTENT);
                popupWindow.showAsDropDown(mToolbar, mToolbar.getWidth(), 0);
                popupWindow.setOnDismissListener(() -> isPopupVisible = true);
                viewPagerAdapter.getCurrentFragment().getView().setOnTouchListener((v, event) -> {
                    v.setOnTouchListener(null);
                    popupWindow.dismiss();
                    return false;
                });

                ((TextView) rootView.findViewById(R.id.overflow_tv_user_name)).setText(malUserName);

                Button settingsButton = rootView.findViewById(R.id.overflow_btn_settings);
                settingsButton.setOnClickListener(v -> {
                    popupWindow.dismiss();

                    startActivity(new Intent(this, SettingsActivity.class));
                });

                Button loginButton = rootView.findViewById(R.id.overflow_btn_login);
                if (!isLoggedIn) {
                    loginButton.setVisibility(View.VISIBLE);
                    loginButton.setOnClickListener(v -> {
                        popupWindow.dismiss();

                        new AnikumiiBottomSheetDialog(this).loginDialog();
                    });
                } else
                    loginButton.setVisibility(View.GONE);

                ImageButton shortcutButton = rootView.findViewById(R.id.overflow_btn_shortcut);
                TooltipCompat.setTooltipText(shortcutButton, getString(R.string.overflow_shortcut_description));
                shortcutButton.setEnabled(isLoggedIn);
                AnikumiiUiHelper.transparentBackground(shortcutButton);
                shortcutButton.setOnClickListener(v -> {
                    popupWindow.dismiss();

                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://myanimelist.net/profile/" + malUserName)));
                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_toolbar_menu, menu);

        userMenuItem = menu.findItem(R.id.userAvatar);

        userStuff();

        return true;
    }

    private void switchActions(MenuItem item) {
        canGoBack = true;

        switch (item.getItemId()) {
            case R.id.nav_home:
                viewPagerAdapter.getCurrentFragment().reactiveRecyclerView(getString(R.string.latest_episodes), Anikumii.dominium + "/", "article.episode", (byte) 20);
                break;
            case R.id.nav_all_anime:
                viewPagerAdapter.getCurrentFragment().reactiveRecyclerView(getString(R.string.nav_all_anime), Anikumii.dominium + "/directorio", "article.anime", (byte) 19);
                break;
            case R.id.nav_live_animes:
                viewPagerAdapter.getCurrentFragment().reactiveRecyclerView(getString(R.string.nav_live_animes), Anikumii.dominium + "/directorio?estado=emision", "article.anime", (byte) 19);
                break;
            case R.id.nav_history_animes:
                startActivity(new Intent(this, AnimeActivity.class).putExtra("title", "Historial"));
                break;
        }
    }

    @SuppressWarnings("NewApi")
    private void circleReveal(final boolean isShow) {
        int cy = searchToolbar.getHeight() / 2;

        Animator anim;
        if (isShow)
            anim = ViewAnimationUtils.createCircularReveal(searchToolbar, searchToolbar.getWidth(), cy, 0, searchToolbar.getWidth());
        else
            anim = ViewAnimationUtils.createCircularReveal(searchToolbar, searchToolbar.getWidth(), cy, searchToolbar.getWidth(), 0);

        anim.setDuration(220);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isShow) {
                    super.onAnimationEnd(animation);
                    searchToolbar.setVisibility(View.GONE);
                }
            }
        });

        // make the view visible and start the animation
        if (isShow)
            searchToolbar.setVisibility(View.VISIBLE);

        anim.start();
    }

    // Thanks to Jaison Fernando for the great tutorial.
    // http://droidmentor.com/searchview-animation-like-whatsapp/
    private void searchToolbar() {
        searchToolbar = findViewById(R.id.searchtoolbar);
        searchToolbar.inflateMenu(R.menu.menu_search);
        Menu search_menu = searchToolbar.getMenu();

        searchItem = search_menu.findItem(R.id.action_filter_search);

        searchCursorAdapter = new SearchCursorAdapter(this, R.layout.item_search_suggerence, null, new String[]{BaseColumns._ID, "animeTitle", "animeType", "animeId"}, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        final SearchView searchView = (SearchView) search_menu.findItem(R.id.action_filter_search).getActionView();
        searchView.setSuggestionsAdapter(searchCursorAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchItem.collapseActionView();
                viewPagerAdapter.getCurrentFragment().reactiveRecyclerView(query, Anikumii.dominium + "/directorio?q=" + query, "article.anime", (byte) 19);

                canGoBack = true;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                publishSubject.onNext(newText);
                return true;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                String type;
                if (Anikumii.dominium.startsWith("https://tioanime.com"))
                    type = "https://tioanime.com/anime/";
                else
                    type = "https://tiohentai.com/hentai/";

                startActivity(new Intent(MainActivity.this, EpisodesActivity.class).putExtra("animeUrl", type + matrixCursor.getString(4)));

                searchItem.collapseActionView();
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    circleReveal(false);
                else
                    searchToolbar.setVisibility(View.INVISIBLE);

                publishSubject.onComplete();
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }
        });

        search_menu.findItem(R.id.action_filter).setOnMenuItemClickListener(item -> filterDialog());
    }

    private void searchStuff() {
        if (isDisposed)
            publishSubject = PublishSubject.create();

        AnikumiiConnection anikumiiConnection = new AnikumiiConnection();

        publishSubject
                .debounce(300, TimeUnit.MILLISECONDS)
                .filter(s -> !s.isEmpty())
                .distinctUntilChanged()
                .observeOn(Schedulers.io())
                .doOnNext(query -> {
                    jsonArray = new JSONArray(anikumiiConnection.getStringResponse("POST", Anikumii.dominium + "/api/search", "value=" + query));

                    matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID, "animeTitle", "animeType", "animeId", "animeUrl"});

                    for (byte a = 0; a < jsonArray.length(); a++)
                        matrixCursor.addRow(new Object[]{1, jsonArray.getJSONObject(a).getString("title"), Utils.getTypeFromNumber(jsonArray.getJSONObject(a).getString("type")), jsonArray.getJSONObject(a).getString("id"), jsonArray.getJSONObject(a).getString("slug")});
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(String s) {
                        searchCursorAdapter.changeCursor(matrixCursor);
                    }

                    @Override
                    public void onError(Throwable e) {
                        AnikumiiUiHelper.errorSnackbar(findViewById(R.id.activity_main_root_view), Snackbar.LENGTH_LONG, e.toString(), null).show();
                    }

                    @Override
                    public void onComplete() {
                        if (matrixCursor != null && !matrixCursor.isClosed())
                            matrixCursor.close();
                        isDisposed = true;
                    }
                });
    }

    private boolean filterDialog() {
        AnikumiiBottomSheetDialog filterDialog = new AnikumiiBottomSheetDialog(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.bottom_sheet_filter, null);

        String[] preloaded;
        if (Anikumii.dominium.startsWith("https://tioanime.com"))
            preloaded = new String[]{"Acción", "Artes Marciales", "Aventuras", "Carreras", "Ciencia Ficción", "Comedia", "Demencia", "Demonios", "Deportes", "Drama", "Ecchi", "Escolares", "Espacial", "Fantasía", "Harem", "Historico", "Infantil", "Josei", "Juegos", "Magia", "Mecha", "Militar", "Misterio", "Música", "Parodia", "Policía", "Psicológico", "Recuentos de la vida", "Romance", "Samurai", "Seinen", "Shoujo", "Shounen", "Sobrenatural", "Superpoderes", "Suspenso", "Terror", "Vampiros", "Yaoi", "Yuri"}; //40;
        else
            preloaded = new String[]{"Ahegao", "Anal", "Bestialidad", "Bondage", "Chikan", "Comedia", "Enfermeras", "Escolar", "Fantasia", "Futanari", "Gangbang", "Harem", "Incesto", "Lolicon", "Maids", "Milf", "Netorare", "Masturbacion", "Romance", "Shota", "Tentaculos", "Tetonas", "Violacion", "Virgenes", "Yaoi", "Yuri", "Demonios", "Felacion"}; //28

        anikumiiInputChip = new AnikumiiInputChip(dialogView, preloaded);

        ImageButton allGenres = dialogView.findViewById(R.id.allGenres);
        TooltipCompat.setTooltipText(allGenres, "Todos los géneros");
        AnikumiiUiHelper.transparentBackground(allGenres);
        allGenres.setOnClickListener(v -> {
            AnikumiiBottomSheetDialog genresDialog = new AnikumiiBottomSheetDialog(this);
            NestedScrollView genresScroll = new NestedScrollView(this);

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setPadding(10, 10, 10, 10);

            ChipGroup chipGroup = new ChipGroup(this);
            chipGroup.setSingleSelection(true); // por ahora

            for (byte checkboxesCount = 0; checkboxesCount < 39; checkboxesCount++) {
                Chip genre = (Chip) inflater.inflate(R.layout.chip_filter, null);

                genre.setText(preloaded[checkboxesCount]);
                genre.setTag(preloaded[checkboxesCount]);
                genre.setChecked(anikumiiInputChip.contains(genre.getText()));
                genre.setOnClickListener(view -> {
                    if (anikumiiInputChip.contains(view.getTag())) {
                        anikumiiInputChip.remove(view.getTag());
                    } else {
                        anikumiiInputChip.add(view.getTag().toString());
                    }
                });
                chipGroup.addView(genre);
            }
            linearLayout.addView(chipGroup, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            MaterialButton materialButton = new MaterialButton(MainActivity.this);
            materialButton.setText(getText(android.R.string.ok));
            materialButton.setTextColor(getResources().getColor(R.color.celestito));
            materialButton.setStrokeWidth(1);
            materialButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.celestito)));
            materialButton.setOnClickListener(view -> {
                ((ChipGroup) dialogView.findViewById(R.id.tagsChipGroup)).removeAllViews();

                for (byte count = 0; count < anikumiiInputChip.size(); count++)
                    anikumiiInputChip.addRemovableChip(anikumiiInputChip.get(count), dialogView, false);

                genresDialog.dismiss();
            });
            linearLayout.addView(materialButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            genresScroll.addView(linearLayout);

            genresDialog.initialize("Géneros", genresScroll, R.color.colorPrimary);
        });

      /*  final Spinner type = dialogView.findViewById(R.id.typeChooser);
        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        types = "Anime";
                        break;
                    case 1:
                        types = "Película";
                        break;
                    case 2:
                        types = "Especial";
                        break;
                    case 3:
                        types = "OVA";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });*/

        StringBuilder genresBuilder = new StringBuilder();

        MaterialButton positiveButton = dialogView.findViewById(R.id.filter_positive_button);
        positiveButton.setOnClickListener(view -> {
            for (byte count = 0; count < anikumiiInputChip.size(); count++) {
                genresBuilder.append("?genero=").append(anikumiiInputChip.get(count).replace(" ", "-"));
            }

            viewPagerAdapter.getCurrentFragment().reactiveRecyclerView("Filtrados", Anikumii.dominium + "/directorio" + genresBuilder, "article.anime", (byte) 19);
            anikumiiInputChip.clear();
            genresBuilder.delete(0, genresBuilder.length());
        });

        AppCompatButton negativeButton = dialogView.findViewById(R.id.filter_negative_button);
        negativeButton.setOnClickListener(view -> {
            anikumiiInputChip.clear();
            filterDialog.dismiss();
        });

        filterDialog.initialize("Filtros", dialogView, R.color.colorPrimary);

        return true;
    }

    private void userStuff() {
        if (isLoggedIn)
            GlideApp.with(MainActivity.this).asDrawable().load(anikumiiSharedPreferences.getString("malUserAvatar", null)).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).circleCrop().into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    userMenuItem.setIcon(resource);
                    userMenuItem.setTitle(malUserName);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                }
            });
    }
}