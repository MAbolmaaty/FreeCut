package force.freecut.freecut.Data;

import java.io.File;

public class TrimmedVideo {

    private File mVideoFile;
    private int mTrimProgress;

    public TrimmedVideo(File videoFile, int trimProgress) {
        mVideoFile = videoFile;
        mTrimProgress = trimProgress;
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
}
