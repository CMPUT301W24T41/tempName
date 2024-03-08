package com.example.eventsigninapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Document;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DatabaseController {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final FirebaseStorage storage = FirebaseStorage.getInstance();;

    public DatabaseController() {}

    /**
     * This method stores a user or updates an existing user to the database
     * @param user the user to add
     */
    public void putUserToFirestore(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("firstName", user.getFirstName());
        userData.put("lastName", user.getLastName());
        userData.put("contact", user.getContact());

        DocumentReference userDocument = db.collection("users").document(user.getId());

        userDocument.set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Database", "putUserToFirestore: User data successfully updated"))
                .addOnFailureListener(e -> Log.e("Database", "putUserToFirestore: Error updating user data", e));
    }



    /**
     * This function gets a user from the database using the given id and updates the current
     * user of a given userController object with information of that user
     * if the task fails, this creates a new user instead to add
     * @param id the id of the user to acquire
     * @param userController the UserController to update
     */
    public void updateWithUserFromFirestore(String id, UserController userController) {
        db.collection("users")
                .whereEqualTo("id", id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        User pulledUser = new User(id, document.getString("firstName"), document.getString("lastName"), document.getString("contact"));
                        userController.setUser(pulledUser);
                        this.updateWithProfPictureFromWeb(pulledUser);
                    } else {
                        // user does not exist, create a new user
                        User createdUser = new User(id);
                        userController.setUser(createdUser);
                    }
                });
    }


    /**Finds a user based on their unique id in the database and fetches it from the database,
     * returning the user in a callback
     *
     * @param id:       the unique id of the user to be fetched
     * @param callback: due to the asynchronous nature of firestore, to fetch the user properly a callback is needed
     *                  to access a user fetched from the database, use the following code:
     *                  userIDController.getOtherUserFromFirestore(userID, new UserIDController.userCallback() {
     *                  public void onCallback(User user) {
     *
     *                  }
     *                  });
     */
    public void getUserFromFirestore(String id, UserCallback callback) {
        db.collection("users")
                .whereEqualTo("id", id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        User pulledUser = new User(id, document.getString("firstName"), document.getString("lastName"), document.getString("contact"));
                        callback.onCallback(pulledUser);
                    } else {
                        callback.onError(new Exception("failed to retrieve user"));
                    }
                });
    }


    /**
     * This method uploads the given picture uri to the storage for the given user
     * @param picture the picture to upload
     * @param user the user whose profile is being updated
     */
    public void uploadProfilePicture(Uri picture, User user) {
        StorageReference storageRef = storage.getReference();

        // Reference to store the image
        StorageReference profilePicRef = storageRef.child("profile_pictures/" + user.getId());

        // Upload file to Firebase Storage
        profilePicRef.putFile(picture)
                .addOnSuccessListener(taskSnapshot -> {
                    profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Update upon successful completion
                        user.setPicture(uri);
                    }).addOnFailureListener(e -> {
                        Log.e("Database", "uploadProfilePicture: Error, failure to get URL data", e);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("Database", "uploadProfilePicture: Error, failure to upload image", e);
                });
    }


    /**
     * this updates the profile of a given user with the result acquired from the database
     * @param user the user to be updated
     */
    public void updateWithProfPictureFromWeb(User user) {
        StorageReference storageRef = storage.getReferenceFromUrl(user.getImgUrl());

        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            user.setPicture(uri);
            this.putUserToFirestore(user); // Update the user's profile in Firestore
        }).addOnFailureListener(e -> {
            // Handle failure to retrieve the URL
            Log.e("Database", "updateWithProfPictureFromWeb: Failed to retrieve image URL", e);
        });
    }


    /**
     * fetches the profile picture of other users on the platform using a callback
     * @param userID the id of the user's picture to fetch
     * @param callback due to the asynchronous nature of firestore, to fetch the user properly a callback is needed
     *      *                  to access a user fetched from the database, use the following code:
     *      *                  userIDController.getOtherUserProfilePicture(userID, new UserIDController.userCallback() {
     *      *                  public void onCallback(Uri picture) {
     *      *
     *      *                  }
     *      *                  });
     */
    public void getUserProfilePicture(String userID, ImageUriCallback callback) {
        StorageReference storageRef = storage.getReference();
        StorageReference profilePicRef = storageRef.child("profile_pictures/" + userID);

        profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
            callback.onImageUriCallback(uri);
        }).addOnFailureListener(e -> {
            Log.e("Database", "getOtherUserProfilePicture: Failed to retrieve image", e);
            callback.onError(e);
        });
    }

    public void putEventPosterToFirestore(String eventID, Uri imageUri) {
        if (imageUri == null) {
            return;
        }

        StorageReference storageRef = storage.getReference();
        StorageReference eventPosterRef = storageRef.child("event_posters/" + eventID);

        eventPosterRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> eventPosterRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d("Database", "Image download URL: " + uri.toString());
                }).addOnFailureListener(e -> {
                    Log.e("Database", "putEventPosterToFirestore: Error, failure to get URL data", e);
                }))
                .addOnFailureListener(e -> {
                    Log.e("Database", "putEventPosterToFirestore: Error, failure to upload image", e);
                });
    }

    public void putEventCheckInQRCodeToFirestore(String eventID, Uri imageUri) {
        if (imageUri == null) {
            return;
        }

        StorageReference storageRef = storage.getReference();
        StorageReference eventQRCodeRef = storageRef.child("event_check_in_qr_codes/" + eventID);

        eventQRCodeRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> eventQRCodeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d("Database", "Image download URL: " + uri.toString());
                }).addOnFailureListener(e -> {
                    Log.e("Database", "putEventCheckInQRCodeToFirestore: Error, failure to get URL data", e);
                }))
                .addOnFailureListener(e -> {
                    Log.e("Database", "putEventCheckInQRCodeToFirestore: Error, failure to upload image", e);
                });
    }

    public void putEventDescriptionQRCodeToFirestore(String eventID, Uri imageUri) {
        if (imageUri == null) {
            return;
        }

        StorageReference storageRef = storage.getReference();
        StorageReference eventQRCodeRef = storageRef.child("event_description_qr_codes/" + eventID);

        eventQRCodeRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> eventQRCodeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d("Database", "Image download URL: " + uri.toString());
                }).addOnFailureListener(e -> {
                    Log.e("Database", "putEventDescriptionQRCodeToFirestore: Error, failure to get URL data", e);
                }))
                .addOnFailureListener(e -> {
                    Log.e("Database", "putEventDescriptionQRCodeToFirestore: Error, failure to upload image", e);
                });
    }

    public void getEventImages(String eventID, EventImageUriCallbacks callbacks) {
        getEventPoster(eventID, callbacks);
        getEventCheckInQRCode(eventID, callbacks);
        getEventDescriptionQRCode(eventID, callbacks);
    }

    public void getEventPoster(String eventID, EventImageUriCallbacks callback) {
        StorageReference storageRef = storage.getReference();
        StorageReference eventPosterRef = storageRef.child("event_posters/" + eventID);

        eventPosterRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Log.d("Database", "Image download URL: " + uri.toString());
            callback.onEventPosterCallback(uri);
        }).addOnFailureListener(e -> {
            Log.e("Database", "getEventPoster: Failed to retrieve image", e);
            callback.onError(e);
        });
    }

    public void getEventCheckInQRCode(String eventID, EventImageUriCallbacks callback) {
        StorageReference storageRef = storage.getReference();
        StorageReference eventQRCodeRef = storageRef.child("event_check_in_qr_codes/" + eventID);

        eventQRCodeRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Log.d("Database", "Image download URL: " + uri.toString());
            callback.onEventCheckInQRCodeCallback(uri);
        }).addOnFailureListener(e -> {
            Log.e("Database", "getEventCheckInQRCode: Failed to retrieve image", e);
            callback.onError(e);
        });
    }

    public void getEventDescriptionQRCode(String eventID, EventImageUriCallbacks callback) {
        StorageReference storageRef = storage.getReference();
        StorageReference eventQRCodeRef = storageRef.child("event_description_qr_codes/" + eventID);

        eventQRCodeRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Log.d("Database", "Image download URL: " + uri.toString());
            callback.onEventDescriptionQRCodeCallback(uri);
        }).addOnFailureListener(e -> {
            Log.e("Database", "getEventDescriptionQRCode: Failed to retrieve image", e);
            callback.onError(e);
        });
    }

    public void pushEventToFirestore(Event event) {
        String uuid = event.getUuid();
        DocumentReference eventRef = db.collection("events").document(event.getUuid());
        eventRef.set(event.toMap())
                .addOnSuccessListener(aVoid -> Log.d("Database", "pushEventToFirestore: Event data successfully updated"))
                .addOnFailureListener(e -> Log.e("Database", "pushEventToFirestore: Error updating event data", e));

        putEventCheckInQRCodeToFirestore(uuid, event.getCheckInQRCodeUri());
        putEventDescriptionQRCodeToFirestore(uuid, event.getDescriptionQRCodeUri());
        putEventPosterToFirestore(uuid, event.getPosterUri());
    }

    public void getEventFromFirestore(String uuid, GetEventCallback callback) {
        DocumentReference eventRef = db.collection("events").document(uuid);

        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    callback.onGetEventCallback(document.toObject(Event.class));
                } else {
                    callback.onGetEventCallback(null);
                }
            }
        });
    }

    /**
     * This function gets all the events from the database.
     * @param callback callback to add the events to the list of events
     */
    public void getAllEventsFromFirestore(GetAllEventsCallback callback) {
        CollectionReference events = db.collection("events");
        events.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                callback.onGetAllEventsCallback(doc.toObject(Event.class));
                            }
                        } else {
                            Log.e("DEBUG", "Error retrieving events");
                        }
                    }
                });
    }

    public interface GetEventCallback {
        void onGetEventCallback(Event event);
    }

    public interface EventImageUriCallbacks {
        void onEventPosterCallback(Uri imageUri);
        void onEventCheckInQRCodeCallback(Uri imageUri);
        void onEventDescriptionQRCodeCallback(Uri imageUri);
        void onError(Exception e);
    }

    /**
     * This interface allows an event to be retrieved from the database and added to the list of events.
     */
    public interface GetAllEventsCallback {
        void onGetAllEventsCallback(Event event);
    }


    /**
     * This interface allows images to be retrieved
     */
    public interface ImageUriCallback {
        void onImageUriCallback(Uri imageUri);
        void onError(Exception e);
    }

    /**
     * This interface allows users to be retrieved
     */
    public interface UserCallback {
        void onCallback(User user);

        void onError(Exception e);
    }
}