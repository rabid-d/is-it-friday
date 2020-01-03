package `is`.it.friday

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v4.widget.CircularProgressDrawable
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.Calendar.*

// TODO: Lint check
class MainActivity : AppCompatActivity() {
    private val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this) }
    private val minSessionDuration = 300L
    private var inForeground = false
    private var day: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.title = "Is it Friday?"

        initAnalytics()

        Log.e("IsItFriday", "onCreate")
    }

    private fun initAnalytics() {
        val dayString = when (Calendar.getInstance().get(DAY_OF_WEEK)) {
            MONDAY -> "monday"
            TUESDAY -> "tuesday"
            WEDNESDAY -> "wednesday"
            THURSDAY -> "thursday"
            FRIDAY -> "friday"
            SATURDAY -> "saturday"
            SUNDAY -> "sunday"
            else -> "error"
        }
        Log.e("IsItFriday", "Day string: $dayString")
        val params = Bundle()
        params.putString("open_day", dayString)
        with(firebaseAnalytics) {
            setMinimumSessionDuration(minSessionDuration)
            logEvent("app_open_day", params)
        }
    }

    private fun getRandomGifUrl() = when (day) {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY -> GifUrl.WEEKDAYS.shuffled()[0]
        FRIDAY -> GifUrl.FRIDAY.shuffled()[0]
        SATURDAY, SUNDAY -> GifUrl.WEEKEND.shuffled()[0]
        else -> "error"
    }

    //private fun getRandomGifUrl() = "https://media.giphy.com/media/rk8InLF3Hi7CM/giphy.gif" // Not working GIF.
    //private fun getRandomGifUrl() = "https://media.giphy.com/media/amUVFzg1wNZKg/giphy.gif"

    private fun hideGifImgView() {
        gifImageView.setImageDrawable(null)
        gifImageView.visibility = View.GONE
        val params = longAnswerTextView.layoutParams as ConstraintLayout.LayoutParams
        params.bottomToBottom = guideline.id
    }

    override fun onResume() {
        Log.e("IsItFriday", "onResume")
        inForeground = true
        super.onResume()
        // If day changed while app was in background.
        if (day != Calendar.getInstance().get(DAY_OF_WEEK)) {
            day = Calendar.getInstance().get(DAY_OF_WEEK)
            setAnswerCaption()
            // And update gif if phone has internet connection.
            if (isOnline()) {
                downloadGif()
            } else {
                hideGifImgView()
            }
        } else { // If day wasn't changed.
            // If there was some troubles with downloading a gif, try to download it again.
            if (gifImageView.visibility == View.GONE && isOnline()) {
                downloadGif()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        inForeground = false
        Log.e("IsItFriday", "onPause")
    }

    private fun downloadGif() {
        gifImageView.setImageDrawable(null)
        gifImageView.visibility = View.VISIBLE
        val params = longAnswerTextView.layoutParams as ConstraintLayout.LayoutParams
        params.bottomToBottom = ConstraintSet.PARENT_ID
        val circularProgressDrawable = CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN)
        circularProgressDrawable.start()
        GlideApp.with(applicationContext)
                .asGif()
                .load(getRandomGifUrl())
                .error(android.R.color.transparent)
                .placeholder(circularProgressDrawable)
                .transition(withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .listener(downloadListener)
                .into(gifImageView)
    }

    private val downloadListener = object: RequestListener<GifDrawable> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>?, isFirstResource: Boolean): Boolean {
            Log.e("IsItFriday", "onLoadFailed")
            hideGifImgView()
            return false
        }

        override fun onResourceReady(resource: GifDrawable?, model: Any?, target: Target<GifDrawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            Log.e("IsItFriday", "onResourceReady")
            val params = Bundle()
            params.putString("when_app", if (inForeground) "in_foreground" else "in_background")
            firebaseAnalytics.logEvent("gif_download_ready", params)
            return false
        }
    }

    private fun setAnswerCaption() {
        shortAnswerTextView.text = when (day) {
            FRIDAY -> getString(R.string.short_answer_yes)
            else   -> getString(R.string.short_answer_no)
        }
        longAnswerTextView.text = when (day) {
            MONDAY    -> getString(R.string.long_answer_monday)
            TUESDAY   -> getString(R.string.long_answer_tuesday)
            WEDNESDAY -> getString(R.string.long_answer_wednesday)
            THURSDAY  -> getString(R.string.long_answer_thursday)
            FRIDAY    -> getString(R.string.long_answer_friday)
            SATURDAY  -> getString(R.string.long_answer_saturday)
            SUNDAY    -> getString(R.string.long_answer_sunday)
            else      -> ""
        }
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }
}
