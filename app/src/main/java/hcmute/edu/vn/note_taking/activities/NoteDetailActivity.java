package hcmute.edu.vn.note_taking.activities;

import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.note_taking.R;
import hcmute.edu.vn.note_taking.models.Note;
import hcmute.edu.vn.note_taking.sqlite.NoteTakingOpenHelper;

public class NoteDetailActivity extends AppCompatActivity {
    LinearLayout llImages;
    LinearLayout llVoice;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        int noteId = bundle.getInt("note_id");
        Note note = new NoteTakingOpenHelper(getApplicationContext()).getNoteById(noteId);

        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(note.getTitle());

        TextView tv_content = findViewById(R.id.tv_content);
        tv_content.setText(note.getText_content());

        llImages = findViewById(R.id.layout_image);
        llVoice = findViewById(R.id.layout_voice);

        try {
            JSONArray jsonArray = new JSONArray(note.getListImages());
            List<String> images = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                images.add(jsonArray.getString(i));
            }
            String[] imageArray = images.toArray(new String[0]);
            addImagesToView(imageArray);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        addVoiceToView(note.getVoice());

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                    image_card.removeView(btnDelete);

                } catch (Exception e) {
                    continue;
                }

                llImages.addView(image_card);
            }
        }

    }
}
