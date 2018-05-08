package skill573.myapplication;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
public class MainActivity extends AppCompatActivity {
    //推播儲存變數
    public String prb;
    //連接網頁介質
    private WebView mWebView;
    private WebView noti ;
    private Object a;
    private boolean test;
    private TextView tv;
    //連結藍芽介質
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 1000000; //10 seconds 搜尋頻率 1S:1000
    private Handler mHandler;

    private NotificationManager notificationManager;

    private boolean mIsPageLoading;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prb="test1";
        tv=(TextView)findViewById(R.id.textView);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
        mWebView = (WebView) findViewById(R.id.wv);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("http://140.128.80.192:80/posts");
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //判断重定向的方式二
                if(mIsPageLoading) {
                    return false;
                }

                if(url != null && url.startsWith("http")) {
                    mWebView.loadUrl(url);
                    return true;
                } else {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        view.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mIsPageLoading= true;
                Log.d(TAG, "onPageStarted");
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mIsPageLoading= false;
                Log.d(TAG, "onPageFinished");
            }
        });
        //瀏覽介面
        noti=(WebView)findViewById(R.id.noti);
        noti.getSettings().setJavaScriptEnabled(true);
        noti.loadUrl("http://140.128.80.192:80/noti");
        noti.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url){
                noti.loadUrl("javascript:test.test()");
            }
        });

        //推播接收媒介
        mHandler = new Handler();
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //檢查是否支援藍芽
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "硬體不支援", Toast.LENGTH_SHORT).show();
        }
        // 檢查手機是否開啟藍芽裝置
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "請開啟藍芽裝置", Toast.LENGTH_SHORT).show();
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
        } else {
            scanLeDevice(true);
        }
    };

    private class JsOperation  //從網頁取值
    {
        @JavascriptInterface
        public void responseID(String ID,String title,String context,int number)
        {
            test=prb=="USBeacon" && ID=="2";
            tv.getTop();
            tv.setText(Boolean.toString(test));

            if((prb=="Dfutarg" && ID=="1")||(prb=="USBeacon" && ID == "2")){
                int notifyID = number; // 通知的識別號碼
                Notification notification = new Notification.Builder(getApplicationContext()).setSmallIcon(R.drawable.test).setContentTitle(title).setContentText(context).build(); // 建立通知
                notificationManager.notify(notifyID, notification); // 發送通知
            }
        }
    }
    // 掃描藍芽裝置

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                        //判斷是否有重新申請推播的必要
                        //BEACON 取其名稱

                    notificationManager.cancelAll();//清理舊的通知資料
                    prb=device.getName().toString();
                    noti.addJavascriptInterface(new JsOperation(),"test1");
                }
                };
    };


