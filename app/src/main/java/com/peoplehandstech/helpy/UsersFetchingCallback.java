package com.peoplehandstech.helpy;

public interface UsersFetchingCallback {
    void onLocationReady(String country);
    void onLocationNull();
    void onLocationError();
    void onUsersSet();
}
