package me.jamiethompson.forgeaccount;

import android.app.Activity;
import android.view.View;
import android.widget.ScrollView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

/**
 * Created by jamie on 10/10/17.
 */

public class Tutorial implements View.OnClickListener {
    private static Activity activity;
    private ViewTarget globalRefreshButton;
    private ViewTarget refreshButton;
    private ViewTarget copyButton;
    private ViewTarget preferencesButton;
    private ViewTarget inbox;
    private ScrollView scrollView;
    private ShowcaseView showcase;
    private int position;
    private final int GLOBAL_REFRESH_POSITION = 0;
    private final int LOCAL_REFRESH_POSITION = 1;
    private final int COPY_POSITION = 2;
    private final int LOCAL_PREFERENCES_POSITION = 3;
    private final int INBOX_POSITION = 4;


    public Tutorial(Activity activity, int globalRefreshButton, int refreshButton, int copyButton, int preferencesButton, int inbox, ScrollView scrollView) {
        this.activity = activity;
        this.globalRefreshButton = new ViewTarget(globalRefreshButton, activity);
        this.refreshButton = new ViewTarget(refreshButton, activity);
        this.copyButton = new ViewTarget(copyButton, activity);
        this.preferencesButton = new ViewTarget(preferencesButton, activity);
        this.inbox = new ViewTarget(inbox, activity);
        this.scrollView = scrollView;
        position = 0;
    }

    public void startTutorial() {
        showcase = new ShowcaseView.Builder(activity)
                .withNewStyleShowcase()
                .setTarget(globalRefreshButton)
                .setStyle(R.style.TutorialTheme)
                .setContentTitle(activity.getString(R.string.tutorial_global_refresh_title))
                .setContentText(activity.getString(R.string.tutorial_global_refresh_text))
                .setOnClickListener(this)
                .build();
        position++;
    }

    @Override
    public void onClick(View view) {
        switch (position) {
            case GLOBAL_REFRESH_POSITION: {
                showcase.setShowcase(globalRefreshButton, true);
                showcase.setContentTitle(activity.getString(R.string.tutorial_global_refresh_title));
                showcase.setContentText(activity.getString(R.string.tutorial_global_refresh_text));
                break;
            }
            case LOCAL_REFRESH_POSITION: {
                showcase.setShowcase(refreshButton, true);
                showcase.setContentTitle(activity.getString(R.string.tutorial_local_refresh_title));
                showcase.setContentText(activity.getString(R.string.tutorial_local_refresh_text));
                break;
            }
            case COPY_POSITION: {
                showcase.setShowcase(copyButton, true);
                showcase.setContentTitle(activity.getString(R.string.tutorial_copy_title));
                showcase.setContentText(activity.getString(R.string.tutorial_copy_text));
                break;
            }
            case LOCAL_PREFERENCES_POSITION: {
                showcase.setShowcase(preferencesButton, true);
                showcase.setContentTitle(activity.getString(R.string.tutorial_local_preferences_title));
                showcase.setContentText(activity.getString(R.string.tutorial_local_preferences_text));
                break;
            }
            case INBOX_POSITION: {
                scrollView.scrollTo(0, scrollView.getBottom());
                showcase.setShowcase(inbox, true);
                showcase.setContentTitle(activity.getString(R.string.tutorial_inbox_title));
                showcase.setContentText(activity.getString(R.string.tutorial_inbox_text));
                showcase.setButtonText(activity.getString(android.R.string.ok));
                break;
            }
            default: {
                showcase.hide();
                break;
            }
        }
        position++;
    }
}
