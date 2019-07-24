package com.zeerooo.anikumii.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiConnection;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiUiHelper;
import com.zeerooo.anikumii.misc.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ZeeRooo on 24/02/18
 */

public class MALInfoFragment extends Fragment {

    public static JSONObject MAL;
    private short episodes;
    private int malID;
    private StringBuilder genre, stringBuilder;
    private String title_english, title_japanese, synonyms, score, type, premiered, airedStatus, synopsis, source, duration, classification, background, rank, popularity, members, favorites, producers, licensor, studio, title;
    private boolean isFirstTime = true;
    private ImageButton goToSite, editStats;

    public MALInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.mal_fragment, container, false);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        if (menuVisible && isFirstTime) {
            if (getArguments() != null && getActivity() != null) {
                malID = getArguments().getInt("malID");
                genre = new StringBuilder();
                stringBuilder = new StringBuilder();

                Observable
                        .just(true)
                        .observeOn(Schedulers.io())
                        .doOnNext(aBoolean -> networkRequest())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableObserver<Boolean>() {
                            @Override
                            public void onNext(Boolean aBoolean) {

                            }

                            @Override
                            public void onError(Throwable e) {
                                if (getView() != null)
                                    AnikumiiUiHelper.errorSnackbar(getView(), Snackbar.LENGTH_LONG, e.toString(), null).show();
                            }

                            @Override
                            public void onComplete() {
                                onCompleted();

                                if (!isDisposed())
                                    dispose();

                                isFirstTime = false;
                            }
                        });
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        goToSite = getActivity().findViewById(R.id.goToMAL);
        TooltipCompat.setTooltipText(goToSite, getActivity().getString(R.string.visit_mal_page));
        AnikumiiUiHelper.transparentBackground(goToSite);

        editStats = getActivity().findViewById(R.id.editStats);
        TooltipCompat.setTooltipText(editStats, getActivity().getString(R.string.edit_mal_stats));
        AnikumiiUiHelper.transparentBackground(editStats);
    }

    private void networkRequest() throws JSONException, IOException {
        MALInfoFragment.MAL = new JSONObject(new AnikumiiConnection().getStringResponse("GET", "https://api.jikan.moe/v3/anime/" + malID, null));
        // -- statics
        score = "Valoración: " + MAL.getString("score") + " (por " + MAL.getString("rank") + " usuarios)";
        rank = "Rango: #" + MAL.getString("scored_by");
        popularity = "Popularidad: " + MAL.getString("popularity");
        members = "Miembros: " + MAL.getString("members");
        favorites = "Favoritos: " + MAL.getString("favorites");

        // -- info
        title = MAL.getString("title");
        title_english = "Título inglés: " + Html.fromHtml(MAL.getString("title_english"));
        title_japanese = "Título japonés: " + MAL.getString("title_japanese");

        for (int i = 0; i < MAL.getJSONArray("title_synonyms").length(); ++i) {
            stringBuilder.append(MAL.getJSONArray("title_synonyms").getString(i));
            if (i != MAL.getJSONArray("title_synonyms").length() - 1)
                stringBuilder.append(", ");
        }
        synonyms = "Sinónimo: " + stringBuilder;
        stringBuilder.delete(0, stringBuilder.length());

        type = "Tipo: " + MAL.getString("type");
        episodes = Short.valueOf(MAL.getString("episodes").replace("null", "0"));
      /*  if (episodes == 0)
            episodes = 1;*/
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
    }

    private String arrayToString(JSONObject MAL, String jsonArray) throws JSONException {
        for (byte i = 0; i < MAL.getJSONArray(jsonArray).length(); ++i) {
            stringBuilder.append(MAL.getJSONArray(jsonArray).getJSONObject(i).getString("name"));
            if (i != MAL.getJSONArray(jsonArray).length() - 1)
                stringBuilder.append(", ");
        }
        return stringBuilder.toString();
    }

    private void onCompleted() {
        goToSite.setVisibility(View.VISIBLE);
        goToSite.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://myanimelist.net/anime/" + malID))));

        editStats.setVisibility(View.VISIBLE);
        editStats.setOnClickListener(view -> {
            if (getActivity().getSharedPreferences("ZeeRooo@Anikumii!!", MODE_PRIVATE).getString("malUserAvatar", null) == null) {
                Snackbar.make(getActivity().findViewById(R.id.act_episodes_rootView), getActivity().getString(R.string.warning_not_logged), Snackbar.LENGTH_LONG).show();
            } else {
                Bundle bundle = new Bundle();
                bundle.putString("malName", title);
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

        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.rgb(66, 104, 179));
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        for (byte count = 1; count < 22; count++) {
            String text = "";

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
           // info.setId(count);
            info.setText(Utils.getBold(text, foregroundColorSpan, styleSpan));

            if (count <= 14)
                ((LinearLayout) getActivity().findViewById(R.id.malInformation)).addView(info);
            else if (count == 15 || count == 16)
                ((LinearLayout) getActivity().findViewById(R.id.malSynopsis)).addView(info);
            else
                ((LinearLayout) getActivity().findViewById(R.id.malStatics)).addView(info);
        }
    }
}