package nwhack_sb.wheretherecipeat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Build;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

import static android.widget.SearchView.OnQueryTextListener;

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

    private List<String> ingredientsArr;
    private String inputIngredient;

    private Menu menu;

    final String PearsonAPIURL = "http://api.pearson.com/kitchen-manager/v1/recipes?name-contains=rice";

    String searchOption;
    ListView searchDisplay;
    Map<String, Recipe> recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setDisplayShowTitleEnabled(false); //TODO temp solution
        initVars();

        RecipeParser parser = new RecipeParser();
        parser.execute();
    }

    private void initVars() {
        searchDisplay = (ListView) findViewById(R.id.displaySearch);
        ingredientsArr = new ArrayList<String>();
        recipes = new HashMap<String, Recipe>();
        searchOption = "rice";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Toast.makeText(getBaseContext(),"Not implemented",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.add_ingredient:
                openIngredientInput();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void openIngredientInput(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add an Ingredient");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputIngredient = input.getText().toString();
                ingredientsArr.add(inputIngredient);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,ingredientsArr);
                searchDisplay.setAdapter(adapter);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private class RecipeParser extends AsyncTask<Void, Void, Void> {
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
                JSONArray jArr = jObj.getJSONArray("results");
                for (int i = 0; i < jArr.length(); i++) {
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
                    for (int j = 0; j < ingredientsArr.length(); j++) {
                        ingredients.add(ingredientsArr.getString(j));
                    }

                    Recipe r = new Recipe(name, id, url, cuisine, cookingMethod, ingredients, fullImage, thumbImage, null);
                    recipes.put(id, r);
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
            for (Recipe r : recipes.values()) {
                if (r.getName().toLowerCase().contains(searchOption.toLowerCase()))
                    recipeNames.add(r.getName());
            }
        }
    }

}
