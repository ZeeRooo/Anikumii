package com.zeerooo.anikumii2.fragments;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiDialog;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiInputChip;
import com.zeerooo.anikumii2.services.MALApiService;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * Created by ZeeRooo on 25/02/18
 */

public class MALEditFragment extends Fragment {
    private int malID;
    private StringBuilder params = new StringBuilder();
    private AnikumiiInputChip anikumiiInputChip;

    public MALEditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.mal_edit, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() != null && getArguments() != null) {
            malID = getArguments().getInt("malID");

            final Spinner statusSpinner = getActivity().findViewById(R.id.malSetStatus);

            final NumberPicker seenEpisodesPicker = new NumberPicker(getActivity());
            final TextView seenEpisodes = getActivity().findViewById(R.id.malSetEpisodes);
            seenEpisodes.setOnClickListener((View view) -> {
                AnikumiiDialog anikumiiDialog = new AnikumiiDialog(getActivity());
                seenEpisodesPicker.setMaxValue(getArguments().getShort("episodes"));
                seenEpisodesPicker.setMinValue(0);

                anikumiiDialog.setButton(DialogInterface.BUTTON_POSITIVE, getText(android.R.string.ok), (DialogInterface dialogInterface, int i) -> {
                    ((TextView) getActivity().findViewById(R.id.malSetEpisodes)).setText(getString(R.string.mal_seenEpisodesNumber, seenEpisodesPicker.getValue()));
                    anikumiiDialog.dismiss();
                });
                anikumiiDialog.addCancelButton();

                if (seenEpisodesPicker.getParent() != null)
                    ((ViewGroup) seenEpisodesPicker.getParent()).removeAllViews();

                anikumiiDialog.initialize("Episodios vistos", seenEpisodesPicker);
            });

            final TextView startDate = getActivity().findViewById(R.id.malSetStart);
            startDate.setOnClickListener((View view) -> calendarPicker(startDate, true));

            final TextView endDate = getActivity().findViewById(R.id.malSetEnd);
            endDate.setOnClickListener((View view) -> calendarPicker(endDate, false));

            anikumiiInputChip = new AnikumiiInputChip(getView(), null);

            final Spinner prioritySpinner = getActivity().findViewById(R.id.malSetPriority);

            final Spinner sourceSpinner = getActivity().findViewById(R.id.malSetSource);

            final NumberPicker rewatchPicker = new NumberPicker(getActivity());
            TextView rewatch = getActivity().findViewById(R.id.malSetRewatchTimes);
            rewatch.setOnClickListener((View view) -> {
                AnikumiiDialog anikumiiDialog = new AnikumiiDialog(getActivity());

                rewatchPicker.setMaxValue(20);
                rewatchPicker.setMinValue(0);

                anikumiiDialog.setButton(DialogInterface.BUTTON_POSITIVE, getText(android.R.string.ok), (DialogInterface dialogInterface, int i) -> {
                    ((TextView) getActivity().findViewById(R.id.malSetRewatchTimes)).setText(getString(R.string.mal_rewatchTimes, rewatchPicker.getValue()));
                    anikumiiDialog.dismiss();
                });
                anikumiiDialog.addCancelButton();

                if (rewatchPicker.getParent() != null)
                    ((ViewGroup) rewatchPicker.getParent()).removeAllViews();

                anikumiiDialog.initialize("Veces que he revisto el anime", rewatchPicker);
            });

            final Spinner rewatchValueSpinner = getActivity().findViewById(R.id.malSetRewatchProbability);

            final Spinner stars = getActivity().findViewById(R.id.malSetStars);

            final AutoCompleteTextView comment = getActivity().findViewById(R.id.malSetComment);

            Button btnSend = getActivity().findViewById(R.id.malEditSend);
            btnSend.setOnClickListener((View view) -> {
                params.append("&anime_id=").append(malID)
                        .append("&aeps=").append(seenEpisodesPicker.getValue())
                        .append("&astatus=").append((statusSpinner.getSelectedItemPosition() + 1))
                        .append("&add_anime%5Bstatus%5D=").append((statusSpinner.getSelectedItemPosition() + 1))
                        .append("&add_anime%5Bnum_watched_episodes%5D=").append(seenEpisodesPicker.getValue())
                        .append("&add_anime%5Bscore%5D=").append((stars.getSelectedItemPosition() + 1))
                        .append("&add_anime%5Btags%5D=").append(anikumiiInputChip.toString().replace("[", "").replace("]", ""))
                        .append("&add_anime%5Bpriority%5D=").append(prioritySpinner.getSelectedItemPosition())
                        .append("&add_anime%5Bstorage_type%5D=").append((sourceSpinner.getSelectedItemPosition() + 1))
                        .append("&add_anime%5Bstorage_value%5D=0")
                        .append("&add_anime%5Bnum_watched_times%5D=").append(rewatchPicker.getValue())
                        .append("&add_anime%5Brewatch_value%5D=").append((rewatchValueSpinner.getSelectedItemPosition() + 1))
                        .append("&add_anime%5Bcomments%5D=").append(comment.getText().toString())
                        .append("&add_anime%5Bis_asked_to_discuss%5D=1")
                        .append("&add_anime%5Bsns_post_type%5D=1")
                        .append("&submitIt=0");
                Intent MALApi = new Intent(getActivity(), MALApiService.class);
                MALApi.putExtra("params", params.toString()).putExtra("malID", malID).putExtra("delete", false);
                getActivity().startService(MALApi);
                params.delete(0, params.length());
                anikumiiInputChip.clear();

                Snackbar.make(getView(), "Anime añadido y actualizado", Snackbar.LENGTH_LONG).show();
            });

            Button deleteAnime = getActivity().findViewById(R.id.malEditDelete);
            deleteAnime.setOnClickListener((View view) -> {
                Intent MALApi = new Intent(getActivity(), MALApiService.class);
                MALApi.putExtra("malID", malID).putExtra("delete", true);
                getActivity().startService(MALApi);

                Snackbar.make(getView(), "Anime borrado de la lista", Snackbar.LENGTH_LONG).show();
            });
        }
    }

    private void calendarPicker(final TextView textView, final boolean start) {
        final Calendar calendar = Calendar.getInstance();
        if (getActivity() != null) {
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), R.style.dialog, (DatePicker datePicker, int i, int i1, int i2) -> {
                i1 += 1;
                if (start) {
                    params.append("&add_anime%5Bstart_date%5D%5Bmonth%5D=").append(i1)
                            .append("&add_anime%5Bstart_date%5D%5Bday%5D=").append(i2)
                            .append("&add_anime%5Bstart_date%5D%5Byear%5D=").append(i);
                    textView.setText(getString(R.string.mal_startDateText, i2, i1, i));
                } else {
                    params.append("&add_anime%5Bfinish_date%5D%5Bmonth%5D=").append(i1)
                            .append("&add_anime%5Bfinish_date%5D%5Bday%5D=").append(i2)
                            .append("&add_anime%5Bfinish_date%5D%5Byear%5D=").append(i);
                    textView.setText(getString(R.string.mal_endDateText, i2, i1, i));
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getActivity() != null)
                getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
