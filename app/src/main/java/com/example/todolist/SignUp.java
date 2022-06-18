package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class SignUp extends AppCompatActivity
{

    Button btnSignUp;
    EditText etFName,etLName,etEmailAddress,etPhone,etUserName,etPassword,etCheckPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {signUp();}});

        etFName = (EditText) findViewById(R.id.etFName);
        etLName = (EditText) findViewById(R.id.etLName);
        etEmailAddress = (EditText) findViewById(R.id.etEmailAddress);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etUserName = (EditText) findViewById(R.id.etUserName);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etCheckPassword = (EditText) findViewById(R.id.etCheckPassword);


        Clear();
    }

    public void Clear()
    {
        etFName.setText("");
        etLName.setText("");
        etEmailAddress.setText("");
        etPhone.setText("");
        etUserName.setText("");
        etPassword.setText("");
        etCheckPassword.setText("");
    }


    public void signUp()
    {
        //First name error
        if(etFName.getText().toString().equals(""))
            etFName.setError("Enter your first name");

            //Last name error
        else if(etLName.getText().toString().equals(""))
            etLName.setError("Enter your last name");

        //EmailAddress error
        else if(etEmailAddress.getText().toString().equals(""))
            etEmailAddress.setError("You mast enter an email address");
        else if(!isEmailValid(etEmailAddress.getText().toString()))
            etEmailAddress.setError("This email address doesn't exist");

            //phone number error
        else if(etPhone.getText().toString().length()<10 )
            etPhone.setError("Incorrect phone number, please try again");

            //User name error
        else if(etUserName.getText().toString().length()<6)
            etUserName.setError("User name must contain at least 6 characters");

            //Password error
        else if(etPassword.getText().toString().length()<6)
        {
            etPassword.setError("Password must contain at least 6 characters");
            etPassword.setText("");
            etCheckPassword.setText("");
        }
        //CheckPassword error
        else if(!etPassword.getText().toString().equals(etCheckPassword.getText().toString()))
        {
            etCheckPassword.setError("The passwords doesn't mach");
            etCheckPassword.setText("");
        }
        else
        {
            PotInFirebase();
        }
    }

    boolean isEmailValid(CharSequence email)
    {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void PotInFirebase()
    {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
        reference1.child(etUserName.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (task.getResult().exists() ||
                            String.valueOf(dataSnapshot.child("userName").getValue()) == etUserName.getText().toString() ||
                            String.valueOf(dataSnapshot.child("userName").getValue()).equals(etUserName.getText().toString()))
                    {
                        //user name already exist
                        etUserName.setError("This user name already exist");
                    }
                    else
                    {

                        User u = new User(etFName.getText().toString() + " " + etLName.getText().toString(),
                                etEmailAddress.getText().toString(), etPhone.getText().toString(), etUserName.getText().toString(),
                                etPassword.getText().toString());
                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                        DatabaseReference reference = db.getReference("Users");
                        reference.child(etUserName.getText().toString()).setValue(u).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                            }
                        });
                        Intent HomeScreen = new Intent(SignUp.this, HomeScreen.class);
                        HomeScreen.putExtra("UserName",etUserName.getText().toString());
                        startActivity(HomeScreen);
                    }
                }
            }
        });
    }

    // Back pressed
    int i=0;
    public void onBackPressed()
    {
        i++;
        if(i==2)
        {
            super.onBackPressed();
            Intent BackPressed = new Intent(SignUp.this, LogIn.class);
            startActivity(BackPressed);
            i=0;
        }
    }
}