package force.freecut.freecut.view_models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ToolbarViewModel extends ViewModel {

    private MutableLiveData<Boolean> mDeleteAllMergeVideos = new MutableLiveData<>();
    private MutableLiveData<Boolean> mShowDeleteAll = new MutableLiveData<>();
    private MutableLiveData<String> mToolbarTitle = new MutableLiveData<>();
    private MutableLiveData<Boolean> mBackHome = new MutableLiveData<>();
    private MutableLiveData<Boolean> mBack = new MutableLiveData<>();

    public void deleteAllMergeVideos(boolean delete){
        mDeleteAllMergeVideos.setValue(delete);
    }

    public LiveData<Boolean> listenForDeleteAllMergeVideos(){
        return mDeleteAllMergeVideos;
    }

    public void showDeleteAll(boolean visible){
        mShowDeleteAll.setValue(visible);
    }

    public LiveData<Boolean> deleteAllVisibility(){
        return mShowDeleteAll;
    }

    public void setToolbarTitle(String title){
        mToolbarTitle.setValue(title);
    }

    public LiveData<String> getToolbarTitle(){
        return mToolbarTitle;
    }

    public void showBackHomeButton(boolean visible){
        mBackHome.setValue(visible);
    }

    public LiveData<Boolean> backHomeButtonVisibility(){
        return mBackHome;
    }

    public void showBackButton(boolean visible){
        mBack.setValue(visible);
    }

    public LiveData<Boolean> backButtonVisibility(){
        return mBack;
    }
}
