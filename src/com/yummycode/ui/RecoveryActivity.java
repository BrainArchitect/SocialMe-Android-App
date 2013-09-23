package com.yummycode.ui;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.yummycode.api.SocialMe;


public class RecoveryActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sml_recovery);
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        Button resetButton = (Button) findViewById(R.id.prResetButton);
        resetButton.setOnClickListener(resetButtonClick);
    }
    
    private OnClickListener resetButtonClick = new OnClickListener()
    {
        public void onClick(View v) {
            EditText etEmail = (EditText) findViewById(R.id.prEmail);
            
            if(etEmail.getText().toString().isEmpty()) {
                Resources resources = getResources();
                String alertTitle = resources.getString(R.string.formEmptyFieldsTitle);
                String alertMessage = resources.getString(R.string.formEmptyFieldsMessage);
                
                new AlertDialog.Builder(RecoveryActivity.this)
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
                ResetTask rt = new ResetTask();
                rt.execute();
            }
        }
    };
    
    class ResetTask extends AsyncTask<Void, Void, JSONObject>
    {
        private ProgressDialog progressDialog = null;
        private String email = "";
        
        @Override
        protected void onPreExecute()
        {
            EditText etEmail = (EditText) findViewById(R.id.prEmail);
            email = etEmail.getText().toString();
            etEmail.setText("");
            
            // Show a "Loading..." dialog
            super.onPreExecute();
            Resources resources = getResources();
            String loadingMessage = resources.getString(R.string.prLoadingMessage);
            
            progressDialog = ProgressDialog.show(RecoveryActivity.this, null, loadingMessage, true);
        }

        @Override
        protected JSONObject doInBackground(Void... params)
        {
            SocialMe sm = new SocialMe();
            return sm.smResetPassword(email);
        }

        @Override
        protected void onPostExecute(JSONObject response)
        {
            try {
                String messageTitle;
                final String status = response.getString("status");
                
                // Dismiss the "Loading..." dialog
                progressDialog.dismiss();
                
                if(status.equalsIgnoreCase("200")) {
                    messageTitle = "Passwrod Reset";
                }
                else {
                    messageTitle = "Error";
                }
                
                new AlertDialog.Builder(RecoveryActivity.this)
                .setTitle(messageTitle)
                .setMessage(response.getString("message"))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        if(status.equalsIgnoreCase("560")) {
                            Intent intent = new Intent(RecoveryActivity.this, MapActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Intent intent = new Intent(RecoveryActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    }
                })
                .show();
            }
            catch (JSONException e) {
                Log.e("SocialMe", "Error parsing JSON response during login");
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.sml_recovery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
