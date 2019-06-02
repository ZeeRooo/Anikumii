package com.zeerooo.anikumii.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiInputChip;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiNumberPicker;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiSharedPreferences;
import com.zeerooo.anikumii.misc.MyAnimeListModel;
import com.zeerooo.anikumii.misc.Utils;

import java.util.Calendar;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ZeeRooo on 25/02/18
 */

public class MALEditFragment extends Fragment {
    private AnikumiiInputChip anikumiiInputChip;
    private MyAnimeListModel myAnimeListModel;

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

        if (getActivity() != null && getArguments() != null)
            Observable
                    .just(true)
                    .subscribeOn(Schedulers.io())
                    .doOnNext(aBoolean -> myAnimeListModel = new MyAnimeListModel(Utils.encodeString(Utils.removeLastNumberAndSpace(getArguments().getString("malName")).split("\\(")[0]), new AnikumiiSharedPreferences(getActivity()).getString("malUserName", null), true))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean aBoolean) {

                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {
                            final Spinner statusSpinner = getActivity().findViewById(R.id.malSetStatus);
                            statusSpinner.setSelection(myAnimeListModel.getStatus() - 1);

                            AnikumiiNumberPicker seenEpisodesPicker = getActivity().findViewById(R.id.episodes_numberpicker);
                            seenEpisodesPicker.setMaxValue(getArguments().getShort("episodes"));
                            seenEpisodesPicker.setValue(String.valueOf(myAnimeListModel.getSeenEpisodes()));

                            final TextView startDate = getActivity().findViewById(R.id.malSetStart);
                            startDate.setOnClickListener(view -> calendarPicker(startDate, true));
                            startDate.setText(getString(R.string.mal_startDateText, myAnimeListModel.getStartYear()));

                            final TextView endDate = getActivity().findViewById(R.id.malSetEnd);
                            endDate.setOnClickListener(view -> calendarPicker(endDate, false));
                            endDate.setText(getString(R.string.mal_endDateText, myAnimeListModel.getEndYear()));

                            anikumiiInputChip = new AnikumiiInputChip(getView(), null);
                            for (byte count = 0; count < myAnimeListModel.getStrings().length; count++)
                                anikumiiInputChip.addRemovableChip(myAnimeListModel.getStrings()[count], getView(), true);

                            final Spinner prioritySpinner = getActivity().findViewById(R.id.malSetPriority);
                            prioritySpinner.setSelection(myAnimeListModel.getPriority());

                            final Spinner sourceSpinner = getActivity().findViewById(R.id.malSetSource);
                            sourceSpinner.setSelection(myAnimeListModel.getStorageType());

                            final AnikumiiNumberPicker rewatchPicker = getActivity().findViewById(R.id.rewatch_numberpicker);
                            rewatchPicker.setMaxValue((short) 100);
                            rewatchPicker.setValue("0");

                            final Spinner rewatchValueSpinner = getActivity().findViewById(R.id.malSetRewatchProbability);

                            final Spinner stars = getActivity().findViewById(R.id.malSetStars);
                            stars.setSelection(myAnimeListModel.getScore() - 1);

                            final TextInputEditText comment = getActivity().findViewById(R.id.malSetComment);

                            MaterialButton btnSend = getActivity().findViewById(R.id.malEditSend);
                            btnSend.setOnClickListener(view -> {
                                myAnimeListModel.setStatus((byte) (statusSpinner.getSelectedItemPosition() + 1));
                                myAnimeListModel.setSeenEpisodes(seenEpisodesPicker.getValue());
                                myAnimeListModel.setScore((byte) (stars.getSelectedItemPosition() + 1));
                                myAnimeListModel.setTags(anikumiiInputChip);
                                myAnimeListModel.setPriority((byte) prioritySpinner.getSelectedItemPosition());
                                myAnimeListModel.setStorageType((byte) (sourceSpinner.getSelectedItemPosition() + 1));
                                myAnimeListModel.setRewatched((byte) rewatchPicker.getValue());
                                myAnimeListModel.setRewatchValue((byte) (rewatchValueSpinner.getSelectedItemPosition() + 1));
                                myAnimeListModel.setComment(comment.getText().toString());
                                myAnimeListModel.apiHandler(getActivity(), (byte) 2);

                                Snackbar.make(getView(), "Anime añadido y actualizado", Snackbar.LENGTH_LONG).show();
                            });

                            MaterialButton deleteAnime = getActivity().findViewById(R.id.malEditDelete);
                            deleteAnime.setOnClickListener(view -> {
                                myAnimeListModel.apiHandler(getActivity(), (byte) 0);

                                Snackbar.make(getView(), "Anime borrado de la lista", Snackbar.LENGTH_LONG).show();
                            });
                        }
                    });
    }

    private void calendarPicker(final TextView textView, final boolean start) {
        final Calendar calendar = Calendar.getInstance();
        if (getActivity() != null) {
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), R.style.dialog, (DatePicker datePicker, int i, int i1, int i2) -> {
                i1 += 1;
                if (start) {
                    myAnimeListModel.setStartYear(i + "-" + i1 + "-" + i2);
                    textView.setText(getString(R.string.mal_startDateText, myAnimeListModel.getStartYear()));
                } else {
                    myAnimeListModel.setEndYear(i + "-" + i1 + "-" + i2);
                    textView.setText(getString(R.string.mal_endDateText, myAnimeListModel.getEndYear()));
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
