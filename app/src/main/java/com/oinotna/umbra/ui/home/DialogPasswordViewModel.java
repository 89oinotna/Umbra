package com.oinotna.umbra.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DialogPasswordViewModel extends ViewModel {
    private MutableLiveData<String> password;

    public DialogPasswordViewModel() {
        password=new MutableLiveData<>();
    }

    public LiveData<String> getPassword(){
        return password;
    }


    public void setPassword(String password) {
        this.password.postValue(password);
    }
}
