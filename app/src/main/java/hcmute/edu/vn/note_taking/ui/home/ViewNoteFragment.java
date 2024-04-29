package hcmute.edu.vn.note_taking.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.List;

import hcmute.edu.vn.note_taking.R;
import hcmute.edu.vn.note_taking.activities.NoteDetailActivity;
import hcmute.edu.vn.note_taking.models.Note;
import hcmute.edu.vn.note_taking.sqlite.NoteTakingOpenHelper;
import hcmute.edu.vn.note_taking.utils.ImageUtils;

public class ViewNoteFragment extends Fragment {


    LinearLayout ll_note_container;

    List<Note> notes;

    public ViewNoteFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notes = new NoteTakingOpenHelper(requireActivity().getApplicationContext()).getAllNotes();
        for (Note note : notes) {
            Log.e("NoteQuery", "Note ID: " + note.getId());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_view_note, container, false);

        ll_note_container = root.findViewById(R.id.ll_note_container);

        Context context = requireContext();
        for (Note note : notes) {
            // Inflate layout từ tệp layout XML đã định nghĩa trước
            View noteLayout = LayoutInflater.from(context).inflate(R.layout.note_card, null);
            noteLayout.setId(View.generateViewId());
            // Tìm các thành phần trong layout bằng id của chúng
            TextView tvTitle = noteLayout.findViewById(R.id.tv_title);
            TextView tvContent = noteLayout.findViewById(R.id.tv_content);
            TextView tvDate = noteLayout.findViewById(R.id.tv_date);

            tvTitle.setId(View.generateViewId());
            tvContent.setId(View.generateViewId());
            tvDate.setId(View.generateViewId());

            // Sửa đổi nội dung của các thành phần và kiểm tra độ dài của chuỗi
            if (note.getTitle().length() > 2) {
                tvTitle.setText(note.getTitle());
            } else {
                tvTitle.setText("No Title");
            }
            if (note.getText_content().length() > 2) {
                tvContent.setText(note.getText_content());
            } else {
                tvContent.setText("No Content");
            }
            tvDate.setText(note.getCreated_at());

            // Thêm layout đã chỉnh sửa vào ll_note_container

            noteLayout.setOnClickListener(v -> {
                Intent intent = new Intent(context, NoteDetailActivity.class);
                intent.putExtra("note_id", note.getId());
                startActivity(intent);
            });
            ll_note_container.addView(noteLayout);
        }
        return root;
    }
}