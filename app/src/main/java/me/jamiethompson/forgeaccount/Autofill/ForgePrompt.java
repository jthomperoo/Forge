package me.jamiethompson.forgeaccount.Autofill;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import me.jamiethompson.forgeaccount.R;

public class ForgePrompt extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_forge_prompt);
//		final WindowManager.LayoutParams param = new WindowManager.LayoutParams();
//		param.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//		final View view = findViewById(R.id.activity_forge_prompt);
//		final ViewGroup parent = (ViewGroup) view.getParent();
//		if (parent != null)
//		{
//			parent.removeView(view);
//		}
//		param.format = PixelFormat.RGBA_8888;
//		param.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//		param.gravity = Gravity.TOP | Gravity.LEFT;
//		param.width = parent != null ? LinearLayout.LayoutParams.WRAP_CONTENT : view.getLayoutParams().width;
//		param.height = parent != null ? LinearLayout.LayoutParams.WRAP_CONTENT : view.getLayoutParams().height;
//		final WindowManager wmgr = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
//		wmgr.addView(view, param);
	}
}
