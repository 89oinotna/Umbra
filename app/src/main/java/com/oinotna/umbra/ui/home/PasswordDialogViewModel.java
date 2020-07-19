package com.oinotna.umbra.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PasswordDialogViewModel extends ViewModel {
    private MutableLiveData<String> password;

    public PasswordDialogViewModel(){
    }

    public LiveData<String> getPassword(){
        password=new MutableLiveData<>();
        return password;
    }

    public void setPassword(String password) {
        this.password.postValue(password);
    }
}
