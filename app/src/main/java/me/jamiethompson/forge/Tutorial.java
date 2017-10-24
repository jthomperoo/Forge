package me.jamiethompson.forge;

import android.app.Activity;
import android.view.View;
import android.widget.ScrollView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

/**
 * Created by jamie on 10/10/17.
 *
 * Handles the start up tutorial for Forge
 */

public class Tutorial implements View.OnClickListener {
    // Activity to show tutorial on
    private Activity activity;
    // Tutorial targets
    private ViewTarget refreshGlobal;
    private ViewTarget refreshLocal;
    private ViewTarget copy;
    private ViewTarget inbox;
    // Activity scrollview
    private ScrollView scrollView;
    // Tutorial showcase
    private ShowcaseView showcase;
    // Current tutorial position
    private int position;


    /**
     * @param activity      the activity to show the tutorial on
     * @param refreshGlobal the global refresh button ID
     * @param refreshLocal  the local refresh button ID
     * @param copy          the copy button ID
     * @param inbox         the inbox ID
     * @param scrollView    the activity scrollview
     */

    public Tutorial(Activity activity, int refreshGlobal, int refreshLocal, int copy, int inbox, ScrollView scrollView) {
        this.activity = activity;
        // Set up tutorial view targets
        this.refreshGlobal = new ViewTarget(refreshGlobal, activity);
        this.refreshLocal = new ViewTarget(refreshLocal, activity);
        this.copy = new ViewTarget(copy, activity);
        this.inbox = new ViewTarget(inbox, activity);
        this.scrollView = scrollView;
        // Set tutorial position
        position = 0;
    }

    /**
     * Starts the tutorial and sets up tutorial showcase with starting values
     */
    public void startTutorial() {
        showcase = new ShowcaseView.Builder(activity)
                .withNewStyleShowcase()
                .setTarget(refreshGlobal)
                .setStyle(R.style.TutorialTheme)
                .setContentTitle(activity.getString(R.string.tutorial_global_refresh_title))
                .setContentText(activity.getString(R.string.tutorial_global_refresh_text))
                .setOnClickListener(this)
                .build();
        position++;
    }

    @Override
    public void onClick(View view) {
        // Progress the tutorial
        final int GLOBAL_REFRESH_POSITION = 0;
        final int LOCAL_REFRESH_POSITION = 1;
        final int COPY_POSITION = 2;
        final int INBOX_POSITION = 3;

        // Depending on the current position
        switch (position) {
            case GLOBAL_REFRESH_POSITION: {
                // Update showcase target
                showcase.setShowcase(refreshGlobal, true);
                // Update showcase text
                showcase.setContentTitle(activity.getString(R.string.tutorial_global_refresh_title));
                showcase.setContentText(activity.getString(R.string.tutorial_global_refresh_text));
                break;
            }
            case LOCAL_REFRESH_POSITION: {
                showcase.setShowcase(refreshLocal, true);
                showcase.setContentTitle(activity.getString(R.string.tutorial_local_refresh_title));
                showcase.setContentText(activity.getString(R.string.tutorial_local_refresh_text));
                break;
            }
            case COPY_POSITION: {
                showcase.setShowcase(copy, true);
                showcase.setContentTitle(activity.getString(R.string.tutorial_copy_title));
                showcase.setContentText(activity.getString(R.string.tutorial_copy_text));
                break;
            }

            case INBOX_POSITION: {
                // Scroll to the bottom of the screen to show the inbox
                scrollView.scrollTo(0, scrollView.getBottom());
                showcase.setShowcase(inbox, true);
                showcase.setContentTitle(activity.getString(R.string.tutorial_inbox_title));
                showcase.setContentText(activity.getString(R.string.tutorial_inbox_text));
                // Update the button text to show the end of the tutorial
                showcase.setButtonText(activity.getString(android.R.string.ok));
                break;
            }
            default: {
                showcase.hide();
                break;
            }
        }
        // Increment current position
        position++;
    }
}
