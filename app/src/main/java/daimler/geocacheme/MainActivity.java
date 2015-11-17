package daimler.geocacheme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
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
            }
        });
    }
}
