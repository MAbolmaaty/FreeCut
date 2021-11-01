package force.freecut.freecut.Data;

import java.io.File;

public class MergeVideoModel {

    public enum Mode {
        PLAY, PAUSE
    }

    private File mVideoFile;
    private String mVideoName;
    private String mVideoDuration;
    private Mode mVideoMode;
    private int mType;

    public MergeVideoModel(File videoFile, String videoName, String videoDuration, Mode videoMode,
                           int type) {
        mVideoFile = videoFile;
        mVideoName = videoName;
        mVideoDuration = videoDuration;
        mVideoMode = videoMode;
        this.mType = type;
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

    public void setType(int type) {
        mType = type;
    }

    public int getType() {
        return mType;
    }
}
