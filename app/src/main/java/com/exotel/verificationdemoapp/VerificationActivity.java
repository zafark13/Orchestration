/*
 *  Copyright 2018 Exotel Techcom Pvt Ltd
 */

package com.exotel.verificationdemoapp;


import static com.exotel.verification.constant.Constants.OTP_BROADCAST;
import static com.exotel.verification.constant.Constants.OTP_KEY;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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

import com.crashlytics.android.Crashlytics;
import com.exotel.verification.AppSettings;
import com.exotel.verification.ExoverifyApp;
import com.exotel.verification.OrchestrationAppSettings;
import com.exotel.verification.verification_apps.VerificationApp;
import com.exotel.verification.VerificationParams;
import com.exotel.verification.creds.Credentials;
import com.exotel.verification.exposed_interfaces.OtpParser;
import com.exotel.verification.exposed_interfaces.TimerListener;
import com.exotel.verification.VerificationListener;
import com.exotel.verification.VerificationType;
import com.exotel.verification.VerificationDetail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.fabric.sdk.android.Fabric;
import pl.droidsonroids.gif.GifImageView;

public class VerificationActivity extends AppCompatActivity {
    private final String LOGGING_TAG = "VerificatrixDemoApp";
//    for notp flow
//    private static String ACCOUNT_SID="";
//    private static String SECRET="";
//    private static String ID="";

//    for sms flow
//    private static String ACCOUNT_SID="";
//    private static String SECRET="";
//    private static String ID="";

//    for orchestration flow
    private static String ACCOUNT_SID="";
    private static String MASTER_TOKEN="";
    private static String ID= "";
    private static String MASTER_KEY="";
    private TextView timer;

    public static VerificationType mechanism;

    class Otp implements OtpParser{

        @Override
        public String parseOtpFromMessage(String message) {
            String pattern = "\\d+";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(message);

            if (m.find()) {
                String otp = m.group(0);
                return otp;
            }
            return "";
        }
    }

    class Timer implements TimerListener{

        @Override
        public void getTimerTick(final long time) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String text = mechanism == VerificationType.NOTP? "Please expect the verification call in ":"Please expect the sms in ";
                    timer.setText(text + time / 1000 +" seconds.");
                }
            });
        }
    }

    class verifyListener implements VerificationListener {
        ImageView ivtick = (ImageView) findViewById(R.id.imageViewTick);
        ImageView ivcross = (ImageView) findViewById(R.id.imageViewCross);
        TextView textView = (TextView) findViewById(R.id.verificationResult);
        Button button = (Button) findViewById(R.id.verifyButton);
        GifImageView gifImageView = (GifImageView) findViewById(R.id.loadingCircles);
        Button verifyOtpButton = (Button) findViewById(R.id.verifyOtp);
        EditText otpEditText = (EditText) findViewById(R.id.otpField);

        public void onVerificationStarted(final VerificationDetail verificationStart) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mechanism = verificationStart.getVerificationType();
                    if(mechanism == VerificationType.SMSOTP){
                        otpEditText.setVisibility(View.VISIBLE);
                        verifyOtpButton.setVisibility(View.VISIBLE);
                    }
                    else{
                        otpEditText.setVisibility(View.GONE);
                        verifyOtpButton.setVisibility(View.GONE);
                    }
                    timer.setVisibility(View.VISIBLE);
                    Log.d(LOGGING_TAG, "onVerificationStarted: "+verificationStart.getVerificationId() +" "+verificationStart.getVerificationType()+ " "+verificationStart.isLastMechanism()+" "+verificationStart.getVerificationError());
                    Toast.makeText(getApplicationContext(), "Verification started.", Toast.LENGTH_SHORT).show();
                    Log.d(LOGGING_TAG, "Starting time: " + System.currentTimeMillis());
                }
            });
        }

        public void onVerificationSuccess(final VerificationDetail verificationSuccess) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(LOGGING_TAG, "onVerificationSuccess: "+verificationSuccess.getVerificationId() +" "+verificationSuccess.getVerificationType()+ " "+verificationSuccess.isLastMechanism()+" "+verificationSuccess);
                    gifImageView.setVisibility(View.INVISIBLE);
                    button.setVisibility(View.VISIBLE);
                    ivtick.setVisibility(View.VISIBLE);
                    timer.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Verification successful!", Toast.LENGTH_LONG).show();
                    textView.setText("Successful!");
                }
            });
        }

        public void onVerificationFailed(final VerificationDetail verificationFailed) {
            Log.d(LOGGING_TAG, "onVerificationFailed: "+verificationFailed.getVerificationId() +" "+verificationFailed.getVerificationType()+ " "+verificationFailed.isLastMechanism()+" "+verificationFailed);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(verificationFailed.isLastMechanism()){
                        if(verificationFailed.getVerificationType()==VerificationType.SMSOTP && verificationFailed.getVerificationError().getErrorCode() !=802){
                            return;
                        }
                        gifImageView.setVisibility(View.INVISIBLE);
                        button.setVisibility(View.VISIBLE);
                        ivcross.setVisibility(View.VISIBLE);
                        timer.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Verification Failed!", Toast.LENGTH_LONG).show();
                        textView.setText("Couldn't verify!");
                    }
                    Toast.makeText(getApplicationContext(), verificationFailed.getVerificationError().getErrorMessage(), Toast.LENGTH_LONG).show();
                }
            });
            Log.d(LOGGING_TAG,verificationFailed.getVerificationError().getErrorMessage()+"  "+verificationFailed.getVerificationError().getMiscData());
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        timer = (TextView) findViewById(R.id.timer);

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.RECEIVE_SMS,
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

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.country_arrays, R.layout.spinner_item);
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
//                    initializeVerification();
                    String phoneNumber = spinner.getSelectedItem().toString() + editText.getText().toString();
                    try {
                        button.setVisibility(View.INVISIBLE);
                        ivcross.setVisibility(View.INVISIBLE);
                        ivtick.setVisibility(View.INVISIBLE);
                        textView.setText("");
                        gifImageView.setVisibility(View.VISIBLE);
//                        for orchestration
                        Credentials credentials = Credentials.masterCredential(ACCOUNT_SID,MASTER_KEY,MASTER_TOKEN);
//                        for notp/smsotp
//                        Credentials credentials = Credentials.appCredential(ACCOUNT_SID,SECRET);

//                        for smsotp/orchestration
                        AppSettings appSettings = new OrchestrationAppSettings.Builder().enableOtpAutoRead(true).build();
                        VerificationApp verificationApp = new ExoverifyApp.BuildVerificationApp()
                                .setVerificationType(VerificationType.ORCHESTRATION)
                                .setId(ID)
//                                only required for smsotp and orchestration
                                .setAppSettings(appSettings)
                                .setCredentials(credentials)
                                .setContext(getApplicationContext())
                                .build();
                        VerificationParams verificationParams = new VerificationParams.Builder()
                                .setVerificationListener(new verifyListener())
//                                only required if auto read otp is enabled
                                .setOtpParser(new Otp())
//                                optional parameter
                                .setTimerListener(new Timer())
//                                for smsotp/orchestration
                                .setReplacementVar(new ArrayList<String>(Arrays.asList("zafar")))
                                .build();
                        verificationApp.verify(phoneNumber, verificationParams);
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
//                    sending manually entered otp to SDK
                    Intent intent = new Intent();
                    intent.setAction(OTP_BROADCAST);
                    intent.putExtra(OTP_KEY,otpEditText.getText().toString());
                    sendBroadcast(intent);
                }
            });
        } catch (Exception e) {
            Log.e(LOGGING_TAG, "onCreate: Exception occurred " + e.getMessage());
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