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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.widget.Spinner;

import com.yummycode.api.SocialMe;
import com.yummycode.util.LocalCache;
import com.yummycode.util.Request;


public class AddRequestActivity extends Activity
{
    private Location currentLocation = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sml_add_request);
        
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Get location of the user
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        
        Button publishButton = (Button) findViewById(R.id.arAddButton);
        publishButton.setOnClickListener(publishButtonClick);
    }

    // Define a listener that responds to location updates
    private LocationListener locationListener = new LocationListener()
    {
        public void onLocationChanged(Location location) {
            currentLocation = location;
        }
    
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    
        public void onProviderEnabled(String provider) {}
    
        public void onProviderDisabled(String provider) {}
    };
    
    private OnClickListener publishButtonClick = new OnClickListener()
    {
        public void onClick(View v) {
            String etDescription = ((EditText)findViewById(R.id.arRequestDescription)).getText().toString();
            
            if(etDescription.isEmpty()) {
                Resources resources = getResources();
                String alertTitle = resources.getString(R.string.formEmptyFieldsTitle);
                String alertMessage = resources.getString(R.string.formEmptyFieldsMessage);
                
                new AlertDialog.Builder(AddRequestActivity.this)
                .setTitle(alertTitle)
                .setMessage(alertMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                }).show();
            }
            else {
                PublishRequestTask publishRequestTask = new PublishRequestTask();
                publishRequestTask.execute();
            }
        }
    };
    
    class PublishRequestTask extends AsyncTask<Void, Void, JSONObject>
    {
        private ProgressDialog progressDialog = null;
        private String type = "";
        private String range = "";
        private String description = "";
        private String visibility = "";
        
        
        @Override
        protected void onPreExecute()
        {
            type = ((Spinner)findViewById(R.id.arRequestTypeList)).getSelectedItem().toString();
            range = ((Spinner)findViewById(R.id.arRequestRangeList)).getSelectedItem().toString();
            description = ((EditText)findViewById(R.id.arRequestDescription)).getText().toString();
            
            boolean isVisible = ((CheckBox)findViewById(R.id.arMakeVisibleToAnyone)).isChecked();
            
            if(isVisible) {
                visibility = "1";
            }
            else {
                visibility = "0";
            }
            
            // Show a "Loading..." dialog
            super.onPreExecute();
            Resources resources = getResources();
            String loadingMessage = resources.getString(R.string.arLoadingMessage);
            
            progressDialog = ProgressDialog.show(AddRequestActivity.this, null, loadingMessage, true);
        }

        @Override
        protected JSONObject doInBackground(Void... params)
        {
            SocialMe sm = new SocialMe();
            
            String longitude = String.valueOf(currentLocation.getLongitude());
            String latitude = String.valueOf(currentLocation.getLatitude());
            
            return sm.smPublishRequest(LocalCache.getAccount().getEmail(), type, range, description, visibility, longitude, latitude);
        }

        @Override
        protected void onPostExecute(JSONObject response)
        {
            try {
                final String status = response.getString("status");
                
                // Dismiss the "Loading..." dialog
                progressDialog.dismiss();
                
                if(status.equalsIgnoreCase("200")) {
                    JSONObject responseRequest = response.getJSONObject("request");
                    
                    String id = responseRequest.getString("RequestID");
                    String account = responseRequest.getString("Account");
                    String type = responseRequest.getString("Type");
                    String range = responseRequest.getString("Range");
                    String description = responseRequest.getString("Description");
                    String visibility = responseRequest.getString("Visibility");
                    String longitude = responseRequest.getString("Longitude");
                    String latitude = responseRequest.getString("Latitude");
                    
                    Request newRequest = new Request(id, account, type, range, description, visibility, longitude, latitude);
                    
                    LocalCache.addRequest(newRequest);
                    
                    Intent intent = new Intent(AddRequestActivity.this, MapActivity.class);
                    startActivity(intent);
                }
                else if(status.equalsIgnoreCase("500")) {
                    new AlertDialog.Builder(AddRequestActivity.this)
                    .setTitle("Error")
                    .setMessage(response.getString("message"))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                            Intent intent = new Intent(AddRequestActivity.this, MapActivity.class);
                            startActivity(intent);
                        }
                    }).show();
                }
                else if(status.equalsIgnoreCase("550")) {
                    new AlertDialog.Builder(AddRequestActivity.this)
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.sml_add_request, menu);
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
                intent = new Intent(AddRequestActivity.this, MyProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_item_add_request:
                // Don't go anywhere!!
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
            
            progressDialog = ProgressDialog.show(AddRequestActivity.this, null, loadingMessage, true);
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
                    new AlertDialog.Builder(AddRequestActivity.this)
                    .setTitle("Error")
                    .setMessage(response.getString("message"))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                        }
                    }).show();
                }
                
                LocalCache.clearCache();
                
                Intent intent = new Intent(AddRequestActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            catch (JSONException e) {
                Log.e("SocialMe", "Error parsing JSON response during registration");
                e.printStackTrace();
            }
        }
    }
}
