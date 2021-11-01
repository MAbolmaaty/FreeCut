package force.freecut.freecut.view_models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewPagerSwipingViewModel extends ViewModel {

    private MutableLiveData<Boolean> mMainViewPagerSwiping =
            new MutableLiveData<>();

    public void setMainViewPagerSwiping(boolean swiping){
        mMainViewPagerSwiping.setValue(swiping);
    }

    public LiveData<Boolean> getMainViewPagerSwiping(){
        return mMainViewPagerSwiping;
    }
}
