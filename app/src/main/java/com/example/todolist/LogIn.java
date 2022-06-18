package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LogIn extends AppCompatActivity implements View.OnClickListener {

    static DatabaseReference reference;
    Button btnLogIn;
    EditText etUserName,etPassword;
    TextView tvSignUp ;
    Context context;
    CheckBox cb_RememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        btnLogIn =(Button)findViewById(R.id.btnLogIn);
        btnLogIn.setOnClickListener(this);

        etUserName=(EditText)findViewById(R.id.etUserName);
        etPassword=(EditText)findViewById(R.id.etPassword);
        tvSignUp= (TextView) findViewById(R.id.tvSignup);
        tvSignUp.setClickable(true);
        tvSignUp.setOnClickListener(this);


        cb_RememberMe = (CheckBox)findViewById(R.id.cb_RememberMe);

        context = LogIn.this;

        check_rememberMe();
        clear();
    }

    // Clear edit text UserName text and edit text Password text
    public void clear()
    {
        etUserName.setText("");
        etPassword.setText("");
    }

    public void check_rememberMe()
    {
        SharedPreferences preferences = getSharedPreferences("CheckBox", MODE_PRIVATE);
        String Check = preferences.getString("remember","");
        if(Check.equals("true"))
        {
            SharedPreferences preferences_UserName = getSharedPreferences("CheckBox", MODE_PRIVATE);
            String UserName = preferences.getString("UserName","");

            Intent HomeScreen = new Intent(LogIn.this, HomeScreen.class);
            HomeScreen.putExtra("UserName",UserName);
            startActivity(HomeScreen);


        }
    }

    @Override
    public void onClick(View v)
    {
        if(v==tvSignUp)
        {
            Intent SignUp = new Intent(LogIn.this, SignUp.class);
            startActivity(SignUp);
        }
        if(btnLogIn==v)
        {
            if(etUserName.getText().toString().isEmpty())
                etUserName.setError("please enter your user name");
            else if(etPassword.getText().toString().isEmpty())
                etPassword.setError("please enter your password");
            else
                logIn(etUserName.getText().toString(), etPassword.getText().toString());
        }

    }


    public void logIn(String UserName, String Password)
    {
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(UserName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task)
            {

                if (task.isSuccessful())
                {

                    if (task.getResult().exists())
                    {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (String.valueOf(dataSnapshot.child("password").getValue()) == Password ||String.valueOf(dataSnapshot.child("password").getValue()).equals(Password))
                        {
                            if(cb_RememberMe.isChecked())
                            {

                                SharedPreferences preferences = getSharedPreferences("CheckBox", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("remember", "true");
                                editor.putString("UserName", etUserName.getText().toString());
                                editor.apply();
                                editor.commit();
                            }


                            Intent HomeScreen = new Intent(LogIn.this, HomeScreen.class);
                            HomeScreen.putExtra("UserName",UserName);
                            startActivity(HomeScreen);

                        }
                        else // wrong password
                        {
                            etPassword.setError("wrong password");
                            //Toast.makeText(LogIn.this,"Error, try again",Toast.LENGTH_SHORT).show();
                            etPassword.setText("");
                            //tvError.setVisibility(View.VISIBLE);
                        }
                    }
                    else // User Doesn't Exist
                    {
                        Toast.makeText(LogIn.this, "User Doesn't Exist", Toast.LENGTH_SHORT).show();
                        clear();
                    }
                }
                else // Error
                    Toast.makeText(LogIn.this, "Failed to read", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Back presse
    int BackPressed=0;
    @Override
    public void onBackPressed()
    {
        BackPressed++;
        if(BackPressed==2)
        {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            BackPressed=0;
        }
    }


}