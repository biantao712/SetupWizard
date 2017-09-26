/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * cnsetupwizard under the License is cnsetupwizard on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asus.cnsetupwizard.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.security.Credentials;
import android.security.KeyStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.asus.cnsetupwizard.R;
import com.asus.cnsetupwizard.settings.ProxySelector;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;
import static com.asus.cnsetupwizard.wifi.AccessPoint.SECURITY_PSK;


public class WifiConnectActivity extends Activity implements TextWatcher,
        AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    public static final String ACTION = "com.asus.cnsetupwizard.WIFICONNECT";
    private TextView title;
    private boolean mEdit;
    private AccessPoint mAccessPoint;
    private final Handler mTextViewChangedHandler = new Handler();
    private boolean mSupportMobileNetwork;
    private ArrayAdapter<String> PHASE2_PEAP_ADAPTER;
    /* Full list of phase2 methods */
    private ArrayAdapter<String> PHASE2_FULL_ADAPTER;
    private String unspecifiedCert = "unspecified";

    private View mIpSettingsSpinner;
    private TextView mIpAddressView;
    private TextView mGatewayView;
    private TextView mNetworkPrefixLengthView;
    private TextView mDns1View;
    private TextView mDns2View;

    private View mProxySettings;
    private TextView mProxyHostView;
    private TextView mProxyPortView;
    private TextView mProxyExclusionListView;

    private TextView mSsidView;
    private int mAccessPointSecurity;
    private TextView mPasswordView;
    private TextView mSummary;
    private View divider;
    private TextView mEapSummary;

    private View mSecurity;
    private Spinner mEapMethodSpinner;
    private Spinner mPhase2Spinner;
    private Spinner mEapUserCertSpinner;
    private TextView mEapIdentityView;
    private TextView mEapAnonymousView;
    private View mEapMethod;
    private View mPhase2;
    private TextView mPhase2Summary;
    private View certView;
    private TextView mCertSummary;
    private View userCertView;
    private TextView mUserCertSummary;
    private TextView mProxySettingsText;
    private TextView mIpSettingText;
    private TextView connect;
    private TextView mProxySummary;
    private TextView mIpSummary;
    private CheckBox advancedSetting;
    private TextView mProxyPacView;

    private static final int DHCP = 0;
    private static final int STATIC_IP = 1;

    public static final int PROXY_NONE = 0;
    public static final int PROXY_STATIC = 1;
    public static final int PROXY_PAC = 2;

    /* These values come from "wifi_eap_method" resource array */
    public static final int WIFI_EAP_METHOD_PEAP = 0;
    public static final int WIFI_EAP_METHOD_TLS  = 1;
    public static final int WIFI_EAP_METHOD_TTLS = 2;
    public static final int WIFI_EAP_METHOD_PWD  = 3;
    public static final int WIFI_EAP_METHOD_SIM  = 4;
    public static final int WIFI_EAP_METHOD_AKA  = 5;

    private static final int unspecifiedCertIndex = 0;

    private ArrayAdapter<String> mPhase2Adapter;

    /* These values come from "wifi_peap_phase2_entries" resource array */
    public static final int WIFI_PEAP_PHASE2_NONE         = 0;
    public static final int WIFI_PEAP_PHASE2_PAP         = 1;
    public static final int WIFI_PEAP_PHASE2_MSCHAP         = 2;
    public static final int WIFI_PEAP_PHASE2_MSCHAPV2     = 3;
    public static final int WIFI_PEAP_PHASE2_GTC        = 4;

    private boolean showPassword = false;

    private WifiManager.ActionListener mConnectListener;
    private WifiManager.ActionListener mSaveListener;
    private WifiManager.ActionListener mForgetListener;
    private WifiManager mWifiManager;

    private IpAssignment mIpAssignment = IpAssignment.UNASSIGNED;
    private ProxySettings mProxySetting = ProxySettings.UNASSIGNED;
    private ProxyInfo mHttpProxy = null;
    private StaticIpConfiguration mStaticIpConfiguration = null;

    private int mProxySettingsPosition = 0;
    private int mIpSettingsPosition = 0;
    private int mEapMethodPosition = 4;
    private int mPhase2Positon = 0;
    private int mCertPosition = 0;
    private int mUserCertPosition = 0;

    private static final int PROXYSETTING = 0;
    private static final int IPSETTING = 1;
    private static final int SECURITY = 2;
    private static final int EAP = 3;
    private static final int PHASE2 = 4;
    private static final int CERT = 5;
    private static final int USERCERT = 6;

    private AlertDialog dialog;

    private String[] mEapItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_dialog);
        //mView = getLayoutInflater().inflate(R.layout.wifi_dialog, null);
        //setView(mView);
        //setInverseBackgroundForced(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window win = getWindow();
            //win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏
            // 状态栏字体设置为深色，SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 为SDK23增加
            win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

            // 部分机型的statusbar会有半透明的黑色背景
            win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            win.setStatusBarColor(getResources().getColor(R.color.asus_action_bottom_bar_color));// SDK21

        }
        /*Resources resources = getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        int height = resources.getDimensionPixelSize(resourceId);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.area_title);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.lock_button_height));
        lp.setMargins(0, height, 0, 0);
        relativeLayout.setLayoutParams(lp);*/



        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mConnectListener = new WifiManager.ActionListener() {
            @Override
            public void onSuccess() {
            }
            @Override
            public void onFailure(int reason) {
                /*Activity activity = getActivity();
                if (activity != null) {
                    //isNeedUpdateAccessPoints = true;
                    Toast.makeText(activity,
                            R.string.wifi_failed_connect_message,
                            Toast.LENGTH_SHORT).show();
                }*/
            }
        };

        mSaveListener = new WifiManager.ActionListener() {
            @Override
            public void onSuccess() {
            }
            @Override
            public void onFailure(int reason) {
                /*Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity,
                            R.string.wifi_failed_save_message,
                            Toast.LENGTH_SHORT).show();
                }*/
            }
        };

        mForgetListener = new WifiManager.ActionListener() {
            @Override
            public void onSuccess() {
            }
            @Override
            public void onFailure(int reason) {
                /*Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity,
                            R.string.wifi_failed_forget_message,
                            Toast.LENGTH_SHORT).show();
                }*/
            }
        };

        Intent intent = getIntent();
        String ssid = intent.getStringExtra("ssid");
        mEdit = intent.getBooleanExtra("isEdit",false);
        mAccessPoint = null;
        Collection<AccessPoint> accessPoints = constructAccessPoints();
        for (AccessPoint accessPoint : accessPoints) {
            //Log.e(TAG,"accessPoint.ssid="+accessPoint.ssid);
            if(accessPoint.ssid.equals(ssid) &&
                    !(accessPoint.getConfig() != null && accessPoint.getConfig().hiddenSSID &&
                    accessPoint.getConfig().numAssociation == 0 && (accessPoint.getSummary() == null || getResources().getString(R.string.wifi_not_in_range).equals(accessPoint.getSummary())))){
                mAccessPoint = accessPoint;
            }
        }
        title = (TextView)findViewById(R.id.title);
        if(!mEdit){
            title.setText(ssid);
        }

        mAccessPointSecurity = (mAccessPoint == null) ? AccessPoint.SECURITY_NONE :
                mAccessPoint.security;
        TextView cancel = (TextView)findViewById(R.id.cancel);
        connect = (TextView)findViewById(R.id.connect);
        ImageView backIcon = (ImageView)findViewById(R.id.button_back);
        cancel.setOnClickListener(this);
        connect.setOnClickListener(this);
        backIcon.setOnClickListener(this);


        boolean supportMobileNetwork = false;
        NetworkInfo[] nis = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getAllNetworkInfo();
        if(nis!=null){
            for (NetworkInfo ni : nis) {
                if (ni.getType() == ConnectivityManager.TYPE_MOBILE) {
                    supportMobileNetwork = true;
                    break;
                }
            }
        }
        mSupportMobileNetwork = supportMobileNetwork;


        PHASE2_PEAP_ADAPTER = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.wifi_peap_phase2_entries));
        PHASE2_PEAP_ADAPTER.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        PHASE2_FULL_ADAPTER = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.wifi_phase2_entries));
        PHASE2_FULL_ADAPTER.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        unspecifiedCert = getString(R.string.wifi_unspecified);
        mIpSettingsSpinner = findViewById(R.id.ip_settings);
        mIpSettingsSpinner.setOnClickListener(this);
        mProxySettings = findViewById(R.id.proxy_settings);
        mProxySettings.setOnClickListener(this);
        mProxySettings.setClickable(false);
        mIpSettingsSpinner.setClickable(false);
        mProxySettingsText = (TextView)findViewById(R.id.proxy_settings_text);
        mIpSettingText = (TextView)findViewById(R.id.ip_settings_text);
        mProxySummary = (TextView)findViewById(R.id.proxy_summary);
        mIpSummary = (TextView)findViewById(R.id.ip_summary);
        mPasswordView = (TextView) findViewById(R.id.password);
        mProxySummary.setText(getResources().getStringArray(R.array.wifi_proxy_settings)[mProxySettingsPosition]);
        mIpSummary.setText(getResources().getStringArray(R.array.wifi_ip_settings)[mIpSettingsPosition]);
        advancedSetting = (CheckBox)findViewById(R.id.wifi_advanced_togglebox);
        if (mAccessPoint == null) { // new network
            //mConfigUi.setTitle(R.string.wifi_add_network);
            mSsidView = (TextView) findViewById(R.id.ssid);
            mSsidView.addTextChangedListener(this);
            mSecurity =  findViewById(R.id.security);
            //mSecuritySpinner.setOnItemSelectedListener(this);
            mSecurity.setOnClickListener(this);
            mSummary = (TextView)findViewById(R.id.summary);
            divider = findViewById(R.id.divider);
//            if (mInXlSetupWizard) {
//                mView.findViewById(R.id.type_ssid).setVisibility(View.VISIBLE);
//                mView.findViewById(R.id.type_security).setVisibility(View.VISIBLE);
//                // We want custom layout. The content must be same as the other cases.
//
//                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
//                        R.layout.wifi_setup_custom_list_item_1, android.R.id.text1,
//                        context.getResources().getStringArray(R.array.wifi_security_no_eap));
//                mSecuritySpinner.setAdapter(adapter);
//            } else {
            findViewById(R.id.type).setVisibility(View.VISIBLE);
//            }

            showIpConfigFields();
            showProxyFields();
            findViewById(R.id.wifi_advanced_toggle).setVisibility(View.VISIBLE);
            advancedSetting.setOnCheckedChangeListener(this);


            //mConfigUi.setSubmitButton(context.getString(R.string.wifi_save));
        } else {
            //mConfigUi.setTitle(mAccessPoint.ssid);

            ViewGroup group = (ViewGroup) findViewById(R.id.info);

            NetworkInfo.DetailedState state = mAccessPoint.getState();
            int level = mAccessPoint.getLevel();
            addBlankRow(group);
            /*if (state != null) {
                addRow(group, R.string.wifi_status, Summary.get(this, state));
            }

            int level = mAccessPoint.getLevel();
            if (level != -1) {
                String[] signal = resources.getStringArray(R.array.wifi_signal);
                level=(signal.length-1 < level)? signal.length-1 : level;
                addRow(group, R.string.wifi_signal, signal[level]);
            }

            WifiInfo info = mAccessPoint.getInfo();
            if (info != null && info.getLinkSpeed() != -1) {
                addRow(group, R.string.wifi_speed, info.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS);
            }

            addRow(group, R.string.wifi_security, mAccessPoint.getSecurityString(false));*/

            boolean showAdvancedFields = false;
            if (mAccessPoint.networkId != INVALID_NETWORK_ID) {
                WifiConfiguration config = mAccessPoint.getConfig();
                if (config.getIpAssignment() == IpAssignment.STATIC) {
                    //mIpSettingsSpinner.setSelection(STATIC_IP);
                    showAdvancedFields = true;
                } else {
                    //mIpSettingsSpinner.setSelection(DHCP);
                }
                //Display IP addresses
                /*StaticIpConfiguration staticConfig = config.getStaticIpConfiguration();
                if (staticConfig != null && staticConfig.ipAddress != null) {
                    addRow(group, R.string.wifi_ip_address,
                            staticConfig.ipAddress.getAddress().getHostAddress());
                }*/
//                for(InetAddress a : config.linkProperties.getAddresses()) {
//                    addRow(group, R.string.wifi_ip_address, a.getHostAddress());
//                }

                if(config.getStaticIpConfiguration()!= null){
                    mIpSettingsPosition = 1;
                    mIpSummary.setText(getResources().getStringArray(R.array.wifi_proxy_settings)[mIpSettingsPosition]);
                }

                if (config.getProxySettings() == ProxySettings.STATIC) {
                    //mProxySettingsSpinner.setSelection(PROXY_STATIC);
                    mProxySettingsPosition = 1;
                    mProxySummary.setText(getResources().getStringArray(R.array.wifi_proxy_settings)[mProxySettingsPosition]);
                    showAdvancedFields = true;
                } else if (config.getProxySettings() == ProxySettings.PAC){
                    mProxySettingsPosition = 2;
                    mProxySummary.setText(getResources().getStringArray(R.array.wifi_proxy_settings)[mProxySettingsPosition]);
                    showAdvancedFields = true;
                    //mProxySettingsSpinner.setSelection(PROXY_NONE);
                }
            }
            //if (mAccessPoint.networkId == INVALID_NETWORK_ID || mEdit || mAccessPoint.getConfig().numAssociation != 0
             //       || mAccessPoint.getSummary().equals(getResources().getString(R.string.wifi_disabled_password_failure))) {
                showSecurityFields();
                showIpConfigFields();
                showProxyFields();
                findViewById(R.id.wifi_advanced_toggle).setVisibility(View.VISIBLE);
                advancedSetting.setOnCheckedChangeListener(this);
                if (showAdvancedFields) {
                    advancedSetting.setChecked(true);
                    findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
                }
            //}

            if (mEdit) {
                //mConfigUi.setSubmitButton(context.getString(R.string.wifi_save));
            } else {
                if (state == null && level != -1) {
                    //mConfigUi.setSubmitButton(context.getString(R.string.wifi_connect));
                } else {
                    //findViewById(R.id.ip_fields).setVisibility(View.GONE);
                }
                if (mAccessPoint.networkId != INVALID_NETWORK_ID) {
                    //mConfigUi.setForgetButton(context.getString(R.string.wifi_forget));
                }
            }

            if(isSubmittable()){
                connect.setEnabled(true);
                connect.setTextColor(getResources().getColor(R.color.button_enable_color));
            }else {
                connect.setEnabled(false);
                connect.setTextColor(getResources().getColor(R.color.button_disable_color));
            }
        }


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }

    @Override
    protected void onResume(){
        super.onResume();
        final View decorView = getWindow().getDecorView();
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(uiOptions);
        decorView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {

                    @Override
                    public boolean onPreDraw() {
                        decorView.setSystemUiVisibility(uiOptions);
                        return true;
                    }

                });
    }

    private List<AccessPoint> constructAccessPoints() {
        ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
        /** Lookup table to more quickly update AccessPoints by only considering objects with the
         * correct SSID.  Maps SSID -> List of AccessPoints with the given SSID.  */
        Multimap<String, AccessPoint> apMap = new Multimap<String, AccessPoint>();
        WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        WifiInfo mLastInfo = mWifiManager.getConnectionInfo();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                AccessPoint accessPoint = new AccessPoint(this, config);
                accessPoint.update(mLastInfo, null);
                accessPoint.setLayoutResource(R.layout.preference_wifi_access_point);
                accessPoints.add(accessPoint);
                apMap.put(accessPoint.ssid, accessPoint);
            }
        }

        final List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0 ||
                        result.capabilities.contains("[IBSS]")) {
                    continue;
                }

                boolean found = false;
                for (AccessPoint accessPoint : apMap.getAll(result.SSID)) {
                    if (accessPoint.update(result))
                        found = true;
                }
                if (!found) {
                    AccessPoint accessPoint = new AccessPoint(this, result);
                    accessPoint.setLayoutResource(R.layout.preference_wifi_access_point);
                    accessPoints.add(accessPoint);
                    apMap.put(accessPoint.ssid, accessPoint);
                }
            }
        }

        // Pre-sort accessPoints to speed preference insertion
        Collections.sort(accessPoints);
        return accessPoints;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        /*if(s.length() == 0 && mAccessPointSecurity != AccessPoint.SECURITY_EAP){
            connect.setEnabled(false);
            connect.setTextColor(getResources().getColor(R.color.button_disable_color));
        }else if((!mEdit || mAccessPointSecurity == SECURITY_PSK)&&mPasswordView != null && mPasswordView.getText().length()<8){
            connect.setEnabled(false);
            connect.setTextColor(getResources().getColor(R.color.button_disable_color));
        }else {
            connect.setEnabled(true);
            connect.setTextColor(getResources().getColor(R.color.button_enable_color));
        }*/
        if(isSubmittable()){
            connect.setEnabled(true);
            connect.setTextColor(getResources().getColor(R.color.button_enable_color));
        }else {
            connect.setEnabled(false);
            connect.setTextColor(getResources().getColor(R.color.button_disable_color));
        }
        //connect.setEnabled(true);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == mSecurity) {
            mAccessPointSecurity = position;
            showSecurityFields();
        } else if (parent == mEapMethodSpinner) {
            showSecurityFields();
        } else if (parent == mProxySettings) {
            showProxyFields();
        } else {
            showIpConfigFields();
        }
        //enableSubmitIfAppropriate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (buttonView.getId() == R.id.show_password) {
            int pos = mPasswordView.getSelectionEnd();
            mPasswordView.setInputType(
                    InputType.TYPE_CLASS_TEXT | (isChecked ?
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                            InputType.TYPE_TEXT_VARIATION_PASSWORD));
            if (pos >= 0) {
                ((EditText)mPasswordView).setSelection(pos);
            }
        } else if (buttonView.getId() == R.id.wifi_advanced_togglebox) {
            if (isChecked) {
                mProxySettings.setClickable(true);
                mIpSettingsSpinner.setClickable(true);
                mProxySettingsText.setTextColor(getResources().getColor(R.color.button_enable_color));
                mIpSettingText.setTextColor(getResources().getColor(R.color.button_enable_color));
                //findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
            } else {
                mProxySettings.setClickable(false);
                mIpSettingsSpinner.setClickable(false);
                mProxySettingsText.setTextColor(getResources().getColor(R.color.button_disable_color));
                mIpSettingText.setTextColor(getResources().getColor(R.color.button_disable_color));
                findViewById(R.id.staticip).setVisibility(View.GONE);
                findViewById(R.id.proxy_fields).setVisibility(View.GONE);
                findViewById(R.id.proxy_pac_field).setVisibility(View.GONE);
                //findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
                mProxySummary.setText(getResources().getStringArray(R.array.wifi_proxy_settings)[0]);
                mProxySettingsPosition = 0;
                mIpSummary.setText(getResources().getStringArray(R.array.wifi_ip_settings)[0]);
                mIpSettingsPosition = 0;
            }
            if(isSubmittable()){
                connect.setEnabled(true);
                connect.setTextColor(getResources().getColor(R.color.button_enable_color));
            }else {
                connect.setEnabled(false);
                connect.setTextColor(getResources().getColor(R.color.button_disable_color));
            }
        }

    }

    @Override
    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        switch (v.getId()){
            case R.id.proxy_settings:
                showLongClickDialog(this,getResources().getString(R.string.proxy_settings_title), getResources().getStringArray(R.array.wifi_proxy_settings),v,PROXYSETTING);
                break;
            case R.id.cancel:
                finish();
                break;
            case R.id.connect:
                submit();
                finish();
                break;
            case R.id.ip_settings:
                showLongClickDialog(this,getResources().getString(R.string.wifi_ip_settings), getResources().getStringArray(R.array.wifi_ip_settings),v,IPSETTING);
                break;
            case R.id.security:
                showLongClickDialog(this,getResources().getString(R.string.wifi_security), getResources().getStringArray(R.array.wifi_security),v,SECURITY);
                break;
            case R.id.l_method:
                List<CharSequence> list1 = new ArrayList<CharSequence>(Arrays.asList(getResources().getTextArray(R.array.wifi_eap_method)));
                if (mSupportMobileNetwork) {
                    List<CharSequence> list2 = new ArrayList<CharSequence>(Arrays.asList(getResources().getTextArray(R.array.wifi_eap_sim_method)));
                    list1.addAll(list2);
                }
                mEapItems = new String[list1.size()];
                for(int i=0;i<list1.size();i++){
                    mEapItems[i] = list1.get(i).toString();
                }
                showLongClickDialog(this,getResources().getString(R.string.wifi_eap_method), mEapItems,v,EAP);
                break;
            case R.id.l_phase2:
                showLongClickDialog(this,getResources().getString(R.string.please_select_phase2), getResources().getStringArray(R.array.wifi_phase2_entries),v,PHASE2);
                break;
            case R.id.l_ca_cert:
                showLongClickDialog(this,getResources().getString(R.string.wifi_eap_ca_cert), loadCertificates(Credentials.CA_CERTIFICATE),v,CERT);
                break;
            case R.id.l_user_cert:
                showLongClickDialog(this,getResources().getString(R.string.wifi_eap_user_cert), loadCertificates(Credentials.USER_PRIVATE_KEY),v,USERCERT);
                break;
            case R.id.button_back:
                finish();
                break;
        }
    }

    private class Multimap<K,V> {
        private final HashMap<K,List<V>> store = new HashMap<K,List<V>>();
        /** retrieve a non-null list of values with key K */
        List<V> getAll(K key) {
            List<V> values = store.get(key);
            return values != null ? values : Collections.<V>emptyList();
        }

        void put(K key, V val) {
            List<V> curVals = store.get(key);
            if (curVals == null) {
                curVals = new ArrayList<V>(3);
                store.put(key, curVals);
            }
            curVals.add(val);
        }
    }

    private void showIpConfigFields() {
        WifiConfiguration config = null;

        //findViewById(R.id.ip_fields).setVisibility(View.VISIBLE);

        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
            config = mAccessPoint.getConfig();
        }

        if (mIpSettingsPosition == STATIC_IP) {
            findViewById(R.id.staticip).setVisibility(View.VISIBLE);
            if (mIpAddressView == null) {
                mIpAddressView = (TextView) findViewById(R.id.ipaddress);
                //mIpAddressView.addTextChangedListener(this);
                mGatewayView = (TextView) findViewById(R.id.gateway);
                //mGatewayView.addTextChangedListener(this);
                mNetworkPrefixLengthView = (TextView) findViewById(
                        R.id.network_prefix_length);
                mNetworkPrefixLengthView.addTextChangedListener(this);
                mDns1View = (TextView) findViewById(R.id.dns1);
                //mDns1View.addTextChangedListener(this);
                mDns2View = (TextView) findViewById(R.id.dns2);
               // mDns2View.addTextChangedListener(this);
            }
            if (config != null) {
//                LinkProperties linkProperties = config.linkProperties;
//                Iterator<LinkAddress> iterator = linkProperties.getLinkAddresses().iterator();
//                if (iterator.hasNext()) {
//                    LinkAddress linkAddress = iterator.next();
//                    mIpAddressView.setText(linkAddress.getAddress().getHostAddress());
//                    mNetworkPrefixLengthView.setText(Integer.toString(linkAddress
//                            .getNetworkPrefixLength()));
//                }

//            	for (RouteInfo route : linkProperties.getRoutes()) {
//            		if (route.isDefaultRoute()) {
//            			mGatewayView.setText(route.getGateway().getHostAddress());
//            			break;
//            		}
//            	}
//
//            	Iterator<InetAddress> dnsIterator = linkProperties.getDnses().iterator();
//            	if (dnsIterator.hasNext()) {
//            		mDns1View.setText(dnsIterator.next().getHostAddress());
//            	}
//            	if (dnsIterator.hasNext()) {
//            		mDns2View.setText(dnsIterator.next().getHostAddress());
//            	}
                StaticIpConfiguration staticConfig = config.getStaticIpConfiguration();
                if (staticConfig != null) {
                    if (staticConfig.ipAddress != null) {
                        mIpAddressView.setText(
                                staticConfig.ipAddress.getAddress().getHostAddress());
                        mNetworkPrefixLengthView.setText(Integer.toString(staticConfig.ipAddress
                                .getNetworkPrefixLength()));
                    }

                    if (staticConfig.gateway != null) {
                        mGatewayView.setText(staticConfig.gateway.getHostAddress());
                    }

                    Iterator<InetAddress> dnsIterator = staticConfig.dnsServers.iterator();
                    if (dnsIterator.hasNext()) {
                        mDns1View.setText(dnsIterator.next().getHostAddress());
                    }
                    if (dnsIterator.hasNext()) {
                        mDns2View.setText(dnsIterator.next().getHostAddress());
                    }
                }
            }
        } else {
            findViewById(R.id.staticip).setVisibility(View.GONE);
        }
    }

    private void showProxyFields() {
        WifiConfiguration config = null;

        //findViewById(R.id.proxy_settings_fields).setVisibility(View.VISIBLE);

        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
            config = mAccessPoint.getConfig();
        }

        if (mProxySettingsPosition == PROXY_STATIC) {
            //findViewById(R.id.proxy_warning_limited_support).setVisibility(View.VISIBLE);
            findViewById(R.id.proxy_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.proxy_pac_field).setVisibility(View.GONE);
            if (mProxyHostView == null) {
                mProxyHostView = (TextView) findViewById(R.id.proxy_hostname);
                mProxyHostView.addTextChangedListener(this);
                mProxyPortView = (TextView) findViewById(R.id.proxy_port);
                mProxyPortView.addTextChangedListener(this);
                mProxyExclusionListView = (TextView) findViewById(R.id.proxy_exclusionlist);
                mProxyExclusionListView.addTextChangedListener(this);
            }
            if (config != null) {
//                ProxyProperties proxyProperties = config.linkProperties.getHttpProxy();
                ProxyInfo proxyProperties = config.getHttpProxy();
                if (proxyProperties != null) {
                    mProxyHostView.setText(proxyProperties.getHost());
                    mProxyPortView.setText(Integer.toString(proxyProperties.getPort()));
                    mProxyExclusionListView.setText(proxyProperties.getExclusionListAsString());
                }
            }
        } else if (mProxySettingsPosition == PROXY_PAC){
            findViewById(R.id.proxy_fields).setVisibility(View.GONE);
            findViewById(R.id.proxy_pac_field).setVisibility(View.VISIBLE);

            if (mProxyPacView == null) {
                mProxyPacView = (TextView) findViewById(R.id.proxy_pac);
                mProxyPacView.addTextChangedListener(this);
            }
            if (config != null) {
                ProxyInfo proxyInfo = config.getHttpProxy();
                if (proxyInfo != null) {
                    mProxyPacView.setText(proxyInfo.getPacFileUrl().toString());
                }
            }

        }  else{
            //findViewById(R.id.proxy_warning_limited_support).setVisibility(View.GONE);
            findViewById(R.id.proxy_pac_field).setVisibility(View.GONE);
            findViewById(R.id.proxy_fields).setVisibility(View.GONE);
        }
    }

    private void addRow(ViewGroup group, int name, String value) {
        View row = getLayoutInflater().inflate(R.layout.wifi_dialog_row, group, false);
        ((TextView) row.findViewById(R.id.name)).setText(name);
        ((TextView) row.findViewById(R.id.value)).setText(value);
        group.addView(row);
    }

    private void addBlankRow(ViewGroup group){
        View row = getLayoutInflater().inflate(R.layout.blank_row, group, false);
        group.addView(row);
    }

    private void showSecurityFields() {
//        if (mInXlSetupWizard) {
//            // Note: XL SetupWizard won't hide "EAP" settings here.
//            if (!((WifiSettingsForSetupWizardXL)mConfigUi.getContext()).initSecurityFields(mView,
//                        mAccessPointSecurity)) {
//                return;
//            }
//        }
        if (mAccessPointSecurity == AccessPoint.SECURITY_NONE) {
            findViewById(R.id.security_fields).setVisibility(View.GONE);
            return;
        }
        findViewById(R.id.security_fields).setVisibility(View.VISIBLE);

        if (mAccessPointSecurity == AccessPoint.SECURITY_WEP || mAccessPointSecurity == AccessPoint.SECURITY_PSK) {
            if(mPasswordView == null) {
                mPasswordView = (TextView) findViewById(R.id.password);
            }
            findViewById(R.id.password_layout).setVisibility(View.VISIBLE);
            mPasswordView.addTextChangedListener(this);

            if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
                mPasswordView.setHint(R.string.wifi_unchanged);
            }
        }
        final ImageView showPasswordImg = (ImageView)findViewById(R.id.show_password);
        showPasswordImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = mPasswordView.getSelectionEnd();
                mPasswordView.setInputType(
                        InputType.TYPE_CLASS_TEXT | (!showPassword ?
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                                InputType.TYPE_TEXT_VARIATION_PASSWORD));
                if (pos >= 0) {
                    ((EditText)mPasswordView).setSelection(pos);
                }
                if(showPassword){
                    showPasswordImg.setImageResource(R.drawable.asusres_icon_hide_uncheck);
                }else {
                    showPasswordImg.setImageResource(R.drawable.asusres_icon_hide_check);
                }
                showPassword = !showPassword;
            }
        });

        if (mAccessPointSecurity != AccessPoint.SECURITY_EAP) {
            findViewById(R.id.eap).setVisibility(View.GONE);
            return;
        }

        connect.setEnabled(true);
        connect.setTextColor(getResources().getColor(R.color.button_enable_color));

        findViewById(R.id.eap).setVisibility(View.VISIBLE);
        mEapSummary = (TextView)findViewById(R.id.eap_summary);

        if (mEapMethodSpinner == null) {
            mEapMethodSpinner = (Spinner) findViewById(R.id.method);
            mEapMethodSpinner.setOnItemSelectedListener(this);
            mEapMethod = findViewById(R.id.l_method);
            mEapMethod.setVisibility(View.VISIBLE);
            mEapMethod.setOnClickListener(this);


                ArrayList al = new ArrayList<CharSequence>(Arrays.asList(getResources().
                        getTextArray(R.array.wifi_eap_method)));
            if (mSupportMobileNetwork) {
                al.addAll(Arrays.asList(getResources().
                        getTextArray(R.array.wifi_eap_sim_method)));
            }
                ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
                        android.R.layout.simple_spinner_dropdown_item, al);
                mEapMethodSpinner.setAdapter(adapter);


            mPhase2Spinner = (Spinner) findViewById(R.id.phase2);
            mPhase2 = findViewById(R.id.l_phase2);
            mPhase2.setOnClickListener(this);
            mPhase2Summary = (TextView)findViewById(R.id.phase2_summary);
            //mEapCaCertSpinner = (Spinner) findViewById(R.id.ca_cert);
            certView = findViewById(R.id.l_ca_cert);
            certView.setOnClickListener(this);
            mCertSummary  = (TextView)findViewById(R.id.cert_summary);
            mCertSummary.setText(loadCertificates(Credentials.CA_CERTIFICATE)[mCertPosition]);

            //mEapUserCertSpinner = (Spinner) findViewById(R.id.user_cert);
            userCertView = findViewById(R.id.l_user_cert);
            userCertView.setOnClickListener(this);
            mUserCertSummary  = (TextView)findViewById(R.id.user_cert_summary);
            mUserCertSummary.setText(loadCertificates(Credentials.USER_PRIVATE_KEY)[mCertPosition]);

            mEapIdentityView = (TextView) findViewById(R.id.identity);
            mEapAnonymousView = (TextView) findViewById(R.id.anonymous);

            //loadCertificates(mEapCaCertSpinner, Credentials.CA_CERTIFICATE);
            //loadCertificates(mEapUserCertSpinner, Credentials.USER_PRIVATE_KEY);

            // Modifying an existing network
            if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
                WifiEnterpriseConfig enterpriseConfig = mAccessPoint.getConfig().enterpriseConfig;
                int eapMethod = enterpriseConfig.getEapMethod();
                List<CharSequence> list1 = new ArrayList<CharSequence>(Arrays.asList(getResources().getTextArray(R.array.wifi_eap_method)));
                if (mSupportMobileNetwork) {
                    list1.addAll(Arrays.asList(getResources().
                            getTextArray(R.array.wifi_eap_sim_method)));
                }
                mEapItems = new String[list1.size()];
                for(int i=0;i<list1.size();i++){
                    mEapItems[i] = list1.get(i).toString();
                }
                mEapMethodPosition = eapMethod;
                mEapSummary.setText(mEapItems[eapMethod]);
                int phase2Method = enterpriseConfig.getPhase2Method();
                mEapMethodSpinner.setSelection(eapMethod);
                showEapFieldsByMethod(eapMethod);
                switch (eapMethod) {
                    case WifiEnterpriseConfig.Eap.PEAP:
                        switch (phase2Method) {
                            case WifiEnterpriseConfig.Phase2.NONE:
                                mPhase2Spinner.setSelection(WIFI_PEAP_PHASE2_NONE);
                                break;
                            case WifiEnterpriseConfig.Phase2.MSCHAPV2:
                                mPhase2Spinner.setSelection(WIFI_PEAP_PHASE2_MSCHAPV2);
                                break;
                            case WifiEnterpriseConfig.Phase2.GTC:
                                mPhase2Spinner.setSelection(WIFI_PEAP_PHASE2_GTC);
                                break;
                            default:
                                //Log.e(TAG, "Invalid phase 2 method " + phase2Method);
                                break;
                        }
                        break;
                    default:
                        mPhase2Spinner.setSelection(phase2Method);
                        break;
                }
                //setSelection(mEapCaCertSpinner, enterpriseConfig.getCaCertificateAlias());
                //setSelection(mEapUserCertSpinner, enterpriseConfig.getClientCertificateAlias());
                mEapIdentityView.setText(enterpriseConfig.getIdentity());
                mEapAnonymousView.setText(enterpriseConfig.getAnonymousIdentity());
            } else {
                // Choose a default for a new network and show only appropriate
                // fields
                if (mSupportMobileNetwork) {
                    showEapFieldsByMethod(WifiEnterpriseConfig.Eap.PEAP);
                    mEapMethodSpinner.setSelection(WIFI_EAP_METHOD_SIM);
                    showEapFieldsByMethod(WIFI_EAP_METHOD_SIM);
                } else{
                    mEapMethodSpinner.setSelection(WifiEnterpriseConfig.Eap.PEAP);
                    showEapFieldsByMethod(WifiEnterpriseConfig.Eap.PEAP);
                }
            }
        } else {
            //showEapFieldsByMethod(mEapMethodSpinner.getSelectedItemPosition());
            showEapFieldsByMethod(mEapMethodPosition);
        }
    }

    private void loadCertificates(Spinner spinner, String prefix) {

        String[] certs = KeyStore.getInstance().list(prefix, android.os.Process.WIFI_UID);
        if (certs == null || certs.length == 0) {
            certs = new String[] {unspecifiedCert};
        } else {
            final String[] array = new String[certs.length + 1];
            array[0] = unspecifiedCert;
            System.arraycopy(certs, 0, array, 1, certs.length);
            certs = array;
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, certs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private String[] loadCertificates(String prefix) {

        String[] certs = KeyStore.getInstance().list(prefix, android.os.Process.WIFI_UID);
        if (certs == null || certs.length == 0) {
            certs = new String[] {unspecifiedCert};
        } else {
            final String[] array = new String[certs.length + 1];
            array[0] = unspecifiedCert;
            System.arraycopy(certs, 0, array, 1, certs.length);
            certs = array;
        }

        return certs;
    }

    /**
     * EAP-PWD valid fields include
     *   identity
     *   password
     * EAP-PEAP valid fields include
     *   phase2: MSCHAPV2, GTC
     *   ca_cert
     *   identity
     *   anonymous_identity
     *   password
     * EAP-TLS valid fields include
     *   user_cert
     *   ca_cert
     *   identity
     * EAP-TTLS valid fields include
     *   phase2: PAP, MSCHAP, MSCHAPV2, GTC
     *   ca_cert
     *   identity
     *   anonymous_identity
     *   password
     */
    private void showEapFieldsByMethod(int eapMethod) {
        // Common defaults
        findViewById(R.id.l_method).setVisibility(View.VISIBLE);
        findViewById(R.id.l_identity).setVisibility(View.VISIBLE);

        // Defaults for most of the EAP methods and over-riden by
        // by certain EAP methods
        findViewById(R.id.l_ca_cert).setVisibility(View.VISIBLE);
        findViewById(R.id.password_layout).setVisibility(View.VISIBLE);
        //findViewById(R.id.show_password_layout).setVisibility(View.VISIBLE);

        switch (eapMethod) {
            case WIFI_EAP_METHOD_PWD:
                setPhase2Invisible();
                setCaCertInvisible();
                setAnonymousIdentInvisible();
                setUserCertInvisible();
                break;
            case WIFI_EAP_METHOD_TLS:
                findViewById(R.id.l_user_cert).setVisibility(View.VISIBLE);
                setPhase2Invisible();
                setAnonymousIdentInvisible();
                setPasswordInvisible();
                break;
            case WIFI_EAP_METHOD_PEAP:
                // Reset adapter if needed
                if (mPhase2Adapter != PHASE2_PEAP_ADAPTER) {
                    mPhase2Adapter = PHASE2_PEAP_ADAPTER;
                    mPhase2Spinner.setAdapter(mPhase2Adapter);
                }
                findViewById(R.id.l_phase2).setVisibility(View.VISIBLE);
                findViewById(R.id.l_anonymous).setVisibility(View.VISIBLE);
                setUserCertInvisible();
                break;
            case WIFI_EAP_METHOD_TTLS:
                // Reset adapter if needed
                if (mPhase2Adapter != PHASE2_FULL_ADAPTER) {
                    mPhase2Adapter = PHASE2_FULL_ADAPTER;
                    mPhase2Spinner.setAdapter(mPhase2Adapter);
                }
                findViewById(R.id.l_phase2).setVisibility(View.VISIBLE);
                findViewById(R.id.l_anonymous).setVisibility(View.VISIBLE);
                setUserCertInvisible();
                break;

            case WIFI_EAP_METHOD_SIM:
            case WIFI_EAP_METHOD_AKA:
                setIdentityInvisible();
                setPhase2Invisible();
                setCaCertInvisible();
                setAnonymousIdentInvisible();
                setUserCertInvisible();
                setPasswordInvisible();
                break;
        }
    }

    private void setPhase2Invisible() {
        findViewById(R.id.l_phase2).setVisibility(View.GONE);
        mPhase2Spinner.setSelection(WifiEnterpriseConfig.Phase2.NONE);
    }

    private void setCaCertInvisible() {
        findViewById(R.id.l_ca_cert).setVisibility(View.GONE);
        //mEapCaCertSpinner.setSelection(unspecifiedCertIndex);
    }

    private void setUserCertInvisible() {
        findViewById(R.id.l_user_cert).setVisibility(View.GONE);
        //mEapUserCertSpinner.setSelection(unspecifiedCertIndex);
    }

    private void setAnonymousIdentInvisible() {
        findViewById(R.id.l_anonymous).setVisibility(View.GONE);
        mEapAnonymousView.setText("");
    }

    private void setPasswordInvisible() {
        mPasswordView.setText("");
        findViewById(R.id.password_layout).setVisibility(View.GONE);
        //findViewById(R.id.show_password_layout).setVisibility(View.GONE);
    }

    private void setIdentityInvisible() {
        View view = findViewById(R.id.l_identity);
        if (view != null)
            view.setVisibility(View.GONE);
        if (mEapIdentityView != null)
            mEapIdentityView.setText("");
    }

    private void setSelection(Spinner spinner, String value) {
        if (value != null) {
            @SuppressWarnings("unchecked")
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
            for (int i = adapter.getCount() - 1; i >= 0; --i) {
                if (value.equals(adapter.getItem(i))) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    public void submit() {

        final WifiConfiguration config = getConfig();

        if (config == null) {

            if (mAccessPoint.networkId != INVALID_NETWORK_ID) {
                mWifiManager.connect(mAccessPoint.networkId,
                        mConnectListener);
            }
        
        } else {
            if(!mEdit && mAccessPoint.getInfo() != null){
                mWifiManager.save(config, mSaveListener);
            }else {
                mWifiManager.connect(config, mConnectListener);
            }
            //if (mEdit) {
            //    mWifiManager.save(config, mSaveListener);
            //} else {
                //mWifiManager.connect(config, mConnectListener);

            //}
        }


    }

    /* package */ public WifiConfiguration getConfig() {
        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID && !mEdit) {
            //return null;
        }

        WifiConfiguration config = new WifiConfiguration();

        if (mAccessPoint == null) {
            config.SSID = AccessPoint.convertToQuotedString(
                    mSsidView.getText().toString());
            // If the user adds a network manually, assume that it is hidden.
            config.hiddenSSID = true;
        } else if (mAccessPoint.networkId == INVALID_NETWORK_ID) {
            config.SSID = AccessPoint.convertToQuotedString(
                    mAccessPoint.ssid);
        } else {
            config.networkId = mAccessPoint.networkId;
        }

        switch (mAccessPointSecurity) {
            case AccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;

            case AccessPoint.SECURITY_WEP:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                if (mPasswordView.length() != 0) {
                    int length = mPasswordView.length();
                    String password = mPasswordView.getText().toString();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58) &&
                            password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                break;

            case SECURITY_PSK:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                if (mPasswordView.length() != 0) {
                    String password = mPasswordView.getText().toString();
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_EAP:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                config.enterpriseConfig = new WifiEnterpriseConfig();
                //int eapMethod = mEapMethodSpinner.getSelectedItemPosition();
                int eapMethod = mEapMethodPosition;
                //int phase2Method = mPhase2Spinner.getSelectedItemPosition();
                int phase2Method = mPhase2Positon;
                config.enterpriseConfig.setEapMethod(eapMethod);
                switch (eapMethod) {
                    case WifiEnterpriseConfig.Eap.PEAP:
                        // PEAP supports limited phase2 values
                        // Map the index from the PHASE2_PEAP_ADAPTER to the one used
                        // by the API which has the full list of PEAP methods.
                        switch(phase2Method) {
                            case WIFI_PEAP_PHASE2_NONE:
                                config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.NONE);
                                break;
                            case WIFI_PEAP_PHASE2_PAP:
                                config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.PAP);
                                break;
                            case WIFI_PEAP_PHASE2_MSCHAP:
                                config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAP);
                                break;
                            case WIFI_PEAP_PHASE2_MSCHAPV2:
                                config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);
                                break;
                            case WIFI_PEAP_PHASE2_GTC:
                                config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.GTC);
                                break;
                            default:
                                //Log.e(TAG, "Unknown phase2 method" + phase2Method);
                                break;
                        }
                        break;
                    default:
                        // The default index from PHASE2_FULL_ADAPTER maps to the API
                        config.enterpriseConfig.setPhase2Method(phase2Method);
                        break;
                }
                String caCert = loadCertificates(Credentials.CA_CERTIFICATE)[mCertPosition];
                if (caCert.equals(unspecifiedCert)) caCert = "";
                config.enterpriseConfig.setCaCertificateAlias(caCert);
                String clientCert = loadCertificates(Credentials.USER_PRIVATE_KEY)[mCertPosition];
                if (clientCert.equals(unspecifiedCert)) clientCert = "";
                config.enterpriseConfig.setClientCertificateAlias(clientCert);
                config.enterpriseConfig.setIdentity(mEapIdentityView.getText().toString());
                config.enterpriseConfig.setAnonymousIdentity(
                        mEapAnonymousView.getText().toString());

                if (mPasswordView.isShown()) {
                    // For security reasons, a previous password is not displayed to user.
                    // Update only if it has been changed.
                    if (mPasswordView.length() > 0) {
                        config.enterpriseConfig.setPassword(mPasswordView.getText().toString());
                    }
                } else {
                    // clear password
                    config.enterpriseConfig.setPassword(mPasswordView.getText().toString());
                }
                break;
            default:
                return null;
        }

//        config.proxySettings = mProxySettings;
//        config.ipAssignment = mIpAssignment;
//        config.linkProperties = new LinkProperties(mLinkProperties);
        ipAndProxyFieldsAreValid();
        config.setIpConfiguration(
                new IpConfiguration(mIpAssignment, mProxySetting,
                        mStaticIpConfiguration, mHttpProxy));

        return config;
    }

    private void showLongClickDialog(final Context context,final String title,final String[] items,View v,final int type){

        dialog = new AlertDialog(this){
            @Override
            protected void onCreate(Bundle savedInstanceState) {

                Window window = getWindow();
                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
                //window.requestWindowFeature(Window.FEATURE_NO_TITLE);
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                window.getDecorView().setSystemUiVisibility(uiOptions);
                super.onCreate(savedInstanceState);
                setContentView(R.layout.choice_popup);

                final ListView listView = (ListView)findViewById(R.id.choice_list);
                TextView mTitle = (TextView)findViewById(R.id.title);
                mTitle.setText(title);
                ChoiceAdapter choiceAdapter = new ChoiceAdapter(context,items,type);
                listView.setAdapter(choiceAdapter);

                TextView cancel = (TextView) findViewById(R.id.cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (type){
                            case PROXYSETTING:
                                mProxySettingsPosition = position;
                                showProxyFields();
                                mProxySummary.setText(getResources().getStringArray(R.array.wifi_proxy_settings)[position]);
                                dialog.dismiss();
                                if(isSubmittable()){
                                    connect.setEnabled(true);
                                    connect.setTextColor(getResources().getColor(R.color.button_enable_color));
                                }else {
                                    connect.setEnabled(false);
                                    connect.setTextColor(getResources().getColor(R.color.button_disable_color));
                                }
                                break;
                            case IPSETTING:
                                mIpSettingsPosition = position;
                                showIpConfigFields();
                                mIpSummary.setText(getResources().getStringArray(R.array.wifi_ip_settings)[position]);
                                dialog.dismiss();
                                break;
                            case SECURITY:
                                mAccessPointSecurity = position;
                                showSecurityFields();
                                mSummary.setText(getResources().getStringArray(R.array.wifi_security)[position]);
                                if(position != 0){
                                    divider.setVisibility(View.VISIBLE);
                                }else {
                                    divider.setVisibility(View.GONE);
                                }
                                if(isSubmittable()){
                                    connect.setEnabled(true);
                                    connect.setTextColor(getResources().getColor(R.color.button_enable_color));
                                }else {
                                    connect.setEnabled(false);
                                    connect.setTextColor(getResources().getColor(R.color.button_disable_color));
                                }
                                dialog.dismiss();
                                break;
                            case EAP:
                                mEapMethodPosition = position;
                                mEapSummary.setText(mEapItems[position]);
                                showSecurityFields();
                                dialog.dismiss();
                                break;
                            case PHASE2:
                                mPhase2Summary.setText(getResources().getStringArray(R.array.wifi_phase2_entries)[position]);
                                mPhase2Positon = position;
                                dialog.dismiss();
                                break;
                            case CERT:
                                mCertSummary.setText(loadCertificates(Credentials.CA_CERTIFICATE)[mCertPosition]);
                                mCertPosition = position;
                                dialog.dismiss();
                                break;
                            case USERCERT:
                                mCertSummary.setText(loadCertificates(Credentials.USER_PRIVATE_KEY)[mUserCertPosition]);
                                mUserCertPosition = position;
                                dialog.dismiss();
                                break;
                        }

                    }
                });


            }

        };

        dialog.show();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Window win = dialog.getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.y = wm.getDefaultDisplay().getHeight() - getResources().getDimensionPixelOffset(R.dimen.alertdialog_height) - getResources().getDimensionPixelOffset(R.dimen.content_margin_top);
        win.setAttributes(lp);
    }

    class ChoiceAdapter extends BaseAdapter {
        Context mContext;
        String[] mItems;
        int mType;

        public ChoiceAdapter(Context context, String[] items, int type) {
            mContext = context;
            mItems = items;
            mType = type;
        }

        @Override
        public int getCount() {
            return mItems.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ChoiceAdapter.ViewHolder holder = null;
            if(convertView == null) {
                holder = new ChoiceAdapter.ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.choice_list_item, null);
                holder.name = (TextView)convertView.findViewById(R.id.name);
                holder.choice = (RadioButton)convertView.findViewById(R.id.choice) ;
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder)convertView.getTag();
            }

            holder.name.setText(mItems[position]);
            switch (mType){
                case PROXYSETTING:
                    if(position == mProxySettingsPosition){
                        holder.choice.setChecked(true);
                    }else {
                        holder.choice.setChecked(false);
                    }
                    break;
                case IPSETTING:
                    if(position == mIpSettingsPosition){
                        holder.choice.setChecked(true);
                    }else {
                        holder.choice.setChecked(false);
                    }
                    break;
                case SECURITY:
                    if(position == mAccessPointSecurity){
                        holder.choice.setChecked(true);
                    }else {
                        holder.choice.setChecked(false);
                    }
                    break;
                case EAP:
                    if(position == mEapMethodPosition){
                        holder.choice.setChecked(true);
                    }else {
                        holder.choice.setChecked(false);
                    }
                    break;
                case PHASE2:
                    if(position == mPhase2Positon){
                        holder.choice.setChecked(true);
                    }else {
                        holder.choice.setChecked(false);
                    }
                    break;
                case CERT:
                    if (position == mCertPosition){
                        holder.choice.setChecked(true);
                    }else {
                        holder.choice.setChecked(false);
                    }
                    break;
                case USERCERT:
                    if (position == mUserCertPosition){
                        holder.choice.setChecked(true);
                    }else {
                        holder.choice.setChecked(false);
                    }
                    break;

            }

            return convertView;
        }

        class ViewHolder{
            TextView name;
            RadioButton choice;
        }
    }


    boolean isSubmittable() {
        boolean enabled = false;
        boolean passwordInvalid = false;

        if (mPasswordView != null
                && ((mAccessPointSecurity == AccessPoint.SECURITY_WEP
                && mPasswordView.length() == 0)
                || (mAccessPointSecurity == AccessPoint.SECURITY_PSK
                && mPasswordView.length() < 8))) {
            passwordInvalid = true;
        }

//WAPI+++
       /* WAPI PSK >=8, and WAPI HEX PSK must be EVEN & HEX */
        /*if (mPasswordView != null && mAccessPointSecurity == AccessPoint.SECURITY_WAPI_PSK ) {
            if (mPasswordView.length() >= 8){
                passwordInvalid = true;
            }
        }*/
//WAPI---

        /*if ((mPasswordView != null && mPasswordView.length() == 0) &&
                (mAccessPoint != null && mAccessPoint.networkId != WifiConfiguration.INVALID_NETWORK_ID))
            passwordInvalid = false;*/

        if ((mSsidView != null && mSsidView.length() == 0) ||
                passwordInvalid) {

            enabled = false;
        } else {
            enabled = ipAndProxyFieldsAreValid();
        }
        /*if (mEapCaCertSpinner != null
                && mView.findViewById(R.id.l_ca_cert).getVisibility() != View.GONE) {
            String caCertSelection = (String) mEapCaCertSpinner.getSelectedItem();
            if (caCertSelection.equals(mUnspecifiedCertString)&&(mAccessPointSecurity == AccessPoint.SECURITY_EAP)) {
                // Disallow submit if the user has not selected a CA certificate for an EAP network
                // configuration.
                enabled = false;
            }
            if (caCertSelection.equals(mUseSystemCertsString)
                    && mEapDomainView != null
                    && mView.findViewById(R.id.l_domain).getVisibility() != View.GONE
                    && TextUtils.isEmpty(mEapDomainView.getText().toString())) {
                // Disallow submit if the user chooses to use system certificates for EAP server
                // validation, but does not provide a domain.
                enabled = false;
            }
        }
        if (mEapUserCertSpinner != null
                && mView.findViewById(R.id.l_user_cert).getVisibility() != View.GONE
                && ((String) mEapUserCertSpinner.getSelectedItem())
                .equals(mUnspecifiedCertString)) {
            // Disallow submit if the user has not selected a user certificate for an EAP network
            // configuration.
            enabled = false;
        }*/
        return enabled;
    }

    private boolean ipAndProxyFieldsAreValid() {
        mIpAssignment =
                (mIpSettingsSpinner != null
                        && mIpSettingsPosition == STATIC_IP)
                        ? IpAssignment.STATIC
                        : IpAssignment.DHCP;

        if (mIpAssignment == IpAssignment.STATIC) {
            mStaticIpConfiguration = new StaticIpConfiguration();
            int result = validateIpConfigFields(mStaticIpConfiguration);
            if (result != 0) {
                return false;
            }
        }

        final int selectedPosition = mProxySettingsPosition;
        mProxySetting = ProxySettings.NONE;
        mHttpProxy = null;
        if (selectedPosition == PROXY_STATIC && mProxyHostView != null && advancedSetting.isChecked()) {
            mProxySetting = ProxySettings.STATIC;
            String host = mProxyHostView.getText().toString();
            String portStr = mProxyPortView.getText().toString();
            String exclusionList = mProxyExclusionListView.getText().toString();
            int port = 0;
            int result = 0;
            try {
                port = Integer.parseInt(portStr);
                result = ProxySelector.validate(host, portStr, exclusionList);
            } catch (NumberFormatException e) {
                result = R.string.proxy_error_invalid_port;
            }
            if (result == 0) {
                mHttpProxy = new ProxyInfo(host, port, exclusionList);
            } else {
                return false;
            }
        } else if (selectedPosition == PROXY_PAC && mProxyPacView != null) {
            mProxySetting = ProxySettings.PAC;
            CharSequence uriSequence = mProxyPacView.getText();
            if (TextUtils.isEmpty(uriSequence)) {
                return false;
            }
            Uri uri = Uri.parse(uriSequence.toString());
            if (uri == null) {
                return false;
            }
            mHttpProxy = new ProxyInfo(uri);
        }else {
            mProxySetting = ProxySettings.NONE;
            mHttpProxy = null;
        }
        return true;
    }

    private int validateIpConfigFields(StaticIpConfiguration staticIpConfiguration) {
        if (mIpAddressView == null) return 0;

        String ipAddr = mIpAddressView.getText().toString();
        if (TextUtils.isEmpty(ipAddr)) return R.string.wifi_ip_settings_invalid_ip_address;

        InetAddress inetAddr = null;
        try {
            inetAddr = NetworkUtils.numericToInetAddress(ipAddr);
        } catch (IllegalArgumentException e) {
            return R.string.wifi_ip_settings_invalid_ip_address;
        }

        int networkPrefixLength = -1;
        try {
            networkPrefixLength = Integer.parseInt(mNetworkPrefixLengthView.getText().toString());
            if (networkPrefixLength < 0 || networkPrefixLength > 32) {
                return R.string.wifi_ip_settings_invalid_network_prefix_length;
            }
//            linkProperties.addLinkAddress(new LinkAddress(inetAddr, networkPrefixLength));
            staticIpConfiguration.ipAddress = new LinkAddress(inetAddr, networkPrefixLength);
        } catch (NumberFormatException e) {
            // Set the hint as default after user types in ip address
            //mNetworkPrefixLengthView.setText(mConfigUi.getContext().getString(
            //        R.string.wifi_network_prefix_length_hint));
        }

        String gateway = mGatewayView.getText().toString();
        if (TextUtils.isEmpty(gateway)) {
            try {
                //Extract a default gateway from IP address
                InetAddress netPart = NetworkUtils.getNetworkPart(inetAddr, networkPrefixLength);
                byte[] addr = netPart.getAddress();
                addr[addr.length-1] = 1;
                mGatewayView.setText(InetAddress.getByAddress(addr).getHostAddress());
            } catch (RuntimeException ee) {
            } catch (java.net.UnknownHostException u) {
            }
        } else {
            InetAddress gatewayAddr = null;
            try {
                gatewayAddr = NetworkUtils.numericToInetAddress(gateway);
            } catch (IllegalArgumentException e) {
                return R.string.wifi_ip_settings_invalid_gateway;
            }
//            linkProperties.addRoute(new RouteInfo(gatewayAddr));
            staticIpConfiguration.gateway = gatewayAddr;
        }

        String dns = mDns1View.getText().toString();
        InetAddress dnsAddr = null;

        if (TextUtils.isEmpty(dns)) {
            //If everything else is valid, provide hint as a default option
            //mDns1View.setText(mConfigUi.getContext().getString(R.string.wifi_dns1_hint));
        } else {
            try {
                dnsAddr = NetworkUtils.numericToInetAddress(dns);
            } catch (IllegalArgumentException e) {
                return R.string.wifi_ip_settings_invalid_dns;
            }
            staticIpConfiguration.dnsServers.add(dnsAddr);
        }

        if (mDns2View.length() > 0) {
            dns = mDns2View.getText().toString();
            try {
                dnsAddr = NetworkUtils.numericToInetAddress(dns);
            } catch (IllegalArgumentException e) {
                return R.string.wifi_ip_settings_invalid_dns;
            }
            staticIpConfiguration.dnsServers.add(dnsAddr);
        }
        return 0;
    }


}
