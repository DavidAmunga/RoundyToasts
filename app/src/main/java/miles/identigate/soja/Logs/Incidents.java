package miles.identigate.soja.Logs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import miles.identigate.soja.Adapters.IncidentsAdapter;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.SojaActivity;
import miles.identigate.soja.R;
public class Incidents extends SojaActivity {
    DatabaseHandler handler;
    ListView listView;
    IncidentsAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidents);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handler=new DatabaseHandler(this);
        listView=(ListView)findViewById(R.id.incidents);
        listView.setEmptyView(findViewById(R.id.empty));
        adapter=new IncidentsAdapter(this,handler.getIncidents());
        listView.setAdapter(adapter);
    }
}
