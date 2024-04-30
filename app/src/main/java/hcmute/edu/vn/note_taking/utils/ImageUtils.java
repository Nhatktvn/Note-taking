package hcmute.edu.vn.note_taking.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ImageUtils {

    public static String saveBitmapToStorage(Context context, Bitmap bitmap) {
        try {
            String fileName = "image_" + System.currentTimeMillis() + ".jpg";
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String encodeAndSaveImage(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Nén ảnh thành JPEG
            byte[] imageBytes = baos.toByteArray();

            // Mã hóa dữ liệu ảnh
            byte[] encodedBytes = Base64.encode(imageBytes, Base64.DEFAULT);

            String fileName = "encoded_image" + System.currentTimeMillis() + ".txt";
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(encodedBytes);
            fos.close();

            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap decodeImageFromFile(Context context, String fileName) {
        try {
            FileInputStream fis = context.openFileInput(fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            byte[] encodedBytes = baos.toByteArray();

            // Giải mã dữ liệu ảnh
            byte[] imageBytes = Base64.decode(encodedBytes, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
