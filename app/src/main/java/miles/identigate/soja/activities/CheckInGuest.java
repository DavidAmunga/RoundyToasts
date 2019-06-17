package miles.identigate.soja.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.client.android.BeepManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import miles.identigate.soja.R;
import miles.identigate.soja.adapters.ServiceAdapter;
import miles.identigate.soja.fragments.ScanEventTicket;
import miles.identigate.soja.fragments.SelectEvent;
import miles.identigate.soja.fragments.SelectServiceOption;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.services.DataService;

public class CheckInGuest extends AppCompatActivity {

    String qr_token;
    MaterialDialog dialog;
    Preferences preferences;

    ProgressDialog progressDialog;


    private static final String TAG = "CheckInGuest";
    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    private BeepManager beepManager;

    DataService mService;


    String lastText;
    Animation scale_up;

    ServiceAdapter serviceAdapter;

    Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = new Preferences(this);

        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_guest);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);


        displaySelectedScreen(R.id.nav_select_event, bundle);


    }

    public void displaySelectedScreen(int itemId, Bundle bundle) {
        // Handle navigation view item clicks here.

        // Navigation drawer item selection logic goes here
        Fragment fragment = null;
        Class fragmentClass = null;
        String title = null;


        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_select_event:
                fragmentClass = SelectEvent.class;
                title = "Select Event";
                break;
            case R.id.nav_scan_event_ticket:
                fragmentClass = ScanEventTicket.class;
                title = "Scan Event Ticket";
                break;
            case R.id.nav_select_service:
                fragmentClass = SelectServiceOption.class;
                title = "Select Service";
                break;

        }

        try {
            if (fragmentClass != null) {
                fragment = (Fragment) fragmentClass.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Insert the fragment by replacing any existing fragment
        if (fragment != null) {
            replaceFragment(fragment, title, bundle);
        }
    }


    public void replaceFragment(Fragment fragment, String title, Bundle bundle) {
        FragmentManager ft = getSupportFragmentManager();

        fragment.setArguments(bundle);

        ft.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_out_right, R.anim.slide_in_right)
                .replace(R.id.container, fragment, title)
                // Add this transaction to the back stack
                .addToBackStack(title)
                .commit();

        // Set the title in the action bar.
        getSupportActionBar().setTitle(fragment.getTag());
        resolveBackStack();

        showUpButton(true);


    }

    private void resolveBackStack() {
        // Update your UI here
        getSupportFragmentManager().addOnBackStackChangedListener(this::updateUI);
    }

    private void updateUI() {
        int lastBackStackEntryCount = getSupportFragmentManager().getBackStackEntryCount() - 1;
        if (lastBackStackEntryCount >= 0) {
            FragmentManager.BackStackEntry lastBackStackEntry = getSupportFragmentManager().getBackStackEntryAt(lastBackStackEntryCount);
            // Check if we are the Home fragment if so, show hamburger icon.
            if (lastBackStackEntry.getName().equalsIgnoreCase("Select Event")) {
                showUpButton(false);
            } else {
                // Show the <- (up arrow)
                showUpButton(true);
            }
            getSupportActionBar().setTitle(lastBackStackEntry.getName());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
            if (backStackCount >= 1) {
                Log.d(TAG, "onBackPressed: " + backStackCount);
                getSupportFragmentManager().popBackStack();
                // Check if backStackCount is 1, means we are at HOME page.
                // Therefore when user press back at this point, close the app.
                if (backStackCount == 1) {
                    super.onBackPressed();
                }
            } else {
                super.onBackPressed();

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void resolveUpButtonWithFragmentStack() {
        showUpButton(getSupportFragmentManager().getBackStackEntryCount() > 0);
        resolveBackStack();
    }

    private void showUpButton(boolean show) {
        // To keep states of ActionBar and ActionBarDrawerToggle synchronized,
        // when you enable on one, you disable on the other.
        // And as you may notice, the order for this operation is disable first, then enable - VERY VERY IMPORTANT.
        if (show) {
            // Remove hamburger

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // when DrawerToggle is disabled i.e. setDrawerIndicatorEnabled(false), navigation icon
            // clicks are disabled i.e. the UP button will not work.
            // We need to add a listener, as in below, so DrawerToggle will forward
            // click events to this listener.

        } else {
            // Remove back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Get the current title before change in device configuration from the action bar.
        CharSequence titleBeforeOrientation = getSupportActionBar().getTitle();
        outState.putString("actionBarTitle", String.valueOf(titleBeforeOrientation));

        //Save the fragment's state here
        Log.e("Main: SaveInstanceState", "Triggered onSaveInstanceState!");
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.e("Restore Instance State", "I am back " +
                savedInstanceState.getString("actionBarTitle"));
        getSupportActionBar().setTitle(savedInstanceState.getString("actionBarTitle"));
        resolveUpButtonWithFragmentStack();
    }


    @Override
    public void onBackPressed() {

        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackCount >= 1) {
            Log.d(TAG, "onBackPressed: " + backStackCount);
            getSupportFragmentManager().popBackStack();
            // Check if backStackCount is 1, means we are at HOME page.
            // Therefore when user press back at this point, close the app.
            if (backStackCount == 1) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();

        }
    }
}
