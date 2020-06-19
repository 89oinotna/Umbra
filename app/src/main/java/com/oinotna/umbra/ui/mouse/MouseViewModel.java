package com.oinotna.umbra.ui.mouse;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MouseViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MouseViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is mouse fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}