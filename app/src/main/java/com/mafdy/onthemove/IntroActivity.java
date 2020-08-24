package com.mafdy.onthemove;

import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        addSlide(AppIntroFragment.newInstance("Hi there","",
                "Welcome to On the move app\n an app that can help you while being on the move and want to share a simple status with your friends that includes your location, your activity ( walking, running, in a car ..etc.) and optionally your destination",
                "",
                R.drawable.screen1,getResources().getColor(R.color.colorPrimaryDark),
                getResources().getColor(R.color.colorAccent),getResources().getColor(android.R.color.white)));
        addSlide(AppIntroFragment.newInstance("Main Activity","",
                "From there you can see your latest Status ( location and activity ), you can slide up to see a destination you have already set using the search bar above OR you can use the share button to open the share activity",
                "",
                R.drawable.screen2,getResources().getColor(R.color.colorPrimaryDark),
                getResources().getColor(R.color.colorAccent),getResources().getColor(android.R.color.white)));
        addSlide(AppIntroFragment.newInstance("Share page","",
                "tapping on the share button opens the share page which has 2 functions: 1. share your latest status by tapping Share Current OR\n 2. select From and To dates to view the history stored on your device to view them on the map and share them using the share button.",
                "",
                R.drawable.screen3,getResources().getColor(R.color.colorPrimaryDark),
                getResources().getColor(R.color.colorAccent),getResources().getColor(android.R.color.white)));
        addSlide(AppIntroFragment.newInstance("Finale","",
                "Please be advised that the app continues to work in the background and you can stop the app using the notification section, from there you can share your latest status as well.\n the app provides a widget for displaying and sharing your latest status. Have fun using the app and don't hesitate to provide your feedback",
                "",
                R.drawable.screen4,getResources().getColor(R.color.colorPrimaryDark),
                getResources().getColor(R.color.colorAccent),getResources().getColor(android.R.color.white)));


        setBarColor(Color.parseColor("#3F51B5"));
        setNavBarColor(R.color.colorAccent);
        setSeparatorColor(Color.parseColor("#2196F3"));


        showSkipButton(true);
        showDoneButton(true);
        setProgressButtonEnabled(true);

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);

        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
        finish();

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);

    }
}