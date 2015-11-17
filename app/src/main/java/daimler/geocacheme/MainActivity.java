package daimler.geocacheme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import daimler.geocacheme.GeoCacheLogic.GeoCache;
import daimler.geocacheme.UserManagement.User;

public class MainActivity extends AppCompatActivity
{
    SharedPreferences userPrefs;
    SharedPreferences.Editor prefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageButton mapButton = (ImageButton) findViewById(R.id.mapbutton);

        View.OnClickListener findClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Getting reference to EditText to get the user input location
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        };

        // Setting button click event listener for the find button
        mapButton.setOnClickListener(findClickListener);

        ImageButton userButton = (ImageButton) findViewById(R.id.userbutton);

        userButton.setImageResource(R.drawable.user);

        ImageButton optionsButton = (ImageButton) findViewById(R.id.optionsbutton);

        optionsButton.setImageResource(R.drawable.optionsmenue);

        View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.user_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(view);
        final EditText userInput = (EditText) view.findViewById(R.id.userinput);

        builder.setCancelable(true).setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String input = userInput.getText().toString();
                String id = UUID.randomUUID().toString();
                User user = new User(input, id);
                saveUserIntoPrefs(user);
            }
        });
        if (getUserFromPrefs() == null)
        {
            builder.show();
        }

    }

    public void saveUserIntoPrefs(User user)
    {
        userPrefs = getPreferences(MODE_PRIVATE);
        prefsEditor = userPrefs.edit();
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user); // myObject - instance of MyObject
        prefsEditor.putString("UserObject", jsonUser);
        prefsEditor.apply();
    }

    public User getUserFromPrefs()
    {
        userPrefs = getPreferences(MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonUser = userPrefs.getString("UserObject", "");
        Type type = new TypeToken<User>()
        {
        }.getType();
        return gson.fromJson(jsonUser, type);
    }
}
