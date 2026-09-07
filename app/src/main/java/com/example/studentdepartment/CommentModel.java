package com.example.studentdepartment;

import java.util.ArrayList;

/**
 * Holds the data for one question/comment thread -- who asked it, the
 * question text, whether it's been answered yet, and any images/links
 * attached to it.
 *
 * Field names here (Question, Name, Event...) start with a capital letter
 * because that's how they were already saved in the original data --
 * kept as-is so existing data still loads correctly.
 */
public class CommentModel {

    private String key;
    private Boolean answered;
    private String Question;
    private String Name;
    private String Event;
    private ArrayList<String> ImageList = new ArrayList<>();
    private ArrayList<String> LinkList = new ArrayList<>();

    public CommentModel() {
        // Required empty constructor for Firebase deserialization.
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Boolean getAnswered() {
        return answered;
    }

    public void setAnswered(Boolean answered) {
        this.answered = answered;
    }

    public String getQuestion() {
        return Question;
    }

    public void setQuestion(String question) {
        this.Question = question;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getEvent() {
        return Event;
    }

    public void setEvent(String event) {
        this.Event = event;
    }

    public ArrayList<String> getImageList() {
        return ImageList;
    }

    public void setImageList(ArrayList<String> imageList) {
        this.ImageList = imageList;
    }

    public ArrayList<String> getLinkList() {
        return LinkList;
    }

    public void setLinkList(ArrayList<String> linkList) {
        this.LinkList = linkList;
    }
}
