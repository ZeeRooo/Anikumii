package com.zeerooo.anikumii2.activities;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zeerooo.anikumii2.Anikumii;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiDialog;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiSharedPreferences;
import com.zeerooo.anikumii2.fragments.NewAccountFragment;
import com.zeerooo.anikumii2.services.AccountsService;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    private TextInputEditText mPasswordView, mEmailView;
    private View mProgressView;
    private WebView webView = null;
    private boolean myanimelist;
    private AnikumiiSharedPreferences anikumiiSharedPreferences;
    private FloatingActionButton logInFacebook;
    private TextInputLayout emailInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myanimelist = getIntent().getBooleanExtra("startMAL", false);

        emailInputLayout = findViewById(R.id.text_input_email);
        mEmailView = findViewById(R.id.email);

        webView.setWillNotDraw(true);

        logInFacebook = findViewById(R.id.log_in_facebook);
        logInFacebook.setOnClickListener((View view) -> {
            final Dialog dialog = new Dialog(LoginActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            // dialog.setTitle("Iniciar sesión con Facebook");

            FrameLayout layout = new FrameLayout(LoginActivity.this);

            final ProgressBar progressBar = new ProgressBar(LoginActivity.this);
            progressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.circular_progress));

            webView.loadUrl("https://animeflv.net/auth/facebook/sign_in");
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    progressBar.setVisibility(View.GONE);
                    if (url.contains("https://animeflv.net/")) {
                        setCookie(CookieManager.getInstance().getCookie(url));

                        startService(new Intent(LoginActivity.this, AccountsService.class).putExtra("startMain", true));

                        dialog.dismiss();
                        //   questionDialog("AnimeFLV", false);
                    }
                }
            });

            layout.addView(webView);
            layout.addView(progressBar);
            dialog.setContentView(layout);
            dialog.show();
        });

        RadioButton malBtn = findViewById(R.id.malBtn);
        malBtn.setChecked(myanimelist);
        malBtn.setOnCheckedChangeListener((CompoundButton compoundButton, boolean b) -> {
            myanimelist = b;
            if (b) {
                logInFacebook.hide();
                emailInputLayout.setHint("Nombre de usuario");
            } else {
                logInFacebook.show();
                emailInputLayout.setHint(getString(R.string.prompt_email));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                circleReveal(malBtn);
        });

        mPasswordView = findViewById(R.id.password);

        FloatingActionButton logIn = findViewById(R.id.log_in_button);
        logIn.setOnClickListener((View view) -> attemptLogin());

        Button sign_up = findViewById(R.id.create_an_account);
        sign_up.setOnClickListener((View view) -> {
            if (!myanimelist) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    circleReveal(sign_up);

                Bundle bundle = new Bundle();
                bundle.putBoolean("myanimelist", myanimelist);
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                NewAccountFragment fragment = new NewAccountFragment();
                fragment.setArguments(bundle);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.act_login_rootView, fragment);
                fragmentTransaction.commit();
                if (getSupportActionBar() != null) {
                    //  getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.newAccountBackground)));
                    getSupportActionBar().setTitle(getString(R.string.new_account));
                }
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://myanimelist.net/register.php")));
                finish();
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (myanimelist) {
            logInFacebook.hide();
            emailInputLayout.setHint("Nombre de usuario");
        } else {
            logInFacebook.show();
            emailInputLayout.setHint(getString(R.string.prompt_email));
            ((RadioButton) findViewById(R.id.animeflvBtn)).setChecked(true);
        }

        webView = new WebView(this);
        webView.getSettings().setUserAgentString(Anikumii.userAgent);
        if (Build.VERSION.SDK_INT >= 19)
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        else
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        anikumiiSharedPreferences = new AnikumiiSharedPreferences(this);

        mPasswordView.setOnEditorActionListener((TextView textView, int id, KeyEvent keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            } else
                return false;
        });

        mProgressView = findViewById(R.id.login_progress);
    }

    private void setCookie(String cookie) {
        ((Anikumii) getApplicationContext()).setUserCookie(cookie.substring(cookie.indexOf("PHPSESSID=") + 10).split(";")[0] + ";" + cookie.substring(cookie.indexOf("login=") + 6).split(";")[0] + ";" + cookie.substring(cookie.indexOf("__cfduid=") + 9).split(";")[0]);
        if (cookie.contains("cf_clearance"))
            ((Anikumii) getApplicationContext()).setCloudFlare(cookie.substring(cookie.indexOf("cf_clearance=") + 13).split(";")[0]);
    }

    @TargetApi(21)
    private void circleReveal(View btn) {
        View rootView = findViewById(R.id.act_login_rootView);
        int endRadius = (int) Math.hypot(rootView.getWidth(), rootView.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(rootView, (int) btn.getX(), (int) btn.getY(), 20, endRadius);
        anim.setDuration(400);

        anim.start();
    }

    public void lostPasswordOnClick(View v) {
        AnikumiiDialog lostDialog = new AnikumiiDialog(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(15, 25, 15, 15);


        final TextInputEditText userEditText = new TextInputEditText(this);
        userEditText.setHint("Nombre de usuario");
        if (myanimelist) {
            linearLayout.addView(userEditText);
        }

        final TextInputEditText emailEditText = new TextInputEditText(this);
        emailEditText.setHint("Correo electrónico");
        emailEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        linearLayout.addView(emailEditText);

        lostDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Enviar", (DialogInterface dialogInterface, int i) -> {
            String email = emailEditText.getText().toString();
            String user = userEditText.getText().toString();
            if (!email.isEmpty() && email.contains("@")) {
                if (!myanimelist)
                    new UserLoginTask().execute(new String[]{email, user, "https://animeflv.net/auth/password/new"});
                else
                    webWorker(user, email, "https://myanimelist.net/password.php");
            } else
                Snackbar.make(findViewById(R.id.act_login_rootView), getString(R.string.error_invalid_email), Snackbar.LENGTH_LONG).show();
        });

        lostDialog.initialize("Recuperar la contraseña", linearLayout);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email) && !myanimelist) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid password address.
        if (TextUtils.isEmpty(email)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(email)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            showProgress(true);
            if (!myanimelist) {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                webWorker(email, password, "https://animeflv.net/auth/sign_in");
            } else
                webWorker(email, password, "https://myanimelist.net/login.php?from=%2F");
            //new UserLoginTask().execute(new String[]{email, password});
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.login_error_cardview).setVisibility(View.GONE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            super.onBackPressed();
        else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
                getSupportActionBar().setTitle("Iniciar sesión");
            }
            getSupportFragmentManager().popBackStack();
        }
    }

    private void webWorker(final String param1, final String param2, String url) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavaScriptInterface(), "HTMLOUT");
        webView.setWebViewClient(new WebViewClient() {
            byte times = 0;

            @Override
            public void onPageFinished(WebView view, final String url) {
                super.onPageFinished(view, url);
                if (!myanimelist) {
                    if (url.equals("https://animeflv.net/")) {
                            setCookie(CookieManager.getInstance().getCookie(url));

                            startService(new Intent(LoginActivity.this, AccountsService.class).putExtra("startMain", true));

                            //questionDialog("MyAnimeList", true);
                            // new UserLoginTask().execute(new String[]{CookieManager.getInstance().getCookie(url)});
                        } else {
                            view.loadUrl("javascript:(function(){document.getElementById('inputEmail').value='" + param1 + "'})()");
                            view.loadUrl("javascript:(function(){document.getElementById('inputPassword').value='" + param2 + "'})()");
                            view.loadUrl("javascript:(function(){document.getElementsByClassName('btn btn-primary btn-block')[0].click()})()");
                            if (times > 2) {
                                findViewById(R.id.login_error_cardview).setVisibility(View.VISIBLE);
                                view.stopLoading();
                            }
                        }
                    } else {
                        switch (url) {
                            case "https://myanimelist.net/":
                                anikumiiSharedPreferences.edit().putString("MALuserName", param1).apply();
                                view.loadUrl("javascript:window.HTMLOUT.processHTML(document.getElementsByName('csrf_token')[0].getAttribute('content'));");

                                //new UserLoginTask().execute(new String[]{CookieManager.getInstance().getCookie(url), param1});
                                break;
                            case "https://myanimelist.net/login.php?from=%2F":
                                view.loadUrl("javascript:(function(){document.getElementById('loginUserName').value='" + param1 + "'})()");
                                view.loadUrl("javascript:(function(){document.getElementById('login-password').value='" + param2 + "'})()");
                                view.loadUrl("javascript:(function(){document.getElementsByClassName('inputButton btn-form-submit')[0].click()})()");
                                if (times > 2) {
                                    findViewById(R.id.login_error_cardview).setVisibility(View.VISIBLE);
                                    view.stopLoading();
                                }
                                break;
                            default:
                                view.loadUrl("javascript:(function(){document.getElementsByName('user_name')[0].value='" + param1 + "'})()");
                                view.loadUrl("javascript:(function(){document.getElementsByName('email')[0].value='" + param2 + "'})()");
                                view.loadUrl("javascript:(function(){document.getElementsByName('submit')[0].click()})()");
                                Snackbar.make(findViewById(R.id.act_login_rootView), "Instrucciones enviadas por correo", Snackbar.LENGTH_LONG).show();
                                finish();
                                if (times > 2)
                                    view.stopLoading();
                                break;
                        }
                    }
                    times++;
                }
            });
        webView.loadUrl(url);
        }

        @Override
        public void onDestroy () {
            super.onDestroy();

            if (webView != null) {
                webView.clearCache(true);
                webView.clearHistory();
                webView.removeAllViews();
                webView.destroy();
            }
        }

        private class JavaScriptInterface {
            @JavascriptInterface
            public void processHTML(String token) {
                StringBuilder cookiesBuilder = new StringBuilder();
                String[] strings = CookieManager.getInstance().getCookie("https://myanimelist.net/").split(";");
                for (int i = 0; i < strings.length; i++) {
                    if (strings[i].contains("MALHLOGSESSID") || strings[i].contains("MALSESSIONID"))
                        cookiesBuilder.append(strings[i]).append(";");
                }
                cookiesBuilder.append("&csrf_token=").append(token);
                anikumiiSharedPreferences.encrypt("mal", cookiesBuilder.toString());

                startService(new Intent(LoginActivity.this, AccountsService.class).putExtra("startMain", true));
            }
        }

        /**
         * Represents an asynchronous login/registration task used to authenticate
         * the user.
         */
        private class UserLoginTask extends AsyncTask<String[], Void, Byte> {

            @Override
            protected Byte doInBackground(String[]... array) {
                byte success = 0;
                try {
                    //  if (!myanimelist) {
                   /* if (array[0].length == 1) {
                        Map<String, String> cookies = new HashMap<>();
                        cookies.put("__cfduid", array[0][0].substring(array[0][0].indexOf("__cfduid=") + 9).split(";")[0]);
                        cookies.put("PHPSESSID", array[0][0].substring(array[0][0].indexOf("PHPSESSID=") + 10).split(";")[0]);
                        cookies.put("login", array[0][0].substring(array[0][0].indexOf("login=") + 6).split(";")[0]);

                        anikumiiSharedPreferences.encrypt("UserCookie", cookies.get("phpssid") + ";" + cookies.get("login") + ";" + cookies.get("__cfduid"));*/
                    // } else {
                    Jsoup.connect("https://animeflv.net/auth/password/new")
                            .userAgent(Anikumii.userAgent)
                            .data("email", array[0][0])
                            .method(Connection.Method.POST)
                            .execute();
                    return 1;
                    //    }
                    // } else {
                  /*  HttpsURLConnection connection = (HttpsURLConnection) new URL("https://myanimelist.net/search/prefix.json?type=user&keyword=" + array[0][1]).openConnection();
                    connection.setRequestProperty("user-agent", "Mozilla/5.0 (X11; Linux x86_64; rv:63.0) Gecko/20100101 Firefox/63.0");
                    connection.setRequestMethod("GET");
                    connection.setUseCaches(false);

                    success = connection.getResponseCode();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    JSONObject jsonObject = new JSONObject(stringBuilder.toString()).getJSONArray("categories").getJSONObject(0).getJSONArray("items").getJSONObject(0);
                    mPreferences.edit().putString("MALuserName", jsonObject.getString("name")).apply();
                    mPreferences.edit().putString("MALuserAvatar", jsonObject.getString("image_url")).apply();

                    bufferedReader.close();

                    StringBuilder cookiesBuilder = new StringBuilder();
                    String[] strings = array[0][0].split(";");
                    for (int i = 0; i < strings.length; i++) {
                        if (strings[i].contains("MALHLOGSESSID") || strings[i].contains("MALSESSIONID"))
                            cookiesBuilder.append(strings[i]).append(";");
                    }
                    cookiesBuilder.append("&csrf_token=").append("");

                    anikumiiSharedPreferences.encrypt("mal", cookiesBuilder.toString());*/
                    //connection.disconnect();
                    //  }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return success;
            }

            @Override
            protected void onPostExecute(final Byte success) {
                showProgress(false);

                if (success == 1)
                    Snackbar.make(findViewById(R.id.act_login_rootView), "Instrucciones enviadas por correo", Snackbar.LENGTH_LONG).show();
                else
                    findViewById(R.id.login_error_cardview).setVisibility(View.VISIBLE);
            }

            @Override
            protected void onCancelled() {
                showProgress(false);
            }
        }
    }