package com.example.expensify;

import android.os.Parcel;
import android.os.Parcelable;

public class Expense implements Parcelable {
    private String category;
    private double amount;
    private String note;
    private long timestamp;

    public Expense(String category, double amount, String note, long timestamp) {
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.timestamp = timestamp;
    }

    protected Expense(Parcel in) {
        category = in.readString();
        amount = in.readDouble();
        note = in.readString();
        timestamp = in.readLong();
    }

    public static final Creator<Expense> CREATOR = new Creator<Expense>() {
        @Override
        public Expense createFromParcel(Parcel in) {
            return new Expense(in);
        }

        @Override
        public Expense[] newArray(int size) {
            return new Expense[size];
        }
    };

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getNote() {
        return note;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(category);
        parcel.writeDouble(amount);
        parcel.writeString(note);
        parcel.writeLong(timestamp);
    }
}
