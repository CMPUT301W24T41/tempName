package com.example.eventsigninapp;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.UUID;

public class AdminCodeGeneratorFragment extends Fragment {


    DatabaseController databaseController = new DatabaseController();
    // onCreate method
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // onCreateView method
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.admin_code_generator, container, false);

        TextView codeGenerated = view.findViewById(R.id.codeGenerated);
        Button generateButton = view.findViewById(R.id.adminGenerateButton);

        codeGenerated.setText("No code Generated Yet");

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String adminCode = UUID.randomUUID().toString();
                codeGenerated.setText(adminCode);
                databaseController.pushAdminCode(adminCode);
            }
        });

        return view;
    }
}
