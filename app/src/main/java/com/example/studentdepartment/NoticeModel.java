package com.example.studentdepartment;

/**
 * Holds the data for one notice posted by a Department user.
 *
 * The empty constructor is required by Firebase's database library, which
 * uses it to rebuild objects from stored data (even though this app isn't
 * connected to Firebase yet, the model is already written to be compatible
 * with it).
 */
public class NoticeModel {

    private String title;
    private String image;
    private String description;
    private String dateofnotice;

    public NoticeModel() {
        // Required empty constructor for Firebase deserialization.
    }

    public NoticeModel(String title, String image, String description, String dateofnotice) {
        this.title = title;
        this.image = image;
        this.description = description;
        this.dateofnotice = dateofnotice;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDateofnotice() {
        return dateofnotice;
    }

    public void setDateofnotice(String dateofnotice) {
        this.dateofnotice = dateofnotice;
    }
}
