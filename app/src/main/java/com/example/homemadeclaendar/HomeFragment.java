package com.example.homemadeclaendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

public class HomeFragment extends Fragment {
    private EventDatabase eventDB = null;
    private View vHome;
    private TextView today;
    private Calendar cal = Calendar.getInstance();
    private TextInputLayout tiStartTime;
    private TextInputLayout tiEndTime;
    private TextInputLayout tiName;
    private TextInputLayout tiLocation;
    private TextInputLayout tiDescription;
    private TextInputLayout tiEventType;
    private Button saveEventDataButton;
    private Button backEventDataButton;
    private View eventInputDialogView;
    private Spinner spEventType;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        vHome = inflater.inflate(R.layout.fragment_home, container, false);
        //Connect to event database
        eventDB  = Room.databaseBuilder(vHome.getContext(), EventDatabase.class, "EventDatabase").fallbackToDestructiveMigration().build();
        today = (TextView)vHome.findViewById(R.id.tv_today);

        String dateToday = CalendarFragment.calFormat.format(cal.getTime());
        today.setText(dateToday);

        ImageButton addEvent = (ImageButton)vHome.findViewById(R.id.btn_add);
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(vHome.getContext());
                alertDialogBuilder.setCancelable(false);
                initEventViewControls();
                alertDialogBuilder.setView(eventInputDialogView);
                final AlertDialog eventViewDialog = alertDialogBuilder.create();
                eventViewDialog.setTitle("Add New Event");
                eventViewDialog.show();

                saveEventDataButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Get user input from event dialog
                        try{
                            String iName = tiName.getEditText().getText().toString().trim();
                            String iLocation = tiLocation.getEditText().getText().toString().trim();
                            String iStartTime = tiStartTime.getEditText().getText().toString();
                            String iEndTime = tiEndTime.getEditText().getText().toString();
                            String iDescription = tiDescription.getEditText().getText().toString();
                            String iType= tiEventType.getEditText().getText().toString();
                            //event validation check
                            if (eventTimeCheck() & eventNameCheck()){
                                //Insert event to event database and  create view based on the event time frames and duration
                               InsertEventToDatabase insertEvent = new InsertEventToDatabase();
                                insertEvent.execute(iStartTime,iEndTime,iName,iLocation,iDescription,iType);

                                //create event view
                                eventViewDialog.cancel();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                backEventDataButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        eventViewDialog.cancel();
                    }
                });
            }
        });
        return vHome;
    }
    public void initEventViewControls()
    {
        // Get layout inflater object.
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        // Inflate the event dialog from a layout xml file.
        eventInputDialogView = layoutInflater.inflate(R.layout.add_event_dialog, null);

        // Get user input, edittext and button ui controls in the event dialog.
        tiEventType = (TextInputLayout) eventInputDialogView.findViewById(R.id.til_eventType);
        spEventType = (Spinner) eventInputDialogView.findViewById(R.id.sp_eventType);
        String[] type = {"Other","Study", "Work", "Leisure", "Sport", "Go Out"};
        ArrayAdapter<String> spinnerAdp = new ArrayAdapter<String>(vHome.getContext(), android.R.layout.simple_spinner_item, type);
        spinnerAdp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEventType.setAdapter(spinnerAdp);
        spEventType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tiEventType.getEditText().setText(spEventType.getSelectedItem().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        tiEventType.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spEventType.performClick();
            }
        });
        tiEndTime = (TextInputLayout) eventInputDialogView.findViewById(R.id.til_endTime);
        tiStartTime = (TextInputLayout) eventInputDialogView.findViewById(R.id.til_startTime);
        tiName = (TextInputLayout) eventInputDialogView.findViewById(R.id.til_eventName);
        tiLocation = (TextInputLayout) eventInputDialogView.findViewById(R.id.til_eventLocation);
        tiDescription = (TextInputLayout) eventInputDialogView.findViewById(R.id.til_eventDescription);
        saveEventDataButton = eventInputDialogView.findViewById(R.id.btn_save_event);
        backEventDataButton = eventInputDialogView.findViewById(R.id.btn_back_event);
    }

    private  boolean eventNameCheck(){
        String eventName =  tiName.getEditText().getText().toString().trim();
        if (eventName.isEmpty()){
            tiName.setError("Event name cannot be empty");
            return false;
        }else {
            tiName.setError(null);
            tiName.setErrorEnabled(false);
            return true;
        }
    }
    private  boolean eventTimeCheck() {
        //Clear error message
        tiStartTime.setError(null);
        tiEndTime.setError(null);
        String eventStartTime = tiStartTime.getEditText().getText().toString();
        String eventEndTime = tiEndTime.getEditText().getText().toString();
        //Check if time input is empty
        if (eventStartTime.isEmpty() || eventEndTime.isEmpty()) {
            if (eventStartTime.isEmpty()) {
                tiStartTime.setError("Start Time cannot be empty");
            }
            if(eventEndTime.isEmpty()){
                tiEndTime.setError("End Time cannot be empty");
            }
            return false;
        }else{
            //Check if the time inout format is validate(HH:MM,except 24:00)
            if (!CalendarFragment.TIME_24HOURS_PATTERN.matcher(eventStartTime).matches()||!CalendarFragment.TIME_24HOURS_PATTERN.matcher(eventEndTime).matches()){
                if(!CalendarFragment.TIME_24HOURS_PATTERN.matcher(eventStartTime).matches()){
                    tiStartTime.setError("Please use correct time format: HH:MM");
                }
                if (!CalendarFragment.TIME_24HOURS_PATTERN.matcher(eventEndTime).matches()){
                    tiEndTime.setError("Please use correct time format: HH:MM");
                }
                return false;
            }else{
                //Get Hours and Minutes values separately
                String[] startHourMinutes = eventStartTime.split(":");
                int sHours = Integer.valueOf(startHourMinutes[0]);
                int sMinutes = Integer.valueOf(startHourMinutes[1]);
                String[] endHourMinutes = eventEndTime.split(":");
                int eHours = Integer.valueOf(endHourMinutes[0]);
                int eMinutes = Integer.valueOf(endHourMinutes[1]);

                //Check if end time is before the start time
                if(eHours < sHours || (eHours == sHours && eMinutes <= sMinutes)) {
                    tiEndTime.setError("End time must be after the start time");
                    return false;
                }
            }
            return true;
        }
    }
    //Insert Event record to the database
    private class InsertEventToDatabase extends AsyncTask<String,Void,String[]> {
        @Override
        protected String[] doInBackground(String...params){
            String[] eventInputs ={"","","","","",""};
            String thatDay = today.getText().toString();
            Date dateCreated = new Date();
            //Pares currentDate(String) to Date
            try {
                dateCreated = CalendarFragment.calFormat.parse(thatDay);
            }catch (Exception e){
                e.printStackTrace();
            }
            Event newEvent = new Event(dateCreated,params[0],params[1],params[2],params[3],params[4],params[5]);
            //get rowid from new inserted event
            String rowid = String.valueOf(eventDB.eventDao().insert(newEvent));
            eventInputs[0] = params[0];
            eventInputs[1] = params[1];
            eventInputs[2] = params[2];
            eventInputs[3] = params[3];
            eventInputs[4] = rowid;
            eventInputs[5] = params[5];
            return eventInputs;
        }
        @Override
        protected void onPostExecute( String[] result){
            createEventNotification(result[0],result[1],result[2],result[3],Integer.valueOf(result[4]));
//            createEventView(result[0],result[1],result[2],result[3],Integer.valueOf(result[4]),result[5]);
        }

        private void createEventNotification(String start, String end,String name,String location,int id){
            String[] nHourMinutes = start.split(":");
            int nHour = Integer.valueOf(nHourMinutes[0]);
            int nMinutes = Integer.valueOf(nHourMinutes[1]);
            Calendar calendar = (Calendar)cal.clone();
            calendar.set(Calendar.HOUR_OF_DAY, nHour);
            calendar.set(Calendar.MINUTE,nMinutes);
            calendar.set(Calendar.SECOND,0);
            AlarmManager alarmManager = (AlarmManager) vHome.getContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(vHome.getContext(),NotificationPublish.class);
            intent.putExtra("Start",start);
            intent.putExtra("End",end);
            intent.putExtra("Name",name);
            intent.putExtra("Location",location);
            intent.putExtra("Id",id);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(vHome.getContext(),id,intent,0);
            if(calendar.getTimeInMillis()>= System.currentTimeMillis()){
                alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
            }
        }

        //Initial event view




    }
    //
}
