package io.bitfountain.matthewparker.taskit;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by matthewparker on 12/12/14.
 */
public class Task implements Serializable{
    private String mName;
    private Date mDueDate;
    private boolean mDone;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Date getDueDate() {
        return mDueDate;
    }

    public void setDueDate(Date dueDate) {
        mDueDate = dueDate;
    }

    public boolean isDone() {
        return mDone;
    }

    public void setDone(boolean done) {
        mDone = done;
    }

    public String toString(){
        return mName;
    }
}
