package com.example.umbrella;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.umbrella.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private double lat;
    private double lon;
    private String API_KEY = "siiOw9ZfDhBZ3QgLxYlOKTu4dloUBDKGSP6SkeU1nbZdq72vsB3SREoLX4xP4l%2FA9pUayhKSQsB0Ea9CAqCR%2BA%3D%3D";
    private String target_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
    private String target_time = "0500"; //default

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater()); // binding 변수에 XML에 접근할 수 있는 객체 선언
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        //위치 정보
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions( MainActivity.this, new String[] {
                android.Manifest.permission.ACCESS_FINE_LOCATION}, 0 );
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            lat = location.getLatitude();
            lon = location.getLongitude();

            int[] grid = mapToGrid(lat, lon);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                       String result = getWeatherData(grid[0], grid[1]);

                       JSONObject obj = new JSONObject(result);
                       JSONObject response = (JSONObject)obj.get("response");
                       JSONObject body = (JSONObject)response.get("body");
                       JSONObject items = (JSONObject)body.get("items");
                       JSONArray item = (JSONArray)items.get("item");

                       boolean rain_bool = rain_check(item);
                       if (rain_bool){
                           System.out.println("TEST code rain bool : true");
                       } else {
                           System.out.println("TEST code rain bool : false");
                       }

                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               binding.testView.setText("test code");
                           }
                       });
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


//            binding.testView.setText("위치 정보" + lat + " = " + lon);
            System.out.println("위도 경도 : " + lat + " = " + lon);
        } else {
            System.out.println("location null");
        }


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public double getLat() {
        return lat;
    }
    public double getLon() { return lon;}

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getWeatherData(int xlat, int ylon) throws IOException {
        String numOfRows = "1000";
        String pageNo = "1";
        String dataType = "JSON";

        //date
        SimpleDateFormat real_time = new SimpleDateFormat("yyyyMMdd");
        Date time = new Date();
        String base_date = real_time.format(time);

        StringBuilder queryURL = new StringBuilder(target_URL);
        queryURL.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + API_KEY);
        queryURL.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" +URLEncoder.encode(pageNo, "UTF-8"));
        queryURL.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode(numOfRows, "UTF-8"));
        queryURL.append("&" + URLEncoder.encode("dataType","UTF-8") + "=" + URLEncoder.encode(dataType, "UTF-8")); /*요청자료형식(XML/JSON) Default: XML*/
        queryURL.append("&" + URLEncoder.encode("base_date","UTF-8") + "=" + URLEncoder.encode(base_date, "UTF-8")); /*‘21년 6월 28일발표*/
        queryURL.append("&" + URLEncoder.encode("base_time","UTF-8") + "=" + URLEncoder.encode("0500", "UTF-8")); /*05시 발표*/
        queryURL.append("&" + URLEncoder.encode("nx","UTF-8") + "=" + URLEncoder.encode(String.valueOf(xlat), "UTF-8")); /*예보지점의 X 좌표값*/
        queryURL.append("&" + URLEncoder.encode("ny","UTF-8") + "=" + URLEncoder.encode(String.valueOf(ylon), "UTF-8")); /*예보지점의 Y 좌표값*/

        URL url = new URL(queryURL.toString());
        System.out.println(url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        System.out.println(sb.toString());
        return sb.toString();
    }

    public int[] mapToGrid(double lat, double lon){
        int[] grid = new int[2];

        double RE = 6371.00977;
        double GRID = 5.0;
        double SLAT1 = 30.0;
        double SLAT2 = 60.0;
        double OLON = 126.0;
        double OLAT = 38.0;
        double XO = 43;
        double YO = 136;

        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);


        double ra = Math.tan(Math.PI * 0.25 + (lat) * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lon * DEGRAD - olon;
        if ( theta > Math.PI) theta -= 2.0 * Math.PI;
        if ( theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;
        double x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
        double y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);

        grid[0] = (int)x;
        grid[1] = (int)y;
        return grid;
    }

    public boolean rain_check(JSONArray item) throws JSONException {

        boolean PTY_rain = false;
        String start_time = (String) item.getJSONObject(0).get("fcstTime");
        String end_time = "";
        System.out.println(start_time);
        int test_cnt = 0;

        for (int i = 0; i < item.length(); i++){
            JSONObject temp = item.getJSONObject(i);
            //카테고리 추출
            String category = (String) temp.get("category");
            if (test_cnt > 12){
                break;
            }
            //pop 강수 확률
            if (category.equals("POP")){
                String pop = (String) temp.get("fcstValue");
                end_time = (String) temp.get("fcstTime");
            }

            //강수 형태 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
            if (category.equals("PTY")){
                String pty = (String) temp.get("fcstValue");
                test_cnt += 1;
            }
        }
        System.out.println(test_cnt);
        System.out.println(end_time);

        return PTY_rain;
    }
}