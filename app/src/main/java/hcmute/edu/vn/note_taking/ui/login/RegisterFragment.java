package hcmute.edu.vn.note_taking.ui.login;

import static android.content.Context.MODE_PRIVATE;
import static hcmute.edu.vn.note_taking.utils.NetworkUtils.sendHttpRequest;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import hcmute.edu.vn.note_taking.R;
import hcmute.edu.vn.note_taking.utils.Constants;

public class RegisterFragment extends Fragment {
    EditText et_email;
    EditText et_username;
    EditText et_password;
    EditText et_confirm_password;

    Button btn_submit_regist;
    TextView tv_back_to_login;

    SharedPreferences userSharedPreferences;
    SharedPreferences settingsSharedPreferences;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        et_email = view.findViewById(R.id.et_email);
        et_username = view.findViewById(R.id.et_username);
        et_password = view.findViewById(R.id.et_password);
        et_confirm_password = view.findViewById(R.id.et_confirm_password);
        btn_submit_regist = view.findViewById(R.id.btn_submit_regist);
        tv_back_to_login = view.findViewById(R.id.tv_back_to_login);

        userSharedPreferences = requireContext().getSharedPreferences(Constants.USER_SHARED_PREFERENCES, MODE_PRIVATE);
        settingsSharedPreferences = requireContext().getSharedPreferences(Constants.SETTINGS_SHARED_PREFERENCES, MODE_PRIVATE);

        tv_back_to_login.setOnClickListener(v -> {
            Fragment fragment = new LoginFragment();
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_login, fragment).commit();
        });

        btn_submit_regist.setOnClickListener(v -> {
            String email = et_email.getText().toString();
            String username = et_username.getText().toString();
            String password = et_password.getText().toString();
            String confirm_password = et_confirm_password.getText().toString();
            if (email.isEmpty()) {
                et_email.setError("Please enter email");
            }
            if (username.isEmpty()) {
                et_username.setError("Please enter username");
            }
            if (password.isEmpty()) {
                et_password.setError("Please enter password");
            }
            if (!password.equals(confirm_password)) {
                et_confirm_password.setError("Password not match");
            }
            if (!email.isEmpty() && !username.isEmpty() && !password.isEmpty() && password.equals(confirm_password))
                new Thread(() -> {
                    String urlString = Constants.getHOST() + "/auth/regist";
                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("username", username);
                    params.put("password", password);
                    JSONObject jsonResponse = sendHttpRequest(urlString, "POST", params);

                    try {
                        assert jsonResponse != null;
                        if (jsonResponse.getString("status").equals("success")) {
                            SharedPreferences.Editor editor = userSharedPreferences.edit();
                            editor.remove("otp_type");
                            editor.remove("email_otp");
                            editor.remove("password");
                            editor.remove("username");
                            editor.apply();
                            editor.putString("email_otp", email);
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.putString("otp_type", "regist");
                            editor.apply();

                            Fragment otpFragment = new OtpConfirmationFragment();
                            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_login, otpFragment).commit();
                        } else {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireActivity(), "Register failed", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (JSONException e) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireActivity(), "Register failed", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
        });

        et_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.charAt(s.length() - 1) == '\n') {
                    s.delete(s.length() - 1, s.length());
                    et_email.requestFocus();
                }
            }
        });

        et_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.charAt(s.length() - 1) == '\n') {
                    s.delete(s.length() - 1, s.length());
                    et_password.requestFocus();
                }
            }
        });

        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.charAt(s.length() - 1) == '\n') {
                    s.delete(s.length() - 1, s.length());
                    et_confirm_password.requestFocus();
                }
            }
        });

        et_confirm_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.charAt(s.length() - 1) == '\n') {
                    s.delete(s.length() - 1, s.length());
                    btn_submit_regist.callOnClick();
                }
            }
        });
        return view;
    }
}