package com.oinotna.umbra.ui.tutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

import com.oinotna.umbra.MainActivity;
import com.oinotna.umbra.R;

public class TutorialActivity extends AppCompatActivity {
    public static final int PAGES_NUM=2;
    private TutorialAdapter mAdapter;
    private ViewPager2 mViewPager;
    private Button btnNext;
    public static class TutorialAdapter extends FragmentStateAdapter {
        public TutorialAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        public TutorialAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        public TutorialAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch(position){
                case 0:
                    return new FirstTutorialFragment();
                case 1:
                    return new SecondTutorialFragment();
            }
            return null;
        }

        @Override
        public int getItemCount() {
            return PAGES_NUM;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tutorial);
        mAdapter=new TutorialAdapter(this);
        // hide the action bar
        getSupportActionBar().hide();
        mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);
        btnNext = (Button) findViewById(R.id.btn_next);
        btnNext.setOnClickListener((v) -> {
            int position = mViewPager.getCurrentItem();
            if (position < PAGES_NUM-1) {
                position++;
                mViewPager.setCurrentItem(position);
            }
            else{
                Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit().putBoolean("tutorial", true).apply();
                startActivity(mainActivity);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }
}