package force.freecut.freecut.Data;

import android.view.View;

import java.io.File;

public class TrimVideoModel {

    public enum Mode{
        PLAY, PAUSE
    }

    private File mVideoFile;
    private String mVideoName;
    private String mVideoDuration;
    private String mTrimmingStatus;
    private int mProgress;
    private Mode mVideoMode;

    public TrimVideoModel(File videoFile, String videoName, String videoDuration,
                          String trimmingStatus, int progress, Mode videoMode) {
        mVideoFile = videoFile;
        mVideoName = videoName;
        mVideoDuration = videoDuration;
        mTrimmingStatus = trimmingStatus;
        mProgress = progress;
        mVideoMode = videoMode;
    }

    public void setVideoFile(File videoFile) {
        mVideoFile = videoFile;
    }

    public File getVideoFile() {
        return mVideoFile;
    }

    public void setVideoName(String videoName) {
        mVideoName = videoName;
    }

    public String getVideoName() {
        return mVideoName;
    }

    public void setTrimmingStatus(String trimmingStatus) {
        mTrimmingStatus = trimmingStatus;
    }

    public String getTrimmingStatus() {
        return mTrimmingStatus;
    }


    public void setProgress(int progress) {
        mProgress = progress;
    }

    public int getProgress() {
        return mProgress;
    }

    public void setVideoMode(Mode videoMode) {
        mVideoMode = videoMode;
    }

    public Mode getVideoMode() {
        return mVideoMode;
    }

    public void setVideoDuration(String videoDuration) {
        mVideoDuration = videoDuration;
    }

    public String getVideoDuration() {
        return mVideoDuration;
    }
}
