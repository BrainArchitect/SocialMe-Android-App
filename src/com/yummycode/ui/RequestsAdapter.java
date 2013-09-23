package com.yummycode.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.yummycode.util.Request;

public class RequestsAdapter extends ArrayAdapter<Request>
{
    private Context currentContext;
    private List<Request> requests;
    private int resource;
        
    //Initialize adapter
    public RequestsAdapter(Context context, int resource, List<Request> requests) {
        super(context, resource, requests);
        this.currentContext = context;
        this.requests = requests;
        this.resource = resource;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) currentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View requestItem = inflater.inflate(resource, parent, false);
        
        TextView requestItemTitle = (TextView) requestItem.findViewById(R.id.reqTitle);
        TextView requestItemDescr = (TextView) requestItem.findViewById(R.id.reqDescription);
        
        requestItemTitle.setText(requests.get(position).getType());
        requestItemDescr.setText(requests.get(position).getDescription());
        
        return requestItem;
    }
}