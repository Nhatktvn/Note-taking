package hcmute.edu.vn.note_taking.ui.login;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import hcmute.edu.vn.note_taking.R;
import hcmute.edu.vn.note_taking.activities.LoginActivity;
import hcmute.edu.vn.note_taking.activities.MainActivity;
import hcmute.edu.vn.note_taking.sqlite.NoteTakingOpenHelper;
import hcmute.edu.vn.note_taking.utils.Constants;
import hcmute.edu.vn.note_taking.utils.NetworkUtils;

public class LoginFragment extends Fragment {

    EditText et_password;
    EditText et_email;
    Button btn_login;
    Button btn_register;
    TextView tv_forgot_password;

    NoteTakingOpenHelper openHelper;
    SharedPreferences sharedPreferences;

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        et_email = view.findViewById(R.id.et_email);
        et_password = view.findViewById(R.id.et_password);
        btn_login = view.findViewById(R.id.btn_login);
        btn_register = view.findViewById(R.id.btn_register);
        tv_forgot_password = view.findViewById(R.id.tv_forget_password);

        openHelper = new NoteTakingOpenHelper(requireActivity().getApplicationContext());
        et_email.setHint(Constants.getHOST());

        tv_forgot_password.setOnClickListener(v -> {
            Fragment forgetPasswordFragment = new ForgetPasswordFragment();
            getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_login, forgetPasswordFragment).commit();
        });

        btn_register.setOnClickListener(v -> {
            Fragment registerFragment = new RegisterFragment();
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_login, registerFragment).commit();
        });

        btn_login.setOnClickListener(v -> {
            String email = et_email.getText().toString();
            String password = et_password.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                new Thread(() -> {
                    String url = Constants.getHOST() + "/auth/login";
                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("password", password);
                    JSONObject response = NetworkUtils.sendHttpRequest(url, "POST", params);
                    try {
                        if (response != null) {
                            if (response.getString("status").equals("success")) {
                                sharedPreferences = requireContext().getSharedPreferences(Constants.USER_SHARED_PREFERENCES, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("email", email);
                                editor.apply();
                                Intent intent = new Intent(requireActivity(), MainActivity.class);
                                startActivity(intent);
                                requireActivity().finish();
                            } else {
                                requireActivity().runOnUiThread(() -> {
                                    try {
                                        Toast.makeText(requireActivity(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        } else {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireActivity(), "An error occurred. Please try again later", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });

        return view;
    }
}