package force.freecut.freecut.Data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import static android.content.ContentValues.TAG;

/**
 * Created by MohamedDev on 2/14/2018.
 */

public class Utils {
    public static String milliSecondsToTimer(long milliseconds) {
        String secondsString = "";
        String minstring = "";
        String houurstring = "";


        //Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours <10) {
            houurstring ="0"+hours + ":";
        }
        else
        {
            houurstring = hours + ":";

        }
        if (minutes <10) {
            minstring ="0"+minutes + ":";
        }
        else
        {
            minstring = minutes + ":";

        }


        // Pre appending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }


        // return timer string
        return houurstring+minstring+secondsString;
    }
    public static String generateList(String[] inputs) {
        File list;
        Writer writer = null;
        try {
            list = File.createTempFile("ffmpeg-list", ".txt");
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)));
            for (String input: inputs) {
                writer.write("file '" + input + "'\n");
                Log.d(TAG, "Writing to list file: file '" + input + "'");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "/";
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        Log.d(TAG, "Wrote list file to " + list.getAbsolutePath());
        return list.getAbsolutePath();
    }
    public static boolean AreAllSame(String[] array)
    {
     for(int i=0;i<array.length;i++)
     {
       if(i!=array.length-1)
       {
         int s=i+1;
          if(!array[i].equals(array[s]))
          {
            return false;

          }
       }

     }
     return true;

    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }


    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    public static long getInternalAvailableSpace() {
        long availableSpace = -1L;
        try {StatFs stat = new StatFs(Environment.getDataDirectory()
                .getPath());
            stat.restat(Environment.getDataDirectory().getPath());
            availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return availableSpace;
    }
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        try {


            String filePath = "";

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {

                    if (Build.VERSION.SDK_INT > 20) {
                        //getExternalMediaDirs() added in API 21
                        File extenal[] = context.getExternalMediaDirs();
                        if (extenal.length > 1) {
                            filePath = extenal[1].getAbsolutePath();
                            filePath = filePath.substring(0, filePath.indexOf("Android")) + split[1];
                        }
                    } else {
                        filePath = "/storage/" + type + "/" + split[1];
                    }
                    return filePath;
                }

            } else if (isDownloadsDocument(uri)) {
                // DownloadsProvider
                final String id = DocumentsContract.getDocumentId(uri);
                //final Uri contentUri = ContentUris.withAppendedId(
                // Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                Cursor cursor = null;
                final String column = "_data";
                final String[] projection = {column};

                try {
                    cursor = context.getContentResolver().query(uri, projection, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        final int index = cursor.getColumnIndexOrThrow(column);
                        String result = cursor.getString(index);
                        cursor.close();
                        return result;
                    }
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
            } else if (DocumentsContract.isDocumentUri(context, uri)) {
                // MediaProvider
                String wholeID = DocumentsContract.getDocumentId(uri);

                // Split at colon, use second item in the array
                String[] ids = wholeID.split(":");
                String id;
                String type;
                if (ids.length > 1) {
                    id = ids[1];
                    type = ids[0];
                } else {
                    id = ids[0];
                    type = ids[0];
                }

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{id};
                final String column = "_data";
                final String[] projection = {column};
                Cursor cursor = context.getContentResolver().query(contentUri,
                        projection, selection, selectionArgs, null);

                if (cursor != null) {
                    int columnIndex = cursor.getColumnIndex(column);

                    if (cursor.moveToFirst()) {
                        filePath = cursor.getString(columnIndex);
                    }
                    cursor.close();
                }
                return filePath;
            } else {
                String[] proj = {MediaStore.Audio.Media.DATA};
                Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                    if (cursor.moveToFirst())
                        filePath = cursor.getString(column_index);
                    cursor.close();
                }


                return filePath;
            }
        }
        catch (Exception e)
        {

        }
        return null;
    }
}
