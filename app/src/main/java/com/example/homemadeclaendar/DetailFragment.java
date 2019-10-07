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
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class DetailFragment extends Fragment {
    private View vDetail;
    private TextView detailName;
    private TextView detailType;
    private TextView detailDuration;
    private TextView detailDate;
    private TextView detailLocation;
    private TextView detailDescription;
    private Button detailBack;
//    private Button detailDelete;
//    private Button detailUpdate;
    private EventDatabase eventDB = null;
    private int eventId;
    private String eventDate;
    private View eventDetailDialogView;
    private TextInputLayout tiStartTimeDetail;
    private TextInputLayout tiEndTimeDetail;
    private TextInputLayout tiNameDetail;
    private TextInputLayout tiLocationDetail;
    private TextInputLayout tiDescriptionDetail;
    private TextInputLayout tiEventTypeDetail;
    private Spinner spEventTypeDetail;
    private Button updateEventDetailButton;
    private Button backEventDetailButton;
    public final Pattern TIME_24HOURS_PATTERN = Pattern.compile(
            "^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$"
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        vDetail = inflater.inflate(R.layout.fragment_detail, container, false);
        eventDB  = Room.databaseBuilder(vDetail.getContext(), EventDatabase.class, "EventDatabase").fallbackToDestructiveMigration().build();
        Bundle bundle =this.getArguments();
        detailDate = (TextView)vDetail.findViewById(R.id.tv_detail_date);
        detailName = (TextView)vDetail.findViewById(R.id.tv_detail_name);
        detailType = (TextView)vDetail.findViewById(R.id.tv_detail_type);
        detailDuration = (TextView)vDetail.findViewById(R.id.tv_detail_duration);
        detailLocation = (TextView)vDetail.findViewById(R.id.tv_detail_location_content);
        detailDescription = (TextView)vDetail.findViewById(R.id.tv_detail_description_content);
        if(bundle !=null){
            detailDuration.setText(bundle.getString("Event Duration",""));
            detailName.setText(bundle.getString("Event Name",""));
            detailLocation.setText(bundle.getString("Event Location",""));
            detailDescription.setText(bundle.getString("Event Description",""));
            detailType.setText(bundle.getString("Event Type",""));
            detailDate.setText(bundle.getString("Event Date",""));
            eventDate = bundle.getString("Event Date","");
            eventId = bundle.getInt("Event Id",0);
        }
        detailBack = (Button)vDetail.findViewById(R.id.btn_back1);
        detailBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Back to previous fragment
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStack();
            }
        });
//        detailDelete = (Button)vDetail.findViewById(R.id.btn_delete1);
//        detailDelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//               DeleteEventById deleteEventById = new DeleteEventById();
//               deleteEventById.execute(eventId);
//            }
//        });
//        detailUpdate = (Button)vDetail.findViewById(R.id.btn_update1);
//        detailUpdate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(vDetail.getContext());
//                alertDialogBuilder.setCancelable(false);
//                initEventDetailControls();
//                alertDialogBuilder.setView(eventDetailDialogView);
//                final AlertDialog eventDetailDialog = alertDialogBuilder.create();
//                eventDetailDialog.setTitle("Event Detail");
//                eventDetailDialog.show();
//                DisplayEventDetail displayEventDetail = new DisplayEventDetail();
//                displayEventDetail.execute(eventId);
//                backEventDetailButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        eventDetailDialog.cancel();
//                    }
//                });
//                updateEventDetailButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        String sTimeDetail = tiStartTimeDetail.getEditText().getText().toString();
//                        String eTimeDetail = tiEndTimeDetail.getEditText().getText().toString();
//                        String eNameDetail = tiNameDetail.getEditText().getText().toString();
//                        String eLocationDetail = tiLocationDetail.getEditText().getText().toString();
//                        String rowid = String.valueOf(eventId);
//                        String eTypeDetail = tiEventTypeDetail.getEditText().getText().toString();
//                        if(eventTimeDetailCheck() & eventNameDetailCheck()){
//                            UpdateEventById updateEventById = new UpdateEventById();
//                            updateEventById.execute(sTimeDetail,eTimeDetail,eNameDetail,eLocationDetail,rowid,eTypeDetail);
//                            eventDetailDialog.cancel();
//                        }
//                    }
//                });
//
//            }
//        });


    return vDetail;
    }
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

            AlarmManager alarmManager = (AlarmManager)vDetail.getContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(vDetail.getContext().getApplicationContext(),NotificationPublish.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(vDetail.getContext().getApplicationContext(),id,intent,0);
            alarmManager.cancel(pendingIntent);
            //Back to previous fragment
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.popBackStack();
            //Show result
            Toast.makeText(getContext(), "An event has been deleted", Toast.LENGTH_LONG).show();
        }
    }



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
    public class UpdateEventById extends AsyncTask<String, Void, String[]> {
        @Override protected String[] doInBackground(String... params) {
            Event event = null;
            int id = Integer.valueOf(params[4]);
            String[] eventDetailInputs ={"","","","","",""};
            event = eventDB.eventDao().findById(id);
            event.setStartTime(params[0]);
            event.setEndTime(params[1]);
            event.setEventName(params[2]);
            event.setEventLocation(params[3]);
            event.setEventType(params[5]);
//                Date eventDay = calFormat.parse(currentDate.getText().toString());
//                event.setEventDate(eventDay);
            event.setEventDescription(tiDescriptionDetail.getEditText().getText().toString());
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
                View v = vDetail.findViewById(Integer.valueOf(detail[4]));
                createEventNotification(detail[0],detail[1],detail[2],detail[3],Integer.valueOf(detail[4]));
                Toast.makeText(getContext(), "The event has benn updated", Toast.LENGTH_LONG).show();
            }
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
        ArrayAdapter<String> spinnerAdp = new ArrayAdapter<String>(vDetail.getContext(), android.R.layout.simple_spinner_item, type);
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
    public void createEventNotification(String start, String end,String name,String location,int id){
        String[] nHourMinutes = start.split(":");
        int nHour = Integer.valueOf(nHourMinutes[0]);
        int nMinutes = Integer.valueOf(nHourMinutes[1]);
        try {
            Date date = CalendarFragment.calFormat.parse(eventDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, nHour);
            calendar.set(Calendar.MINUTE,nMinutes);
            calendar.set(Calendar.SECOND,0);
            AlarmManager alarmManager = (AlarmManager) vDetail.getContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(vDetail.getContext(),NotificationPublish.class);
            intent.putExtra("Start",start);
            intent.putExtra("End",end);
            intent.putExtra("Name",name);
            intent.putExtra("Location",location);
            intent.putExtra("Id",id);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(vDetail.getContext(),id,intent,0);
            if(calendar.getTimeInMillis()>= System.currentTimeMillis()){
                alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
