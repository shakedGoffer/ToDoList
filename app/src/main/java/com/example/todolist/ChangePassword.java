package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangePassword extends AppCompatActivity implements View.OnClickListener {

    String UserName;
    static DatabaseReference reference;
    Button btnDone;
    EditText etPassword, etNewPassword, etCheckNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        UserName = getIntent().getStringExtra("UserName");

        btnDone = (Button) findViewById(R.id.btnDone);
        btnDone.setOnClickListener(this);

        etPassword = (EditText) findViewById(R.id.etPassword);
        etNewPassword = (EditText) findViewById(R.id.etNewPassword);
        etCheckNewPassword = (EditText) findViewById(R.id.etCheckNewPassword);

        Clear();
    }

    public void Clear() {
        etNewPassword.setText("");
        etPassword.setText("");
        etCheckNewPassword.setText("");
    }


    @Override
    public void onClick(View v) {
        if (v == btnDone)
            getPassword(UserName, etPassword.getText().toString());
    }

    public void getPassword(String UserName, String Password)
    {
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(UserName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<DataSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (String.valueOf(dataSnapshot.child("password").getValue()) == Password || String.valueOf(dataSnapshot.child("password").getValue()).equals(Password)) {

                            if(etNewPassword.getText().toString().length()<6)
                            {
                                etNewPassword.setError("Password must be at least 6 characters long");
                                etCheckNewPassword.setError("Password must be at least 6 characters long");
                            }
                            else if (etNewPassword.getText().toString().equals(etCheckNewPassword.getText().toString()) == false)
                            {
                                etCheckNewPassword.setError("Error, try again");
                                etCheckNewPassword.setText("");
                            }
                            else if(etNewPassword.getText().toString().equals(etPassword.getText().toString()))
                            {
                                etNewPassword.setError("The new password can not be same");
                                etCheckNewPassword.setError("The new password can not be same");
                                etCheckNewPassword.setText("");
                                etNewPassword.setText("");
                            }
                            else
                                setNewPassword();

                        } else // wrong password
                        {
                            etPassword.setError("Wrong password ,try again");
                        }
                    } else // User Doesn't Exist
                    {
                        Toast.makeText(ChangePassword.this, "User Doesn't Exist", Toast.LENGTH_SHORT).show();
                        Clear();
                    }
                } else // Error
                    Toast.makeText(ChangePassword.this, "Failed to read", Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void setNewPassword()
    {
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(UserName).child("password").setValue(etNewPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                Intent HomeScreen = new Intent(ChangePassword.this, HomeScreen.class);
                HomeScreen.putExtra("UserName",UserName);
                startActivity(HomeScreen);

                SharedPreferences preferences = getSharedPreferences("CheckBox", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("remember", "false");
                editor.apply();
                editor.commit();
            }
        });
    }

}