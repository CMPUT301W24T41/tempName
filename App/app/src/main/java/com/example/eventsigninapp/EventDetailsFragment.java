package com.example.eventsigninapp;


import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.eventsigninapp.DatabaseController.EventImageUriCallbacks;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * This class acts as a controller for the event details page.
 */
public class EventDetailsFragment extends Fragment {
    DatabaseController databaseController = new DatabaseController();
    UserController userController = new UserController();
    private TextView eventDescription, announcement;
    private ImageView eventPoster;
    private Button backButton, editEventButton, notifyUsersButton;
    private ToggleButton signUpButton;
    private Event event;
    private ArrayList<String> signedUpUsersUUIDs = new ArrayList<>();
    private String eventCreator;

    /**
     * Used for passing in data through Bundle from
     * Event list fragment. Data passed should be Event Class.
     */
    static EventDetailsFragment newInstance(Event event) {
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        EventController eventController = new EventController(event);

        EventDetailsFragment eventFragment = new EventDetailsFragment();
        eventFragment.setArguments(args);
        return eventFragment;
    }


    /**
     *
     * Called when fragment is created.
     * Setting display details to data passed through Bundle.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the event from the bundle passed from the EventListFragment
        Bundle bundle = getArguments();
        if (bundle != null) {
            event = (Event) bundle.getSerializable("event");
            if (event == null) {
                // Handle the case where event is null
                Log.e("EventDetailsFragment", "Event is null.");
            }
        } else {
            // Handle the case where bundle is null
            Log.e("EventDetailsFragment", "Bundle is null.");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_eventdetails, container, false);

        // Find views
        eventDescription = view.findViewById(R.id.eventDetails);
        announcement = view.findViewById(R.id.eventAnnouncements);
        eventPoster = view.findViewById(R.id.poster);
        editEventButton = view.findViewById(R.id.editEventButton);
        notifyUsersButton = view.findViewById(R.id.notifyUsersButton);
        backButton = view.findViewById(R.id.btnEventDetails);
        signUpButton = view.findViewById(R.id.signUpButton);


        // Retrieve the event from the bundle passed from the EventListFragment
        if (event != null) {
            // Use the event object to update the UI
            // Call the getEventPoster function
            databaseController.getEventPoster(event.getUuid(), callback);
            // get Event from firestore - possibly not used or implemented correctly
            databaseController.getEventFromFirestore(event.getUuid(), getEventCallback);
            // get signed uo users from firestore
            databaseController.getSignedUpUsersFromFirestore(event, getSignedUpUsersCallback);
            //get event creator
            databaseController.getEventCreatorUUID(event, getEventCreatorUUIDCallback);
        } else {
            // Handle the case where event is null
            Log.e("EventDetails", "Event is null. Cannot populate UI.");
        }

        notifyUsersButton.setOnClickListener(v -> {
            NotifyUsersBottomSheetFragment bottomSheetFragment = new NotifyUsersBottomSheetFragment();
            bottomSheetFragment.show(getChildFragmentManager(), bottomSheetFragment.getTag());
        });

        backButton.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());


        return view;
    }

    private void showSignUpPopup(Event event) {

        String[] signupOptions = {"All including promotions", "Important updates only", "Reminders and Updates", "None"};

        // Inflate the custom layout
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.notification_preferences_dialog, null);
        Spinner spinner = dialogView.findViewById(R.id.notification_preferences_spinner);

        // Populate the spinner with data
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, signupOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle signup confirmation
                confirmSignUp(spinner.getSelectedItem().toString(), event);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                signUpButton.setChecked(false);
                signUpButton.setText("Sign Up");
                dialog.dismiss();
            }
        });
        builder.create().show();


    }

    private void confirmSignUp(String selectedItem, Event event) {

        // Sign up the user for the event
        userController.signUp(event);
        signUpButton.setChecked(true);
        signUpButton.setText("Signed Up");
        signUpButton.setClickable(false);
        databaseController.addSignedUpUser(event, userController.getUser());
        databaseController.addEventToUser(userController.getUser(),event);
        subscribeToNotifications(selectedItem, event);
    }

    private void subscribeToNotifications(String selectedItem, Event event) {
        //signupOptions = {"All including promotions", "Important updates only", "Reminders and Updates", "None"};
        //<item>Important Update</item>
        //        <item>Reminders</item>
        //        <item>Promotion</item>
        if(selectedItem.equals("None")){
            return;
        }
        if(selectedItem.equals("Important updates only")){
            subscribeToTopic("Important Update");
        }
        if(selectedItem.equals("Reminders and Updates")){
            subscribeToTopic("Reminders");
            subscribeToTopic("Important Update");
        }
        if(selectedItem.equals("All including promotions")){
            subscribeToTopic("Promotion");
            subscribeToTopic("Important Update");
            subscribeToTopic("Reminders");
        }

    }



    EventImageUriCallbacks callback = new EventImageUriCallbacks() {
        @Override
        public void onEventPosterCallback(Uri imageUri) {
            // Handle successful retrieval of the image URI (e.g., load the image into an ImageView)
            System.out.println("EventPoster Image URI retrieved: " + imageUri.toString());
            Picasso.get().load(imageUri).into(eventPoster);

        }

        @Override
        public void onEventCheckInQRCodeCallback(Uri imageUri) {
            // to be implement
        }

        @Override
        public void onEventDescriptionQRCodeCallback(Uri imageUri) {
            // to be implemented
        }

        @Override
        public void onError(Exception e) {
            // Handle failure to retrieve the image URI
            Log.e("EventPoster", "Error getting image URI", e);
        }
    };

    DatabaseController.GetEventCallback getEventCallback = new DatabaseController.GetEventCallback() {
        @Override
        public void onGetEventCallback(Event event) {
            // Handle the retrieved event here
            if (event != null) {
                // Event found, update UI with event details
                updateUIWithEventDetails(event);
            } else {
                // Event not found or error occurred
                // Handle this case
                Log.e("EventDetails", "Event not found or error occurred while fetching event details.");
            }

        }
    };
    DatabaseController.GetSignedUpUsersCallback getSignedUpUsersCallback = new DatabaseController.GetSignedUpUsersCallback() {
        @Override
        public void onGetSignedUpUsersCallback(Event event, ArrayList<?> users) {
            // Handle the retrieved signed up users here
            if (users != null) {
                // Users found, update UI with signed up users
                signedUpUsersUUIDs = (ArrayList<String>) users;
                updateSignUpButton(event);
                Log.d("EventDetails", "Signed up users found: " + signedUpUsersUUIDs.toString());
            } else {
                // Users not found or error occurred
                // Handle this case
                Log.e("EventDetails", "Signed up users not found or error occurred while fetching signed up users.");
            }
        }
    };

    DatabaseController.GetEventCreatorUUIDCallback getEventCreatorUUIDCallback = new DatabaseController.GetEventCreatorUUIDCallback() {
        @Override
        public void onGetEventCreatorUUIDCallback(Event event, String creatorUUID) {
            if (creatorUUID != null){
                eventCreator = creatorUUID;
                if(isUserOwner()){
                    editEventButton.setVisibility(View.VISIBLE);
                    notifyUsersButton.setVisibility(View.VISIBLE);
                    signUpButton.setVisibility(View.GONE);

                }
                else{
                    editEventButton.setVisibility(View.GONE);
                    notifyUsersButton.setVisibility(View.GONE);
                    signUpButton.setVisibility(View.VISIBLE);
                }

                Log.d("EventDetails","Event creator: " + eventCreator);

            }else{
                Log.e("EventDetails", "Event creatorUUID not found or error occurred while fetching");
            }
        }
    };

    private boolean isUserOwner(){
        return eventCreator.equals(userController.getUser().getId());
    }
    private boolean isUserSignedUp(){
        Log.d("EventDetails", "isUserSignedUp: " + signedUpUsersUUIDs.toString());
        return signedUpUsersUUIDs.contains(userController.getUser().getId());

    }

    private void updateUIWithEventDetails(Event event) {
        // Update UI elements with event details
        eventDescription.setText(event.getDescription());
        //announcement.setText(event.getAnnouncement(); Not implemented yet
        // You can handle other UI elements here as well
    }
    private void updateSignUpButton(Event event){
        if(isUserSignedUp()){
            signUpButton.setChecked(true);
            signUpButton.setText("Signed Up");
            signUpButton.setClickable(false);
        }
        else{
            signUpButton.setChecked(false);
            signUpButton.setText("Sign Up");
            signUpButton.setOnClickListener(v -> {
                showSignUpPopup(event);
            });
        }
    }

    private void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic + event.getUuid())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        Log.d("EventDetails", msg);
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
