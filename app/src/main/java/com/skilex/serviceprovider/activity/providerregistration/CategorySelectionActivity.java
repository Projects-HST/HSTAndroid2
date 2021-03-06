package com.skilex.serviceprovider.activity.providerregistration;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.skilex.serviceprovider.R;
import com.skilex.serviceprovider.activity.loginmodule.OTPVerificationActivity;
import com.skilex.serviceprovider.activity.serviceperson.ServicePersonDocumentUploadActivity;
import com.skilex.serviceprovider.adapter.CategoryListAdapter;
import com.skilex.serviceprovider.bean.support.Category;
import com.skilex.serviceprovider.bean.support.Preference;
import com.skilex.serviceprovider.bean.support.SetCategory;
import com.skilex.serviceprovider.helper.AlertDialogHelper;
import com.skilex.serviceprovider.helper.ProgressDialogHelper;
import com.skilex.serviceprovider.interfaces.DialogClickListener;
import com.skilex.serviceprovider.languagesupport.BaseActivity;
import com.skilex.serviceprovider.servicehelpers.ServiceHelper;
import com.skilex.serviceprovider.serviceinterfaces.IServiceListener;
import com.skilex.serviceprovider.utils.CommonUtils;
import com.skilex.serviceprovider.utils.PreferenceStorage;
import com.skilex.serviceprovider.utils.SkilExConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class CategorySelectionActivity extends BaseActivity implements View.OnClickListener, IServiceListener,
        DialogClickListener, CategoryListAdapter.OnItemClickListener {

    private static final String TAG = OTPVerificationActivity.class.getName();
    private RecyclerView mRecyclerView;
    private CategoryListAdapter categoryListAdapter;
    private ArrayList<Category> categoryArrayList, selectedList;
    private ServiceHelper serviceHelper;
    private ProgressDialogHelper progressDialogHelper;
    private MenuItem menuSet;
    private GridLayoutManager mLayoutManager;
    private boolean selval = false;
    private ImageView PrefSelect;
    int pos;
    private TextView txtGoNext, txtSelect;
    private CheckBox txtSelectAll;
    HashSet<String> hashSet;
    private String responseActivity = "";

    private String serviceFlag = "";
    private String checkProviderAndPerson = "";

    boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_selection);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            checkProviderAndPerson = extras.getString("ProviderPersonCheck");
            //The key argument here must match that used in the other activity
        }

        if (checkProviderAndPerson.equalsIgnoreCase("ProviderUpdate")) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        txtGoNext = findViewById(R.id.text_gonext);
        txtGoNext.setOnClickListener(this);
        txtSelectAll = findViewById(R.id.checkBox);
        txtSelectAll.setOnClickListener(this);
        mRecyclerView = findViewById(R.id.listView_categories);
        PrefSelect = findViewById(R.id.pref_tick);
        mLayoutManager = new GridLayoutManager(this, 6);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (categoryListAdapter.getItemViewType(position) > 0) {
                    return categoryListAdapter.getItemViewType(position);
                } else {
                    return 4;
                }
                //return 2;
            }
        });
        mRecyclerView.setLayoutManager(mLayoutManager);

        selectedList = new ArrayList<>();
        hashSet = new HashSet<String>();

        serviceHelper = new ServiceHelper(this);
        serviceHelper.setServiceListener(this);
        progressDialogHelper = new ProgressDialogHelper(this);

        getCategories();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        //Checking for fragment count on backstack
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else if (!doubleBackToExitPressedOnce) {
            this.doubleBackToExitPressedOnce = true;
            if (checkProviderAndPerson.equalsIgnoreCase("ProviderUpdate")) {
                Toast.makeText(this, "Please click again to back.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please click BACK again to exit.", Toast.LENGTH_SHORT).show();
            }
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

    private void setPreferences() {
        //save preferences selected
        Log.d(TAG, "size of selected preferences" + selectedList.size());
        PreferenceStorage.savePreferencesSelected(this, true);
        ArrayList<Preference> preferences = new ArrayList<>();
        for (Category category : selectedList) {
            Preference preference = new Preference();
            Log.d(TAG, "add category id" + category.getId());
            preference.setCategoryId(category.getId());

            preferences.add(preference);
        }

        SetCategory setCategory = new SetCategory();
        setCategory.setPreferences(preferences);
        Gson gson = new Gson();
        String json = gson.toJson(preferences);

        String removeSquareBracketOpen = "[";
        String removeSquareBracketClose = "]";
        String removeEtcCharSet1 = "{\"cat_id\":\"";
        String removeEtcCharSet2 = "\"}";

        String new_string = json.replace(removeSquareBracketOpen, "");
        String new_string1 = new_string.replace(removeSquareBracketClose, "");
        String new_string2 = new_string1.replace(removeEtcCharSet1, "");
        String new_string3 = new_string2.replace(removeEtcCharSet2, "");

        String[] strArray = new_string3.split(",");
        final int[] animalsArray = new int[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            animalsArray[i] = Integer.parseInt(strArray[i]);
        }

        System.out.println(Arrays.toString(animalsArray));
        final int[] buckets = new int[1001];
        for (final int i : animalsArray) {
            buckets[i]++;
        }
        final int[] unique = new int[animalsArray.length];
        int count = 0;
        for (int i = 0; i < buckets.length; ++i) {
            if (buckets[i] > 0) {
                unique[count++] = i;
            }
        }
        final int[] compressed = new int[count];
        System.arraycopy(unique, 0, compressed, 0, count);
        System.out.println(Arrays.toString(compressed));

        String newOk = Arrays.toString(compressed);

        String newOk3 = newOk.replace(removeSquareBracketOpen, "");
        String newOk4 = newOk3.replace(removeSquareBracketClose, "");


        if (CommonUtils.isNetworkAvailable(this)) {
            serviceFlag = "Selection";
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(SkilExConstants.USER_MASTER_ID, PreferenceStorage.getUserMasterId(getApplicationContext()));
                jsonObject.put(SkilExConstants.KEY_SERVICE_PERSON_ID, PreferenceStorage.getServicePersonId(getApplicationContext()));
                jsonObject.put(SkilExConstants.KEY_CATEGORIES_ID, newOk4);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
            String url = "";
            if (checkProviderAndPerson.equalsIgnoreCase("Provider") || checkProviderAndPerson.equalsIgnoreCase("ProviderUpdate")) {
                url = SkilExConstants.BUILD_URL + SkilExConstants.PROVIDER_CATEGORY_UPDATE;
            } else {
                url = SkilExConstants.BUILD_URL + SkilExConstants.PERSON_CATEGORY_UPDATE;
            }
            serviceHelper.makeGetServiceCall(jsonObject.toString(), url);


        } else {
            AlertDialogHelper.showSimpleAlertDialog(this, getString(R.string.error_no_net));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(new View(this).getWindowToken(), 0);
        return true;
    }

    private void getCategories() {
        if (CommonUtils.isNetworkAvailable(getApplicationContext())) {
            serviceFlag = "List";
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(SkilExConstants.USER_MASTER_ID, PreferenceStorage.getUserMasterId(getApplicationContext()));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
            String url = SkilExConstants.BUILD_URL + SkilExConstants.LIST_ALL_CATEGORIES;
            serviceHelper.makeGetServiceCall(jsonObject.toString(), url);

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
//                        AlertDialogHelper.showSimpleAlertDialog(this, msg);
                        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
                        alertDialogBuilder.setMessage(msg);
                        alertDialogBuilder.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                selectedList.clear();
                                finish();
                                startActivity(getIntent());
                            }
                        });
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.show();

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
            PreferenceStorage.savePreferencesSelected(this, true);
            if (serviceFlag.equalsIgnoreCase("List")) {
                try {
                    JSONArray getData = response.getJSONArray("categories");
                    Gson gson = new Gson();
                    Type listType = new TypeToken<ArrayList<Category>>() {
                    }.getType();
                    categoryArrayList = (ArrayList<Category>) gson.fromJson(getData.toString(), listType);
                    categoryListAdapter = new CategoryListAdapter(this, categoryArrayList, this);
                    mRecyclerView.setAdapter(categoryListAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (serviceFlag.equalsIgnoreCase("Selection")) {

                if (checkProviderAndPerson.equalsIgnoreCase("Provider")) {
                    Intent intent = new Intent(this, OrganizationTypeSelectionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                    startActivity(intent);
                    finish();
                } else if (checkProviderAndPerson.equalsIgnoreCase("ProviderUpdate")) {
                    finish();
                } else {
                    Intent intent = new Intent(this, ServicePersonDocumentUploadActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void onError(String error) {
        progressDialogHelper.hideProgressDialog();
        AlertDialogHelper.showSimpleAlertDialog(this, error);
    }

    public void onCategorySelected(int position) {
        Log.d(TAG, "selected category position" + position);
        if (selectedList != null) {
            Category category = (Category) categoryListAdapter.getItem(position);
            Log.d(TAG, "id" + category.getId());
            selectedList.add(category);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == txtGoNext) {

            int totalAmount = 0;
            for (int i = 0; i < selectedList.size(); i++) {
                String totalprice = selectedList.get(i).getCategoryPreference();
                if (totalprice.equalsIgnoreCase("Y")) {
                    totalAmount = +1;
                }
            }

            if (selectedList.size() >= 1) {

                setPreferences();
            } else {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.select_enough_cat);
                alertDialogBuilder.setMessage(R.string.select_atleaset);
                alertDialogBuilder.setPositiveButton(R.string.alert_button_ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }
    }


    @Override
    public void onItemClick(View view, int position) {
        Category tag = categoryListAdapter.getItem(position);
        if (tag.getCategoryPreference().equals("N")) {

            tag.setCategoryPreference("Y");
            selectedList.add(tag);

//            categoryListAdapter.notifyItemChanged(position);
        } else {
            tag.setCategoryPreference("N");
            selectedList.remove(tag);
//            categoryListAdapter.notifyItemChanged(position);
        }
        categoryListAdapter.notifyItemChanged(position);
    }
}
