package de.codekenner.roadtrip.Activities;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import de.codekenner.roadtrip.R;

public class TripsActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, TripsFragment.OnFragmentInteractionListener, NotesFragment.OnFragmentInteractionListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getString(R.string.title_my_trips);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments

        Fragment fragment = null;
        switch (position) {
            // My Trips
            case 0:
                fragment = new TripsFragment();
                break;

            // Connect
            case 1:
                // fragment = new ConnectFragment();
                break;

            // Browse
            case 2:
                // fragment = new BrowseFragment();
                break;

            default:
                break;
        }

        FragmentManager fragmentManager = getFragmentManager();

        if (fragmentManager.findFragmentById(R.id.container) != fragment) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_my_trips);
                break;
            case 2:
                mTitle = getString(R.string.title_connect);
                break;
            case 3:
                mTitle = getString(R.string.title_browse);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.trips, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void showNotesForTrip(Long id) {

        // Create Bundle to send data
        Bundle bundle = new Bundle();
        bundle.putLong("id", id);

        // Create fragment and attach data
        Fragment fragment = new NotesFragment();
        fragment.setArguments(bundle);

        // Switch to new Fragment
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack("base")
                .commit();
    }

    public void addNoteForTrip(Long id) {
        // Launch edit notes activity
        Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra("ID_TRIP", id);
        this.startActivity(intent);
    }

    public void setActionBarTitle(String title) {
        getActionBar().setTitle(title);
    }

}
