package daimler.geocacheme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.UUID;

import daimler.geocacheme.GeoCacheLogic.GeoCache;
import daimler.geocacheme.GeoCacheLogic.GeoCacheProvider;
import daimler.geocacheme.UserManagement.User;
import daimler.geocacheme.UserManagement.UserManagement;

public class UserManagementActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView userNameView = (TextView) findViewById(R.id.username);

        userNameView.setText(UserManagement.getUserFromPrefs(UserManagementActivity.this).Name);

        TextView visitedGeoCachesView = (TextView) findViewById(R.id.geocachesvisited);

        String userId = UserManagement.getUserFromPrefs(UserManagementActivity.this).ID;
        int visited = CountVisitedGeoCaches(userId);
        visitedGeoCachesView.setText(""+visited);

     //   TextView userIdView = (TextView) findViewById(R.id.userid);
     //   userIdView.setText(UserManagement.getUserFromPrefs(UserManagementActivity.this).ID);

        Button changeUserNameButton = (Button) findViewById(R.id.usernamebutton);
        View.OnClickListener findClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Getting reference to EditText to get the user input location
                ChangeUserName();
            }
        };
        // Setting button click event listener for the find button
        changeUserNameButton.setOnClickListener(findClickListener);
    }

    public void ChangeUserName()
    {
        View view = (LayoutInflater.from(UserManagementActivity.this)).inflate(R.layout.user_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(UserManagementActivity.this);
        builder.setView(view);
        final EditText userInput = (EditText) view.findViewById(R.id.userinput);

        builder.setCancelable(true).setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String input = userInput.getText().toString();
                User user = UserManagement.getUserFromPrefs(UserManagementActivity.this);
                user.Name = input;
                UserManagement.saveUserIntoPrefs(user, UserManagementActivity.this);
                TextView userNameView = (TextView) findViewById(R.id.username);
                userNameView.setText(input);
            }
        });
        builder.show();
    }

    public static int CountVisitedGeoCaches(String userID)
    {
        int count = 0;
        for (GeoCache geoCache : GeoCacheProvider.GetGeoCacheList())
        {
            if (geoCache.Visited && !userID.equals(geoCache.OwnerID))
            {
                count++;
            }
        }
        return count;
    }
}
