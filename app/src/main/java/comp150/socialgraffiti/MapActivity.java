package comp150.socialgraffiti;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import android.util.Log;
import android.location.Location;

import java.text.DateFormat;
import java.util.Date;

public class MapActivity extends FragmentActivity
                         implements OnMapReadyCallback,
                                    OnMarkerClickListener,
                                    OnMyLocationButtonClickListener,
                                    ConnectionCallbacks,
                                    OnConnectionFailedListener,
                                    LocationListener,
                                    OnCameraChangeListener {

    protected static final String TAG = "MapActivity";
    private final static int REQUEST_CODE_NEW_POST = 2;

    private GoogleMap mMap;

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected Location mCurrentLocation;
    protected LocationRequest mLocationRequest;
    protected String mLastUpdateTime;

    public static final long UPDATE_INTERVAL = 10000;
    public static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;

    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buildGoogleApiClient();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        enableMyLocation();
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onCameraChange(CameraPosition position) {
//        float maxZoom = 20.0f;
//        float minZoom = 17.0f;
//
//        if (mCurrentLocation != null) {
//            LatLng center = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(center));
//        } else if (mLastLocation != null) {
//            LatLng center = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(center));
//        }
//
//        if (position.zoom > maxZoom)
//            mMap.animateCamera(CameraUpdateFactory.zoomTo(maxZoom));
//        else if (position.zoom < minZoom)
//            mMap.animateCamera(CameraUpdateFactory.zoomTo(minZoom));
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
             marshmallowPermission.requestPermissionForLocation();
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            marshmallowPermission.requestPermissionForLocation();
        } else if (mMap != null) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        startLocationUpdates();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // Connection was lost for some reason. Attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            marshmallowPermission.requestPermissionForLocation();
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = mCurrentLocation;
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        LatLng center = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(center));
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Do nothing
        return false;
    }

    public void createPost (View view) {
        Intent createPostIntent = new Intent(this, NewPostActivity.class);

        if (mCurrentLocation == null) {
            if (mLastLocation != null) {
                createPostIntent.putExtra("LOCATION_EXTRA", mLastLocation);
                startActivityForResult(createPostIntent, REQUEST_CODE_NEW_POST);
            } else {
                Toast.makeText(this, "Cannot get location", Toast.LENGTH_LONG).show();
            }
        } else {
            createPostIntent.putExtra("LOCATION_EXTRA", mCurrentLocation);
            startActivityForResult(createPostIntent, REQUEST_CODE_NEW_POST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_NEW_POST && resultCode == RESULT_OK) {
            Log.e("-----", "Create a marker here");
            if (data != null) {
                Graffiti graffiti = (Graffiti) data.getParcelableExtra("GRAFFITI_EXTRA");

                LatLng pin = new LatLng(graffiti.getLocation().getLatitude(),
                                        graffiti.getLocation().getLongitude());

                mMap.addMarker(new MarkerOptions()
                        .position(pin)
                        .title(graffiti.getContent()));
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setVisible(false);
        return false;
    }
}
