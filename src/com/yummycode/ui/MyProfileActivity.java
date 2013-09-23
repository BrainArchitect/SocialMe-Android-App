package com.yummycode.ui;

import java.util.List;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yummycode.api.SocialMe;
import com.yummycode.util.LocalCache;
import com.yummycode.util.Request;


public class MyProfileActivity extends Activity
{
    private ListView requestsList;
    private RequestsAdapter adapter;
    private List<Request> requests = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sml_my_profile);
        
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Populate profile information from local cache
        TextView tvFullname = (TextView)findViewById(R.id.mpAccountName);
        tvFullname.setText(LocalCache.getAccount().getFullname());
        
        TextView tvInfo = (TextView)findViewById(R.id.mpAccountInfo);
        tvInfo.setText(LocalCache.getAccount().getSex() + ", " + LocalCache.getAccount().getAge() + " years old!");
        
        TextView tvDescription = (TextView)findViewById(R.id.mpAccountDescritpion);
        tvDescription.setText(LocalCache.getAccount().getDescription());
        
        requests = LocalCache.getAccount().getRequests();
        
        // Assign listeners to UI controls
        Button addRequestButton = (Button) findViewById(R.id.mpAddRequestButton);
        addRequestButton.setOnClickListener(addRequestButtonButtonClick);
        
        requestsList = (ListView) findViewById(R.id.mpRequestsList);
        
        if(requests != null) {
            adapter = new RequestsAdapter(this, R.layout.request_item, requests);
            requestsList.setAdapter(adapter);
        }
        
        requestsList.setOnItemClickListener(requestClickListener);
        requestsList.setOnItemLongClickListener(requestLongClickListener);
    }
    
    private OnClickListener addRequestButtonButtonClick = new OnClickListener()
    {
        public void onClick(View v) {
            // Create an intent to start another activity. Extra parameters
            // for the new activity are specified here
            Intent intent = new Intent(MyProfileActivity.this, AddRequestActivity.class);
            startActivity(intent);
        }
    };
    
    private AdapterView.OnItemClickListener requestClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
        {            
            // Create and set a dialog to show account info
            Resources resources = getResources();
            String strBasketItemInfoTitlte = resources.getString(R.string.mpRequestInfoPopupTitle);
            
            Dialog requestInfoDialog = new Dialog(MyProfileActivity.this);
            requestInfoDialog.setContentView(R.layout.request_info_popup);
            requestInfoDialog.setTitle(strBasketItemInfoTitlte);
            requestInfoDialog.setCancelable(true);
            requestInfoDialog.setCanceledOnTouchOutside(true);
            
            // Populate dialog fields
            TextView requestInfoType = (TextView) requestInfoDialog.findViewById(R.id.mpRequestInfoType);
            requestInfoType.setText(LocalCache.getAccount().getRequests().get(position).getType());
            
            TextView requestInfoDescription = (TextView) requestInfoDialog.findViewById(R.id.mpRequestInfoDescription);
            requestInfoDescription.setText(LocalCache.getAccount().getRequests().get(position).getDescription());
            
            TextView requestInfoRange = (TextView) requestInfoDialog.findViewById(R.id.mpRequestInfoRange);
            requestInfoRange.setText(LocalCache.getAccount().getRequests().get(position).getRange() + " meters");
            
            TextView requestInfoLongitude = (TextView) requestInfoDialog.findViewById(R.id.mpRequestInfoLongitude);
            requestInfoLongitude.setText(LocalCache.getAccount().getRequests().get(position).getLongitude());
            
            TextView requestInfoLatitude = (TextView) requestInfoDialog.findViewById(R.id.mpRequestInfoLatitude);
            requestInfoLatitude.setText(LocalCache.getAccount().getRequests().get(position).getLatitude());
            
            // Show dialog on screen
            requestInfoDialog.show();
        }
    };
    
    private AdapterView.OnItemLongClickListener requestLongClickListener = new AdapterView.OnItemLongClickListener()
    {
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final int pos = position;
            Resources resources = getResources();
            String strBasketItemDeleteTitlte = resources.getString(R.string.mpRequestDeletePopupTitle);
            String strBasketItemDeleteMessage = resources.getString(R.string.mpRequestDeletePopupMessage);
            String strBasketItemDeltePosButton = resources.getString(R.string.mpRequestDeletePopupPositiveButton);
            String strBasketItemDelteNegButton = resources.getString(R.string.mpRequestDeletePopupNegativeButton);
            
            
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MyProfileActivity.this);
            alertBuilder.setTitle(strBasketItemDeleteTitlte);
            alertBuilder.setMessage(strBasketItemDeleteMessage);
            alertBuilder.setPositiveButton(strBasketItemDeltePosButton, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                   DeleteRequestTask deleteRequestTask = new DeleteRequestTask();
                   deleteRequestTask.execute(LocalCache.getAccount().getRequests().get(pos).getId());
               }
            });
            alertBuilder.setNegativeButton(strBasketItemDelteNegButton, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {}
            });   
            AlertDialog alertDialog = alertBuilder.create();            
            alertDialog.setIcon(android.R.drawable.ic_dialog_info);
            alertDialog.show();
            return true;
        }
    };
    
    class DeleteRequestTask extends AsyncTask<String, Void, JSONObject>
    {
        private ProgressDialog progressDialog = null;
        private String delID = null;
        
        @Override
        protected void onPreExecute()
        {
            // Show a "Loading..." dialog
            super.onPreExecute();
            Resources resources = getResources();
            String loadingMessage = resources.getString(R.string.delReqLoadingMessage);
            
            progressDialog = ProgressDialog.show(MyProfileActivity.this, null, loadingMessage, true);
        }

        @Override
        protected JSONObject doInBackground(String... id)
        {
            delID = id[0];
            SocialMe sm = new SocialMe();
            return sm.smDeleteRequest(id[0]);
        }

        @Override
        protected void onPostExecute(JSONObject response)
        {
            try {
                final String status = response.getString("status");
                
                // Dismiss the "Loading..." dialog
                progressDialog.dismiss();
                
                if(status.equalsIgnoreCase("200")) {
                    LocalCache.removeRequestByID(delID);
                    removeRequestByID(delID);
                    adapter.notifyDataSetChanged();
                    
                    Toast.makeText(MyProfileActivity.this, "Request Deleted!", Toast.LENGTH_SHORT).show();
                }
                else if(status.equalsIgnoreCase("510")){
                    new AlertDialog.Builder(MyProfileActivity.this)
                    .setTitle("Error")
                    .setMessage(response.getString("message"))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                        }
                    }).show();
                    
                    Intent intent = new Intent(MyProfileActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                else if(status.equalsIgnoreCase("520")) {
                    LocalCache.removeRequestByID(delID);
                    removeRequestByID(delID);
                    adapter.notifyDataSetChanged();
                }
                else  {
                    new AlertDialog.Builder(MyProfileActivity.this)
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
    
    private void removeRequestByID(String id) {
        for(int i = 0; i < requests.size(); i++) {
            if(requests.get(i).getId().equalsIgnoreCase(id)) {
                requests.remove(i);
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.sml_my_profile, menu);
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
                // Don't go anywhere!
                break;
            case R.id.menu_item_edit_profile:
                intent = new Intent(MyProfileActivity.this, RegisterActivity.class);
                intent.putExtra("action", "edit");
                startActivity(intent);
                break;
            case R.id.menu_item_add_request:
                intent = new Intent(MyProfileActivity.this, AddRequestActivity.class);
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
            
            progressDialog = ProgressDialog.show(MyProfileActivity.this, null, loadingMessage, true);
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
                    new AlertDialog.Builder(MyProfileActivity.this)
                    .setTitle("Error")
                    .setMessage(response.getString("message"))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                        }
                    }).show();
                }
                
                LocalCache.clearCache();
                
                Intent intent = new Intent(MyProfileActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            catch (JSONException e) {
                Log.e("SocialMe", "Error parsing JSON response during registration");
                e.printStackTrace();
            }
        }
    }
}
