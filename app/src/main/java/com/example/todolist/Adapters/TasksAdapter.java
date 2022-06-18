package com.example.todolist.Adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.HomeScreen;
import com.example.todolist.R;
import com.example.todolist.RecyclerItemTouch;
import com.example.todolist.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ViewHolder>
{

    private List<Task> tasksList;
    private Context context;
    private String UserName;

    public List<Task> getTasksList()
    {
        return this.tasksList;
    }

    public String getUserName()
    {
        return this.UserName;
    }

    public TasksAdapter(Context context, List<Task> data, String UserName)
    {
        this.tasksList = data;
        this.context = context;
        this.UserName = UserName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_cell, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, @SuppressLint("RecyclerView") int position) {
       // db.openDatabase();
        final Task item = tasksList.get(position);
        String taskId = item.getId();
        holder.task.setText(item.getTitle());
        if(item.getStat()==1)
            holder.task.setChecked(false);
        else
            holder.task.setChecked(true);
        holder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                            .child(UserName).child("Tasks");
                    reference.child(taskId).child("stat").setValue(0);
                }
                else
                {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                            .child(UserName).child("Tasks");
                    reference.child(taskId).child("stat").setValue(1);
                }
            }
        });
        holder.task.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                editTask(item,position);
                return false;
            }
        });
    }


    @Override
    public int getItemCount()
    {
        return tasksList.size();
    }

    public void setTasks(List<Task> toDoList)
    {
        this.tasksList = toDoList;
        notifyDataSetChanged();
    }

    public void deleteItem(int position)
    {
        Task item = tasksList.get(position);
        String taskId = item.getId();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(UserName).child("Tasks");
        reference.child(taskId).removeValue();
        tasksList.remove(position);
        notifyItemRemoved(position);
    }


    private void editTask(Task task,int position)
    {

        Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.new_task);
        dialog.show();

        TextView tv_newTask = dialog.findViewById(R.id.tv_newTask);
        tv_newTask.setText("EDIT TASK");

        EditText etTitle = dialog.findViewById(R.id.etTitle);
        EditText etDescription = dialog.findViewById(R.id.etDescription);

        etTitle.setText(task.getTitle());
        etDescription.setText(task.getDescription());


        Button btnAdd = dialog.findViewById(R.id.btnAdd);
        btnAdd.setText("Done");
        btnAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(etTitle.getText().toString().isEmpty())
                    etTitle.setError("Pleas enter a title for your task");
                else
                {
                    task.setTitle(etTitle.getText().toString());
                    task.setDescription(etDescription.getText().toString());
                    task.setStat(1);
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(UserName)
                            .child("Tasks");
                    reference.child(task.getId()).setValue(task);
                    //Toast.makeText(context, "The task was add to your to do list", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                    notifyItemChanged(position);
                }
            }
        });

    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;

        ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.todoCheckBox);
        }
    }
}
