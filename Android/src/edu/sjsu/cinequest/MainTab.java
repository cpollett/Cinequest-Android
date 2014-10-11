package edu.sjsu.cinequest;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;


/**
 * This class creates the main cotainer used in the Application. A TabActivity displays a child Activity in
 * each tab. Each of these Activities is defined in its own class. They are loaded here using Intents.
 * This class has been depracted since API13. Fragments should be used For API13+ handsets. Backwards compatablity
 * for previous versions can be defined in a v4 support library.
 */
public class MainTab extends TabActivity {

	public void onCreate(Bundle savedInstanceState) {    	    	
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// Get host object from super class
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;

		intent = new Intent().setClass(this, HomeActivity.class);
		spec = tabHost.newTabSpec("news").setIndicator("News",getResources().getDrawable(R.drawable.news)).setContent(intent);
		tabHost.addTab(spec);

		// Create the intent associated with the activity
		intent = new Intent().setClass(this, FilmsActivity.class);
		intent.putExtra("tab", "films");
		// Create a new TabSpec with a name, an icon and intent
		spec = tabHost.newTabSpec("films").setIndicator("Films",getResources().getDrawable(R.drawable.film_icon)).setContent(intent);
		// Add it to the tab
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, FilmsActivity.class);
		intent.putExtra("tab", "events");
		spec = tabHost.newTabSpec("events").setIndicator("Events",getResources().getDrawable(R.drawable.events_icon)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, FilmsActivity.class);
		intent.putExtra("tab", "forums");
		spec = tabHost.newTabSpec("forums").setIndicator("Forums",getResources().getDrawable(R.drawable.forums_icon)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, ScheduleActivity.class);        
		spec = tabHost.newTabSpec("schedule").setIndicator("Schedule",getResources().getDrawable(R.drawable.schedule_icon)).setContent(intent);
		tabHost.addTab(spec);

		// Default tab is the first tab.
		int tab = getIntent().getIntExtra("open_tab", 0);
		tabHost.setCurrentTab(tab);                
	}
}
