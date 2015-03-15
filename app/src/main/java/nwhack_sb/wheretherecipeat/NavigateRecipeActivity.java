package nwhack_sb.wheretherecipeat;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kingeric0201 on 2015-03-14.
 */
public class NavigateRecipeActivity extends Activity {

    //Bundle hi = getIntent().getExtras();
    //String value1 = hi.getString("test");
    private String url;
    private TextView directions;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        directions = (TextView) findViewById(R.id.displayRecipe);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("recipeURL_String");
            url = value;
            //Toast.makeText(getApplicationContext(),value, Toast.LENGTH_SHORT).show();
            //String recipeURL = value;
        }
        RecipeStepParser parser = new RecipeStepParser();
        parser.execute(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private class RecipeStepParser extends AsyncTask<String, Void, Void> {
        private InputStream is;
        private String json;
        private JSONObject jObj;
        private String dir;

        @Override
        protected Void doInBackground(String... arg0) {
            // Make Http request
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            try {
                response = httpclient.execute(new HttpGet(arg0[0]));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity httpEntity = response.getEntity();
                    is = httpEntity.getContent();
                } else {
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Read Http request
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                is.close();
                json = builder.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                jObj = new JSONObject(json);
                JSONArray dirArray = jObj.getJSONArray("directions");
                StringBuilder builder = new StringBuilder();
                for (int i=0;i<dirArray.length();i++){
                    builder.append(dirArray.getString(i));
                }
                dir = builder.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            directions.setText(dir);
        }
    }

}
