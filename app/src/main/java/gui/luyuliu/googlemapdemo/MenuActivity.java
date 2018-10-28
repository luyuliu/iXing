package gui.luyuliu.googlemapdemo;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowCloseListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import com.google.maps.android.data.Feature;
import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPointStyle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static java.lang.Integer.parseInt;

public class MenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    private static final String TAG="MenuActivity";
    private static final int ERROR_DIALOG_REQUEST=9001;
    private String url="";

    private int mYear;
    private int mMonth;
    private int mDayOfMonth;
    private int mHour;
    private int mMinute;

    private boolean isCurrent=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (isServiceOk()){
            Log.d(TAG,"Session Start.");
            getLocationPermission();

        }
    }

    // Init Methods

    public boolean isServiceOk(){
        Log.d(TAG,"isServiceOk: checking google service version: ");
        int availble= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MenuActivity.this);
        if (availble== ConnectionResult.SUCCESS){
            Log.d(TAG,"isServiceOK: Google Play Services are working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(availble)){
            Log.d(TAG, "isServiceOk: an error occered.");
            Dialog dialog=GoogleApiAvailability.getInstance().getErrorDialog(MenuActivity.this,availble,ERROR_DIALOG_REQUEST);
            dialog.show();
            return false;
        }
        else{
            Toast.makeText(this,"Map requests fatal error.",Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Map Methods
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: ready");
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            GeoJsonLayer layer = null;
            try {
                layer = new GeoJsonLayer(mMap, R.raw.cameras, this);
            } catch (IOException e) {
                Log.e(TAG, "GeoJSON file could not be read");
            } catch (JSONException e) {
                Log.e(TAG, "GeoJSON file could not be converted to a JSONObject");
            }
            addGeoJsonLayerToMap(layer);
        }
    }
    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getResources().openRawResource(R.raw.detection_result);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void addGeoJsonLayerToMap(GeoJsonLayer layer) {

        addColorsToMarkers(layer);
        layer.addLayerToMap();
        // Demonstrate receiving features via GeoJsonLayer clicks.
        layer.setOnFeatureClickListener(new GeoJsonLayer.GeoJsonOnFeatureClickListener() {
            @Override
            public void onFeatureClick(Feature feature) {
                Toast.makeText(MenuActivity.this,
                        "Feature clicked: " + feature.getProperty("ID"),
                        Toast.LENGTH_SHORT).show();
                // start Popup insert.s

                if(isCurrent){
                    url=feature.getProperty("SmallImage");
                }
                else{
                    if (mYear==2018 && mMonth==10 && mDayOfMonth==27 && mHour == 12 &&mMinute==0){
                        url = "http://gis.osu.edu/hackoht18/demo_detection/result_image_167/results_2018_10_27_1200_"+feature.getProperty("ID")+".jpg";
                    }
                    else{

                        if (mDayOfMonth<27 && mDayOfMonth>26){
                            url = "http://gis.osu.edu/hackoht18/php/weekimage.php?camera="+feature.getProperty("ID")+"&dateindex=%22"+mYear+"-"+ String.format("%02d", mMonth)+"-"+String.format("%02d", mDayOfMonth)+"%22&timeindex=%22"+String.format("%02d", mHour)+"_"+String.format("%02d", mMinute)+"_00%22";
                        }

                        else{
                            url = "http://gis.osu.edu/hackoht18/php/weekimage.php?camera="+feature.getProperty("ID")+"&dateindex=%22"+mYear+"-"+ String.format("%02d", mMonth)+"-"+String.format("%02d", mDayOfMonth)+"%22&timeindex=%22"+String.format("%02d", mHour)+"_"+String.format("%02d", mMinute)+"_00%22";
                        }
                        }
                }
                Log.e(TAG, url);
                LatLng ll=(LatLng)feature.getGeometry().getGeometryObject();

                Intent popIntent=new Intent(MenuActivity.this, PopActivity.class);
                popIntent.putExtra("url",url);

                startActivity(popIntent);
            }

        });

    }

    private void addColorsToMarkers(GeoJsonLayer layer) {
        // Iterate over all the features stored in the layer
        for (GeoJsonFeature feature : layer.getFeatures()) {
            //Log.d(TAG,""+parseInt(feature.getProperty("Time")));
            // Check if the magnitude property exists
            if (feature.getProperty("Time") != null ) {
                int magnitude = parseInt(feature.getProperty("Time"));
                Log.d(TAG,""+magnitude);

                // Get the icon for the feature
                BitmapDescriptor pointIcon = BitmapDescriptorFactory
                        .defaultMarker(magnitudeToColor(magnitude));

                // Create a new point style
                GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();

                // Set options for the point style
                pointStyle.setIcon(pointIcon);
                pointStyle.setTitle("Cars detected: " + magnitude);

                // Assign the point style to the feature
                feature.setPointStyle(pointStyle);
            }
        }
    }

    private static float magnitudeToColor(double magnitude) {
        if (magnitude < 1.0) {
            return BitmapDescriptorFactory.HUE_CYAN;
        } else if (magnitude < 2.5) {
            return BitmapDescriptorFactory.HUE_GREEN;
        } else if (magnitude < 4.5) {
            return BitmapDescriptorFactory.HUE_YELLOW;
        } else {
            return BitmapDescriptorFactory.HUE_RED;
        }
    }

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private static final int LOCATION_PERMISSION_REQUEST_CODE=1234;
    private static final float DEFAULT_ZOOM=15f;
    //vars
    private Boolean mLocationPermissionGranted=false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private void getDeviceLocation(){
        Log.d(TAG,"getDeviceLocation: Getting the current location.");
        mFusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        try{
            if(mLocationPermissionGranted){
                Task location=mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG,"onComplete: found locatioin.");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);
                        }
                        else{
                            Log.d(TAG,"onComplete: current Location is null..");
                            Toast.makeText(MenuActivity.this,"unable to get current location", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }

        }catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        };
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.e(TAG, "moveCamera: moving the camera to: lat:" + latLng.latitude+", lng: "+latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

    }

    private void initMap(){
        Log.d(TAG,"initMap: Initializing.");
        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MenuActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted=false;
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length>0){
                    for (int i =0; i<grantResults.length;i++){
                        if (grantResults[i]!= PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted=false;
                            return;
                        }
                    }

                    Log.d(TAG,"getLocationPermission: Permission granted..");
                    mLocationPermissionGranted=true;
                    //initialize our map.
                    initMap();
                }
            }
        }

    }

    private void getLocationPermission(){
        Log.d(TAG,"getLocationPermission: Getting permission..");
        String[] permissions= {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            //permission has been granted,
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),COURSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted=true;
                initMap();
            }
            else{
                ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else{
            ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    // Menu methods
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

        } else if (id == R.id.nav_setting) {
            if (!isCurrent){
                DatePickerDialog datePickerDialog = new DatePickerDialog(MenuActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                mYear=year;
                                mMonth=month+1;
                                mDayOfMonth=day;

                                TimePickerDialog timePickerDialog = new TimePickerDialog(MenuActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                                        mHour=hourOfDay;
                                        mMinute=minutes;
                                        Log.d(TAG,""+mYear+":"+mMonth+":"+mDayOfMonth+":"+mHour+":"+mMinute);
                                    }
                                }, 0, 0, false);
                                timePickerDialog.show();
                            }
                        }, 2018, 9, 27);
                datePickerDialog.show();
            }
            else{
                Toast.makeText(this,"Please switch to history mode.",Toast.LENGTH_SHORT).show();
            }



        } else if (id == R.id.nav_authors) {

            Toast.makeText(this,"Greeting from hackDerbyTeam: Luyu_Liu, Jialin_Li, and Yuxiao_Zhao!",Toast.LENGTH_SHORT).show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void switchShowingStatus(MenuItem item){
        if (item.getTitle()=="Now showing: Current")
        {
            isCurrent=false;
            item.setTitle("Now showing: History");
        }
        else{
            isCurrent=true;
            item.setTitle("Now showing: Current");
        }
    }

}
