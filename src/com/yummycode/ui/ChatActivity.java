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

import com.yummycode.api.SocialMe;
import com.yummycode.util.LocalCache;


public class ChatActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sml_chat);
        
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.sml_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        Intent intent;
        
        switch(item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.menu_item_myprofile:
                intent = new Intent(ChatActivity.this, MyProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_item_add_request:
                intent = new Intent(ChatActivity.this, AddRequestActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_item_logout:
                LogoutTask logoutTask = new LogoutTask();
                logoutTask.execute();
                break;
            default:
                return false;
        }
        
        return true;
    }

    class LogoutTask extends AsyncTask<Void, Void, JSONObject>
    {
        private ProgressDialog progressDialog = null;
        
        @Override
        protected void onPreExecute()
        {
            // Show a "Loading..." dialog
            super.onPreExecute();
            Resources resources = getResources();
            String loadingMessage = resources.getString(R.string.logoutLoadingMessage);
            
            progressDialog = ProgressDialog.show(ChatActivity.this, null, loadingMessage, true);
        }

        @Override
        protected JSONObject doInBackground(Void... params)
        {
            SocialMe sm = new SocialMe();
            return sm.smLogout();
        }

        @Override
        protected void onPostExecute(JSONObject response)
        {
            try {
                final String status = response.getString("status");
                
                // Dismiss the "Loading..." dialog
                progressDialog.dismiss();
                
                if(status.equalsIgnoreCase("500")) {
                    new AlertDialog.Builder(ChatActivity.this)
                    .setTitle("Error")
                    .setMessage(response.getString("message"))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                        }
                    }).show();
                }
                
                LocalCache.clearCache();
                
                Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            catch (JSONException e) {
                Log.e("SocialMe", "Error parsing JSON response during registration");
                e.printStackTrace();
            }
        }
    }
}
