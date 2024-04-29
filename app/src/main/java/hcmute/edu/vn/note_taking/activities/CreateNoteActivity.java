package hcmute.edu.vn.note_taking.activities;

import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

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
    Map<Integer, String> mapImages = new HashMap<>();

    Button btnSave;
    Button btnCancel;
    Button btnAddImage;
    ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;
    TextInputEditText etTitle;
    TextInputEditText etTextContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_note);

        llImages = findViewById(R.id.llImages);
        etTitle = findViewById(R.id.etTitle);
        etTextContent = findViewById(R.id.etTextContent);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String text_content = bundle.getString("text_content");
            String[] listImages = bundle.getStringArray("listImages");
            addImagesToView(listImages);
        }

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnAddImage = findViewById(R.id.btnAddImage);

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

                    Note newLocalNote = noteTakingOpenHelper.insertNote(final_title, final_text_content, final_list_images, null);
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
                    }
                    finish();
                }).start();
            }
        });
    }

    private void addImagesToView(String[] listImages) {
        if (listImages != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;

            int newWidth = (int) (screenWidth * 0.4);
            int newHeight = (int) (screenHeight * 0.4);

            for (String image : listImages) {
                // Tạo mới một LinearLayout ngang
                LinearLayout llHorizontal = new LinearLayout(this);
                llHorizontal.setOrientation(LinearLayout.HORIZONTAL);
                llHorizontal.setId(View.generateViewId());
                llHorizontal.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

                // Tạo mới một ImageView
                ImageView ivImage = new ImageView(this);
                ivImage.setId(View.generateViewId());
                ivImage.setLayoutParams(new LinearLayout.LayoutParams(newWidth, newHeight));
                Glide.with(this).load(image).centerCrop().into(ivImage);

                // Thêm ImageView vào LinearLayout ngang
                llHorizontal.addView(ivImage);

                // Tạo mới một Button xóa
                Button btnDelete = new Button(this);
                btnDelete.setId(View.generateViewId());
                btnDelete.setText("Xóa");

                // Thêm sự kiện click cho Button xóa
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Loại bỏ LinearLayout cha của Button
                        llImages.removeView((View) v.getParent());
                        mapImages.remove(((View) v.getParent()).getId());
                    }
                });

                // Thêm Button vào LinearLayout ngang
                llHorizontal.addView(btnDelete);

                mapImages.put(llHorizontal.getId(), image);

                // Thêm LinearLayout ngang vào LinearLayout cha
                llImages.addView(llHorizontal);
            }
        }

    }
}
