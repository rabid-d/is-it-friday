package `is`.it.friday

import `is`.it.friday.R.id.longAnswerTextView
import `is`.it.friday.R.id.shortAnswerTextView
import `is`.it.friday.R.string.short_answer_no
import `is`.it.friday.R.string.short_answer_yes
import `is`.it.friday.R.string.long_answer_monday
import `is`.it.friday.R.string.long_answer_tuesday
import `is`.it.friday.R.string.long_answer_wednesday
import `is`.it.friday.R.string.long_answer_thursday
import `is`.it.friday.R.string.long_answer_friday
import `is`.it.friday.R.string.long_answer_saturday
import `is`.it.friday.R.string.long_answer_sunday
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import org.junit.Rule
import java.util.*
import java.util.Calendar.*


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Rule @JvmField
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    fun checkShortAnswer() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        //assertEquals("is.it.friday", appContext.packageName)
        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) != FRIDAY) {
            onView(withId(shortAnswerTextView)).check(matches(withText(appContext.getString(short_answer_no))))
        } else {
            onView(withId(shortAnswerTextView)).check(matches(withText(appContext.getString(short_answer_yes))))
        }
    }

    @Test
    fun checkLongAnswer() {
        val id = when(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            MONDAY -> long_answer_monday
            TUESDAY -> long_answer_tuesday
            WEDNESDAY -> long_answer_wednesday
            THURSDAY -> long_answer_thursday
            FRIDAY -> long_answer_friday
            SATURDAY -> long_answer_saturday
            SUNDAY -> long_answer_sunday
            else -> -1
        }
        val appContext = InstrumentationRegistry.getTargetContext()
        onView(withId(longAnswerTextView)).check(matches(withText(appContext.getString(id))))
    }
}
