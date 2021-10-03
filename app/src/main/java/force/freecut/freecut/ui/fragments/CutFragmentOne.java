package force.freecut.freecut.ui.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.util.ArrayList;

import force.freecut.freecut.Data.TinyDB;
import force.freecut.freecut.R;

import static android.content.ContentValues.TAG;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static force.freecut.freecut.ui.activities.MainActivity.loadFragment;

/**
 * Created by MohamedDev on 2/19/2018.
 */

public class CutFragmentOne extends Fragment {
    private static final String TAG = CutFragmentOne.class.getSimpleName();

    ArrayList<String> listvideos;
    ArrayList<String> Dur_videos;
    int begin = 0;
    File cutfile;
    String type;
    boolean err = false;
    TinyDB tinyDB;
    String repeatString = "rr";
    int repST = 0;
    String seconds;
    String testpath;
    String[] list_videos;
    String fault;
    String second_loop1 = "";
    String minutes_loop1 = "";
    String hours_loop1 = "";
    int cuts = 0;
    String cut_part_path0 = "";
    String cut_part_path1 = "";
    String cut_part_path2 = "";
    String cut_part_path3 = "";
    String cut_part_path4 = "";
    String cut_part_path5 = "";
    String cut_part_path6 = "";
    String testFolder;
    String Main = "";
    int start = 0;
    int end = 0;
    int totalSeconds;
    Long index_dur;
    String index_path;
    String extension;
    long plus = 0;
    MediaMetadataRetriever retriever;
    long[] list_durations;

    private ProgressBar mProgressBar;
    private TextView mPercentage;
    private TextView mFileNumber;
    private TextView mProgress;
    private TextView mEstimatedTime;

    public CutFragmentOne() {
        super();
    }

    @SuppressLint("StaticFieldLeak")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.cut_one, container, false);
        Log.d(TAG, "CutFragmentOne onCreateView");
        mPercentage = view.findViewById(R.id.percentage);
        mProgressBar = view.findViewById(R.id.progressBar);
        mFileNumber = view.findViewById(R.id.file);
        mProgress = view.findViewById(R.id.progress);
        mEstimatedTime = view.findViewById(R.id.estimated_time);
        AdView adView = view.findViewById(R.id.adView);

        MobileAds.initialize(getActivity(), "ca-app-pub-6503532425142366~2924525320");
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // Initialize SharedPreference
                tinyDB = new TinyDB(getActivity());
                cutfile = new File(tinyDB.getString("cut"));
                    // cutfile : /storage/emulated/0/FreeCut/FreeCut-video.mp4-[secs]-1
                    // User specified seconds

                seconds = tinyDB.getString("seconds");
                    // seconds : 50

                extension = tinyDB.getString("extension");
                    // extension : .mp4
                start = Integer.parseInt(tinyDB.getString("start"));
                    // start : 0, 2

                mFileNumber.setText("");
                mFileNumber.setText(getString(R.string.prepare_file_number) + " " + (start + 1));

                end = Integer.parseInt(tinyDB.getString("end"));
                    // end : 1, 3

                fault = tinyDB.getString("fault");
                    // fault : true

                cuts = Integer.parseInt(tinyDB.getString("cuts"));
                    // cuts : 1, 15

                Main = tinyDB.getString("Main");
                    // Main : 0, 1

                if (Main.equals("1")) {
                    plus = Integer.parseInt(tinyDB.getString("plus"));
                    // plus : 49
                }

                if (Main.equals("0")) {
                    begin = tinyDB.getInt("begin");
                    // begin : 1
                }

                retriever = new MediaMetadataRetriever();
                listvideos = new ArrayList<String>();
                Dur_videos = new ArrayList<String>();
            }

            @Override
            protected String doInBackground(Void... params) {
                    // cutfile : /storage/emulated/0/FreeCut/FreeCut-video.mp4-[secs]-1
                File directory = new File(cutfile.getAbsolutePath());

                File[] files = directory.listFiles();

                if (Main.equals("0")) {
                    for (int i = 0; i < files.length; i++) {
                        retriever.setDataSource(files[i].getAbsolutePath());
                        String time =
                                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        long timeInSec = Long.parseLong(time) / 1000;
                            // timeInSec : 14, 14, 9, 50, 23
                            // files[i].getParent :
                            // /storage/emulated/0/FreeCut/FreeCut-video.mp4-[secs]-1
                            // files[i].getName() : cut50100.mp4, cut50101.mp4, cut50102.mp4,
                            // cut50103.mp4, cut50104.mp4
                            // begin : 1
                            // extension : .mp4
                        File newFile = new File(files[i].getParent()
                                + "/" + files[i].getName() + "_" + (i + begin) + extension);
                            // newFile :
                            // /storage/emulated/0/FreeCut/FreeCut-video.mp4-50-1/cut50100.mp4_1.mp4
                            // /storage/emulated/0/FreeCut/FreeCut-video.mp4-50-1/cut50101.mp4_2.mp4
                            // /storage/emulated/0/FreeCut/FreeCut-video.mp4-50-1/cut50102.mp4_3.mp4
                            // /storage/emulated/0/FreeCut/FreeCut-video.mp4-50-1/cut50103.mp4_4.mp4
                            // /storage/emulated/0/FreeCut/FreeCut-video.mp4-50-1/cut50104.mp4_5.mp4
                        files[i].renameTo(newFile);

                        listvideos.add(newFile.getAbsolutePath());
                        Dur_videos.add(timeInSec + "");
                    }

                } else {
                    listvideos = tinyDB.getListString("videos");
                    Dur_videos = tinyDB.getListString("durations");
                }

                    // cutfile : /storage/emulated/0/FreeCut/FreeCut-video.mp4-[secs]-1
                type = MimeTypeMap.getFileExtensionFromUrl(cutfile.getAbsolutePath());
                    // type : mp4-[secs]-1

                File mediaStorageDir =
                        new File(Environment.getExternalStorageDirectory(), "FreeCut Test");

                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.d("App", "failed to create directory");
                    }
                }

                testpath = mediaStorageDir.getAbsolutePath();
                testFolder = testpath;
                    // testFolder, testpath : /storage/emulated/0/FreeCut Test

                    //String [] of video paths
                list_videos = new String[listvideos.size()];
                    //long [] of video durations
                list_durations = new long[listvideos.size()];

                totalSeconds = Integer.parseInt(seconds);
                    // totalSeconds : 50

                int seconds_user = totalSeconds % 60;
                    // seconds_user : 50
                int totalMinutes = totalSeconds / 60;
                    // totalMinutes : 0
                int minutes_user = totalMinutes % 60;
                    // minutes_user : 0
                int hours_user = totalMinutes / 60;
                    // hours_user : 0

                if (seconds_user < 10) {
                    second_loop1 = "0" + seconds_user;
                }
                else {
                    second_loop1 = seconds_user + "";
                        // second_loop1 : 50
                }

                if (minutes_user < 10) {
                    minutes_loop1 = "0" + minutes_user;
                        // minutes_loop1 : 00
                }
                else {
                    minutes_loop1 = minutes_user + "";
                }

                if (hours_user < 10) {
                    hours_loop1 = "0" + hours_user;
                        // hours_loop1 : 00
                }
                else {
                    hours_loop1 = hours_user + "";
                }

                for (int i = 0; i < list_videos.length; i++) {
                    list_videos[i] = listvideos.get(i);
                    list_durations[i] = Long.parseLong(Dur_videos.get(i));
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                    // testpath : /storage/emulated/0/FreeCut Test
                    // cuts : 1
                    // extension : .mp4
                cut_part_path0 = testpath + "/" + "cut" + cuts++ + "." + extension;
                    // /storage/emulated/0/FreeCut Test/cut1..mp4
                    // /storage/emulated/0/FreeCut Test/cut15..mp4
                cut_part_path1 = testpath + "/" + "cut" + cuts++ + "." + extension;
                    // /storage/emulated/0/FreeCut Test/cut2..mp4
                    // /storage/emulated/0/FreeCut Test/cut16..mp4
                cut_part_path2 = testpath + "/" + "cut" + cuts++ + "." + extension;
                    // /storage/emulated/0/FreeCut Test/cut3..mp4
                    // /storage/emulated/0/FreeCut Test/cut17..mp4
                cut_part_path3 = testpath + "/" + "cut" + cuts++ + "." + extension;
                    // /storage/emulated/0/FreeCut Test/cut4..mp4
                    // /storage/emulated/0/FreeCut Test/cut18..mp4
                cut_part_path4 = testpath + "/" + "cut" + cuts++ + "." + extension;
                    // /storage/emulated/0/FreeCut Test/cut5..mp4
                    // /storage/emulated/0/FreeCut Test/cut19..mp4
                cut_part_path5 = testpath + "/" + "cut" + cuts++ + "." + "ts";
                    // /storage/emulated/0/FreeCut Test/cut6..ts
                    // /storage/emulated/0/FreeCut Test/cut20..ts
                cut_part_path6 = testpath + "/" + "cut" + cuts++ + "." + "ts";
                    // /storage/emulated/0/FreeCut Test/cut7..ts
                    // /storage/emulated/0/FreeCut Test/cut21..ts

                // start : 0, 2
                index_path = list_videos[start];
                    // index_path : /storage/emulated/0/FreeCut/FreeCut-video.mp4-50-2
                    // /cut50100.mp4_1.mp4
                    // index_path : /storage/emulated/0/FreeCut/FreeCut-video.mp4-50-2
                    // /cut50102.mp4_3.mp4

                // start : 0, 2
                index_dur = list_durations[start];
                    // index_dur : 50, 49

                    // Main : 0, 1
                if (Main.equals("0")) {
                        // start : 0, 2
                        // list_durations[start] : 50, 49
                        // totalSeconds : 50
                    plus = list_durations[start] - totalSeconds;
                        // plus : 0
                } else {
                    // plus : 49
                    // totalSeconds : 50
                    plus = plus - totalSeconds;
                        // plus : -1
                }

                // start : 0 , 2
                // plus : 0, -1
                // list_durations.length : 5
                if (plus > 0 && start != list_durations.length - 1) {
                    Log.d(TAG, "First Condition");
                    try {
                        bindEvent();
                    } catch (Exception e) {
                        for (int i = 0; i < list_videos.length; i++) {
                            new File(list_videos[i]).delete();
                        }
                        e.printStackTrace();
                    }
                    // plus : -1
                    // start : 0 , 2
                    // list_durations.length - 1 : 4
                } else if (plus < 0 && start != list_durations.length - 1) {
                    Log.d(TAG, "Second Condition");
                    plus *= -1;
                    // plus : 1
                    try {
                        bindEvent2();
                    } catch (NullPointerException e) {
                        for (int i = 0; i < list_videos.length; i++) {
                            new File(list_videos[i]).delete();
                        }
                        getActivity().onBackPressed();
                    }
                } else if (start == list_durations.length - 1) {
                    Log.d(TAG, "Third Condition");
                    final String[] ErrorCommand = {"-loglevel", "error", "-t", "30", "-i",
                            list_videos[start], "-f", "null", "-"};
                    long executionId = FFmpeg.executeAsync(ErrorCommand, new ExecuteCallback() {
                        @Override
                        public void apply(long executionId, int returnCode) {
                            if (returnCode == RETURN_CODE_SUCCESS) {
                                Log.d("Em-FFMPEG", "CutFragmentOne : ErrorCommand Success");
                                tinyDB.putString("pathtest", testFolder);
                                tinyDB.putString("seconds", seconds);
                                tinyDB.putString("cut", cutfile.getAbsolutePath());
                                tinyDB.putListString("videos", listvideos);
                                tinyDB.putString("cuts", cuts + "");
                                tinyDB.putString("extension", extension);
                                loadFragment(getActivity().getSupportFragmentManager(),
                                        SuccessFragment.newInstance(null, null),
                                        false);
                            } else {
                                Log.d("Em-FFMPEG", "CutFragmentOne : ErrorCommand Failed " + returnCode);
                                new  File(list_videos[start]).delete();
                                tinyDB.putString("pathtest", testFolder);
                                tinyDB.putString("seconds", seconds);
                                tinyDB.putString("cut", cutfile.getAbsolutePath());
                                tinyDB.putListString("videos", listvideos);
                                tinyDB.putString("cuts", cuts + "");
                                tinyDB.putString("extension", extension);
                                loadFragment(getActivity().getSupportFragmentManager(),
                                        SuccessFragment.newInstance(null, null),
                                        false);
                            }
                        }

                    });
                } else {
                    Log.d(TAG, "Fourth Condition");
                        // tinyDB is a ShredPreferences object
                        // seconds : 50
                    tinyDB.putString("seconds", seconds);
                        // cutfile.getAbsolutePath() :
                        // /storage/emulated/0/FreeCut/FreeCut-videoplayback.mp4-50-1
                    tinyDB.putString("cut", cutfile.getAbsolutePath());
                        // list_durations[end] : 50
                        // end : 1
                    tinyDB.putString("plus", list_durations[end] + "");
                        // testFolder : /storage/emulated/0/FreeCut Test
                    tinyDB.putString("pathtest", testFolder);
                        // (start + 1) : 1
                    tinyDB.putString("start", (start + 1) + "");
                        // fault : true
                    tinyDB.putString("fault", fault);
                        // extension : .mp4
                    tinyDB.putString("extension", extension);
                    mFileNumber.setText("");
                    mProgress.setText("");
                    mEstimatedTime.setText("");
                    tinyDB.putListString("videos", listvideos);
                    tinyDB.putListString("durations", Dur_videos);
                        // end : 1
                        // (end + 1) : 2
                    tinyDB.putString("end", (end + 1) + "");
                        // cuts : 8
                    tinyDB.putString("cuts", cuts + "");

                    Log.d(TAG , "Nav From CutFragmentOne to CutFragmentTwo");
                    Fragment fragment = new CutFragmentTwo();
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.commitAllowingStateLoss();
                }
            }
        }.execute();

        return view;
    }

    private void bindEvent() throws NullPointerException {
        Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent");
        command1();
    }

    private void bindEvent2() throws NullPointerException {
        Log.d(TAG, "CutFragmentOne : bindEvent2");
        command7();
    }

    private void command1(){
        final String[] command = {"-ss", hours_loop1 + ":" + minutes_loop1 + ":" + second_loop1,
                "-i", index_path, "-t", plus + "", "-c:v", "libx264", "-c:a", "aac", cut_part_path0};

        long executionId = FFmpeg.executeAsync(command, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    mPercentage.setText("30 %");
                    mProgressBar.setProgress(30);
                    command2();
                }
            }
        });
    }

    private void command2(){
        final String[] command2 = {"-ss", "00:00:00", "-i", index_path, "-y", "-t",
                hours_loop1 + ":" + minutes_loop1 + ":" + second_loop1,
                "-codec:v", "copy", "-codec:a", "copy", cut_part_path1};

        long executionId2 = FFmpeg.executeAsync(command2, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent : command2");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent : command2 success");
                    mPercentage.setText("60 %");
                    mProgressBar.setProgress(60);
                    new File(list_videos[start]).delete();
                    new File(cut_part_path1).renameTo(new File(list_videos[start]));
                    command3();
                } else {
                    Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent : command2 failed " + returnCode);
                }
            }
        });
    }

    private void command3(){
        final String[] command3 = {"-ss","00:00:00","-i",list_videos[end],
                "-c:v", "libx264", "-c:a", "aac",cut_part_path2};

        long executionId3 = FFmpeg.executeAsync(command3, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent : command3");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent : command3 success");
                    mPercentage.setText("90 %");
                    mProgressBar.setProgress(90);
                    command4();
                } else {
                    Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent : command3 failed " + returnCode);
                }
            }
        });
    }

    private void command4(){
        final String[] command4 = {"-y","-i",cut_part_path0,
                "-c","copy","-bsf:v","h264_mp4toannexb","-f","mpegts",cut_part_path5};

        long executionId4 = FFmpeg.executeAsync(command4, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent : command4");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent : command4 success");
                    mPercentage.setText("92 %");
                    mProgressBar.setProgress(92);
                    command5();
                } else {
                    Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent : command4 failed " + returnCode);
                }
            }
        });
    }

    private void command5(){
        final String[] command5 = {"-y","-i",cut_part_path2,
                "-c","copy","-bsf:v","h264_mp4toannexb","-f","mpegts",cut_part_path6};

        long executionId5 = FFmpeg.executeAsync(command5, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent : command5");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent : command5 success");
                    mPercentage.setText("94 %");
                    mProgressBar.setProgress(94);
                    command6();
                } else {
                    Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent : command5 failed" + returnCode);
                }
            }
        });
    }

    private void command6(){
        final String[] command6 = {"-y","-i","concat:"+cut_part_path5+"|"+cut_part_path6,
                "-c", "copy","-fflags", "+genpts", "-bsf:a", "aac_adtstoasc", cut_part_path4};

        long executionId6 = FFmpeg.executeAsync(command6, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent : command6");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent : command6 success");
                    mPercentage.setText("100 %");
                    mProgressBar.setProgress(100);

                    mFileNumber.setText("");
                    mProgress.setText("");
                    mEstimatedTime.setText("");

                    new File(list_videos[end]).delete();
                    new File(cut_part_path4).renameTo(new File(list_videos[end]));

                    tinyDB.putString("seconds", totalSeconds + "");
                    tinyDB.putString("cut", cutfile.getAbsolutePath());
                    tinyDB.putString("cuts", cuts + "");
                    tinyDB.putString("plus", plus + list_durations[end] + "");
                    tinyDB.putString("extension", extension);
                    tinyDB.putString("pathtest", testFolder);

                    tinyDB.putString("fault", fault);
                    new File(cut_part_path0).delete();
                    new File(cut_part_path1).delete();
                    new File(cut_part_path2).delete();
                    new File(cut_part_path3).delete();
                    new File(cut_part_path4).delete();
                    new File(cut_part_path5).delete();
                    new File(cut_part_path6).delete();
                    tinyDB.putListString("videos", listvideos);
                    tinyDB.putListString("durations", Dur_videos);

                    tinyDB.putString("start", (start + 1) + "");
                    tinyDB.putString("end", (end + 1) + "");
                    Fragment fragment = new CutFragmentTwo();
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.commitAllowingStateLoss();

                } else {
                    Log.d("Em-FFMPEG", "CutFragmentOne : bindEvent : command6 failed " + returnCode);
                }
            }
        });
    }

    private void command7(){
        Log.d(TAG, "CutFragmentOne command 7");

        // list_videos[end] :
        // /storage/emulated/0/FreeCut/FreeCut-videoplayback.mp4-50-1/cut50103.mp4_4.mp4
        // plus : 1
        // cut_part_path0 : /storage/emulated/0/FreeCut Test/cut15..mp4 : 1 sec video

        final String[] command = {"-ss", "00:00:00", "-i", list_videos[end], "-t", plus + "",
                "-c:v", "libx264", "-c:a", "aac", cut_part_path0};

        long executionId = FFmpeg.executeAsync(command, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d(TAG, "CutFragmentOne command 7 success");
                    mPercentage.setText("15 %");
                    mProgressBar.setProgress(15);
                    command8();
                } else {
                    Log.d(TAG, "CutFragmentOne command 7 fail");
                }
            }
        });
    }

    private void command8(){
        Log.d(TAG, "CutFragmentOne command 8");

        // start : 2
        // list_videos[start] :
        // /storage/emulated/0/FreeCut/FreeCut-videoplayback.mp4-50-1/cut50102.mp4_3.mp4
        // cut_part_path1 : /storage/emulated/0/FreeCut Test/cut16..mp4 : same video

        final String[] command2;
        command2 = new String[]{"-ss", "00:00:00", "-i", list_videos[start],
                "-c:v", "libx264", "-c:a", "aac", cut_part_path1};

        long executionId2 = FFmpeg.executeAsync(command2, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d(TAG, "CutFragmentOne command 8 success");
                    mPercentage.setText("45 %");
                    mProgressBar.setProgress(45);
                    command9();
                } else {
                    Log.d(TAG, "CutFragmentOne command 8 fail");
                }
            }
        });
    }

    private void command9(){
        Log.d(TAG, "CutFragmentOne command 9");

        // cut_part_path1 : /storage/emulated/0/FreeCut Test/cut16..mp4 : 49 sec
        // cut_part_path5 : /storage/emulated/0/FreeCut Test/cut20.ts same as cut16 : 49 sec

        final String[] command3;
        command3 = new String[]{"-y", "-i", cut_part_path1, "-c", "copy",
                "-bsf:v", "h264_mp4toannexb", "-f", "mpegts", cut_part_path5};

        long executionId3 = FFmpeg.executeAsync(command3, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d(TAG, "CutFragmentOne command 9 success");
                    mPercentage.setText("65 %");
                    mProgressBar.setProgress(65);
                    command10();
                } else {
                    Log.d(TAG, "CutFragmentOne command 9 fail");
                }
            }
        });
    }

    private void command10(){
        Log.d(TAG, "CutFragmentOne command 10");

        // cut_part_path0 : /storage/emulated/0/FreeCut Test/cut15..mp4 : 1 sec video
        // cut_part_path6 : /storage/emulated/0/FreeCut Test/cut21.ts : same 1 sec video

        final String[] command4;
        command4 = new String[]{"-y", "-i", cut_part_path0, "-c", "copy", "-bsf:v",
                "h264_mp4toannexb", "-f", "mpegts", cut_part_path6};

        long executionId4 = FFmpeg.executeAsync(command4, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d(TAG, "CutFragmentOne command 10 success");
                    mPercentage.setText("75 %");
                    mProgressBar.setProgress(75);
                    command11();
                } else {
                    Log.d(TAG, "CutFragmentOne command 10 fail");
                }
            }
        });
    }

    private void command11(){
        Log.d(TAG, "CutFragmentOne command 11");

        // cut_part_path5 : /storage/emulated/0/FreeCut Test/cut20.ts same as cut16 : 49 sec
        // cut_part_path6 : /storage/emulated/0/FreeCut Test/cut21.ts : same 1 sec video
        // cut_part_path4 : /storage/emulated/0/FreeCut Test/cut19..mp4 : deleted
        // list_videos[start] :
        // /storage/emulated/0/FreeCut/FreeCut-videoplayback.mp4-50-1/cut50102.mp4_3.mp4 : 50 sec

        final String[] command5;
        command5 = new String[]{"-y", "-i", "concat:" + cut_part_path5 + "|" + cut_part_path6,
                "-c", "copy", "-fflags", "+genpts", "-bsf:a", "aac_adtstoasc", cut_part_path4};

        long executionId5 = FFmpeg.executeAsync(command5, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d(TAG, "CutFragmentOne command 11 success");
                    mPercentage.setText("85 %");
                    mProgressBar.setProgress(85);
                    new File(list_videos[start]).delete();
                    new File(cut_part_path4).renameTo(new File(list_videos[start]));
                    command12();
                } else {
                    Log.d(TAG, "CutFragmentOne command 11 fail");
                }
            }
        });
    }

    private void command12(){
        Log.d(TAG, "CutFragmentOne command 12");

        // plus : 1
        // end : 3
        // list_videos[end] :
        // /storage/emulated/0/FreeCut/FreeCut-videoplayback.mp4-50-1/cut50103.mp4_4.mp4 : 49 sec
        // cut_part_path3 : /storage/emulated/0/FreeCut Test/cut18..mp4 : same video

        String[] command6 = {"-ss", plus + "", "-i", list_videos[end], "-c:v", "libx264", "-c:a",
                "aac", cut_part_path3};

        long executionId6 = FFmpeg.executeAsync(command6, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d(TAG, "CutFragmentOne command 12 success");
                    mPercentage.setText("100 %");
                    mProgressBar.setProgress(100);
                    new File(list_videos[end]).delete();
                    if (new File(cut_part_path3).renameTo(new File(list_videos[end]))) {

                    } else {
                        for (int i = 0; i < list_videos.length; i++) {
                            new File(list_videos[i]).delete();
                        }
                        loadFragment(getActivity().getSupportFragmentManager(),
                                SuccessFragment.newInstance(null, null),
                                false);
                    }

                        // totalSeconds : 50
                    tinyDB.putString("seconds", totalSeconds + "");
                        // cutfile.getAbsolutePath() :
                        // /storage/emulated/0/FreeCut/FreeCut-videoplayback.mp4-50-1
                    tinyDB.putString("cut", cutfile.getAbsolutePath());
                        // cuts : 22
                    tinyDB.putString("cuts", cuts + "");
                        // end : 3
                        // plus : 1
                        // list_durations[end] - plus : 49
                    tinyDB.putString("plus", list_durations[end] - plus + "");
                        // extension : .mp4
                    tinyDB.putString("extension", extension);
                    mFileNumber.setText("");
                    mProgress.setText("");

                        // testFolder : /storage/emulated/0/FreeCut Test
                    tinyDB.putString("pathtest", testFolder);

                    tinyDB.putListString("videos", listvideos);
                    tinyDB.putListString("durations", Dur_videos);

                    // cut_part_path0 : /storage/emulated/0/FreeCut Test/cut15..mp4
                    new File(cut_part_path0).delete();
                    // cut_part_path1 : /storage/emulated/0/FreeCut Test/cut16..mp4
                    new File(cut_part_path1).delete();
                    // cut_part_path2 : /storage/emulated/0/FreeCut Test/cut17..mp4
                    new File(cut_part_path2).delete();
                    // cut_part_path3 : /storage/emulated/0/FreeCut Test/cut18..mp4
                    new File(cut_part_path3).delete();
                    // cut_part_path4 : /storage/emulated/0/FreeCut Test/cut19..mp4
                    new File(cut_part_path4).delete();
                    // cut_part_path5 : /storage/emulated/0/FreeCut Test/cut20.ts
                    new File(cut_part_path5).delete();
                    // cut_part_path6 : /storage/emulated/0/FreeCut Test/cut21.ts
                    new File(cut_part_path6).delete();

                    // fault : true
                    tinyDB.putString("fault", fault);
                    // (start + 1) : 3
                    tinyDB.putString("start", (start + 1) + "");
                    // (end + 1) : 4
                    tinyDB.putString("end", (end + 1) + "");

                    Log.d(TAG, "Nav From CutFragmentOne to CutFragmentTwo through command 12");
                    Fragment fragment = new CutFragmentTwo();
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.commitAllowingStateLoss();
                } else {
                    Log.d(TAG, "CutFragmentOne command 12 fail");
                }
            }
        });
    }
}
