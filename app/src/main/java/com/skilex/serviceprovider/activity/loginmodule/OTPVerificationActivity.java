package com.skilex.serviceprovider.activity.loginmodule;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.skilex.serviceprovider.R;
import com.skilex.serviceprovider.activity.LandingPageActivity;
import com.skilex.serviceprovider.activity.providerregistration.CategorySelectionActivity;
import com.skilex.serviceprovider.activity.providerregistration.DocumentVerificationStatusActivity;
import com.skilex.serviceprovider.activity.providerregistration.DocumentVerifySuccessActivity;
import com.skilex.serviceprovider.activity.providerregistration.InitialDepositActivity;
import com.skilex.serviceprovider.activity.providerregistration.OrganizationTypeSelectionActivity;
import com.skilex.serviceprovider.activity.providerregistration.RegOrgDocumentUploadActivity;
import com.skilex.serviceprovider.activity.providerregistration.RegisteredOrganizationInfoActivity;
import com.skilex.serviceprovider.activity.providerregistration.UnRegOrgDocumentUploadActivity;
import com.skilex.serviceprovider.activity.providerregistration.UnRegisteredOrganizationInfoActivity;
import com.skilex.serviceprovider.activity.providerregistration.UploadProfilePicActivity;
import com.skilex.serviceprovider.activity.providerregistration.WelcomeActivity;
import com.skilex.serviceprovider.customview.CustomOtpEditText;
import com.skilex.serviceprovider.helper.AlertDialogHelper;
import com.skilex.serviceprovider.helper.ProgressDialogHelper;
import com.skilex.serviceprovider.interfaces.DialogClickListener;
import com.skilex.serviceprovider.languagesupport.BaseActivity;
import com.skilex.serviceprovider.servicehelpers.ServiceHelper;
import com.skilex.serviceprovider.serviceinterfaces.IServiceListener;
import com.skilex.serviceprovider.utils.CommonUtils;
import com.skilex.serviceprovider.utils.PreferenceStorage;
import com.skilex.serviceprovider.utils.SkilExConstants;

import org.json.JSONException;
import org.json.JSONObject;

public class OTPVerificationActivity extends BaseActivity implements View.OnClickListener, IServiceListener, DialogClickListener {

    private static final String TAG = OTPVerificationActivity.class.getName();

    private CustomOtpEditText otpEditText;
    private TextView tvResendOTP;
    private ImageView btnConfirm;
    private String mobileNo;
    private String checkVerify;
    private ServiceHelper serviceHelper;
    private ProgressDialogHelper progressDialogHelper;
    String getUserMasterId, getMobileNumber;

    boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        serviceHelper = new ServiceHelper(this);
        serviceHelper.setServiceListener(this);
        progressDialogHelper = new ProgressDialogHelper(this);

        otpEditText = findViewById(R.id.otp_view);
        tvResendOTP = findViewById(R.id.resend);
        tvResendOTP.setOnClickListener(this);
        btnConfirm = findViewById(R.id.sendcode);
        btnConfirm.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        //Checking for fragment count on backstack
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else if (!doubleBackToExitPressedOnce) {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit.", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            super.onBackPressed();
            return;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isNetworkAvailable(getApplicationContext())) {
            if (v == tvResendOTP) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.resend_otp);
                alertDialogBuilder.setMessage(getString(R.string.otp_confirm_no) + " " + PreferenceStorage.getMobileNo(getApplicationContext()));
                alertDialogBuilder.setPositiveButton(R.string.otp_proceed,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                checkVerify = "Resend";
                                JSONObject jsonObject = new JSONObject();
                                try {

                                    jsonObject.put(SkilExConstants.PHONE_NUMBER, PreferenceStorage.getMobileNo(getApplicationContext()));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
                                String url = SkilExConstants.BUILD_URL + SkilExConstants.MOBILE_VERIFICATION;
                                serviceHelper.makeGetServiceCall(jsonObject.toString(), url);

                            }
                        });
                alertDialogBuilder.setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

//                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialogBuilder.show();

            } else if (v == btnConfirm) {
                if (otpEditText.hasValidOTP()) {
                    checkVerify = "Confirm";
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(SkilExConstants.USER_MASTER_ID, PreferenceStorage.getUserMasterId(getApplicationContext()));
                        jsonObject.put(SkilExConstants.PHONE_NUMBER, PreferenceStorage.getMobileNo(getApplicationContext()));
                        jsonObject.put(SkilExConstants.OTP, otpEditText.getOTP());
                        jsonObject.put(SkilExConstants.DEVICE_TOKEN, PreferenceStorage.getGCM(getApplicationContext()));
                        jsonObject.put(SkilExConstants.MOBILE_TYPE, "1");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
                    String url = SkilExConstants.BUILD_URL + SkilExConstants.LOGIN;
                    serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
                } else {
                    AlertDialogHelper.showSimpleAlertDialog(this, getString(R.string.otp_invalid));
                }
            }
        } else {
            AlertDialogHelper.showSimpleAlertDialog(this, getString(R.string.error_no_net));
        }
    }

    @Override
    public void onAlertPositiveClicked(int tag) {

    }

    @Override
    public void onAlertNegativeClicked(int tag) {

    }

    private boolean validateResponse(JSONObject response) {
        boolean signInSuccess = false;
        if ((response != null)) {
            try {
                String status = response.getString("status");
                String msg = response.getString(SkilExConstants.PARAM_MESSAGE);
                Log.d(TAG, "status val" + status + "msg" + msg);

                if ((status != null)) {
                    if (((status.equalsIgnoreCase("activationError")) || (status.equalsIgnoreCase("alreadyRegistered")) ||
                            (status.equalsIgnoreCase("notRegistered")) || (status.equalsIgnoreCase("error")))) {
                        signInSuccess = false;
                        Log.d(TAG, "Show error dialog");
                        AlertDialogHelper.showSimpleAlertDialog(this, msg);

                    } else {
                        signInSuccess = true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return signInSuccess;
    }

    @Override
    public void onResponse(JSONObject response) {
        progressDialogHelper.hideProgressDialog();
        if (validateResponse(response)) {
            try {

                String countCategory = response.getString("categoryCount");
                JSONObject userData = response.getJSONObject("userData");
                String getCompanyType = userData.getString("company_status");
                JSONObject docData = response.getJSONObject("docData");
                String docStatus = docData.getString("status");
                JSONObject companyData = response.getJSONObject("companyData");
                String companyStatus = companyData.getString("status");
                String getServiceProviderBasicStatus = userData.getString("also_service_person");
                String getBankStatus = userData.getString("bank_name");
                String getPaymentStatus = userData.getString("deposit_status");
                String getProfilePicStatus = userData.getString("profile_pic");

                //If company

                if (getProfilePicStatus.isEmpty()) {
                    Intent i = new Intent(OTPVerificationActivity.this, UploadProfilePicActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("ProviderPersonCheck", "Provider");
                    startActivity(i);
                    finish();
                } else if (countCategory.equalsIgnoreCase("0")) {
                    Intent i = new Intent(OTPVerificationActivity.this, CategorySelectionActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("ProviderPersonCheck", "Provider");
                    startActivity(i);
                    finish();
                } else if (getCompanyType.isEmpty()) {
                    Intent intent = new Intent(this, OrganizationTypeSelectionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else if (getServiceProviderBasicStatus.isEmpty()) {
                    if (getCompanyType.equalsIgnoreCase("Company")) {
                        Intent intent = new Intent(this, RegisteredOrganizationInfoActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(this, UnRegisteredOrganizationInfoActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                } else if (getBankStatus.isEmpty()) {

                    if (getCompanyType.equalsIgnoreCase("Company")) {
                        Intent i = new Intent(this, RegOrgDocumentUploadActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    } else {
                        Intent intent = new Intent(this, UnRegOrgDocumentUploadActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                } else if (getPaymentStatus.equalsIgnoreCase("Unpaid")) {
                    Intent i = new Intent(getApplicationContext(), InitialDepositActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                } else {
                    String loginType = PreferenceStorage.getLoginType(getApplicationContext());
                    if (loginType.equalsIgnoreCase("Register")) {
                        Intent i = new Intent(OTPVerificationActivity.this, CategorySelectionActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.putExtra("ProviderPersonCheck", "Provider");
                        startActivity(i);
                        finish();
                    } else if (loginType.equalsIgnoreCase("Login")) {
                        if (userData.getString("serv_prov_display_status").equalsIgnoreCase("Inactive")) {

                            if (userData.getString("serv_prov_verify_status").equalsIgnoreCase("Pending")) {

                                Intent i = new Intent(OTPVerificationActivity.this, DocumentVerificationStatusActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();
                            } else if (userData.getString("serv_prov_verify_status").equalsIgnoreCase("Approved")) {

                                Intent i = new Intent(OTPVerificationActivity.this, DocumentVerifySuccessActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();
                            }
                        } else {

                            String userMasterId = userData.getString("user_master_id");
                            String fullName = userData.getString("full_name");
                            String phoneNo = userData.getString("phone_no");
                            String email = userData.getString("email");
                            String userType = userData.getString("user_type");
                            String profilePic = userData.getString("profile_pic");

                            PreferenceStorage.saveUserMasterId(this, userMasterId);
                            PreferenceStorage.saveFullName(this, fullName);
                            PreferenceStorage.saveMobileNo(this, phoneNo);
                            PreferenceStorage.saveEmail(this, email);
                            PreferenceStorage.saveLoginType(this, userType);
                            PreferenceStorage.saveProfilePicture(this, profilePic);

                            Intent i = new Intent(OTPVerificationActivity.this, LandingPageActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }
                    }
                }
//                finish();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onError(String error) {
        progressDialogHelper.hideProgressDialog();
        AlertDialogHelper.showSimpleAlertDialog(this, error);
    }
}
