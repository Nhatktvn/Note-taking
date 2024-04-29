package hcmute.edu.vn.note_taking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import hcmute.edu.vn.note_taking.R;
import hcmute.edu.vn.note_taking.activities.CreateNoteActivity;
import hcmute.edu.vn.note_taking.utils.PermissionUtils;

public class HomeFragment extends Fragment {

    ImageView iv_browse_gallery;
    ImageView iv_take_new_photo;
    ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;

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
                        startActivity(intent);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        iv_browse_gallery = root.findViewById(R.id.iv_browse_gallery);
        iv_take_new_photo = root.findViewById(R.id.iv_take_new_photo);

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
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}