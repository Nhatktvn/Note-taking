package hcmute.edu.vn.note_taking.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AudioUtils {
    public static String encodeAndSaveAudio(Context context, Uri audioUri) {
        try {
            InputStream inputStream = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                inputStream = Files.newInputStream(Paths.get(audioUri.getPath()));
            } else {
                inputStream = context.getContentResolver().openInputStream(audioUri);
            }
            byte[] audioBytes = new byte[inputStream.available()];
            inputStream.read(audioBytes);
            inputStream.close();

            // Mã hóa dữ liệu âm thanh
            byte[] encodedBytes = Base64.encode(audioBytes, Base64.DEFAULT);

            String fileName = "encoded_audio" + System.currentTimeMillis() + ".txt";
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(encodedBytes);
            fos.close();

            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decodeAudioFromFile(Context context, String fileName) {
        try {
            FileInputStream fis = context.openFileInput(fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
