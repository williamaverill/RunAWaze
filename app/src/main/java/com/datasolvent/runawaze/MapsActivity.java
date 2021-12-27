package com.datasolvent.runawaze;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.datasolvent.runawaze.contacts.ContactListActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.datasolvent.runawaze.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.nambimobile.widgets.efab.ExpandableFab;
import com.nambimobile.widgets.efab.FabOption;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import info.androidhive.fontawesome.FontDrawable;
import it.beppi.tristatetogglebutton_library.TriStateToggleButton;
import mehdi.sakout.fancybuttons.FancyButton;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, EasyPermissions.PermissionCallbacks {

    private GoogleMap mGoogleMap;
    private ActivityMapsBinding mActivityMapsBinding;

    private boolean mGPSState = true;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Marker mCurrLocationMarker;
    private LocationRequest mLocationRequest;

    private Drawer mDrawer;
    private FloatingActionButton mDirectionsFAB;
    private FloatingActionButton mReportFAB;

    private boolean mMarkObstructionEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityMapsBinding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(mActivityMapsBinding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (mMarkObstructionEnabled) {
                    mMarkObstructionEnabled = false;
                    new MaterialDialog.Builder(MapsActivity.this)
                            .title("Report Obstruction To Trail")
                            .content("Enter Name Of Obstruction Marker")
                            .inputRangeRes(2, 20, R.color.material_drawer_selected_text)
                            .input("Obstruction Name", "New Obstruction", new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                }
                            }).positiveText("Save")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog dialog, DialogAction which) {
                                    FontDrawable obstructionMapMarkerFontDrawable = new FontDrawable(getBaseContext(), R.string.fa_exclamation_triangle_solid, true, false);
                                    obstructionMapMarkerFontDrawable.setTextColor(0xFFFF6700);
                                    mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(dialog.getInputEditText().getText().toString()).icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(obstructionMapMarkerFontDrawable)))).setTag("obstruction");
                                }
                            }).negativeText("Cancel")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog dialog, DialogAction which) {
                                }
                            }).positiveColor(0xFFFF6700)
                            .negativeColor(0xFFFF6700)
                            .widgetColor(0xFFFF6700)
                            .show();
                }
            }
        });
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (!marker.getTitle().equals("Current Position")) {

                    if (marker.getTag() != null && (marker.getTag().equals("restroom") || marker.getTag().equals("water fountain") || marker.getTag().equals("obstruction"))) {
                        mReportFAB.setVisibility(View.VISIBLE);
                        mReportFAB.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showReportDialog(marker);
                            }
                        });
                    }

                    if (marker.getTag() != null && !marker.getTag().equals("obstruction")) {
                        mDirectionsFAB.setVisibility(View.VISIBLE);
                        mDirectionsFAB.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + marker.getPosition().latitude + "," + marker.getPosition().longitude)));
                            }
                        });
                    }
                }

                return false;
            }
        });
        mGoogleMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(Marker marker) {
                mReportFAB.setVisibility(View.GONE);
                mDirectionsFAB.setVisibility(View.GONE);
            }
        });
        buildGoogleApiClient();
        new ShowPolyLine().execute();

        FontDrawable gpsFontDrawable = new FontDrawable(this, R.string.fa_satellite_solid, true, false);
        gpsFontDrawable.setTextColor(0xFFFFFFFF);

        FancyButton gpsFancyButton = findViewById(R.id.gpsFancyButton);
        gpsFancyButton.setIconResource(gpsFontDrawable);
        gpsFancyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGPSState = !mGPSState;

                if (mGPSState) {
                    gpsFancyButton.setText("GPS ON");
                } else {
                    gpsFancyButton.setText("GPS OFF");
                }
            }
        });

        TriStateToggleButton mapTypeTriStateToggleButton = (TriStateToggleButton) findViewById(R.id.mapTypeTriStateToggleButton);
        mapTypeTriStateToggleButton.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {
                switch (toggleStatus) {
                    case off: {
                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    }
                    case mid: {
                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    }
                    case on: {
                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    }
                }
            }
        });

        FontDrawable bathroomFontDrawable = new FontDrawable(this, R.string.fa_toilet_paper_solid, true, false);
        bathroomFontDrawable.setTextColor(0xFF000000);

        FontDrawable waterFountainFontDrawable = new FontDrawable(this, R.string.fa_tint_solid, true, false);
        waterFountainFontDrawable.setTextColor(0xFF000000);

        FontDrawable noneFontDrawable = new FontDrawable(this, R.string.fa_eye_slash, true, false);
        noneFontDrawable.setTextColor(0xFF000000);

        FontDrawable obstructionFontDrawable = new FontDrawable(this, R.string.fa_exclamation_triangle_solid, true, false);
        obstructionFontDrawable.setTextColor(0xFF000000);

        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(1).withName(R.string.bathroom_primary_drawer_item).withIcon(bathroomFontDrawable),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(2).withName(R.string.water_fountain_primary_drawer_item).withIcon(waterFountainFontDrawable),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(3).withName(R.string.none_primary_drawer_item).withIcon(noneFontDrawable),
                        new DividerDrawerItem()
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        mDrawer.closeDrawer();

                        resetMap();

                        switch ((int) drawerItem.getIdentifier()) {
                            case (1): {
                                new ShowRestrooms().execute();
                                Toast.makeText(getBaseContext(), "Showing Restrooms", Toast.LENGTH_LONG).show();

                                break;
                            }
                            case (2): {
                                new ShowWaterFountains().execute();
                                Toast.makeText(getBaseContext(), "Showing Water Fountains", Toast.LENGTH_LONG).show();

                                break;
                            } case (4): {
                                mMarkObstructionEnabled = true;
                                Toast.makeText(getBaseContext(), "Tap the map to mark where the obstruction to the trail is.", Toast.LENGTH_LONG).show();

                                break;
                            }
                        }

                        return false;
                    }
                }).build();
        mDrawer.setSelection(3);
        mDrawer.addStickyFooterItem(new PrimaryDrawerItem().withIdentifier(4).withName(R.string.obstruction_primary_drawer_item).withIcon(obstructionFontDrawable));

        FontDrawable barsFontDrawable = new FontDrawable(this, R.string.fa_bars_solid, true, false);
        barsFontDrawable.setTextColor(0xFFFFFFFF);

        FloatingActionButton navigationDrawerFAB = findViewById(R.id.navigationDrawerFAB);
        navigationDrawerFAB.setImageDrawable(barsFontDrawable);
        navigationDrawerFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawer.openDrawer();
            }
        });

        FontDrawable contactListFontDrawable = new FontDrawable(this, R.string.fa_address_book_solid, true, false);
        contactListFontDrawable.setTextColor(0xFFFFFFFF);

        FloatingActionButton contactListFAB = findViewById(R.id.contactListFAB);
        contactListFAB.setImageDrawable(contactListFontDrawable);
        contactListFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapsActivity.this, ContactListActivity.class));
            }
        });

        FontDrawable reportFontDrawable = new FontDrawable(this, R.string.fa_bug_solid, true, false);
        reportFontDrawable.setTextColor(0xFFFFFFFF);

        mReportFAB = findViewById(R.id.reportFAB);
        mReportFAB.setImageDrawable(reportFontDrawable);
        mReportFAB.setVisibility(View.GONE);

        FontDrawable directionsFontDrawable = new FontDrawable(this, R.string.fa_directions_solid, true, false);
        directionsFontDrawable.setTextColor(0xFFFFFFFF);

        mDirectionsFAB = findViewById(R.id.directionsFAB);
        mDirectionsFAB.setImageDrawable(directionsFontDrawable);
        mDirectionsFAB.setVisibility(View.GONE);

        // Preset to show up on Washington DC
        LatLng mWashingtonDC = new LatLng(38.9072, -77.0369);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mWashingtonDC, 12.0f));

        ExpandableFab landmarkExpandableFab = findViewById(R.id.landmarkExpandableFab);
        landmarkExpandableFab.setEfabColor(0xFFFF6700);

        FontDrawable landmarkMapMarkerFontDrawable = new FontDrawable(this, R.string.fa_landmark_solid, true, false);
        landmarkMapMarkerFontDrawable.setTextColor(0xFFFF6700);

        FontDrawable landmarkFABFontDrawable = new FontDrawable(this, R.string.fa_landmark_solid, true, false);
        landmarkFABFontDrawable.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        // Iwo Jima Memorial map marker
        LatLng mIwoJimaMemorial = new LatLng(38.8904, -77.0697);
        mGoogleMap.addMarker(new MarkerOptions().position(mIwoJimaMemorial).title("Iwo Jima Memorial").icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(landmarkMapMarkerFontDrawable)))).setTag("landmark");

        FabOption firstFabOption = findViewById(R.id.firstFabOption);
        firstFabOption.setImageDrawable(landmarkFABFontDrawable);
        firstFabOption.setFabOptionColor(0xFFFF6700);
        firstFabOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mIwoJimaMemorial, 15.0f));
            }
        });

        // Lincoln Memorial map marker
        LatLng mLincolnMemorial = new LatLng(38.8892, -77.0506);
        mGoogleMap.addMarker(new MarkerOptions().position(mLincolnMemorial).title("Lincoln Memorial").icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(landmarkMapMarkerFontDrawable)))).setTag("landmark");

        FabOption secondFabOption = findViewById(R.id.secondFabOption);
        secondFabOption.setImageDrawable(landmarkFABFontDrawable);
        secondFabOption.setFabOptionColor(0xFFFF6700);
        secondFabOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLincolnMemorial, 15.0f));
            }
        });

        // Washington Monument map marker
        LatLng mWashingtonMonument = new LatLng(38.8895, -77.0353);
        mGoogleMap.addMarker(new MarkerOptions().position(mWashingtonMonument).title("Washington Monument").icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(landmarkMapMarkerFontDrawable)))).setTag("landmark");

        FabOption thirdFabOption = findViewById(R.id.thirdFabOption);
        thirdFabOption.setImageDrawable(landmarkFABFontDrawable);
        thirdFabOption.setFabOptionColor(0xFFFF6700);
        thirdFabOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mWashingtonMonument, 15.0f));
            }
        });

        // US Capitol Building map marker
        LatLng mUSCapitolBuilding = new LatLng(38.8899, -77.0091);
        mGoogleMap.addMarker(new MarkerOptions().position(mUSCapitolBuilding).title("US Capitol Building").icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(landmarkMapMarkerFontDrawable)))).setTag("landmark");

        FabOption fourthFabOption = findViewById(R.id.fourthFabOption);
        fourthFabOption.setImageDrawable(landmarkFABFontDrawable);
        fourthFabOption.setFabOptionColor(0xFFFF6700);
        fourthFabOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mUSCapitolBuilding, 15.0f));
            }
        });

        // Jefferson Memorial map marker
        LatLng mJeffersonMemorial = new LatLng(38.8814, -77.0365);
        mGoogleMap.addMarker(new MarkerOptions().position(mJeffersonMemorial).title("Jefferson Memorial").icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(landmarkMapMarkerFontDrawable)))).setTag("landmark");

        FabOption fifthFabOption = findViewById(R.id.fifthFabOption);
        fifthFabOption.setImageDrawable(landmarkFABFontDrawable);
        fifthFabOption.setFabOptionColor(0xFFFF6700);
        fifthFabOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mJeffersonMemorial, 15.0f));
            }
        });
    }

    public void showReportDialog(Marker marker) {
        final String[] title = {""};
        final String[] content = {""};

        if (marker.getTag() != null) {

            if (marker.getTag().equals("restroom")) {
                title[0] = "Report Broken Restroom";
                content[0] = marker.getTitle();
            } else if (marker.getTag().equals("water fountain")) {
                title[0] = "Report Broken Water Fountain";
                content[0] = marker.getTitle();
            } else if (marker.getTag().equals("obstruction")) {
                title[0] = "Report Obstruction To Trail";
                content[0] = marker.getTitle();
            }

            new MaterialDialog.Builder(MapsActivity.this)
                    .title(title[0] + ": " + content[0])
                    .customView(R.layout.view_report, true)
                    .positiveText("Send")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            View reportView = dialog.getCustomView();
                            EditText locationEditText = reportView.findViewById(R.id.locationEditText);
                            EditText descriptionEditText = reportView.findViewById(R.id.descriptionEditText);

                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                            intent.putExtra(Intent.EXTRA_SUBJECT, title[0] + ": " + content[0]);
                            intent.putExtra(Intent.EXTRA_TEXT, locationEditText.getText().toString() + "\n\n" + descriptionEditText.getText().toString());
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        }
                    }).negativeText("Cancel")
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                        }
                    }).positiveColor(0xFFFF6700)
                    .negativeColor(0xFFFF6700)
                    .widgetColor(0xFFFF6700)
                    .show();
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * 2, bitmap.getHeight() * 2, false);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void getUserLocation() {
        FontDrawable userLocationMapMarkerFontDrawable = new FontDrawable(this, R.string.fa_running_solid, true, false);
        userLocationMapMarkerFontDrawable.setTextColor(0xFFFF6700);

        if (mGPSState) {

            try {
                mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {

                    if (location != null) {
                        mLastLocation = location;

                        if (mCurrLocationMarker != null) {
                            mCurrLocationMarker.remove();
                        }

                        //Place current location marker
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title("Current Position");
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(userLocationMapMarkerFontDrawable)));
                        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

                        //move map camera
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    } else {
                        // Handle null case or Request periodic location update https://developer.android.com/training/location/receive-location-updates
                    }
                });
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void addLandmarkDataLayer() {
        FontDrawable landmarkMapMarkerFontDrawable = new FontDrawable(this, R.string.fa_landmark_solid, true, false);
        landmarkMapMarkerFontDrawable.setTextColor(0xFFFF6700);

        // Iwo Jima Memorial map marker
        LatLng mIwoJimaMemorial = new LatLng(38.8904, -77.0697);
        mGoogleMap.addMarker(new MarkerOptions().position(mIwoJimaMemorial).title("Iwo Jima Memorial").icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(landmarkMapMarkerFontDrawable)))).setTag("landmark");

        // Lincoln Memorial map marker
        LatLng mLincolnMemorial = new LatLng(38.8892, -77.0506);
        mGoogleMap.addMarker(new MarkerOptions().position(mLincolnMemorial).title("Lincoln Memorial").icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(landmarkMapMarkerFontDrawable)))).setTag("landmark");

        // Washington Monument map marker
        LatLng mWashingtonMonument = new LatLng(38.8895, -77.0353);
        mGoogleMap.addMarker(new MarkerOptions().position(mWashingtonMonument).title("Washington Monument").icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(landmarkMapMarkerFontDrawable)))).setTag("landmark");

        // US Capitol Building map marker
        LatLng mUSCapitolBuilding = new LatLng(38.8899, -77.0091);
        mGoogleMap.addMarker(new MarkerOptions().position(mUSCapitolBuilding).title("US Capitol Building").icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(landmarkMapMarkerFontDrawable)))).setTag("landmark");

        // Jefferson Memorial map marker
        LatLng mJeffersonMemorial = new LatLng(38.8814, -77.0365);
        mGoogleMap.addMarker(new MarkerOptions().position(mJeffersonMemorial).title("Jefferson Memorial").icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(landmarkMapMarkerFontDrawable)))).setTag("landmark");
    }

    public void addNationalParkServiceTrailsDataLayer() {
        AndroidNetworking.get("https://maps2.dcgis.dc.gov/dcgis/rest/services/DCGIS_DATA/Transportation_WebMercator/MapServer/75/query?where=1%3D1&outFields=*&outSR=4326&f=json")
                .setTag("National Park Service Trails")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray features = response.getJSONArray("features");

                            for (int i = 0; i < features.length(); i++) {
                                JSONArray paths = features.getJSONObject(i).getJSONObject("geometry").getJSONArray("paths");

                                for (int j = 0; j < paths.length(); j++) {
                                    JSONArray path = paths.getJSONArray(j);
                                    List<LatLng> latLngList = new ArrayList<>();

                                    for (int k = 0; k < path.length(); k++) {
                                        double longitude = path.getJSONArray(k).getDouble(0);
                                        double latitude = path.getJSONArray(k).getDouble(1);

                                        latLngList.add(new LatLng(latitude, longitude));
                                    }

                                    mGoogleMap.addPolyline(new PolylineOptions()
                                            .addAll(latLngList)
                                            .width(10)
                                            .color(0xFFFF6700));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        error.printStackTrace();
                    }
                });
    }

    private class ShowPolyLine extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {

            try {
                progressDialog = new ProgressDialog(MapsActivity.this, R.style.ProgressDialogStyle);
                progressDialog.show();
                progressDialog.setCancelable(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                addNationalParkServiceTrailsDataLayer();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
        }

    }

    public void addRestroomDataLayer() {
        AndroidNetworking.get("https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=" + getString(R.string.google_maps_key) + "&location=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "&keyword=public%20toilet&rankby=distance")
                .setTag("Restrooms")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray results = response.getJSONArray("results");

                            for (int i = 0; i < results.length(); i++) {
                                String name = results.getJSONObject(i).getString("name");
                                double latitude = results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                                double longitude = results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");

                                FontDrawable restroomMapMarkerFontDrawable = new FontDrawable(getBaseContext(), R.string.fa_toilet_paper_solid, true, false);
                                restroomMapMarkerFontDrawable.setTextColor(0xFFFF6700);

                                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(name).icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(restroomMapMarkerFontDrawable)))).setTag("restroom");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        error.printStackTrace();
                    }
                });
    }

    private class ShowRestrooms extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {

            try {
                progressDialog = new ProgressDialog(MapsActivity.this, R.style.ProgressDialogStyle);
                progressDialog.show();
                progressDialog.setCancelable(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                addRestroomDataLayer();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
        }

    }

    public void addWaterFountainDataLayer() {
        AndroidNetworking.get("https://maps2.dcgis.dc.gov/dcgis/rest/services/DCGIS_DATA/Public_Service_WebMercator/MapServer/20/query?where=1%3D1&outFields=*&outSR=4326&f=json")
                .setTag("Water Fountains")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray features = response.getJSONArray("features");

                            for (int i = 0; i < features.length(); i++) {
                                String name = features.getJSONObject(i).getJSONObject("attributes").getString("NAME");
                                double latitude = features.getJSONObject(i).getJSONObject("geometry").getDouble("y");
                                double longitude = features.getJSONObject(i).getJSONObject("geometry").getDouble("x");

                                FontDrawable waterFountainMapMarkerFontDrawable = new FontDrawable(getBaseContext(), R.string.fa_tint_solid, true, false);
                                waterFountainMapMarkerFontDrawable.setTextColor(0xFFFF6700);

                                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(name).icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(waterFountainMapMarkerFontDrawable)))).setTag("water fountain");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        error.printStackTrace();
                    }
                });
    }

    private class ShowWaterFountains extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {

            try {
                progressDialog = new ProgressDialog(MapsActivity.this, R.style.ProgressDialogStyle);
                progressDialog.show();
                progressDialog.setCancelable(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                addWaterFountainDataLayer();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
        }

    }

    private void resetMap() {
        mGoogleMap.clear();
        addLandmarkDataLayer();
        new ShowPolyLine().execute();

    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getBaseContext());

        if (EasyPermissions.hasPermissions(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    getUserLocation();
                }
            }, 0, 1000);
        } else {
            EasyPermissions.requestPermissions(this, "Our app requires a permission to access your location", 1, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
       getUserLocation();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
}