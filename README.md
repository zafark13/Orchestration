# ExoVerify

This repository contains code of a sample android application where the Exotel Orchestration SDK is implemented.

Note: You need to add credentials to the variables declared in the MainActivity.java to get the code working
Exoverify SDK is a native java sdk for andriod only.

### Overview
Verify user’s mobile number quickly and seamlessly using Orchestration verification library (Android only).

Orchestration feature ensures efficient and streamlined verification processes by allowing clients to trigger a sequence of mechanisms with pre-defined timeouts. Currently, we offer two verification mechanisms nOTP and SMSOTP, with more to be launched in the future. This feature allows for all mechanisms to be seamlessly integrated for a smooth and efficient verification process.

Learn more about nOTP : https://exotel.com/products/authentication/

### Orchestration Verification Process
The below flow diagram explains a typical verification process:

![Orchestration Verification Process](https://github.com/exotel-products/Orchestration/blob/master/verificationProcess.png)

### Orchestration Sequence Diagram
The below squence diagram explains the sequence of opeartions between client server and Orchestration SDK and Backend :

![Orchestration Sequence Diagram](https://github.com/exotel-products/Orchestration/blob/master/OrchestrationSequenceDiagram.png)

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

        dependencies { implementation 'org.bitbucket.Exotel:exoverify:1.7.0' }

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
}

4. The below permissions are required to be added in your AndroidManifest file:

       <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
       <uses-permission android:name="android.permission.INTERNET" />
       <uses-permission android:name="android.permission.CALL_PHONE" />
       <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
       <uses-permission android:name="android.permission.READ_CALL_LOG"/>

5. Request for the user for the above mentioned permissions, as SDK requires these permissions in-order to verify the mobile numbers.


### Note
verification to completely function on devices running  android version Pie (9) & above requires the following permission :

        <uses-permission android:name="android.permission.READ_CALL_LOG"/>

Above mentioned is one of the permissions that requires use-case (how this permission would be used) declaration as per updated [Google Play Policies](https://support.google.com/googleplay/android-developer/answer/9047303?hl=en).

Upon submission of your App to Google play, make sure to check the exact use-case in declaration form as shown in the below screenshot :

![Google Play Declaration](https://github.com/exotel/ExoVerify/blob/master/app/readme-pictures/notp_read_call_log_reason.png)

### JAVA Class Integration
6. Declare the ExotelVerification object in the activity where the verification is to take place:

        ExotelVerification eVerification;

7. Create a Config object using ConfigBuilder. Initialize Verification by passing the config object to the constructor:

        Config config = new ConfigBuilder(<YOUR JOURNEY ID>, <MASTER KEY>, <MASTER TOKEN>, <ACCOUNT SID>,getApplicationContext()).Build();
        eVerification = new ExotelVerification(config);

NOTE: You should get your AccountSid, Journey ID and Master Token and Master key by logging into exoverify dashboard.

8. Create a class that implements ‘VerificationListener’. This is where you can define the actions that take place when the verification of the number succeeds or fails :

        class verifyListener implements VerificationListener {
            public void onVerificationStarted(VerificationStart verificationStart) {
                Log.i(LOGGING_TAG, "Verification Started!" + "Mechanism Type :"+ verificationStart.getVerificationType());  
            }   
            public void onVerificationSuccess(VerificationSuccess verificationSuccess) {
                Log.i(LOGGING_TAG, "Verification Successful!");
            }
            public void onVerificationFailed(VerificationFailed verificationFailed) {
                Log.i(LOGGING_TAG, "Verification Failed: "+verificationFailed.getRequestID()+ " "+verificationFailed.getErrorCode()+" "+verificationFailed.getErrorMessage()+" "+verificationFailed.getMiscData() ); 
            }
        }
9. In VerificationStart Object you can get type of verfication that is started using this method getVerificationType() and change the screen accordingly.

10. Start Verification:
    Start the verification process by using the startVerification method. This method takes the following parameters:
* listener - new verifyListerner created in Step 8
* phone number - number should be in E164
* HTTP timeout - timeout value in seconds.
* vars - Replacement vars that will be placed on the placeholder {#var#} in the sequence they are passed. If there are no {#var#} placeholder in sms template, pass empty string array


        eVerification.startOrchestrationVerification(new verifyListener(), phoneNumber, timeOutValueInSeconds,vars);

11. Once the user enters the OTP, you need to call verifyOtp function of ExotelVerification class to verify it. This function doesnot return anything. onVerificationSuccess, onVerificationFailed will be called based on success or failure.

12. Getting time in seconds after which the verification will time out.
* Import the Timer class and TimerListener interface.

        import com.exotel.verification.Timer;
        import com.exotel.verification.TimerListener;

* Create an instance of the Timer class and implement the TimerListener.

        Timer customTimer = new Timer();
        customTimer.setTimerListener(new TimerListener() {
      @Override
        public void getTimerTick(long time) {
            secondsTv.setText("Please expect the verification call in " + String.valueOf(time/1000) + " seconds.");
        }
        });

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
    * 811 - Unknown Error occurred. Please report this.
    * 812 - Phone might have been busy on another call during verification. Or it's a timeout
    * 813 - Not able to get Cell Signal/ No country Code returned
    * 814 - Invalid Number/ not in E164 format
    * 1210 - OTP has expired
    * 1211 - Invalid OTP Entered
    * 1017 - OTP already verified
    * 1016 - Maximum allowed verification attempts has been made to verify OTP


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
