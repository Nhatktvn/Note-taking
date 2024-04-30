package hcmute.edu.vn.note_taking.activities;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.note_taking.models.Note;
import hcmute.edu.vn.note_taking.R;
import hcmute.edu.vn.note_taking.sqlite.NoteTakingOpenHelper;
import hcmute.edu.vn.note_taking.utils.Constants;
import hcmute.edu.vn.note_taking.utils.ImageUtils;
import hcmute.edu.vn.note_taking.utils.NetworkUtils;
import hcmute.edu.vn.note_taking.utils.PermissionUtils;

public class CreateNoteActivity extends AppCompatActivity {

    LinearLayout llImages;
    LinearLayout llVoice;
    Map<Integer, String> mapImages = new HashMap<>();

    Button btnSave;
    Button btnCancel;
    Button btnAddImage;
    ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;
    TextInputEditText etTitle;
    TextInputEditText etTextContent;

    MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_note);

        llImages = findViewById(R.id.llImages);
        llVoice = findViewById(R.id.llVoice);
        etTitle = findViewById(R.id.etTitle);
        etTextContent = findViewById(R.id.etTextContent);


        String voicePath;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String text_content = bundle.getString("text_content");
            if (text_content != null) {
                etTextContent.setText(text_content);
            }
            String[] listImages = bundle.getStringArray("listImages");
            addImagesToView(listImages);
            String voice = bundle.getString("recordingPath");
            if (voice != null) {
                addVoiceToView(voice);
                voicePath = voice;
            } else {
                voicePath = null;
            }
        } else {
            voicePath = null;
        }


        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnAddImage = findViewById(R.id.btnAddImage);
        mediaPlayer = new MediaPlayer();

        pickMultipleMedia =
                registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5), uris -> {
                    // Callback is invoked after the user selects media items or closes the
                    // photo picker.
                    if (!uris.isEmpty()) {
                        ArrayList<String> selectedMedia = new ArrayList<>();
                        for (int i = 0; i < uris.size(); i++) {
                            selectedMedia.add(uris.get(i).toString());
                        }

                        String[] listImages = selectedMedia.toArray(new String[0]);
                        addImagesToView(listImages);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionUtils.hasReadExternalStoragePermission(CreateNoteActivity.this)
                        && PermissionUtils.hasWriteExternalStoragePermission(CreateNoteActivity.this)) {
                    pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                } else {
                    PermissionUtils.requestReadExternalStoragePermission(CreateNoteActivity.this, 1);
                    PermissionUtils.requestWriteExternalStoragePermission(CreateNoteActivity.this, 2);
                    pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(() -> {
                    NoteTakingOpenHelper noteTakingOpenHelper = new NoteTakingOpenHelper(getApplicationContext());
                    String final_title = etTitle.getText().toString();
                    String final_text_content = etTextContent.getText().toString();
                    String[] list_images = mapImages.values().toArray(new String[0]);
                    List<String> list_encoded_images = new ArrayList<>();
                    for (String image : list_images) {
                        list_encoded_images.add(ImageUtils.encodeAndSaveImage(getApplicationContext(), Uri.parse(image)));
                    }
                    String final_list_images = new JSONArray(list_encoded_images).toString();

                    Note newLocalNote = noteTakingOpenHelper.insertNote(final_title, final_text_content, final_list_images, voicePath);
                    if (NetworkUtils.isConnectedToInternet(getApplicationContext())) {
                        String email = getSharedPreferences(Constants.USER_SHARED_PREFERENCES, MODE_PRIVATE).getString("email", "");
                        JSONObject result = NetworkUtils.sendNoteToServer(getApplicationContext(), newLocalNote, email);
                        if (result != null) {
                            try {
                                String status = result.getString("status");
                                if (status.equals("success")) {
                                    noteTakingOpenHelper.updateNoteStatus(newLocalNote.getId());
                                }
                            } catch (Exception e) {
                                Log.e("CreateNoteActivity", e.getMessage());
                            }
                        }
                    } else {
                        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SYNC_SHARED_PREFERENCES, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        String syncData = sharedPreferences.getString("syncData", "");
                        editor.remove("syncData");
                        editor.apply();
                        if (syncData.equals("")) {
                            editor.putString("syncData", "" + newLocalNote.getId());
                        } else {
                            editor.putString("syncData", syncData + "," + newLocalNote.getId());
                        }
                        editor.apply();
                    }

                    finish();
                }).start();
            }
        });
    }

    private void addVoiceToView(String filePath) {
        ConstraintLayout voice_card = (ConstraintLayout) getLayoutInflater().inflate(R.layout.voice_card, null);
        voice_card.setId(View.generateViewId());
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            TextView durationTextView = voice_card.findViewById(R.id.durationTextView);
            durationTextView.setId(View.generateViewId());
//            durationTextView.setText(mediaPlayer.getDuration() / 1000 + ":" + mediaPlayer.getDuration() % 1000 + "s");
            durationTextView.setText(filePath);
            ImageView btnPlay = voice_card.findViewById(R.id.playImageView);
            btnPlay.setId(View.generateViewId());

            btnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        btnPlay.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.baseline_play_circle_outline_24));
                    } else {
                        mediaPlayer.start();
                        btnPlay.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.stop_recording));
                    }
                }
            });
        } catch (Exception e) {
            Log.e("CreateNoteActivity", "Error while adding voice to view: ");
            mediaPlayer.release();
            mediaPlayer = null;
            return;
        }

        llVoice.addView(voice_card);
    }

    private void addImagesToView(String[] listImages) {
        if (listImages != null) {
            for (String image : listImages) {
                ConstraintLayout image_card = (ConstraintLayout) getLayoutInflater().inflate(R.layout.image_card, null);
                image_card.setId(View.generateViewId());

                // Tạo mới một ImageView
                try {
                    ImageView ivImage = image_card.findViewById(R.id.imageView);
                    ivImage.setId(View.generateViewId());
                    Glide.with(this).load(image).centerCrop().into(ivImage);

                    TextView tvImageName = image_card.findViewById(R.id.textViewTitle);
                    tvImageName.setId(View.generateViewId());
                    tvImageName.setText("Image " + (llImages.getChildCount() + 1));

                    // Tạo mới một Button xóa
                    Button btnDelete = image_card.findViewById(R.id.btnRemoveImage);
                    btnDelete.setId(View.generateViewId());
                    btnDelete.setText("Xóa");

                    btnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            llImages.removeView(image_card);
                            mapImages.remove(v.getId());
                        }
                    });

                    mapImages.put(btnDelete.getId(), image);
                } catch (Exception e) {
                    continue;
                }

                llImages.addView(image_card);
            }
        }

    }
}
