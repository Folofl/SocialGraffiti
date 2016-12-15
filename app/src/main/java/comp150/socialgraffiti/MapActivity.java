package comp150.socialgraffiti;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.location.Location;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class MapActivity extends AppCompatActivity
                         implements OnMapReadyCallback,
                                    OnMarkerClickListener,
                                    OnMyLocationButtonClickListener,
                                    ConnectionCallbacks,
                                    OnConnectionFailedListener,
                                    LocationListener {

    protected static final String TAG = "MapActivity";
    private final static int REQUEST_CODE_NEW_POST = 2;

    private GoogleMap mMap;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference();

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

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);

        setSupportActionBar(myToolbar);

        buildGoogleApiClient();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_logout:
                FirebaseAuth auth = FirebaseAuth.getInstance();

                if (auth.getCurrentUser() != null) {
                    auth.signOut();
                    stopLocationUpdates();
                    startActivity(new Intent(MapActivity.this, LogInActivity.class));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        enableMyLocation();
        mMap.setOnMarkerClickListener(this);
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
             marshmallowPermission.requestPermissionForLocation();
        } else if (mMap != null) {
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
        loadMarkers();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
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
        loadMarkers();
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

        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    // https://www.learnhowtoprogram.com/android/gestures-animations-flexible-uis/using-the-camera-and-saving-images-to-firebase
    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    public void loadMarkers () {
        final Location mLocation;

        if (mCurrentLocation != null) {
            mLocation = mCurrentLocation;
        } else if (mLastLocation != null){
            mLocation = mLastLocation;
        } else {
            onLocationChanged(mCurrentLocation);
            mLocation = mCurrentLocation;
        }

        final Query queryRef = ref.orderByChild("lat").startAt(mLocation.getLatitude() - 0.1)
                .endAt(mLocation.getLatitude() + 0.1);

        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                final Graffiti newGraffiti = dataSnapshot.getValue(Graffiti.class);

                if (newGraffiti.getLon() > mLocation.getLongitude() - 0.1
                        && newGraffiti.getLon() < mLocation.getLongitude() + 0.1) {

                    long timeLive = (System.currentTimeMillis() - newGraffiti.getTime())/3600000;

                    if (timeLive < newGraffiti.getDuration()) {
                        LatLng pin = new LatLng(newGraffiti.getLat(),
                                newGraffiti.getLon());

                        Marker newMarker = mMap.addMarker(new MarkerOptions()
                                .position(pin)
                                .title(newGraffiti.getContent()));
                        newMarker.setTag(newGraffiti.getPhotoURL());

                        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                            @Override
                            public View getInfoWindow(Marker marker) {
                                return null;
                            }

                            @Override
                            public View getInfoContents(Marker marker) {

                                View v = getLayoutInflater().inflate(R.layout.info_window, null);

                                TextView pinContent = (TextView) v.findViewById(R.id.tv_content);
                                ImageView pinPhoto = (ImageView) v.findViewById(R.id.iv_photo);

                                pinContent.setText(marker.getTitle());

                                String photoURL = (String) marker.getTag();
                                if (!photoURL.equals("")) {
                                    try {
                                        Bitmap imageBitmap = decodeFromFirebaseBase64(photoURL);
                                        pinPhoto.setImageBitmap(imageBitmap);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                return v;
                            }
                        });
                    }
                    else if (timeLive >= newGraffiti.getDuration()) {
                        FirebaseDatabase.getInstance().getReference().getRoot().child(dataSnapshot.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
