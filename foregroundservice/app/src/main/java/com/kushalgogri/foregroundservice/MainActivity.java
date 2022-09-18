package com.kushalgogri.foregroundservice;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    int i = 0;
    int check;
    RequestQueue queue;
    EditText editText1,editText2;
    TextView textView;
    Button button1,button2;
    String destinationlist[]= new String[3];
    String source,destin;

    String n1,b1,n11;
    Spinner dest;
    JSONArray jsonArray;

    ArrayAdapter aa1;
    String pos;
    String local="https://bustracktest.azurewebsites.net";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            if( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                buildAlertMessageNoGps();
            }
        }
        else if( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }

        editText1= findViewById(R.id.busno);
        editText2= findViewById(R.id.name);


        n1=editText1.getText().toString();
        b1=editText2.getText().toString();
        destinationlist[0]="Add Destination Here";

        button1=findViewById(R.id.stop);
        button2=findViewById(R.id.start);

        dest= findViewById(R.id.spinner_dest);

        dest.setOnItemSelectedListener(MainActivity.this);
        aa1 = new ArrayAdapter(this, R.layout.row_layout, R.id.rowtextview, destinationlist);
        dest.setAdapter(aa1);


        queue = Volley.newRequestQueue(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        final String latitude = intent.getStringExtra(ExampleService.EXTRA_LATITUDE);
                        final String longitude = intent.getStringExtra(ExampleService.EXTRA_LONGITUDE);

                        Log.e("lat",latitude);
                        if ((latitude != null) && (longitude != null) && (i == 0) && (check == 0)) {
                            Log.e("lat", latitude);
                            insertcoordinates(Double.parseDouble(latitude), Double.parseDouble(longitude));
                            i = 1;
                        }
                        if((latitude != null) && (longitude != null) && (i == 1) && (check == 0)){

                            Log.e("lath", latitude);
                            updatecoordinates(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        }
                        if(check==1){
                            Log.e("stop","stop button pressed");
                        }


                    }
                }, new IntentFilter(ExampleService.ACTION_LOCATION_BROADCAST)
        );



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults){
        switch (requestCode){
            case 1: {
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public void startService(View v) {


        n1=editText1.getText().toString();
        b1=editText2.getText().toString();
        pos=dest.getSelectedItem().toString();
        Log.e("pos",pos);
        Log.e("n1",n1);
        Log.e("b1",b1);


        if(n1.matches("") || b1.matches("") || pos.matches("select")){
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
        }else{

           Intent serviceIntent = new Intent(this, ExampleService.class);
           ContextCompat.startForegroundService(this, serviceIntent);
           Log.e("bus no",n1);
           button1.setVisibility(v.VISIBLE);
           button2.setVisibility(v.INVISIBLE);
           check=0;
       }

    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    public void check(View v) {

        Toast.makeText(this,"Wait for a second",Toast.LENGTH_LONG).show();
        n11=editText1.getText().toString();
        CheckBus(n11,v);

    }


    public void stopService(View v) {
        Intent serviceIntent = new Intent(this, ExampleService.class);
        stopService(serviceIntent);
        Log.e("Stop","Service stopped");
        button2.setVisibility(v.VISIBLE);
        button1.setVisibility(v.INVISIBLE);

        deletecoordinates();

        check = 1;
    }


    public void CheckBus(final String n1, final View v) {


        StringRequest strReq = new StringRequest(Request.Method.POST, local+"/checkBus.php", new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.e("check response", response);
                if(response.matches("not")) {

                    Toast.makeText(MainActivity.this, "Bus not available", Toast.LENGTH_LONG).show();

                }else {
                    try {
                        jsonArray = new JSONArray(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.e("Json array", String.valueOf(jsonArray));
                    for (int i = 0; i < jsonArray.length(); i++) {


                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            source = (jsonObject.getString("Source"));
                            destin = jsonObject.getString("Destination");
                            destinationlist[1]=source;
                            destinationlist[2]=destin;
                            dest.setVisibility(v.VISIBLE);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }

            }
        }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("track error", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("Busno", n1);
                return params;
            }
        };
        queue.add(strReq);
    }



    public void insertcoordinates(final Double latitude, final Double longitude) {

        StringRequest strReq = new StringRequest(Request.Method.POST, local+"/insertcoordinates.php", new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("insert response", response);
                //manager.StoreProfile(true);


            }
        }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("track error", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("Bno", n1);
                params.put("Name", b1);
                params.put("Destination", pos);
                params.put("latitude", String.valueOf(latitude));
                params.put("longitude", String.valueOf(longitude));
                Log.e("hashmap", String.valueOf(params));
                return params;
            }
        };
        queue.add(strReq);


    }

    public void updatecoordinates(final Double latitude, final Double longitude) {

        StringRequest strReq = new StringRequest(Request.Method.POST, local+"/updatecoordinates.php", new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("update response", response);


            }
        }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("track error", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("Bno", n1);
                params.put("Name", b1);
                params.put("Destination", pos);
                params.put("latitude", String.valueOf(latitude));
                params.put("longitude", String.valueOf(longitude));
                Log.e("hashmap", String.valueOf(params));
                return params;
            }
        };
        queue.add(strReq);

    }




    public void deletecoordinates() {

        StringRequest strReq = new StringRequest(Request.Method.POST, local+"/deletecoordinates.php", new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                    Log.e("delete","Row Deleted");

            }
        }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("track error", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("Bno", n1);
                params.put("Name", b1);
                params.put("Destination", pos);
                Log.e("hashmap", String.valueOf(params));
                return params;
            }
        };
        queue.add(strReq);

    }




    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
