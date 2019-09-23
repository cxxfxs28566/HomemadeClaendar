package com.example.homemadeclaendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;



public class MainActivity extends AppCompatActivity {
    public final Pattern TIME_24HOURS_PATTERN = Pattern.compile(
            "^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$"
    );
    private final SimpleDateFormat calFormat = new SimpleDateFormat(" EEEE, MMM d, yyyy", Locale.ENGLISH);
    private EventDatabase eventDB = null;
    private RelativeLayout mLayout;
    private TextInputLayout tiStartTime;
    private TextInputLayout tiEndTime;
    private TextInputLayout tiName;
    private TextInputLayout tiLocation;
    private TextInputLayout tiDescription;
    private TextInputLayout tiEventType;
    private Button saveEventDataButton;
    private Button backEventDataButton;
    private TextInputLayout tiStartTimeDetail;
    private TextInputLayout tiEndTimeDetail;
    private TextInputLayout tiNameDetail;
    private TextInputLayout tiLocationDetail;
    private TextInputLayout tiDescriptionDetail;
    private TextInputLayout tiEventTypeDetail;
    private Button deleteEventDetailButton;
    private Button updateEventDetailButton;
    private Button backEventDetailButton;
    private View eventInputDialogView;
    private View eventDetailDialogView;
    private Calendar cal = Calendar.getInstance();
    private TextView currentDate;
    private ImageButton previousDay;
    private ImageButton nextDay;
    private Spinner spEventType;
    private Spinner spEventTypeDetail;
    //
    private NotificationManagerCompat notificationManagerCompat;
    //



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
//        notificationManagerCompat = NotificationManagerCompat.from(this);
        //
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main); // get the reference of Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mLayout = (RelativeLayout)findViewById(R.id.layout_main);
        currentDate = (TextView)findViewById(R.id.tv_current_date);
        currentDate.setText(displayDateInString(cal.getTime()));
        //Connect to event database
        eventDB  = Room.databaseBuilder(this.getApplicationContext(), EventDatabase.class, "EventDatabase").fallbackToDestructiveMigration().build();

        displayDailyEvents();

        previousDay = (ImageButton)findViewById(R.id.btn_previous_day);
        nextDay = (ImageButton)findViewById(R.id.btn_next_day);
        previousDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousCalendarDate();
            }
        });
        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextCalendarDate();
            }
        });

        //read dailyevents


        //delete all events from database, just for test
//        final Button deleteAll = (Button)findViewById(R.id.btn_deleteAll);
//        deleteAll.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DeleteAllEventsFromDatabase deleteAllEvents = new DeleteAllEventsFromDatabase();
//                deleteAllEvents.execute();
//
//            }
//        });


        ImageButton addEvent = (ImageButton)findViewById(R.id.btn_add);

        //create event dialog by press "add" button
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
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
    }
    //
    public void sendOnChannel1(String start, String end,String name,String location,int id){
        String[] nHourMinutes = start.split(":");
        int nHour = Integer.valueOf(nHourMinutes[0]);
        int nMinutes = Integer.valueOf(nHourMinutes[1]);
        Calendar calendar = (Calendar)cal.clone();
        calendar.set(Calendar.HOUR_OF_DAY, nHour);
        calendar.set(Calendar.MINUTE,nMinutes);
        calendar.set(Calendar.SECOND,0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this,NotificationPublish.class);
        intent.putExtra("Start",start);
        intent.putExtra("End",end);
        intent.putExtra("Name",name);
        intent.putExtra("Location",location);
        intent.putExtra("Id",id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,id,intent,0);
        if(calendar.getTimeInMillis()>= System.currentTimeMillis()){
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
        }
    }
    //

    //create and display event view
    private void createEventView(String startTime, String endTime, String name, String location, final int rowId,String type ){
        //display event name ,location and time
        String info = name +"\n" + startTime + "-" + endTime +"\n"+ location;
        String color =SetEventViewColor(type);
        //get event view size and position factors based on event time
        float sTime = getUnifyEventTime(startTime);
        float eTime = getUnifyEventTime(endTime);
        float duration = eTime - sTime;
        int topMargin = Math.round(15+50*sTime);//convert float to int
        int height = Math.round(50*duration);//convert float to int

        final TextView mEventView = new TextView(MainActivity.this);
        RelativeLayout.LayoutParams lParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lParam.addRule(RelativeLayout.BELOW,R.id.toolbar_main);
        lParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lParam.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, getResources().getDisplayMetrics());//convert dp to px
        lParam.rightMargin = 0;
        lParam.leftMargin = 30;
        lParam.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());//convert dp to px
        mEventView.setLayoutParams(lParam);
        mEventView.setPadding(24, 0, 24, 0);
        //Get the width of View line
        View v = findViewById(R.id.v_0am);
        //set width of event view equal to the width of view line
        mEventView.setWidth(v.getWidth());
        mEventView.setGravity(0x11);
        mEventView.setTextColor(Color.parseColor("#000000"));
        mEventView.setBackgroundColor(Color.parseColor(color));
        mEventView.setText(info);
        mEventView.setId(rowId);
        mEventView.setTag("ViewOfDay");
        mEventView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int eventViewId = v.getId();
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setCancelable(false);
                initEventDetailControls();
                alertDialogBuilder.setView(eventDetailDialogView);
                final AlertDialog eventDetailDialog = alertDialogBuilder.create();
                eventDetailDialog.setTitle("Event Detail");
                eventDetailDialog.show();
                DisplayEventDetail displayEventDetail = new DisplayEventDetail();
                displayEventDetail.execute(eventViewId);

                deleteEventDetailButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeleteEventById deleteEventById = new DeleteEventById();
                        deleteEventById.execute(eventViewId);
                        eventDetailDialog.cancel();
                        Toast.makeText(MainActivity.this, "An event has been deleted", Toast.LENGTH_LONG).show();
                    }
                });
                backEventDetailButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        eventDetailDialog.cancel();
                    }
                });
                updateEventDetailButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String sTimeDetail = tiStartTimeDetail.getEditText().getText().toString();
                        String eTimeDetail = tiEndTimeDetail.getEditText().getText().toString();
                        String eNameDetail = tiNameDetail.getEditText().getText().toString();
                        String eLocationDetail = tiLocationDetail.getEditText().getText().toString();
                        String rowid = String.valueOf(eventViewId);
                        String eTypeDetail = tiEventTypeDetail.getEditText().getText().toString();
                        if(eventTimeDetailCheck() & eventNameDetailCheck()){
                        UpdateEventById updateEventById = new UpdateEventById();
                        updateEventById.execute(sTimeDetail,eTimeDetail,eNameDetail,eLocationDetail,rowid,eTypeDetail);
                        eventDetailDialog.cancel();
                        }
                        }
                });
            }
        });
        final int childCount = mLayout.getChildCount();
        mLayout.addView(mEventView,childCount-1);
        Log.i("ViewOfDay", "The index is " + (childCount-1)+" The rowId is " + rowId);
    }
    //Initial event view
    private void initEventViewControls()
    {
        // Get layout inflater object.
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);

        // Inflate the event dialog from a layout xml file.
        eventInputDialogView = layoutInflater.inflate(R.layout.add_event_dialog, null);

        // Get user input, edittext and button ui controls in the event dialog.
        tiEventType = (TextInputLayout) eventInputDialogView.findViewById(R.id.til_eventType);
        spEventType = (Spinner) eventInputDialogView.findViewById(R.id.sp_eventType);
        String[] type = {"Other","Study", "Work", "Leisure", "Sport", "Go Out"};
        ArrayAdapter<String> spinnerAdp = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, type);
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

    //Initial event detail
    private void initEventDetailControls()
    {
        // Get layout inflater object.
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        // Inflate the event dialog from a layout xml file.
        eventDetailDialogView = layoutInflater.inflate(R.layout.event_detail_dialog, null);
        // Get user input, edittext and button ui controls in the event dialog.
        tiEventTypeDetail  = (TextInputLayout) eventDetailDialogView.findViewById(R.id.til_eventTypeDetail);
        spEventTypeDetail = (Spinner) eventDetailDialogView.findViewById(R.id.sp_eventTypeDetail);
        String[] type = {"Other","Study", "Work", "Leisure", "Sport", "Go Out"};
        ArrayAdapter<String> spinnerAdp = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, type);
        spinnerAdp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEventTypeDetail.setAdapter(spinnerAdp);
        spEventTypeDetail.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tiEventTypeDetail.getEditText().setText(spEventTypeDetail.getSelectedItem().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        tiEventTypeDetail.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spEventTypeDetail.performClick();
            }
        });

        tiEndTimeDetail = (TextInputLayout) eventDetailDialogView.findViewById(R.id.til_endTimeDetail);
        tiStartTimeDetail = (TextInputLayout) eventDetailDialogView.findViewById(R.id.til_startTimeDetail);
        tiNameDetail = (TextInputLayout) eventDetailDialogView.findViewById(R.id.til_eventNameDetail);
        tiLocationDetail = (TextInputLayout) eventDetailDialogView.findViewById(R.id.til_eventLocationDetail);
        tiDescriptionDetail = (TextInputLayout) eventDetailDialogView.findViewById(R.id.til_eventDescriptionDetail);
        updateEventDetailButton = eventDetailDialogView.findViewById(R.id.btn_update_eventDetail);
        backEventDetailButton = eventDetailDialogView.findViewById(R.id.btn_back_eventDetail);
        deleteEventDetailButton = eventDetailDialogView.findViewById(R.id.btn_delete_eventDetail);
    }

    //Express time in decimals
    private  float getUnifyEventTime(String time){
        String[]sHourMinutes = time.split(":");
        float hours = Float.valueOf(sHourMinutes[0]);
        float minutes = Float.valueOf(sHourMinutes[1]);
        float unifyTime = hours + minutes/60;
        return unifyTime;
    }
    //Display event detail by id
    private class DisplayEventDetail extends AsyncTask<Integer,Void,Event> {
        @Override
        protected Event doInBackground(Integer... params) {
            Event event = null;
            event = eventDB.eventDao().findById(params[0]);
            return event;
        }@Override
        protected void onPostExecute(Event event){
            if(event!=null){
            try{
                tiStartTimeDetail.getEditText().setText(event.getStartTime());
                tiEndTimeDetail.getEditText().setText(event.getEndTime());
                tiNameDetail.getEditText().setText(event.getEventName());
                tiLocationDetail.getEditText().setText(event.getEventLocation());
                tiDescriptionDetail.getEditText().setText(event.getEventDescription());
                tiEventTypeDetail.getEditText().setText(event.getEventType());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        }
    }
    //Insert Event record to the database
    private class InsertEventToDatabase extends AsyncTask<String,Void,String[]> {
        @Override
        protected String[] doInBackground(String...params){
            String[] eventInputs ={"","","","","",""};
            String thatDay = currentDate.getText().toString();
            Date dateCreated = new Date();
            //Pares currentDate(String) to Date
            try {
                dateCreated = calFormat.parse(thatDay);
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
            sendOnChannel1(result[0],result[1],result[2],result[3],Integer.valueOf(result[4]));
            createEventView(result[0],result[1],result[2],result[3],Integer.valueOf(result[4]),result[5]);
        }
    }
    //Delete event view by id
    private class DeleteEventById extends AsyncTask<Integer,Void,Integer> {
        @Override
        protected Integer doInBackground(Integer... params) {
            Event event = null;
            int id = params[0];
            event = eventDB.eventDao().findById(id);
            if(event!=null){
                eventDB.eventDao().delete(event);
            }
            return id;
        }
        @Override
        protected void onPostExecute(Integer id){
           try{
           View v = findViewById(id);
           mLayout.removeView(v);
            }catch (Exception e){
                e.printStackTrace();
            }
            AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
           Intent intent = new Intent(getApplicationContext(),NotificationPublish.class);
           PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),id,intent,0);
           alarmManager.cancel(pendingIntent);
        }
    }
    //Delete all event records
    private class DeleteAllEventsFromDatabase extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void...params){
            eventDB.eventDao().deleteAll();
            return "";
        }
        @Override
        protected void onPostExecute(String result){

        }
    }
    //Update
    private class UpdateEventById extends AsyncTask<String, Void, String[]> {
        @Override protected String[] doInBackground(String... params) {
            Event event = null;
            int id = Integer.valueOf(params[4]);
            String[] eventDetailInputs ={"","","","","",""};
            try {
                event = eventDB.eventDao().findById(id);
                event.setStartTime(params[0]);
                event.setEndTime(params[1]);
                event.setEventName(params[2]);
                event.setEventLocation(params[3]);
                event.setEventType(params[5]);
                Date eventDay = calFormat.parse(currentDate.getText().toString());
                event.setEventDate(eventDay);
                event.setEventDescription(tiDescriptionDetail.getEditText().getText().toString());
            }catch (Exception e){
                e.printStackTrace();
            }
            eventDB.eventDao().updateEvents(event);
            eventDetailInputs[0] = params[0];
            eventDetailInputs[1] = params[1];
            eventDetailInputs[2] = params[2];
            eventDetailInputs[3] = params[3];
            eventDetailInputs[4] = params[4];
            eventDetailInputs[5] = params[5];
            return eventDetailInputs;
        }
        @Override
        protected void onPostExecute(String[] detail) {
            if(!detail[4].equals("")){
            View v = findViewById(Integer.valueOf(detail[4]));
            mLayout.removeView(v);
            createEventView(detail[0],detail[1],detail[2],detail[3],Integer.valueOf(detail[4]),detail[5]);
            sendOnChannel1(detail[0],detail[1],detail[2],detail[3],Integer.valueOf(detail[4]));
            Toast.makeText(MainActivity.this, "The event has benn updated", Toast.LENGTH_LONG).show();
            }
        }
    }


    //Read events from database and create event view for a certain day
    private class ReadDailyEvents extends AsyncTask<String, Void, List<Event>> {
        @Override
        protected List<Event> doInBackground(String... params) {
                String thatDay = currentDate.getText().toString();
                Date selectedDay = new Date();
                //Pares currentDate(String) to Date
                try {
                    selectedDay = calFormat.parse(thatDay);
                }catch (Exception e){
                 e.printStackTrace();
                }
                //find event list by day
                List<Event> events = eventDB.eventDao().findByDate(selectedDay);
            return events;
        }
         @Override
        protected void onPostExecute(List<Event> events) {
            //create event view from event list
             for(Event e:events){
                 String start = e.getStartTime();
                 String end = e.getEndTime();
                 String name = e.getEventName();
                 String location = e.getEventLocation();
                 String description = e.getEventDescription();
                 String type = e.getEventType();
                 int eid = e.getEid();
                 createEventView(start,end,name,location,eid,type);
             }
        }
    }
    //Set event view color
    private String SetEventViewColor(String eventType){
        String color = "#D9ff0000";
        switch(eventType){
            case "Other":
                color = "#D9b5c8f0";
                break;
            case "Study":
                color = "#D988c7d1";
                break;
            case "Work":
                color = "#D9cae7e5";
                break;
            case "Leisure":
                color = "#D9ffdfe5";
                break;
            case "Sport":
                color = "#D9ccff99";
                break;
            case "Go Out":
                color = "#D9ffff99";
                break;
        }
        return color;
    }

    //check validation for event name creation view
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
    //check validation for event name detail view
    private  boolean eventNameDetailCheck(){
        try {
        String eventName =  tiNameDetail.getEditText().getText().toString().trim();
        if (eventName.isEmpty()){
            tiNameDetail.setError("Event name cannot be empty");
            return false;
        }else {
            tiNameDetail.setError(null);
            tiNameDetail.setErrorEnabled(false);
            return true;
        }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    //check validation for event time creation view
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
                if (!TIME_24HOURS_PATTERN.matcher(eventStartTime).matches()||!TIME_24HOURS_PATTERN.matcher(eventEndTime).matches()){
                    if(!TIME_24HOURS_PATTERN.matcher(eventStartTime).matches()){
                        tiStartTime.setError("Please use correct time format: HH:MM");
                    }
                    if (!TIME_24HOURS_PATTERN.matcher(eventEndTime).matches()){
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

    //check validation for event time detail view
    private  boolean eventTimeDetailCheck() {
        //Clear error message
        tiStartTimeDetail.setError(null);
        tiEndTimeDetail.setError(null);
        String eventStartTime = tiStartTimeDetail.getEditText().getText().toString();
        String eventEndTime = tiEndTimeDetail.getEditText().getText().toString();
        //Check if time input is empty
        if (eventStartTime.isEmpty() || eventEndTime.isEmpty()) {
            if (eventStartTime.isEmpty()) {
                tiStartTimeDetail.setError("Start Time cannot be empty");
            }
            if(eventEndTime.isEmpty()){
                tiEndTimeDetail.setError("End Time cannot be empty");
            }
            return false;
        }else{
            //Check if the time inout format is validate(HH:MM,except 24:00)
            if (!TIME_24HOURS_PATTERN.matcher(eventStartTime).matches()||!TIME_24HOURS_PATTERN.matcher(eventEndTime).matches()){
                if(!TIME_24HOURS_PATTERN.matcher(eventStartTime).matches()){
                    tiStartTimeDetail.setError("Please use correct time format: HH:MM");
                }
                if (!TIME_24HOURS_PATTERN.matcher(eventEndTime).matches()){
                    tiEndTimeDetail.setError("Please use correct time format: HH:MM");
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
                    tiEndTimeDetail.setError("End time must be after the start time");
                    return false;
                }
            }
            return true;
        }
    }

    //Change current date when click "<",">" arrow
    private void previousCalendarDate(){
        //Find all existed daily event view and remove
        int childCount = mLayout.getChildCount();
        for(int i = childCount -1;i>=0;i--){
             View child = mLayout.getChildAt(i);
             Object tagObj = child.getTag();
            if(tagObj != null && tagObj.equals("ViewOfDay")){
                mLayout.removeView(child);
            }
        }
        //set current date
        cal.add(Calendar.DAY_OF_MONTH, -1);
        currentDate.setText(displayDateInString(cal.getTime()));
        //Load all current daily event view
        displayDailyEvents();
    }
    private void nextCalendarDate(){
        //Find all existed daily event view and remove
        int childCount = mLayout.getChildCount();
        for(int i = childCount -1;i>=0;i--){
             View child = mLayout.getChildAt(i);
             Object tagObj = child.getTag();
            if(tagObj != null && tagObj.equals("ViewOfDay")){
                mLayout.removeView(child);
            }
        }
        //set current date
        cal.add(Calendar.DAY_OF_MONTH, 1);
        currentDate.setText(displayDateInString(cal.getTime()));
        //Load all current daily event view
        displayDailyEvents();
    }
    //display current date in certain format
    private String displayDateInString(Date mDate){
        SimpleDateFormat calFormat = new SimpleDateFormat(" EEEE, MMM d, yyyy", Locale.ENGLISH);
        return calFormat.format(mDate);
        }
    //display all-days events
    private void displayDailyEvents(){
        ReadDailyEvents readDailyEvents = new ReadDailyEvents();
        readDailyEvents.execute();
    }
}

