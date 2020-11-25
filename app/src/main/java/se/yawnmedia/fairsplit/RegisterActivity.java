package se.yawnmedia.fairsplit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private FairSplit app;
    private UserRegisterTask registerTask;
    private User registeredUser;
    private ArrayList<Group> groups = new ArrayList<>();

    // UI references.
    private EditText loginNameView;
    private EditText passwordView;
    private EditText passwordAgainView;
    private View progressView;
    private View registerFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = ((FairSplit) this.getApplication());
        app.setupAppPrefs(this);
        setContentView(R.layout.activity_register);

        loginNameView = findViewById(R.id.login_name);
        passwordView = findViewById(R.id.password);
        passwordAgainView = findViewById(R.id.password_again);

        Button mRegisterButton = findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        registerFormView = findViewById(R.id.app_register_form);
        progressView = findViewById(R.id.register_progress);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    }

    private void attemptRegister() {
        if (registerTask != null) {
            return;
        }

        // Reset errors.
        loginNameView.setError(null);
        passwordView.setError(null);
        passwordAgainView.setError(null);

        // Store values at the time of the loginName attempt.
        String loginName = loginNameView.getText().toString();
        String password = passwordView.getText().toString();
        String passwordAgain = passwordAgainView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(loginName)) {
            loginNameView.setError(getString(R.string.error_field_required));
            focusView = loginNameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(passwordAgain)) {
            passwordAgainView.setError(getString(R.string.error_field_required));
            focusView = passwordAgainView;
            cancel = true;
        }

        if (!password.equals(passwordAgain)) {
            passwordAgainView.setError(getString(R.string.passwords_not_matching));
            focusView = passwordAgainView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt loginName and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user loginName attempt.
            showProgress(true);
            registerTask = new UserRegisterTask(loginName, password, passwordAgain);
            registerTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the loginName form.
     */
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        registerFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
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

    /**
     * Represents an asynchronous loginName/registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String loginName;
        private final String password;
        private final String passwordAgain;
        private String errorMessage;

        UserRegisterTask(String loginName, String password, String passwordAgain) {
            this.loginName = loginName;
            this.password = password;
            this.passwordAgain = passwordAgain;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject response;
            try {
                JSONObject registerData = new JSONObject();
                registerData.put("userName", loginName);
                registerData.put("password", password);
                registerData.put("passwordAgain", passwordAgain);
                response = RESTHelper.POST("/user/register", registerData, null, RegisterActivity.this);
                if (response.has("errorCode") && (int) response.get("errorCode") != 0) {
                    Snackbar.make(registerFormView, response.get("message").toString(), Snackbar.LENGTH_LONG).show();
                    return false;
                } else {
                    JSONObject responseUser = response.getJSONArray("data").getJSONObject(0);
                    registeredUser = new User(responseUser, RegisterActivity.this);

                    errorMessage = RESTHelper.populateOtherData(app, registeredUser, RegisterActivity.this);
                    if (errorMessage != null) {
                        Snackbar.make(registerFormView, errorMessage, Snackbar.LENGTH_LONG).show();
                        return false;
                    }
                }
            } catch (Exception ex) {
                Snackbar.make(registerFormView, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                return false;
            }

            app.setLoginName(loginName);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            registerTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                passwordView.setError(getString(R.string.error_incorrect_password));
                passwordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            registerTask = null;
            showProgress(false);
        }
    }
}

