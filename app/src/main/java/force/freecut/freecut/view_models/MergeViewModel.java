package force.freecut.freecut.view_models;

import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MergeViewModel extends ViewModel {

    private MutableLiveData<Bundle> mMergeBundle = new MutableLiveData<>();

    public void setMergeBundle(Bundle mergeBundle){
        mMergeBundle.setValue(mergeBundle);
    }

    public LiveData<Bundle> getMergeBundle(){
        return mMergeBundle;
    }
}
