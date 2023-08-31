package com.example.finelpro;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.protobuf.Internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneralIndicesFragment extends Fragment {////


    private ListView listView;
    private Button showWinnerButton;
    private Button showEventsByRiskButton;
    private DB db;
    private String winnerName;
    int winnerScorel = 0;

    public GeneralIndicesFragment() {
    }
    //creating the user interface for the fragment by inflating the fragment's layout from an XML resource file
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_general_indices, container, false);

        db = DB.getInstance(getContext());
        showWinnerButton = rootView.findViewById(R.id.showWinnerButton);
        showEventsByRiskButton = rootView.findViewById(R.id.showEventsByRiskButton);
        listView = rootView.findViewById(R.id.listView);
        // Load all creators
        db.open();
        List<String> allEventCreatorsreators = db.getAllEventCreators();
        //Users and scoring(Every event that the user raises is a point)
        Map<String, Integer> usersScoring = new HashMap<>();
        for (String creator : allEventCreatorsreators) {
            int scoring = db.getAllmyEvents(creator).size();
            usersScoring.put(creator, scoring);
        }


        allEventCreatorsreators.clear();
        //The requirement is to present in sorted order from the highest to the lowest according to the number of points
        List<Map.Entry<String, Integer>> sortedUserScoringList = sortHashMapByValues(usersScoring, false);

        for (Map.Entry<String, Integer> entry : sortedUserScoringList) {
            String key = entry.getKey();
            int value = entry.getValue();

            String concatenatedString = key + " " + String.valueOf(value);
            allEventCreatorsreators.add(concatenatedString);
        }
        //Takes the first 10 members only
        List<String> first10Creators = allEventCreatorsreators.subList(0, Math.min(10, allEventCreatorsreators.size()));


        // Populate the list using an ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_list_item_1, first10Creators);
        listView.setAdapter(adapter);

        //The piece of code calculates for us who is the user with the highest number of points
        for (String user : db.getAllUsersWithApprovals()) {
            if (((db.getApprovedEventCountForUser(user) + db.getRejectedEventCountForUser(user)) * 3)+db.getEventsApprovedByMajorityCountForUser(user) > winnerScorel) {
                this.winnerScorel = ((db.getApprovedEventCountForUser(user) + db.getRejectedEventCountForUser(user)) * 3)+db.getEventsApprovedByMajorityCountForUser(user);
                this.winnerName = user;
            }
        }


        db.close();
        //A dialog opens showing the winning user
        showWinnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWinnerDialog();
            }
        });

        //A dialogue is opened that presents the risk level of the events and their frequency
        showEventsByRiskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEventsByRiskDialog();
            }
        });


        return rootView;
    }

    private void showWinnerDialog() {
        // Create a dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Set the dialog title and message with the winner's information
        String message = "The winner is: " + winnerName + " with " + String.valueOf(winnerScorel) + " points";
        builder.setTitle(R.string.WINNER_TITLE_DIALOG)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog when the "OK" button is clicked
                        dialog.dismiss();
                    }
                });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showEventsByRiskDialog() {
        int[] countOfRiskLevel = {0, 0, 0};//We will use it to keep the frequency for each risk level

        db.open();

        //Going over all the events and keeping the frequency of the summary levels in our array
        for (Event riskLevel : db.getAllEvents()) {
            if (riskLevel.getRiskLevel().equals("EASY"))
                countOfRiskLevel[0] = countOfRiskLevel[0] + 1;
            else if (riskLevel.getRiskLevel().equals("MEDIUM"))
                countOfRiskLevel[1] = countOfRiskLevel[1] + 1;
            else
                countOfRiskLevel[2] = countOfRiskLevel[2] + 1;
        }
//        // Create a dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
//
//        // Set the dialog title and message with the events by risk level information
        StringBuilder messageBuilder = new StringBuilder("Events by Risk Level:\nLOW:" + String.valueOf(countOfRiskLevel[0]) + "\nMEDIUM:" + String.valueOf(countOfRiskLevel[1]) + "\nHARD:" + String.valueOf(countOfRiskLevel[2]));
//
        builder.setTitle(R.string.RISKLEVEL_TITLE_DIALOG)
                .setMessage(messageBuilder.toString())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog when the "OK" button is clicked
                        dialog.dismiss();
                    }
                });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Sorting our Map where the first member is the user with the highest number of points and it goes down accordingly
    private List<Map.Entry<String, Integer>> sortHashMapByValues(Map<String, Integer> map, boolean descending) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());

        list.sort((entry1, entry2) -> {
            int value1 = entry2.getValue();
            int value2 = entry1.getValue();
            return descending ? Integer.compare(value2, value1) : Integer.compare(value1, value2);
        });

        return list;
    }

}
