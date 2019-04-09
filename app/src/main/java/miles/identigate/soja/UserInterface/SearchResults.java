package miles.identigate.soja.UserInterface;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.R;

public class SearchResults extends AppCompatActivity {
    DatabaseHandler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        handler=new DatabaseHandler(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handleIntent(getIntent());
    }
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }
    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            //ArrayList<Visitor> visitors=handler.search(query,handler.name);
            /*for(int i=0;i<visitors.size();i++){
                Log.v("NAME",visitors.get(i).getName());
            }*/
        }
    }
}
