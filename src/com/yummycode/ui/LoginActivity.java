package com.yummycode.ui;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.yummycode.api.SocialMe;
import com.yummycode.util.LocalCache;

public class LoginActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sml_login);
        
        // Check for Internet connectivity
        if(!isOnline())
        {
            Resources resources = getResources();
            String alertTitle = resources.getString(R.string.internetAccessAlertTitle);
            String alertMessage = resources.getString(R.string.internetAccessAlertMessage);
            
            new AlertDialog.Builder(this)
            .setTitle(alertTitle)
            .setMessage(alertMessage)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    finish();
                }
            })
            .show();
        }

        Button loginButton = (Button) findViewById(R.id.loginLoginButton);
        loginButton.setOnClickListener(loginButtonClick);
        
        Button registerAccountButton = (Button) findViewById(R.id.loginRegisterButton);
        registerAccountButton.setOnClickListener(registerAccountButtonClick);
        
        TextView recoverPasswordLink = (TextView) findViewById(R.id.loginPasswordRecoveryLink);
        recoverPasswordLink.setOnClickListener(recoveryLinkClick);
    }
    
    private OnClickListener loginButtonClick = new OnClickListener()
    {
        public void onClick(View v) {
            EditText etEmail = (EditText) findViewById(R.id.loginEmail);
            EditText etPassword = (EditText) findViewById(R.id.loginPassword);
            
            if(etEmail.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty()) {
                Resources resources = getResources();
                String alertTitle = resources.getString(R.string.formEmptyFieldsTitle);
                String alertMessage = resources.getString(R.string.formEmptyFieldsMessage);
                
                new AlertDialog.Builder(LoginActivity.this)
                .setTitle(alertTitle)
                .setMessage(alertMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                })
                .show();
            }
            else {
                LoginTask loginTask = new LoginTask();
                loginTask.execute();
            }
        }
    };
    
    class LoginTask extends AsyncTask<Void, Void, JSONObject>
    {
        private ProgressDialog progressDialog = null;
        private String email = "";
        private String password = "";
        
        @Override
        protected void onPreExecute()
        {
            EditText etEmail = (EditText) findViewById(R.id.loginEmail);
            email = etEmail.getText().toString();
            
            EditText etPassword = (EditText) findViewById(R.id.loginPassword);
            password = etPassword.getText().toString();
            
            // Show a "Loading..." dialog
            super.onPreExecute();
            Resources resources = getResources();
            String loadingMessage = resources.getString(R.string.loginLoadingMessage);
            
            progressDialog = ProgressDialog.show(LoginActivity.this, null, loadingMessage, true);
        }

        @Override
        protected JSONObject doInBackground(Void... params)
        {
            SocialMe sm = new SocialMe();
            return sm.smLogin(email, password);
        }

        @Override
        protected void onPostExecute(JSONObject response)
        {
            try {
                final String status = response.getString("status");
                
                // Dismiss the "Loading..." dialog
                progressDialog.dismiss();
                
                if(status.equalsIgnoreCase("200")) {                    
                    LocalCache.cacheUser(response);
                    
                    Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                    startActivity(intent);
                }
                else {
                    new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("Error")
                    .setMessage(response.getString("message"))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                            
                            // If the user is already logged in show social map
                            if(status.equalsIgnoreCase("550")) {
                                Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                                startActivity(intent);
                            }
                        }
                    })
                    .show();
                }
            }
            catch (JSONException e) {
                Log.e("SocialMe", "Error parsing JSON response during login");
                e.printStackTrace();
            }
        }
    }

    private OnClickListener registerAccountButtonClick = new OnClickListener() {
        public void onClick(View v) {
            EditText etEmail = (EditText) findViewById(R.id.loginEmail);
            EditText etPassword = (EditText) findViewById(R.id.loginPassword);
            
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            intent.putExtra("action", "register");
            intent.putExtra("email", etEmail.getText().toString());
            intent.putExtra("password", etPassword.getText().toString());
            startActivity(intent);
        }
    };
    
    private OnClickListener recoveryLinkClick = new OnClickListener()
    {
        public void onClick(View v) {
            Intent intent = new Intent(LoginActivity.this, RecoveryActivity.class);
            startActivity(intent);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.sml_login, menu);
        return true;
    }
    
    public boolean isOnline() {
        ConnectivityManager connman = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connman.getActiveNetworkInfo();
        
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        
        return false;
    }
}
