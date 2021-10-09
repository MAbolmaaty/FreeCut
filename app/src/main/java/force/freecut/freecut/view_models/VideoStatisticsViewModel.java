package force.freecut.freecut.view_models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class VideoStatisticsViewModel extends ViewModel {

    private final MutableLiveData<Boolean> mVideoStatistics =
            new MutableLiveData<>();

    public void setVideoStatisticsStatus(Boolean status){
        mVideoStatistics.setValue(status);
    }

    public LiveData<Boolean> getVideoStatisticsStatus(){
        return mVideoStatistics;
    }
}
