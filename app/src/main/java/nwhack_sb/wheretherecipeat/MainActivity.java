package nwhack_sb.wheretherecipeat;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity {

    final String PearsonAPIURL = "http://api.pearson.com/kitchen-manager/v1/recipes?name-contains=rice";

    String searchOption;
    ListView searchDisplay;
    Map<String,Recipe> recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVars();

        RecipeParser parser = new RecipeParser();
        parser.execute();
    }

    private void initVars() {
        searchDisplay = (ListView) findViewById(R.id.displaySearch);
        recipes = new HashMap<String, Recipe>();
        searchOption = "rice";
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

    private class RecipeParser extends AsyncTask<Void,Void,Void>{
        private InputStream is;
        private String json;
        private JSONObject jObj;

        @Override
        protected Void doInBackground(Void... arg0) {
            // Make Http request
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            try {
                response = httpclient.execute(new HttpGet(PearsonAPIURL));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    HttpEntity httpEntity = response.getEntity();
                    is = httpEntity.getContent();
                } else{
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
                while ((line = reader.readLine()) != null){
                    builder.append(line);
                }
                is.close();
                json = builder.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try{
                jObj = new JSONObject(json);
                JSONArray jArr = jObj.getJSONArray("results");
                for (int i = 0; i < jArr.length(); i++){
                    JSONObject result = jArr.getJSONObject(i);

                    String id = result.getString("id");
                    String name = result.getString("name");
                    String url = result.getString("url");
                    String cuisine = result.getString("cuisine");
                    String cookingMethod = result.getString("cooking_method");
                    String fullImage = result.getString("image");
                    String thumbImage = result.getString("thumb");
                    List<String> ingredients = new ArrayList<String>();

                    JSONArray ingredientsArr = result.getJSONArray("ingredients");
                    for (int j = 0; j < ingredientsArr.length(); j++){
                        ingredients.add(ingredientsArr.getString(j));
                    }

                    Recipe r = new Recipe(name, id, url, cuisine, cookingMethod, ingredients, fullImage, thumbImage, null);
                    recipes.put(id,r);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            List<String> recipeNames = new ArrayList<String>();
            for (Recipe r : recipes.values()){
                if (r.getName().toLowerCase().contains(searchOption.toLowerCase()))
                    recipeNames.add(r.getName());
            }

            // Displaying into ListView
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this ,android.R.layout.simple_list_item_1, recipeNames);
            searchDisplay.setAdapter(adapter);
        }
    }

}
