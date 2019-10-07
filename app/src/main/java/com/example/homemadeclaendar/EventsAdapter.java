package com.example.homemadeclaendar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventListHolder> {
    private Context context;
    private List<Event> eventList;

    public EventsAdapter(Context context,List<Event> eventList){
        this.context = context;
        this.eventList = eventList;
    }
    public class EventListHolder extends RecyclerView.ViewHolder {

        public TextView eventName,eventTime,eventLocation;
        public  EventListHolder(View v)
        {
            super(v);
            eventName = (TextView)v.findViewById(R.id.cv_tv_name);
            eventTime = (TextView)v.findViewById(R.id.cv_tv_time);
            eventLocation = (TextView)v.findViewById(R.id.cv_tv_lication);
        }

    }
    @Override
    public EventListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_card, parent, false);

        return new EventListHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final EventListHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventName.setText(event.getEventName());
        holder.eventTime.setText(event.getStartTime() + "-" + event.getEndTime());
        holder.eventLocation.setText(event.getEventLocation());

//        holder.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showPopupMenu(holder.overflow);
//            }
//        });
    }
    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
