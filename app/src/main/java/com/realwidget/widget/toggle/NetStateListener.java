package com.realwidget.widget.toggle;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import com.realwidget.util.SwitchUtils;

public class NetStateListener extends PhoneStateListener {
    public static ServiceState serviceState;
    private Context mContext;

    public NetStateListener(Context context) {
        mContext = context;
    }

    @Override
    public void onDataConnectionStateChanged(int state) {
        super.onDataConnectionStateChanged(state);

        switch (state) {
            case TelephonyManager.DATA_DISCONNECTED:
                SwitchUtils.trafficStatis(mContext);
                break;
            default:
                break;
        }
    }

    @Override
    public void onServiceStateChanged(ServiceState pServiceState) {
        super.onServiceStateChanged(serviceState);

        // 通知widget更新
        serviceState = pServiceState;
    }
}