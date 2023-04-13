# ExoVerify

This repository contains code of a sample android application where the Exoverify SDK is implemented.

Note: You need to add credentials to the variables declared in the MainActivity.java to get the code working
Exoverify SDK is a native java sdk for andriod only.

### Overview
Verify user’s mobile number quickly and seamlessly using Exoverify verification library (Android only).

This SDK supports three types of verification:
1. nOTP: Exoverify SDK automatically intercepts a phone call triggered by the Exoverify system for the mobile number verification, allowing you to verify your users with no user interaction. Using the SDK, you can have nOTP verification into your existing android apps with just a few lines of code.

2. SMSOTP: Exoverify SDK automatically detects the sms received on phone, extracts OTP from sms and verify it seamlessly without any user interaction, it also supports OTP verification manually by user.

3. ORCHESTRATION: This feature ensures efficient and streamlined verification processes by allowing clients to trigger a sequence of mechanisms with pre-defined timeouts. Currently, we offer two verification mechanisms nOTP and SMSOTP, with more to be launched in the future. This feature allows for all mechanisms to be seamlessly integrated for a smooth and efficient verification process.

Learn more about nOTP : https://exotel.com/products/authentication/

### Verification Process
The below flow diagram explains a typical verification process:

![Verification Process](https://github.com/exotel-products/Orchestration/blob/master/VerificationProcess.png)

### Sequence Diagram
The below squence diagram explains the sequence of opeartions between client server and Orchestration SDK and Backend :

![Sequence Diagram](https://github.com/exotel-products/Orchestration/blob/master/VerificationSequenceDiagram.png)

### Pre-requisites
* User needs to have a KYC verified account with Exotel.
* The SDK can be used with Android 5.0 and above versions.
* The compileSdkVersion of your app needs to be 28+.
* For verification, the phone number should be passed in E.164 format, prefixed with the plus sign (+).
* Create a nOTP app
* Create a SMSOTP app
* Create a Orchestration Journey

Please visit [Exoverify Dashboard](https://verify.exotel.com) to Create your nOTP, SMSOTP Application and Orchestration journey 

## Android SDK Integration Steps
1. Include the SDK library in your Android Project:
   If you are using Gradle, you need to add the libray in the app level Gradle file, as shown below:

        dependencies { implementation 'org.bitbucket.Exotel:exoverify:2.0.0' }

2. A few other dependencies that you need to add are:

       implementation 'com.squareup.okhttp3:okhttp:3.6.0'
       implementation 'com.google.code.gson:gson:2.8.0'
       implementation 'com.googlecode.libphonenumber:libphonenumber:8.8.3'
       implementation 'dnsjava:dnsjava:2.1.6’

3. Add the following jitpack dependency in Gradle Setting file:

        dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
                repositories {
                        ...
                        maven {
                                url 'https://jitpack.io'
                                credentials  { username 'jp_etcct006nc8pkd0ntra0n5uk9k' }
                        }
                }
        }

4. The below permissions are required to be added in your AndroidManifest file:

        NOTP:
                <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
                <uses-permission android:name="android.permission.CALL_PHONE" />
                <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
                <uses-permission android:name="android.permission.READ_CALL_LOG"/>
                <uses-permission android:name="android.permission.ANSWER_PHONE_CALLS" /> (only for android API level >= 26)

        SMSOTP:
                <uses-permission android:name="android.permission.RECEIVE_SMS" />

        COMMON:
                <uses-permission android:name="android.permission.INTERNET" />

        ORCHESTRATION:
                All of above Permissions



5. Request for the user for the above mentioned permissions, as SDK requires these permissions in-order to verify the mobile numbers.


### Note
verification to completely function on devices running  android version Pie (9) & above requires the following permission :

        <uses-permission android:name="android.permission.READ_CALL_LOG"/>

Above mentioned is one of the permissions that requires use-case (how this permission would be used) declaration as per updated [Google Play Policies](https://support.google.com/googleplay/android-developer/answer/9047303?hl=en).

Upon submission of your App to Google play, make sure to check the exact use-case in declaration form as shown in the below screenshot :

![Google Play Declaration](https://github.com/exotel/ExoVerify/blob/master/app/readme-pictures/notp_read_call_log_reason.png)

### JAVA Class Integration

6. Getting time in seconds after which the verification will time out. (Optional)
* Import TimerListener interface.

        import com.exotel.verification.exposed_interfaces.TimerListener;

* Create a Timer class and implement the TimerListener.

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

7. Parsing OTP from the received message (only required for SMSOTP with auto read enabled or ORCHESTRATION  with auto read enabled).
* Import the Timer class and TimerListener interface.

        import com.exotel.verification.exposed_interfaces.OtpParser;

* Create a class and implement the OtpParser.

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
8. Create a class that implements ‘VerificationListener’. This is where you can define the actions that take place when the verification of the number succeeds or fails :

        class verifyListener implements VerificationListener {
            public void onVerificationStarted(VerificationDetail verificationDetail) {
                Log.i(LOGGING_TAG, "Verification Started!" + "Mechanism Type :"+ verificationDetail.getVerificationType());  
            }   
            public void onVerificationSuccess(VerificationDetail verificationDetail) {
                Log.i(LOGGING_TAG, "Verification Successful!");
            }
            public void onVerificationFailed(VerificationDetail verificationDetail) {
                Log.i(LOGGING_TAG, "Verification Failed: "+VerificationDetail.getRequestID()+ " "+VerificationDetail.getErrorCode()+" "+VerificationDetail.getErrorMessage()+" "+VerificationDetail.getMiscData() ); 
            }
        }

NOTE:
* VerificationError in VerificationDetails can be NULL.
* miscData in VerificationError can be NULL.
* VerificationId in VerificationDetails can be NULL, if Verification failed to start.


9. Create Credentials:

        Master Credentials (for Orchestration)
                Credentials credentials = Credentials.masterCredential(ACCOUNT_SID,MASTER_KEY,MASTER_TOKEN);
        App Cedentials(for Notp or SMSOTP)
                Credentials credentials = Credentials.appCredential(ACCOUNT_SID,SECRET);

10. Create app setting(only for Orchestration or SMSOTP):

        AppSettings appSettings = new OrchestrationAppSettings.Builder().enableOtpAutoRead(true).build();

11. Create Verification App:

        VerificationApp verificationApp = new ExoverifyApp.BuildVerificationApp()
                                .setVerificationType(VerificationType.ORCHESTRATION) -------> specify correct verification type (notp, smsotp, orchestration)
                                .setId(ID) ----> app Id / Journey Id
                                .setAppSettings(appSettings) -----> only required for smsotp and orchestration
                                .setCredentials(credentials) 
                                .setContext(getApplicationContext())
                                .build();

12. Create Verification Params:

        VerificationParams verificationParams = new VerificationParams.Builder()
                                .setVerificationListener(new verifyListener())
                                .setOtpParser(new Otp()) ----> only required for smsotp with auto read and orchestration with auto read
                                .setTimerListener(new Timer()) ------> optional
                                .setReplacementVar(new ArrayList<String>())  ------> onl required for smsotp and orchestration
                                .build();

13. Start Verification:

        verificationApp.verify(phoneNumber, verificationParams);

14. If user enters OTP, to verify OTP, send OTP to SDK like this:

        Imports:
                import static com.exotel.verification.constant.Constants.OTP_BROADCAST;
                import static com.exotel.verification.constant.Constants.OTP_KEY;

        Intent intent = new Intent();
        intent.setAction(OTP_BROADCAST);
        intent.putExtra(OTP_KEY,OTP_ENTERED_BY_USER);
        sendBroadcast(intent);

NOTE: 
* You should get your AccountSid, Journey ID and Master Token and Master key by logging into exoverify dashboard.
* In VerificationStart Object you can get type of verfication that is started using this method getVerificationType() and change the screen accordingly.

### Troubleshooting
* Make sure you use the latest version of the SDK for best performance and security
* Try logging the results from the verification listener callbacks and try debugging the issue
* Make sure all checked Exceptions are caught and permissions are given
* Following exceptions are thrown from the SDK:
    1. ClientBuilderException - Occurs when building a verification client and the config is wrong. Please build the config with all fields set.
    2. InvalidConfigException - Config passed with initialization is invalid. Check if all fields are set.
    3. PermissionNotGrantedException - Necessary permissions are not granted for SDK to work.
    4. VerificationAlreadyInProgressException - There is an ongoing verification request which did not reach a terminal state yet.
    5. InvalidParametersException - Config doesnot match the verification type(Orchestration/nOTP) you are tring to initiate.
* If Client App is using progaurd , Add this to the [proguard-rules.pro](proguard-rules.pro) file in the application:

        -keep class org.xbill.DNS.ResolverConfig
        -keep class org.xbill.DNS.Lookup
        -keep class com.exotel.** { *; }

* Fail code and messages when verification fails:

    * 801 - The config for ExotelVerification is null. Check config or initialize properly
    * 802 - The ExotelVerification request has been timed out. Call was not received on this phone.
    * 803 - The connection to the server timed out
    * 804 - Not able to establish a connection to the server
    * 805 - Auth failure error
    * 806 - Server error occurred. Please report this
    * 807 - Network error
    * 808 - Error parsing the response / Invalid response from the server. Please report this
    * 809 - API throttle limit for this application has been reached. Please wait for it to reset
    * 810 - Throttle limit for this phone number has been reached. Please wait for it to reset
    * 811 - Unknown Error occurred. (Please report this)
    * 812 - Phone might have been busy on another call during verification. Or it's a timeout
    * 813 - Not able to get Cell Signal/ No country Code returned
    * 814 - Invalid Number/ not in E164 format
    * 819 - Wrong caller Id (please report us)
    * 1210 - OTP has expired
    * 1211 - Invalid OTP Entered
    * 1017 - OTP already verified
    * 1016 - Maximum allowed verification attempts has been made to verify OTP
    * 1030 - Throtle limit breached
    * 1031 - App id or journey id not found
    * 115 - Inactive app or journey
    * 116 - Inactive account
    * 1009 - Insufficient baance
    * 500 - Internal server error (please report us)
    * 400 - Bad request (please report us)

Contact Exoverify support with supporting logs if issue persists or requires further support.

### Webhooks :
* Event Webhook: <p> Please configure an API here to receive updates on the status (success/failure) of the verifications and the mechanism being implemented.</p>

    * The sample payload for the webhook in case of <b>success</b> is:
      ```json
        {
                "mechanism_type": "notp",
                "verification_id": "ac53fb78a80dc653670f764be032171i",
                "app_mechanism_id": "6791cd7e71c747a7879db94ec7854995",
                "phone": "+918298765625",
                "status": "success",
                "webhook_timestamp": "2023-01-18T09:01:52Z"
        } 
        ```

    * The sample payload for the webhook in case of <b>failure</b> is:
      ```josn
        {
                "mechanism_type": "notp",
                "verification_id": "d9dd21bb5uhgb5d6f83ec9fb0a52171i",
                "app_mechanism_id": "6791cd7e71bvc87a7a29db94ec7854995",
                "phone": "+918298765625",
                "status": "fail",
                "reason": "Timeout",
                "next_mechanism": {
                        "mechanism_type": "smsotp",
                        "app_mechanism_id": "501b48dubcg239b17d20104a2f36167r",
                        "verification_id": "11a60e01d980c9099c77d44985b3171i",
                        "timeout_in_second": 30
                },
                "webhook_timestamp": "2023-01-18T09:04:00Z"
        }
        ```
