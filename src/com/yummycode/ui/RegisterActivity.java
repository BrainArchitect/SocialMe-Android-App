package com.yummycode.ui;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.yummycode.api.SocialMe;
import com.yummycode.util.LocalCache;


public class RegisterActivity extends Activity
{
    private String action = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sml_register);
        
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        action = getIntent().getExtras().getString("action");
        
        Button registerAccountButton = (Button)findViewById(R.id.regRegisterButton);
        EditText email = (EditText)findViewById(R.id.regEmail);
        
        String lipEmail = getIntent().getExtras().getString("email");
        String lipPassword = getIntent().getExtras().getString("password");
        
        if(lipEmail != null && !lipEmail.isEmpty()) {
            email.setText(lipEmail);
        }
        
        if(lipPassword != null && !lipPassword.isEmpty()) {
            EditText etLipPass = (EditText)findViewById(R.id.regPassword);
            etLipPass.setText(lipPassword);
        }
        
        // If we are editing an account adjust UI appropriately
        if(action.equalsIgnoreCase("edit")) {
            EditText fullname = (EditText)findViewById(R.id.regFullName);
            fullname.setText(LocalCache.getAccount().getFullname());
            
            EditText description = (EditText)findViewById(R.id.regDescription);
            description.setText(LocalCache.getAccount().getDescription());
            
            LinearLayout terms = (LinearLayout)findViewById(R.id.regTermsLayout);
            terms.setVisibility(View.GONE);
            
            CheckBox termsAccept = (CheckBox)findViewById(R.id.regTermsCheck);
            termsAccept.setChecked(true);
            
            registerAccountButton.setText("Update Profile");
            
            email.setText(LocalCache.getAccount().getEmail());
            email.setEnabled(false);
        }
        
        registerAccountButton.setOnClickListener(registerButtonClick);
        
        Button readTermsButton = (Button) findViewById(R.id.regTermsConditionsButton);
        readTermsButton.setOnClickListener(readTermsButtonClick);
    }
    
    private OnClickListener registerButtonClick = new OnClickListener()
    {
        public void onClick(View v) {
            String etFullname = ((EditText)findViewById(R.id.regFullName)).getText().toString();
            String etEmail = ((EditText)findViewById(R.id.regEmail)).getText().toString();
            String etPassword = ((EditText)findViewById(R.id.regPassword)).getText().toString();
            String etRePassword = ((EditText)findViewById(R.id.regRePassword)).getText().toString();
            String etDescription = ((EditText)findViewById(R.id.regDescription)).getText().toString();
            boolean cbTerms = ((CheckBox)findViewById(R.id.regTermsCheck)).isChecked();
            
            if(etFullname.isEmpty() || etEmail.isEmpty() || etPassword.isEmpty() || etRePassword.isEmpty() || etDescription.isEmpty()) {
                Resources resources = getResources();
                String alertTitle = resources.getString(R.string.formEmptyFieldsTitle);
                String alertMessage = resources.getString(R.string.formEmptyFieldsMessage);
                
                new AlertDialog.Builder(RegisterActivity.this)
                .setTitle(alertTitle)
                .setMessage(alertMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                }).show();
            }
            else if(!etPassword.equals(etRePassword)) {
                Resources resources = getResources();
                String alertTitle = resources.getString(R.string.formPasswordMatchTitle);
                String alertMessage = resources.getString(R.string.formPasswordMatchMessage);
                
                new AlertDialog.Builder(RegisterActivity.this)
                .setTitle(alertTitle)
                .setMessage(alertMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                }).show();
            }
            else if(!cbTerms) {
                Resources resources = getResources();
                String alertTitle = resources.getString(R.string.formAcceptTermsTitle);
                String alertMessage = resources.getString(R.string.formAcceptTermsMessage);
                
                new AlertDialog.Builder(RegisterActivity.this)
                .setTitle(alertTitle)
                .setMessage(alertMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                }).show();
            }
            else {
                RegisterTask registerTask = new RegisterTask();
                registerTask.execute();
            }
        }
    };
    
    class RegisterTask extends AsyncTask<Void, Void, JSONObject>
    {
        private ProgressDialog progressDialog = null;
        private String fullname = "";
        private String email = "";
        private String password = "";
        private String sex = "";
        private String age = "";
        private String description = "";
        
        @Override
        protected void onPreExecute()
        {
            fullname = ((EditText)findViewById(R.id.regFullName)).getText().toString();
            email = ((EditText)findViewById(R.id.regEmail)).getText().toString();
            password = ((EditText)findViewById(R.id.regPassword)).getText().toString();
            
            String spSex = ((Spinner)findViewById(R.id.regSex)).getSelectedItem().toString();
            
            if(spSex.equals("Male")) {
                sex = "0";
            }
            else {
                sex = "1";
            }
            
            age = ((Spinner)findViewById(R.id.regAge)).getSelectedItem().toString();
            description = ((EditText)findViewById(R.id.regDescription)).getText().toString();
            
            // Show a "Loading..." dialog
            super.onPreExecute();
            Resources resources = getResources();
            String loadingMessage = null;
            
            if(action.equalsIgnoreCase("edit")) {
                loadingMessage = resources.getString(R.string.updateLoadingMessage);
            }
            else {
                loadingMessage = resources.getString(R.string.regLoadingMessage);
            }
            
            progressDialog = ProgressDialog.show(RegisterActivity.this, null, loadingMessage, true);
        }

        @Override
        protected JSONObject doInBackground(Void... params)
        {
            SocialMe sm = new SocialMe();
            
            if(action.equalsIgnoreCase("edit")) {
                return sm.smUpdateProfile(email, fullname, password, sex, age, description);
            }
            
            return sm.smRegister(email, fullname, password, sex, age, description);
        }

        @Override
        protected void onPostExecute(JSONObject response)
        {
            try {
                final String status = response.getString("status");
                
                // Dismiss the "Loading..." dialog
                progressDialog.dismiss();
                
                if(status.equalsIgnoreCase("200")) {
                    if(action.equalsIgnoreCase("edit")) {
                        LocalCache.updateUserInfo(response);
                        Intent intent = new Intent(RegisterActivity.this, MyProfileActivity.class);
                        startActivity(intent);
                    }
                    else {
                        LocalCache.cacheUser(response);
                        Intent intent = new Intent(RegisterActivity.this, MapActivity.class);
                        startActivity(intent);
                    }
                }
                else if(status.equalsIgnoreCase("500")) {
                    new AlertDialog.Builder(RegisterActivity.this)
                    .setTitle("Error")
                    .setMessage(response.getString("message"))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                            Intent intent = new Intent(RegisterActivity.this, MapActivity.class);
                            startActivity(intent);
                        }
                    }).show();
                }
                else if(status.equalsIgnoreCase("550")) {
                    new AlertDialog.Builder(RegisterActivity.this)
                    .setTitle("Error")
                    .setMessage(response.getString("message"))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                        }
                    }).show();
                }
            }
            catch (JSONException e) {
                Log.e("SocialMe", "Error parsing JSON response during registration");
                e.printStackTrace();
            }
        }
    }
    
    private OnClickListener readTermsButtonClick = new OnClickListener()
    {
        public void onClick(View v) {
            // Create and set a dialog to show account info
            Resources resources = getResources();
            String readTermsConditionsTitlte = resources.getString(R.string.regTermsConditionsTitle);
            String readTermsConditionsText = resources.getString(R.string.regTermsConditionsText);
            
            Dialog readTermsConditionsDialog = new Dialog(RegisterActivity.this);
            readTermsConditionsDialog.setContentView(R.layout.popup_message_layout);
            readTermsConditionsDialog.setTitle(readTermsConditionsTitlte);
            readTermsConditionsDialog.setCancelable(true);
            readTermsConditionsDialog.setCanceledOnTouchOutside(true);
            
            // Populate dialog fields
            TextView snText = (TextView) readTermsConditionsDialog.findViewById(R.id.snText);
            snText.setText(readTermsConditionsText);
            
            // Show dialog on screen
            readTermsConditionsDialog.show();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.sml_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            if(action.equalsIgnoreCase("edit")) {
                finish();
            }
            else {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
