package daimler.geocacheme.UserManagement;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by CGsch on 24.11.2015.
 */
public class UserManagement
{
    static SharedPreferences userPrefs;
    static SharedPreferences.Editor prefsEditor;

    public static void saveUserIntoPrefs(User user, Context context)
    {
        userPrefs = context.getSharedPreferences("UserObject", Context.MODE_PRIVATE);
        prefsEditor = userPrefs.edit();
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user); // myObject - instance of MyObject
        prefsEditor.putString("UserObject", jsonUser);
        prefsEditor.apply();
    }

    public static User getUserFromPrefs(Context context)
    {
        userPrefs = context.getSharedPreferences("UserObject", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonUser = userPrefs.getString("UserObject", "");
        Type type = new TypeToken<User>()
        {
        }.getType();
        return gson.fromJson(jsonUser, type);
    }
}
