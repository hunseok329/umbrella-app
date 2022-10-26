package com.example.umbrella;

import android.app.Activity;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.umbrella.databinding.FragmentFirstBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

// 페이지 시작
public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    private double lat;
    private double lon;
    private boolean rain_bool;
    private String API_KEY = "siiOw9ZfDhBZ3QgLxYlOKTu4dloUBDKGSP6SkeU1nbZdq72vsB3SREoLX4xP4l%2FA9pUayhKSQsB0Ea9CAqCR%2BA%3D%3D";
    private String target_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // page binding this FirstFragment
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        System.out.println("TEST: onCreateView");
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println("TEST: onViewCreated");

        // go to SecondFragment button
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onStart() {
        super.onStart();
        lat = ((MainActivity) getActivity()).getLat();
        lon = ((MainActivity) getActivity()).getLon();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int[] grid = mapToGrid(lat, lon);

                    long now = System.currentTimeMillis();
                    Date date = new Date(now);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    String getTime = dateFormat.format(date);

                    // 0: hour, 1: min, 2: sec
                    String[] hour_min_list = getTime.split(":");
                    String get_base_time = hour_min_list[0] + "00";

                    //base time 0200, 0500, 0800, 1100, 1400, 1700, 2000, 2300
                    Integer[] base_time = {200, 500, 800, 1100, 1400, 1700, 2000, 2300};
                    Integer int_base_time = Integer.parseInt(get_base_time);


                    ArrayList<Integer> time_stack = new ArrayList<>();
                    // API base time check
                    for (Integer time : base_time){
                        Integer temp_time = time + 10;
                        if (int_base_time >= temp_time) {
                            time_stack.add(time);
                        }
                    }

                    String result_time = "";
                    if (time_stack.isEmpty()) {
                        result_time = getWeatherData(grid[0], grid[1], base_time[0]);
                    } else {
                        result_time = getWeatherData(grid[0], grid[1], time_stack.get(time_stack.size() - 1 ));
                    }

                    JSONObject obj = new JSONObject(result_time);
                    JSONObject response = (JSONObject)obj.get("response");
                    JSONObject body = (JSONObject)response.get("body");
                    JSONObject items = (JSONObject)body.get("items");
                    JSONArray item = (JSONArray)items.get("item");

                    rain_bool = rain_check(item, hour_min_list[0]);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        if (rain_bool) {
            binding.testTextView.setText("우산을 챙겨가세요");
            binding.imageView.setImageResource(R.drawable.rain);
        } else {
            binding.testTextView.setText("비가 올 확률이 낮아요");
            binding.imageView.setImageResource(R.drawable.sun);
        }

        System.out.println("TEST: onStart");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getWeatherData(int xlat, int ylon, int baseTime) throws IOException {
        String numOfRows = "1000";
        String pageNo = "1";
        String dataType = "JSON";
        String base_time = "";

        if (baseTime < 1000) {
            base_time = "0" + String.valueOf(baseTime);
        } else {
            base_time = String.valueOf(baseTime);
        }


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
        queryURL.append("&" + URLEncoder.encode("base_time","UTF-8") + "=" + URLEncoder.encode(base_time, "UTF-8")); /*05시 발표*/
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

    public boolean rain_check(JSONArray item, String hour) throws JSONException {

        boolean PTY_rain = false;

        //아마 String 4자리
        String base_time = hour + "00";
        //12시간 확인 변수
        int test_cnt = 0;


        for (int i = 0; i < item.length(); i++){
            JSONObject temp = item.getJSONObject(i);
            //카테고리 추출
            String category = (String) temp.get("category");


            if (test_cnt >= 12){
                break;
            }
            //pop 강수 확률 50% 이상 확인
            if (category.equals("POP")){
                String pop = (String) temp.get("fcstValue");
                Integer pop_int = Integer.parseInt(pop);
                if (pop_int >= 50) {
                    PTY_rain = true;
                }
            }

            //강수 형태 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
            if (category.equals("PTY")){
                String pty = (String) temp.get("fcstValue");
                if (!pty.equals("0")){
                    PTY_rain = true;
                }
                test_cnt += 1;
            }
        }

        return PTY_rain;
    }

    //위도 경도 => X, Y 변환
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
}