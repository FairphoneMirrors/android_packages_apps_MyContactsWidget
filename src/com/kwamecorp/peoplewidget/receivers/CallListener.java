package com.kwamecorp.peoplewidget.receivers;

public interface CallListener {

    public void onOutgoingCall(String number);
    public void onOutgoingSMS(String number);
}
