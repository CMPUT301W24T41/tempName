package com.example.eventsigninapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AttendeeListActivity extends AppCompatActivity implements DatabaseController.GetSignedUpUsersCallback,
        DatabaseController.GetCheckedInUsersCallback {
    TextView eventTitle;
    ListView checkedInListView;
    ListView signedUpListView;
    Button switchToMapButton;
    Button backButton;
    Event anEvent = new Event();
    List<String> signedUpUsers = (List<String>) anEvent.getSignedUpUsersUUIDs();
    List<String> checkedInUsers = (List<String>) anEvent.getCheckedInUsersUUIDs();
    ArrayAdapter<String> signedUpUserAdapter;
    ArrayAdapter<String> checkedInUserAdapter;
    AttendeeListController alController;
    DatabaseController dbController;
    String eventId = "131e7de5-38de-4cce-ab04-230a5f2ca76f"; // for testing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        anEvent.setUuid(eventId);
        setContentView(R.layout.activity_attendee_list);

        // for testing, no event is passed yet
        alController = new AttendeeListController();
        // TODO: Uncomment
        // alController = new AttendeeListController(event);
        dbController = new DatabaseController();
        eventTitle = findViewById(R.id.event_title_text);
        checkedInListView = findViewById(R.id.checked_in_list);
        signedUpListView = findViewById(R.id.signed_up_list);
        switchToMapButton = findViewById(R.id.button_to_map_view);
        backButton = findViewById(R.id.back_button);


        signedUpUserAdapter = new UserArrayAdapter(this, signedUpUsers);
        checkedInUserAdapter = new UserArrayAdapter(this, checkedInUsers);

        signedUpListView.setAdapter(signedUpUserAdapter);
        checkedInListView.setAdapter(checkedInUserAdapter);


        switchToMapButton.setOnClickListener(listener -> {
            Intent startMapActivity = new Intent(AttendeeListActivity.this, MapActivity.class);
            startActivity(startMapActivity);
        });

        signedUpUserAdapter.notifyDataSetChanged();
        checkedInUserAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetSignedUpUsersCallback(Event event, List<?> userIDs) {
        try {
            for (int i = 0; i < userIDs.size(); i++) {
                event.signUpUser((String) userIDs.get(i));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onGetCheckedInUsersCallback(Event event, List<?> userIDs) {
        try {
            for (int i = 0; i < userIDs.size(); i++) {
                event.checkInUser((String) userIDs.get(i));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public interface GetSignedUpUserCallback {}
}
