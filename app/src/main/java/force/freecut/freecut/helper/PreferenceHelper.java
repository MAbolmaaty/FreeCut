package force.freecut.freecut.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class PreferenceHelper {

	private SharedPreferences app_prefs;
	private static String app_ref = "force_store";
	private final String LanguageID = "lang_id";
	private final String FirstOpen = "firstopen";
	 Context context;


	public PreferenceHelper(Context context) {
		this.context = context;
		try {
			app_prefs = context.getSharedPreferences(app_ref, Context.MODE_PRIVATE);
		}
		catch (NullPointerException e)
		{

		}

	}
	public boolean isFirstOpen() {
		return app_prefs.getBoolean(FirstOpen, true);

	}

	public void setFirstOpen(boolean cityID) {
		Editor edit = app_prefs.edit();
		edit.putBoolean(FirstOpen, cityID);
		edit.apply();
	}





	public int getLanguageID()
	{
		return app_prefs.getInt(LanguageID, 1);
	}
	public void setLanguageID(int cityID) {
		Editor edit = app_prefs.edit();
		edit.putInt(LanguageID, cityID);
		edit.apply();
	}






}
