package com.zeerooo.anikumii2.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.app.PictureInPictureParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Rational;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.htetznaing.xgetter.XGetter;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiDialog;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiSharedPreferences;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiUiHelper;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiVideoView;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiWebHelper;
import com.zeerooo.anikumii2.misc.ServerHelper;

import org.jsoup.nodes.Element;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ZeeRooo on 09/03/18
 */

public class VPlayerActivity extends AppCompatActivity {

    private AnikumiiVideoView anikumiiVideoView;
    private int primaryProgress, secondaryProgressMax;
    private short episode;//, natsukiHosters;
    private RelativeLayout mButtonsHeader;//, nextEpisodeHeader;
    private SeekBar mSeekbar;
    private TextView remainingTime;
    private String /*cookie, animeID,*/ url, animeName, animeNumber, nextUrl, prevUrl, episodes, rawUrl, serverOptionDefault;
    private boolean /*haveNext, seen,*/
            enablePip;
    private BottomSheetBehavior mBottomSheetBehavior;
    private ImageButton previous, next, pause;
    private Handler handler = new Handler(), headerHandler = new Handler();
    private ServerHelper serverHelper;
    private AnikumiiSharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private SimpleDateFormat simpleDateFormat;
    private Date date;
    private BroadcastReceiver networkReceiver;
    private XGetter xGetter;

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        xGetter = new XGetter();
        setReactive(getIntent());
        Toolbar mToolbar = findViewById(R.id.videoToolbar);
        setSupportActionBar(mToolbar);

        sharedPreferences = new AnikumiiSharedPreferences(this);

        anikumiiVideoView = findViewById(R.id.video_view);

        progressBar = findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);

        mButtonsHeader = findViewById(R.id.buttons_header);

        mSeekbar = findViewById(R.id.progress);
        mSeekbar.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        remainingTime = findViewById(R.id.remainingTime);
        // nextEpisodeHeader = findViewById(R.id.nextEpisodeHeader);
        previous = findViewById(R.id.previous_btn);
        AnikumiiUiHelper.transparentBackground(previous);
        next = findViewById(R.id.next_btn);
        AnikumiiUiHelper.transparentBackground(next);
        mBottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_videoPlayer));

        pause = findViewById(R.id.pauseplay_btn);
        AnikumiiUiHelper.transparentBackground(pause);

        date = new Date();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        anikumiiVideoView.setOnErrorListener((MediaPlayer mp, int what, int extra) -> {
            progressBar.setVisibility(View.VISIBLE);

            if (serverOptionDefault.equals("Natsuki") && isConnected()) {
                anikumiiVideoView.stopPlayback();
                //   getNatsukiHosters();
               // natsukiHosters++;

                anikumiiVideoView.setVideoURI(Uri.parse(url));
                anikumiiVideoView.start();
            }

            if (!isConnected()) {
                countDown();
                AnikumiiUiHelper.Snackbar(anikumiiVideoView, "Conexión a internet perdida", Snackbar.LENGTH_LONG).show();
                pause.setImageResource(android.R.drawable.ic_media_play);

                primaryProgress = mp.getCurrentPosition();

                anikumiiVideoView.pause();

                networkReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (isConnected()) {
                            anikumiiVideoView.resume();
                            resumeVideo();
                        }
                    }
                };
                IntentFilter filter = new IntentFilter();
                filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
                registerReceiver(networkReceiver, filter);
            }
            return true;
        });
        anikumiiVideoView.setOnTouchListener((View v, MotionEvent event) -> {
            if (!mButtonsHeader.isShown()) {
                mButtonsHeader.setVisibility(View.VISIBLE);
                setAnimation(mButtonsHeader, android.R.anim.fade_in);
                headerHandler.postDelayed(() -> {
                    mButtonsHeader.setVisibility(View.GONE);
                    setAnimation(mButtonsHeader, android.R.anim.fade_out);
                }, 4000);

                if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                return v.performClick();
            } else
                return false;
        });

        //  cookie = ((Anikumii) getApplication()).getUserCookie();//"f;f;f;f;f;f"

        anikumiiVideoView.setOnPreparedListener((MediaPlayer mp) -> {
            mp.start();

            progressBar.setVisibility(View.GONE);

            if (mp.getDuration() > 3600000)
                simpleDateFormat = new SimpleDateFormat("mmm:ss", Locale.getDefault());
            else
                simpleDateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());

            mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser)
                        anikumiiVideoView.seekTo(progress);
                }
            });

            pause.setOnClickListener((View v) -> {
                if (anikumiiVideoView.isPlaying()) {
                    anikumiiVideoView.pause();
                    ((ImageButton) v).setImageResource(android.R.drawable.ic_media_play);
                } else {
                    anikumiiVideoView.start();
                    ((ImageButton) v).setImageResource(android.R.drawable.ic_media_pause);
                    countDown();
                }
            });

            mSeekbar.setMax(mp.getDuration());
            secondaryProgressMax = mp.getDuration() / 100;
            setAnimation(mButtonsHeader, android.R.anim.fade_out);
            mButtonsHeader.setVisibility(View.GONE);
            countDown();
        /*    if (!cookie.equals("f;f;f") && !seen)
                seenHelper((byte) 1);*/

            mp.setOnBufferingUpdateListener((MediaPlayer mediaPlayer, int percent) -> mSeekbar.setSecondaryProgress(percent * secondaryProgressMax));
        });

        bottomSheetSetup();

        enablePip = sharedPreferences.getBoolean("enablePip", true);

        serverHelper = new ServerHelper();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (isPlaying())
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                DownloadManager mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

                final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setDestinationInExternalPublicDir("/Anikumii!!/" + animeName, animeNumber + ".mp4");
                request.setVisibleInDownloadsUi(true);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                mDownloadManager.enqueue(request);
            } else
                AnikumiiUiHelper.Snackbar(findViewById(R.id.videoPlayerRootView), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
    }

    private boolean isPlaying() {
        if (anikumiiVideoView.getCurrentPosition() > 0)
            return true;
        else {
            AnikumiiUiHelper.Snackbar(findViewById(R.id.videoPlayerRootView), getString(R.string.unload_video), Snackbar.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setReactive(intent);
    }

    @Override
    public void onStart() {
        super.onStart();

        resumeVideo();
    }

    private void resumeVideo() {
        pause.setImageResource(android.R.drawable.ic_media_pause);
        anikumiiVideoView.seekTo(primaryProgress);
        anikumiiVideoView.start();
        if (primaryProgress > 0)
            countDown();
    }

    @Override
    public void onUserLeaveHint() {
        if (Build.VERSION.SDK_INT > 26 && enablePip)
            enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(new Rational(anikumiiVideoView.getWidth(), anikumiiVideoView.getHeight())).build());
    }

    @Override
    public void onPause() {
        super.onPause();

        primaryProgress = anikumiiVideoView.getCurrentPosition();

        if (Build.VERSION.SDK_INT <= 23)
            anikumiiVideoView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (Build.VERSION.SDK_INT >= 24)
            anikumiiVideoView.pause();

        try {
            if (networkReceiver != null)
                unregisterReceiver(networkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        anikumiiVideoView.stopPlayback();
        handler.removeCallbacksAndMessages(null);
        headerHandler.removeCallbacksAndMessages(null);

        if (serverHelper.isAlive())
            serverHelper.stop();
    }

    @Override
    public void onBackPressed() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else
            super.onBackPressed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.episodesList:
                Intent episodesAct = new Intent(this, EpisodesActivity.class);
                episodesAct.putExtra("animeUrl", episodes).putExtra("animeName", animeName);
                startActivity(episodesAct);
                break;
            case R.id.episodesComments:
                startActivity(new Intent(this, CommentsActivity.class).putExtra("rawUrl", rawUrl));
                break;
            case R.id.overflowVideoPlayer:
                if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setReactive(final Intent intent) {
        if (serverHelper != null && serverHelper.isAlive())
            serverHelper.stop();

        rawUrl = intent.getStringExtra("chapterUrl");
        animeName = intent.getStringExtra("chapterTitle");
        animeNumber = intent.getStringExtra("chapterNumber");

        Observable
                .just(true)
                .subscribeOn(Schedulers.io())
                .doOnNext((Boolean aBoolean) -> networkRequest(intent))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (!isDestroyed()) {
                            final Snackbar snackbar = AnikumiiUiHelper.Snackbar(anikumiiVideoView, getString(R.string.rxerror), Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("Cambiar server", (View view) -> {
                                serverSwitcherDialog();
                                snackbar.dismiss();
                            });
                            snackbar.show();
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (!isDestroyed())
                            setVideoStuff(intent);

                        dispose();
                    }
                });
    }

    private void networkRequest(Intent intent) throws IOException, StringIndexOutOfBoundsException {
        Element controls = AnikumiiWebHelper.go(rawUrl, this).get();

        if (serverOptionDefault == null)
            serverOptionDefault = intent.getStringExtra("serverOption");

        if (serverOptionDefault == null)
            serverOptionDefault = sharedPreferences.getString("defaultServer", "Zippyshare");

        String connect;

        switch (serverOptionDefault) {
            case "MediaFire":
                connect = controls.selectFirst(".text-center > a[href~=mediafire]").attr("href");
                url = xGetter.mediafire(connect);
                break;
            case "Streamango":
                connect = controls.selectFirst(".text-center > a[href~=streamango]").attr("href");
                url = xGetter.fruits(connect);
                break;
            case "Zippyshare":
                connect = controls.selectFirst(".text-center > a[href~=zippyshare]").attr("href");
                url = xGetter.zippyshare(connect);
                break;
        }

        episodes = controls.select("span.btn:nth-child(2) > a:nth-child(1)").attr("href");
        prevUrl = controls.select("span.btn:nth-child(1) > a:nth-child(1)").attr("href");
        nextUrl = controls.select("span.btn:nth-child(3) > a:nth-child(1)").attr("href");

        //  seen = controls.getElementsByClass("BtnNw CVst BxSdw fa-eye").attr("data-seen").isEmpty();
    }

    private void setVideoStuff(final Intent intent) {
        anikumiiVideoView.setVideoURI(Uri.parse(url));

        primaryProgress = 0;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(animeName);
            getSupportActionBar().setSubtitle(animeNumber);
        }

        episode = Short.parseShort(intent.getStringExtra("chapterNumber").split(" ")[1]);

        if (!prevUrl.equals("#")) {
            previous.setVisibility(View.VISIBLE);
            previous.setOnClickListener((View v) -> {
                if (animeNumber != null)
                    videoHelper("https://tioanime.com" + prevUrl, "Episodio " + (episode - 1));
                else
                    videoHelper("https://tioanime.com" + prevUrl, null);
            });
        } else
            previous.setVisibility(View.INVISIBLE);

        if (!nextUrl.equals("#")) {
            next.setVisibility(View.VISIBLE);
            next.setOnClickListener((View v) -> {
                if (animeNumber != null)
                    videoHelper("https://tioanime.com" + nextUrl, "Episodio " + (Short.parseShort(animeNumber.split(" ")[1]) + 1));
                else
                    videoHelper("https://tioanime.com" + nextUrl, null);
            });
        } else
            next.setVisibility(View.INVISIBLE);
    }

    private void bottomSheetSetup() {
     /*   ImageButton unseen = findViewById(R.id.unseenAflv);
        AnikumiiUiHelper.transparentBackground(unseen);
        TooltipCompat.setTooltipText(unseen, getString(R.string.unseen));
        if (cookie.equals("f;f;f"))//"f;f;f;f;f;f"
            unseen.setVisibility(View.GONE);
        else
            unseen.setVisibility(View.VISIBLE);
        unseen.setOnClickListener((View view) -> {
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
         //   seenHelper((byte) 0);
        });*/

        ImageButton changeServer = findViewById(R.id.changeServer);
        AnikumiiUiHelper.transparentBackground(changeServer);
        TooltipCompat.setTooltipText(changeServer, getString(R.string.changeServer));
        changeServer.setOnClickListener((View view) -> {
            serverSwitcherDialog();
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        ImageButton download = findViewById(R.id.downloadEpisode);
        AnikumiiUiHelper.transparentBackground(download);
        TooltipCompat.setTooltipText(download, getString(R.string.download));
        download.setOnClickListener((View view) -> {
            if (isPlaying())
                downloadDialog();

            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        ImageButton stream = findViewById(R.id.streamButtom);
        AnikumiiUiHelper.transparentBackground(stream);
        TooltipCompat.setTooltipText(stream, getString(R.string.bottom_sheet_stream));
        stream.setOnClickListener((View view) -> {
            if (isPlaying()) {
                serverHelper.setUrl(url);
                serverHelper.setTitle(animeName + " " + animeNumber + " - Anikumii!!");
                try {
                    serverHelper.start();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

                View sheetView = getLayoutInflater().inflate(R.layout.video_player_stream_bottom_sheet, null);
                final BottomSheetDialog dialog = new BottomSheetDialog(VPlayerActivity.this);
                dialog.setContentView(sheetView);

                ((TextView) sheetView.findViewById(R.id.ipText)).setText(serverHelper.getMyIp(VPlayerActivity.this));

                Button disconnect = sheetView.findViewById(R.id.disconnectStream);
                disconnect.setOnClickListener((View v) -> {
                    serverHelper.stop();
                    dialog.dismiss();
                });

                dialog.show();
            }
        });
    }

    private void downloadDialog() {
        AnikumiiDialog downloadDialog = new AnikumiiDialog(this);

        downloadDialog.setMessage(getString(R.string.downloadMessage));

        ChipGroup chipGroup = new ChipGroup(this);
        chipGroup.setPadding(30, 30, 30, 30);
        chipGroup.setSingleSelection(true);

        Chip appChip = new Chip(this);
        appChip.setText(getString(R.string.app_name));
        appChip.setCheckable(true);
        appChip.setTextColor(Color.BLACK);
        appChip.setChipBackgroundColor(ColorStateList.valueOf(Color.WHITE));
        chipGroup.addView(appChip);

        Chip browserChip = new Chip(this);
        browserChip.setText("Navegador");
        browserChip.setCheckable(true);
        browserChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#fffdd1")));
        browserChip.setTextColor(Color.BLACK);
        chipGroup.addView(browserChip);

        chipGroup.setOnCheckedChangeListener((ChipGroup group, int checkedId) -> {
            if (checkedId != -1) {
                if (((Chip) group.findViewById(checkedId)).getText().equals(getString(R.string.app_name)))
                    ActivityCompat.requestPermissions(VPlayerActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                else
                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)));

                downloadDialog.dismiss();
            }
        });

        downloadDialog.initialize(getString(R.string.download), chipGroup);
    }

    private void serverSwitcherDialog() {
        AnikumiiDialog changeServer = new AnikumiiDialog(this);

        ChipGroup chipGroup = new ChipGroup(this);
        chipGroup.setPadding(30, 30, 30, 30);
        chipGroup.setSingleSelection(true);

        Chip streamangoChip = new Chip(this);
        streamangoChip.setText(getString(R.string.streamango));
        streamangoChip.setCheckable(true);
        streamangoChip.setTextColor(Color.BLACK);
        streamangoChip.setChipBackgroundColor(ColorStateList.valueOf(Color.WHITE));
        chipGroup.addView(streamangoChip);

        Chip zippyshareChip = new Chip(this);
        zippyshareChip.setText(getString(R.string.zippyshare));
        zippyshareChip.setCheckable(true);
        zippyshareChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#fffdd1")));
        zippyshareChip.setTextColor(Color.BLACK);
        chipGroup.addView(zippyshareChip);

        Chip mediafireChip = new Chip(this);
        mediafireChip.setText(getString(R.string.mediafire));
        mediafireChip.setCheckable(true);
        mediafireChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#0077ff")));
        mediafireChip.setTextColor(Color.BLACK);
        chipGroup.addView(mediafireChip);

        if (serverOptionDefault != null) {
            streamangoChip.setChecked(serverOptionDefault.equals("Streamango"));
            zippyshareChip.setChecked(serverOptionDefault.equals("Zippyshare"));
            mediafireChip.setChecked(serverOptionDefault.equals("MediaFire"));
        }

        chipGroup.setOnCheckedChangeListener((ChipGroup group, int checkedId) -> {
            if (checkedId != -1) {
                serverOptionDefault = (String) ((Chip) group.findViewById(checkedId)).getText();
                setReactive(getIntent());
                changeServer.dismiss();
            }
        });

        changeServer.initialize(getString(R.string.changeServer), chipGroup);
    }

   /* private void seenHelper(byte action) {
        Intent service = new Intent(this, AnimeFlvApiService.class);
        service.putExtra("toLoad", "https://animeflv.net/api/animes/markEpisode").putExtra("params", "seen=" + action + "&anime_id=" + animeID + "&number=" + episode);
        startService(service);
    }*/

    private void countDown() {
        final int duration = anikumiiVideoView.getDuration();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                remainingTime.setText(dateFormatter(duration - anikumiiVideoView.getCurrentPosition()));
                mSeekbar.setProgress(anikumiiVideoView.getCurrentPosition());

                if (anikumiiVideoView.isPlaying()) {
                    handler.postDelayed(this, 1000);

                } else
                    handler.removeCallbacksAndMessages(this);
            }
        }, 1000);
    }

    private void setAnimation(View v, int animation) {
        v.startAnimation(AnimationUtils.loadAnimation(this, animation));
    }

    private void videoHelper(String url, String number) {
        Intent videoAct = new Intent(this, VPlayerActivity.class);
        videoAct.putExtra("chapterUrl", url).putExtra("chapterTitle", animeName).putExtra("chapterNumber", number).putExtra("serverOption", serverOptionDefault)/*.putExtra("natsukiHosters", natsukiHosters - 1)*/;
        setReactive(videoAct);
    }

    private String dateFormatter(long ms) {
        date.setTime(ms);
        return simpleDateFormat.format(date);
    }
}