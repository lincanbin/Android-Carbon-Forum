package com.lincanbin.carbonforum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lincanbin.carbonforum.application.CarbonForumApplication;
import com.lincanbin.carbonforum.config.APIAddress;
import com.lincanbin.carbonforum.tools.VerificationCode;
import com.lincanbin.carbonforum.util.HttpUtil;
import com.lincanbin.carbonforum.util.JSONUtil;
import com.lincanbin.carbonforum.util.MD5Util;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A register screen that offers register via email/password.
 */
public class RegisterActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the register task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mAuthTask = null;

    // UI references.
    private Toolbar mToolbar;
    private EditText mUsernameView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mVerificationCodeView;
    private ImageView mVerificationCodeImageView;
    private View mProgressView;
    private View mRegisterFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.title_activity_register);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mUsernameView = (EditText) findViewById(R.id.username);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mVerificationCodeView = (EditText) findViewById(R.id.verification_code);
        mVerificationCodeView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });
        mVerificationCodeImageView = (ImageView)  findViewById(R.id.verification_code_img);
        mVerificationCodeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshVerificationCode();
            }
        });
        refreshVerificationCode();
        
        mPasswordView = (EditText) findViewById(R.id.password);

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);
    }
    
    private void refreshVerificationCode(){
        //接口回调的方法，完成验证码的异步读取与显示
        VerificationCode verificationCodeImage = new VerificationCode(this);
        verificationCodeImage.loadImage(new VerificationCode.ImageCallBack() {
            @Override
            public void getDrawable(Drawable drawable) {
                mVerificationCodeImageView.setImageDrawable(drawable);
            }
        });
    }
    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the register form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual register attempt is made.
     */
    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mVerificationCodeView.setError(null);

        // Store values at the time of the register attempt.
        String username = mUsernameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String verification_code = mVerificationCodeView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid verification code.
        if (TextUtils.isEmpty(verification_code)) {
            mVerificationCodeView.setError(getString(R.string.error_field_required));
            focusView = mVerificationCodeView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user register attempt.
            showProgress(true);
            mAuthTask = new UserRegisterTask(username, email, password, verification_code);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 5;
    }

    /**
     * Shows the progress UI and hides the register form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous register/registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, JSONObject> {

        private final Map<String, String> parameter = new HashMap<>();

        UserRegisterTask(String username, String email, String password, String verification_code) {
            parameter.put("UserName", username);
            parameter.put("Email", email);
            parameter.put("Password", MD5Util.md5(password));
            parameter.put("VerifyCode", verification_code);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            return HttpUtil.postRequest(RegisterActivity.this, APIAddress.REGISTER_URL, parameter, true, false);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            mAuthTask = null;
            showProgress(false);
            if(result !=null) {
                try {
                    //Log.v("JSON", result.toString());
                    if (result.getInt("Status") == 1) {
                        //Log.v("JSON", result.toString());

                        SharedPreferences.Editor editor = CarbonForumApplication.userInfo.edit();
                        editor.putString("UserID", result.getString("UserID"));
                        editor.putString("UserExpirationTime", result.getString("UserExpirationTime"));
                        editor.putString("UserCode", result.getString("UserCode"));

                        JSONObject userInfo =  JSONUtil.jsonString2Object(result.getString("UserInfo"));
                        if(userInfo!=null){
                            editor.putString("UserName", userInfo.getString("UserName"));
                            editor.putString("UserRoleID", userInfo.getString("UserRoleID"));
                            editor.putString("UserMail", userInfo.getString("UserMail"));
                            editor.putString("UserIntro", userInfo.getString("UserIntro"));
                        }
                        editor.apply();
                        //发送广播刷新
                        Intent intent = new Intent();
                        intent.setAction("action.refreshDrawer");
                        sendBroadcast(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, result.getString("ErrorMessage"), Toast.LENGTH_SHORT).show();
                        refreshVerificationCode();
                        switch(result.getInt("ErrorCode")){
                            case 104001:
                            case 104002:
                            case 104005:
                                mUsernameView.setError(result.getString("ErrorMessage"));
                                mUsernameView.requestFocus();
                                break;
                            case 104003:
                                mEmailView.setError(result.getString("ErrorMessage"));
                                mEmailView.requestFocus();
                                break;
                            case 104004:
                                mVerificationCodeView.setError(result.getString("ErrorMessage"));
                                mVerificationCodeView.requestFocus();
                                break;
                            default:
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Snackbar.make(mRegisterFormView, R.string.network_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

