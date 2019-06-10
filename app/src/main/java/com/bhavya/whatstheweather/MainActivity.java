package com.bhavya.whatstheweather;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    EditText cityName;
    TextView summary;
    TextView temp;
    String t;
    Double calc;
    double i;

    //Function to convert city names to-> lat and lang
    public ArrayList<LatLng> getLat()
    {
        String city=cityName.getText().toString();
        if(Geocoder.isPresent()){
            try {
                String locationcity = city;
                Geocoder gc = new Geocoder(this);
                List<Address> addresses= gc.getFromLocationName(locationcity, 5); // get the found Address Objects

                ArrayList<LatLng> ll = new ArrayList<LatLng>(addresses.size()); // A list to save the coordinates if they are available
                for(Address a : addresses){
                    if(a.hasLatitude() && a.hasLongitude()){
                        ll.add(new LatLng(a.getLatitude(), a.getLongitude()));
                    }
                    //It returns the array of latlang which has one element consisting city's coordinates
                    return ll;
                }
            } catch (IOException e) {
                return null;
                // handle the exception
            }
        }
        return  null;
    }


//Function to find weather on clicking button;

    public void findWeather(View v) {
        //It saves the array of latlang
        ArrayList<LatLng> ll=getLat();

        LatLng lat=ll.get(0);
        //I have to convert it into latitude and longitude otherwise lat[0] =(x,y)
        double lati=lat.latitude;//x
        double longi=lat.longitude;//y
        DownloadTask task = new DownloadTask();
        String r = null;

        try {//this command send the url with api key and lat ,lang of cities to download the weather info
            r = task.execute("https://api.darksky.net/forecast/4e9dab46af60793d6ec4f06372ea6a2e/" + lati + "," + longi).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }


    }



    @SuppressLint("StaticFieldLeak")
    public class DownloadTask extends AsyncTask<String,Void,String> {
        String result="";
        String r=null;

        @Override
        protected String doInBackground(String... urls) {



            try {
                URL url = new URL(urls[0]);

                HttpsURLConnection urlconnection = (HttpsURLConnection) url.openConnection();
                InputStream in = urlconnection.getInputStream();//download the data
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                if (urlconnection.getResponseCode() != 200)
                    throw new Exception("Failed to connect");
                while (data != -1) {
                    char curr = (char) data;
                    result += curr;
                    if(result.indexOf("Temperature")>0)
                    {
                        //I have taken only the 1 or 2 lines of data and then break the loop because it was taking a long time to get all the data
                        break;
                    }

                    data = reader.read();
                }

                return result;
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
                return "Fail";
            } catch (IOException e1) {
                e1.printStackTrace();
                return "Faillll";
            } catch (Exception e) {
                e.printStackTrace();
                return "FAIL";
            }

        }

        @Override
        protected void onPostExecute(String result) {


            super.onPostExecute(result);
            Pattern p=Pattern.compile("summary\":\"(.*?)\",\"icon\"");//this is the pattern
            Matcher m=p.matcher(result);//above pattern will be searched in result
            while(m.find())
            {
                summary.setText("Summary : " + m.group(1));
            }
            Pattern p1=Pattern.compile("temperature\":(.*?),\"apparentTemperature");
            Matcher m1=p1.matcher(result);
            while(m1.find())
            {
                t=m1.group(1);
            }
            i=Double.valueOf(t);

            calc=Double.valueOf((((i-32)*5))/9);//fahrenite to celcius
            DecimalFormat df=new DecimalFormat("#.##");//trim it into 2 decimal places

            Log.i("calc",String.valueOf(calc));
            temp.setText("Temperature : " + String.valueOf(df.format(calc)) + "°C");

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cityName=(EditText)findViewById(R.id.edit);
        temp=(TextView)findViewById(R.id.text2);
        summary=(TextView)findViewById(R.id.text1);
    }
}
