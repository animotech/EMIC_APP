package jp.co.dst.emic_app;

import android.app.Activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import static android.app.PendingIntent.getActivity;

/**
 *メイン制御クラス
 */
public class EMIC_Activity extends AppCompatActivity  {
    private String spinnerItems[] = {"Spinner", "Android", "Apple", "Windows"};
    private static final String TAG = "EMIC_Activity";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private Oscillo mSurfaceView;
    private MeasureFragment mMesureFragment;
    private SurfaceView SV;
    private TextView connectText ;


    private static final int CONNECTDEVICE = 1;
    private static final int ENABLEBLUETOOTH = 2;
    private static final int Max_Size = 400;



    private static String[] CmdStr = {"GIN ","STT ","PW1 ","PW2 " ,"SWP ","ACL ","MES ","AGS ","ACK " ,"P2A " };
   // private static byte[] CmdCnt = { 9,9,9,9,7,7};
    private static byte[] CmdCnt = { 11,11,11,11,9,9,9,9,9,11};
    private  static int CMDNUMMMAX= 10;


    private static final byte StatusCmd[] = {0x53, 0x54, 0x54, 0x20};//FRQ_
    private static final byte  Cr[] ={0x0d};
    //private byte[][] RcvPacket = new byte[3][Max_Size];		// 受信バッファ
    private short[][] RcvQdata = new short[100][Max_Size];        // 受信バッ
    private short[][] RcvIdata = new short[100][Max_Size];        // 受信バッファ ファ
    private BluetoothAdapter BTadapter = null;
    private BluetoothControl BTclient;
    private UsbControl USBclient;
    private byte[] SndPacket = new byte[4];
    private int Index, qnum;
    private byte temp;
    private static byte cmd_detct = 0;
    private static byte cmdLength = 0;
    private static byte measure_do = 0;
    private static byte iqSel = 0;
    private byte[] RcvCmd = new byte[100];

    private Button sensorLed;
    private Button defLed;
    private Button overLed;
    private Button ngLed;
    private Button goLed;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private FreqSweepFragment mFreqSweepFragment;
    private AgcSettingSelectFragment mAgcSetSelFragment;
    private AgcSettingFragment mAgcSetFragment;
    private String AgcCalNumMain;
    private int pw1Int;
    private int pw2Int;
    private int pw2AvreageInt;
    private boolean defLedOffTimer;
    private boolean overLedOffTimer;
    private String fpgaStatusStr="0000";
    private boolean statusCheckFlag;
    private boolean usbEnable=false;
    private boolean detNG=false;


    private int fpgaStatus;



    public enum FuncEnable{NONE,MEASURE,AGS, }
    public FuncEnable funcflag=FuncEnable.NONE;
    private FuncEnable buf_funcflag=FuncEnable.NONE;


    /**
     * アクティビティ生成時
     * @param savedInstanceState　//保存データ
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //画面表示
        setContentView(R.layout.activity_main);
        //アクションバーの設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




        //表示初期化
        connectText = (TextView) findViewById(R.id.text0);
        connectText.setText("未接続");
        ngLed = (Button) findViewById(R.id.ngLED);
        sensorLed= (Button) findViewById(R.id.sensorLED);
        overLed = (Button) findViewById(R.id.overLED);
        defLed= (Button) findViewById(R.id.defLED);
        goLed= (Button) findViewById(R.id.goLED);


        Index = 0;




        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
       // Fragment mesureFragment = new MeasureFragment();
        mMesureFragment= new MeasureFragment();
        fragmentTransaction.replace(R.id.container, mMesureFragment);
   //     fragmentTransaction.addToBackStack(null); // 戻るボタンでreplace前に戻る
        Bundle bundle = new Bundle();
        bundle.putString("CalNum", AgcCalNumMain);
        mMesureFragment.setArguments(bundle);
        fragmentTransaction.commit();



        //USBを抜き差ししたときの動作を追加
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.setPriority(500);
        this.registerReceiver(mUsbReceiver, filter);
    }

    /*******
     * アクティビティ開始時（ストップからの復帰時）
     ****/
    @Override
    public void onStart() {
        super.onStart();

        if(statusCheckFlag==false) {
            statusCheckFlag=true;
            handler.postDelayed(satusCheckRunnable, 1000);
        }
    }

    /**
     *メニュー表示
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Makes this device discoverable.
     */

    private void ensureDiscoverable() {
        if (BTadapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {

            //Buletooth接続開始
            case R.id.secure_connect_scan: {
                if(usbEnable==false) {
                    //Buletoothアダプタ確保
                    if (BTadapter == null) {
                        BTadapter = BluetoothAdapter.getDefaultAdapter();
                    }
                    if (BTadapter == null) {
                        Toast.makeText(this, "Bluetoothは使用できません", Toast.LENGTH_LONG).show();
                    }

                    if (BTadapter.isEnabled() == false) {        // Bluetoorhが有効でない場合
                        // Bluetoothを有効にするダイアログ画面に遷移し有効化要求
                        Intent BTenable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        // 有効化されて戻ったらENABLEパラメータ発行
                        startActivityForResult(BTenable, ENABLEBLUETOOTH);
                    } else {
                        if (BTclient == null) {
                            // BluetoothClientをハンドラで生成
                            BTclient = new BluetoothControl(this, handler);
                        }
                    }

                    // Launch the DeviceListActivity to see devices and do scan

                    Intent serverIntent = new Intent(this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                }else{
                    Toast.makeText(getBaseContext(), "USBが接続されています。", Toast.LENGTH_LONG).show();
                }

                    return true;
            }
            /*今回は使用しないでおく　低セキュリティバージョン
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do
                if(usbEnable==false) {
                    Intent serverIntent = new Intent(this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                }else {
                    Toast.makeText(getBaseContext(), "USBが接続されています。", Toast.LENGTH_LONG).show();
                }
                return true;
            }
            */
            //USB接続　選択
            case R.id.usb_open: {

                //Bluetoothをストップ
                if (BTclient != null) {
                    BTclient.stop();
                    BTclient = null;
                }

                //すでにインスタンスがあれば実行しない
                if(USBclient ==null) {
                    USBclient = new UsbControl(this, handler);
                }
                //すでにイネーブルになっていたら実行しない
                if(usbEnable==false){
                    //USB接続開始
                    if(USBclient.connectFunction()==true) {
                        //成功
                        connectText.setTextColor(Color.GREEN);
                        connectText.setText("USB接続");
                        Log.d(TAG, "USB接続");
                        usbEnable=true;
                    }else{
                        //失敗
                        connectText.setTextColor(Color.RED);
                        connectText.setText("未接続");
                        Toast.makeText(getBaseContext(), "USB接続に失敗しました。", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "USB接続に失敗しました");
                        usbEnable=false;
                    }

                }else{
                    Toast.makeText(getBaseContext(), "すでにUSBが接続されています。", Toast.LENGTH_LONG).show();
                }
                return true;
            }
            /*
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }*/
        }
        return false;
//        return super.onOptionsItemSelected(item);
    }

    /**
     * シリアル送信処理　USBかBluetoothの有効な方の送信
     * @param buf 書き込みデータ
     */
    public void write(byte[] buf) {
        if (BTclient != null) {
            try {
            BTclient.write(buf);
            }catch(Exception e) {
                e.printStackTrace();
                Log.d(TAG, "BTclient　writeエラー");
            }
        }
        if(usbEnable==true) {
            try {
                USBclient.usbSendMessage(buf);
            }catch(Exception e) {
                e.printStackTrace();
                Log.d(TAG, "USBclient　writeエラー");
            }
        }
    }

    /********
     * BT端末接続処理の戻り値ごとの処理実行
     **********/
    private final Handler handler = new Handler() {
        // ハンドルメッセージごとの処理
        @Override
        public void handleMessage(Message msg) {

            byte[] buf = new byte[Max_Size];            // 受信バッファの用意
        ;
            int i;
            switch (msg.what) {
                case BluetoothControl.MESSAGE_STATECHANGE:
                    switch (msg.arg1) {
                        case BluetoothControl.STATE_CONNECTED:

                            connectText.setTextColor(Color.GREEN);
                            connectText.setText("Bluetooth接続完了");
                            //FirstFlag = 1;
                            break;
                        case BluetoothControl.STATE_CONNECTING:
                            connectText.setTextColor(Color.YELLOW);
                            connectText.setText("Bluetooth接続中");
                            break;
                        case BluetoothControl.STATE_NONE:
                            if(usbEnable == true){
                                connectText.setTextColor(Color.GREEN);
                                connectText.setText("USB接続");

                            }else {
                                  connectText.setTextColor(Color.RED);
                                connectText.setText("未接続");
                            }
                            break;
                    }
                    break;
                // BT受信処理
                case BluetoothControl.MESSAGE_READ:
                   buf = (byte[]) msg.obj;
               //     Log.d(TAG, "MESSAGE_READ");
        //            System.arraycopy((byte[]) msg.obj, 0,  buf, 0, msg.arg1);
                    //                   Log.d(TAG, "MESSAGE_READ"+Index);
                    //                      Log.d(TAG, "msg.arg1="+msg.arg1);

                    for (i = 0; i < msg.arg1; i++) {            // 受信バイト数だけ繰り返し

                        if (buf[i] == -128) {
                            //  コマンド検出またはＩＱデータヘッダ検出
                            cmd_detct = 1;
                            cmdLength = 0;
                        } else if (cmd_detct == 1) {

                            cmdLength++;
                            if (cmdLength == 1 && buf[i] == 0x44) {
                                //ＩＱデータヘッダ検出
                                measure_do = 1;
                                iqSel = 0;
                                cmd_detct = 0;
                            } else {
                                //コマンド受信
                                measure_do = 0;
                                if (buf[i] == 0x0d) {//改行
                                    cmdCheck(RcvCmd, cmdLength);
                                    cmd_detct = 0;
                                    cmdLength = 0;
                                } else {
                                    if (cmdLength <= 100) {
                                        RcvCmd[cmdLength - 1] = buf[i];
                                    } else {
                                        //エラー
                                        cmd_detct = 0;
                                        cmdLength = 0;

                                    }

                                }


                            }
                        } else if (measure_do == 1) {
                            // RcvPacket[qnum][Index]=buf[i];			// バッファコピー
                            if (iqSel == 0) {
                                RcvIdata[qnum][Index] = (short)Math.round(buf[i] * 1.574);            // バッファコピー
                                iqSel = 1;
                                //                                  Log.d(TAG, "idata" +i);
                            } else {
                                RcvQdata[qnum][Index] = (short)Math.round(buf[i]*1.574);            // バッファコピー
                                iqSel = 0;
                                //                              Log.d(TAG, "qdata" +i);
                                if (Index < Max_Size - 2) {
                                    Index++;                // バイトカウンタ更新
                                } else {
                                    Index = 0;                            // バイトカウンタリセット
                                    //                                     Log.d(TAG, "RCV" + RcvPacket[qnum]);

                                        mMesureFragment.putIdata(RcvIdata[qnum]);
                                        mMesureFragment.putQdata(RcvQdata[qnum]);

                                    if (qnum < 99) {
                                        qnum++;
                                    } else {
                                        qnum = 0;
                                    }
                                }
                            }
                        }

                    }

        //            Log.d(TAG, "MESSAGE_READ_END");
//                mSurfaceView.putData(RcvPacket);


//                        mSurfaceView.dummy();
                    //                  Index = 0;

                    break;//                       Log.d(TAG, "MESSAGE_READ END "+Index);
            }

        }

    };


    /**
     *　インテントからの結果　Buletoothに使用
     * @param requestCode　
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            //Bluetooth接続要求返答
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    BTclient = new BluetoothControl(this, handler);
                   //
                   // setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                   Log.d(TAG, "BT not enabled");
                   finish();
                }
        }
    }

    /**
     *Establish Buletooth connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = BTadapter.getRemoteDevice(address);
        // Attempt to connect to the device
        BTclient.connect(device);
    }

    /**
     * 受信したコマンドをチェック
     * @param data　受信データ
     * @param num　　受信サイズ
     */
    private void cmdCheck(byte[] data, byte num) {
           int  cmdsize;
           byte[] cmdby = new byte[30];
           int cmdnum=0,count=0;
           boolean cmdOk=false;
            int sum=0;

        //すべてのコマンドをチェック
        for( cmdnum=0; cmdnum < CMDNUMMMAX; cmdnum++ ){
            if(CmdCnt[cmdnum] == cmdLength) {  //まずは長さをチェック

                cmdsize = CmdStr[cmdnum].length();
                cmdby = CmdStr[cmdnum].getBytes();

                //コマンドが一致しているかをチェック　不一致で中止
                for ( count = 0; count < cmdsize; count++) {
                    if (data[count] != cmdby[count]) {
                        break;
                    }

                }

                 //チェックした数とコマンド文字数が同じなら一致していたのでtrueを返す
                if(count == cmdsize){
                    cmdOk =true;
                    break;
                }else{
                    cmdOk =false;
                }

            }else{
                //長さが不一致だったのでエラー
                cmdOk =false;
            }
        }

        //コマンドが一致していたらチェックサム比較
        if(  cmdOk == true) {

            //チェックサムを計算
            while (count < cmdLength-3) {
                sum =0xFF & (sum + (byte) ~data[count]);
                count++;
                sum =0xFF &(sum + (byte) data[count]);
                count++;
            }


            byte[] get_sum = new byte[2];
            get_sum[0] = data[cmdLength-3];
            get_sum[1] = data[cmdLength-2 ];

            //受信データはbyte[]なのでintにするために文字列にする
            String result = "0";
            try {
                result = new String(get_sum, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            int intGetSum=0;

            try {
                //受信データをintに変換
                intGetSum =0xFF& Integer.parseInt(result, 16);
                //チェックサム比較　違っていたらエラー
                if (sum != intGetSum) {
                    cmdOk = false;
                }
            } catch ( Exception e) {

                e.printStackTrace();
            }

        }

        //コマンドとチェックサムが一致したので各処理を行う
        if(  cmdOk=true){
            switch (cmdnum) {
                case 0:
                    if(mMesureFragment != null)
                        mMesureFragment.gainSet(data);
                    break;
                case 1:
                    status_check(data);
                    break;
                case 2:

                    pw1Set(data);
                    break;
                case 3:
  //                  if(mFreqSweepFragment != null)
  //                  mFreqSweepFragment.pw2_set(data);
                       pw2Set(data);

                    break;

                case 4:
                    if(mFreqSweepFragment != null)
                        mFreqSweepFragment.mpuSwpNUM_set(data);
                    break;

                case 5:
                    if(mAgcSetFragment != null)
                        mAgcSetFragment.mpuAgcCalNUM_set(data);
                    break;

                case 6:
                    measureSigRX(data);
                    break;


                case 7:
                    if(mMesureFragment != null)
                        mMesureFragment.agsSet(data);
                    break;

                case 8:
                    if(mMesureFragment != null)
                        mMesureFragment.ackSet();
                    break;

                case 9:
                    pw2AverageSet(data);
                    break;
            }
        }




    }


    /**
     * 本体からの外部制御による測定開始信号受信処理
     * @param data　受信したデータ
     */
    private  void measureSigRX(byte[] data) {

        if(mMesureFragment != null) {

            if(data[5]==0x31) {//測定終了信号
                if (funcflag == FuncEnable.MEASURE) {
                    funcflag = FuncEnable.NONE;
                    mMesureFragment.measureBt.setText("検査開始");
                }
            }
            if(data[5]==0x32) {//測定開始信号
                if (funcflag == FuncEnable.NONE) {
                    funcflag = FuncEnable.MEASURE;
                    mMesureFragment.measureBt.setText("検査停止");
                }
            }
        }
    }


    static private String pw1Str="0000";

    /**
     *PW1データの受信
     * @param data　Pw1データ
     */
    void pw1Set(byte[] data){

        byte[] now_pw1=new byte[4];


        System.arraycopy(data, 4, now_pw1, 0, 4);
        try {
            pw1Str = new String(now_pw1, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try{
            pw1Int=Integer.parseInt(pw1Str,16);
        } catch (NumberFormatException nfex) {
            nfex.printStackTrace();
        }
        Log.d(TAG,"pw1set="+pw1Int);
    };


    /**
     *PW2データの受信
     * @param data　Pw2データ
     */
    void pw2Set(byte[] data){
        byte[] now_pw2=new byte[4];
        String pw2Str="0000";

        System.arraycopy(data, 4, now_pw2, 0, 4);
        try {
            pw2Str = new String(now_pw2, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try{
            pw2Int=Integer.parseInt(pw2Str,16);
        } catch (NumberFormatException nfex) {
            nfex.printStackTrace();
        }
        Log.d(TAG,"pw2set="+ pw2Int);
    };
    /**
     *PW2データの受信
     * @param data　Pw2データ
     */
    void pw2AverageSet(byte[] data){
        byte[] now_pw2=new byte[4];
        String pw2AStr="0000";

        System.arraycopy(data, 4, now_pw2, 0, 4);
        try {
            pw2AStr = new String(now_pw2, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try{
            pw2AvreageInt=Integer.parseInt(pw2AStr,16);
        } catch (NumberFormatException nfex) {
            nfex.printStackTrace();
        }
        Log.d(TAG,"pw2set="+ pw2Int);
    };
    /**
     *　Pw2データを返す
     * @return　Pw2データ
     */
    public int pw2Get() {
    return  pw2Int;
    }

    /**
     *　Pw1データを返す
     * @return　Pw1データ
     */
    public int pw1Get() {
        return  pw1Int;
    }

    /**
     *
     * @return
     */
    public int pw2AverageGet() {
        return  pw2AvreageInt;
    }

    /**
     * 本体から受信したステータスをintにしてチェック
     * @param data 本体から受信したステータス
     */
    void status_check(byte[] data){
        byte[] Status = new byte[4];
        long longData = 0L;


        for(int count=0 ; count < 4 ; count++){
            Status[count]=data[count+4];//データをコピー
        }

        try {
            fpgaStatusStr = new String(Status, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

   try {
            fpgaStatus = Integer.parseInt( fpgaStatusStr, 16);
        }catch (NumberFormatException nfex){
            nfex.printStackTrace();
        }
        Log.d(TAG,"statuscheck"+fpgaStatus);
        //Sensor NG Check
        if(( fpgaStatus & 0x0C) != 0  ) {
            sensorLed.setBackgroundResource(R.drawable.circle_green);
        }else {
            sensorLed.setBackgroundResource(R.drawable.circle_black);
        }

        //Def sig Check
        if(( fpgaStatus & 0x01) != 0 ) {

            if (defLedOffTimer == false) {
                Log.e(TAG,"def1");
                 defLedOffTimer = true;
                defLed.setBackgroundResource(R.drawable.circle_green);
                handler.postDelayed(defLedRunnable, 500);
            }else{
                Log.e(TAG,"def1NG");

            }
        }

        //Over sig Check
        if(( fpgaStatus & 0xC000) != 0 ) {
            if (overLedOffTimer == false) {
                overLedOffTimer = true;
                overLed.setBackgroundResource(R.drawable.circle_green);
                handler.postDelayed(overLedRunnable, 500);
            }

        }
        //NG sig Check
        if(( fpgaStatus & 0x02) != 0 ) {
            detNG=true;
            ngLed.setBackgroundResource(R.drawable.circle_green);
        }else{

        }

    }

    /**
     * 受信した本体のステータスを返す
     * @return　受信した本体のステータス
     */
    public String getFpgaStatus(){
        return fpgaStatusStr;
    }

    /**
     *ＤＥＦＬＥＤを点灯後、一定時間経過で呼ばれＤＥＦＬＥＤを消灯する
     */
    private Runnable defLedRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG,"defoff");
            defLed.setBackgroundResource(R.drawable.circle_black);
            defLedOffTimer=false;
        }
    };



    /**
     *ＯＶＥＲＬＥＤを点灯後、一定時間経過で呼ばれＯＶＥＲＬＥＤを消灯する
     */
    private Runnable overLedRunnable = new Runnable() {
        @Override
        public void run() {
            overLed.setBackgroundResource(R.drawable.circle_black);
            overLedOffTimer=false;
        }
    };

    /**
     *ＤＥＦ及びＯＶＥＲＬＥＤは 一定期間で消灯してしまい、
     * ずっと１だと（ステータスが変わらないと）本体からコマンドが送られず点灯しないので、一定期間でチェックする。
     * ↑マイコン側で対応したので消した
     * 測定開始、及び測定停止も監視し、
     */
    private Runnable satusCheckRunnable = new Runnable() {
        @Override
        public void run() {
//Log.d(TAG,"statuscheck");
/*
            //Def sig Check
            if(( fpgaStatus & 0x01) != 0 ) {
                if (defLedOffTimer == false) {
                     defLed.setBackgroundResource(R.drawable.circle_green);
                    Log.e(TAG, "def2");
                    defLedOffTimer = true;
                    handler.postDelayed(defLedRunnable, 1000);
                }
            }

            //Over sig Check
            if(( fpgaStatus & 0xC000) != 0 ) {
                if (overLedOffTimer == false) {
                overLed.setBackgroundResource(R.drawable.circle_green);
                overLedOffTimer = true;
                handler.postDelayed(overLedRunnable, 1000);
                }

            }
  */
            if(buf_funcflag!=funcflag) {

                switch (buf_funcflag) {
                    case NONE:
                        //検査開始　GOとNGのLED 消灯
                        if (funcflag == FuncEnable.MEASURE) {
                            detNG=false;
                            ngLed.setBackgroundResource(R.drawable.circle_black);
                            goLed.setBackgroundResource(R.drawable.circle_black);

                        }
                        break;
                    case MEASURE:
                        //検査終了　傷がなければGOのLED 点灯
                  //      if (funcflag == FuncEnable.NONE) { //タイミングを考慮して消去
                        if (detNG == false) {
                            if ((fpgaStatus & 0x02) == 0) {
                                goLed.setBackgroundResource(R.drawable.circle_green);
                            }
                        }
                        break;
                }
                buf_funcflag=funcflag;
            }

            byte[] sum=new byte[1];
            byte[] SndStatus = new byte[4];
            try {

                SndStatus =getFpgaStatus().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            write(StatusCmd);            // 送信実行
            write(SndStatus);            // 送信実行
            sum[0] = (byte) (~SndStatus[0] + SndStatus[1] + ~SndStatus[2] + SndStatus[3]);
            write(sum);
            write(Cr);            // 送信実行

            if(statusCheckFlag==true)
            handler.postDelayed(satusCheckRunnable, 200);
        }
    };

    /**
     * USB broadcast receiver 抜き差しを検知
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String TAG = "FragL";
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                // when USB device attached to Android device
                Toast.makeText(getBaseContext(), "ATTACHED", Toast.LENGTH_LONG).show();
                Log.i(TAG, "ATTACHED...");



            }else if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
            {
                Toast.makeText(getBaseContext(), "DETACHED", Toast.LENGTH_LONG).show();
                Log.i(TAG,"DETACHED...");

                if(usbEnable == true)
                {
                    usbEnable=false;
                    USBclient.notifyUSBDeviceDetach();

//                    USBclient=null;
                    connectText.setTextColor(Color.RED);
                    connectText.setText("未接続");

                }
            }
        }
    };

    /**
     * 周波数掃引画面表示
     */
    void displayFreqSweep() {

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Fragment mesureFragment = new MeasureFragment();
       // if(mFreqSweepFragment==null)
            mFreqSweepFragment= new FreqSweepFragment();
        fragmentTransaction.replace(R.id.container, mFreqSweepFragment);
        fragmentTransaction.addToBackStack(null); // 戻るボタンでreplace前に戻る
        fragmentTransaction.commit();
    }


    /**
     *
     */
    void displayAgcSel() {

       FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Fragment mesureFragment = new MeasureFragment();

    //    if(mAgcSetSelFragment==null)

            mAgcSetSelFragment= new AgcSettingSelectFragment();
        fragmentTransaction.replace(R.id.container, mAgcSetSelFragment);
        fragmentTransaction.addToBackStack(null); // 戻るボタンでreplace前に戻る
        fragmentTransaction.commit();
    }

    /**
     *
     */
    void displayAgcSetting(String calNum) {

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Fragment mesureFragment = new MeasureFragment();


      //  if(mAgcSetFragment==null)
      mAgcSetFragment= new AgcSettingFragment();
        Bundle args = new Bundle();
        args.putString("CalNum", calNum);
        mAgcSetFragment.setArguments(args);
        fragmentTransaction.replace(R.id.container, mAgcSetFragment);
        fragmentTransaction.addToBackStack(null); // 戻るボタンでr     Bundle bundle = new Bundle();

        fragmentTransaction.commit();
    }

    /**
     *
     */
    void displayBackStack(){
        fragmentManager.popBackStack();
        ;
    }

    /**
     * 戻るボタンを押されたときの処理
     * 一つ前のフラグメントに戻る
     */
    @Override
    public void onBackPressed(){

        Log.d(TAG, "onBackPressed" + getFragmentManager().getBackStackEntryCount());
        if (0==getFragmentManager().getBackStackEntryCount()){
            super.onBackPressed();//これが実行されると終了される
        }else{
            fragmentManager.popBackStack();
        }
    }


    /**
     * 終了処理
     * usbとbluetoothを終了する
     */
    @Override
    protected void onDestroy() {
        statusCheckFlag=false;
        super.onDestroy();
        if (BTclient != null) {
            BTclient.stop();
            BTclient=null;
        }
        if (USBclient != null) {
            USBclient.stop();
            USBclient=null;
        }

    }
}
