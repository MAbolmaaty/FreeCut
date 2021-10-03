package force.freecut.freecut.ui.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;

import force.freecut.freecut.Data.TinyDB;
import force.freecut.freecut.Data.Utils;
import force.freecut.freecut.R;

import static android.content.ContentValues.TAG;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static force.freecut.freecut.ui.activities.MainActivity.loadFragment;


public class CutFragmentTwo extends Fragment {
    private static final String TAG = CutFragmentTwo.class.getSimpleName();
    ArrayList<String> listvideos;
    ArrayList<String> Dur_videos;
    boolean err = false;
    File cutfile;
    String type;
    TinyDB tinyDB;
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
    String repeatString = "rr";
    int repST = 0;
    String testFolder;
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

    public CutFragmentTwo() {
        super();
    }

    @SuppressLint("StaticFieldLeak")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.cut_two, container, false);
        Log.d(TAG, "CutFragmentTwo onCreateView");
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
                // tinyDB is a ShredPreferences object
                tinyDB = new TinyDB(getActivity());
                    // tinyDB.getString("cut")
                    // /storage/emulated/0/FreeCut/FreeCut-videoplayback.mp4-50-1
                cutfile = new File(tinyDB.getString("cut"));
                    // seconds : 50
                seconds = tinyDB.getString("seconds");
                    // extension : .mp4
                extension = tinyDB.getString("extension");
                    // start : 1, 3, 4
                start = Integer.parseInt(tinyDB.getString("start"));

                mFileNumber.setText("");
                mFileNumber.setText(getString(R.string.prepare_file_number) + " " + (start + 1));
                    // end : 2
                end = Integer.parseInt(tinyDB.getString("end"));
                    // fault : true
                fault = tinyDB.getString("fault");
                    // cuts : 8
                cuts = Integer.parseInt(tinyDB.getString("cuts"));
                    // plus : 50
                plus = Integer.parseInt(tinyDB.getString("plus"));

                retriever = new MediaMetadataRetriever();
                listvideos = new ArrayList<>();
                Dur_videos = new ArrayList<>();
            }

            @Override
            protected String doInBackground(Void... params) {
                Dur_videos = tinyDB.getListString("durations");
                listvideos = tinyDB.getListString("videos");

                type = MimeTypeMap.getFileExtensionFromUrl(cutfile.getAbsolutePath());
                    // type : mp4-50-1

                File mediaStorageDir = new File(Environment.getExternalStorageDirectory(),
                        "FreeCut Test");

                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.d("App", "failed to create directory");
                    }
                }

                testpath = mediaStorageDir.getAbsolutePath();
                    // testpath: /storage/emulated/0/FreeCut Test

                testFolder = testpath;
                    // testFolder: /storage/emulated/0/FreeCut Test

                list_videos = new String[listvideos.size()];
                list_durations = new long[listvideos.size()];

                    // totalSeconds : 50
                totalSeconds = Integer.parseInt(seconds);

                    // seconds_user : 50
                int seconds_user = totalSeconds % 60;
                    // totalMinutes : 0
                int totalMinutes = totalSeconds / 60;
                    // minutes_user : 0
                int minutes_user = totalMinutes % 60;
                    // hours_user : 0
                int hours_user = totalMinutes / 60;

                if (seconds_user < 10)
                    second_loop1 = "0" + seconds_user;
                else
                    second_loop1 = seconds_user + "";

                    // second_loop1 : 50

                if (minutes_user < 10)
                    minutes_loop1 = "0" + minutes_user;
                else
                    minutes_loop1 = minutes_user + "";

                    // minutes_loop1 : 00

                if (hours_user < 10)
                    hours_loop1 = "0" + hours_user;
                else
                    hours_loop1 = hours_user + "";

                    // hours_loop1 : 00

                for (int i = 0; i < list_videos.length; i++) {
                    list_videos[i] = listvideos.get(i);
                    list_durations[i] = Long.parseLong(Dur_videos.get(i));
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                cut_part_path0 = testpath + "/" + "cut" + cuts++ + "." + extension;
                    // cut_part_path0 : /storage/emulated/0/FreeCut Test/cut8..mp4
                    // cut_part_path0 : /storage/emulated/0/FreeCut Test/cut22..mp4
                    // cut_part_path0 : /storage/emulated/0/FreeCut Test/cut29..mp4
                cut_part_path1 = testpath + "/" + "cut" + cuts++ + "." + extension;
                    // cut_part_path1 : /storage/emulated/0/FreeCut Test/cut9..mp4
                    // cut_part_path1 : /storage/emulated/0/FreeCut Test/cut23..mp4
                    // cut_part_path1 : /storage/emulated/0/FreeCut Test/cut30..mp4
                cut_part_path2 = testpath + "/" + "cut" + cuts++ + "." + extension;
                    // cut_part_path2 : /storage/emulated/0/FreeCut Test/cut10..mp4
                    // cut_part_path2 : /storage/emulated/0/FreeCut Test/cut24..mp4
                    // cut_part_path2 : /storage/emulated/0/FreeCut Test/cut31..mp4
                cut_part_path3 = testpath + "/" + "cut" + cuts++ + "." + extension;
                    // cut_part_path3 : /storage/emulated/0/FreeCut Test/cut11..mp4
                    // cut_part_path3 : /storage/emulated/0/FreeCut Test/cut25..mp4
                    // cut_part_path3 : /storage/emulated/0/FreeCut Test/cut32..mp4
                cut_part_path4 = testpath + "/" + "cut" + cuts++ + "." + extension;
                    // cut_part_path4 : /storage/emulated/0/FreeCut Test/cut12..mp4
                    // cut_part_path4 : /storage/emulated/0/FreeCut Test/cut26..mp4
                    // cut_part_path4 : /storage/emulated/0/FreeCut Test/cut33..mp4
                cut_part_path5 = testpath + "/" + "cut" + cuts++ + "." + "ts";
                    // cut_part_path5 : /storage/emulated/0/FreeCut Test/cut13..ts
                    // cut_part_path5 : /storage/emulated/0/FreeCut Test/cut27..ts
                    // cut_part_path5 : /storage/emulated/0/FreeCut Test/cut34..ts
                cut_part_path6 = testpath + "/" + "cut" + cuts++ + "." + "ts";
                    // cut_part_path6 : /storage/emulated/0/FreeCut Test/cut14..ts
                    // cut_part_path6 : /storage/emulated/0/FreeCut Test/cut28..ts
                    // cut_part_path6 : /storage/emulated/0/FreeCut Test/cut35..ts

                    // start : 1
                index_path = list_videos[start];
                    // index_path:
                    // /storage/emulated/0/FreeCut/FreeCut-videoplayback.mp4-50-1/cut50101.mp4_2.mp4

                    // start : 1
                index_dur = list_durations[start];
                    // index_dur : 50

                    // plus : 50
                    // totalSeconds : 50
                plus = plus - totalSeconds;
                    // plus : 0

                if (plus > 0 && start != list_durations.length - 1) {
                    try {
                        bindEvent();
                    } catch (NullPointerException e) {
                        for (int i = 0; i < list_videos.length; i++) {
                            new File(list_videos[i]).delete();
                        }
                    }
                } else if (plus < 0 && start != list_durations.length - 1) {
                    plus *= -1;
                    try {
                        bindEvent2();
                    } catch (NullPointerException e) {
                        for (int i = 0; i < list_videos.length; i++) {
                            new File(list_videos[i]).delete();
                        }
                        loadFragment(getActivity().getSupportFragmentManager(),
                                TrimFragment.newInstance(null, null),
                                false);
                    }
                    // start : 1
                    // list_durations.length - 1 : 4
                } else if (start == list_durations.length - 1) {
                    final String[] ErrorCommand = {"-loglevel", "error",
                            "-t", "30", "-i", list_videos[start], "-f", "null", "-"};
                    FFmpeg.executeAsync(ErrorCommand, new ExecuteCallback() {
                        @Override
                        public void apply(long executionId, int returnCode) {
                            if (returnCode == RETURN_CODE_SUCCESS) {
                                Log.d("Em-FFMPEG", "CutFragmentTwo : ErrorCommand Success");
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
                                Log.d("Em-FFMPEG", "CutFragmentTwo : ErrorCommand Failed " + returnCode);
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
                    tinyDB.putString("seconds", seconds);
                        // seconds : 50
                    tinyDB.putString("cut", cutfile.getAbsolutePath());
                        // cutfile.getAbsolutePath() : /storage/emulated/0/FreeCut
                        // /FreeCut-videoplayback.mp4-50-1
                    tinyDB.putString("plus", list_durations[end] + "");
                        // end : 2
                        // list_durations[end] : 49
                    tinyDB.putString("pathtest", testFolder);
                        // testFolder : /storage/emulated/0/FreeCut Test
                    tinyDB.putString("start", (start + 1) + "");
                        // start : 1
                        // (start + 1) : 2
                    tinyDB.putString(fault, fault);
                        // fault : true
                    tinyDB.putString("extension", extension);
                        // extension : .mp4
                    tinyDB.putString("Main", "1");
                    tinyDB.putString("end", (end + 1) + "");
                        // end : 2
                        // (end + 1) : 3
                    tinyDB.putString("cuts", cuts + "");
                        // cuts : 15
                    mFileNumber.setText("");
                    mProgress.setText("");
                    mEstimatedTime.setText("");

                    tinyDB.putListString("videos", listvideos);
                    tinyDB.putListString("durations", Dur_videos);

                    Log.d(TAG , "Nav From CutFragmentTwo to CutFragmentOne");
                    Fragment fragment = new CutFragmentOne();
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
        Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent");
        command1();
    }

    private void bindEvent2() throws NullPointerException {
        Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2");
        command7();
    }

    private void command1(){
        final String[] command = {"-ss", hours_loop1 + ":" + minutes_loop1 + ":" + second_loop1,
                "-i", index_path, "-t", plus + "", "-c:v", "libx264", "-c:a", "aac", cut_part_path0};

        long executionId = FFmpeg.executeAsync(command, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command success");
                    mPercentage.setText("30 %");
                    mProgressBar.setProgress(30);
                    command2();
                } else {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command failed " + returnCode);
                }
            }
        });
    }

    private void command2(){
        final String[] command2 = {"-ss", "00:00:00", "-i", index_path, "-y", "-t",
                hours_loop1 + ":" + minutes_loop1 + ":" + second_loop1, "-codec:v", "copy",
                "-codec:a", "copy", cut_part_path1};

        long executionId2 = FFmpeg.executeAsync(command2, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command2");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command2 success");
                    mProgressBar.setProgress(60);
                    mPercentage.setText("60 %");
                    new File(list_videos[start]).delete();
                    new File(cut_part_path1).renameTo(new File(list_videos[start]));
                    command3();
                } else {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command2 failed " + returnCode);
                }
            }
        });
    }

    private void command3(){
        final String[] command3;
        command3 = new String[]{"-ss", "00:00:00", "-i", list_videos[end],
                "-c:v", "libx264", "-c:a", "aac", cut_part_path2};

        long executionId3 = FFmpeg.executeAsync(command3, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command3");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command3 success");
                    mProgressBar.setProgress(90);
                    mPercentage.setText("90 %");
                    command4();
                } else {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command3 failed " + returnCode);
                }
            }
        });
    }

    private void command4(){
        final String[] command4;
        command4 = new String[]{"-i", cut_part_path0, "-c", "copy",
                "-bsf:v", "h264_mp4toannexb", "-f", "mpegts", cut_part_path5};

        long executionId4 = FFmpeg.executeAsync(command4, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command4");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command4 success");
                    mProgressBar.setProgress(92);
                    mPercentage.setText("92 %");
                    command5();
                } else {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command4 failed " + returnCode);
                }
            }
        });
    }

    private void command5(){
        final String[] command5;
        command5 = new String[]{"-i", cut_part_path2, "-c", "copy", "-bsf:v", "h264_mp4toannexb",
                "-f", "mpegts", cut_part_path6};

        long executionId5 = FFmpeg.executeAsync(command5, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command5");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command5 success");
                    mProgressBar.setProgress(94);
                    mPercentage.setText("94 %");
                    command6();
                } else {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command5 failed " + returnCode);
                }
            }
        });
    }

    private void command6(){
        final String[] command6;
        command6 = new String[]{"-i", "concat:" + cut_part_path5 + "|" + cut_part_path6, "-c",
                "copy", "-fflags", "+genpts", "-bsf:a", "aac_adtstoasc", cut_part_path4};

        long executionId6 = FFmpeg.executeAsync(command6, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command6");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command6 success");
                    mProgressBar.setProgress(100);
                    mPercentage.setText("100 %");

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
                    tinyDB.putListString("videos", listvideos);
                    tinyDB.putListString("durations", Dur_videos);
                    tinyDB.putString("start", (start + 1) + "");
                    tinyDB.putString("end", (end + 1) + "");

                    new File(cut_part_path0).delete();
                    new File(cut_part_path1).delete();
                    new File(cut_part_path2).delete();
                    new File(cut_part_path3).delete();
                    new File(cut_part_path4).delete();
                    new File(cut_part_path5).delete();
                    new File(cut_part_path6).delete();
                    Fragment fragment = new CutFragmentTwo();
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.commitAllowingStateLoss();

                } else {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent : command6 failed " + returnCode);
                }
            }
        });
    }

    private void command7(){
        final String[] command = {"-ss", "00:00:00", "-i", list_videos[end], "-t", plus + "",
                "-c:v", "libx264", "-c:a", "aac", cut_part_path0};

        long executionId = FFmpeg.executeAsync(command, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command success");
                    mProgressBar.setProgress(15);
                    mPercentage.setText("15 %");
                    command8();
                } else {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command failed " + returnCode);
                }
            }
        });
    }

    private void command8(){
        final String[] command2;
        command2 = new String[]{"-ss", "00:00:00", "-i", list_videos[start],
                "-c:v", "libx264", "-c:a", "aac", cut_part_path1};

        long executionId2 = FFmpeg.executeAsync(command2, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command2");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command2 success");
                    mProgressBar.setProgress(45);
                    mPercentage.setText("45 %");
                    command9();
                } else {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command2 failed " + returnCode);
                }
            }
        });
    }

    private void command9(){
        final String[] command3;
        command3 = new String[]{"-i", cut_part_path1, "-c", "copy", "-bsf:v", "h264_mp4toannexb",
                "-f", "mpegts", cut_part_path5};

        long executionId3 = FFmpeg.executeAsync(command3, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command3");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command3 success");
                    mProgressBar.setProgress(65);
                    mPercentage.setText("65 %");
                    command10();
                } else {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command3 failed " + returnCode);
                }
            }
        });
    }

    private void command10(){
        final String[] command4;
        command4 = new String[]{"-i", cut_part_path0, "-c", "copy", "-bsf:v", "h264_mp4toannexb",
                "-f", "mpegts", cut_part_path6};

        long executionId4 = FFmpeg.executeAsync(command4, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command4");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command4 success");
                    mProgressBar.setProgress(75);
                    mPercentage.setText("75 %");
                    command11();
                } else {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command4 failed " + returnCode);
                }
            }
        });
    }

    private void command11(){
        final String[] command5;
        command5 = new String[]{"-i", "concat:" + cut_part_path5 + "|" + cut_part_path6, "-c",
                "copy", "-fflags", "+genpts", "-bsf:a", "aac_adtstoasc", cut_part_path4};

        long executionId5 = FFmpeg.executeAsync(command5, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command5");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command5 success");
                    mProgressBar.setProgress(85);
                    mPercentage.setText("85 %");
                    new File(list_videos[start]).delete();
                    new File(cut_part_path4).renameTo(new File(list_videos[start]));
                    command12();
                } else {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command5 failed " + returnCode);
                }
            }
        });
    }

    private void command12(){
        String[] command6 = {"-ss", plus + "", "-i", list_videos[end], "-c:v", "libx264", "-c:a",
                "aac", cut_part_path3};
        long executionId6 = FFmpeg.executeAsync(command6, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command6");
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command6 success");
                    mProgressBar.setProgress(100);
                    mPercentage.setText("100 %");
                    new File(list_videos[end]).delete();
                    new File(cut_part_path3).renameTo(new File(list_videos[end]));
                    tinyDB.putString("seconds", totalSeconds + "");
                    tinyDB.putString("cut", cutfile.getAbsolutePath());
                    tinyDB.putString("cuts", cuts + "");
                    tinyDB.putString("plus", list_durations[end] - plus + "");
                    tinyDB.putString("extension", extension);
                    mFileNumber.setText("");
                    mProgress.setText("");
                    mEstimatedTime.setText("");

                    tinyDB.putString("pathtest", testFolder);
                    tinyDB.putListString("videos", listvideos);
                    tinyDB.putListString("durations", Dur_videos);
                    new File(cut_part_path0).delete();
                    new File(cut_part_path1).delete();
                    new File(cut_part_path2).delete();
                    new File(cut_part_path3).delete();
                    new File(cut_part_path4).delete();
                    new File(cut_part_path5).delete();
                    new File(cut_part_path6).delete();
                    tinyDB.putString("fault", fault);
                    tinyDB.putString("start", (start + 1) + "");
                    tinyDB.putString("end", (end + 1) + "");
                    Fragment fragment = new CutFragmentTwo();
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.commitAllowingStateLoss();
                } else {
                    Log.d("Em-FFMPEG", "CutFragmentTwo : bindEvent2 : command6 failed " + returnCode);
                }
            }
        });
    }
}
