package edu.sjsu.cinequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import edu.sjsu.cinequest.android.AndroidPlatform;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.ImageManager;
import edu.sjsu.cinequest.comm.Platform;
import edu.sjsu.cinequest.comm.QueryManager;
import edu.sjsu.cinequest.comm.cinequestitem.MobileItem;
import edu.sjsu.cinequest.comm.cinequestitem.News;
import edu.sjsu.cinequest.comm.cinequestitem.NewsFeed;
import edu.sjsu.cinequest.comm.cinequestitem.User;

// TODO: Add click for each item; show the section info

/**
 * The home screen of the app  
 * 
 * @author Prabhjeet Ghuman
 *
 */
public class HomeActivity extends Activity {	
	private ListView list;
	ImageView title_image;
	String lastupdated="";

	private static QueryManager queryManager;
	private static ImageManager imageManager;
	private static User user;
	LazyAdapter adapter;

	private static String calendarName="Cinequest Calendar";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_layout);

		// TODO: Remove this to turn on test mode
		// DateUtils.setMode(DateUtils.FESTIVAL_TEST_MODE);
		Context context = getApplicationContext();
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(getPackageName(), 0);
			String version = pi.versionName;
			setTitle("Cinequest" + (version == null ? "" : " " + version));
		} catch (NameNotFoundException ex) {
			// We tried...
		}

		String[] proj = new String[]{"_id", "calendar_displayName"};
		String calSelection = "(calendar_displayName= ?) "; 				
		String[] calSelectionArgs = new String[] {calendarName}; 
		Uri event = Uri.parse("content://com.android.calendar/calendars");        

		Cursor l_managedCursor = getContentResolver().query(event, proj, calSelection, calSelectionArgs, null );
		if (l_managedCursor.getCount()<=0) {                                                    

			ContentValues l_event = new ContentValues();
			l_event.put("account_name", "Cinequest");
			l_event.put("account_type", "LOCAL");                  
			l_event.put("name", calendarName);
			//l_event.put("displayName", "Cinequest Calendar");
			//l_event.put("color", 0xffff0000);
			//l_event.put("access_level",  700);
			//l_event.put("timezone", TimeZone.getDefault().getID());
			l_event.put("calendar_displayName", calendarName);
			l_event.put("calendar_color", 0xffff0000);
			l_event.put("calendar_access_level",  700);
			l_event.put("calendar_timezone", TimeZone.getDefault().getID());
			l_event.put("ownerAccount", "owner");
			l_event.put("visible", 1);
			Uri.Builder builder = event.buildUpon();
			builder.appendQueryParameter("account_name", "Cinequest");
			builder.appendQueryParameter("account_type", "LOCAL");                
			builder.appendQueryParameter( "caller_is_syncadapter", "true");
			Uri uri = getContentResolver().insert(builder.build(), l_event); 
			l_managedCursor.close();
			l_managedCursor=null;
		}

		Platform.setInstance(new AndroidPlatform(getApplicationContext()));
		queryManager = new QueryManager();
		imageManager = new ImageManager();
		user = new User();


		queryManager.getFestivalDates(new ProgressMonitorCallback(this) {
			public void invoke(Object result)
			{
				super.invoke(result);
				DateUtils.setFestivalDates((String[]) result);            
			}
		});        

		title_image = (ImageView) this.findViewById(R.id.homescreen_title_image);
		title_image.setImageDrawable(getResources().getDrawable(R.drawable.creative));

		list = (ListView)this.findViewById(R.id.home_newslist);
		/*list.setAdapter(new SeparatedListAdapter(this));

		list.setOnItemClickListener( new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				MobileItem item = (MobileItem) list.getItemAtPosition(position);
				Intent intent = new Intent();
				intent.setClass(HomeActivity.this, FilmDetail.class);
				intent.putExtra("target", item);
				startActivity(intent);
			}
		});
		 */
		/*		Button festivalButton = (Button) findViewById(R.id.goto_festival_button);
		festivalButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(HomeActivity.this, MainTab.class);
				i.putExtra("open_tab", MainTab.FILMS_TAB);
				startActivity(i);
			}
		});

		Button dvdButton = (Button) findViewById(R.id.goto_dvd_button);
		dvdButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(HomeActivity.this, MainTab.class);
				i.putExtra("open_tab", MainTab.DVDS_TAB);
				startActivityForResult(i, 0);
			}
		});              
		 */	}

	/**
	 * Called when activity resumes
	 */
	@Override
	public void onResume(){
		super.onResume();    	
		queryManager.getSpecialScreen("ihome", new Callback(){
			@Override
			public void invoke(Object result) {

				//populateNewsEventsList((Vector<Section>) result);

				populateNewsEventsList((NewsFeed) result);
				// TODO: Why doesn't this work???
				queryManager.prefetchFestival();
			}
			@Override public void starting() {}			
			@Override public void failure(Throwable t) {
				Platform.getInstance().log(t);				
			}        	
		});
	}

	protected void onStop(){
		user.persistSchedule();
		imageManager.close();
		Platform.getInstance().close();
		super.onStop();
	}

	/**
	 * Display the header image and schedule to the user with 
	 * section-title being separator-header.
	 */
	//private void populateNewsEventsList(Vector<Section> newsSections)
	private void populateNewsEventsList(NewsFeed newsSections)
	{		
		/*if (lastupdated!=newsSections.getLastUpdated() || lastupdated=="")
		{
			lastupdated=newsSections.getLastUpdated();
			ImageLoader imageLoader = new ImageLoader(this);
			imageLoader.clearCache();			
		}*/
		title_image.setVisibility(View.GONE);
		final List<News> news=newsSections.getNewsList();
		ArrayList<String> imgURL = new ArrayList<String>();
		for (int i=0;i<news.size();i++)
		{
			imgURL.add(news.get(i).getEventImage());
			Log.i("trace1",news.get(i).getEventImage());
		}
		String[] imageURL=imgURL.toArray(new String[imgURL.size()]);

		//if there is no news to display, return
		/*if (newsSections.size() == 0) {
			//Clear the items of previous list being displayed (if any)
			list.setAdapter(new SeparatedListAdapter(this));
			return;
		}

		// create our list and custom adapter  
		SeparatedListAdapter separatedListAdapter = new SeparatedListAdapter(this);

		for(int i = 0; i < newsSections.size(); i++) {
			Section s = newsSections.get(i);
			String sectionTitle = s.getTitle();
			Vector items = s.getItems();

			ArrayList<MobileItem> newsEvents = new ArrayList<MobileItem>();

			if (i == 0) {
				if (items.size() > 0) {
					String imageURL = ((MobileItem) items.get(0)).getImageURL();
					getImageManager().getImage(imageURL, new Callback() {
						@Override public void invoke(Object result) {
							title_image.setImageBitmap((Bitmap) result);	        		
						}
						@Override public void starting() {}
						@Override public void failure(Throwable t) {
							Platform.getInstance().log(t); 			        		
						}
					}, null, false);		
				}
			}
			else {     		
				for(int j = 0; j < items.size(); j++) {
					newsEvents.add((MobileItem) items.get(j));     			
				}
				separatedListAdapter.addSection(sectionTitle, new MobileItemAdapter(this, R.layout.home_event_row, newsEvents));
			}
		}

		list.setAdapter(separatedListAdapter);

		 */	
		/*SeparatedListAdapter adapter = new SeparatedListAdapter(this);
		adapter.addSection("News",new LazyAdapter(this, imageURL, news));*/
		adapter=new LazyAdapter(this, imageURL, news);
		list.setAdapter(adapter);		
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {

				/*Object result = getListview().getItemAtPosition( position );
				Log.i("tabactivity",result.getClass().toString());
				launchFilmDetail(result);				*/
				Intent intent;
				if (news.get(position).getInfoLink().trim().toLowerCase().startsWith("http"))
				{
					
					intent = new Intent(android.content.Intent.ACTION_VIEW, 
							Uri.parse(news.get(position).getInfoLink()));			
					startActivity(intent);
				}
				else
				{
					int itemId= Integer.parseInt(news.get(position).getInfoLink());
					intent = new Intent();
					intent.setClass(HomeActivity.this, FilmDetail.class);
					intent.putExtra("target", (Serializable) news.get(position).getInfoLink());
					startActivity(intent);
				}
			}
		});		
	}

	/**
	 * Get the QueryManager
	 * @return queryManager
	 */
	public static QueryManager getQueryManager() {
		return queryManager;
	}

	public static ImageManager getImageManager() {
		return imageManager;
	}

	/**
	 * Get the User
	 * @return user
	 */
	public static User getUser() {
		return user;
	}

	/**
	 * Create a menu to be displayed when user hits Menu key on device
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.homeactivity_menu, menu);

		return true;
	}

	/** Menu Item Click Listener*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_option_about:
			DialogPrompt.showAppAboutDialog(this);
			return true;	        	            

		default:
			return super.onOptionsItemSelected(item);
		}

	}

	/**
	 * Custom List-Adapter to show the MobileItems in news event list 
	 */
	private class MobileItemAdapter extends ArrayAdapter<MobileItem>{

		private ArrayList<MobileItem> itemsList;
		private int view_resource_id;	//id of the list's row's layout xml

		public MobileItemAdapter(Context context, int textViewResourceId, ArrayList<MobileItem> list) {
			super(context, textViewResourceId, list);
			this.itemsList = list;
			this.view_resource_id = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(view_resource_id, null);
			}
			MobileItem result = itemsList.get(position);

			if (result != null) {
				//get text from list, and fill it into the row		
				TextView title = (TextView) v.findViewById(R.id.news_event_title);                        

				//Set title text
				if (title != null)
					title.setText(result.getTitle());     
			}

			return v;
		}
	}
}
