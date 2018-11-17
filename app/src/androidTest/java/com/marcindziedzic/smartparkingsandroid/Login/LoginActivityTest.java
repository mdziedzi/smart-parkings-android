package com.marcindziedzic.smartparkingsandroid.Login;


import com.marcindziedzic.smartparkingsandroid.GUI.LoginActivity;
import com.marcindziedzic.smartparkingsandroid.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;


import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule =
            new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void typeSomething() {
        String s = "this is test";

        onView(withId(R.id.agentNameText)).perform(typeText(s), closeSoftKeyboard());
        onView(withId(R.id.agentNameText)).check(matches(withText(s)));
    }
}
