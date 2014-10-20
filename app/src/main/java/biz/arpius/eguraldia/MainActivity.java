package biz.arpius.eguraldia;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

import utilidades.JsonRemoto;
import utilidades.PreferenciaCiudad;
import utilidades.TextViewEx;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
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
            //return true;
            mostrarDialogo();
        }

        if(id == R.id.info) {
            mostrarInfo();
        }

        //return super.onOptionsItemSelected(item);
        return false;
    }

    private void mostrarInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.info);

        final TextViewEx txt = new TextViewEx(this);
        txt.setText(R.string.autor);
        txt.setTextAlign(Paint.Align.CENTER);

        builder.setView(txt);
        builder.setPositiveButton("Ok", null);
        builder.show();
    }

    private void mostrarDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.cambiar_ciudad);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cambiarCiudad(input.getText().toString());
            }
        });

        builder.show();
    }

    public void cambiarCiudad(String ciudad) {
        PlaceholderFragment fragmento = (PlaceholderFragment) getFragmentManager().findFragmentById(R.id.container);
        fragmento.cambiarCiudad(ciudad);

        new PreferenciaCiudad(this).setCiudad(ciudad);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        Typeface fuente;
        TextView ciudad, coordenadas, actualizado, descripcion, tempActual, iconoTiempo;
        TextViewEx detalles;
        Handler handler;

        public PlaceholderFragment() {
            handler = new Handler();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            ciudad = (TextView) rootView.findViewById(R.id.ciudad);
            coordenadas = (TextView) rootView.findViewById(R.id.coordenadas);
            actualizado = (TextView) rootView.findViewById(R.id.actualizado);
            detalles = (TextViewEx) rootView.findViewById(R.id.detalles);
            descripcion = (TextView) rootView.findViewById(R.id.descripcion);
            tempActual = (TextView) rootView.findViewById(R.id.temperatura_actual);
            iconoTiempo = (TextView) rootView.findViewById(R.id.icono_tiempo);

            iconoTiempo.setTypeface(fuente);

            return rootView;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            fuente = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
            actualizarDatosTiempo(new PreferenciaCiudad(getActivity()).getCiudad());
        }

        private void actualizarDatosTiempo(final String ciudad) {
            new Thread() {
                public void run() {
                    final JSONObject json = JsonRemoto.getJson(getActivity(), ciudad);

                    if(json == null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(),
                                        getActivity().getString(R.string.no_encontrado),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                representarTiempo(json);
                            }
                        });
                    }
                }
            }.start();
        }

        private void representarTiempo(JSONObject json) {
            try {
                ciudad.setText(json.getString("name").toUpperCase()+
                        " (" +json.getJSONObject("sys").getString("country")+ ")");

                coordenadas.setText("[" +json.getJSONObject("coord").getString("lon") + ", "
                        +json.getJSONObject("coord").getString("lat")+ "]");

                JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                JSONObject main = json.getJSONObject("main");
                JSONObject wind = json.getJSONObject("wind");

                descripcion.setText(details.getString("description").toUpperCase()+ "\n");

                detalles.setText(
                        getActivity().getString(R.string.humedad) +main.getString("humidity")+ " %\n"
                        + getActivity().getString(R.string.presion) +String.format("%.2f", main.getDouble("pressure"))+ " hPa\n"
                        + getActivity().getString(R.string.viento) +String.format("%.2f", wind.getDouble("speed"))+ " m/s\n"
                        + getActivity().getString(R.string.temp_min) +String.format("%.2f", main.getDouble("temp_min"))+ " ºC\n"
                        + getActivity().getString(R.string.temp_max) +String.format("%.2f", main.getDouble("temp_max"))+ " ºC\n"
                        , true
                );

                tempActual.setText(String.format("%.2f", main.getDouble("temp"))+ " ºC");

                DateFormat df = DateFormat.getDateTimeInstance();
                String estaActualizado = df.format(new Date(json.getLong("dt")*1000));
                actualizado.setText(getActivity().getString(R.string.actualizacion) +estaActualizado);

                setIconoTiempo(details.getInt("id"),
                        json.getJSONObject("sys").getLong("sunrise")*1000,
                        json.getJSONObject("sys").getLong("sunset")*1000);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private void setIconoTiempo(int idActual, long amanecer, long anochecer) {
            int id = idActual/100;
            String icono = "";

            if(idActual == 800) {
                long horaActual = new Date().getTime();

                if(horaActual >= amanecer && horaActual<anochecer) {
                    icono = getActivity().getString(R.string.soleado);
                }
                else {
                    icono = getActivity().getString(R.string.despejado_noche);
                }
            }
            else {
                switch (id) {
                    case 2:
                        icono = getActivity().getString(R.string.tormenta);
                        break;
                    case 3:
                        icono = getActivity().getString(R.string.xirimiri);
                        break;
                    case 5:
                        icono = getActivity().getString(R.string.lluvia);
                        break;
                    case 6:
                        icono = getActivity().getString(R.string.nieve);
                        break;
                    case 7:
                        icono = getActivity().getString(R.string.niebla);
                        break;
                    case 8:
                        icono = getActivity().getString(R.string.nuboso);
                        break;
                    case 9:
                        icono = getActivity().getString(R.string.granizo);
                        break;
                }
            }

            iconoTiempo.setText(icono);
        }

        public void cambiarCiudad(String ciudad) {
            actualizarDatosTiempo(ciudad);
        }
    }
}
