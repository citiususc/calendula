//package es.usc.citius.servando.calendula.activities;
//
//import android.animation.Animator;
//import android.animation.AnimatorListenerAdapter;
//import android.annotation.TargetApi;
//import android.app.Activity;
//import android.content.Intent;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.inputmethod.EditorInfo;
//import android.widget.AutoCompleteTextView;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import org.apache.http.HttpStatus;
//import org.json.JSONObject;
//
//import es.usc.citius.servando.calendula.HomeActivity;
//import es.usc.citius.servando.calendula.R;
//import es.usc.citius.servando.calendula.user.Session;
//import es.usc.citius.servando.calendula.user.User;
//import es.usc.citius.servando.calendula.util.api.ApiLoginResponse;
//import es.usc.citius.servando.calendula.util.api.ApiRequestBuilder;
//
///**
// * A login screen that offers login via email/password.
// */
//public class LoginActivity extends Activity {
//
//    private static final String TAG = LoginActivity.class.getName();
//    /**
//     * Keep track of the login task to ensure we can cancel it if requested.
//     */
//    private UserLoginTask mAuthTask = null;
//
//    // UI references.
//    private AutoCompleteTextView mEmailView;
//    private EditText mPasswordView;
//    private View mProgressView;
//    private View mLoginFormView;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//        // Set up the login form.
//        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
//        //populateAutoComplete();
//
//        mPasswordView = (EditText) findViewById(R.id.password);
//        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == R.id.login || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
//                    return true;
//                }
//                return false;
//            }
//        });
//
//        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
//        mEmailSignInButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                attemptLogin();
//            }
//        });
//
//        mLoginFormView = findViewById(R.id.login_form);
//        mProgressView = findViewById(R.id.login_progress);
//    }
//
//    /**
//     * Attempts to sign in or register the account specified by the login form.
//     * If there are form errors (invalid email, missing fields, etc.), the
//     * errors are presented and no actual login attempt is made.
//     */
//    public void attemptLogin() {
//        if (mAuthTask != null) {
//            return;
//        }
//
//        // Reset errors.
//        mEmailView.setError(null);
//        mPasswordView.setError(null);
//
//        // Store values at the time of the login attempt.
//        String email = mEmailView.getText().toString();
//        String password = mPasswordView.getText().toString();
//
//        boolean cancel = false;
//        View focusView = null;
//
//
//        // Check for a valid password, if the user entered one.
//        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
//            mPasswordView.setError(getString(R.string.error_invalid_password));
//            focusView = mPasswordView;
//            cancel = true;
//        }
//
//        // Check for a valid email address.
//        if (TextUtils.isEmpty(email)) {
//            mEmailView.setError(getString(R.string.error_field_required));
//            focusView = mEmailView;
//            cancel = true;
//        } else if (!isEmailValid(email)) {
//            mEmailView.setError(getString(R.string.error_invalid_email));
//            focusView = mEmailView;
//            cancel = true;
//        }
//
//        if (cancel) {
//            // There was an error; don't attempt login and focus the first
//            // form field with an error.
//            focusView.requestFocus();
//        } else {
//            // Show a progress spinner, and kick off a background task to
//            // perform the user login attempt.
//            showProgress(true);
//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);
//        }
//    }
//
//    private boolean isEmailValid(String email) {
//        //TODO: Replace this with your own logic
//        return email.contains("@");
//    }
//
//    private boolean isPasswordValid(String password) {
//        //TODO: Replace this with your own logic
//        return password.length() > 4;
//    }
//
//    /**
//     * Shows the progress UI and hides the login form.
//     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//    public void showProgress(final boolean show) {
//        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//        // for very easy animations. If available, use these APIs to fade-in
//        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });
//
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mProgressView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
//        } else {
//            // The ViewPropertyAnimator APIs are not available, so simply show
//            // and hide the relevant UI components.
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
//    }
//
//    /**
//     * Represents an asynchronous login/registration task used to authenticate
//     * the user.
//     */
//    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
//
//        private final String mEmail;
//        private final String mPassword;
//
//        private String error = null;
//
//        UserLoginTask(String email, String password) {
//            mEmail = email;
//            mPassword = password;
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            // attempt authentication against server
//            try {
//
//                JSONObject data = new JSONObject();
//                data.put("username", mEmail);
//                data.put("password", mPassword);
//
//                ApiLoginResponse loginResponse = new ApiRequestBuilder()
//                        .to("login")
//                        .withBody(data)
//                        .expect(ApiLoginResponse.class)
//                        .post();
//
//                if (loginResponse.success && loginResponse.data.token != null) {
//                    User user = new User();
//                    user.setEmail(mEmail);
//                    user.setToken(loginResponse.data.token);
//                    Session.instance().create(getApplicationContext(), user);
//                    Log.d(TAG, "Login response : " + loginResponse.status);
//                    return true;
//                } else if (!loginResponse.success && loginResponse.status == HttpStatus.SC_NOT_FOUND) {
//
//                    Log.d(TAG, "Login response: " + loginResponse.status + ", username not found");
//                    // username not found, so register
//
//                    ApiLoginResponse registerResponse = new ApiRequestBuilder()
//                            .to("register")
//                            .withBody(data)
//                            .expect(ApiLoginResponse.class)
//                            .post();
//
//                    if (registerResponse.success && registerResponse.data.token != null) {
//                        User user = new User();
//                        user.setEmail(mEmail);
//                        user.setToken(registerResponse.data.token);
//                        Log.d(TAG, "Register response: " + loginResponse.status);
//                        Session.instance().create(getApplicationContext(), user);
//                        return true;
//                    }
//
//
//                } else if (loginResponse.status == HttpStatus.SC_UNAUTHORIZED) {
//                    // incorrect password
//                    Log.d(TAG, "Login response: " + loginResponse.status + ", incorrect password");
//                    error = getString(R.string.error_incorrect_password);
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                return false;
//            }
//
//            return false;
//        }
//
//        @Override
//
//        protected void onPostExecute(final Boolean success) {
//            mAuthTask = null;
//            showProgress(false);
//
//            if (success) {
//                startActivity(new Intent(getBaseContext(), HomeActivity.class));
//                finish();
//
//            } else {
//                if (error != null) {
//                    mPasswordView.setError(error);
//                    mPasswordView.requestFocus();
//                } else {
//                    Toast.makeText(LoginActivity.this, "Unknown error on login :-(", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//
//        @Override
//        protected void onCancelled() {
//            mAuthTask = null;
//            showProgress(false);
//        }
//    }
//
//}
//
//
//
