package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.todolist.Adapters.TasksAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class HomeScreen extends AppCompatActivity {
    String UserName;
    ImageButton btnAdd_task;
    Task task;
    RecyclerView ryTasks;
    TasksAdapter tasksAdapter;
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;

    public static ArrayList<Task> toDoList = new ArrayList<Task>();

    // deu date and time
    String dueDate ,dueTime;

    //Firebase
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        UserName = getIntent().getStringExtra("UserName");

        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.setTitle(UserName);
        setSupportActionBar(toolbar);

        ryTasks = (RecyclerView) findViewById(R.id.ryTasks);
        ryTasks.setLayoutManager(new LinearLayoutManager(this));



        btnAdd_task=(ImageButton)findViewById(R.id.btnAdd_task);
        btnAdd_task.setClickable(true);
        btnAdd_task.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                newTask();
                dueDate = "non";
                dueTime = "non";
            }
        });

        setUpData();

    }


    // menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return  true;
    }
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.log_out)
        {
            SharedPreferences preferences = getSharedPreferences("CheckBox", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("remember", "false");
            editor.apply();
            editor.commit();

            Intent MainActivity = new Intent(HomeScreen.this, LogIn.class);
            startActivity(MainActivity);
            return true;
        }
        else if(id == R.id.change_password)
        {
            Intent change_password = new Intent(HomeScreen.this,ChangePassword.class);
            change_password.putExtra("UserName",getIntent().getStringExtra("UserName"));
            startActivity(change_password);
            return true;

        }
        else if(id == R.id.help)
        {
            helpMe();
        }
        return true;
    }

    private void helpMe()
    {
        Dialog dialog = new Dialog(HomeScreen.this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.help);
        dialog.show();

        EditText etHelp = dialog.findViewById(R.id.etHelp);

        Button btnDone = dialog.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(etHelp.getText().toString().isEmpty())
                    etHelp.setError("Pleas enter your complain/request so we can help you");
                else
                {

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Request help").child(UserName);
                    reference.push().setValue(etHelp.getText().toString());

                    Toast.makeText(HomeScreen.this,
                            "Your question / complain is being checked, we will get back to you soon",Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        });
    }


    // Set to do list
    private void setUpData()
    {
        toDoList.clear();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(UserName);
        reference.child("Tasks").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot)
            {
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Task task;
                    String taskTitle = dataSnapshot.child("title").getValue().toString();
                    String taskId = dataSnapshot.getKey();
                    int taskStat = Integer.valueOf(dataSnapshot.child("stat").getValue().toString());
                    String taskDescription = dataSnapshot.child("description").getValue().toString();
                    String taskDueDate = dataSnapshot.child("dueDate").getValue().toString();
                    String taskDueTime = dataSnapshot.child("dueTime").getValue().toString();

                    task = new Task(taskTitle,taskDescription,taskStat,taskId,taskDueDate,taskDueTime);

                    toDoList.add(task);
                }
                setUpList();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                SharedPreferences preferences = getSharedPreferences("CheckBox", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("remember", "false");
                editor.apply();
                editor.commit();

                Intent MainActivity = new Intent(HomeScreen.this, LogIn.class);
                startActivity(MainActivity);
            }
        });

    }

    private void setUpList()
    {
        tasksAdapter = new TasksAdapter(HomeScreen.this,toDoList,UserName);
        ryTasks.setAdapter(tasksAdapter);

        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new RecyclerItemTouch(tasksAdapter,HomeScreen.this,ryTasks));
        itemTouchHelper.attachToRecyclerView(ryTasks);

    }

    private void  newTask()
    {
        Dialog dialog = new Dialog(HomeScreen.this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.new_task);
        dialog.show();

        EditText etTitle = dialog.findViewById(R.id.etTitle);
        EditText etDescription = dialog.findViewById(R.id.etDescription);
        TextView  tvDate = dialog.findViewById(R.id.tvDate);
        LinearLayout ly_DueDate = dialog.findViewById(R.id.ly_DueDate);
        ly_DueDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            { DatePicker(tvDate); }});

        Button btnAdd = dialog.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(etTitle.getText().toString().isEmpty())
                    etTitle.setError("Pleas enter a title for your task");
                else
                {
                    task = new Task(etTitle.getText().toString(), etDescription.getText().toString(),1,dueDate,dueTime);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(UserName).child("Tasks");
                    reference.push().setValue(task);
                    task.setId(reference.getKey());
                    Toast.makeText(HomeScreen.this, "The task was add to your to do list", Toast.LENGTH_SHORT).show();
                    Notification(task);
                    dialog.dismiss();
                    toDoList.add(task);
                    tasksAdapter.notifyItemInserted(toDoList.size());



                    if(!task.getDueDate().equals("non") && !task.getDueTime().equals("non"))
                    {
                        SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a");
                        SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yy");
                        Calendar cal = null;
                        try
                        {
                            cal = Calendar.getInstance();
                            cal.setTime(sdfTime.parse(task.getDueTime()));
                            cal.setTime(sdfDate.parse(task.getDueDate()));
                        }
                        catch (ParseException e)
                        {
                            e.printStackTrace();
                        }

                       // startAlarm(cal);
                    }


                }
            }
        });

    }

    private void DatePicker(TextView  tvDate)
    {
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int date) {

                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(Calendar.YEAR, year);
                calendar1.set(Calendar.MONTH, month);
                calendar1.set(Calendar.DATE, date);

                Calendar now = Calendar.getInstance();
                final String FormatString = "EEEE, MMM d, yyyy";

                if(now.get(Calendar.YEAR) - calendar1.get(Calendar.YEAR) > 0 || now.get(Calendar.MONTH) - calendar1.get(Calendar.MONTH) > 0)
                    tvDate.setText("Pleas enter a reasonable date");
                else if(now.get(Calendar.YEAR) == calendar1.get(Calendar.YEAR) && now.get(Calendar.MONTH) == calendar1.get(Calendar.MONTH))
                {
                    if(now.get(Calendar.DATE) - calendar1.get(Calendar.DATE) > 0 )
                        tvDate.setText("Pleas enter a reasonable date");
                    else if(now.get(Calendar.DATE) == calendar1.get(Calendar.DATE))
                    {
                        tvDate.setText("Today");
                        dueDate = DateFormat.format("MM/dd/yy", calendar1).toString();
                        timePicker(tvDate,calendar1);
                    }
                    else if(now.get(Calendar.DATE) - calendar1.get(Calendar.DATE) == -1 )
                    {
                        tvDate.setText("Tomorrow");
                        dueDate = DateFormat.format("MM/dd/yy", calendar1).toString();
                        timePicker(tvDate,calendar1);
                    }
                    else
                    {
                        tvDate.setText(DateFormat.format(FormatString, calendar1).toString());
                        dueDate = DateFormat.format("MM/dd/yy", calendar1).toString();
                        timePicker(tvDate,calendar1);
                    }
                }
                else
                {
                    tvDate.setText(DateFormat.format(FormatString, calendar1).toString());
                    dueDate = DateFormat.format("MM/dd/yy", calendar1).toString();
                    timePicker(tvDate,calendar1);
                }

            }
        }, YEAR, MONTH, DATE);

        datePickerDialog.show();
    }

    private void timePicker(TextView  tvDate,Calendar calendarDate)
    {
        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR);
        int MINUTE = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                Log.i("HomeScreen", "onTimeSet: " + hour + minute);

                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(Calendar.HOUR, hour);
                calendar1.set(Calendar.MINUTE, minute);
                String dateText = DateFormat.format("h:mm a", calendar1).toString();

                calendarDate.set(Calendar.HOUR,hour);
                calendarDate.set(Calendar.MINUTE,minute);

                String date = tvDate.getText().toString();
                Calendar now = Calendar.getInstance();

                Date date1 = calendar1.getTime();
                Date date2 = now.getTime();


                long millis = date1.getTime() - date2.getTime();
                int hours = (int) (millis / (1000 * 60 * 60));
                int mins = (int) ((millis / (1000 * 60)) % 60);


                if(now.get(Calendar.MONTH) == calendarDate.get(Calendar.MONTH) && now.get(Calendar.DATE) == calendarDate.get(Calendar.DATE) && now.get(Calendar.YEAR) == calendarDate.get(Calendar.YEAR) )
                {
                    // Late --> not possible
                    if(date1.getTime() - date2.getTime()<0.3 && (hours != -now.get(Calendar.HOUR)  && hours <-1 ))
                        tvDate.setText("Pleas enter a reasonable time and date");
                    else if(hours < 0 &&  hours != -now.get(Calendar.HOUR) && (now.get(Calendar.AM_PM) == calendar1.get(Calendar.AM_PM)))
                        tvDate.setText("Pleas enter a reasonable time and date");

                        // At list 30 mints until the task due ends
                    else if(hours==0 && mins < 30)
                        tvDate.setText("Pleas enter at least 30 minutes difference");
                    else if(hours==1 && mins < -30)
                        tvDate.setText("Pleas enter at least 30 minutes difference");
                    else
                    {
                        tvDate.setText(date+", "+dateText);
                        dueTime = DateFormat.format("h:mm a", calendar1).toString();

                    }

                }
                else
                {
                    tvDate.setText(date+", "+dateText);
                    dueTime = DateFormat.format("h:mm a", calendar1).toString();

                }

                /*
                if(now.get(Calendar.MONTH) == calendarDate.get(Calendar.MONTH) && now.get(Calendar.DATE) == calendarDate.get(Calendar.DATE) && now.get(Calendar.YEAR) == calendarDate.get(Calendar.YEAR) )
                {
                    // Late --> not possible
                    if(now.get(Calendar.HOUR) > calendar1.get(Calendar.HOUR))
                        tvDate.setText("Pleas enter a reasonable time and date");
                    else if(now.get(Calendar.HOUR) == calendar1.get(Calendar.HOUR) &&  now.get(Calendar.MINUTE) - calendar1.get(Calendar.MINUTE) > 0)
                        tvDate.setText("Pleas enter a reasonable time and date");

                    // At list 30 mints until the task due ends
                    else if(now.get(Calendar.HOUR) == calendar1.get(Calendar.HOUR) &&  now.get(Calendar.MINUTE) - calendar1.get(Calendar.MINUTE) > -30)
                        tvDate.setText("Pleas enter at least 30 minutes difference");
                    else if(now.get(Calendar.HOUR)+1 == calendar1.get(Calendar.HOUR)
                            &&  now.get(Calendar.MINUTE) - calendar1.get(Calendar.MINUTE) > 30)
                    {
                        tvDate.setText("Pleas enter at least 30 minutes difference");
                    }
                    else
                    {
                        tvDate.setText(date+", "+dateText);
                        dueTime = DateFormat.format("h:mm a", calendar1).toString();
                    }

                }
                else
                {
                    tvDate.setText(date+", "+dateText);
                    dueTime = DateFormat.format("h:mm a", calendar1).toString();
                }

                 */
            }
        }, HOUR, MINUTE, false);

        timePickerDialog.show();
    }


    private void startAlarm(Calendar c)
    {
        Toast.makeText(HomeScreen.this,"1",Toast.LENGTH_LONG).show();


        /*
        if (c.before(Calendar.getInstance()))
        {
            c.add(Calendar.DATE, 1);
        }


        PendingIntent pi = PendingIntent.getService(HomeScreen.this, 0 , new Intent(HomeScreen.this, AlertReceiver.class),PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) HomeScreen.this.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);



        Intent notifyIntent = new Intent(HomeScreen.this,AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (HomeScreen.this, 1 , notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) HomeScreen.this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis(),
                1000 * 60 * 60 * 24, pendingIntent);

        Toast.makeText(HomeScreen.this,"2",Toast.LENGTH_LONG).show();
        */


        ////

        Intent myIntent = new Intent(getApplicationContext() , AlertService. class ) ;
        AlarmManager alarmManager = (AlarmManager) getSystemService( ALARM_SERVICE ) ;
        PendingIntent pendingIntent = PendingIntent. getService ( this, 0 , myIntent , 0 ) ;



        if (c.before(Calendar.getInstance()))
        {
            c.add(Calendar. DAY_OF_MONTH , 1 ) ;
        }

        alarmManager.setRepeating(AlarmManager. RTC_WAKEUP , c.getTimeInMillis() , 1000 * 60 * 60 * 24 , pendingIntent) ;



    }

    private void cancelAlarm()
    {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        alarmManager.cancel(pendingIntent);
        Toast.makeText(HomeScreen.this, "Alarm canceled", Toast.LENGTH_SHORT).show();
    }



    private void Notification(Task task)
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
                    @Override
                    public void run(){

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        {
                            NotificationChannel channel = new NotificationChannel
                                    ("Task reminder","Task reminder",NotificationManager.IMPORTANCE_DEFAULT);
                            NotificationManager manager = getSystemService(NotificationManager.class);
                            manager.createNotificationChannel(channel);
                        }
                        NotificationCompat.Builder builder = new NotificationCompat.Builder( HomeScreen.this, "Task reminder" ) ;
                        builder.setContentTitle( "Task reminder" ) ;
                        builder.setContentText("Your task: " + task.getTitle()+" due date has passed");
                        builder.setSmallIcon(R.drawable. ic_time ) ;
                        builder.setAutoCancel( true ) ;

                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(1, builder.build());
                    }

                }, 50000);

    }





    //Back press
    int BackP=0;
    @Override
    public void onBackPressed()
    {
        BackP++;
        if(BackP==2)
        {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            BackP=0;
        }

    }
}