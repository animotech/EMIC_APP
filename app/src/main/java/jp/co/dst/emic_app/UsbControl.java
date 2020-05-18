package jp.co.dst.emic_app;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
/**
 * Created by Mizukami Hisao on 2016/01/11.
 */
public class UsbControl {
    private final static String TAG = "UsbControl";

    private static D2xxManager ftD2xx = null;
    private final Handler handler;
    private final Context usbControlTContext;
    private D2xxManager.DriverParameters d2xxDrvParameter = null;
    private boolean usbReadThreadGoing = false;
    private readThread read_thread;
    private FT_Device ftDev;
    public static final int READBUF_SIZE = 4100;

    /**
     * コンストラクタによるAdapterのインスタンス生成
     * @param context
     * @param handler
     */
    public UsbControl(Context context, Handler handler) {
        //ハンドラを登録 Bluetoothと共通
        this.handler = handler;
        usbControlTContext = context;
        //ＦＴドライバインスタンス作製
        try {
            ftD2xx = D2xxManager.getInstance(context);
        } catch (D2xxManager.D2xxException ex) {
            Log.e(TAG, ex.toString());
        }


    }

    /**
     *
     * @return
     */
    public boolean connectFunction() {
        //すでにインスタンスがあるか確認
        if (ftDev != null) {
            //すでにオープンしているか確認
            if (ftDev.isOpen()) {
                //オープンしていたら設定してリターン
                setUsbConfig();

                return true;
            }
        }

        int devCount = 0;
        //D2XXデバイスのリストを作成　リストの数を受け取り
        devCount = ftD2xx.createDeviceInfoList(usbControlTContext);

        Log.d(TAG, "Device number : " + Integer.toString(devCount));

        //リストを受け取り
        D2xxManager.FtDeviceInfoListNode[] deviceList = new D2xxManager.FtDeviceInfoListNode[devCount];
        ftD2xx.getDeviceInfoList(devCount, deviceList);

        //パラメータを設定
        d2xxDrvParameter = new D2xxManager.DriverParameters();
        d2xxDrvParameter.setBufferNumber(16);
        d2xxDrvParameter.setMaxBufferSize(4096);//64の倍数でないとだめ   受信バッファのサイズ
        d2xxDrvParameter.setMaxTransferSize(512);//64の倍数でないとだめ　受信数がいくつになったらデバイスがタブレットに送信するかの設定　多いとデータが来るまで時間がかかった
        d2xxDrvParameter.setReadTimeout(5000);//リードをしたときのタイムアウトの時間？少ないとうけとれなかった。

        //デバイスがささってなかったらリターン
        if (devCount <= 0) {
            return false;


        }

        //デバイスのオープン処理　1台しかつながないのでインデックス０を指定
        if (ftDev == null) {
            ftDev = ftD2xx.openByIndex(usbControlTContext, 0, d2xxDrvParameter);
        } else {
            //ftDevがすでにあった場合同期をとる　実際はないはず
            synchronized (ftDev) {
                ftDev = ftD2xx.openByIndex(usbControlTContext, 0, d2xxDrvParameter);
            }
        }

        if (ftDev.isOpen()) {
            //オープンに成功していたら設定
            SetConfig(115200, (byte) 8, (byte) 1, (byte) 0, (byte) 0);
            ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
            ftDev.restartInTask();

        }else {
            Log.e(TAG, "connectFunction: device not open");
            return false;
        }

        //USBのリードスレッドを開始
        if(false == usbReadThreadGoing)
        {
            read_thread = new readThread(handler);
            read_thread.start();
            usbReadThreadGoing = true;
        }



        return true;
    }

    /**
     * ＵＳＢのコンフィグ設定　115200bps
     */
    private void setUsbConfig(){
        SetConfig(115200, (byte) 8, (byte) 1, (byte) 0, (byte) 0);
        ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));//クリア
        ftDev.restartInTask();
    }


    /**
     *USB切断処理
     */
    public void disconnectFunction()
    {
        Log.e(TAG, "disconnectFunction");
        //リードを終了
        usbReadThreadGoing = false;
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(ftDev != null)
        {
            synchronized(ftDev)
            {
                if( true == ftDev.isOpen())
                {
                    ftDev.close();
                }
            }
        }
    }

    /**
     *ＵＳＢ終了処理
     */
    public void stop()
    {
        disconnectFunction();
    }

    /**
     *USBが抜かれたときの処理
     */
    public void notifyUSBDeviceDetach()
    {
        disconnectFunction();
    }

    /**
     *ＵＳＢ送信処理
     * @param OutData　書き込みデータ
     */
    public  void usbSendMessage(byte[] OutData) {
        if (ftDev.isOpen() == false) {
            Log.e("j2xx", "SendMessage: device not open");
        }else{


            ftDev.setLatencyTimer((byte) 16);
 //           ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
            if(ftDev!=null) {
                ftDev.write(OutData, OutData.length);
            }
        }
    }


    /**
     *USBのシリアル設定
     * @param baud
     * @param dataBits
     * @param stopBits
     * @param parity
     * @param flowControl
     */
    public void SetConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        if (ftDev.isOpen() == false) {
            Log.e(TAG, "SetConfig: device not open");


            return;
        }

        // configure our port
        // reset to UART mode for 232 devices
        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

        ftDev.setBaudRate(baud);

        switch (dataBits) {
            case 7:
                dataBits = D2xxManager.FT_DATA_BITS_7;
                break;
            case 8:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
            default:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
        }

        switch (stopBits) {
            case 1:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
            case 2:
                stopBits = D2xxManager.FT_STOP_BITS_2;
                break;
            default:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
        }

        switch (parity) {
            case 0:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
            case 1:
                parity = D2xxManager.FT_PARITY_ODD;
                break;
            case 2:
                parity = D2xxManager.FT_PARITY_EVEN;
                break;
            case 3:
                parity = D2xxManager.FT_PARITY_MARK;
                break;
            case 4:
                parity = D2xxManager.FT_PARITY_SPACE;
                break;
            default:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
        }

        ftDev.setDataCharacteristics(dataBits, stopBits, parity);

        short flowCtrlSetting;
        switch (flowControl) {
            case 0:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
            case 1:
                flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
                break;
            case 2:
                flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
                break;
            case 3:
                flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
                break;
            default:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
        }

        // TODO : flow ctrl: XOFF/XOM
        // TODO : flow ctrl: XOFF/XOM
        ftDev.setFlowControl(flowCtrlSetting, (byte) 0x0b, (byte) 0x0d);
    }


    /**
     *USBリードスレッド
     */
    private class readThread  extends Thread
    {
        Handler mHandler;

        readThread(Handler h){
            mHandler = h;
  //         this.setPriority(7);
        }
        @Override
        public void run() {
            int i;
            int readSize=0;
            byte[][] buf = new byte[100][READBUF_SIZE];            // 受信バッファの用意
            byte[] Rcv = new byte[READBUF_SIZE];            // 取り出しバッファの用意
            int mReadSize;                    // 受信バイト数他
            byte Index;
            Index = 0;                                    // バイトカウンタリセット
            while(true == usbReadThreadGoing)
            {



//              synchronized (ftDev) {
       //           Log.e(TAG,"readThread");
                   readSize = ftDev.getQueueStatus();
                  //    readSize=ftDev.read(buf[Index]);

                    if (readSize > 0) {
                       Log.e(TAG,"readSize"+readSize);
                        if (readSize > READBUF_SIZE) {
                            readSize = READBUF_SIZE;
                        }
                        ftDev.read(buf[Index], readSize,5000);


                        handler.obtainMessage( BluetoothControl.MESSAGE_READ, readSize, -1, buf[Index]).sendToTarget();

                        if (Index >= 99) {
                            Index = 0;
                        } else {
                            Index++;
                        }


                    } // end of if(readSize>0)
  //             } // end of synchronized
            }
        }
    };

}