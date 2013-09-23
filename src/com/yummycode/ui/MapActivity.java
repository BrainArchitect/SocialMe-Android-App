package com.yummycode.ui;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yummycode.api.SocialMe;
import com.yummycode.util.LocalCache;
import com.yummycode.util.Request;


public class MapActivity extends Activity implements OnInfoWindowClickListener
{
    private GoogleMap map;
    private ArrayList<Request> matchingRequests = new ArrayList<Request>();
    private ArrayList<Marker> markers = new ArrayList<Marker>();
    private HashMap<Marker, Request> markerRequestMap = new HashMap<Marker, Request>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sml_map);
        
        ActionBar ab = getActionBar();
        ab.setSubtitle("Find your matches!");
        
        retrieveMatches();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        retrieveMatches();
    }
    
    private void retrieveMatches() {
        GetMatchesTask getMatchesTask = new GetMatchesTask();
        getMatchesTask.execute();
    }
    
    class GetMatchesTask extends AsyncTask<Void, Void, JSONObject>
    {
        private ProgressDialog progressDialog = null;
        
        @Override
        protected void onPreExecute()
        {
            // Show a "Loading..." dialog
            super.onPreExecute();
            Resources resources = getResources();
            String loadingMessage = resources.getString(R.string.matchesLoadingMessage);
            
            progressDialog = ProgressDialog.show(MapActivity.this, null, loadingMessage, true);
        }

        @Override
        protected JSONObject doInBackground(Void... params)
        {
            SocialMe sm = new SocialMe();
            return sm.smRetrieveMatches(LocalCache.getAccount().getEmail());
        }

        @Override
        protected void onPostExecute(JSONObject response)
        {
            try {
                final String status = response.getString("status");
                
                // Dismiss the "Loading..." dialog
                progressDialog.dismiss();
                
                if(status.equalsIgnoreCase("200")) {
                    JSONArray jsonRequests = response.getJSONArray("requests");
                    
                    for (int i = 0; i < jsonRequests.length(); i++) {
                        JSONObject request = jsonRequests.getJSONObject(i);

                        Request newRequest = new Request(request.getString("RequestID"), request.getString("Account"), request.getString("Type"),
                                                         request.getString("Range"), request.getString("Description"), request.getString("Visibility"),
                                                         request.getString("Longitude"), request.getString("Latitude"));
                        
                        matchingRequests.add(newRequest);
                        
                        setUpMap();
                    }
                }
                else {
                    new AlertDialog.Builder(MapActivity.this)
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
    
    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        }
        
        // Check if we were successful in obtaining the map.
        if (map != null) {
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setOnInfoWindowClickListener(this);
            map.setInfoWindowAdapter(new RequestInfoWindowAdapter());
            
            setUpRequestMarkers();
            fitRequestsToMap();
        }
    }
    
    private void fitRequestsToMap() {
        final View mapView = getFragmentManager().findFragmentById(R.id.map).getView();
        
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    LatLngBounds.Builder bounds = new LatLngBounds.Builder();
                    
                    for(Marker marker : markers) {
                        bounds.include(marker.getPosition());
                    }
                    
                    mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50));
                }
            });
        }
    }
    
    private void setUpRequestMarkers() {
        // Setup own requests
        for(Request request : LocalCache.getAccount().getRequests()) {
            map.addMarker(new MarkerOptions()
                .position(new LatLng(Double.valueOf(request.getLatitude()), Double.valueOf(request.getLongitude())))
                .title(request.getType())
                .snippet(request.getDescription())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
        
        // Setup matching requests
        for(Request request : matchingRequests) {
            Marker newMarker = map.addMarker(
                new MarkerOptions()
                .position(new LatLng(Double.valueOf(request.getLatitude()), Double.valueOf(request.getLongitude())))
                .title(request.getType())
                .snippet(request.getDescription())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            );
            
            markers.add(newMarker);
            markerRequestMap.put(newMarker, request);
        }
    }
    
    @Override
    public void onInfoWindowClick(Marker marker) {
        if(markerRequestMap.get(marker) != null) {
            retrieveUserInfo(markerRequestMap.get(marker).getAccount());
        }
        else {
            Intent intent = new Intent(MapActivity.this, MyProfileActivity.class);
            startActivity(intent);
        }
    }

    private void retrieveUserInfo(String email) {
        Intent intent = new Intent(MapActivity.this, UserProfileActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }
    
    class RequestInfoWindowAdapter implements InfoWindowAdapter {
        private final View infoWindow;

        public RequestInfoWindowAdapter() {
            infoWindow = getLayoutInflater().inflate(R.layout.request_info_window_content, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, infoWindow);
            return infoWindow;
        }

        private void render(Marker marker, View view) {
            ImageView riwAvatar = (ImageView)view.findViewById(R.id.riwImage);
            riwAvatar.setImageResource(R.drawable.avatar);
            
            TextView riwTitle = ((TextView)view.findViewById(R.id.riwTitle));
            riwTitle.setText(marker.getTitle());
            
            TextView riwSnippet = ((TextView)view.findViewById(R.id.riwSnippet));
            riwSnippet.setText(marker.getSnippet());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.sml_map, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        Intent intent;
        
        switch(item.getItemId()) {
            case R.id.menu_item_myprofile:
                intent = new Intent(MapActivity.this, MyProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_item_add_request:
                intent = new Intent(MapActivity.this, AddRequestActivity.class);
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
            
            progressDialog = ProgressDialog.show(MapActivity.this, null, loadingMessage, true);
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
                    new AlertDialog.Builder(MapActivity.this)
                    .setTitle("Error")
                    .setMessage(response.getString("message"))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                        }
                    }).show();
                }
                
                LocalCache.clearCache();
                
                Intent intent = new Intent(MapActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            catch (JSONException e) {
                Log.e("SocialMe", "Error parsing JSON response during registration");
                e.printStackTrace();
            }
        }
    }
}
