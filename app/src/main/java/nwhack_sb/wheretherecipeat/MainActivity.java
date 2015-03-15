package nwhack_sb.wheretherecipeat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
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
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.Date;
import java.util.List;

import static android.widget.SearchView.OnQueryTextListener;
import static nwhack_sb.wheretherecipeat.R.id.displaySearch;

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

    final String PearsonAPIURL = "http://api.pearson.com/kitchen-manager/v1/recipes?ingredients-any=";
    ListView searchDisplay;
    Map<String, Recipe> recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setDisplayShowTitleEnabled(false); //TODO temp solution
        initVars();
    }

    private void initVars() {
        searchDisplay = (ListView) findViewById(displaySearch);
        searchDisplay.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Intent myIntent = new Intent(view.getContext(), NavigateRecipeActivity.class);
                //myIntent.putExtra("test", "hello");
                //startActivity(myIntent);
                //Cursor c = (Cursor) searchDisplay.getItemAtPosition(position);
                String recipeURL = "";

                String value = searchDisplay.getAdapter().getItem(position).toString();
                for(Recipe r: recipes.values()){
                    if(r.getName().equals(value)){
                        recipeURL = r.getRecipeURL();
                        break;
                    }
                }
                Intent myIntent = new Intent(view.getContext(), NavigateRecipeActivity.class);
                myIntent.putExtra("recipeURL_String", recipeURL);
                startActivity(myIntent);

                //Toast.makeText(getApplicationContext(),recipeURL,Toast.LENGTH_SHORT).show();
            }
        });
        searchDisplay.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int position, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Delete?");
                builder.setMessage("Are you sure you want to delete " + position);
                final int positionToRemove = position;
                builder.setNegativeButton("Cancel", null);
                List<String> temp = new ArrayList<String>();
                final ListAdapter adapter = searchDisplay.getAdapter();
                builder.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ingredientsArr.remove(positionToRemove);
                        ArrayAdapter<String> test = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, ingredientsArr);
                        searchDisplay.setAdapter(adapter);
                    }
                });
                builder.show();
                return false;
            }
        });
        ingredientsArr = new ArrayList<String>();
        recipes = new HashMap<String, Recipe>();
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
                openSubmitSearch();
                return true;
            case R.id.add_ingredient:
                openIngredientInput();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSubmitSearch() {
        // TODO: Read from ListView
        List<String> ingList = new ArrayList<String>();
        ListAdapter adapter = searchDisplay.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++){
            ingList.add((String)adapter.getItem(i));
        }

        String search = PearsonAPIURL;
        for(String s: ingList) {
            search = search.concat(s.trim());
            if ((ingList.size()-1) != ingList.indexOf(s)){
                search = search.concat("%2C");
            }
        }
        RecipeParser parser = new RecipeParser();
        parser.execute(search);
        // Remove previous search inputs
        ingredientsArr.clear();
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

    private class RecipeParser extends AsyncTask<String, Void, Void> {
        private InputStream is;
        private String json;
        private JSONObject jObj;

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
                    recipeNames.add(r.getName());
            }

            // Displaying into ListView
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this ,android.R.layout.simple_list_item_1, recipeNames);
            searchDisplay.setAdapter(adapter);
        }
    }

}
