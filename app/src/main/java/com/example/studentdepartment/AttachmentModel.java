package com.example.studentdepartment;

import android.net.Uri;

/**
 * Represents one attachment (an image or a link) that the user has picked
 * but not yet submitted -- used while building up a notice or a comment,
 * before it's saved.
 *
 * There are two constructors because the app creates these in two
 * different situations: sometimes just a type + text (e.g. a pasted link),
 * and sometimes with the actual picked file's Uri too (e.g. an image
 * chosen from the gallery, before it's uploaded).
 */
public class AttachmentModel {

    private String Type;
    private String Uri;
    private Uri name;

    public AttachmentModel() {
    }

    public AttachmentModel(String type, String uri) {
        this.Type = type;
        this.Uri = uri;
    }

    public AttachmentModel(String type, String uri, Uri name) {
        this.Type = type;
        this.Uri = uri;
        this.name = name;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        this.Type = type;
    }

    public String getUri() {
        return Uri;
    }

    public void setUri(String uri) {
        this.Uri = uri;
    }

    public Uri getName() {
        return name;
    }

    public void setName(Uri name) {
        this.name = name;
    }
}
