package com.example.myuser;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Map;

public class MainActivity extends FragmentActivity implements LocationListener,OnMapReadyCallback{

    private GoogleMap mMap;
    private GoogleMap mMap1;
    private Double lat,lng;
    String Bno,Destination;
    Button button;
    int click=0;
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private String s_url="https://bustracker20.azurewebsites.net/retrieveCoordinates.php";
    LocationManager locationManager;
    Location location;
    Double userlat,userlng;
    LatLng userlatlng;
    LatLng nearlatlng;


    Double array1[];
    Double array2[];
    Double disarray[];
    int temp;

    JSONArray jsonArray;

    private static final String TAG = MainActivity.class.getSimpleName();
    GoogleApiClient mLocationClient;
    LocationRequest mLocationRequest = new LocationRequest();

    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        button= findViewById(R.id.nearest);
        array1 = new Double[100];
        array2 = new Double[100];

        disarray = new Double[100];


        Bundle bundle = getIntent().getExtras();
        Bno  = bundle.getString("Bno");
        Destination= bundle.getString("Destination");
        Log.e("Received",Bno);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Log.e("pressed","press");

                    click=1;

                for(int i=0;i<jsonArray.length();i++){
                    Log.e("hello2", String.valueOf(jsonArray.length()));
                    double dis=  haversine(userlat,userlng,array1[i],array2[i]);
                    disarray[i]=dis;

                   Log.e("ankeetmc", String.valueOf(dis));
                }
                temp=0;
                for (int i=1;i<jsonArray.length();i++){
                    if (disarray[temp]>disarray[i]){
                        temp=i;
                        Log.e("iteration", String.valueOf(temp));
                    }else{
                        Log.e("iteration", "no");

                    }
                }

                nearlatlng= new LatLng(array1[temp],array2[temp]);
                if (click==1) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nearlatlng, 14f));
                }
            }
        });
    }



    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.e("started","started");
         mMap = googleMap;
         mMap1= googleMap;

         final Handler handler = new Handler();
         final int delay = 5 *1000;


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.

            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location != null) {
                Log.d("lath", String.valueOf(location.getLatitude()));

                userlat = location.getLatitude();
                userlng = location.getLongitude();
                userlatlng = new LatLng(location.getLatitude(), location.getLongitude());

                if (userlatlng.equals("")){
                    Log.e("hello","hello");
                }else{
                    Log.e("hello","no");
                    Log.e("hello", String.valueOf(userlatlng));
                }

            }else{
                Log.e("location null ?","yes");
            }







        }

         newLocation();

         handler.postDelayed(new Runnable() {
             @Override
             public void run() {
                 Log.e("Call","call");
                 googleMap.clear();
                 newLocation();
                 handler.postDelayed(this,delay);
             }
         },delay);

    }


    public void newLocation()
    {

        mRequestQueue = Volley.newRequestQueue(this);
        mStringRequest = new StringRequest(Request.Method.POST, s_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Fvolleyresponse",response);
                try {
                     jsonArray= new JSONArray(response);
                    Log.e("Json array", String.valueOf(jsonArray));
                    DecimalFormat df = new DecimalFormat("#.##");
                    for (int i=0;i<jsonArray.length();i++) {

                        JSONObject jsonObject =jsonArray.getJSONObject(i);

                        lat = Double.valueOf(jsonObject.getString("latitude"));
                        lng = Double.valueOf(jsonObject.getString("longitude"));

                        array1[i]=lat;
                        array2[i]=lng;
                        double dis=  haversine(userlat,userlng,array1[i],array2[i]);
                        Log.e("Finalresponse", "is" + response);

                        LatLng liveBus = new LatLng(lat,lng);

                        mMap.addMarker(new MarkerOptions().position(userlatlng).title("User marker")
                                .icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_directions_walk_black_24dp)));
                        if (click==0) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userlatlng, 12f));
                        }
                     mMap.addMarker(new MarkerOptions().position(liveBus).title("Your Bus is approximate "+df.format(dis) +"km")
                                .icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_directions_bus_black_24dp)));


                    }





                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        },  new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error","Error: "+error.toString());
                error.printStackTrace();
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Bno", Bno);
                params.put("Destination", Destination);

                return params;
            }
        };




        mRequestQueue.add(mStringRequest);
    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId){
        Drawable vectorDrawable = ContextCompat.getDrawable(context,vectorResId);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    public double haversine(double lat1, double lon1,

                            double lat2, double lon2)

    {

        // distance between latitudes and longitudes

        double dLat = Math.toRadians(lat2 - lat1);

        double dLon = Math.toRadians(lon2 - lon1);



        // convert to radians

        lat1 = Math.toRadians(lat1);

        lat2 = Math.toRadians(lat2);



        // apply formulae

        double a = Math.pow(Math.sin(dLat / 2), 2) +

                Math.pow(Math.sin(dLon / 2), 2) *

                        Math.cos(lat1) *

                        Math.cos(lat2);

        double rad = 6371;

        double c = 2 * Math.asin(Math.sqrt(a));

        return rad * c;

    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d("lat", String.valueOf(location.getLatitude()));

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
