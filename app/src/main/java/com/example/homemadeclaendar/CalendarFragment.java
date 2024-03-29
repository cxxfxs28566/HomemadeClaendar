package com.example.homemadeclaendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

public class CalendarFragment extends Fragment {
    private View vCalendar;
    public static final Pattern TIME_24HOURS_PATTERN = Pattern.compile(
            "^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$"
    );
    public static final SimpleDateFormat calFormat = new SimpleDateFormat(" EEEE, MMM d, yyyy", Locale.ENGLISH);
    private EventDatabase eventDB = null;
    private RelativeLayout mLayout;
    private Spinner spEventType;
    private TextInputLayout tiStartTime;
    private TextInputLayout tiEndTime;
    private TextInputLayout tiName;
    private TextInputLayout tiLocation;
    private TextInputLayout tiDescription;
    private TextInputLayout tiEventType;
    private Button saveEventDataButton;
    private Button backEventDataButton;
    private Spinner spEventTypeDetail;
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
    private CardView cardView;
    private Bundle bundle = new Bundle();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        vCalendar = inflater.inflate(R.layout.fragment_calendar, container, false);
        Toolbar toolbar = (Toolbar) vCalendar.findViewById(R.id.toolbar_main); // get the reference of Toolbar
        mLayout = (RelativeLayout)vCalendar.findViewById(R.id.layout_main);
        currentDate = (TextView)vCalendar.findViewById(R.id.tv_current_date);
        currentDate.setText(displayDateInString(cal.getTime()));
        //Connect to event database
        eventDB  = Room.databaseBuilder(vCalendar.getContext(), EventDatabase.class, "EventDatabase").fallbackToDestructiveMigration().build();

        displayDailyEvents();

        previousDay = (ImageButton)vCalendar.findViewById(R.id.btn_previous_day);
        nextDay = (ImageButton)vCalendar.findViewById(R.id.btn_next_day);
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

        ImageButton addEvent = (ImageButton)vCalendar.findViewById(R.id.btn_add);

        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(vCalendar.getContext());
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
                                CalendarFragment.InsertEventToDatabase insertEvent = new CalendarFragment.InsertEventToDatabase();
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
        return vCalendar;
    }
    //Initial event view
    private void initEventViewControls()
    {
        // Get layout inflater object.
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        // Inflate the event dialog from a layout xml file.
        eventInputDialogView = layoutInflater.inflate(R.layout.add_event_dialog, null);

        // Get user input, edittext and button ui controls in the event dialog.
        tiEventType = (TextInputLayout) eventInputDialogView.findViewById(R.id.til_eventType);
        spEventType = (Spinner) eventInputDialogView.findViewById(R.id.sp_eventType);
        String[] type = {"Other","Study", "Work", "Leisure", "Sport", "Go Out"};
        ArrayAdapter<String> spinnerAdp = new ArrayAdapter<String>(vCalendar.getContext(), android.R.layout.simple_spinner_item, type);
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
    //Set notification on certain channel
    public void createEventNotification(String start, String end,String name,String location,int id){
        String[] nHourMinutes = start.split(":");
        int nHour = Integer.valueOf(nHourMinutes[0]);
        int nMinutes = Integer.valueOf(nHourMinutes[1]);
        Calendar calendar = (Calendar)cal.clone();
        calendar.set(Calendar.HOUR_OF_DAY, nHour);
        calendar.set(Calendar.MINUTE,nMinutes);
        calendar.set(Calendar.SECOND,0);
        AlarmManager alarmManager = (AlarmManager) vCalendar.getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(vCalendar.getContext(),NotificationPublish.class);
        intent.putExtra("Start",start);
        intent.putExtra("End",end);
        intent.putExtra("Name",name);
        intent.putExtra("Location",location);
        intent.putExtra("Id",id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(vCalendar.getContext(),id,intent,0);
        if(calendar.getTimeInMillis()>= System.currentTimeMillis()){
            alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
        }
    }
    //Express time in decimals
    private  float getUnifyEventTime(String time){
        String[]sHourMinutes = time.split(":");
        float hours = Float.valueOf(sHourMinutes[0]);
        float minutes = Float.valueOf(sHourMinutes[1]);
        float unifyTime = hours + minutes/60;
        return unifyTime;
    }
    //create and display event view
    private void createEventView(String startTime, String endTime, String name, String location, final int rowId,String type ){
        //display event name ,location and time
        String eDuration = startTime + "-" + endTime;
        String color =SetEventViewColor(type);
        //get event view size and position factors based on event time
        float sTime = getUnifyEventTime(startTime);
        float eTime = getUnifyEventTime(endTime);
        float duration = eTime - sTime;
        int topMargin = Math.round(15+50*sTime);//convert float to int
        int height = Math.round(50*duration);//convert float to int

        cardView = new CardView(vCalendar.getContext());
        RelativeLayout.LayoutParams MainParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        lParam.addRule(RelativeLayout.BELOW,R.id.toolbar_main);
        MainParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        MainParam.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, getResources().getDisplayMetrics());//convert dp to px
        MainParam.rightMargin =(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
//        lParam.leftMargin = 30;
        MainParam.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());//convert dp to px
        //Get the width of View line
        View v = vCalendar.findViewById(R.id.v_0am);
        //set width of event view equal to the width of view line
        MainParam.width = v.getWidth();
        cardView.setLayoutParams(MainParam);
        cardView.setPadding(20, 10, 0, 0);
        cardView.setRadius(12);
        cardView.setCardBackgroundColor(Color.parseColor(color));
        cardView.setCardElevation(15);
        cardView.setMaxCardElevation(6);
        //
        LinearLayout TextLayout = new LinearLayout(vCalendar.getContext());
        TextLayout.setOrientation(LinearLayout.VERTICAL);

        TextView nameView = new TextView(vCalendar.getContext());
        nameView.setTextColor(Color.parseColor("#ffffff"));
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
        nameView.setPadding(15,0,0,5);
        nameView.setText(name);
        TextLayout.addView(nameView);

        TextView durationView = new TextView(vCalendar.getContext());
        durationView.setTextColor(Color.parseColor("#BFffffff"));
        durationView.setPadding(20,0,0,5);
        durationView.setText(eDuration);
        TextLayout.addView(durationView);

        TextView locationView = new TextView(vCalendar.getContext());
        locationView.setTextColor(Color.parseColor("#BFffffff"));
        locationView.setPadding(20,0,0,0);
        locationView.setText(location);
        TextLayout.addView(locationView);

        cardView.addView(TextLayout);
        cardView.setId(rowId);
        cardView.setTag("ViewOfDay");

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                int eventViewId = Integer.valueOf(rowId);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(vCalendar.getContext());
                alertDialogBuilder.setCancelable(false);
                initEventDetailControls();
                alertDialogBuilder.setView(eventDetailDialogView);
                final AlertDialog eventDetailDialog = alertDialogBuilder.create();
                eventDetailDialog.setTitle("Event Detail");
                eventDetailDialog.show();
                CalendarFragment.DisplayEventDetail displayEventDetail = new CalendarFragment.DisplayEventDetail();
                displayEventDetail.execute(rowId);

                deleteEventDetailButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CalendarFragment.DeleteEventById deleteEventById = new CalendarFragment.DeleteEventById();
                        deleteEventById.execute(rowId);
                        eventDetailDialog.cancel();
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
                        String eRowid = String.valueOf(rowId);
                        String eTypeDetail = tiEventTypeDetail.getEditText().getText().toString();
                        if(eventTimeDetailCheck() & eventNameDetailCheck()){
                            CalendarFragment.UpdateEventById updateEventById = new CalendarFragment.UpdateEventById();
                            updateEventById.execute(sTimeDetail,eTimeDetail,eNameDetail,eLocationDetail,eRowid,eTypeDetail);//
                            eventDetailDialog.cancel();
                        }
                    }
                });
            }
        });
        cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MoveToEventDetail moveToEventDetail = new MoveToEventDetail();
                moveToEventDetail.execute(rowId);
                return false;
            }
        });
        final int childCount = mLayout.getChildCount();
        mLayout.addView(cardView,childCount-1);
        Log.i("ViewOfDay", "The index is " + (childCount-1)+" The rowId is " + rowId);
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
    //
    //Display event detail by id
    private class MoveToEventDetail extends AsyncTask<Integer,Void,Event> {
        @Override
        protected Event doInBackground(Integer... params) {
            Event event = null;
            event = eventDB.eventDao().findById(params[0]);
            return event;
        }@Override
        protected void onPostExecute(Event event){
            if(event!=null){
                try{
                    String duration = event.getStartTime() + " - "+ event.getEndTime();
                    bundle.putString("Event Duration",duration);
                    bundle.putString("Event Name",event.getEventName());
                    bundle.putString("Event Location",event.getEventLocation());
                    bundle.putString("Event Description",event.getEventDescription());
                    bundle.putString("Event Type",event.getEventType());
                    bundle.putString("Event Date",calFormat.format(event.getEventDate()));
                    bundle.putInt("Event Id",event.getEid());

                    DetailFragment detailFragment= new DetailFragment();
                    detailFragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(((ViewGroup)getView().getParent()).getId(), detailFragment, "findThisFragment")
                            .addToBackStack(null)
                            .commit();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    //
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
            createEventNotification(result[0],result[1],result[2],result[3],Integer.valueOf(result[4]));
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
                View v = vCalendar.findViewById(id);
                mLayout.removeView(v);
                Toast.makeText(getContext(), "An event has been deleted", Toast.LENGTH_LONG).show();
            }catch (Exception e){
                e.printStackTrace();
            }
            AlarmManager alarmManager = (AlarmManager)vCalendar.getContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(vCalendar.getContext().getApplicationContext(),NotificationPublish.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(vCalendar.getContext().getApplicationContext(),id,intent,0);
            alarmManager.cancel(pendingIntent);
        }
    }
    //Update
    public class UpdateEventById extends AsyncTask<String, Void, String[]> {
        @Override protected String[] doInBackground(String... params) {
            Event event = null;
            String[] eventDetailInputs ={"","","","","",""};
            int id = Integer.valueOf(params[4]);

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
                View v = vCalendar.findViewById(Integer.valueOf(detail[4]));
                mLayout.removeView(v);
                createEventView(detail[0],detail[1],detail[2],detail[3],Integer.valueOf(detail[4]),detail[5]);
                createEventNotification(detail[0],detail[1],detail[2],detail[3],Integer.valueOf(detail[4]));
                Toast.makeText(getContext(), "The event has benn updated", Toast.LENGTH_LONG).show();
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
        String color = "#ff0000";
        switch(eventType){
            case "Other":
                color = "#5050aa";
                break;
            case "Study":
                color = "#005000";
                break;
            case "Work":
                color = "#ff5050";
                break;
            case "Leisure":
                color = "#ce94d7";
                break;
            case "Sport":
                color = "#b0840a";
                break;
            case "Go Out":
                color = "#eeaa99";
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
    private void initEventDetailControls()
    {
        // Get layout inflater object.
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        // Inflate the event dialog from a layout xml file.
        eventDetailDialogView = layoutInflater.inflate(R.layout.event_detail_dialog, null);
        // Get user input, edittext and button ui controls in the event dialog.
        tiEventTypeDetail  = (TextInputLayout) eventDetailDialogView.findViewById(R.id.til_eventTypeDetail);
        spEventTypeDetail = (Spinner) eventDetailDialogView.findViewById(R.id.sp_eventTypeDetail);
        String[] type = {"Other","Study", "Work", "Leisure", "Sport", "Go Out"};
        ArrayAdapter<String> spinnerAdp = new ArrayAdapter<String>(vCalendar.getContext(), android.R.layout.simple_spinner_item, type);
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
//        SimpleDateFormat calFormat = new SimpleDateFormat(" EEEE, MMM d, yyyy", Locale.ENGLISH);
        return calFormat.format(mDate);
    }
    //display all-days events
    private void displayDailyEvents(){
        CalendarFragment.ReadDailyEvents readDailyEvents = new CalendarFragment.ReadDailyEvents();
        readDailyEvents.execute();
    }
}
