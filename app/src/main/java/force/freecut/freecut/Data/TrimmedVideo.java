package force.freecut.freecut.Data;

import java.io.File;

public class TrimmedVideo {

    private File mVideoFile;
    private int mTrimProgress;
    private String mVideoName;

    public TrimmedVideo(File videoFile, int trimProgress, String videoName) {
        mVideoFile = videoFile;
        mTrimProgress = trimProgress;
        mVideoName = videoName;
    }

    public void setVideoFile(File videoFile) {
        mVideoFile = videoFile;
    }

    public void setTrimProgress(int trimProgress) {
        mTrimProgress = trimProgress;
    }

    public File getVideoFile() {
        return mVideoFile;
    }

    public int getTrimProgress() {
        return mTrimProgress;
    }

    public String getVideoName() {
        return mVideoName;
    }
}
