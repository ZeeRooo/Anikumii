package com.zeerooo.anikumii2.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiUiHelper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ZeeRooo on 24/02/18
 */

public class MALInfoFragment extends Fragment {

    private short episodes;
    private int malID;
    private StringBuilder genre = new StringBuilder(), stringBuilder = new StringBuilder();
    private String title_english, title_japanese, synonyms, score, type, premiered, airedStatus, synopsis, source, duration, classification, background, rank, popularity, members, favorites, producers, licensor, studio, animeName;

    public MALInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.mal_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null && getActivity() != null) {
            animeName = getArguments().getString("anime_name");

            ImageButton goToSite = getActivity().findViewById(R.id.goToMAL);
            AnikumiiUiHelper.transparentBackground(goToSite);
            goToSite.setVisibility(View.VISIBLE);
            goToSite.setOnClickListener((View view) -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://myanimelist.net/anime/" + malID))));

            ImageButton editStats = getActivity().findViewById(R.id.editStats);
            AnikumiiUiHelper.transparentBackground(editStats);
            editStats.setVisibility(View.VISIBLE);
            editStats.setOnClickListener((View view) -> {
                if (getActivity().getSharedPreferences("ZeeRooo@Anikumii!!", MODE_PRIVATE).getString("MALuserAvatar", null) == null) {
                    AnikumiiUiHelper.Snackbar(getActivity().findViewById(R.id.act_episodes_rootView), getActivity().getString(R.string.warning_not_logged, "MyAnimeList"), Snackbar.LENGTH_LONG).show();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putInt("malID", malID);
                    bundle.putShort("episodes", episodes);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    MALEditFragment fragment = new MALEditFragment();
                    fragment.setArguments(bundle);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.MALAbout, fragment);
                    fragmentTransaction.commit();
                }
            });
        }

        Observable
                .just(true)
                .subscribeOn(Schedulers.computation())
                .doOnNext((Boolean aBoolean) -> networkRequest())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isAdded() && getView() != null)
                            AnikumiiUiHelper.Snackbar(getView(), getString(R.string.rxerror), Snackbar.LENGTH_LONG).show();

                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        if (isAdded())
                            onCompleted();

                        dispose();
                    }
                });
    }

    private String encodeString(String url) {
        return url.replace("<", "%3C").replace(">", "%3E").replace("#", "%23").replace("%", "%25")
                .replace("{", "%7B").replace("}", "%7D").replace("|", "%7C").replace("\\", "%5C")
                .replace("^", "%5E").replace("~", "%7E").replace("[", "%5B").replace("]", "%5D")
                .replace("`", "%60").replace(";", "%3B").replace("/", "%2F").replace("?", "%3F")
                .replace(":", "%3A").replace("@", "%40").replace("=", "%3D").replace("&", "%26")
                .replace("$", "%24").replace("+", "%2B").replace(",", "%2C").replace(" ", "%20");
    }

    private void networkRequest() throws Exception {
        // animeName = getAnime(animeName);
        HttpsURLConnection conn = (HttpsURLConnection) new URL("https://api.jikan.moe/v3/search/anime?q=" + encodeString(animeName)).openConnection();
        //  HttpsURLConnection conn = (HttpsURLConnection) new URL("https://myanimelist.net/search/prefix.json?type=anime&keyword=" + animeName).openConnection();
        conn.setRequestMethod("GET");
        conn.setUseCaches(true);

        JSONObject MAL = (JSONObject) new JSONTokener(getJson(conn)).nextValue();
        JSONArray jsonArray = MAL.getJSONArray("results");
       /* JSONArray jsonArray = MAL.getJSONArray("categories").getJSONObject(0).getJSONArray("items");

        for (int i = 0; i < jsonArray.length(); ++i) {
            MAL = jsonArray.getJSONObject(i);
            if (MAL.getString("name").equals(animeName))
                malID = MAL.getString("id");
        }

        if (malID == null)
            malID = jsonArray.getJSONObject(0).getString("id");*/
        for (byte i = 0; i < jsonArray.length(); ++i) {
            if (jsonArray.getJSONObject(i).getString("title").equals(animeName)) {
                malID = jsonArray.getJSONObject(i).getInt("mal_id");
                break;
            }
        }

        if (malID == 0)
            malID = jsonArray.getJSONObject(0).getInt("mal_id");

        conn = (HttpsURLConnection) new URL("https://api.jikan.moe/v3/anime/" + malID).openConnection();
        conn.disconnect();

        MAL = (JSONObject) new JSONTokener(getJson(conn)).nextValue();

        // -- info
        title_english = "Título inglés: " + Html.fromHtml(MAL.getString("title_english"));
        title_japanese = "Título japonés: " + MAL.getString("title_japanese");

        for (int i = 0; i < MAL.getJSONArray("title_synonyms").length(); ++i) {
            stringBuilder.append(MAL.getJSONArray("title_synonyms").getString(i));
            if (i != MAL.getJSONArray("title_synonyms").length() - 1)
                stringBuilder.append(", ");
        }
        synonyms = "Sinonimo: " + stringBuilder;
        stringBuilder.delete(0, stringBuilder.length());

        type = "Tipo: " + MAL.getString("type");
        episodes = Short.valueOf(MAL.getString("episodes").replace("null", "0"));
        airedStatus = "Período de emisión: " + MAL.getJSONObject("aired").getString("string") + " (" + MAL.getString("broadcast") + ")";
        premiered = "Estrenado: " + MAL.getString("premiered");

        if (!MAL.getJSONArray("producers").isNull(0)) {
            producers = "Productores: " + arrayToString(MAL, "producers");
            stringBuilder.delete(0, stringBuilder.length());
        } else
            producers = "Productores: ";


        if (!MAL.getJSONArray("licensors").isNull(0)) {
            licensor = "Licenciantes: " + arrayToString(MAL, "licensors");
            stringBuilder.delete(0, stringBuilder.length());
        } else
            licensor = "Licenciantes: ";


        if (!MAL.getJSONArray("studios").isNull(0)) {
            studio = "Estudio: " + arrayToString(MAL, "studios");
            stringBuilder.delete(0, stringBuilder.length());
        } else
            studio = "Estudio: ";

        source = "Fuente: " + MAL.getString("source");
        JSONArray genresArray = MAL.getJSONArray("genres");
        genre.append("Géneros: ");
        for (int genreCount = 0; genreCount < genresArray.length(); genreCount++) {
            genre.append(genresArray.getJSONObject(genreCount).getString("name"));
            if (genreCount != genresArray.length() - 1)
                genre.append(", ");
        }
        duration = "Duración: " + MAL.getString("duration");
        classification = "Clasificación: " + MAL.getString("rating");
        synopsis = "Sinopsis: " + Html.fromHtml(MAL.getString("synopsis"));
        background = "Background: " + Html.fromHtml(MAL.getString("background"));

        // -- statics
        score = "Valoracion: " + MAL.getString("score") + " (por " + MAL.getString("rank") + " usuarios)";
        rank = "Rango: #" + MAL.getString("scored_by");
        popularity = "Popularidad: " + MAL.getString("popularity");
        members = "Miembros: " + MAL.getString("members");
        favorites = "Favoritos: " + MAL.getString("favorites");
    }

 /*   private String getAnime(String string) {
        return string.toLowerCase().replace(":", "").replace("(", "").replace(")", "");
    }*/

    private String arrayToString(JSONObject MAL, String jsonArray) throws Exception {
        for (byte i = 0; i < MAL.getJSONArray(jsonArray).length(); ++i) {
            stringBuilder.append(MAL.getJSONArray(jsonArray).getJSONObject(i).getString("name"));
            if (i != MAL.getJSONArray(jsonArray).length() - 1)
                stringBuilder.append(", ");
        }
        return stringBuilder.toString();
    }

    private String getJson(HttpsURLConnection connection) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder xml = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null)
            xml.append(line);
        bufferedReader.close();
        connection.disconnect();
        return xml.toString();
    }

    private SpannableStringBuilder getBold(String s) {
        if (s == null)
            s = "";
        SpannableStringBuilder sb = new SpannableStringBuilder(s);
        sb.setSpan(new ForegroundColorSpan(Color.rgb(66, 104, 179)), 0, s.indexOf(":") + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, s.indexOf(":") + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return sb;
    }

    private void onCompleted() {
        for (byte count = 1; count < 22; count++) {
            String text = null;

            switch (count) {
                case 1:
                    text = title_english;
                    break;
                case 2:
                    text = title_japanese;
                    break;
                case 3:
                    text = synonyms;
                    break;
                case 4:
                    text = type;
                    break;
                case 5:
                    text = "Episodios: " + episodes;
                    break;
                case 6:
                    text = airedStatus;
                    break;
                case 7:
                    text = premiered;
                    break;
                case 8:
                    text = producers;
                    break;
                case 9:
                    text = licensor;
                    break;
                case 10:
                    text = studio;
                    break;
                case 11:
                    text = source;
                    break;
                case 12:
                    text = genre.toString();
                    break;
                case 13:
                    text = duration;
                    break;
                case 14:
                    text = classification;
                    break;
                case 15:
                    text = synopsis;
                    break;
                case 16:
                    text = background;
                    break;
                case 17:
                    text = score;
                    break;
                case 18:
                    text = rank;
                    break;
                case 19:
                    text = popularity;
                    break;
                case 20:
                    text = members;
                    break;
                case 21:
                    text = favorites;
                    break;
            }

            TextView info = (TextView) getLayoutInflater().inflate(R.layout.mal_textview, null);
            info.setId(count);
            info.setText(getBold(text));
            info.setContentDescription(text);

            if (count <= 14)
                ((LinearLayout) getActivity().findViewById(R.id.malInformation)).addView(info);
            else if (count == 15 || count == 16)
                ((LinearLayout) getActivity().findViewById(R.id.malSynopsis)).addView(info);
            else
                ((LinearLayout) getActivity().findViewById(R.id.malStatics)).addView(info);
        }
    }
}