package com.example.todolist.Adapters;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        final Task item = tasksList.get(position);
        String taskId = item.getId();
        holder.task.setText(item.getTitle());

        //due date and time
        if( item.getDueDate() == "non")
            holder.dueDate.setText("You didn't select a due date");
        DueDate( item,holder.dueDate);
        //notifyItemChanged(position);


        // stat
        if(item.getStat()==1)
            holder.task.setChecked(false);
        else
            holder.task.setChecked(true);

        holder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
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

    private void DueDate(Task task,TextView dueDate)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
        Calendar cal = null;
        try
        {
            cal = Calendar.getInstance();
            cal.setTime(sdf.parse(task.getDueDate()));
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar now = Calendar.getInstance();

        if( task.getDueDate() == "non")
            return;

        if(now.get(Calendar.YEAR) - cal.get(Calendar.YEAR) > 0 || now.get(Calendar.MONTH) - cal.get(Calendar.MONTH) > 0)
        {
            dueDate.setTextColor(Color.RED);
            dueDate.setText("Due: " + task.getDueDate());
        }
        else if(now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) && now.get(Calendar.MONTH) == cal.get(Calendar.MONTH))
        {
            if(now.get(Calendar.DATE) - cal.get(Calendar.DATE) == 1 )
            {
                dueDate.setTextColor(Color.RED);
                dueDate.setText("Due: Yesterday");
                return;
            }
            else if(now.get(Calendar.DATE) - cal.get(Calendar.DATE) > 0 )
            {
                dueDate.setTextColor(Color.RED);
                dueDate.setText("Due: " + task.getDueDate());
                return;
            }
            else if(now.get(Calendar.DATE) == cal.get(Calendar.DATE))
                dueDate.setText("Due: Today");
            else if(now.get(Calendar.DATE) - cal.get(Calendar.DATE) == -1 )
                dueDate.setText("Due: Tomorrow");
            else
                dueDate.setText("Due: " + task.getDueDate());

            dueDate.setTextColor(0x8B000000);
        }
        else
        {
            dueDate.setText("Due: " + task.getDueDate());
            dueDate.setTextColor(0x8B000000);
        }




        if(!task.getDueTime().equals("non"))
            DueTime(task,dueDate,cal);

    }

    private void DueTime(Task task,TextView dueDate,Calendar calendarDate)
    {

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        Calendar cal = null;
        try
        {
            cal = Calendar.getInstance();
            cal.setTime(sdf.parse(task.getDueTime()));
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        Calendar now = Calendar.getInstance();
        if(now.get(Calendar.MONTH) == calendarDate.get(Calendar.MONTH) && now.get(Calendar.DATE) == calendarDate.get(Calendar.DATE) && now.get(Calendar.YEAR) == calendarDate.get(Calendar.YEAR) )
        {
            if(now.get(Calendar.HOUR) > cal.get(Calendar.HOUR))
            {
                dueDate.setTextColor(Color.RED);
                dueDate.setText(dueDate.getText().toString()+", "+ task.getDueTime());
            }
            else if(now.get(Calendar.HOUR) == cal.get(Calendar.HOUR) &&  now.get(Calendar.MINUTE) - cal.get(Calendar.MINUTE) >= 0)
            {
                dueDate.setTextColor(Color.RED);
                dueDate.setText(dueDate.getText().toString()+", "+ task.getDueTime());
            }
            else
                dueDate.setText(dueDate.getText().toString()+", "+ task.getDueTime());
        }
        else
            dueDate.setText(dueDate.getText().toString()+", "+ task.getDueTime());
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
        TextView  tvDate = dialog.findViewById(R.id.tvDate);
        DueDate(task,tvDate);
        LinearLayout ly_DueDate = dialog.findViewById(R.id.ly_DueDate);
        ly_DueDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            { DatePicker(tvDate,task); }});


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

    private void DatePicker(TextView tvDate,Task task)
    {
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);
        task.setDueDate("non");
        task.setDueTime("non");

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
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
                        task.setDueDate(DateFormat.format("MM/dd/yy", calendar1).toString());
                        timePicker(tvDate,calendar1,task);
                    }
                    else if(now.get(Calendar.DATE) - calendar1.get(Calendar.DATE) == -1 )
                    {
                        tvDate.setText("Tomorrow");
                        task.setDueDate(DateFormat.format("MM/dd/yy", calendar1).toString());
                        timePicker(tvDate,calendar1,task);
                    }
                    else
                    {
                        tvDate.setText(DateFormat.format(FormatString, calendar1).toString());
                        task.setDueDate(DateFormat.format("MM/dd/yy", calendar1).toString());
                        timePicker(tvDate,calendar1,task);
                    }
                }
                else
                {
                    tvDate.setText(DateFormat.format(FormatString, calendar1).toString());
                    task.setDueDate(DateFormat.format("MM/dd/yy", calendar1).toString());
                    timePicker(tvDate,calendar1,task);
                }

            }
        }, YEAR, MONTH, DATE);

        datePickerDialog.show();
    }

    private void timePicker(TextView  tvDate,Calendar calendarDate,Task task)
    {
        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR);
        int MINUTE = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                Log.i("HomeScreen", "onTimeSet: " + hour + minute);


                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(Calendar.HOUR, hour);
                calendar1.set(Calendar.MINUTE, minute);
                String dateText = DateFormat.format("h:mm a", calendar1).toString();

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
                    if(hours > 0 && (now.get(Calendar.AM_PM) == calendar1.get(Calendar.AM_PM)))
                    {
                        tvDate.setText("Pleas enter a reasonable time and date");
                    }

                    // At list 30 mints until the task due ends
                    else if(hours==0 && mins < 30)
                    {
                        tvDate.setText("Pleas enter at least 30 minutes difference");
                    }
                    else if(hours==1 && mins < -30)
                        tvDate.setText("Pleas enter at least 30 minutes difference");
                    else
                    {
                        tvDate.setText(date+", "+dateText);
                        task.setDueTime(DateFormat.format("h:mm a", calendar1).toString());
                    }

                    tvDate.setTextColor(0x8B000000);
                }
                else
                {
                    tvDate.setText(date+", "+dateText);
                    task.setDueTime(DateFormat.format("h:mm a", calendar1).toString());
                }
            }
        }, HOUR, MINUTE, true);

        timePickerDialog.show();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        CheckBox task;
        TextView dueDate ;

        ViewHolder(View view)
        {
            super(view);
            task = view.findViewById(R.id.todoCheckBox);
            dueDate = view.findViewById(R.id.tv_DueDate);

        }
    }
}
