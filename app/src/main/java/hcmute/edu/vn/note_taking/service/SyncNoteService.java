package hcmute.edu.vn.note_taking.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import hcmute.edu.vn.note_taking.R;
import hcmute.edu.vn.note_taking.activities.MainActivity;
import hcmute.edu.vn.note_taking.models.Note;
import hcmute.edu.vn.note_taking.sqlite.NoteTakingOpenHelper;
import hcmute.edu.vn.note_taking.utils.Constants;
import hcmute.edu.vn.note_taking.utils.NetworkUtils;

public class SyncNoteService extends Service {

    private static final int NOTIFICATION_ID = 123;
    private static final String NOTIFICATION_CHANNEL_ID = "InternetCheckChannel";
    private static final long INTERVAL = 30000; // Thời gian kiểm tra mạng (30 giây)

    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                checkInternetConnection();
                handler.postDelayed(this, INTERVAL);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        handler.post(runnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Internet Check Service")
                .setContentText("Running")
                .setSmallIcon(R.drawable.smile_wink)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Internet Check Service";
            String description = "Foreground service to check internet connection";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = cm.getActiveNetwork();
        if (activeNetwork != null) {
            NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(activeNetwork);
            if (networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))) {
                SharedPreferences sharedPreferences = getSharedPreferences(Constants.SYNC_SHARED_PREFERENCES, MODE_PRIVATE);
                String syncData = sharedPreferences.getString("syncData", "");
                if (!syncData.equals("")) {
                    String[] noteIds = syncData.split(",");
                    String noteId = noteIds[0];
                    String[] newNoteIds = new String[noteIds.length - 1];
                    System.arraycopy(noteIds, 1, newNoteIds, 0, noteIds.length - 1);
                    if (!noteId.equals("")) {
                        int id = Integer.parseInt(noteId);
                        new Thread(() -> {
                            NoteTakingOpenHelper noteTakingOpenHelper = new NoteTakingOpenHelper(getApplicationContext());
                            Note note = noteTakingOpenHelper.getNoteById(id);
                            if (note != null) {
                                String email = getSharedPreferences(Constants.USER_SHARED_PREFERENCES, MODE_PRIVATE).getString("email", "");
                                JSONObject result = NetworkUtils.sendNoteToServer(getApplicationContext(), note, email);
                                if (result != null) {
                                    try {
                                        String status = result.getString("status");
                                        if (status.equals("success")) {
                                            noteTakingOpenHelper.updateNoteStatus(note.getId());
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.remove("syncData");
                                            editor.apply();
                                        }
                                    } catch (Exception e) {
                                        Log.e("SyncNoteService", e.getMessage());
                                    }
                                }
                            }
                        }).start();
                    }
                    if (newNoteIds.length > 0) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("syncData");
                        editor.apply();
                        editor.putString("syncData", String.join(",", newNoteIds));
                        editor.apply();
                    } else {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("syncData");
                        editor.apply();
                    }
                }
            }
        }
    }


    private void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
