package com.dp.cecile.biomusic;

import java.util.Date;

/**
 * Created by cecilerobertm on 2017-03-03.
 */

public class SessionModel {

    private String ID;
    private String emotion;
    private String comment;
    private String date;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
