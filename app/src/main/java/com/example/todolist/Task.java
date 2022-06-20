package com.example.todolist;

public class Task
{
    private String title, description, id;
    private int stat;

    public Task(String title, String description,int stat)
    {
        this.title = title;
        this.description = description;
        this.stat = stat;
    }

    public Task(String title, String description,int stat,String id)
    {
        this.title = title;
        this.description = description;
        this.stat = stat;
        this.id = id;
    }


    public String getTitle() {return this.title;}
    public void setTitle(String title) {this.title = title;}

    public String getDescription() {return this.description;}
    public void setDescription(String description) {this.description = description;}

    public int getStat() {return this.stat;}
    public void setStat(int stat) {this.stat = stat;}

    public String getId() {return this.id;}
    public void setId(String id) {this.id = id;}
}
