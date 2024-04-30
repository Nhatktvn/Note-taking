package hcmute.edu.vn.note_taking.ui.home;

import android.content.ContextWrapper;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import hcmute.edu.vn.note_taking.R;
import hcmute.edu.vn.note_taking.activities.CameraActivity;
import hcmute.edu.vn.note_taking.activities.CreateNoteActivity;
import hcmute.edu.vn.note_taking.service.SyncNoteService;
import hcmute.edu.vn.note_taking.utils.PermissionUtils;

public class HomeFragment extends Fragment {

    private final int MICROPHONE_PERMISSION_REQUEST_CODE = 300;
    ImageView iv_browse_gallery;
    ImageView iv_take_new_photo;
    ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;

    LinearLayout ll_recording;
    ImageView btnStopRecording;

    ImageView btnRecord;

    MediaRecorder mediaRecorder;
    EditText editTextContent;

    Button buttonSubmitTextNote;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

                        Intent intent = new Intent(requireActivity(), CreateNoteActivity.class);
                        intent.putExtra("listImages", listImages);
                        intent.putExtra("text_content", editTextContent.getText().toString());
                        startActivity(intent);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
        mediaRecorder = new MediaRecorder();
        requireActivity().startService(new Intent(requireContext(), SyncNoteService.class));
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        iv_browse_gallery = root.findViewById(R.id.iv_browse_gallery);
        iv_take_new_photo = root.findViewById(R.id.iv_take_new_photo);
        ll_recording = root.findViewById(R.id.ll_recording);
        btnRecord = root.findViewById(R.id.btn_record);
        btnStopRecording = root.findViewById(R.id.btnStopRecording);
        editTextContent = root.findViewById(R.id.editTextContent);
        buttonSubmitTextNote = root.findViewById(R.id.buttonSubmitTextNote);

        if (iv_browse_gallery != null) {
            iv_browse_gallery.setOnClickListener(v -> {
                if (PermissionUtils.hasReadExternalStoragePermission(requireActivity())
                        && PermissionUtils.hasWriteExternalStoragePermission(requireActivity())) {
                    pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                } else {
                    PermissionUtils.requestReadExternalStoragePermission(requireActivity(), 1);
                    PermissionUtils.requestWriteExternalStoragePermission(requireActivity(), 2);
                    pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                }
            });
        }

        iv_take_new_photo.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), CameraActivity.class);
            startActivity(intent);
        });
        buttonSubmitTextNote.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), CreateNoteActivity.class);
            intent.putExtra("text_content", editTextContent.getText().toString());
            startActivity(intent);
        });

        AtomicReference<String> filePath = new AtomicReference<>("");

        btnRecord.setOnClickListener(v -> {
            // Xử lý sự kiện ấn vào nút ghi âm ở đây
            btnRecord.setVisibility(View.GONE);
            ll_recording.setVisibility(View.VISIBLE);
            if (isMicrophonePresent()) {
                getMicrophonePermission();
                try {
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    filePath.set(getRecordingFilePath());
                    mediaRecorder.setOutputFile(filePath.get());
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        btnStopRecording.setOnClickListener(v -> {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            ll_recording.setVisibility(View.GONE);
            btnRecord.setVisibility(View.VISIBLE);

            Intent intent = new Intent(requireActivity(), CreateNoteActivity.class);
            intent.putExtra("text_content", editTextContent.getText().toString());
            intent.putExtra("recordingPath", filePath.get());
            startActivity(intent);
        });

        return root;
    }

    private boolean isMicrophonePresent() {
        return requireActivity().getPackageManager().hasSystemFeature("android.hardware.microphone");
    }

    private void getMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_REQUEST_CODE);
        }
    }

    private String getRecordingFilePath() {
        try {
            // Lấy thư mục lưu trữ của ứng dụng
            File storageDirectory = requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            if (storageDirectory != null) {
                // Tạo tệp mới trong thư mục lưu trữ của ứng dụng
                File file = new File(storageDirectory, "recording" + System.currentTimeMillis() + ".3gp");
                file.createNewFile();
                return file.getPath();
            } else {
                // Thư mục lưu trữ không khả dụng, xử lý lỗi hoặc thông báo cho người dùng
                return null;
            }
        } catch (IOException e) {
            // Xử lý ngoại lệ nếu có lỗi xảy ra trong quá trình tạo tệp mới
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}