package edu.tmeyer.ft_hangouts.database;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Contact implements Serializable {

    private int     id;
    private String  firstName;
    private String  lastName;
    private String  phone;
    private String  note;
    private byte[] picture;

    public Contact(int id, String firstName, String lastName, String phone, String note, byte[] picture) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.note = note;
        this.picture = picture;
    }

    public Contact(String firstName, String lastName, String phone, String note, byte[] picture) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.note = note;
        this.picture = picture;
    }

    public Contact() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    @NonNull
    @Override
    public String toString() {
        return this.firstName + " " + this.lastName;
    }
}
