package util;

import android.app.Application;
// we are creating this global class so that we can use the username and id anywhere throughout the application
public class JournalApi extends Application {

    private String username;
    private String userId;
    private static JournalApi instance;   // make this singleton

    public  JournalApi() {
    }

    public static JournalApi getInstance(){
        if(instance==null)
            instance=new JournalApi();

        return instance;
//when we want the Object of JournalApi, it would be null. At that time, as instance is null, the object will be created.
//
//After that, as instance would not be null, so we will return the existing reference of instance.
//this is a singleton pattern.
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
