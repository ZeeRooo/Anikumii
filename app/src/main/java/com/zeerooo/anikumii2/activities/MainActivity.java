package com.zeerooo.anikumii2.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.zeerooo.anikumii2.Anikumii;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.adapters.SearchCursorAdapter;
import com.zeerooo.anikumii2.adapters.ViewPagerAdapter;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiConnection;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiDialog;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiInputChip;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiSharedPreferences;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiUiHelper;
import com.zeerooo.anikumii2.anikumiiparts.glide.GlideApp;
import com.zeerooo.anikumii2.fragments.TioAnimeFragment;
import com.zeerooo.anikumii2.fragments.TioHentaiFragment;
import com.zeerooo.anikumii2.misc.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final String[] preloadGenres = {"Acción", "Artes Marciales", "Aventuras", "Carreras", "Ciencia Ficción", "Comedia", "Demencia", "Demonios", "Deportes", "Drama", "Ecchi", "Escolares", "Espacial", "Fantasía", "Harem", "Historico", "Infantil", "Josei", "Juegos", "Magia", "Mecha", "Militar", "Misterio", "Música", "Parodia", "Policía", "Psicológico", "Recuentos de la vida", "Romance", "Samurai", "Seinen", "Shoujo", "Shounen", "Sobrenatural", "Superpoderes", "Suspenso", "Terror", "Vampiros", "Yaoi", "Yuri"}; //40
    private Toolbar searchToolbar = null;
    private MenuItem searchItem;
    private boolean isDisposed;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private AnikumiiSharedPreferences mPreferences;
    private String userName_str, types;
    private MatrixCursor matrixCursor;
    private SearchCursorAdapter searchCursorAdapter;
    private PublishSubject<String> publishSubject;
    private JSONArray jsonArray;
    private boolean canGoBack;
    private FloatingActionButton filterFAB;
    private AnikumiiInputChip anikumiiInputChip;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = new AnikumiiSharedPreferences(this);

        // Setup the toolbar
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 1);
        viewPagerAdapter.addFragment(new TioAnimeFragment(), "Anime");
        viewPagerAdapter.addFragment(new TioHentaiFragment(), "Hentai");

        ViewPager viewPager = findViewById(R.id.episodes_viewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setAdapter(viewPagerAdapter);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_home);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        publishSubject = PublishSubject.create();

        searchToolbar();

        filterFAB = findViewById(R.id.filterFAB);
        filterFAB.setOnClickListener((View v) -> {
            // TODO: Saca esto
            Snackbar snackbar = AnikumiiUiHelper.Snackbar(drawer, "Actualmente sólo se puede seleccionar un género; y los tipos de anime tampoco funcionan. Esto se debe a restricciones del sitio web.", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Ok", (View view) -> {
                snackbar.dismiss();
                filterDialog();
            });
            snackbar.show();
        });

        navigationView.getMenu().findItem(R.id.nav_log_in).setVisible(mPreferences.getString("MALuserAvatar", null) == null);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                viewPagerAdapter.getCurrentFragment().reactiveRecyclerView(getString(R.string.latest_episodes), Anikumii.dominium, "article.episode", (byte) 20);
                filterFAB.hide();
                canGoBack = true;
                break;
            case R.id.nav_all_anime:
                viewPagerAdapter.getCurrentFragment().reactiveRecyclerView(getString(R.string.nav_all_anime), Anikumii.dominium + "directorio", "article.anime", (byte) 19);
                filterFAB.show();
                canGoBack = true;
                break;
            case R.id.nav_live_animes:
                viewPagerAdapter.getCurrentFragment().reactiveRecyclerView(getString(R.string.nav_live_animes), Anikumii.dominium + "directorio?estado=emision", "article.anime", (byte) 19);
                canGoBack = true;
                filterFAB.hide();
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_log_in:
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else if (searchToolbar.hasExpandedActionView()) {
            searchItem.collapseActionView();
        } else if (canGoBack) {
            viewPagerAdapter.getCurrentFragment().reactiveRecyclerView(getString(R.string.latest_episodes), Anikumii.dominium, "article.episode", (byte) 20);
            canGoBack = false;
            filterFAB.hide();
        } else
            super.onBackPressed();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("loadHeaderInfo", false))
            setHeaderInfo();

        if (intent.getBooleanExtra("reloadMain", false))
            onConfigurationChanged(getResources().getConfiguration());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.searchAnime) {
            filterFAB.hide();

            searchStuff();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                circleReveal(true);
            else
                searchToolbar.setVisibility(View.VISIBLE);

            searchItem.expandActionView();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mainactivity_menu, menu);

        setHeaderInfo();
        return true;
    }

    @SuppressWarnings("NewApi")
    private void circleReveal(final boolean isShow) {
        final View v = findViewById(R.id.searchtoolbar);

        int cy = v.getHeight() / 2;

        Animator anim;
        if (isShow)
            anim = ViewAnimationUtils.createCircularReveal(v, v.getWidth(), cy, 0, v.getWidth());
        else
            anim = ViewAnimationUtils.createCircularReveal(v, v.getWidth(), cy, v.getWidth(), 0);

        anim.setDuration(220);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isShow) {
                    super.onAnimationEnd(animation);
                    v.setVisibility(View.GONE);
                }
            }
        });

        // make the view visible and start the animation
        if (isShow)
            v.setVisibility(View.VISIBLE);

        anim.start();
    }

    private void setHeaderInfo() {
        String userAvatar_str;

        userAvatar_str = mPreferences.getString("MALuserAvatar", "imageNotFound");
        userName_str = mPreferences.getString("MALuserName", "user_nameNotFound");

        navigationView.getMenu().findItem(R.id.nav_log_in).setVisible(userName_str.equals("user_nameNotFound"));

        ImageView userAvatar = findViewById(R.id.user_avatar);
        if (userAvatar != null)
            if (!userAvatar_str.equals("imageNotFound")) {
                userAvatar.setOnClickListener((View view) -> {
                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://myanimelist.net/profile/" + userName_str)));

                });
                GlideApp.with(MainActivity.this).load(userAvatar_str).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop()).into(userAvatar);
            } else
                GlideApp.with(MainActivity.this).load(getResources().getDrawable(R.mipmap.ic_launcher)).into(userAvatar);

        if (!userName_str.equals("user_nameNotFound")) {
            TextView userName = findViewById(R.id.user_name);
            userName.setText(userName_str);
        }
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

                viewPagerAdapter.getCurrentFragment().reactiveRecyclerView(query, Anikumii.dominium + "directorio?q=" + query, "article.anime", (byte) 19);

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
                if (Anikumii.dominium.startsWith("https://tioanime.com/"))
                    type = "/anime/";
                else
                    type = "/hentai/";

                startActivity(new Intent(MainActivity.this, EpisodesActivity.class).putExtra("animeUrl", Anikumii.dominium + type + matrixCursor.getString(4)));

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
    }

    private void searchStuff() {
        if (isDisposed)
            publishSubject = PublishSubject.create();

        AnikumiiConnection anikumiiConnection = new AnikumiiConnection();


        publishSubject
                .debounce(300, TimeUnit.MILLISECONDS)
                .filter((String s) -> !s.isEmpty())
                .distinctUntilChanged()
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String query) throws IOException, JSONException {
                        jsonArray = (JSONArray) new JSONTokener(anikumiiConnection.getStringResponse("POST", Anikumii.dominium + "/api/search", "value=" + query)).nextValue();

                        matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID, "animeTitle", "animeType", "animeId", "animeUrl"});

                        for (byte a = 0; a < jsonArray.length(); a++)
                            matrixCursor.addRow(new Object[]{1, jsonArray.getJSONObject(a).getString("title"), Utils.getTypeFromNumber(jsonArray.getJSONObject(a).getString("type")), jsonArray.getJSONObject(a).getString("id"), jsonArray.getJSONObject(a).getString("slug")});
                    }
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
                        AnikumiiUiHelper.Snackbar(drawer, getString(R.string.rxerror), Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete() {
                        if (matrixCursor != null && !matrixCursor.isClosed())
                            matrixCursor.close();
                        isDisposed = true;
                    }
                });
    }

    private void filterDialog() {
        AnikumiiDialog filterDialog = new AnikumiiDialog(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_filter, null);

        anikumiiInputChip = new AnikumiiInputChip(dialogView, preloadGenres);

        ImageButton allGenres = dialogView.findViewById(R.id.allGenres);
        AnikumiiUiHelper.transparentBackground(allGenres);
        allGenres.setOnClickListener((View v) -> {
            AnikumiiDialog genresDialog = new AnikumiiDialog(this);
            NestedScrollView genresScroll = new NestedScrollView(this);
            ChipGroup chipGroup = new ChipGroup(this);
            chipGroup.setSingleSelection(true); // por ahora
            chipGroup.setPadding(10, 10, 10, 10);

            for (byte checkboxesCount = 0; checkboxesCount < 39; checkboxesCount++) {
                Chip genre = (Chip) inflater.inflate(R.layout.chip_filter, null);

                genre.setText(preloadGenres[checkboxesCount]);
                genre.setTag(preloadGenres[checkboxesCount]);
                genre.setChecked(anikumiiInputChip.contains(genre.getText()));
                genre.setOnClickListener((View view) -> {
                    if (anikumiiInputChip.contains(view.getTag())) {
                        anikumiiInputChip.remove(view.getTag());
                    } else {
                        anikumiiInputChip.add(view.getTag().toString());
                    }
                });
                chipGroup.addView(genre);
            }

            genresScroll.addView(chipGroup);

            genresDialog.addCancelButton();
            genresDialog.setButton(DialogInterface.BUTTON_POSITIVE, getText(android.R.string.ok), (DialogInterface dialogInterface, int i) -> {
                ((ChipGroup) dialogView.findViewById(R.id.tagsChipGroup)).removeAllViews();

                for (byte count = 0; count < anikumiiInputChip.size(); count++)
                    anikumiiInputChip.addRemovableChip(anikumiiInputChip.get(count), dialogView, false);
            });
            genresDialog.initialize(null, genresScroll);
        });

        final Spinner type = dialogView.findViewById(R.id.typeChooser);
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
        });

        StringBuilder genresBuilder = new StringBuilder();

        filterDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Filtrar", (DialogInterface dialogInterface, int i) -> {
            for (byte count = 0; count < anikumiiInputChip.size(); count++) {
                genresBuilder.append("?genero=").append(anikumiiInputChip.get(count).replace(" ", "-"));
            }

            viewPagerAdapter.getCurrentFragment().reactiveRecyclerView("Filtrados", Anikumii.dominium + "directorio" + genresBuilder, "article.anime", (byte) 19);
            anikumiiInputChip.clear();
        });

        filterDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getText(android.R.string.cancel), (DialogInterface dialogInterface, int i) -> {
            anikumiiInputChip.clear();
            filterDialog.dismiss();
        });

        filterDialog.initialize("Filtros", dialogView);
    }
}