package hcmute.edu.vn.note_taking.ui.login;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import hcmute.edu.vn.note_taking.R;
import hcmute.edu.vn.note_taking.utils.Constants;

public class ForgetPasswordFragment extends Fragment {
    EditText et_email;
    TextView tv_back_to_login;
    Button btn_submit_reset;
    SharedPreferences userSharedPreferences;

    public ForgetPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_forget_password, container, false);

        et_email = view.findViewById(R.id.et_email);
        tv_back_to_login = view.findViewById(R.id.tv_back_to_login);
        btn_submit_reset = view.findViewById(R.id.btn_submit_reset);

        userSharedPreferences = requireContext().getSharedPreferences(Constants.USER_SHARED_PREFERENCES, MODE_PRIVATE);

        tv_back_to_login.setOnClickListener(v -> {
            Fragment loginFragment = new LoginFragment();
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_login, loginFragment).commit();
        });

        btn_submit_reset.setOnClickListener(v -> {
            String email = et_email.getText().toString();
            if (email.isEmpty()) {
                et_email.setError("Email is required");
                et_email.requestFocus();
                return;
            }

            SharedPreferences.Editor editor = userSharedPreferences.edit();
            editor.remove("otp_type");
            editor.remove("email_otp");
            editor.remove("password");
            editor.apply();
            editor.putString("email_otp", email);
            editor.putString("otp_type", "reset_password");
            editor.apply();

            Fragment otpFragment = new OtpConfirmationFragment();
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_login, otpFragment).commit();
        });

        // Inflate the layout for this fragment
        return view;
    }
}