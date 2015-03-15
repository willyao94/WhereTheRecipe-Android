package nwhack_sb.wheretherecipeat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
import org.apache.http.client.methods.HttpUriRequest;
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
import java.util.Map;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;


public class MainActivity extends Activity {

    private List<String> ingredientsArr;
    private String inputIngredient;
    private String cameraPicPath;

    private static final String PEARSONAPIURL = "http://api.pearson.com/kitchen-manager/v1/recipes?ingredients-any=";
    private static final String IMAGGAAPIURL = "http://api.imagga.com/v1/tagging?url=";

    private static final String PARSEAPPLICATIONKEY = "YKowzej1GqPXBvD06scKLHgudRl332k2ArAcgqaN";
    private static final String PARSECLIENTKEY = "RyxUkwCQASnnOQbH5mjL3xCJku9H1emYS8eh2jQT";
    private static final String IMAGGAKEY = "acc_8559dece0d6b2e1";
    private static final String IMAGGASECRET = "e5108c4a74bd48c165841a3c3c5cb825";

    ListView searchDisplay;
    Map<String, Recipe> recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Enable Local Datastore
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, PARSEAPPLICATIONKEY, PARSECLIENTKEY);

        getActionBar().setDisplayShowTitleEnabled(false); //TODO temp solution
        initVars();
    }

    private void initVars() {
        searchDisplay = (ListView) findViewById(R.id.displaySearch);
        searchDisplay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, final int position, long l) {
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
                        ArrayAdapter<String> test = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,ingredientsArr);
                        searchDisplay.setAdapter(adapter);
                    }
                });
                builder.show();
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
            case R.id.action_camera:
                openCameraScan();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openCameraScan() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 2);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2 && data != null) {
            try{
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                ParseObject saveObj = new ParseObject("ImagesForRecognition");
                ParseFile parseBtyeArr = new ParseFile(byteArray);
                saveObj.put("cameraImage", parseBtyeArr);
                saveObj.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("ImagesForRecognition");
                        query.orderByDescending("createdAt");
                        query.getFirstInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parseObject, ParseException e) {
                                ParseFile retrieveFile = parseObject.getParseFile("cameraImage");
                                String apiRequest = IMAGGAAPIURL + retrieveFile.getUrl();
                                ImageParser parser = new ImageParser();
                                parser.execute(apiRequest);
                            }
                        });
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void openSubmitSearch() {
        // TODO: Read from ListView
        List<String> ingList = new ArrayList<String>();
        ListAdapter adapter = searchDisplay.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++){
            ingList.add((String)adapter.getItem(i));
        }

        String search = PEARSONAPIURL;
        for(String s: ingList) {
            search = search.concat(s);
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

    private class ImageParser extends AsyncTask<String, Void, Void> {
        private InputStream is;
        private String json;
        private JSONObject jObj;
        private String bestTag;
        private Double topConfidence;

        @Override
        protected Void doInBackground(String... arg0) {
            // Make Http request
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            try {
                HttpUriRequest request = new HttpGet(arg0[0]); // Or HttpPost(), depends on your needs
                String credentials = IMAGGAKEY + ":" + IMAGGASECRET;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT).replace("\n", "");
                request.addHeader("Authorization", "Basic " + base64EncodedCredentials);

                response = httpclient.execute(request);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity httpEntity = response.getEntity();
                    is = httpEntity.getContent();
                } else {
                    //Closes the connection
                    Toast.makeText(MainActivity.this,"Failed to connect",Toast.LENGTH_SHORT).show();
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
                JSONArray jResultsArr = jObj.getJSONArray("results");
                JSONObject jTags = jResultsArr.getJSONObject(0);
                JSONArray allTags = jTags.getJSONArray("tags");
                for (int i=0; i<allTags.length();i++){
                    JSONObject result = allTags.getJSONObject(i);

                    Double confidence = (Double) result.get("confidence");
                    String tag = result.getString("tag");

                    bestTag = tag;
                    topConfidence = confidence;
                    break; //TODO: implement filtering of non food items
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (bestTag == null)
                bestTag = "Failed to find anything";
            Toast.makeText(MainActivity.this, bestTag, Toast.LENGTH_SHORT).show();
        }
    }

}
