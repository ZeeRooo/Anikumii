package com.zeerooo.anikumii.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.app.PictureInPictureParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
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
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.htetznaing.xgetter.XGetter;
import com.zeerooo.anikumii.Anikumii;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiBottomSheetDialog;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiSharedPreferences;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiUiHelper;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiVideoView;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiWebHelper;
import com.zeerooo.anikumii.misc.DataBaseHelper;
import com.zeerooo.anikumii.misc.MyAnimeListModel;
import com.zeerooo.anikumii.misc.ServerHelper;
import com.zeerooo.anikumii.misc.Utils;

import org.jsoup.nodes.Element;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ZeeRooo on 09/03/18
 */

public class VideoPlayerActivity extends AppCompatActivity {

    private AnikumiiVideoView anikumiiVideoView;
    private int primaryProgress, secondaryProgressMax;
    private short episode = -1;
    private boolean isLogedIn, enablePip;
    private RelativeLayout mButtonsHeader;
    private SeekBar mSeekbar;
    private TextView remainingTime;
    private String url, animeName, nextUrl, prevUrl, episodes, rawUrl, serverOptionDefault;
    private BottomSheetBehavior mBottomSheetBehavior;
    private ImageButton previous, next, pause;
    private Handler handler = new Handler(), headerHandler = new Handler();
    private ServerHelper serverHelper;
    private AnikumiiSharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private SimpleDateFormat simpleDateFormat;
    private Date date;
    private BroadcastReceiver networkReceiver = null;
    private XGetter xGetter;
    private MyAnimeListModel myAnimeListModel;
    private ConnectivityManager connectivityManager;

    @SuppressWarnings("deprecation")
    private boolean isConnected() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        xGetter = new XGetter();

        sharedPreferences = new AnikumiiSharedPreferences(this);

        setReactive(getIntent());

        setSupportActionBar(findViewById(R.id.videoToolbar));

        anikumiiVideoView = findViewById(R.id.video_view);

        progressBar = findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);

        mButtonsHeader = findViewById(R.id.buttons_header);

        mSeekbar = findViewById(R.id.progress);
        mSeekbar.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        remainingTime = findViewById(R.id.remainingTime);
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

        anikumiiVideoView.setOnPreparedListener(mp -> {
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

            pause.setOnClickListener(v -> {
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

            mp.setOnBufferingUpdateListener((MediaPlayer mediaPlayer, int percent) -> mSeekbar.setSecondaryProgress(percent * secondaryProgressMax));
        });

        bottomSheetSetup();

        enablePip = sharedPreferences.getBoolean("enablePip", true);

        serverHelper = new ServerHelper();

        isLogedIn = !sharedPreferences.getString("malUserName", "").equals("");

        anikumiiVideoView.setOnErrorListener((MediaPlayer mp, int what, int extra) -> {
            progressBar.setVisibility(View.VISIBLE);
            primaryProgress = mp.getCurrentPosition();

            if (networkReceiver == null) {
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                networkReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (!intent.getBooleanExtra("isConnected", true) || (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT && !isConnected())) {

                            countDown();
                            AnikumiiUiHelper.errorSnackbar(anikumiiVideoView, Snackbar.LENGTH_LONG, "UnknownHostException", null).show();
                            pause.setImageResource(android.R.drawable.ic_media_play);

                            anikumiiVideoView.pause();
                        } else {
                            anikumiiVideoView.resume();

                            resumeVideo();
                        }
                    }
                };

                IntentFilter filter = new IntentFilter("com.zeerooo.anikumii.Broadcast");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Intent intent = new Intent();
                    intent.setAction("com.zeerooo.anikumii.Broadcast");

                    countDown();
                    AnikumiiUiHelper.errorSnackbar(anikumiiVideoView, Snackbar.LENGTH_LONG, "UnknownHostException", null).show();
                    pause.setImageResource(android.R.drawable.ic_media_play);
                    anikumiiVideoView.pause();

                    NetworkRequest.Builder builder = new NetworkRequest.Builder();
                    connectivityManager.registerNetworkCallback(builder.build(), new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(@NonNull Network network) {
                            intent.putExtra("isConnected", true);
                            sendBroadcast(intent);
                        }

                        @Override
                        public void onLost(@NonNull Network network) {
                            intent.putExtra("isConnected", false);
                            sendBroadcast(intent);
                        }
                    });
                } else {
                    filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                }

                registerReceiver(networkReceiver, filter);
            }

            return true;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (isPlaying())
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                DownloadManager mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

                final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setDestinationInExternalPublicDir("/Anikumii!!/" + animeName, episode + ".mp4");
                request.setVisibleInDownloadsUi(true);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                mDownloadManager.enqueue(request);

                final Snackbar snackbar = Snackbar.make(findViewById(R.id.videoPlayerRootView), getString(R.string.snackback_download_confirmation, animeName), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(getString(android.R.string.ok), view -> snackbar.dismiss());
                snackbar.show();
            } else
                AnikumiiUiHelper.errorSnackbar(findViewById(R.id.videoPlayerRootView), Snackbar.LENGTH_LONG, "permission_denied", null).show();
    }

    private boolean isPlaying() {
        if (anikumiiVideoView.getCurrentPosition() > 0)
            return true;
        else {
            Snackbar.make(findViewById(R.id.videoPlayerRootView), getString(R.string.unload_video), Snackbar.LENGTH_LONG).show();
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.episodesList:
                startActivity(new Intent(this, EpisodesActivity.class).putExtra("animeUrl", Anikumii.dominium + episodes).putExtra("animeName", animeName));
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

    private String splitString(String string) {
        return string.split("\\?")[0];
    }

    private void setReactive(final Intent intent) {
        if (serverHelper != null && serverHelper.isAlive())
            serverHelper.stop();

        if (intent.getDataString() == null)
            rawUrl = intent.getStringExtra("chapterUrl");
        else
            rawUrl = splitString(intent.getDataString());

        episode = Short.parseShort(Utils.matcher(rawUrl, "(\\d+)\\D*$"));

        Observable
                .just(true)
                .observeOn(Schedulers.io())
                .doOnNext(aBoolean -> networkRequest(intent))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    if (!isDestroyed())
                        setVideoStuff();
                })
                .observeOn(Schedulers.io())
                .doOnNext(a -> {
                    myAnimeListModel = new MyAnimeListModel(Utils.encodeString(Utils.removeLastNumberAndSpace(animeName).split("\\(")[0]), sharedPreferences.getString("malUserName", null), false);

                    String type;
                    if (Anikumii.dominium.startsWith("https://tioanime.com"))
                        type = "https://tioanime.com/anime/";
                    else
                        type = "https://tiohentai.com/hentai/";

                    DataBaseHelper dataBaseHelper = new DataBaseHelper(this);

                    Cursor cursor = null;

                    int position = 0, databaseRows = dataBaseHelper.getDatabaseRows(cursor);

/*

                    Cursor cursor = dataBaseHelper.getReadableDatabase().rawQuery("SELECT POSITION FROM AnimesDB WHERE TITLE LIKE ?", new String[]{animeName});

                    if (cursor.moveToLast())
                        position = (short) cursor.getInt(0);
                    else {
                        cursor = dataBaseHelper.getReadableDatabase().rawQuery("SELECT POSITION FROM AnimesDB", null);
                        cursor.moveToLast();

                        if (cursor.getCount() == 0)
                            position = 0;
                        else
                            position = (short) (cursor.getCount() + 1);
                    }*/

                    if (databaseRows != 0) {
                        cursor = dataBaseHelper.getReadableDatabase().rawQuery("SELECT POSITION FROM AnimesDB WHERE TITLE LIKE ?", new String[]{animeName});

                        if (cursor.moveToLast())
                            position = cursor.getInt(0);
                        else
                            position = databaseRows + 1;

                        cursor.close();
                    }

                    try {
                        dataBaseHelper.addData(animeName, myAnimeListModel.getType(), type + Utils.matcher(rawUrl, "/ver/(.*)-"), myAnimeListModel.getImage(), Calendar.getInstance().getTime().toString(), episode, position);
                    } catch (NullPointerException npe) {
                        Snackbar.make(anikumiiVideoView, getResources().getString(R.string.jikan_exception) + ". No se agregó al historial.", Snackbar.LENGTH_LONG).show();
                    } finally {
                        dataBaseHelper.close();
                    }
                })
                .subscribe(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        AnikumiiUiHelper.errorSnackbar(anikumiiVideoView, Snackbar.LENGTH_INDEFINITE, "videoPlayer", view -> {
                            serverSwitcherDialog();
                            AnikumiiUiHelper.snackbar.dismiss();
                        }).show();
                    }

                    @Override
                    public void onComplete() {
                        if (isLogedIn) {
                            myAnimeListModel.setSeenEpisodes(episode);
                            myAnimeListModel.apiHandler(VideoPlayerActivity.this, (byte) 1);
                        }
                        dispose();
                    }
                });
    }

    private void networkRequest(Intent intent) throws IOException, StringIndexOutOfBoundsException {
        Element controls = AnikumiiWebHelper.go(rawUrl, this).get();

        animeName = Utils.removeLastNumberAndSpace(controls.getElementsByClass("anime-title text-center mb-4").text());

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
                connect = controls.selectFirst(".text-center > a[href~=stream]").attr("href");
                url = xGetter.fruits(connect);
                break;
            case "Zippyshare":
                connect = controls.selectFirst(".text-center > a[href~=zippyshare]").attr("href");
                url = xGetter.zippyshare(connect);
                break;
            case "ok.ru":
                connect = controls.selectFirst("script:containsData(var videos)").toString();
                url = xGetter.okru(Utils.matcher(connect, "\"Okru\",\"(.*?)\"").replace("\\", ""));
                break;
        }

        episodes = controls.select("span.btn:nth-child(2) > a:nth-child(1)").attr("href");
        prevUrl = controls.select("span.btn:nth-child(1) > a:nth-child(1)").attr("href");
        nextUrl = controls.select("span.btn:nth-child(3) > a:nth-child(1)").attr("href");
    }

    private void setVideoStuff() {
        anikumiiVideoView.setVideoURI(Uri.parse(url));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(animeName);
            getSupportActionBar().setSubtitle("Episodio " + episode);
        }

        primaryProgress = 0;

        if (!prevUrl.equals("#")) {
            previous.setVisibility(View.VISIBLE);
            previous.setOnClickListener(v -> setReactive(new Intent(this, VideoPlayerActivity.class).putExtra("chapterUrl", Anikumii.dominium + prevUrl).putExtra("serverOption", serverOptionDefault)));
        } else
            previous.setVisibility(View.INVISIBLE);

        if (!nextUrl.equals("#")) {
            next.setVisibility(View.VISIBLE);
            next.setOnClickListener(v -> setReactive(new Intent(this, VideoPlayerActivity.class).putExtra("chapterUrl", Anikumii.dominium + nextUrl).putExtra("serverOption", serverOptionDefault)));
        } else
            next.setVisibility(View.INVISIBLE);
    }

    private void bottomSheetSetup() {
        ImageButton changeServer = findViewById(R.id.changeServer);
        AnikumiiUiHelper.transparentBackground(changeServer);
        TooltipCompat.setTooltipText(changeServer, getString(R.string.changeServer));
        changeServer.setOnClickListener(view -> {
            serverSwitcherDialog();
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        ImageButton download = findViewById(R.id.downloadEpisode);
        AnikumiiUiHelper.transparentBackground(download);
        TooltipCompat.setTooltipText(download, getString(R.string.download));
        download.setOnClickListener(view -> {
            if (isPlaying())
                ActivityCompat.requestPermissions(VideoPlayerActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            //downloadDialog();

            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        ImageButton stream = findViewById(R.id.streamButtom);
        AnikumiiUiHelper.transparentBackground(stream);
        TooltipCompat.setTooltipText(stream, getString(R.string.bottom_sheet_stream));
        stream.setOnClickListener(view -> {
            if (isPlaying()) {
                serverHelper.stop();
                serverHelper.setUrl(url);
                serverHelper.setTitle(animeName + " Episodio " + episode + " - Anikumii!!");
                try {
                    serverHelper.start();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

                View sheetView = getLayoutInflater().inflate(R.layout.video_player_stream_bottom_sheet, null);
                final BottomSheetDialog dialog = new BottomSheetDialog(VideoPlayerActivity.this);
                dialog.setContentView(sheetView);

                ((TextView) sheetView.findViewById(R.id.ipText)).setText(serverHelper.getMyIp(VideoPlayerActivity.this));

                Button disconnect = sheetView.findViewById(R.id.disconnectStream);
                disconnect.setOnClickListener(v -> {
                    serverHelper.stop();
                    dialog.dismiss();
                });

                dialog.show();
            }
        });
    }

  /*  private void downloadDialog() {
        AnikumiiBottomSheetDialog downloadDialog = new AnikumiiBottomSheetDialog(this);

       // downloadDialog.setMessage(getString(R.string.downloadMessage));
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);

        TextView messageTextView = new TextView(this);
        messageTextView.setText(getString(R.string.downloadMessage));
        linearLayout.addView(messageTextView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

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
                    ActivityCompat.requestPermissions(VideoPlayerActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                else
                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)));

                downloadDialog.dismiss();
            }
        });

        linearLayout.addView(chipGroup, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        downloadDialog.initialize(getString(R.string.download), linearLayout);
    }*/

    private void serverSwitcherDialog() {
        AnikumiiBottomSheetDialog changeServer = new AnikumiiBottomSheetDialog(this);
        changeServer.serverDialog(serverOptionDefault).setOnCheckedChangeListener((ChipGroup group, int checkedId) -> {
            if (checkedId != -1) {
                serverOptionDefault = ((Chip) group.findViewById(checkedId)).getText().toString();
                setReactive(getIntent());
                changeServer.dismiss();
            }
        });
    }

    private void countDown() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                remainingTime.setText(dateFormatter(anikumiiVideoView.getDuration() - anikumiiVideoView.getCurrentPosition()));
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

    private String dateFormatter(long ms) {
        date.setTime(ms);
        return simpleDateFormat.format(date);
    }
}