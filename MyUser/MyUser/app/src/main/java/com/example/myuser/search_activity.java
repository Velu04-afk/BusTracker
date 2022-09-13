package com.example.myuser;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class search_activity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private Button track;
    private Button Cb;
    private EditText busnumber;
    private String value;
    String[] destinationlist=new String[3];
    String n1,pos,n11;
    String local="https://bustracker20.azurewebsites.net";
    RequestQueue queue;
    ArrayAdapter aa1;
    Spinner dest;
    JSONArray jsonArray;
    TextView textView;
    String source,destin;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_activity);

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if (ContextCompat.checkSelfPermission(search_activity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(search_activity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(search_activity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                ActivityCompat.requestPermissions(search_activity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            if( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                buildAlertMessageNoGps();
            }
        }
        else if( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }

        track = (Button) findViewById(R.id.locTrack);
        busnumber = (EditText)findViewById(R.id.busno);
        dest= findViewById(R.id.spinner_dest);
        Cb =  (Button)findViewById(R.id.checkb);
        destinationlist[0]="Enter the last Destination";

        dest.setOnItemSelectedListener(search_activity.this);
        aa1 = new ArrayAdapter(this, R.layout.row_layout, R.id.rowtextview, destinationlist);
        dest.setAdapter(aa1);


        queue = Volley.newRequestQueue(this);

        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                n1=busnumber.getText().toString();
               // n2=dest.getText().toString();
                pos=dest.getSelectedItem().toString();

                if(n1.matches("") ){
                    Toast.makeText(search_activity.this, "Enter Bus number", Toast.LENGTH_LONG).show();
                }else if(pos.matches("select"))
                {
                    Toast.makeText(search_activity.this, "Enter Bus Destination", Toast.LENGTH_LONG).show();
                }
                else{
                    retrieveCoordinates();
                }






            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults){
        switch (requestCode){
            case 1: {
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(search_activity.this,
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

    public void checkb(View v) {

        n11=busnumber.getText().toString();
        CheckBus(n11,v);
        Toast.makeText(search_activity.this, "please wait while bus is being checked", Toast.LENGTH_LONG).show();


    }

    public void CheckBus(final String n1, final View v) {

        StringRequest strReq = new StringRequest(Request.Method.POST, "https://bustracker20.azurewebsites.net/checkBus.php", new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.e("check response", response);
                if(response.matches("not")) {

                    Toast.makeText(search_activity.this, "Bus not available", Toast.LENGTH_LONG).show();

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
                            Cb.setVisibility(v.INVISIBLE);
                            track.setVisibility(v.VISIBLE);



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


    public void retrieveCoordinates() {

        StringRequest strReq = new StringRequest(Request.Method.POST, local+"/retrieveCoordinates.php", new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("response1",response);

                if(response.matches("not")) {

                    Toast.makeText(search_activity.this, "Bus not available", Toast.LENGTH_LONG).show();

                }else{
                    Intent intent = new Intent(search_activity.this, MainActivity.class);
                    intent.putExtra("Bno", n1);
                    intent.putExtra("Destination",pos);
                    startActivity(intent);
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
                params.put("Bno", n1);
                params.put("Destination", pos);
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
