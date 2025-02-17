package com.example.eventsigninapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;


@RunWith(RobolectricTestRunner.class)
public class UserControllerTest {
    @Mock
    private Context mockContext;

    @Mock
    private SharedPreferences mockPreferences;

    @Mock
    private SharedPreferences.Editor mockEditor;

    @Mock
    private EventController mockEventController;

    @Mock
    private Event mockEvent;

    private UserController userController;

    private User mockUser() {
        return new User();
    }


    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userController = new UserController();
    }

    @Test
    public void testGetUserID_existingUUID() {

        String existingUUID = "existingUUID";
        //mock the process of acquiring a UUID
        when(mockContext.getSharedPreferences("ID", Context.MODE_PRIVATE)).thenReturn(mockPreferences);
        when(mockPreferences.getString("UUID_Default", null)).thenReturn(existingUUID);

        //Perform this using the function
        String result = userController.getUserID(mockContext);

        //check if we acquire the same result
        assertEquals(existingUUID, result);
    }

    @Test
    public void testGetUserID_generateUUID() {
        //mock the results of creating and generating a UUID
        when(mockContext.getSharedPreferences("ID", Context.MODE_PRIVATE)).thenReturn(mockPreferences);
        when(mockPreferences.getString("UUID_Default", null)).thenReturn(null);
        when(mockPreferences.edit()).thenReturn(mockEditor);

        // check if the function performs the necessary action
        String result = userController.getUserID(mockContext);

        // see if the the result was saved to the preference
        assertNotNull(result);
        verify(mockPreferences.edit()).putString("UUID_Default", result);
    }

    @Test
    public void testSaveUUID() {
        //mock the process of saving a result to the preference
        String uuid = "testUUID";
        when(mockContext.getSharedPreferences("ID", Context.MODE_PRIVATE)).thenReturn(mockPreferences);
        when(mockPreferences.edit()).thenReturn(mockEditor);

        // attempt to do so with the function
        userController.saveUUID(mockContext, uuid);

        // see if the result is added then saved and committed
        verify(mockEditor).putString("UUID_Default", uuid);
        verify(mockEditor).apply();
    }


    @Test
    public void testEditProfile() {
        User mockUser = new User("123456", "John", "Doe", "another.com", "123456");


        userController.setUser(mockUser);

        userController.editProfile("Jane", "Smith", "789012", "test.com", Uri.parse("images/prof"));

        assertEquals("Jane", userController.getUser().getFirstName());
        assertEquals("Smith", userController.getUser().getLastName());
        assertEquals("789012", userController.getUser().getContact());
        assertEquals("test.com", userController.getUser().getHomePageUrl());
        assertEquals("images/prof", userController.getUser().getPicture().toString());
    }

}