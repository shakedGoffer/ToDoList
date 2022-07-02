package com.example.todolist;

public class Task
{
    private String title, description, id;
    private String dueDate,dueTime;
    private int stat;


    public Task(String title, String description,int stat, String id, String dueDate, String dueTime)
    {
        this.title = title;
        this.description = description;
        this.id = id;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.stat = stat;
    }

    public Task(String title, String description,int stat, String dueDate, String dueTime)
    {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.stat = stat;
    }

    public String getDueDate() {return  this.dueDate;}
    public void setDueDate(String dueDate) {this.dueDate = dueDate;}

    public String getDueTime() {return  this.dueTime;}
    public void setDueTime(String dueTime) {this.dueTime = dueTime;}

    public String getTitle() {return this.title;}
    public void setTitle(String title) {this.title = title;}

    public String getDescription() {return this.description;}
    public void setDescription(String description) {this.description = description;}

    public int getStat() {return this.stat;}
    public void setStat(int stat) {this.stat = stat;}

    public String getId() {return this.id;}
    public void setId(String id) {this.id = id;}
}
