package com.example.orchestrationSampleApp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;


import com.exotel.verification.ConfigBuilder;
import com.exotel.verification.ExotelVerification;
import com.exotel.verification.Timer;
import com.exotel.verification.TimerListener;
import com.exotel.verification.VerificationListener;
import com.exotel.verification.contracts.VerificationFailed;
import com.exotel.verification.contracts.VerificationStart;
import com.exotel.verification.contracts.VerificationSuccess;
import com.exotel.verification.exceptions.ConfigBuilderException;
import com.exotel.verification.exceptions.PermissionNotGrantedException;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {
    private final String LOGGING_TAG = "OrchestrationSampleApp";
    private static String ACCOUNT_SID="Exotel";
    private static String MASTER_TOKEN="qiwajamerabo";
    private static String ID="7a3a8bd5a3d4a74f6c1cd73d246916cd";
    private static String MASTER_KEY="afa8dcfc557748a89471530cc4effb53";
    private TextView timer;
    ExotelVerification eVerification;
    public static String mechanism = "";

    class verifyListener implements VerificationListener {
        ImageView ivtick = (ImageView) findViewById(R.id.imageViewTick);
        ImageView ivcross = (ImageView) findViewById(R.id.imageViewCross);
        TextView textView = (TextView) findViewById(R.id.verificationResult);
        Button button = (Button) findViewById(R.id.verifyButton);
        GifImageView gifImageView = (GifImageView) findViewById(R.id.loadingCircles);
        Button verifyOtpButton = (Button) findViewById(R.id.verifyOtp);
        EditText otpEditText = (EditText) findViewById(R.id.otpField);

        public void onVerificationStarted(VerificationStart verificationStart) {
            mechanism = verificationStart.getVerificationType();
            if(mechanism == "smsotp"){
                otpEditText.setVisibility(View.VISIBLE);
                verifyOtpButton.setVisibility(View.VISIBLE);
            }
            else{
                otpEditText.setVisibility(View.GONE);
                verifyOtpButton.setVisibility(View.GONE);
            }
            timer.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), "Verification started.", Toast.LENGTH_SHORT).show();
            Log.d(LOGGING_TAG, "Starting time: " + System.currentTimeMillis());
        }

        public void onVerificationSuccess(VerificationSuccess verificationSuccess) {
            Log.d(LOGGING_TAG, "onVerificationSuccess: "+verificationSuccess.getRequestID());
            gifImageView.setVisibility(View.INVISIBLE);
            button.setVisibility(View.VISIBLE);
            ivtick.setVisibility(View.VISIBLE);
            timer.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Verification successful!", Toast.LENGTH_LONG).show();
            textView.setText("Successful!");
        }

        public void onVerificationFailed(VerificationFailed verificationFailed) {
            if(verificationFailed.verificationEnded() == true){
                Log.d(LOGGING_TAG, "onVerificationFailed: " + verificationFailed.getRequestID() + " " + verificationFailed.getErrorCode() + " " + verificationFailed.getErrorMessage() + " " + verificationFailed.getMiscData());
                gifImageView.setVisibility(View.INVISIBLE);
                button.setVisibility(View.VISIBLE);
                ivcross.setVisibility(View.VISIBLE);
                timer.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Verification Failed!", Toast.LENGTH_LONG).show();
                textView.setText("Couldn't verify!");
            }
            Toast.makeText(getApplicationContext(), verificationFailed.getErrorMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timer = (TextView) findViewById(R.id.timer);

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.ANSWER_PHONE_CALLS,
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        try {
            final ImageView ivtick = (ImageView) findViewById(R.id.imageViewTick);
            final ImageView ivcross = (ImageView) findViewById(R.id.imageViewCross);
            final TextView textView = (TextView) findViewById(R.id.verificationResult);
            final Button button = (Button) findViewById(R.id.verifyButton);
            final EditText editText = (EditText) findViewById(R.id.phoneNumber);
            final GifImageView gifImageView = (GifImageView) findViewById(R.id.loadingCircles);
            final Spinner spinner = (Spinner) findViewById(R.id.spinner);
            final Button verifyOtpButton = (Button) findViewById(R.id.verifyOtp);
            final EditText otpEditText = (EditText) findViewById(R.id.otpField);
            final EditText appIdEditText = (EditText) findViewById(R.id.appid);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.country_arrays, R.layout.spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            timer.setVisibility(View.GONE);
            verifyOtpButton.setVisibility(View.GONE);
            otpEditText.setVisibility(View.GONE);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String appId = appIdEditText.getText().toString();
                    if(appId!=null && !appId.isEmpty()){
                        ID=appId;
                    }
                    initializeVerification();
                    String phoneNumber = spinner.getSelectedItem().toString() + editText.getText().toString();
                    try {
                        if(eVerification != null) {
                            String[] vars = new String[]{"Zafar",""};
                            button.setVisibility(View.INVISIBLE);
                            ivcross.setVisibility(View.INVISIBLE);
                            ivtick.setVisibility(View.INVISIBLE);
                            textView.setText("");
                            gifImageView.setVisibility(View.VISIBLE);
                            eVerification.startOrchestrationVerification(new verifyListener(), phoneNumber, 10,vars);
                        } else {
                            Toast.makeText(MainActivity.this,"Not initialized properly. Check permissions.",Toast.LENGTH_SHORT).show();
                            return;
                        }

                    } catch (Exception e) {
                        Log.e(LOGGING_TAG, "Exception: " + e.getMessage());
                    }
                    // Check if no view has focus:
                    View view = getCurrentFocus();
                    if (view != null) {
                        // Hide soft keyboard when clicked
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                }
            });
            verifyOtpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    calling sdk with otp enter by user
                    eVerification.verifyOtp(otpEditText.getText().toString());
                }
            });
        } catch (Exception e) {
            Log.e(LOGGING_TAG, "onCreate: Exception occured " + e.getMessage());
        }

        Timer customTimer = new Timer();

        customTimer.setTimerListener(new TimerListener(){
            @Override
            public void getTimerTick(long time){
                String text = mechanism == "notp"? "Please expect the verification call in ":"Please expect the sms in ";
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("android.provider.Telephony.SMS_RECEIVED"));
                timer.setText(text +String.valueOf(time/1000)+" seconds.");
            }
        });
    }

    private void initializeVerification() {
        try {
            eVerification = new ExotelVerification(new ConfigBuilder(ID,MASTER_KEY,MASTER_TOKEN,ACCOUNT_SID,getApplicationContext()).Build());
        } catch (PermissionNotGrantedException vPNGE) {
            Log.d(LOGGING_TAG, "initializeVerification: permission not granted exception: " + vPNGE.getPermission());
            Log.d(LOGGING_TAG, "initializeVerification: "+vPNGE.getPermission());
            //Try initializing again after 3 seconds
            (new android.os.Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    initializeVerification();
                }
            }, 3000);

        } catch (ConfigBuilderException cBE) {
            Log.d(LOGGING_TAG, "initializeVerification: ClientBuilder Exception!");
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}