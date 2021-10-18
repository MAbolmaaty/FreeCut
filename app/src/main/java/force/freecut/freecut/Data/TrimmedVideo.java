package force.freecut.freecut.Data;

import android.view.View;

import java.io.File;

public class TrimmedVideo {

    private File mVideoFile;
    private String mVideoName;
    private String mTrimmingStatus;
    private float mProgressVisibility;
    private int mProgress;
    private float mOptionsVisibility;
    private int mVideoStatus;

    public TrimmedVideo(File videoFile, String videoName,
                        String trimmingStatus, float progressVisibility, int progress,
                        float optionsVisibility, int videoStatus) {
        mVideoFile = videoFile;
        mVideoName = videoName;
        mTrimmingStatus = trimmingStatus;
        mProgressVisibility = progressVisibility;
        mProgress = progress;
        mOptionsVisibility = optionsVisibility;
        mVideoStatus = videoStatus;
    }

    public void setVideoFile(File videoFile) {
        mVideoFile = videoFile;
    }

    public File getVideoFile() {
        return mVideoFile;
    }

    public String getVideoName() {
        return mVideoName;
    }

    public String getTrimmingStatus() {
        return mTrimmingStatus;
    }

    public void setTrimmingStatus(String trimmingStatus) {
        mTrimmingStatus = trimmingStatus;
    }

    public float getProgressVisibility() {
        return mProgressVisibility;
    }

    public void setProgressVisibility(float progressVisibility) {
        mProgressVisibility = progressVisibility;
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;
    }

    public float getOptionsVisibility() {
        return mOptionsVisibility;
    }

    public void setOptionsVisibility(float optionsVisibility) {
        mOptionsVisibility = optionsVisibility;
    }

    public int getVideoStatus() {
        return mVideoStatus;
    }

    public void setVideoStatus(int videoStatus) {
        mVideoStatus = videoStatus;
    }
}
