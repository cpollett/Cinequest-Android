package edu.sjsu.cinequest;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeMap;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import edu.sjsu.cinequest.comm.Platform;
import edu.sjsu.cinequest.comm.cinequestitem.CommonItem;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;

/*
 * This superclass has convenience methods for making lists of schedules and
 * filmlets.
 */
public class CinequestActivity extends Activity
{
	private static final int HOME_MENUOPTION_ID = Menu.FIRST + 11;
	private static final int SCHEDULE_MENUOPTION_ID = Menu.FIRST + 12;
	private static final int ABOUT_MENUOPTION_ID = Menu.FIRST + 13;	
	public static String calendarName="Cinequest Calendar";
	public static String m_selectedCalendarId = "Cinequest Calendar";

	/**
	 * Launches the FilmDetail activity for the given object.
	 * @param result Object; Can be Schedule, Filmlet etc
	 */
	protected void launchFilmDetail(Object result) {
		Intent intent = new Intent();
		intent.setClass(this, FilmDetail.class);
		intent.putExtra("target", (Serializable) result);
		startActivity(intent);	
	}

	/**
	 * Creates a list of schedules
	 * @param listItems the list items
	 * @param isChecked a function to determine when to check a checkbox, or null for no checkboxes
	 * @param listener the listener for checkboxes, or null for no checkboxes
	 	button.setText("-");
			populateCalendarID();
			configureCalendarIcon(v, button, result);
			Button directions = (Button) v.findViewById(R.id.directionsURL);
			directions.setTag(result);	        
			directions.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
							Uri.parse(result.getDirectionsURL()));					
					startActivity(intent);
				}

			});
			return v;	        
		}

		/**
		 * Override to change the formatting of the contents
		 */
		protected void formatContents(View v, TextView title, TextView time, TextView venue, DateUtils du, Schedule result) {
		}

		//Calendar code for adding/removing events from Device Calendar
		protected void configureCalendarIcon(View v, final Button button, Schedule result) {
			button.setVisibility(View.VISIBLE);

			Schedule s = result;							
			
			SimpleDateFormat formatter;			
			if (s.getStartTime().charAt(10)=='T')
				formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
			else
				formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date startDate = null;
			Date endDate = null;

			try {
				startDate = (Date) formatter.parse(s.getStartTime());
				endDate = (Date) formatter.parse(s.getEndTime());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}                                        
			long begin = startDate.getTime();
			long end = endDate.getTime();
			String[] proj = new String[]{
					"_id", 
					"title",
					"dtstart", 
			"dtend"};

			String calSelection = "((calendar_id= ?) " +                                                                         
					"AND (" +
					"((dtstart= ?) " +
					"AND (dtend= ?) " +
					"AND (title= ?) " +
					") " +                                            
					")" +
					")";         
			String[] calSelectionArgs = new String[] {
					m_selectedCalendarId, begin+"", end+"", s.getTitle()                                       
			}; 

			Uri event=null;
			Cursor l_managedCursor=null;
			
			if (Build.VERSION.SDK_INT >= 8) {
				event = Uri.parse("content://com.android.calendar/events");
			} else {
				//Calendar code for API level < 8, needs lot of testing. 
				//May be some of the paramters (that we are populating above), have different naming conventions in different API Levels
				event = Uri.parse("content://calendar/events");
			}
			try{
				l_managedCursor = getContentResolver().query(event, proj, calSelection, calSelectionArgs, "dtstart DESC, dtend DESC");
			}
			catch (Exception e){
				Log.i("CinequestActivity:configureCalendarIcon","Error while retrieving Event details from Calendar");
			}
			
			if (l_managedCursor.getCount()>0) {                                                    
				button.setBackgroundResource(R.drawable.incalendar);
				button.setHint("exists");				
			}
			else
			{
				button.setBackgroundResource(R.drawable.notincalendar);
				button.setHint("notexist");
			}

			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					Schedule s = (Schedule) v.getTag();										
					SimpleDateFormat formatter;
					if (s.getStartTime().charAt(10)=='T')
						formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
					else
						formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

					Date startDate = null;
					Date endDate = null;

					try {
						startDate = (Date) formatter.parse(s.getStartTime());
						endDate = (Date) formatter.parse(s.getEndTime());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					long begin = startDate.getTime();
					long end = endDate.getTime();										
					
					if (button.getHint().toString()=="exists")
					{
						String[] proj = new String[]{
								"_id", 
								"title",
								"dtstart", 
						"dtend"};

						String calSelection = "((calendar_id= ?) " +                                                                         
								"AND (" +
								"((dtstart= ?) " +
								"AND (dtend= ?) " +
								"AND (title= ?) " +
								") " +                                            
								")" +
								")";         
						String[] calSelectionArgs = new String[] {
								m_selectedCalendarId, begin+"", end+"", s.getTitle()                                       
						}; 

						Uri event=null;
						Cursor l_managedCursor=null;
						if (Build.VERSION.SDK_INT >= 8) {
							event = Uri.parse("content://com.android.calendar/events");
						} else {
							//Calendar code for API level < 8, needs lot of testing. 
							//May be some of the paramters (that we are populating above), have different naming conventions in different API Levels
							event = Uri.parse("content://calendar/events");
						}
						try{
							l_managedCursor = getContentResolver().query(event, proj, calSelection, calSelectionArgs, "dtstart DESC, dtend DESC");
						}
						catch (Exception e){
							Log.i("CinequestActivity:configureCalendarIcon","Error while retrieving Event details from Calendar");
						}
						
						int e_id = 0;
						if (l_managedCursor.moveToFirst()) {														
							int l_colid = l_managedCursor.getColumnIndex(proj[0]);							
							do {
								e_id = l_managedCursor.getInt(l_colid);															
							} 
							while (l_managedCursor.moveToNext());
						}
						Uri eventUri;                    
						if (Build.VERSION.SDK_INT >= 8) {
							eventUri = Uri.parse("content://com.android.calendar/events");
						} else {
							//Calendar code for API level < 8, needs lot of testing. 
							//May be some of the paramters (that we are populating above), have different naming conventions in different API Levels
							eventUri = Uri.parse("content://calendar/events");
						}                        
						Uri deleteUri = ContentUris.withAppendedId(eventUri, e_id);
						try
						{
							int rows = getContentResolver().delete(deleteUri, null, null);
							if (rows==1){
								button.setBackgroundResource(R.drawable.notincalendar);
								button.setHint("notexist");
								Toast toast = Toast.makeText(getContext(), "Event removed from calendar", Toast.LENGTH_SHORT);
								toast.show();                        							
							}
						}
						catch (Exception e){
							Log.i("CinequestActivity:configureCalendarIcon","Error while removing Events from Calendar");
						}
						l_managedCursor.close();
						l_managedCursor=null;
					}

					else{
						ContentValues l_event = new ContentValues();
						l_event.put("calendar_id", m_selectedCalendarId);
								@Override
		public View getView(int position, View v, ViewGroup parent) {            
			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(RESOURCE_ID, null);
			}

			CommonItem result = getItem(position);            
			TextView title = (TextView) v.findViewById(R.id.listitem_titletext);
			title.setText(result.getTitle());	                 
			formatContents(v, title, result);		        
			return v;	        
		}

		/**
		 * Override to change the formatting of the contents
		 */
		protected void formatContents(View v, TextView title, CommonItem result) {
		}
	}	

	/**
	 * Take the user to home activity
	 */
	private void goHome(){

		Intent i = new Intent();
		i.setClass(this, MainTab.class);		
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}
	private void goSchedule(){
		Intent i = new Intent();		
		i.setClass(this, MainTab.class);		
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);        
		i.putExtra("open_tab", 4);
		startActivity(i);
	}

	/**
	 * Create a menu to be displayed when user hits Menu key on device
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, HOME_MENUOPTION_ID, 0,"Home").setIcon(R.drawable.home);
		menu.add(0, SCHEDULE_MENUOPTION_ID, 0,"Schedule").setIcon(R.drawable.schedule_icon);		
		menu.add(0, ABOUT_MENUOPTION_ID, 0,"About").setIcon(R.drawable.about);

		return true;
	}

	/** Menu Item Click Listener*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case HOME_MENUOPTION_ID:
			goHome();
			return true;
		case SCHEDULE_MENUOPTION_ID:
			goSchedule();
			return true;
		case ABOUT_MENUOPTION_ID:
			DialogPrompt.showAppAboutDialog(this);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}

	}

    /**
     * Gets the calendar id for Cinequest
     */
	public void populateCalendarID(){
		String[] proj = new String[]{"_id", "calendar_displayName"};			        
		String calSelection = "(calendar_displayName= ?) ";
		String[] calSelectionArgs = new String[] {calendarName};
		
		Uri event=null;
		
		if (Build.VERSION.SDK_INT >= 8) {
			event = Uri.parse("content://com.android.calendar/calendars");
		} else {
			//Calendar code for API level < 8, needs lot of testing. 
			//May be some of the paramters (that we are populating above), have different naming conventions in different API Levels
			event = Uri.parse("content://calendar/calendars");
		}										      
		Cursor l_managedCursor = null;
		try{
			l_managedCursor = getContentResolver().query(event, proj, calSelection, calSelectionArgs, null );

			if (l_managedCursor.moveToFirst()) {                        			                     
				int l_idCol = l_managedCursor.getColumnIndex(proj[0]);
				do {                
					m_selectedCalendarId = l_managedCursor.getString(l_idCol);                
				}  while (l_managedCursor.moveToNext());
			}
		}
		catch (Exception e){
			Log.i("CinequestActivity:populateCalendarID","Error while retrieving Cinequest Calendar ID from device Calendar");
		}

		l_managedCursor.close();
		l_managedCursor=null;
	}
}
