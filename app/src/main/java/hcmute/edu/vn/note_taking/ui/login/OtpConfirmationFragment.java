package hcmute.edu.vn.note_taking.ui.login;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
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
import hcmute.edu.vn.note_taking.activities.MainActivity;
import hcmute.edu.vn.note_taking.utils.Constants;
import hcmute.edu.vn.note_taking.utils.NetworkUtils;

public class OtpConfirmationFragment extends Fragment {
    final int RESEND_OTP_DELAY = 60;
    EditText et_code;
    TextView tv_resend_code;
    TextView tv_back_to_login;
    Button btn_submit_OTP;

    SharedPreferences userSharedPreferences;
    String token = "";
    String email = null;

    Thread resendThread = new Thread(() -> {
        int count = RESEND_OTP_DELAY;
        while (count > 0) {
            try {
                Thread.sleep(1000);
                int finalCount = count;
                requireActivity().runOnUiThread(() -> {
                    tv_resend_code.setText("Resend code in " + finalCount + "(s)");
                });
                count--;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        requireActivity().runOnUiThread(() -> {
            tv_resend_code.setText("Resend code");
        });
    });

    public OtpConfirmationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_otp_confirmation, container, false);
        et_code = view.findViewById(R.id.et_code);
        tv_resend_code = view.findViewById(R.id.tv_resend_code);
        tv_back_to_login = view.findViewById(R.id.tv_back_to_login);
        btn_submit_OTP = view.findViewById(R.id.btn_submit_OTP);

        userSharedPreferences = requireActivity().getSharedPreferences(Constants.USER_SHARED_PREFERENCES, MODE_PRIVATE);

        new Thread(this::request_otp).start();

        email = userSharedPreferences.getString("email_otp", null);


        if (email != null) {

            tv_resend_code.setOnClickListener(v -> new Thread(() -> {
                if (!resendThread.isAlive()) {
                    if (resendThread.getState() == Thread.State.TERMINATED) {
                        resendThread = new Thread(() -> {
                            int count = RESEND_OTP_DELAY;
                            while (count > 0) {
                                try {
                                    Thread.sleep(1000);
                                    int finalCount = count;
                                    requireActivity().runOnUiThread(() -> {
                                        tv_resend_code.setText("Resend code in " + finalCount + "(s)");
                                    });
                                    count--;
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            requireActivity().runOnUiThread(() -> {
                                tv_resend_code.setText("Resend code");
                            });
                        });
                        resendThread.start();
                    } else {
                        resendThread.start();
                    }
                }
            }));

            tv_back_to_login.setOnClickListener(v -> {
                Fragment loginFragment = new LoginFragment();
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_login, loginFragment).commit();
            });

            btn_submit_OTP.setOnClickListener(v -> {
                String otp_code = et_code.getText().toString();
                if (otp_code.length() == 6) {
                    new Thread(() -> {
                        try {
                            String password = userSharedPreferences.getString("password", null);
                            String username = userSharedPreferences.getString("username", null);
                            String type = userSharedPreferences.getString("otp_type", null);
                            String urlString = Constants.getHOST() + "/auth/verify-otp";

                            Map<String, String> params = new HashMap<>();
                            params.put("otp", otp_code);
                            params.put("token", token);
                            params.put("email", email);
                            params.put("password", password);
                            params.put("username", username);
                            params.put("otp_type", type);

                            JSONObject jsonResponse = NetworkUtils.sendHttpRequest(urlString, "POST", params);

                            if (jsonResponse != null) {
                                String success = jsonResponse.getString("status");
                                if (!success.equals("failed")) {
                                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                                    startActivity(intent);
                                    requireActivity().finish();
                                } else {
                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireActivity(), "OTP code is incorrect", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }
                        } catch (JSONException e) {
                            Log.e("Error JSONException: ", e.getMessage());
                        }
                    }).start();
                }
            });
        }
        return view;
    }

    private void request_otp() {
        String type = userSharedPreferences.getString("otp_type", null);
        if (email == null) {
            Toast.makeText(requireActivity(), "Email is not found", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String urlString = Constants.getHOST() + "/auth/create-otp";
            Map<String, String> params = new HashMap<>();
            params.put("email_otp", email);
            params.put("otp_type", type);

            JSONObject jsonResponse = NetworkUtils.sendHttpRequest(urlString, "POST", params);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireActivity(), "Request OTP success with JSON :" + jsonResponse, Toast.LENGTH_SHORT).show();
            });
            if (jsonResponse != null) {
                String success = jsonResponse.getString("status");
                if (success.equals("success")) {
                    token = jsonResponse.getString("token");
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireActivity(), "Got Token: " + token, Toast.LENGTH_SHORT).show();
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireActivity(), "Request OTP failed", Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireActivity(), "Request OTP failed", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (JSONException e) {
            Log.i("Error JSONException: ", e.getMessage());
        }
    }
}