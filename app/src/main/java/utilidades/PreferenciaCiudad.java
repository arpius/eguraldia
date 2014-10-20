package utilidades;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by aritz on 11/10/14.
 */
public class PreferenciaCiudad {
    private SharedPreferences prefs;

    public PreferenciaCiudad(Activity actividad) {
        prefs = actividad.getPreferences(Activity.MODE_PRIVATE);
    }

    public String getCiudad() {
        return prefs.getString("city", "Bilbao, ES");
    }

    public void setCiudad(String ciudad) {
        prefs.edit().putString("city", ciudad).commit();
    }
}
