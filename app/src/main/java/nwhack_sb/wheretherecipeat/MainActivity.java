package nwhack_sb.wheretherecipeat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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

import java.util.List;

import static android.widget.SearchView.OnQueryTextListener;


public class MainActivity extends Activity {

    private List<String> items;
    ClipData.Item searchBar;
    private EditText editText;
    private Button btnNext;


    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnNext = (Button) findViewById(R.id.SearchButton);
        btnNext.setEnabled(true);

        editText = (EditText) findViewById(R.id.search);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                Toast.makeText(getBaseContext(), "test", Toast.LENGTH_SHORT).show();
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Toast Message",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.LEFT, 0, 0);
                    toast.show();

                    btnNext.setEnabled(true);

                }
                return false;
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast toast = Toast.makeText(getApplicationContext(), "Toast Message",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP | Gravity.LEFT, 0, 0);
                toast.show();
            }
        });
    }




        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

//    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

//        return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_search:
                openSearch();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void openSearch() {
//        startActivity(new Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH));
        Toast toast = Toast.makeText(this, "Your toast message.",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.LEFT, 0, 0);
        toast.show();

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener((OnQueryTextListener) this);

        searchView.performClick();
        searchView.requestFocus();
        searchView.onActionViewExpanded();
        searchView.setIconified(true);
    }

    private void openSettings(){
//        startActivity(new Intent(Settings.ACTION_SETTINGS));
        //startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
    }

    private void sendMessage(){

    }
}
