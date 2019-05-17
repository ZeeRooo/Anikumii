package com.zeerooo.anikumii2.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.MatrixCursor;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.zeerooo.anikumii2.Anikumii;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.adapters.AdapterAnimes;
import com.zeerooo.anikumii2.adapters.SearchCursorAdapter;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiDialog;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiInputChip;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiRecyclerView;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiSharedPreferences;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiUiHelper;
import com.zeerooo.anikumii2.anikumiiparts.glide.GlideApp;
import com.zeerooo.anikumii2.misc.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
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
    private AnikumiiRecyclerView anikumiiRecyclerView;
    private boolean canGoBack, isDisposed;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private AnikumiiSharedPreferences mPreferences;
    private String userName_str, userCookie, types;
    private MatrixCursor matrixCursor;
    private FloatingActionButton filterFAB;
    private SearchCursorAdapter searchCursorAdapter;
    private GridLayoutManager gridLayoutManager;
    private AnikumiiInputChip anikumiiInputChip;
    private PublishSubject<String> publishSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = new AnikumiiSharedPreferences(this);

        // Setup the toolbar
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setToolbarTitle(getString(R.string.latest_episodes));

        anikumiiRecyclerView = findViewById(R.id.new_animes_recycler_view);
        anikumiiRecyclerView.setAdapter(new AdapterAnimes(this, false));
        anikumiiRecyclerView.setElementClass(".episodes > li > article");
        anikumiiRecyclerView.setToLoad("https://tioanime.com/");
        anikumiiRecyclerView.setDynamicListener((byte) 20);
        anikumiiRecyclerView.setItemAnimator(new DefaultItemAnimator());
        anikumiiRecyclerView.setHasFixedSize(true);

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
            Snackbar snackbar = AnikumiiUiHelper.Snackbar(anikumiiRecyclerView, "Actualmente sólo se puede seleccionar un género; y los tipos de anime tampoco funcionan. Esto se debe a restricciones del sitio web.", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Ok", (View view) -> {
                snackbar.dismiss();
                filterDialog();
            });
            snackbar.show();
        });

        userCookie = ((Anikumii) getApplication()).getUserCookie();//"f;f;f;f;f;f"

        if (!userCookie.equals("f;f;f") || mPreferences.getString("MALuserAvatar", null) != null)
            navigationView.getMenu().findItem(R.id.nav_log_in).setVisible(false);

        gridLayoutManager = new GridLayoutManager(this, mPreferences.getInt("gridColumnsPortrait", Math.round((float) getResources().getDisplayMetrics().widthPixels / 300)));
        anikumiiRecyclerView.setLayoutManager(gridLayoutManager);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            gridLayoutManager.setSpanCount(mPreferences.getInt("gridColumnsLandscape", Math.round((float) getResources().getDisplayMetrics().heightPixels / 300)));
        else
            gridLayoutManager.setSpanCount(mPreferences.getInt("gridColumnsPortrait", Math.round((float) getResources().getDisplayMetrics().widthPixels / 300)));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (searchToolbar.hasExpandedActionView())
            searchItem.collapseActionView();

        anikumiiRecyclerView.exit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                reactiveRecyclerView(getString(R.string.latest_episodes), "https://tioanime.com/", ".episodes > li > article", (byte) 20);
                filterFAB.hide();
                break;
            case R.id.nav_all_anime:
                reactiveRecyclerView(getString(R.string.nav_all_anime), "https://tioanime.com/directorio", "article.anime", (byte) 19);
                filterFAB.show();
                break;
            case R.id.nav_live_animes:
                reactiveRecyclerView(getString(R.string.nav_live_animes), "https://tioanime.com/directorio?estado=emision", "article.anime", (byte) 19);
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
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else if (searchToolbar.hasExpandedActionView()) {
            publishSubject.onComplete();
            searchItem.collapseActionView();
        } else if (canGoBack) {
            reactiveRecyclerView(getString(R.string.latest_episodes), "https://tioanime.com/", ".episodes > li > article", (byte) 20);
            canGoBack = false;
            filterFAB.hide();
        } else
            super.onBackPressed();
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);

        if (intent.getBooleanExtra("loadHeaderInfo", false))
            setHeaderInfo(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.searchAnime) {
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

        setHeaderInfo(false);
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

    private void setToolbarTitle(String title) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
    }

    private void filterDialog() {
        AnikumiiDialog filterDialog = new AnikumiiDialog(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_filter, null);

        anikumiiInputChip = new AnikumiiInputChip(dialogView, preloadGenres);

        ImageButton allGenres = dialogView.findViewById(R.id.allGenres);
        AnikumiiUiHelper.transparentBackground(allGenres);
        allGenres.setOnClickListener((View v) -> {
            AnikumiiDialog genresDialog = new AnikumiiDialog(MainActivity.this);
            NestedScrollView genresScroll = new NestedScrollView(MainActivity.this);
            ChipGroup chipGroup = new ChipGroup(MainActivity.this);
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

            reactiveRecyclerView("Filtrados", "https://tioanime.com/directorio" + genresBuilder, "article.anime", (byte) 19);
            anikumiiInputChip.clear();
        });

        filterDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getText(android.R.string.cancel), (DialogInterface dialogInterface, int i) -> {
            anikumiiInputChip.clear();
            filterDialog.dismiss();
        });

        filterDialog.initialize("Filtros", dialogView);
    }

    private void setHeaderInfo(boolean loadCookies) {
        if (loadCookies)
            userCookie = ((Anikumii) getApplication()).getUserCookie();//"f;f;f;f;f;f"

        boolean myanimelist = mPreferences.getBoolean("nav_malAccount", false);
        String userAvatar_str;

        if (myanimelist) {
            userAvatar_str = mPreferences.getString("MALuserAvatar", "imageNotFound");
            userName_str = mPreferences.getString("MALuserName", "user_nameNotFound");
        } else {
            userName_str = mPreferences.getString("userName", "user_nameNotFound");
            userAvatar_str = mPreferences.getString("UserAvatar", "imageNotFound");
        }

        if (!userAvatar_str.equals("imageNotFound")) {
            ImageView userAvatar = findViewById(R.id.user_avatar);
            if (userAvatar != null) {
                if (!myanimelist)
                    userAvatar.setOnClickListener((View view) -> {
                        Intent profile = new Intent(MainActivity.this, AboutUserActivity.class);
                        profile.putExtra("url", "https://animeflv.net/perfil/" + userName_str).putExtra("userName", userName_str);
                        startActivity(profile);

                    });
                GlideApp.with(MainActivity.this).load(userAvatar_str).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop()).into(userAvatar);
            }
        }

        if (!userName_str.equals("user_nameNotFound")) {
            TextView userName = findViewById(R.id.user_name);
            if (userName != null)
                userName.setText(userName_str);
        }
    }

    private void reactiveRecyclerView(String title, String toLoad, String elementClass, byte maxDisplayedItems) {
        anikumiiRecyclerView.clearArray();
        anikumiiRecyclerView.setAdapter(new AdapterAnimes(this, false));
        anikumiiRecyclerView.setToLoad(toLoad);
        anikumiiRecyclerView.setElementClass(elementClass);
        anikumiiRecyclerView.setDynamicListener(maxDisplayedItems);

        canGoBack = true;

        setToolbarTitle(title);
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

                reactiveRecyclerView(query, "https://tioanime.com/directorio?q=" + query, "article.anime", (byte) 19);
                isDisposed = true;
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
                Intent episodesAct = new Intent(MainActivity.this, EpisodesActivity.class);
                episodesAct.putExtra("animeUrl", matrixCursor.getString(4)).putExtra("animeName", matrixCursor.getString(1));
                startActivity(episodesAct);

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

                if (matrixCursor != null && !matrixCursor.isClosed())
                    matrixCursor.close();

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

        publishSubject
                .debounce(300, TimeUnit.MILLISECONDS)
                .filter((String s) -> !s.isEmpty())
                .distinctUntilChanged()
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String query) throws Exception {
                        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                        StringRequest postRequest = new StringRequest(Request.Method.POST, "https://tioanime.com/api/search",
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONArray jsonArray = (JSONArray) new JSONTokener(response).nextValue();

                                            matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID, "animeTitle", "animeType", "animeId", "animeUrl"});

                                            for (byte a = 0; a < jsonArray.length(); a++)
                                                matrixCursor.addRow(new Object[]{1, jsonArray.getJSONObject(a).get("title").toString(), Utils.getTypeFromNumber(jsonArray.getJSONObject(a).get("type").toString()), jsonArray.getJSONObject(a).get("id").toString(), "/anime/" + jsonArray.getJSONObject(a).get("slug").toString()});

                                            searchCursorAdapter.changeCursor(matrixCursor);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                (VolleyError error) -> error.printStackTrace()
                        ) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("value", query);

                                return params;
                            }
                        };
                        queue.add(postRequest);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError((Throwable throwable) -> {
                    if (!isDestroyed())
                        AnikumiiUiHelper.Snackbar(anikumiiRecyclerView, getString(R.string.rxerror), Snackbar.LENGTH_LONG).show();
                })
                .subscribe();
    }
}