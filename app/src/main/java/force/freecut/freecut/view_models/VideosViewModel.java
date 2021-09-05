package force.freecut.freecut.view_models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import force.freecut.freecut.Data.VideoItem;

public class VideosViewModel extends ViewModel {

    private MutableLiveData <ArrayList<VideoItem>> mVideos = new MutableLiveData<>();
    private MutableLiveData <String> mVideoLink = new MutableLiveData<>();
    private MutableLiveData <String> mVideoNumber = new MutableLiveData<>();

    public void setVideos(ArrayList<VideoItem> videos) {
        mVideos.setValue(videos);
    }

    public LiveData<ArrayList<VideoItem>> getVideos(){
        return mVideos;
    }

    public void setVideoLink (String videoLink){
        mVideoLink.setValue(videoLink);
    }

    public LiveData<String>  getVideoLink(){
        return mVideoLink;
    }

    public void setVideoNumber(String number){
        mVideoNumber.setValue(number);
    }

    public LiveData<String> getVideoNumber(){
        return mVideoNumber;
    }
}
