package com.github.zoommaxdecentralnetwork.decentralmessenger;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ContactsAdapter extends ArrayAdapter<Contacts> {
    public ContactsAdapter(Context context, int layout, List<Contacts> contacts) {
        super(context, layout, contacts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contacts contacts = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.contactlist, null);
        }
        ((TextView) convertView.findViewById(R.id.name))
                .setText(contacts.name);
        ((TextView) convertView.findViewById(R.id.key))
                .setText(contacts.key);
        if (contacts.newMsg == 1){
            convertView.setBackgroundColor(Color.GREEN);
        }else if(contacts.newMsg == 0){
            convertView.setBackgroundColor(Color.WHITE);
        }
        return convertView;
    }
}
