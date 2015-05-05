package utilidades;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import biz.arpius.eguraldia.R;

/**
 * Created by aritz on 11/10/14.
 */
public class JsonRemoto {
    private static final String OPEN_WEATHER_MAP = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&lang=%s";

    public static JSONObject getJson(Context ctx, String ciudad, String idioma) {
        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP, ciudad, idioma));
            HttpURLConnection conexion = (HttpURLConnection)url.openConnection();

            conexion.addRequestProperty("x-api-key", ctx.getString(R.string.open_weather_maps_app_id));

            BufferedReader lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            StringBuffer json = new StringBuffer(1024);
            String tmp = "";

            while ((tmp = lector.readLine()) != null) {
                json.append(tmp+ "\n");
            }

            lector.close();

            JSONObject data = new JSONObject(json.toString());

            if(data.getInt("cod") != 200) return null;

            return data;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
