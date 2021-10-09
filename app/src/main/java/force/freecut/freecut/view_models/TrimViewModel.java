package force.freecut.freecut.view_models;

import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TrimViewModel extends ViewModel {

    private final MutableLiveData<Bundle> mTrimBundle = new MutableLiveData<>();

    public void setTrimBundle(Bundle bundle){
        mTrimBundle.setValue(bundle);
    }

    public LiveData<Bundle> getTrimBundle(){
        return mTrimBundle;
    }
}
