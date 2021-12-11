package com.netbyte.vtunnel.thread;

import android.app.NotificationManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.netbyte.vtunnel.model.AppConst;
import com.netbyte.vtunnel.model.Global;
import com.netbyte.vtunnel.model.Stat;
import com.netbyte.vtunnel.service.MyVPNService;
import com.netbyte.vtunnel.utils.FormatUtil;

import java.util.concurrent.TimeUnit;


public class NotifyThread extends BaseThread {
    private static final String TAG = "NotifyThread";
    private final NotificationManager notificationManager;
    private final NotificationCompat.Builder builder;

    public NotifyThread(NotificationManager notificationManager, NotificationCompat.Builder builder, MyVPNService vpnService) {
        this.notificationManager = notificationManager;
        this.builder = builder;
        this.vpnService = vpnService;
    }

    @Override
    public void run() {
        Log.i(TAG, "start");
        vpnService.startForeground(AppConst.NOTIFICATION_ID, builder.build());
        while (Global.RUNNING) {
            try {
                TimeUnit.SECONDS.sleep(1);
                if (TextUtils.isEmpty(Global.LOCAL_IP)) {
                    String title = "VPN failed!!!";
                    builder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle("").bigText(title));
                    break;
                }
                Stat.TOTAL_BYTES.addAndGet(Stat.DOWNLOAD_BYTES.get() + Stat.UPLOAD_BYTES.get());
                String text = String.format("Network: ↓ %s ↑ %s", FormatUtil.formatByte(Stat.DOWNLOAD_BYTES.get()), FormatUtil.formatByte(Stat.UPLOAD_BYTES.get()));
                String summary = String.format("IP: %s Total: %s", Global.LOCAL_IP, FormatUtil.formatByte(Stat.TOTAL_BYTES.get()));
                builder.setStyle(new NotificationCompat.BigTextStyle().setSummaryText(summary).setBigContentTitle("").bigText(text));
                notificationManager.notify(AppConst.NOTIFICATION_ID, builder.build());
                Stat.UPLOAD_BYTES.set(0);
                Stat.DOWNLOAD_BYTES.set(0);
            } catch (InterruptedException e) {
                Log.i(TAG, "error:" + e.getMessage());
            }
        }
        resetData();
        vpnService.stopForeground(true);
        notificationManager.cancel(AppConst.NOTIFICATION_ID);
        Log.i(TAG, "stop");
    }

    public void resetData() {
        // reset notification data
        Stat.UPLOAD_BYTES.set(0);
        Stat.DOWNLOAD_BYTES.set(0);
        Stat.TOTAL_BYTES.set(0);
        Log.i(AppConst.DEFAULT_TAG, "reset data");
    }
}
