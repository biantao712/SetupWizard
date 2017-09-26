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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asus.cnsetupwizard.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.TelephonyIntents;
import com.asus.cnsetupwizard.R;
import com.asus.cnsetupwizard.TelephonyManagerMgr;
import com.asus.cnsetupwizard.WelcomeActivity;
import com.asus.cnsetupwizard.fingerprint.FingerprintActivity;
import com.asus.cnsetupwizard.utils.Constants;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

public class WifiSettingsFragment extends Fragment
        implements DialogInterface.OnClickListener {
    public static final String TAG = "WifiSettingsFragment";
    
    private static final boolean DEBUG = Constants.DEBUG;
    private static final int MENU_ID_WPS_PBC = Menu.FIRST;
    private static final int MENU_ID_WPS_PIN = Menu.FIRST + 1;
    private static final int MENU_ID_P2P = Menu.FIRST + 2;
    private static final int MENU_ID_ADD_NETWORK = Menu.FIRST + 3;
    private static final int MENU_ID_ADVANCED = Menu.FIRST + 4;
    private static final int MENU_ID_SCAN = Menu.FIRST + 5;
    private static final int MENU_ID_CONNECT = Menu.FIRST + 6;
    private static final int MENU_ID_FORGET = Menu.FIRST + 7;
    private static final int MENU_ID_MODIFY = Menu.FIRST + 8;

    private static final int WIFI_DIALOG_ID = 1;
    private static final int WPS_PBC_DIALOG_ID = 2;
    private static final int WPS_PIN_DIALOG_ID = 3;
    private static final int WIFI_SKIPPED_DIALOG_ID = 4;
    private static final int WIFI_AND_MOBILE_SKIPPED_DIALOG_ID = 5;

    // Combo scans can take 5-6s to complete - set to 10s.
    private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;

    // Instance state keys
    private static final String SAVE_DIALOG_EDIT_MODE = "edit_mode";
    private static final String SAVE_DIALOG_ACCESS_POINT_STATE = "wifi_ap_state";

    private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver;
    private final Scanner mScanner;

    private WifiManager.ActionListener mConnectListener;
    private WifiManager.ActionListener mSaveListener;
    private WifiManager.ActionListener mForgetListener;

    private CheckBox mCheckBox;
    private WifiEnabler mWifiEnabler;
    // An access point being editted is stored here.
    private AccessPoint mSelectedAccessPoint;

    private DetailedState mLastState;
    private WifiInfo mLastInfo;

    private final AtomicBoolean mConnected = new AtomicBoolean(false);

    //private WifiDialog mDialog;

    private TextView mEmptyView = null;
    //private WifiGridView gridview;
    private Context mActivityContext;

    /* Used in Wifi Setup context */

    // Save the dialog details
    private boolean mDlgEdit;
    private AccessPoint mDlgAccessPoint;
    private Bundle mAccessPointSavedState;

    /* End of "used in Wifi Setup context" */

    private String mTitleText = null; // The title text that displayed on the top of the fragment
    private String m_cellular_tips = null;
    private String m_cellular_tips_nosim = null;
    private String m_cellular_tips_nophone = null;
    private View wifiView;
    private View mWifiSignalView;
    private ImageView bgImage;
    private RotateAnimation anim;
    private String[] wifiStatus;
    private WifiManager mWifiManager;
    private int lastPosition = -1;
    private int currentPosition = -1;
    private boolean isNeedAutoAnimation = true;
    private DataConnectionContentObserver mDataConnectionObserver;
    private boolean isWifiOnly;
    private TelephonyManager mTelephonyManager;
    private String strWifiTitleNoPhone;
    private ListView wifiList;
    private CheckBox cellularSwitch;
    private CheckBox wifiSwitch;
    ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
    PopupWindow popupWindow;
    private TextView next;
    private boolean hasConnect = false;
    private boolean wifiEnable = true;
    private boolean cellularEnable = false;
    private AlertDialog dialog;


	//自定义的fragment最好有一个Public的参数为空的构造函数,否则app会偶发crash
    public WifiSettingsFragment() {
        //super(DISALLOW_CONFIG_WIFI);
        mFilter = new IntentFilter();
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };
        mScanner = new Scanner();
    }

    public WifiSettingsFragment(Context context) {
        //super(DISALLOW_CONFIG_WIFI);
        mActivityContext = context;
        //用来控制数据流量开关和检测sim是否存在
        mTelephonyManager = TelephonyManagerMgr.getInstance(mActivityContext).getTelephonyManager();
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        
        mFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };

        mScanner = new Scanner();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mTitleText = activity.getResources().getString(R.string.select_wifi_title);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        if (DEBUG)
            Log.i(TAG, "display WifiSettingsFragment");

        View fragView = inflater.inflate(R.layout.wifi_settings_fragment, container, false);
        wifiView = fragView;

        return fragView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mWifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        mConnectListener = new WifiManager.ActionListener() {
                                   @Override
                                   public void onSuccess() {
                                   }
                                   @Override
                                   public void onFailure(int reason) {
                                       Activity activity = getActivity();
                                       if (activity != null) {
										   if (DEBUG)
												Log.d(TAG,"I am in Line 343 and set true ");
                                           Toast.makeText(activity,
                                                R.string.wifi_failed_connect_message,
                                                Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               };

        mSaveListener = new WifiManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                }
                                @Override
                                public void onFailure(int reason) {
                                    Activity activity = getActivity();
                                    if (activity != null) {
                                        Toast.makeText(activity,
                                            R.string.wifi_failed_save_message,
                                            Toast.LENGTH_SHORT).show();
                                    }
                                }
                            };

        mForgetListener = new WifiManager.ActionListener() {
                                   @Override
                                   public void onSuccess() {
                                   }
                                   @Override
                                   public void onFailure(int reason) {
                                       Activity activity = getActivity();
                                       if (activity != null) {
                                           Toast.makeText(activity,
                                               R.string.wifi_failed_forget_message,
                                               Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               };

        if (savedInstanceState != null
                && savedInstanceState.containsKey(SAVE_DIALOG_ACCESS_POINT_STATE)) {
            mDlgEdit = savedInstanceState.getBoolean(SAVE_DIALOG_EDIT_MODE);
            mAccessPointSavedState = savedInstanceState.getBundle(SAVE_DIALOG_ACCESS_POINT_STATE);
        }

        //addPreferencesFromResource(R.xml.wifi_settings);
		
		isWifiOnly =  isWifiOnly();

		//shutdown the MobileData by default when the page oncreated
		/*if(mCheckBox.isEnabled()){
			Log.d(TAG,"shutdown the MobileData by default when the page oncreated ");
			mCheckBox.setChecked(false);
			mCheckBox.setButtonDrawable(R.drawable.btn_uncheck);
			setMobileDataEnabled(false);
		}*/

		mDataConnectionObserver = new DataConnectionContentObserver(new Handler());
        getContext().getContentResolver().registerContentObserver(android.provider.Settings.Global.getUriFor(android.provider.Settings.Global.MOBILE_DATA),
                                                        true, mDataConnectionObserver);

        //gridview = (WifiGridView) getView().findViewById(R.id.gridview);
        //gridview.setParentActivity(mActivityContext);
        TextView previous = (TextView) getView().findViewById(R.id.previous);
        next = (TextView) getView().findViewById(R.id.skip);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent();
                //intent.setAction(WelcomeActivity.ACTION);
                //startActivity(intent);
                getActivity().finish();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Intent accountIntent = new Intent();
                    ComponentName cn = new ComponentName("com.asus.launcher3", "com.asus.zenlife.activity.commonui.user.SetupWizardJumpActivity");
                    accountIntent.setComponent(cn);
                    //intent.setAction(FingerprintActivity.ACTION);
                    accountIntent.setAction("com.asus.zenlife.action.ACCOUNT_WIZARD");
                    accountIntent.putExtra("cnsetupwizard","wifisetting");
                    startActivity(accountIntent);
                    //getActivity().sendBroadcast(accountIntent);
                }catch (Exception e){
                    Intent intent = new Intent();
                    Log.d(TAG,"not found account activity");
                    intent.setAction(FingerprintActivity.ACTION);
                    startActivity(intent);
                }
                //intent.setAction("com.asus.zenlife.action.ACCOUNT_WIZARD");
                //getActivity().sendBroadcast(intent);

                //intent.setClassName("com.android.settings","com.asus.suw.lockscreen.SetupFingerprintEntryPoint");
                //intent.setAction("com.asus.zenlife.action.ACCOUNT_WIZARD");
                //intent.setPackage("com.asus.launcher3");
                //intent.putExtra("from","wizard");
               // startActivity(intent);
				/*Intent homeIntent = new Intent(Intent.ACTION_MAIN, null);
				homeIntent.addCategory(Intent.CATEGORY_HOME);
				homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				startActivity(homeIntent);*/
            }
        });

        wifiList = (ListView) getView().findViewById(R.id.wifi_list);
        View headerView = getActivity().getLayoutInflater().inflate(R.layout.wifi_add_network, null,false);
        final View footView = getActivity().getLayoutInflater().inflate(R.layout.add_new_wifi, null,false);

        wifiList.addHeaderView(headerView);
        wifiList.addFooterView(footView);
        footView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(null, true);
            }
        });
        cellularSwitch = (CheckBox)getView().findViewById(R.id.cellular_switch);
        wifiSwitch = (CheckBox)getView().findViewById(R.id.wifi_switch);
        wifiSwitch.setChecked(true);
        cellularSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    cellularSwitch.setChecked(true); // Always set check state based on current GPRS state
                    cellularSwitch.setButtonDrawable(R.drawable.cootek_asusres_btn_open_s);
                    Thread thread=new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setMobileDataEnabled(true);
                        }
                    });
                    thread.start();

                } else {
                    cellularSwitch.setChecked(false);
                    cellularSwitch.setButtonDrawable(R.drawable.cootek_asusres_btn_open_h);
                    Thread thread=new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setMobileDataEnabled(false);
                        }
                    });
                    thread.start();
                }

                cellularEnable = isChecked;
                if(!cellularEnable && wifiEnable && !hasConnect){
                    next.setText(getResources().getString(R.string.skip));
                }else {
                    next.setText(getResources().getString(R.string.next));
                }
            }
        });

        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mWifiManager.setWifiEnabled(isChecked);
                if(!isChecked){
                    next.setText(getResources().getString(R.string.next));
                    footView.setVisibility(View.GONE);
                    wifiList.setAdapter(null);
                }else {
                    next.setText(getResources().getString(R.string.skip));
                    footView.setVisibility(View.VISIBLE);
                }
                wifiEnable = isChecked;
                if(!cellularEnable && wifiEnable && !hasConnect){
                    next.setText(getResources().getString(R.string.skip));
                }else {
                    next.setText(getResources().getString(R.string.next));
                }
            }
        });
        wifiSwitch.setChecked(mWifiManager.isWifiEnabled());
        if(!mWifiManager.isWifiEnabled()){
            footView.setVisibility(View.GONE);
        }
        wifiList.setAdapter(null);


		//wifiStatus = getResources().getStringArray(R.array.wifi_status);
        //isNeedUpdateAccessPoints = true;
        //updateAccessPoints();
    }

	@Override
    public void onStart() {
        super.onStart();
        m_cellular_tips = getResources().getString(R.string.cellular_tips);
        m_cellular_tips_nosim = getResources().getString(R.string.cellular_tips_no_sim);
        m_cellular_tips_nophone = getResources().getString(R.string.cellular_tips_no_phone);
    
		wifiStatus = getResources().getStringArray(R.array.wifi_status);
        updateAccessPoints();
		
		strWifiTitleNoPhone = getResources().getString(R.string.select_wifi_title_no_phone);
    
		if(!isWifiOnly){
            checkSimState();
		}
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mWifiEnabler != null) {
            mWifiEnabler.resume();
        }

        getActivity().registerReceiver(mReceiver, mFilter);
        updateAccessPoints();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWifiEnabler != null) {
            mWifiEnabler.pause();
        }
        getActivity().unregisterReceiver(mReceiver);
        mScanner.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If the dialog is showing, save its state.
        /*if (mDialog != null && mDialog.isShowing()) {
            outState.putBoolean(SAVE_DIALOG_EDIT_MODE, mDlgEdit);
            if (mDlgAccessPoint != null) {
                mAccessPointSavedState = new Bundle();
                mDlgAccessPoint.saveWifiState(mAccessPointSavedState);
                outState.putBundle(SAVE_DIALOG_ACCESS_POINT_STATE, mAccessPointSavedState);
            }
        }*/
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
        /*if (info instanceof AdapterContextMenuInfo) {
            Preference preference = (Preference) getListView().getItemAtPosition(
                    ((AdapterContextMenuInfo) info).position);

            if (preference instanceof AccessPoint) {
                mSelectedAccessPoint = (AccessPoint) preference;
                menu.setHeaderTitle(mSelectedAccessPoint.ssid);
                if (mSelectedAccessPoint.getLevel() != -1
                        && mSelectedAccessPoint.getState() == null) {
                    menu.add(Menu.NONE, MENU_ID_CONNECT, 0, R.string.wifi_menu_connect);
                }
                if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
                    menu.add(Menu.NONE, MENU_ID_FORGET, 0, R.string.wifi_menu_forget);
                    menu.add(Menu.NONE, MENU_ID_MODIFY, 0, R.string.wifi_menu_modify);
                }
            }
        }*/
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mSelectedAccessPoint == null) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case MENU_ID_CONNECT: {
                if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
                    mWifiManager.connect(mSelectedAccessPoint.networkId,
                            mConnectListener);
                } else if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE) {
                    /** Bypass dialog for unsecured networks */
                    mSelectedAccessPoint.generateOpenNetworkConfig();
                    mWifiManager.connect(mSelectedAccessPoint.getConfig(),
                            mConnectListener);
                } else {
                    showDialog(mSelectedAccessPoint, true);
                }
                return true;
            }
            case MENU_ID_FORGET: {
                mWifiManager.forget(mSelectedAccessPoint.networkId, mForgetListener);
                return true;
            }
            case MENU_ID_MODIFY: {
                showDialog(mSelectedAccessPoint, true);
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    /*@Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        if (preference instanceof AccessPoint) {
            mSelectedAccessPoint = (AccessPoint) preference;
             //Bypass dialog for unsecured, unsaved networks
            if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE &&
                    mSelectedAccessPoint.networkId == INVALID_NETWORK_ID) {
                mSelectedAccessPoint.generateOpenNetworkConfig();
                mWifiManager.connect(mSelectedAccessPoint.getConfig(), mConnectListener);
            } else {
                showDialog(mSelectedAccessPoint, false);
            }
        } else {
            return super.onPreferenceTreeClick(screen, preference);
        }
        return true;
    }*/

    private void showDialog(AccessPoint accessPoint, boolean edit) {
        /*if (mDialog != null) {
            //removeDialog(WIFI_DIALOG_ID);
            mDialog = null;
        }*/

        // Save the access point and edit mode
        mDlgAccessPoint = accessPoint;
        mDlgEdit = edit;
        //showDialog(WIFI_DIALOG_ID);
        AccessPoint ap = mDlgAccessPoint; // For manual launch
        if (ap == null) { // For re-launch from saved state
            if (mAccessPointSavedState != null) {
                ap = new AccessPoint(getActivity(), mAccessPointSavedState);
                // For repeated orientation changes
                mDlgAccessPoint = ap;
                // Reset the saved access point data
                mAccessPointSavedState = null;
            }
        }
        // If it's still null, fine, it's for Add Network
        mSelectedAccessPoint = ap;
        //mDialog = new WifiDialog(getActivity(), this, ap, mDlgEdit);
        Intent intent = new Intent();
        if(ap != null){
            intent.putExtra("ssid",ap.ssid);
        }
        intent.putExtra("isEdit",edit);
        intent.setAction(WifiConnectActivity.ACTION);
        startActivity(intent);

        //mDialog.show();

    }



    /*@Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case WIFI_DIALOG_ID:
                AccessPoint ap = mDlgAccessPoint; // For manual launch
                if (ap == null) { // For re-launch from saved state
                    if (mAccessPointSavedState != null) {
                        ap = new AccessPoint(getActivity(), mAccessPointSavedState);
                        // For repeated orientation changes
                        mDlgAccessPoint = ap;
                        // Reset the saved access point data
                        mAccessPointSavedState = null;
                    }
                }
                // If it's still null, fine, it's for Add Network
                mSelectedAccessPoint = ap;
                mDialog = new WifiDialog(getActivity(), this, ap, mDlgEdit);
                return mDialog;
        }
        return super.onCreateDialog(dialogId);
    }*/

	//当AdapterView被单击(触摸屏或者键盘)，则返回的Item单击事件
    class  ItemClickListener implements OnItemClickListener
    {
		public void onItemClick(AdapterView<?> arg0,//The AdapterView where the click happened 
				                          View arg1,//The view within the AdapterView that was clicked
				                          int arg2,//The position of the view in the adapter
				                          long arg3//The row id of the item that was clicked
				                          ) {
			//itemConnectedWifi=(HashMap<String, Object>) arg0.getItemAtPosition(0);
			if (DEBUG)
				Log.d(TAG,"arg2="+arg2);

			lastPosition = currentPosition;
			currentPosition = arg2;

			//此处arg2=arg3
			HashMap<String, Object> item=(HashMap<String, Object>) lstImageItem.get(arg2-1);
			if (DEBUG)
				Log.d(TAG,"item.get(ItemText)="+item.get("ItemText"));

			final Collection<AccessPoint> accessPoints = constructAccessPoints();
			if(accessPoints.size() == 0) {
				//addMessagePreference(R.string.wifi_empty_list_wifi_on);
			}
			for (AccessPoint accessPoint : accessPoints) {
				//Log.e(TAG,"accessPoint.ssid="+accessPoint.ssid);
				if(accessPoint.ssid.equals((String)item.get("ItemSSID"))){
					mSelectedAccessPoint = accessPoint;
					Log.d(TAG,"The ssid of accessPoint which be clicked is "+accessPoint.ssid);
                    if(accessPoint.getState() != null){
                        return;
                    }else if(accessPoint.networkId != INVALID_NETWORK_ID){
                        mWifiManager.connect(mSelectedAccessPoint.networkId,
                                mConnectListener);
                        return;
                    }else if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE) {
                        /** Bypass dialog for unsecured networks */
                        mSelectedAccessPoint.generateOpenNetworkConfig();
                        mWifiManager.connect(mSelectedAccessPoint.getConfig(),
                                mConnectListener);
                        return;
                    }else {
                        showDialog(accessPoint, false);
                        return;
                    }

				}
			}
			
			bgImage = (ImageView ) arg1.findViewById(R.id.ItemBGImage);
			if(mWifiSignalView != null){
				mWifiSignalView.clearAnimation();
			}
			mWifiSignalView = arg1;

			//只有第一次才需要有自动动画。
			isNeedAutoAnimation = false;
		}    	
    }

    /**
     * Shows the latest access points available with supplimental information like
     * the strength of network and the security for it.
     */
    private void updateAccessPoints() {
        // Safeguard from some delayed event handling

        //new RuntimeException().printStackTrace();
        
        if (DEBUG)
			Log.d(TAG,"I am in updateAccessPoints.");
        if (getActivity() == null) return;
        
        //预防mActivityContext为null
        if ( null == mActivityContext ){
			Log.w(TAG,"Warnning: in updateAccessPoints mActivityContext is NULL!");
			return;
		}

        final int wifiState = mWifiManager.getWifiState();
        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                // AccessPoints are automatically sorted with TreeSet.
                final Collection<AccessPoint> accessPoints = constructAccessPoints();
                lstImageItem.clear();

                //ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
                final List<AccessPoint> accessPointsList = new ArrayList<AccessPoint>();
                for (AccessPoint accessPoint : accessPoints) {
                    //updateConnectedImg((Preference)accessPoint);
                    //getPreferenceScreen().addPreference(accessPoint);//mark by lei_guo                    
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    if(accessPoint.security >= 1){
						switch(accessPoint.getLevel()){
							case 0:
								map.put("ItemImage", R.drawable.asus_wifi_encryption_weak_signal_3);
								break;
							case 1:
								map.put("ItemImage", R.drawable.asus_wifi_encryption_weak_signal_2);
								break;
							case 2:
								map.put("ItemImage", R.drawable.asus_wifi_encryption_weak_signal_1);
								break;
							case 3:
								map.put("ItemImage", R.drawable.asus_wifi_encryption);
								break;
							default:
								map.put("ItemImage", R.drawable.asus_wifi_encryption);
								break;							
						}
					} else {
						switch(accessPoint.getLevel()){
							case 0:
								map.put("ItemImage", R.drawable.asus_wifi_weak_signal_3);
								break;
							case 1:
								map.put("ItemImage", R.drawable.asus_wifi_weak_signal_2);
								break;
							case 2:
								map.put("ItemImage", R.drawable.asus_wifi_weak_signal_1);
								break;
							case 3:
								map.put("ItemImage", R.drawable.asus_wifi_weak_signal_0);
								break;
							default:
								map.put("ItemImage", R.drawable.asus_wifi_weak_signal_0);
								break;
						}
					}
                    /*String shortSSID = accessPoint.ssid;
                    if(shortSSID.length()>9){
						shortSSID = shortSSID.substring(0,9);
						shortSSID = shortSSID + "...";
					}*/
                    /*if(accessPoint.getConfig() != null) {
                        Log.d("zwj", "ssid==" + accessPoint.ssid + " ,summary==" + accessPoint.getSummary() + " ,sss==" + accessPoint.getConfig().hiddenSSID);
                    }*/
                    //Log.d("zwj", "ssid==" + accessPoint.ssid + " ,summary==" + accessPoint.getSummary());
                    if(accessPoint.getConfig() != null && accessPoint.getConfig().hiddenSSID && accessPoint.getConfig().numAssociation == 0 && accessPoint.bssid == null
                            && (accessPoint.getSummary() == null || getResources().getString(R.string.wifi_not_in_range).equals(accessPoint.getSummary()))) {
                        continue;
                    }
                    //Log.d("zwj","ssid=="+accessPoint.ssid+" ,summary=="+accessPoint.getSummary()+" ,config=="+accessPoint.getConfig());
					map.put("ItemSSID", accessPoint.ssid);
                    map.put("ItemSummary",accessPoint.getSummary());
					//Show the green dot if it is connected
					if (wifiStatus[5].equals(accessPoint.getSummary ())) {
						if (DEBUG)
							Log.d(TAG,"accessPoint.getSummary ()" + accessPoint.getSummary ());
						//map.put("ItemBGImage", R.drawable.btn_wifi_check);
					//} else if (wifiStatus[4].equals(accessPoint.getSummary ()) ||
					//		(wifiStatus[3].equals(accessPoint.getSummary ())) ||
					//		(wifiStatus[2].equals(accessPoint.getSummary ()))) {
					//	if (DEBUG)
					//		Log.d(TAG,"accessPoint.getSummary11111111 ()" + accessPoint.getSummary ());
					//	map.put("ItemBGImage", R.drawable.btn_wifi_animation);//如果是正在连接的话，那就让他转起来
					//	isNeedAnimation = 1;
					} else {
						//map.put("ItemBGImage", R.drawable.btn_wifi_uncheck);
					}
                    accessPointsList.add(accessPoint);
					lstImageItem.add(map);
                }

                WifiAdapter wifiAdapter = new WifiAdapter(mActivityContext,lstImageItem,accessPointsList);
                wifiList.setAdapter(wifiAdapter);
                wifiList.setOnItemClickListener(new ItemClickListener());
                wifiList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        if(accessPointsList.get(position-1).getState() != null){
                            //showPupop(getContext(),accessPointsList.get(position-1).ssid,view,0);
                            showLongClickDialog(listener,accessPointsList.get(position-1).ssid,0);
                        }else if(accessPointsList.get(position-1).networkId != INVALID_NETWORK_ID){
                            showLongClickDialog(listener,accessPointsList.get(position-1).ssid,1);
                            //showPupop(getContext(),accessPointsList.get(position-1).ssid,view,1);
                        }else {
                            showLongClickDialog(listener,accessPointsList.get(position-1).ssid,2);
                            //showPupop(getContext(),accessPointsList.get(position-1).ssid,view,2);
                        }
                        mSelectedAccessPoint = accessPointsList.get(position-1);
                        return true;
                    }
                });

				/*gridview.setAdapter(new SimpleAdapter(mActivityContext, //没什么解释
															lstImageItem,//数据来源 
															R.layout.night_item,//night_item的XML实现
															//动态数组与ImageItem对应的子项        
															new String[] {"ItemImage","ItemText","ItemBGImage"},
															//ImageItem的XML文件里面的一个ImageView,两个TextView ID
															new int[] {R.id.ItemImage,R.id.ItemText,R.id.ItemBGImage})
				{
						@Override  
						public View getView(int position, View convertView, ViewGroup parent) {
							if(DEBUG)
								Log.d(TAG,"Adapter.position is " + position);
						  convertView = super.getView(position, convertView, parent); 
						  if(position == currentPosition && (mLastState == DetailedState.OBTAINING_IPADDR ||mLastState == DetailedState.CONNECTING ||mLastState == DetailedState.AUTHENTICATING) ){
							ImageView firstBGImage = (ImageView) convertView.findViewById(R.id.ItemBGImage);
							//if (null != firstBGImage) {
							//	firstBGImage.setImageResource(R.drawable.btn_wifi_uncheck);
							//} else {
							//	Log.e(TAG, "null == firstBGImage");
							//}
							firstBGImage.setImageResource(R.drawable.btn_wifi_animation);
							anim = new RotateAnimation(0f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
							anim.setInterpolator(new LinearInterpolator());
							anim.setRepeatCount(Animation.INFINITE);
							anim.setDuration(700);
							firstBGImage.startAnimation(anim);
						}
						  return convertView;
						}
				});*/
                for(int i = 0; i<accessPointsList.size(); i++){
                    if(accessPointsList.get(i).getState() != null){
                        hasConnect = true;
                        break;
                    }
                    if(i == accessPointsList.size()-1){
                        hasConnect = false;
                    }
                }

                if(!cellularEnable && wifiEnable && !hasConnect){
                    next.setText(getResources().getString(R.string.skip));
                }else {
                    next.setText(getResources().getString(R.string.next));
                }


				lastPosition = -1;
				currentPosition = -1;
				//gridview.setOnItemClickListener(new ItemClickListener());
                break;

            case WifiManager.WIFI_STATE_ENABLING:
                //getPreferenceScreen().removeAll();
                break;

            case WifiManager.WIFI_STATE_DISABLING:
                //addMessagePreference(R.string.wifi_stopping);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                setOffMessage();
                break;
        }
    }

    private void setOffMessage() {
        if (mEmptyView != null) {
            mEmptyView.setText(R.string.wifi_empty_list_wifi_off);
            if (Settings.Global.getInt(getActivity().getContentResolver(),
                    Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE, 0) == 1) {
                mEmptyView.append("\n\n");
                int resId;
                if (Settings.Secure.isLocationProviderEnabled(getActivity().getContentResolver(),
                        LocationManager.NETWORK_PROVIDER)) {
                    resId = R.string.wifi_scan_notify_text_location_on;
                } else {
                    resId = R.string.wifi_scan_notify_text_location_off;
                }
                CharSequence charSeq = getText(resId);
                mEmptyView.append(charSeq);
            }
        }
        //getPreferenceScreen().removeAll();
    }

    private void addMessagePreference(int messageId) {
        if (mEmptyView != null) mEmptyView.setText(messageId);
        //getPreferenceScreen().removeAll();
    }

    /** Returns sorted list of access points */
    private List<AccessPoint> constructAccessPoints() {
        ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
        /** Lookup table to more quickly update AccessPoints by only considering objects with the
         * correct SSID.  Maps SSID -> List of AccessPoints with the given SSID.  */
        Multimap<String, AccessPoint> apMap = new Multimap<String, AccessPoint>();

        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                AccessPoint accessPoint = new AccessPoint(getActivity(), config);
                accessPoint.update(mLastInfo, mLastState);
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
                    AccessPoint accessPoint = new AccessPoint(getActivity(), result);
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

    /** A restricted multimap for use in constructAccessPoints */
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

    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
        //checkSimState();
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {//打开关闭wifi事件
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action) ||//扫描到网络
                WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action) ||//网络配置变化
                WifiManager.LINK_CONFIGURATION_CHANGED_ACTION.equals(action)) {//连接状态变化，已经连上以后，更新信号
				if (DEBUG)
					Log.d(TAG,"I am in Line 720");
                updateAccessPoints();
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {//连接状态
            //Ignore supplicant state changes when network is connected
            //TODO: we should deprecate SUPPLICANT_STATE_CHANGED_ACTION and
            //introduce a broadcast that combines the supplicant and network
            //network state change events so the apps dont have to worry about
            //ignoring supplicant state change when network is connected
            //to get more fine grained information.
            SupplicantState state = (SupplicantState) intent.getParcelableExtra(
                    WifiManager.EXTRA_NEW_STATE);
            if (!mConnected.get() && SupplicantState.isHandshakeState(state)) {
				if (DEBUG)
					Log.d(TAG,"I am in Line 733");
                updateConnectionState(WifiInfo.getDetailedStateOf(state));//更新连接状态：获取IP之类
            } else {
                // During a connect, we may have the supplicant
                // state change affect the detailed network state.
                // Make sure a lost connection is updated as well.
                updateConnectionState(null);
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {//网络状态
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO);
            mConnected.set(info.isConnected());
            if (DEBUG)
				Log.d(TAG,"I am in Line 746");
            updateConnectionState(info.getDetailedState());
            updateAccessPoints();
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
			if (DEBUG)
				Log.d(TAG,"I am in Line 751");
            updateConnectionState(null);
        }else if(TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)){
			Log.d(TAG,"ACTION_SIM_STATE_CHANGED");
			checkSimState();
		}        
    }

    private void updateConnectionState(DetailedState state) {
        /* sticky broadcasts can call this when wifi is disabled */
        if (DEBUG)
			Log.d(TAG,"I am in updateConnectionState and state="+state);
        if (!mWifiManager.isWifiEnabled()) {
            mScanner.pause();
            return;
        }

        if (state == DetailedState.OBTAINING_IPADDR) {//正在获取IP
            mScanner.pause();
            //如果页面是第一次载入并且有wifi正在连接，那么show动画出来
            if(isNeedAutoAnimation){
				//showConnectingAnimation(0);
			}
        }else if (state == DetailedState.CONNECTING) {//正在连接
            mScanner.pause();
            //如果页面是第一次载入并且有wifi正在连接，那么show动画出来
            if(isNeedAutoAnimation){
				//showConnectingAnimation(0);
			}
        }else if (state == DetailedState.AUTHENTICATING) {//正在认证
            mScanner.pause();
            //如果页面是第一次载入并且有wifi正在连接，那么show动画出来
            if(isNeedAutoAnimation){
				//showConnectingAnimation(0);
			}
        }else if (state == DetailedState.CONNECTED || state == DetailedState.FAILED) {//
            mScanner.pause();
			//sleep 1sec 给一点时间让圈圈转起来
			/*Thread thread=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (DEBUG)
						Log.d(TAG,"I am in Line 1018 and set true ");
				}
			});
			thread.start();*/
        }else {
            mScanner.resume();
        }

        mLastInfo = mWifiManager.getConnectionInfo();
        if (state != null) {
            mLastState = state;
        }

        /*for (int i = getPreferenceScreen().getPreferenceCount() - 1; i >= 0; --i) {
            // Maybe there's a WifiConfigPreference
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof AccessPoint) {
                final AccessPoint accessPoint = (AccessPoint) preference;
                accessPoint.update(mLastInfo, mLastState);
                updateConnectedImg(preference);
            }
        }*/
    }

    private void updateConnectedImg(Preference p) {

        if(p != null) {

            String[] wifiStatus = getResources().getStringArray(R.array.wifi_status);

            //Show the green dot if it is connected
            if (wifiStatus[5].equals(p.getSummary ())) {
                p.setIcon(R.drawable.asus_wifi_connection);
            } else {
                p.setIcon(null);
            }
        }
    }

    private void updateWifiState(int state) {

        switch (state) {
            case WifiManager.WIFI_STATE_ENABLED:
                mScanner.resume();
                return; // not break, to avoid the call to pause() below

            case WifiManager.WIFI_STATE_ENABLING:
                //addMessagePreference(R.string.wifi_starting);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                setOffMessage();
                break;
        }

        mLastInfo = null;
        mLastState = null;
        mScanner.pause();
    }

    private class Scanner extends Handler {
        private int mRetry = 0;

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        void forceScan() {
            removeMessages(0);
            sendEmptyMessage(0);
        }

        void pause() {
            mRetry = 0;
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message message) {
            if (mWifiManager.startScan()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, R.string.wifi_fail_to_scan,
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
            sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {
        /*if (button == WifiDialog.BUTTON_FORGET && mSelectedAccessPoint != null) {
            forget();
        } else if (button == WifiDialog.BUTTON_SUBMIT) {
            if (mDialog != null) {
                submit(mDialog.getController());
            }
        }*/
    }

    /* package */ void submit(WifiConfigController configController) {

        final WifiConfiguration config = configController.getConfig();

        if (config == null) {
            if (mSelectedAccessPoint != null
                    && mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
                mWifiManager.connect(mSelectedAccessPoint.networkId,
                        mConnectListener);
            }
        } else if (config.networkId != INVALID_NETWORK_ID) {
            if (mSelectedAccessPoint != null) {
                mWifiManager.save(config, mSaveListener);
            }
        } else {
            if (configController.isEdit()) {
                mWifiManager.save(config, mSaveListener);
            } else {
                mWifiManager.connect(config, mConnectListener);
                
            }
        }

        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
		//add by lei_guo
		if (null != bgImage) {
		/*UE说 连上以后另外一个再取消绿色状态，所以不用去掉已连接的wifi的绿色圈圈
			View firstView = gridview.getChildAt(0);
			if(null != firstView){
				ImageView firstBGImage = (ImageView) firstView.findViewById(R.id.ItemBGImage);
				if (null != firstBGImage) {
					
					firstBGImage.setImageResource(R.drawable.btn_wifi_uncheck);
				} else {
					Log.e(TAG, "null == firstBGImage");
				}
			} else {
				Log.e(TAG, "null == firstView");
			}*/
			
			//点击wifi的时候前一个转圈要停止掉
			/*if(null != anim){
				anim.cancel();
				if(lastPosition != -1){
					View animView = gridview.getChildAt(lastPosition);
					if(null != animView){
						ImageView animBGImage = (ImageView) animView.findViewById(R.id.ItemBGImage);
						if (null != animBGImage) {
							animBGImage.setImageResource(R.drawable.btn_wifi_uncheck);
						} else {
							Log.w(TAG, "Warnning: animBGImage is NULL!");
						}
					} else {
						Log.w(TAG, "Warnning: animView is NULL!");
					}
				}
			}
			
			
			bgImage.setImageResource(R.drawable.btn_wifi_animation);
			anim = new RotateAnimation(0f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			anim.setInterpolator(new LinearInterpolator());
			anim.setRepeatCount(Animation.INFINITE);
			anim.setDuration(700);
			bgImage.startAnimation(anim);
			if (DEBUG)
				Log.d(TAG, "I am in submit(WifiConfigController configController)");*/
		}
    }

    /* package */ void forget() {
        if (mSelectedAccessPoint.networkId == INVALID_NETWORK_ID) {
            // Should not happen, but a monkey seems to triger it
            Log.e(TAG, "Failed to forget invalid network " + mSelectedAccessPoint.getConfig());
            return;
        }

        mWifiManager.forget(mSelectedAccessPoint.networkId, mForgetListener);

        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
        //点击forget以后应该可以updateAccessPoints
        if (DEBUG)
			Log.d(TAG,"I am in Line 1213 and set true ");
        updateAccessPoints();
    }

    /**
     * Refreshes acccess points and ask Wifi module to scan networks again.
     */
    /* package */ void refreshAccessPoints() {
        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }

        //getPreferenceScreen().removeAll();
    }

    /**
     * Called when "add network" button is pressed.
     */
    /* package */ void onAddNetworkPressed() {
        // No exact access point is selected.
        mSelectedAccessPoint = null;
        showDialog(null, true);
    }

    /* package */ int getAccessPointsCount() {
        final boolean wifiIsEnabled = mWifiManager.isWifiEnabled();
        if (wifiIsEnabled) {
            //return getPreferenceScreen().getPreferenceCount();
            return 0;
        } else {
            return 0;
        }
    }

    /**
     * Requests wifi module to pause wifi scan. May be ignored when the module is disabled.
     */
    /* package */ void pauseWifiScan() {
        if (mWifiManager.isWifiEnabled()) {
            mScanner.pause();
        }
    }

    /**
     * Requests wifi module to resume wifi scan. May be ignored when the module is disabled.
     */
    /* package */ void resumeWifiScan() {
        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
    }

    
    private void toggleMobileData(Context context, boolean enabled){
			ConnectivityManager connectivityManager =
			(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			Method setMobileDataEnabl;
			try {
				setMobileDataEnabl = connectivityManager.getClass().getDeclaredMethod("setMobileDataEnabled", boolean.class);
				setMobileDataEnabl.invoke(connectivityManager, enabled);
			} catch (Exception e) {
				e.printStackTrace();
			}
	 }
    
    private void setMobileDataEnabled(boolean enabled) {
		//TelephonyManager mTelephonyManager = TelephonyManager.from(mActivityContext);
        //Log.d(TAG, "mTelephonyManager  mTelephonyManager.getDataEnabled");
        //TelephonyManager mTelephonyManager = TelephonyManagerMgr.getInstance(mActivityContext).getTelephonyManager();
        if(mTelephonyManager != null)
        {
			if(DEBUG)
				Log.d(TAG, "enabled == " + enabled);
			if(mTelephonyManager.getDataEnabled() != enabled){
				mTelephonyManager.setDataEnabled(enabled);
			}
			if(DEBUG)
				Log.d(TAG, "getDataEnabled == " + mTelephonyManager.getDataEnabled());
				
		} else {
			Log.d(TAG, "mTelephonyManager == null");
			}
    }
    
    private void checkSimState() {
        //TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		//Log.d(TAG, "mTelephonyManager  mTelephonyManager.getSimState");
		if(isWifiOnly){
			return;
		}
		//TelephonyManager mTelephonyManager = TelephonyManagerMgr.getInstance(mActivityContext).getTelephonyManager();
		if(mTelephonyManager != null)
		{
			if(TelephonyManager.SIM_STATE_READY == mTelephonyManager.getSimState(2)){
				Log.d(TAG,"There is sim2 card in device!");
			}
			
			if( TelephonyManager.SIM_STATE_READY == mTelephonyManager.getSimState() || TelephonyManager.SIM_STATE_READY == mTelephonyManager.getSimState(2) || TelephonyManager.SIM_STATE_READY == mTelephonyManager.getSimState(1)  ){
				Log.d(TAG,"There is a available Sim card in device!");
                cellularSwitch.setEnabled(true);

				//mCheckBox.setButtonDrawable(R.drawable.btn_check);
				boolean isDataEnabled = mTelephonyManager.getDataEnabled();
				if(isDataEnabled){
                    cellularSwitch.setChecked(true); // Always set check state based on current GPRS state
                    cellularSwitch.setButtonDrawable(R.drawable.cootek_asusres_btn_open_s);
				} else {
                    cellularSwitch.setChecked(false);
                    cellularSwitch.setButtonDrawable(R.drawable.cootek_asusres_btn_open_h);
				}
			} else {
                cellularSwitch.setEnabled(false);
                cellularSwitch.setButtonDrawable(R.drawable.cootek_asusres_btn_open_h);
			}
		}
    }
    
	private class DataConnectionContentObserver extends ContentObserver {
        DataConnectionContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            //int enable = android.provider.Settings.Global.getInt(getContext().getContentResolver(),
            //        android.provider.Settings.Global.MOBILE_DATA, 1);
            //Log.d(TAG, "DataConnectionContentObserver onChange(): MOBILE_DATA=" + enable);
            checkSimState();
        }
    }
    
    /*private void showConnectingAnimation(int position) {
		View firstView = gridview.getChildAt(position);
		if(null != firstView){
			ImageView firstBGImage = (ImageView) firstView.findViewById(R.id.ItemBGImage);
			//if (null != firstBGImage) {
			//	firstBGImage.setImageResource(R.drawable.btn_wifi_uncheck);
			//} else {
			//	Log.e(TAG, "null == firstBGImage");
			//}
			firstBGImage.setImageResource(R.drawable.btn_wifi_animation);
			anim = new RotateAnimation(0f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			anim.setInterpolator(new LinearInterpolator());
			anim.setRepeatCount(Animation.INFINITE);
			anim.setDuration(700);
			firstBGImage.startAnimation(anim);
		} else {
			Log.e(TAG, "null == firstView");
		}
	}*/
	
	private boolean isWifiOnly(){
		TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return !(telephonyManager.isVoiceCapable());
	}
	
	private void setNoPhoneText(){
		//String strWifiTitleNoPhone = getResources().getString(R.string.select_wifi_title_no_phone);
		TextView textViewSwitchCellular = (TextView) wifiView.findViewById(R.id.text_switch_cellular);
		textViewSwitchCellular.setText(strWifiTitleNoPhone);
		
		if(m_cellular_tips_nophone != null)
		{
			TextView textViewCellularDeclare = (TextView) wifiView.findViewById(R.id.text_cellular_declare);
			textViewCellularDeclare.setText(m_cellular_tips_nophone);
		}
	}

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.connnect_network:
                    if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
                        mWifiManager.connect(mSelectedAccessPoint.networkId,
                                mConnectListener);
                    } else if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE) {
                        /** Bypass dialog for unsecured networks */
                        mSelectedAccessPoint.generateOpenNetworkConfig();
                        mWifiManager.connect(mSelectedAccessPoint.getConfig(),
                                mConnectListener);
                    } else {
                        showDialog(mSelectedAccessPoint, false);
                    }
                    dialog.dismiss();
                    break;
                case R.id.modify_network:
                    showDialog(mSelectedAccessPoint, false);
                    dialog.dismiss();
                    break;
                /*case R.id.disconnnect_network:
                    mWifiManager.disable(mSelectedAccessPoint.networkId, mForgetListener);
                    dialog.dismiss();
                    break;*/
                case R.id.delete_network:
                    mWifiManager.forget(mSelectedAccessPoint.networkId, mForgetListener);
                    dialog.dismiss();
                    break;
                /*case R.id.ignore_network:
                    if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
                        mWifiManager.forget(mSelectedAccessPoint.networkId, mForgetListener);
                    } else if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE) {
                        mWifiManager.forget(mSelectedAccessPoint.networkId, mForgetListener);
                    } else {
                        //showDialog(mSelectedAccessPoint, false);
                    }
                    dialog.dismiss();
                    break;*/
            }
        }
    };

    private void showLongClickDialog(final View.OnClickListener listener, final String title, final int type){

        dialog = new AlertDialog(getContext()){
            @Override
            protected void onCreate(Bundle savedInstanceState) {

                super.onCreate(savedInstanceState);
                //getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                            | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    //window.setStatusBarColor(Color.TRANSPARENT);
                    window.setNavigationBarColor(Color.TRANSPARENT);
                }
                setContentView(R.layout.wifi_long_click_item);
                TextView mTitle = (TextView)findViewById(R.id.title);
                mTitle.setText(title);
                View connectNetwork = findViewById(R.id.connnect_network);
                View modifynNetwork = findViewById(R.id.modify_network);
                //View disConnectNetwork = findViewById(R.id.disconnnect_network);
                View deleteNetwork = findViewById(R.id.delete_network);
                //View ignoreNetwork = findViewById(R.id.ignore_network);
                connectNetwork.setOnClickListener(listener);
                modifynNetwork.setOnClickListener(listener);
                //disConnectNetwork.setOnClickListener(listener);
                deleteNetwork.setOnClickListener(listener);
                //ignoreNetwork.setOnClickListener(listener);
                if (type == 0){
                    connectNetwork.setVisibility(View.GONE);
                }
                if (type == 1){
                    //disConnectNetwork.setVisibility(View.GONE);
                }
                if (type == 2){
                    modifynNetwork.setVisibility(View.GONE);
                    //disConnectNetwork.setVisibility(View.GONE);
                    deleteNetwork.setVisibility(View.GONE);
                }


            }
        };

        dialog.show();
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Window win = dialog.getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.y = wm.getDefaultDisplay().getHeight() - getResources().getDimensionPixelOffset(R.dimen.alertdialog_height) - getResources().getDimensionPixelOffset(R.dimen.content_margin_top);
        win.setAttributes(lp);
    }

}
