package com.dedalexey.criminalintent;

/**
 * Created by Alexey on 02.09.2017.
 */

public class Suspect {
    String mName;
    String mPhoneNumber;

    public Suspect(){}

    public Suspect(String name) {
        mName = name;
    }

    public Suspect(String name, String phoneNumber) {
        mName = name;
        mPhoneNumber = phoneNumber;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return mName + " tel." + mPhoneNumber;
    }
}
