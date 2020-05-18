package jp.co.dst.emic_app;


import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.logging.Handler;

/**
 * 周波数掃引用フラグメント
 *.
 */
public class FreqSweepFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "FreqSweepFragment";




    private static final byte FRQCmd[] = {0x46, 0x52, 0x51, 0x20};//FRQ_
    private static final byte  FSWCmd[]={ 0x46, 0x53, 0x57, 0x20} ;//FSW_
    private static final byte  Cr[] ={0x0d};
    private byte[] SndSfrq = new byte[4];
    private byte[] now_pw1 = new byte[4];
    private byte[] now_pw2 = new byte[4];
    private int[] pw1 = new int[62];
    private int[] pw2 = new int[62];
    private long sfreq=0;
    private int sweepCnt=0;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    FreqSweepView freqView;
    private SurfaceView FSV;
    private boolean sweepTimerOn;
    Thread sweepTimerThread;
    private EMIC_Activity mainActivty;
    private String pw1Str="0000";
    private String pw2Str="0000";
    private int nowMpuSwpNum;
    private byte[] SndSweep;
    private int pw1Int;
    private int pw2Int;
    private Button swbutton;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FreqSweepFragment.
     */
/*
    // TODO: Rename and change types and number of parameters
    public static FreqSweepFragment newInstance(String param1, String param2) {
        FreqSweepFragment fragment = new FreqSweepFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
*/

    /**
     * 引数無しコンストラクタ
     */
    public FreqSweepFragment() {
        // Required empty public constructor
    }


    /**
     * 生成時関数　　特に何もせず
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    /*     if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    */
    }

    /**
     * 開始時画面作成
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_freqsweep, container, false);
        Button button3 = (Button) v.findViewById(R.id.returnButton);
        button3.setOnClickListener(this);
        swbutton = (Button) v.findViewById(R.id.sweepStartButton);
        swbutton.setOnClickListener(this);

        FSV = (SurfaceView) v.findViewById(R.id.surfaceView2);
        freqView = new FreqSweepView(getActivity(), FSV);
        mainActivty = (EMIC_Activity) getActivity();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "invoked onStart");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "invoked onDestroyView");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "invoked onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "invoked onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        sweepTimerOn=false;
        Log.d(TAG, "invoked onStop");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "invoked onDestroy");

    }

    /**
     * ボタンクリック時呼び出し関数
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //掃引開始
            case R.id.sweepStartButton:
                //すでに掃引が始まってなかったら開始
                if(sweepTimerOn == false) {
                    sfreq = 212000;
                    sweepCnt = 0;
                    nowMpuSwpNum = -1;
                    sweepTimerOn = true;
                    swbutton.setText("停止");
                    //掃引タイマー開始
                    sweeptTimerHandler.post(sweepTimerRunnable);
                //すでに始まっていたら停止
                }else{
                    sweepTimerOn = false;
                    swbutton.setText("周波数掃引開始");
                }

                break;
            //戻るボタン
            case R.id.returnButton:
                 mainActivty.displayBackStack();
                break;
        }
    }


    private android.os.Handler sweeptTimerHandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            if (sweepTimerThread != null) {
                sweepTimerThread.stop();
                sweepTimerThread = null;
            }
        }
    };

    /**
     * 掃引一定期間処理1
     */
    private Runnable sweepTimerRunnable = new Runnable() {

        int i;
        byte[] sum=new byte[1];

        @Override
        public void run() {
      /* do what you need to do */
            Log.d(TAG, "sweepTimerRunnable" + sweepCnt);

            //MPUから同じカウントが送られてきたら次に進む
            if(nowMpuSwpNum == sweepCnt) {

                Log.d(TAG, "pw1=" + pw1Str);
                Log.d(TAG, "pw2=" + pw2Str);

                //表示画面にデータをセット
                freqView.sweepDataSet(mainActivty.pw1Get(), mainActivty.pw2Get(), sweepCnt);
                //掃引回数が達するまで周波数を足す
                if (sweepCnt < 60) {
                    sweepCnt++;
                    sfreq = sfreq + 10000;
                //掃引回数が達したら終了処理
                } else {
                    sweepTimerOn = false;
                    swbutton.setText("周波数掃引開始");
                }
                Log.d(TAG, "sfreq=" + sfreq);
            }

            //設定する周波数をbyte[]に変換
            String frqstr = String.format("%09d", sfreq);
            try {
                SndSfrq = frqstr.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {

            }
            //送信するカウントをbyte[]に変換
            String swpstr = String.format("%02d", sweepCnt);
            try {
                SndSweep = swpstr.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {

            }
            //送信
            mainActivty.write(FRQCmd);            // 送信実行
            mainActivty.write(SndSfrq);            // 送信実行
           sum[0]=(byte)(~SndSfrq[0]+SndSfrq[1]+~SndSfrq[2]+SndSfrq[3]+~SndSfrq[4]+SndSfrq[5]+~SndSfrq[6]+SndSfrq[7]+~SndSfrq[8]);
            mainActivty.write(sum);            // 送信実行
            mainActivty.write(Cr);            // 送信実行

            if (sweepTimerOn == true)
                sweeptTimerHandler.postDelayed(sweepTimerRunnable2, 1000);
        }
    } ;

    /**
     * 掃引一定期間処理2　送信間隔を空けたいので別の関数とした
     */
    private Runnable sweepTimerRunnable2 = new Runnable() {

        @Override
        public void run() {
            byte[] sum=new byte[1];
            mainActivty.write(FSWCmd);            // 送信実行
            mainActivty.write(SndSweep);            // 送信実行
            sum[0]=(byte)(~SndSweep[0]+SndSweep[1]);
            mainActivty.write(sum);            // 送信実行
            mainActivty.write(Cr);            // 送信実行

            if (sweepTimerOn == true)
                sweeptTimerHandler.postDelayed(sweepTimerRunnable, 1000);

        }
    };


    /**
     *MPUから受信したカウントをint型にして保存
     * @param data
     */
    void mpuSwpNUM_set(byte[] data){
        byte[] mpuSwpNum= new byte[2];
        String  mpuSwpNumStr="00";
        System.arraycopy(data,4, mpuSwpNum, 0, 2);
        try {
            mpuSwpNumStr = new String(mpuSwpNum, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        try {
            nowMpuSwpNum = Integer.parseInt(mpuSwpNumStr, 16);//16進数を変換
        } catch (NumberFormatException nfex) {

            nfex.printStackTrace();
        }
        
        
        };


}