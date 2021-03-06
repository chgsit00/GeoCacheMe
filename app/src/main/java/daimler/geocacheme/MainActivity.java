package daimler.geocacheme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.UUID;

import android.os.Handler;

import daimler.geocacheme.GeoCacheLogic.GeoCacheProvider;
import daimler.geocacheme.InternetConnection.InternetConnectionTester;
import daimler.geocacheme.Server.GeoCacheServerProvider;
import daimler.geocacheme.Server.SaveGeoCache;
import daimler.geocacheme.Server.UserServerProvider;
import daimler.geocacheme.Server.VisitGeoCache;
import daimler.geocacheme.UserManagement.User;
import daimler.geocacheme.UserManagement.UserManagement;

public class MainActivity extends AppCompatActivity
{
    GeoCacheServerProvider geoCacheServerProvider;
    InternetConnectionTester internetConnectionTester;
    boolean internetCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        geoCacheServerProvider = new GeoCacheServerProvider();
        internetConnectionTester = new InternetConnectionTester();

        GeoCacheProvider.SetGeoCacheListfromPrefs(MainActivity.this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageButton mapButton = (ImageButton) findViewById(R.id.mapbutton);
        mapButton.setImageResource(R.drawable.map);
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

        View.OnClickListener findClickListener2 = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Getting reference to EditText to get the user input location
                startActivity(new Intent(MainActivity.this, UserManagementActivity.class));
            }
        };
        userButton.setOnClickListener(findClickListener2);

        ImageButton optionsButton = (ImageButton) findViewById(R.id.optionsbutton);

      //  optionsButton.setImageResource(R.drawable.optionsmenu);

        ImageButton exitButton = (ImageButton) findViewById(R.id.exitbutton);
        exitButton.setImageResource(R.drawable.xicon);

        View.OnClickListener findClickListener3 = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                View exitView = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.exit_dialog, null);
                AlertDialog.Builder exitBuilder = new AlertDialog.Builder(MainActivity.this);
                exitBuilder.setView(exitView);
                exitBuilder.setCancelable(true);
                exitBuilder.setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        dialog.cancel();
                    }
                });
                exitBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        dialog.cancel();
                        finish();
                        System.exit(0);
                    }
                });
                exitBuilder.show();
            }
        };
        exitButton.setOnClickListener(findClickListener3);

        View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.user_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(view);
        final EditText userInput = (EditText) view.findViewById(R.id.userinput);
        builder.setTitle("Start App");
        builder.setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String input = userInput.getText().toString();
                String id = UUID.randomUUID().toString();
                User user = new User(input, id);
                UserManagement.saveUserIntoPrefs(user, MainActivity.this);
            }
        });
        if (UserManagement.getUserFromPrefs(MainActivity.this) == null)
        {
            builder.show();
        }

        Thread GeoCacheServerProviderThread = new Thread(GeoCacheServerProviderRunnable);
        Thread saveGeoCacheThread = new Thread(saveGeoCacheRunnable);
        Thread userServerProviderThread = new Thread(userServerProviderRunnable);
        Thread visitGeoCacheThread = new Thread(visitGeoCacheRunnable);
        GeoCacheServerProviderThread.start();
        saveGeoCacheThread.start();
        userServerProviderThread.start();
        visitGeoCacheThread.start();
    }

    class CheckInternetConnectionTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                if (internetConnectionTester.hasActiveInternetConnection(MainActivity.this))
                {
                    internetCheck = true;
                }
                else internetCheck = false;
                return "1";
            } catch (Exception e)
            {
                Log.i("Internet Connection", "Connection failed");
            }
            return "0";
        }
    }

    public Runnable GeoCacheServerProviderRunnable = new Runnable()
    {
        public Handler handler = new Handler();

        @Override
        public void run()
        {
            new CheckInternetConnectionTask().execute();
            if (internetCheck)
            {
                try
                {
                    geoCacheServerProvider.StartGeoCacheServerProvider();
                } catch (Exception e)
                {
                    handler.postDelayed(GeoCacheServerProviderRunnable, 1000);
                }
                GeoCacheProvider.saveGeoCacheListIntoPrefs(MainActivity.this);
            }
            handler.postDelayed(GeoCacheServerProviderRunnable, 1000);
        }
    };

    public Runnable saveGeoCacheRunnable = new Runnable()
    {
        public Handler handler = new Handler();
        final SaveGeoCache saveGeoCache = new SaveGeoCache();

        @Override
        public void run()
        {
            User Owner = UserManagement.getUserFromPrefs(MainActivity.this);
            new CheckInternetConnectionTask().execute();
            if (internetCheck)
            {
                try
                {
                    if (Owner != null)
                    {
                        saveGeoCache.StartSaveGeoCache(Owner.ID);
                    }
                } catch (Exception e)
                {
                    handler.postDelayed(saveGeoCacheRunnable, 1000);
                }
            }
            handler.postDelayed(saveGeoCacheRunnable, 1000);
        }
    };

    public Runnable userServerProviderRunnable = new Runnable()
    {
        public Handler handler = new Handler();
        final UserServerProvider userServerProvider = new UserServerProvider();
        @Override
        public void run()
        {
            User Owner = UserManagement.getUserFromPrefs(MainActivity.this);
            new CheckInternetConnectionTask().execute();
            if (internetCheck)
            {
                try
                {
                    if (Owner != null)
                    {
                        userServerProvider.StartUserServerProvider(Owner.Name, Owner.ID);
                    }
                } catch (Exception e)
                {
                    handler.postDelayed(userServerProviderRunnable, 1000);
                }
            }
            handler.postDelayed(userServerProviderRunnable, 1000);
        }
    };

    public Runnable visitGeoCacheRunnable = new Runnable()
    {
        public Handler handler = new Handler();
        final VisitGeoCache visitGeoCache = new VisitGeoCache();

        @Override
        public void run()
        {
            User Owner = UserManagement.getUserFromPrefs(MainActivity.this);
            new CheckInternetConnectionTask().execute();
            if (internetCheck)
            {
                try
                {
                    if (Owner != null)
                    {
                        visitGeoCache.StartVisitGeoCache(Owner.ID);
                    }
                } catch (Exception e)
                {
                    handler.postDelayed(visitGeoCacheRunnable, 1000);
                }
            }
            handler.postDelayed(visitGeoCacheRunnable, 1000);
        }
    };
}
