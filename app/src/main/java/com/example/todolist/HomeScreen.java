package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.util.ArrayList;
import java.util.Collections;

public class HomeScreen extends AppCompatActivity
{
    String UserName;
    ImageButton btnAdd_task;
    Task task;
    RecyclerView ryTasks;
    TasksAdapter tasksAdapter;

    public static ArrayList<Task> toDoList = new ArrayList<Task>();

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
            helpMy();
        }
        return true;
    }

    private void helpMy()
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
                    String taskDescription = "";
                    String taskTitle = dataSnapshot.child("title").getValue().toString();
                    String taskId = dataSnapshot.getKey();
                    int taskStat = Integer.valueOf(dataSnapshot.child("stat").getValue().toString());

                    if(dataSnapshot.child("description").exists())
                        taskDescription = dataSnapshot.child("description").getValue().toString();

                    task = new Task(taskTitle,taskStat,taskId);

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
                    task = new Task(etTitle.getText().toString(), etDescription.getText().toString(),1);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(UserName).child("Tasks");
                    reference.push().setValue(task);
                    task.setId(reference.getKey());
                    Toast.makeText(HomeScreen.this, "The task was add to your to do list", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                    toDoList.add(task);
                    tasksAdapter.notifyItemInserted(toDoList.size());

                }
            }
        });

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