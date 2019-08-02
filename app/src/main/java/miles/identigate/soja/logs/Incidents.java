package miles.identigate.soja.logs;

import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import miles.identigate.soja.adapters.IncidentsAdapter;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.SojaActivity;
import miles.identigate.soja.R;
public class Incidents extends SojaActivity {
    DatabaseHandler handler;
    ListView listView;
    IncidentsAdapter adapter;
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidents);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handler=new DatabaseHandler(this);
        listView = findViewById(R.id.incidents);
        listView.setEmptyView(findViewById(R.id.empty));
        adapter=new IncidentsAdapter(this,handler.getIncidents());
        listView.setAdapter(adapter);
    }
}