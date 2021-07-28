package com.example.swiftmind;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.swiftmind.database.AppDatabase;
import com.example.swiftmind.database.Mood;
import com.example.swiftmind.database.MoodDao;
import com.example.swiftmind.database.Resources;
import com.example.swiftmind.database.ResourcesDao;
import com.example.swiftmind.intro.IntroActivity;
import com.example.swiftmind.notification.AlarmReceiver;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    /**
     * This is the core, single activity that runs throughout the lifetime of
     * the application. The rest of the UI consists of fragments embedded
     * into the UI for this activity using the Navigation component or
     * AlertDialogs and other Android components for requesting for input.
     */
    public static final String QUERY_MOOD_PARAMETER = "MainActivity.QueryMood";
    public static final String NOTIFICATION_CHANNEL_ID = "MainActivity.NotificationChan";

    private int mCurrentMood = 1;
    private int mCurrentMoodIntensity = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        /**
         * Runs when the activity is first launched. For more information about
         * the Android activity lifecycle, please refer to https://bit.ly/2q7i3eK.
         */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPrefs.getBoolean(IntroActivity.LAUNCHED_APP_BEFORE, false)) {
            Intent intent = new Intent(this, IntroActivity.class);
            finish();
            startActivity(intent);
            setupResourcesDatabase();
        }

        registerNotificationChannel();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        /**
         * This callback is called when the app is being restored after being paused.
         * This could be due to one of two reasons: the user opened the app from
         * the app icon or by clicking a notification from Talk.
         */
        super.onResume();

        // TODO: Remove, this is for demo purposes
        boolean resumingFromNotification = getIntent().getBooleanExtra(QUERY_MOOD_PARAMETER, false);
        if (resumingFromNotification) {
            showCurrentMoodDialog();
        } else {
            // Show notification if opening the app
            showNotification();
        }
    }

    void registerNotificationChannel() {
        /**
         * Registers a notification channel which is required to post notifications
         * to the user. This is done repeatedly whenever the app is started but
         * there is not problem with calling .createNotificationChannel repeatedly.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, "DailyNotification", importance);
            channel.setDescription("Talk.Notifications");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    void setupBottomNavigation() {
        /**
         * Links the BottomNavigationView element to the NavController that controls
         * the NavHost containing all the top-level UI fragments. The NavController
         * is then used to switch between UIs/fragments.
         */
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNav, navController);
    }

    void setupResourcesDatabase() {
        /**
         * Sets up the Resources database with all the articles.
         */
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        ResourcesDao resDao = database.resourcesDao();

        // Depressed = 1, Sad = 2, Angry = 3, Scared = 4, Moderate = 5, Happy = 6
        final int MoodDepressed = 1;
        final int MoodSad = 2;
        final int MoodAngry = 3;
        final int MoodScared = 4;
        final int MoodModerate = 5;
        final int MoodHappy = 6;

        // Create the list of all the resources
        Resources[] allResources = {
                // Depressed
                new Resources("What is real depression?", "When you’re depressed, you can’t just will yourself to “snap out of it.” But these tips can help put you on the road to recovery.", "https://www.medicalnewstoday.com/articles/314071", MoodDepressed),
                new Resources("Causes of depression", "Depression is a disorder that is evidenced by excessive sadness, loss of interest in enjoyable things, and low motivation.", "https://www.webmd.com/depression/guide/causes-depression", MoodDepressed),
                new Resources("Overcoming Depression", "Watch this video.", "https://www.youtube.com/watch?v=HvSOHmqWMAw", MoodDepressed),
                new Resources("Warning Signs of depression", "Do you think you might be depressed? Here are some of the signs and symptoms to look for—and tips for getting the help you need.", "https://www.helpguide.org/articles/depression/depression-symptoms-and-warning-signs.htm", MoodDepressed),

                // Sad
                new Resources("Reasons of Sadness", "Know the top reasons causing it and learn how to overcome it.", "https://www.healthline.com/health/mental-health/why-am-i-sad-for-no-reason", MoodSad),
                new Resources("Sadness or Depression", "People often think they’re depressed when they’re sad, or sad when they’re depressed.", "https://www.healthline.com/health/depression/depression-vs-sadness", MoodSad),
                new Resources("Getting over you Sadness", "Ever felt sad or stressed for no apparent reason? Watch this video.", "https://au.reachout.com/articles/why-am-i-sad-all-the-time", MoodSad),
                new Resources("Motivational Stories", "If you're afraid that your depressed, there are many things you can do to help figure it out.", "https://thoughtcatalog.com/january-nelson/2018/09/motivational-stories/#:~:text=2.,a%20nail%20into%20the%20fence.", MoodSad),

                // Angry
                new Resources("Learning about Anger Management", "Is your temper hijacking your life? These tips and techniques can help you get anger under control and express your feelings in healthier ways.", "https://www.mayoclinic.org/healthy-lifestyle/adult-health/in-depth/anger-management/art-20045434", MoodAngry),
                new Resources("Gaining control on yourself", "We all know what anger is, and we've all felt it: whether as a fleeting annoyance or as full-fledged rage.", "https://www.americanexpress.com/en-us/business/trends-and-insights/articles/8-easy-ways-to-increase-your-self-control/", MoodAngry),
                new Resources("Impact of Anger", "Watch this video.", "https://www.youtube.com/watch?v=vyMx7s9cThU", MoodAngry),
                new Resources("Why Therapy?", "Anger can be a force for good. But ongoing, intense anger is neither helpful nor healthy. Here's how to get a grip.", "https://www.healthline.com/health/benefits-of-therapy#individual", MoodAngry),

                // Scared
                new Resources("Why fear?", "Is a phobia keeping you from doing things you’d like to do? Learn how to recognize, treat, and overcome the problem.", "https://www.verywellmind.com/why-am-i-afraid-of-people-3024436", MoodScared),
                new Resources("Getting over Loneliness and Fear", "The fact that you feel scared about these intrusive thought means that you need to see a psychotherapist.", "https://www.mentalhealth.org.uk/publications/overcome-fear-anxiety", MoodScared),
                new Resources("Stand to be Strong", "Listen to song about loneliness.", "https://www.youtube.com/watch?v=iyEUvUcMHgE", MoodScared),
                new Resources("Controlling your fears", "So, you're scared. Let's finally talk about that, shall we?", "https://www.nhsinform.scot/healthy-living/mental-wellbeing/fears-and-phobias/ten-ways-to-fight-your-fears", MoodScared),

                // Moderate
                new Resources("Being Human", "Remember the fire in the belly you felt on the way to achieving a goal?", "https://medium.com/@lucia.karachorova/how-does-it-feel-to-be-human-aec73e7bec85", MoodModerate),
                new Resources("Learning about Yourself", "Leah Reich was one of the first internet advice columnists", "https://lifeskillsthatmatter.com/learn-about-yourself/", MoodModerate),
                new Resources("Bringing more Positivity", "Motivate yourself with this video", "https://www.youtube.com/watch?v=2o8fmUlHAyk", MoodModerate),
                new Resources("Best use of time", "Watch this video about using your free time", "https://www.youtube.com/watch?v=afoAXho6EHs", MoodModerate),

                // Happy
                new Resources("Staying Happy", "Can you be wrong about whether you are happy?", "https://www.lifehack.org/articles/communication/10-scientifically-proven-ways-stay-happy-all-the-time.html", MoodHappy),
                new Resources("Spreading Happiness", "Researchers have known for decades that certain activities make us feel better, and they're just beginning to understand what happens in the brain to boost our mood.", "https://www.onecrazyhouse.com/spread-happiness/", MoodHappy),
                new Resources("Stay peaceful Playlitst", "Listen to Pharrell sing about being Happy!", "https://www.youtube.com/watch?v=ZbZSe6N_BXs", MoodHappy),
                new Resources("Motivation", "We all want to be happy. Period. In fact, I would argue that nearly everything we do, whether it’s working, marrying, running, or even filing our taxes is done with an overarching purpose: To feel happier.", "https://www.psychologytoday.com/us/basics/motivation", MoodHappy),
        };

        resDao.insertAll(allResources);

        Toast.makeText(this, "Resource database loaded", Toast.LENGTH_LONG).show();
    }

    void showCurrentMoodDialog() {
        /**
         * Shows the user a dialog that asks them for their current mood then
         * stores the result inside of an instance variable.
         */
        CharSequence moods[] = {
                // Depressed = 1, Sad = 2, Angry = 3, Scared = 4, Moderate = 5, Happy = 6
                "Depressed", "Sad", "Angry", "Scared", "Moderate", "Happy"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DarkAlertDialog);
        builder.setTitle("How are you feeling?");
        builder.setSingleChoiceItems(moods, 0, new MoodDialogChoiceListener());
        builder.setPositiveButton("Next", new MoodDialogListener());
        builder.show();
    }

    void showMoodIntensityDialog() {
        /**
         * Shows the dialog that asks the user the intensity of the mood
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.DarkAlertDialog);
        SeekBar seekBar = new SeekBar(MainActivity.this);
        seekBar.setMax(4);
        seekBar.setOnSeekBarChangeListener(new MoodIntensityDialogSeekListener());

        builder.setTitle("How intense is this feeling?");
        builder.setView(seekBar);
        builder.setPositiveButton("Save", new MoodIntesityDialogListener());

        builder.show();
    }

    void saveMoodToDatabase() {
        /**
         * Saves the current mood state to the SQLite database.
         */
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        MoodDao moodDao = database.moodDao();

        // Get the last entered date
        List<Mood> allMoods = moodDao.getAll();
        int numberOfMoods = allMoods.size();
        int lastEnteredDate = 0;
        if (numberOfMoods > 0) {
             lastEnteredDate = allMoods.get(numberOfMoods - 1).date;
        }

        // Create the new Mood
        Mood currentMood = new Mood(lastEnteredDate + 1, mCurrentMood, mCurrentMoodIntensity);
        moodDao.insert(currentMood);

        // Ask the user (again)
        showNotification();
    }

    void showNotification() {
        /**
         * Sets the alarm to display a notification in the notification bar asking the user to hit
         * the notification so that they get prompted to enter their mood. The notification is
         * shown 3 seconds after requested for demo purposes.
         * TODO: Remove.
         */
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr != null) {
            alarmMgr.set(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + 2 * 1000,
                    pendingIntent
            );
        }
    }

    class MoodDialogListener implements DialogInterface.OnClickListener {
        /**
         * Listeners for the click event when the user chooses the mood
         * that they're in and hits the submit button.
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            showMoodIntensityDialog();
        }
    }

    class MoodDialogChoiceListener implements DialogInterface.OnClickListener {
        /**
         * Listens for the event in which the user chooses a different mood
         * from the multiple choice menu.
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Offset the choice because it goes from 0-5
            mCurrentMood = which + 1;
        }
    }

    class MoodIntesityDialogListener implements DialogInterface.OnClickListener {
        /**
         * Listens for the event in which the user chooses a mood intensity
         * using the SeekBar then hits the submit button.
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            saveMoodToDatabase();
            Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_LONG).show();
        }
    }

    class MoodIntensityDialogSeekListener implements SeekBar.OnSeekBarChangeListener {
        /**
         * Listens for updates on the SeekBar used to get the user's current mood.
         * The data is stored which is then used to save to the local SQLite database.
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // Progress goes from 0-5 but we use 1-6
            mCurrentMoodIntensity = progress + 1;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
}