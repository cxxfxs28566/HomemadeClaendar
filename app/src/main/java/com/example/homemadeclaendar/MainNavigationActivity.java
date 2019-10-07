package com.example.homemadeclaendar;

import android.arch.persistence.room.Room;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MainNavigationActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    private EventDatabase eventDB = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);
        eventDB  = Room.databaseBuilder(this, EventDatabase.class, "EventDatabase").fallbackToDestructiveMigration().build();
        loadFragment(new HomeFragment());

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new HomeFragment();
                break;

            case R.id.navigation_calendar:
                fragment = new CalendarFragment();
                break;

            case R.id.navigation_agenda:
                //fragment = new AgendaFragment();
                break;
            case R.id.navigation_setting:
                DeleteAllEventsFromDatabase deleteAllEventsFromDatabase = new DeleteAllEventsFromDatabase();
                deleteAllEventsFromDatabase.execute();
                break;
        }

        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private class DeleteAllEventsFromDatabase extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void...params){
            eventDB.eventDao().deleteAll();
            return "";
        }
        @Override
        protected void onPostExecute(String result){

        }
    }
}
