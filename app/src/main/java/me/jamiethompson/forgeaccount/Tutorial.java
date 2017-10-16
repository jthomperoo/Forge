package me.jamiethompson.forgeaccount;

import android.app.Activity;
import android.view.View;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

/**
 * Created by jamie on 10/10/17.
 */

public class Tutorial implements View.OnClickListener
{
	private static Activity activity;
	ViewTarget globalRefreshButton;
	ViewTarget refreshButton;
	ViewTarget copyButton;
	ViewTarget preferencesButton;
	ViewTarget inbox;
	private ShowcaseView showcase;
	private int position;
	private final int GLOBAL_REFRESH_POSITION = 0;
	private final int LOCAL_REFRESH_POSITION = 1;
	private final int COPY_POSITION = 2;
	private final int LOCAL_PREFERENCES_POSITION = 3;
	private final int INBOX_POSITION = 4;


	public Tutorial(Activity activity, int globalRefreshButton, int refreshButton, int copyButton, int preferencesButton, int inbox)
	{
		this.activity = activity;
		this.globalRefreshButton = new ViewTarget(globalRefreshButton, activity);
		this.refreshButton = new ViewTarget(refreshButton, activity);
		this.copyButton = new ViewTarget(copyButton, activity);
		this.preferencesButton = new ViewTarget(preferencesButton, activity);
		this.inbox = new ViewTarget(inbox, activity);
		position = 0;
	}

	public void startTutorial()
	{
		showcase = new ShowcaseView.Builder(activity)
				.withNewStyleShowcase()
				.setTarget(globalRefreshButton)
				.setStyle(R.style.TutorialTheme)
				.setContentTitle("ShowcaseView")
				.setContentText("This is highlighting the Home button")
				.setOnClickListener(this)
				.build();
		position++;
	}

	@Override
	public void onClick(View view)
	{
		switch (position)
		{
			case GLOBAL_REFRESH_POSITION:
			{
				showcase.setShowcase(globalRefreshButton, true);
				break;
			}
			case LOCAL_REFRESH_POSITION:
			{
				showcase.setShowcase(refreshButton, true);
				break;
			}
			case COPY_POSITION:
			{
				showcase.setShowcase(copyButton, true);
				break;
			}
			case LOCAL_PREFERENCES_POSITION:
			{
				showcase.setShowcase(preferencesButton, true);
				break;
			}
			case INBOX_POSITION:
			{
				showcase.setShowcase(inbox, true);
				break;
			}
			default:
			{
				showcase.hide();
				break;
			}
		}
		position++;
	}
}
