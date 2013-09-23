package com.yummycode.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.yummycode.api.SocialMe;
import com.yummycode.util.Account;
import com.yummycode.util.LocalCache;
import com.yummycode.util.Request;


public class UserProfileActivity extends Activity
{
    private Account account = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sml_user_profile);
        
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Retrieve profile information from server
        GetProfileInfoTask getProfileInfoTask = new GetProfileInfoTask();
        getProfileInfoTask.execute(getIntent().getExtras().getString("email"));
    }
    
    private AdapterView.OnItemClickListener requestClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
        {
            Resources resources = getResources();
            String strBasketItemInfoTitlte = resources.getString(R.string.mpRequestInfoPopupTitle);
            
            Dialog requestInfoDialog = new Dialog(UserProfileActivity.this);
            requestInfoDialog.setContentView(R.layout.request_info_popup);
            requestInfoDialog.setTitle(strBasketItemInfoTitlte);
            requestInfoDialog.setCancelable(true);
            requestInfoDialog.setCanceledOnTouchOutside(true);
            
            // Populate dialog fields
            TextView requestInfoType = (TextView) requestInfoDialog.findViewById(R.id.mpRequestInfoType);
            requestInfoType.setText(account.getRequests().get(position).getType());
            
            TextView requestInfoDescription = (TextView) requestInfoDialog.findViewById(R.id.mpRequestInfoDescription);
            requestInfoDescription.setText(account.getRequests().get(position).getDescription());
            
            TextView requestInfoRange = (TextView) requestInfoDialog.findViewById(R.id.mpRequestInfoRange);
            requestInfoRange.setText(account.getRequests().get(position).getRange() + " meters");
            
            TextView requestInfoLongitude = (TextView) requestInfoDialog.findViewById(R.id.mpRequestInfoLongitude);
            requestInfoLongitude.setText(account.getRequests().get(position).getLongitude());
            
            TextView requestInfoLatitude = (TextView) requestInfoDialog.findViewById(R.id.mpRequestInfoLatitude);
            requestInfoLatitude.setText(account.getRequests().get(position).getLatitude());
            
            // Show dialog on screen
            requestInfoDialog.show();
        }
    };
    
    private AdapterView.OnItemLongClickListener requestLongClickListener = new AdapterView.OnItemLongClickListener()
    {
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
            startActivity(intent);
            
            return true;
        }
    };
    
    class GetProfileInfoTask extends AsyncTask<String, Void, JSONObject>
    {
        private ProgressDialog progressDialog = null;
        
        @Override
        protected void onPreExecute()
        {
            // Show a "Loading..." dialog
            super.onPreExecute();
            Resources resources = getResources();
            String loadingMessage = resources.getString(R.string.getProfInfoLoadingMessage);
            
            progressDialog = ProgressDialog.show(UserProfileActivity.this, null, loadingMessage, true);
        }

        @Override
        protected JSONObject doInBackground(String... email)
        {
            SocialMe sm = new SocialMe();
            return sm.smRetrieveProfile(email[0]);
        }

        @Override
        protected void onPostExecute(JSONObject response)
        {
            try {
                final String status = response.getString("status");
                
                // Dismiss the "Loading..." dialog
                progressDialog.dismiss();
                
                if(status.equalsIgnoreCase("200")) {
                    account = parseProfileInfo(response);
                    
                    // Populate profile information from local cache
                    TextView tvFullname = (TextView)findViewById(R.id.upAccountName);
                    tvFullname.setText(account.getFullname());
                    
                    TextView tvInfo = (TextView)findViewById(R.id.upAccountInfo);
                    tvInfo.setText(account.getSex() + ", " + account.getAge() + " years old!");
                    
                    TextView tvDescription = (TextView)findViewById(R.id.upAccountDescritpion);
                    tvDescription.setText(account.getDescription());
                    
                    ListView requestsList = (ListView) findViewById(R.id.upRequestsList);
                    List<Request> requests = account.getRequests();
                    
                    if(requests != null) {
                        RequestsAdapter adapter = new RequestsAdapter(UserProfileActivity.this, R.layout.request_item, requests);
                        requestsList.setAdapter(adapter);
                    }
                    
                    requestsList.setOnItemClickListener(requestClickListener);
                    requestsList.setOnItemLongClickListener(requestLongClickListener);
                }
                else  {
                    new AlertDialog.Builder(UserProfileActivity.this)
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
                Log.e("SocialMe", "Error parsing JSON response during login");
                e.printStackTrace();
            }
        }
    }
    
    private Account parseProfileInfo(JSONObject response) {
        Account account = null;
        
        try {
            JSONObject jsonAccount = response.getJSONObject("account");
            
            String fullname = jsonAccount.getString("fullname");
            String email = jsonAccount.getString("email");
            
            String sex = "Male";
            if(jsonAccount.getString("sex").equals("1")) {
                sex = "Female";
            }
            
            String age = jsonAccount.getString("age");
            String description = jsonAccount.getString("description");
            
            account = new Account(fullname, email, sex, age, description);
            
            JSONArray jsonRequests = jsonAccount.getJSONArray("requests");
            ArrayList<Request> requests = new ArrayList<Request>();
            
            for (int i = 0; i < jsonRequests.length(); i++) {
                JSONObject request = jsonRequests.getJSONObject(i);

                Request newRequest = new Request(request.getString("RequestID"), request.getString("Account"), request.getString("Type"),
                                                 request.getString("Range"), request.getString("Description"), request.getString("Visibility"),
                                                 request.getString("Longitude"), request.getString("Latitude"));
                
                requests.add(newRequest);
            }
            
            account.setRequests(requests);
        }
        catch (JSONException e) {
            Log.e("SocialMe", "Error while storing account information in local cache");
            e.printStackTrace();
        }
        
        return account;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.sml_user_profile, menu);
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
                intent = new Intent(UserProfileActivity.this, MyProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_item_add_request:
                intent = new Intent(UserProfileActivity.this, AddRequestActivity.class);
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
            
            progressDialog = ProgressDialog.show(UserProfileActivity.this, null, loadingMessage, true);
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
                    new AlertDialog.Builder(UserProfileActivity.this)
                    .setTitle("Error")
                    .setMessage(response.getString("message"))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                        }
                    }).show();
                }
                
                LocalCache.clearCache();
                
                Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            catch (JSONException e) {
                Log.e("SocialMe", "Error parsing JSON response during registration");
                e.printStackTrace();
            }
        }
    }
}
