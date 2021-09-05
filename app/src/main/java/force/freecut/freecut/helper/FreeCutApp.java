package force.freecut.freecut.helper;

import android.app.Application;
import android.content.Context;


public class FreeCutApp extends Application {

    private static FreeCutApp instance;


    public static final String TAG = FreeCutApp.class.getSimpleName();




    public static synchronized FreeCutApp getInstance() {
        if (instance == null){
            throw new IllegalStateException("Something went horribly wrong!!, no application attached!");
        }
        return instance;
    }


    @Override
    protected void attachBaseContext(Context base)
    {
       // super.attachBaseContext(MyContextWrapper.wrap(base, "ar"));
        super.attachBaseContext(base);


    }





    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }


}
