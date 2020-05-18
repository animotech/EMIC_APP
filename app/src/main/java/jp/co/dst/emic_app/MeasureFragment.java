package jp.co.dst.emic_app;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

/**
 * .
 */
public class MeasureFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {
    ;
    private static final String TAG = "MeasureFragment";
    private static final byte GainCmd[] = {0x47, 0x41, 0x49, 0x4e, 0x20};//GAIN_
    private static final byte HpfCmd[] = {0x48, 0x50, 0x46, 0x20};//HPS_
    private static final byte LpfCmd[] = {0x4c, 0x50, 0x46, 0x20};//LPS_
    private static final byte SphsCmd[] = {0x53, 0x50, 0x48, 0x53, 0x20};//SPHS_
    private static final byte SpheCmd[] = {0x53, 0x50, 0x48, 0x45, 0x20};//SPHE_
    private static final byte SpwCmd[] = {0x53, 0x50, 0x57, 0x20};//SPW_
    private static final byte FRQCmd[] = {0x46, 0x52, 0x51, 0x20};//FRQ_
    private static final byte DisChCmd[] = {0x44, 0x49, 0x53, 0x43, 0x20};//DISC_
    private static final byte MeaChCmd[] = {0x4d, 0x45, 0x41, 0x43, 0x20};//MEAC_
    private static final byte SETChCmd[] = {0x53, 0x45, 0x54, 0x43, 0x20};//SETC_
    private static final byte CICRCmd[] = {0x43, 0x49, 0x43, 0x52,0x20};//FRQ_
    private static final byte LthCmd[] = {0x4c, 0x54, 0x48, 0x20};//LTH_
    private static final byte LbkCmd[] = {0x4c, 0x42, 0x4b, 0x20};//LBK_
    private static final byte AphCmd[] = {0x41, 0x50, 0x48, 0x20};//APH_

    private static byte  FncCmd[]={ 0x46, 0x4e, 0x43, 0x20, 0x30} ;//FNC
    private static byte  Pw2AveNumCmd[]={ 0x50, 0x32, 0x4e, 0x20} ;//P2W
    private static byte  LOFcmd[]={ 0x4c, 0x4f, 0x46, 0x20} ;//LOF リフトオフ係数送信用
    private static final byte  Cr[] ={0x0d};
    private String pw2AveTime[] = {"10msec", "20msec", "40msec", "80msec","160msec", "320msec", "640msec", "1280msec"};
    private static double[] FeCalData ={
            0.5, //1mm
            0.6, //0.9mm
            0.7,
            0.8,
            0.9,
            1,
            1.1,
            1.2,
            1.3,
            1.4,
    };

    private static double[] AlCalData ={
            0.5, //1mm
            0.6, //0.9mm
            0.7,//0.8
            0.8,//0.7
            0.9,//0.6
            1,//0.5
            1.1,//0.4
            1.2,//0.3
            1.3,//0.2
            1.4,//0.1
    };

    private  double[] agcCorrectTable = new double[10];

    private static final String FilterStr[] = {"1.0", "1.2", "1.4", "1.7", "2", "2.4", "2.8", "3.4", "4", "4.8",//0-9
                                                     "5.6", "6.8", "8", "10", "12", "14", "17", "20", "24", "28",//10-19
            "34", "40", "48", "56", "68", "80", "100", "120", "140", "170",//20-29
            "200", "240", "280", "340", "400", "480", "560", "680", "800", "1.0k",//30-39
            "1.2k", "1.4k", "1.7k", "2.0k", "2.4k", "2.8k", "3.4k", "4.0k", "4.8k", "5.6k",//40-49
            "6.8k", "8.0k", "10k", "12k","14k","17k", "20k"};//50-56
    private static final double FilterDobule[] = { 1.0, 1.2, 1.4, 1.7, 2, 2.4, 2.8, 3.4, 4, 4.8,//0-9
            5.6, 6.8, 8, 10, 12, 14, 17, 20, 24, 28,//10-19
            34, 40, 48, 56, 68, 80, 100, 120, 140, 170,//20-29
            200, 240, 280, 340, 400, 480, 560, 680, 800, 1000,//30-39
            1200, 1400, 1700, 2000, 2400, 2800, 3400, 4000, 4800, 5600,//40-49
            6800, 8000, 10000, 12000,14000,17000, 20000};//50-56
    private static final int HPFMAX=52;
    private static final int LPFMAX=56;
    private static final int HpsNum[] = {0, 1, 2, 3, 4, 1, 2, 3, 4, 1,//"1.0", "1.2", "1.4", "1.7" ,"2", "2.4", "2.8", "3.4", "4", "4.8",
            2, 3, 4, 0, 1, 2, 3, 4, 1, 2,// "5.6", "6.8", "8", "10", "12","14", "17", "20", "24", "28
            3, 4, 1, 2, 3, 4, 0, 1, 2, 3,// "34", "40", "48", "56", "68", "80", "100", "120", "140", "170"
            4, 1, 2, 3, 4, 1, 2, 3, 4, 0,//"200", "240", "280", "340", "400", "480", "560", "680", "800", "1.0k",
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10,//1.2k", "1.4k", "1.7k", "2.0k", "2.4k", "2.8k", "3.4k", "6.8k", "8.0k", "10k",
            11,12,13, 14, 15, 16, 17, -1};

    private static final int LpsNum[][] ={
                    {0, 1, 2, 3, 4, 5, 6, 8, 9, 11,//"1.0", "1.2", "1.4", "1.7" ,"2", "2.4", "2.8", "3.4", "4", "4.8",
                    13, 15, 17, 19, 20, 21, 22, 23, 24, 25,// "5.6", "6.8", "8", "10", "12","14", "17", "20", "24", "28
                    26, 27, 28},

                    {0, 1, 2, 3, 4, 5, 6, 8, 9, 12,//"1.0", "1.2", "1.4", "1.7" ,"2", "2.4", "2.8", "3.4", "4", "4.8",
                    14, 16, 18, 19, 20, 21, 22, 23, 24, 25,// "5.6", "6.8", "8", "10", "12","14", "17", "20", "24", "28
                    27, 28, -1,},

                     {0, 1, 2, 3, 4, 5, 7, 8, 10, 12,//"1.0", "1.2", "1.4", "1.7" ,"2", "2.4", "2.8", "3.4", "4", "4.8",
                    14, 16, 18, 19, 20, 21, 22, 23, 24, 26,// "5.6", "6.8", "8", "10", "12","14", "17", "20", "24", "28
                    27, 29, -1,},

    };

    private static final byte CicNum[][] ={
                                                {0x36, 0x35},//100 65    0
                                                {0x36, 0x34},//200 64    1
                                                {0x36, 0x33},//400 63   2
                                                {0x33, 0x34},//1000 34  3
                                                {0x33, 0x33},//2000 33  4
                                                {0x33, 0x32},//4000 32  5
                                                {0x33, 0x31},//10000 31 6
                                                {0x33, 0x30},//20000 30 7
                                                {0x32, 0x30},//40000 20 8
                                                {0x31, 0x30}//100000 10 9

    }  ;

/*
            0:	1
            1:	2
            2:	5
            3:	10
            4:	20
            5:	40
            6:	50
*/





    static private int settingCh=1;
    static private int diplayCh=1;
    static private int measureCh=1;
    static private long[] dfrq= new long[]{512000,512000 , 512000,512000};
    static private int[] hpf=new int[]{8,7,6,5};//0-52
    static private int[] lpf=new int[]{25,24,23,22};//0-53
    static private int[] lpfIndex=new int[4];
    static private int cic=1;//0-8
    static private int[] gain=new int[]{1000,1100,1200,1300};
    static private int[] phase=new int[]{1000,1100,1200,1300};
    static private int locBlankingtime;
    static private int locThreshold;
    static private long cmpPHend;
    static private long cmpPHstart;
    static private int cmpPower= 64;
    private int cmpPhase1;
    private int cmpPhase2;

    private String AgcCalNum;
    private Button agsBt;

    TextView dfrqText;
    TextView gainText;
    TextView hpfText;
    TextView lpfText;
    TextView setChText;
    EMIC_Activity mainActivty;
    private Oscillo mSurfaceView;


    private SurfaceView SV;
    boolean isRepeat= false;
    boolean agsOn= false;
    boolean gosig= false;
    private boolean timerOn=false;
    public Button measureBt;
    private SQLiteDatabase sdb;
    private int[] calPw2Data=new int[AgcSettingFragment.CALNUMMAX];
    private int[] calPw1Data=new int[AgcSettingFragment.CALNUMMAX];
    private int btRepiatNUM;
    private int btRpeatCnt;
    private EditText dfrqEdit;
    private Spinner nSpinner;
    private static String[] dfrqUnit={" ","k","M"};
    private String AgcSampleType;
    private double agcCorrect;
    private Button agcBt;
    private EditText gainEdit;
    private EditText blankingEdit;
    private EditText thresholdEdit;
    static private String LPF_LIMIT_NOTIFY="ＨＰフィルタ設定値によりＬＰフィルタ設定値が制限されます。";
    private double reciprocal0_5mm;
    private int pre_lpf;
    private Toast toast1;
    private RadioGroup rg;
    private ProgressDialog progressDialog;
    private Spinner aveNumSpinner;
    private  TextView disChText;
    private  TextView measureChText;
    private  TextView phaseText;
    private  TextView thresholdText;
    private  TextView blankingText;

    private enum LongPush{NONE,LPFPLUS,LPFMINUS,HPFPLUS,HPFMINUS, FRQPLUS,FRQMINUS,GAINPLUS,GAINMINUS,
        CMPPWPLUS,CMPPWMINUS,CMPPH1PLUS,CMPPH1MINUS,CMPPH2PLUS,CMPPH2MINUS,
        PHASEPLUS,PHASEMINUS,BLANKINGPLUS,BLANKINGMINUS,THRESHOLDPLUS,THRESHOLDMINUS }


    LongPush longbtflag= LongPush.NONE;

    Thread cntThread;
    Thread timerThread;
    int intCount = 0;
    public MeasureFragment() {
        Log.d(TAG, "invoked MeasureFragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "invoked onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "invoked onCreateView");

        mainActivty= (EMIC_Activity) getActivity();

        View v = inflater.inflate(R.layout.fragment_measure, container, false);

        measureBt = (Button) v.findViewById(R.id.measureButton);
        measureBt.setOnClickListener(this);

      //  Button button2 = (Button)  v.findViewById(R.id.stop);
       // button2.setOnClickListener(this);

        Button agsbt = (Button)  v.findViewById(R.id.agsButton);
        agsbt.setOnClickListener(this);


        Button button3 = (Button)  v.findViewById(R.id.dfrqPbutton);
        button3.setOnClickListener(this);
        button3.setOnLongClickListener(this);
        button3.setOnTouchListener(this);

        Button button4 = (Button)  v.findViewById(R.id.dfrqMbutton);
        button4.setOnClickListener(this);
        button4.setOnLongClickListener(this);
        button4.setOnTouchListener(this);

        Button hpMbt = (Button)  v.findViewById(R.id.hpMbutton);
        hpMbt.setOnClickListener(this);
        hpMbt.setOnLongClickListener(this);
        hpMbt.setOnTouchListener(this);


        Button hpPbt = (Button)  v.findViewById(R.id.hpPbutton);
        hpPbt.setOnClickListener(this);
        hpPbt.setOnLongClickListener(this);
        hpPbt.setOnTouchListener(this);


        Button gainMbt = (Button)  v.findViewById(R.id.gainMbutton);
        gainMbt.setOnClickListener(this);
        gainMbt.setOnLongClickListener(this);
        gainMbt.setOnTouchListener(this);

        Button gainPbt = (Button)  v.findViewById(R.id.gainPbutton);
        gainPbt.setOnClickListener(this);
        gainPbt.setOnLongClickListener(this);
        gainPbt.setOnTouchListener(this);

        Button lpMbt = (Button)  v.findViewById(R.id.lpMbutton);
        lpMbt.setOnClickListener(this);
        lpMbt.setOnLongClickListener(this);
        lpMbt.setOnTouchListener(this);

        Button lpPbt = (Button)  v.findViewById(R.id.lpPbutton);
        lpPbt.setOnClickListener(this);
        lpPbt.setOnLongClickListener(this);
        lpPbt.setOnTouchListener(this);

        Button pwMbt = (Button)  v.findViewById(R.id.PwMbutton);
        pwMbt.setOnClickListener(this);
        pwMbt.setOnLongClickListener(this);
        pwMbt.setOnTouchListener(this);

        Button pwPbt = (Button)  v.findViewById(R.id.PwPbutton);
        pwPbt.setOnClickListener(this);
        pwPbt.setOnLongClickListener(this);
        pwPbt.setOnTouchListener(this);

        Button ph1Mbt = (Button)  v.findViewById(R.id.PH1Mbutton);
        ph1Mbt.setOnClickListener(this);
        ph1Mbt.setOnLongClickListener(this);
        ph1Mbt.setOnTouchListener(this);

        Button ph1Pbt = (Button)  v.findViewById(R.id.PH1Pbutton);
        ph1Pbt.setOnClickListener(this);
        ph1Pbt.setOnLongClickListener(this);
        ph1Pbt.setOnTouchListener(this);

        Button ph2Mbt = (Button)  v.findViewById(R.id.PH2Mbutton);
        ph2Mbt.setOnClickListener(this);
        ph2Mbt.setOnLongClickListener(this);
        ph2Mbt.setOnTouchListener(this);

        Button ph2Pbt = (Button)  v.findViewById(R.id.PH2Pbutton);
        ph2Pbt.setOnClickListener(this);
        ph2Pbt.setOnLongClickListener(this);
        ph2Pbt.setOnTouchListener(this);

        Button freqbt = (Button)  v.findViewById(R.id.freqSweepButton);
        freqbt.setOnClickListener(this);

        Button agcSetbt = (Button)  v.findViewById(R.id.agcSelButton);
        agcSetbt.setOnClickListener(this);

         agcBt = (Button)  v.findViewById(R.id.agcButton);
        agcBt.setOnClickListener(this);
        if( agcOnFlag == true) {
            agcBt.setText("LOC OFF");

        } else {
            agcBt.setText("LOC ON");
        }

        Button dfrqbt = (Button)  v.findViewById(R.id.dfrqEnterButton);
        dfrqbt.setOnClickListener(this);

        Button gainbt = (Button)  v.findViewById(R.id.gainEnterButton);
        gainbt.setOnClickListener(this);

        agsBt = (Button)  v.findViewById(R.id.agsButton);
        agsBt.setOnClickListener(this);
        Button afsBt = (Button)  v.findViewById(R.id.autoFilterButton);
        afsBt.setOnClickListener(this);



        Button setChMbt = (Button)  v.findViewById(R.id.setChMbutton);
        setChMbt.setOnClickListener(this);
        setChMbt.setOnLongClickListener(this);
        setChMbt.setOnTouchListener(this);

        Button setChPbt = (Button)  v.findViewById(R.id.setChPbutton);
        setChPbt.setOnClickListener(this);
        setChPbt.setOnLongClickListener(this);
        setChPbt.setOnTouchListener(this);
        setChText=(TextView) v.findViewById(R.id.setChtextView);

        Button disChMbt = (Button)  v.findViewById(R.id.disChMbutton);
        disChMbt.setOnClickListener(this);
        disChMbt.setOnLongClickListener(this);
        disChMbt.setOnTouchListener(this);

        Button disChPbt = (Button)  v.findViewById(R.id.disChPbutton);
        disChPbt.setOnClickListener(this);
        disChPbt.setOnLongClickListener(this);
        disChPbt.setOnTouchListener(this);
        disChText=(TextView) v.findViewById(R.id.disChtextView);

        Button meaChPbt = (Button)  v.findViewById(R.id.measureChPbutton);
        meaChPbt.setOnClickListener(this);
        meaChPbt.setOnLongClickListener(this);
        meaChPbt.setOnTouchListener(this);

        Button meaChMbt = (Button)  v.findViewById(R.id.measureChMbutton);
        meaChMbt.setOnClickListener(this);
        meaChMbt.setOnLongClickListener(this);
        meaChMbt.setOnTouchListener(this);
        measureChText=(TextView) v.findViewById(R.id.measureChtextView);


        Button phaseMbt = (Button)  v.findViewById(R.id.phaseMbutton);
        phaseMbt.setOnClickListener(this);
        phaseMbt.setOnLongClickListener(this);
        phaseMbt.setOnTouchListener(this);

        Button phasePbt = (Button)  v.findViewById(R.id.phasePbutton);
        phasePbt.setOnClickListener(this);
        phasePbt.setOnLongClickListener(this);
        phasePbt.setOnTouchListener(this);
        phaseText=(TextView) v.findViewById(R.id.phaseTextview);


        Button phasebt = (Button)  v.findViewById(R.id.phaseEnterButton);
        phasebt.setOnClickListener(this);


        Button thresholdMbt = (Button)  v.findViewById(R.id.thresholdMbutton);
        thresholdMbt.setOnClickListener(this);
        thresholdMbt.setOnLongClickListener(this);
        thresholdMbt.setOnTouchListener(this);

        Button thresholdPbt = (Button)  v.findViewById(R.id.thresholdPbutton);
        thresholdPbt.setOnClickListener(this);
        thresholdPbt.setOnLongClickListener(this);
        thresholdPbt.setOnTouchListener(this);
        thresholdText=(TextView) v.findViewById(R.id.thresholdTextview);


        Button thresholdbt = (Button)  v.findViewById(R.id.thresholdEnterButton);
        thresholdbt.setOnClickListener(this);

        Button blankingMbt = (Button)  v.findViewById(R.id.blankingMbutton);
        blankingMbt.setOnClickListener(this);
        blankingMbt.setOnLongClickListener(this);
        blankingMbt.setOnTouchListener(this);

        Button blankingPbt = (Button)  v.findViewById(R.id.blankingPbutton);
        blankingPbt.setOnClickListener(this);
        blankingPbt.setOnLongClickListener(this);
        blankingPbt.setOnTouchListener(this);
        blankingText=(TextView) v.findViewById(R.id.blankingTextview);


        Button blankingbt = (Button)  v.findViewById(R.id.blankingEnterButton);
        blankingbt.setOnClickListener(this);


;


        if( agcOnFlag == true) {
            agcBt.setText("LOC OFF");

        } else {
            agcBt.setText("LOC ON");
        }

/*データ初期化*/

        SharedPreferences sp = mainActivty.getSharedPreferences("measure", mainActivty.MODE_PRIVATE);
        dfrq[0] = sp.getLong("dfrq", 512000);
        hpf[0] =sp.getInt("hpf", 8);
        lpf[0] =sp.getInt("lpf", 8);
        gain[0] =sp.getInt("gain", 8);
        cmpPower =sp.getInt("cmpPower", 64);
        cmpPhase1 =sp.getInt("cmpPhase1", 64);
        cmpPhase2 =sp.getInt("cmpPhase2", 64);
        sp = mainActivty.getSharedPreferences("agcSettingSel", mainActivty.MODE_PRIVATE);
        AgcCalNum =sp.getString("AgcCalNum", AgcSettingSelectFragment.calNumStr[0]);
        AgcSampleType =sp.getString( AgcCalNum + "TYPE", AgcSettingSelectFragment.sampleTypeStr[0]);
        Log.e(TAG, "AgcCalNum = " + AgcCalNum);
        Log.e(TAG, "AgcSampleType = " + AgcSampleType);
        //trueにすると最終的なlayoutに再度、同じView groupが表示されてしまうのでfalseでOKらしい


        dfrqText=(TextView) v.findViewById(R.id.dfrqText);
        dfrqEdit=(EditText) v.findViewById(R.id.dfrqEditText);

        dfrqDisplay();
    /*
        String frqstr=String.format("%09d", dfrq);
        try {
            SndDfrq = frqstr.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
*/
        gainText=(TextView) v.findViewById(R.id.gainText);
        gainEdit=(EditText) v.findViewById(R.id.gainEditText);
        String   gainStr=String.format("%04d", gain[settingCh]);//文字に変換
        gainText.setText(gainStr);//表示

        thresholdEdit=(EditText) v.findViewById(R.id.thresholdEditText);
        blankingEdit=(EditText) v.findViewById(R.id.blankingEditText);

        hpfText=(TextView) v.findViewById(R.id.hpfText);
        lpfText=(TextView) v.findViewById(R.id.lpfText);
        filterSet();//中で送信データもセットしている。
/*
        long lcmpPower = cmpPower;
        String scmpPower = String.format("%04X", lcmpPower);
        try {
            SndPower = scmpPower.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
*/
        compPhaseSet();

        SV = (SurfaceView) v.findViewById(R.id.surfaceView);
        mSurfaceView = new Oscillo(getActivity(), SV);
        mSurfaceView.setCmpPW(cmpPower);
        mSurfaceView.setCmpPH1(cmpPhase1);
        mSurfaceView.setCmpPH2(cmpPhase2);
        nSpinner = (Spinner)v.findViewById(R.id.dfreqSpinner);
        // ラジオグループのオブジェクトを取得
        rg = (RadioGroup)v.findViewById(R.id.movemode);
        // ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, dfrqUnit);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // spinner に adapter をセット

        nSpinner.setAdapter(adapter);
/*

        aveNumSpinner = (Spinner)v.findViewById(R.id.agcAveSpinner);
        ArrayAdapter<String> aveadapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, pw2AveTime);
        aveadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // spinner に adapter をセット
        aveNumSpinner.setAdapter(aveadapter);
*/
        mainActivty.funcflag= EMIC_Activity.FuncEnable.NONE;
/*
        timerOn=true;


       timerHandler.post(timerRunnable1);
        */
        return v;

    }
    public   void putIdata(short[] data) {
//        Log.d(TAG, "putDataI");

       mSurfaceView.putIdata(data);

    }
    public   void putQdata(short[] data) {
//        Log.d(TAG, "putDataQ");

        mSurfaceView.putQdata(data);

    }


        private boolean agcOnFlag=false;
        private boolean mainLoop = true;
        private boolean ackRecive;
        private int calSndNum;
        private int sendCh=0;
        private Runnable timerRunnable1 = new Runnable() {
        int i;
        private byte[] SndDfrq = new byte[9];
        private byte[] SndGain = new byte[4];
        private byte[] SndHpf = new byte[2];
        private byte[] SndLpf = new byte[2];
        private byte[] SndDisplayCh = new byte[1];
        private byte[] SndMeasureCh = new byte[1];
        private byte[] SndSettingCh = new byte[1];
        private byte[] SndPower = new byte[4];
        private byte[] SndPhaseStart = new byte[4];
        private byte[] SndPhaseEnd = new byte[4];
        private byte[] SndStatus = new byte[4];
        private byte[] SndLocTh = new byte[4];
        private byte[] SndLocBk = new byte[4];
        private byte[] SndAph = new byte[4];

        @Override
        public void run() {
      /* do what you need to do */


            while (timerOn==true) {
           // Log.d(TAG, "STATE" + SndStatus[0]+ SndStatus[1]+ SndStatus[2]+ SndStatus[3]);

      // Log.d(TAG, "timer1");
      //     Log.d(TAG, "dfrq"+dfrq);

            byte[] sum =new byte[1];
            if(mainLoop == true){
                    if (mainActivty.funcflag != EMIC_Activity.FuncEnable.AGS) {

                        if(sendCh==3) {
                            sendCh = 0;
                        }else {
                            sendCh++;
                        }


                        String diplayChStr = "0";
                        String measureChStr = "0";
                        String sendChStr = "0";
                        diplayChStr = String.format("%01X",diplayCh);//文字に変換
                        measureChStr = String.format("%01X",measureCh);//文字に変換
                        sendChStr = String.format("%01X",sendCh);//文字に変換
                        try {
                            SndDisplayCh = diplayChStr.getBytes("UTF-8");
                            SndMeasureCh = measureChStr.getBytes("UTF-8");
                            SndSettingCh = sendChStr.getBytes("UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        mainActivty.write(SETChCmd);            // 送信実行
                        mainActivty.write(SndSettingCh);            // 送信実行
                        sum[0] = (byte) (~SndSettingCh[0]);
                        mainActivty.write(sum);            // 送信実行
                        mainActivty.write(Cr);


                        mainActivty.write(DisChCmd);            // 送信実行
                        mainActivty.write(SndDisplayCh);            // 送信実行
                        sum[0] = (byte) (~SndDisplayCh[0]);
                        mainActivty.write(sum);            // 送信実行
                        mainActivty.write(Cr);            // 送信実行

                        mainActivty.write(MeaChCmd);            // 送信実行
                        mainActivty.write(SndMeasureCh);            // 送信実行
                        sum[0] = (byte) (~SndMeasureCh[0]);
                        mainActivty.write(sum);            // 送信実行
                        mainActivty.write(Cr);            // 送信実行


                        try {
                            Thread.sleep(10); //3000ミリ秒Sleepする
                        } catch (InterruptedException e) {
                        }


                        String frqstr = String.format("%09d", dfrq[sendCh]);

                        try {
                            SndDfrq = frqstr.getBytes("UTF-8");
                        } catch (UnsupportedEncodingException e) {

                        }
                        mainActivty.write(FRQCmd);            // 送信実行
                        mainActivty.write(SndDfrq);            // 送信実行

                        sum[0] = (byte) (~SndDfrq[0] + SndDfrq[1] + ~SndDfrq[2] + SndDfrq[3] + ~SndDfrq[4] + SndDfrq[5] + ~SndDfrq[6] + SndDfrq[7] + ~SndDfrq[8]);

                        mainActivty.write(sum);            // 送信実行
                        mainActivty.write(Cr);            // 送信実行
                        try {
                            Thread.sleep(10); //10ミリ秒Sleepする
                        } catch (InterruptedException e) {
                        }


          // 送信実行


                        String gainStr = "0";
                        gainStr = String.format("%04X", gain[sendCh] * 10);//文字に変換
                        try {
                            SndGain = gainStr.getBytes("UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        mainActivty.write(GainCmd);            // 送信実行
                        mainActivty.write(SndGain);            // 送信実行
                        sum[0] = (byte) (~SndGain[0] + SndGain[1] + ~SndGain[2] + SndGain[3]);
                        mainActivty.write(sum);            // 送信実行
                        mainActivty.write(Cr);            // 送信実行
                        try {
                            Thread.sleep(10); //10ミリ秒Sleepする
                        } catch (InterruptedException e) {
                        }

                        String lpfStr = String.format("%02X", lpfIndex[sendCh]);
                        String hpfStr = String.format("%02X", HpsNum[hpf[sendCh]]);
                        try {

                            SndHpf = hpfStr.getBytes("UTF-8");
                            SndLpf = lpfStr.getBytes("UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        mainActivty.write(HpfCmd);            // 送信実行
                        //              Log.d(TAG, "hpf" + SndHpf[0] + SndHpf[1]);
                        mainActivty.write(SndHpf);            // 送信実行
                        sum[0] = (byte) (~SndHpf[0] + SndHpf[1]);
                        mainActivty.write(sum);            // 送信実行
                        mainActivty.write(Cr);            // 送信実行
                        try {
                            Thread.sleep(10); //10ミリ秒Sleepする
                        } catch (InterruptedException e) {
                        }



                        String locBlankingtimeStr = "0";
                        String locThresholdStr = "0";
                        String aphStr = "0";
                        locBlankingtimeStr = String.format("%04X",locBlankingtime);//文字に変換
                        locThresholdStr = String.format("%04X", locThreshold);//文字に変換
                        aphStr = String.format("%04X", phase[settingCh]);//文字に変換



                        try {
                            SndLocBk = locBlankingtimeStr.getBytes("UTF-8");
                            SndLocTh = locThresholdStr.getBytes("UTF-8");
                            SndAph = aphStr.getBytes("UTF-8");
                         } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        mainActivty.write(LbkCmd);            // 送信実行
                        mainActivty.write(SndLocBk);            // 送信実行
                        sum[0] = (byte) (~SndLocBk[0] + SndLocBk[1] + ~SndLocBk[2] + SndLocBk[3]);
                        mainActivty.write(sum);
                        mainActivty.write(Cr);            // 送信実行

                        mainActivty.write(LthCmd);            // 送信実行
                        mainActivty.write(SndLocTh);            // 送信実行
                        sum[0] = (byte) (~SndLocTh[0] + SndLocTh[1] + ~SndLocTh[2] + SndLocTh[3]);
                        mainActivty.write(sum);
                        mainActivty.write(Cr);            // 送信実行

                        mainActivty.write(AphCmd);            // 送信実行
                        mainActivty.write(SndAph);            // 送信実行
                        sum[0] = (byte) (~SndAph[0] + SndAph[1] + ~SndAph[2] + SndAph[3]);
                        mainActivty.write(sum);
                        mainActivty.write(Cr);            // 送信実行

                        mainActivty.write(LpfCmd);            // 送信実行
                        mainActivty.write(SndLpf);            // 送信実行
                        sum[0] = (byte) (~SndLpf[0] + SndLpf[1]);
                        mainActivty.write(sum);
                        mainActivty.write(Cr);            // 送信実行
                        //   AgsCmd[4] = 0x30;
                        try {
                            Thread.sleep(10); //10ミリ秒Sleepする
                        } catch (InterruptedException e) {
                        }

                        String phStartStr = String.format("%04X", cmpPHstart);

                        try {

                            SndPhaseStart = phStartStr.getBytes("UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        mainActivty.write(SphsCmd);            // 送信実行
                        mainActivty.write(SndPhaseStart);            // 送信実行
                        sum[0] = (byte) (~SndPhaseStart[0] + SndPhaseStart[1] + ~SndPhaseStart[2] + SndPhaseStart[3]);
                        mainActivty.write(sum);            // 送信実行
                        mainActivty.write(Cr);            // 送信実行
                        try {
                            Thread.sleep(10); //3000ミリ秒Sleepする
                        } catch (InterruptedException e) {
                        }


                        String phEndStr = String.format("%04X", cmpPHend);

                        try {
                            SndPhaseEnd = phEndStr.getBytes("UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        mainActivty.write(SpheCmd);            // 送信実行
                        mainActivty.write(SndPhaseEnd);            // 送信実行
                        sum[0] = (byte) (~SndPhaseEnd[0] + SndPhaseEnd[1] + ~SndPhaseEnd[2] + SndPhaseEnd[3]);
                        mainActivty.write(sum);            // 送信実行
                        mainActivty.write(Cr);            // 送信実行
                        try {
                            Thread.sleep(10); //10ミリ秒Sleepする
                        } catch (InterruptedException e) {
                        }
                        mainActivty.write(SpwCmd);            // 送信実行
                        long lcmpPower = cmpPower;
                        String scmpPower = String.format("%04X", lcmpPower);
                        try {

                            SndPower = scmpPower.getBytes("UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        mainActivty.write(SndPower);            // 送信実行
                        sum[0] = (byte) (~SndPower[0] + SndPower[1] + ~SndPower[2] + SndPower[3]);
                        mainActivty.write(sum);
                        mainActivty.write(Cr);            // 送信実行
                        try {
                            Thread.sleep(10); //3000ミリ秒Sleepする
                        } catch (InterruptedException e) {
                        }

/*
                    try {

                        SndStatus =mainActivty.getFpgaStatus().getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    mainActivty.write(StatusCmd);            // 送信実行
                    mainActivty.write(SndStatus);            // 送信実行
                    sum[0] = (byte) (~SndStatus[0] + SndStatus[1] + ~SndStatus[2] + SndStatus[3]);
                    mainActivty.write(sum);
                    mainActivty.write(Cr);            // 送信実行
*/
                        try {
                            Thread.sleep(10); //3000ミリ秒Sleepする
                        } catch (InterruptedException e) {
                        }

                        mainActivty.write(CICRCmd);            // 送信実行
                        mainActivty.write(CicNum[cic]);            // 送信実行
                        sum[0] = (byte) (~CicNum[cic][0] + CicNum[cic][1]);
                        mainActivty.write(sum);
                        mainActivty.write(Cr);            // 送信実行


                    }
                    try {
                        Thread.sleep(10); //10ミリ秒Sleepする
                    } catch (InterruptedException e) {
                    }

                    switch (mainActivty.funcflag) {
                        case NONE:
                            FncCmd[4] = 0x30;//0
                            break;
                        case MEASURE:
                            FncCmd[4] = 0x31;//1
                            break;
                        case AGS:
                            FncCmd[4] = 0x32;//2
                            break;
                    }

                    if (agcOnFlag == true) {
                        FncCmd[4] = (byte) (FncCmd[4] + 4);

                    }
                    mainActivty.write(FncCmd);            //
                    sum[0] = (byte) (~FncCmd[4]);
                    mainActivty.write(sum);
                    mainActivty.write(Cr);


                    try {
                        Thread.sleep(10); //3000ミリ秒Sleepする
                    } catch (InterruptedException e) {
                    }

/*
                    mainActivty.write(Pw2AveNumCmd);
                    String Pw2AveNumStr = String.format("%02X", aveNumSpinner.getSelectedItemPosition());
                    byte[] SndPw2AveNum = new byte[2];
                    try {
                        SndPw2AveNum = Pw2AveNumStr.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    mainActivty.write(SndPw2AveNum);

                    sum[0] = (byte) (~SndPw2AveNum[0] + SndPw2AveNum[1]);
                    mainActivty.write(sum);
                    mainActivty.write(Cr);
*/

                    try {
                        Thread.sleep(10); //10ミリ秒Sleepする
                    } catch (InterruptedException e) {
                    }
            //mainloop　end
            }else{


                mainActivty.write(LOFcmd);
                String calNumStr = String.format("%01X", calSndNum);
                byte[] Sndcalnum = new byte[1];
                try {
                    Sndcalnum = calNumStr.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if(calSndNum < 5) {
                    String calPw1str = String.format("%06X", calPw1Data[calSndNum]);
                    byte[] SndcalPw1 = new byte[6];
                    try {
                        SndcalPw1 = calPw1str.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    mainActivty.write(SndcalPw1);
                    mainActivty.write(Sndcalnum);
                    sum[0] = (byte) (~SndcalPw1[0] + SndcalPw1[1] + ~SndcalPw1[2] + SndcalPw1[3] + ~SndcalPw1[4] + SndcalPw1[5] + ~Sndcalnum[0]);
                    mainActivty.write(sum);
                    mainActivty.write(Cr);


                }else if(calSndNum < 10 ){

                    String calPw2str = String.format("%06X", calPw2Data[calSndNum-5]);
                    byte[] SndcalPw2 = new byte[6];
                    try {
                        SndcalPw2 = calPw2str.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    mainActivty.write(SndcalPw2);
                    mainActivty.write(Sndcalnum);
                    sum[0] = (byte) (~SndcalPw2[0] + SndcalPw2[1] + ~SndcalPw2[2]+ SndcalPw2[3] + ~SndcalPw2[4] + SndcalPw2[5]  + ~Sndcalnum[0]);
                    mainActivty.write(sum);
                    mainActivty.write(Cr);


                }else{
                    mainLoop = true;
                    progressDialog.cancel();
                }
                try {
                    Thread.sleep(50); //50ミリ秒Sleepする
                } catch (InterruptedException e) {
                }
                if(ackRecive == true){
                    ackRecive=false;
                    calSndNum++;
                }

            }



            }
      /* and here comes the "trick" */

           // timerHandler.postDelayed(this, 1000);
      //      timerHandler.postDelayed(timerRunnable3,250);


        }


    };


    private Runnable timerRunnable2 = new Runnable() {

        @Override
        public void run() {
      /* do what you need to do */





            // timerHandler.postDelayed(this, 1000);
         //   timerHandler.postDelayed(timerRunnable3, 250);
        }


    };

    private Runnable timerRunnable3 = new Runnable() {

        @Override
        public void run() {
            Log.d(TAG, "timer3");

            if (agsOn == false) {


            }

                if (timerOn == true){
                // timerHandler.postDelayed(this, 1000);
              //  timerHandler.postDelayed(timerRunnable1, 250);
        }
        }


    };


    /**
     * ボタンクリック時動作
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {



            case R.id.measureButton:
                // 次のデータ送信要求
                if(mainActivty.funcflag==EMIC_Activity.FuncEnable.MEASURE) {

                    mainActivty.funcflag = EMIC_Activity.FuncEnable.NONE;
                    measureBt.setText("検査開始");
                }else{

                    mainActivty.funcflag=EMIC_Activity.FuncEnable.MEASURE;
                    measureBt.setText("検査停止");
                }


                break;
    /*
            case R.id.stop:
                if(funcflag==FuncEnable.GO)
                     funcflag=FuncEnable.NONE;
*/
              //  break;
            case R.id.dfrqPbutton:

                //dfrq++;
                //dfrqText.setText(String.valueOf((double) dfrq / 1000));
                dfrqPlus();
                 Log.d(TAG, "plus" + dfrq);
                break;


            case R.id.dfrqMbutton:
             //   dfrq--;
              //  dfrqText.setText(String.valueOf((double ) dfrq/1000));
                dfrqMinus();

                Log.d(TAG, "minus" + dfrq);
                break;

            case R.id.hpPbutton:
                hpf[settingCh]++;
                filterSet();
    //            hpfText.setText(String.valueOf(hpf));

                Log.d(TAG, "minus"+dfrq);
                break;

            case R.id.hpMbutton:
                hpf[settingCh]--;
                filterSet();
    //            hpfText.setText(String.valueOf(hpf));
                Log.d(TAG, "minus"+dfrq);
                break;

            case R.id.lpPbutton:
                lpf[settingCh]++;
                filterSet();
 //               lpfText.setText(String.valueOf(lpf));
                Log.d(TAG, "minus" + dfrq);
                break;

            case R.id.lpMbutton:
                lpf[settingCh]--;
                filterSet();
  //              lpfText.setText(String.valueOf(lpf));
                Log.d(TAG, "minus"+dfrq);
                break;

            case R.id.gainPbutton:
                gain[settingCh]++;
                if(gain[settingCh]>6000)
                    gain[settingCh]=6000;


                gainText.setText(String.valueOf(gain[settingCh]));

                break;

            case R.id.gainMbutton:
                gain[settingCh]--;
                gainText.setText(String.valueOf(gain));

                break;
            case R.id.phasePbutton:
                phase[settingCh]++;
                if(phase[settingCh]>0xffff)
                    phase[settingCh]=0;


                phaseText.setText(String.valueOf(phase[settingCh]));

                break;
            case R.id.phaseMbutton:
                phase[settingCh]--;
                if(phase[settingCh]==0)
                    phase[settingCh]=0xffff;


                phaseText.setText(String.valueOf(phase[settingCh]));

                break;

            case R.id.blankingPbutton:
                locBlankingtime++;
                if(locBlankingtime>9999)
                    locBlankingtime=9999;


                blankingText.setText(String.valueOf(locBlankingtime));

                break;
            case R.id.blankingMbutton:
                locBlankingtime--;
                if(locBlankingtime < 0 )
                    locBlankingtime=0;


                blankingText.setText(String.valueOf(locBlankingtime));

                break;
            case R.id.thresholdPbutton:
                locThreshold++;
                if(locThreshold >9999)
                    locThreshold =9999;


                thresholdText.setText(String.valueOf(locThreshold));
                break;
            case R.id.thresholdMbutton:
                locThreshold--;
                if(locThreshold < 0)
                    locThreshold =0;


                thresholdText.setText(String.valueOf(locThreshold));
                break;

            case R.id.agsButton:

                switch (mainActivty.funcflag){
                   case AGS:

                       mainActivty.funcflag = EMIC_Activity.FuncEnable.NONE;
                   //    agsBt.setText("設定開始");
                        break;
                    case NONE:

                        mainActivty.funcflag = EMIC_Activity.FuncEnable.AGS;



                         progressDialog = new ProgressDialog(getActivity());
                        // プログレスダイアログのタイトルを設定します
   //                     progressDialog.setTitle("タイトル");
                        // プログレスダイアログのメッセージを設定します
                        progressDialog.setMessage("GAINを調整しています。");
                        // プログレスダイアログの確定（false）／不確定（true）を設定します
                        progressDialog.setIndeterminate(false);
                        // プログレスダイアログのスタイルを水平スタイルに設定します
 //                      progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// プログレスダイアログのスタイルを円スタイルに設定します
                       progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // ProgressDialog をキャンセル
                                        Log.d("test", "BUTTON_CANCEL clicked");
                                        mainActivty.funcflag=EMIC_Activity.FuncEnable.NONE;
                    //                    agsBt.setText("設定開始");
                                        dialog.cancel();
                                    }
                                });
                        // プログレスダイアログの最大値を設定します
                        progressDialog.setMax(10);
                        // プログレスダイアログの値を設定します
   //                     progressDialog.incrementProgressBy(0);
                        // プログレスダイアログのセカンダリ値を設定します
    //                    progressDialog.incrementSecondaryProgressBy(70);
                        // プログレスダイアログのキャンセルが可能かどうかを設定します
                        progressDialog.setCancelable(false);
                        // プログレスダイアログを表示します
                        progressDialog.show();
               //         agsBt.setText("キャンセル");
                    break;
                    case MEASURE:
                        if(toast1 != null) {
                            toast1.cancel();
                        }
                        toast1 = Toast.makeText(getActivity(), "検査中は使用できません。", Toast.LENGTH_SHORT);
                        toast1.show();
                        break;
               }

                Log.d(TAG, "agsbutton");
                break;

            case R.id.PwPbutton:

                cmpPower++;
                if(cmpPower > 254)
                    cmpPower=254;
                mSurfaceView.setCmpPW(cmpPower);
                break;

            case R.id.PwMbutton:
                cmpPower--;
                if(cmpPower <= 128 )
                    cmpPower=128;

                mSurfaceView.setCmpPW(cmpPower);

                break;
            case R.id.PH1Pbutton:
                cmpPhase1++;
                if(cmpPhase1 > 90)
                    cmpPower=90;
                mSurfaceView.setCmpPH1(cmpPhase1);
                break;
            case R.id.PH1Mbutton:
                cmpPhase1--;
                if(cmpPhase1 <-90)
                    cmpPhase1=-90;
                mSurfaceView.setCmpPH1(cmpPhase1);
                break;

            case R.id.PH2Pbutton:
                cmpPhase2++;
                if(cmpPhase2 > 180)
                    cmpPhase2=180;
                mSurfaceView.setCmpPH2(cmpPhase2);
                break;
            case R.id.PH2Mbutton:
                cmpPhase2--;
                if(cmpPhase2 <0)
                    cmpPhase2=0;
                mSurfaceView.setCmpPH2(cmpPhase2);
                break;

            case R.id.setChPbutton:

                if(settingCh == 3)
                    settingCh=0;
                else
                    settingCh++;

              setChChange();

                break;



            case R.id.setChMbutton:

                if(settingCh == 0)
                    settingCh=3;
                else
                    settingCh--;
                setChChange();

                break;

            case R.id.disChMbutton:

                if(diplayCh == 1)
                    diplayCh=4;
                else
                    diplayCh--;

                if(diplayCh==4) {
                    disChText.setText("S");
                }else {
                    disChText.setText(String.valueOf(diplayCh));
                }
                break;
            case R.id.disChPbutton:

                if(diplayCh == 5)
                    diplayCh=1;
                else
                    diplayCh++;

                if(diplayCh==5) {
                    disChText.setText("S");
                }else {
                    disChText.setText(String.valueOf(diplayCh));
                }

                break;


            case R.id.measureChPbutton:

                if(measureCh == 4)
                    measureCh=1;
                else
                    measureCh++;

                measureChText.setText(String.valueOf(measureCh));
               break;
            case R.id.measureChMbutton:

                if(measureCh == 1)
                    measureCh=4;
                else
                    measureCh--;
                measureChText.setText(String.valueOf(measureCh));
               break;

            case R.id.freqSweepButton:
                mainActivty.displayFreqSweep();
                break;
            case R.id.agcSelButton:
                mainActivty.displayAgcSel();
                break;
            case R.id.agcButton:
                if(agcOnFlag==false){
                       agcBt.setText("LOC OFF");
                       agcOnFlag = true;
                       locStart();

                }else {
                      agcBt.setText("LOC ON");
                    agcOnFlag=false;
                }
                break;

            case R.id.dfrqEnterButton:
                editDfrqSet();
                break;
            case R.id.gainEnterButton:
                String gainstr = gainEdit.getText().toString();

                try {
                    gain[settingCh] = Integer.parseInt(gainstr);
                }catch (NumberFormatException e){

                }

                gainText.setText(String.valueOf(gain));
                break;
            case R.id.blankingEnterButton:
                String blankingstr = blankingEdit.getText().toString();

                try {
                  locBlankingtime= Integer.parseInt(blankingstr);
                }catch (NumberFormatException e){
                    locBlankingtime=0;
                }
                    if(locBlankingtime>0xffff)
                        locBlankingtime=0xfffff;

                blankingText.setText(String.valueOf(locBlankingtime));
                break;
            case R.id.thresholdEnterButton:
                String thresholdstr = thresholdEdit.getText().toString();

                try {
                    locThreshold= Integer.parseInt(thresholdstr);
                }catch (NumberFormatException e){
                    locThreshold=0;
                }
                if(locThreshold>0xffff)
                    locThreshold=0xfffff;

                thresholdText.setText(String.valueOf(locThreshold));
                break;

            case R.id.autoFilterButton:



                // チェックされているラジオボタンの ID を取得
                int id = rg.getCheckedRadioButtonId();
                // チェックされているラジオボタンオブジェクトを取得
                switch (id) {
                    case R.id.slideSel:
                 //       Toast.makeText(getActivity(), "スライド動作では設定できません。", Toast.LENGTH_SHORT).show();
                        hpf[settingCh]=9;
                        lpf[settingCh]=LPFMAX;
                        filterSet();
                        if(toast1 != null) {
                            toast1.cancel();
                        }

                        break;
                    case R.id.rollSel:
                        //テキスト入力を受け付けるビューを作成します。
                        final EditText editView = new EditText(mainActivty);
                        editView.setInputType( InputType.TYPE_CLASS_NUMBER);
                        editView.setWidth(10);
                        new AlertDialog.Builder(mainActivty)
                                .setTitle("回転数を入力してください。(rpm)")
                                        //setViewにてビューを設定します。
                                .setView(editView)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        filterSertch(editView.getText().toString());
                                        if(toast1 != null) {
                                            toast1.cancel();
                                        }

                                    }
                                })
                                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                })
                                .show();
                        if(toast1 != null) {
                            toast1.cancel();
                        }
                        break;
                }


            break;
        }

    }


    private void setChChange() {

        setChText.setText(String.valueOf(settingCh+1));
        gainText.setText(String.valueOf(gain[settingCh]));
        hpfText.setText(FilterStr[hpf[settingCh]] + "Hz");
        lpfText.setText(FilterStr[lpf[settingCh]]+"Hz");

    }

    /**
     *
     * @param rpmStr
     */
    private  void filterSertch(String rpmStr ) {

        int rpm;
        try {
            rpm = Integer.parseInt(rpmStr);
        }catch (NumberFormatException e){
            return;
        }
        double freq = (double)rpm/(60)*2;
        int filternum=0;
        for(filternum=0;filternum<=HPFMAX;filternum++){
            if(FilterDobule[filternum] > freq) {
                break;
            }
        }
        hpf[settingCh]=filternum;
        lpf[settingCh]=LPFMAX;
        filterSet();
    }


    /**
     * 長押し処理
     * @param v
     * @return
     */
    @Override
    public boolean onLongClick(View v) {
        isRepeat = true;
        switch (v.getId()) {
            case R.id.lpMbutton:
                longbtflag= LongPush.LPFMINUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();
                break;
            case R.id.lpPbutton:
                longbtflag= LongPush.LPFPLUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();
                break;
            case R.id.hpMbutton:
                longbtflag= LongPush.HPFMINUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();
                break;
            case R.id.hpPbutton:
                longbtflag= LongPush.HPFPLUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();

                filterSet();

                break;
            case R.id.dfrqPbutton:

                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス

                longbtflag= LongPush.FRQPLUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();
                Log.d(TAG, "plus"+dfrq);
                break;
            case R.id.dfrqMbutton:
                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス
                longbtflag= LongPush.FRQMINUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();
                Log.d(TAG, "minus"+dfrq);
                break;


            case R.id.gainPbutton:

                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス
                longbtflag= LongPush.GAINPLUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();

                break;

            case R.id.gainMbutton:

                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス
                longbtflag= LongPush.GAINMINUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();

                break;
            case R.id.PwMbutton:
                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス
                longbtflag= LongPush.CMPPWMINUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();

                break;

            case R.id.PwPbutton:
                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス
                longbtflag= LongPush.CMPPWPLUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();

                break;


            case R.id.PH1Mbutton:
                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス
                longbtflag= LongPush.CMPPH1MINUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();
                break;
            case R.id.PH1Pbutton:


                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス
                longbtflag= LongPush.CMPPH1PLUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();

                break;
            case R.id.PH2Mbutton:
                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス
                longbtflag= LongPush.CMPPH2MINUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();
                break;
            case R.id.PH2Pbutton:
                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス
                longbtflag= LongPush.CMPPH2PLUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();

                break;

            case R.id.phasePbutton:

                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス
                longbtflag= LongPush.PHASEPLUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();

                break;
            case R.id.phaseMbutton:
                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス
                longbtflag= LongPush.PHASEMINUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();

                break;

            case R.id.blankingPbutton:
                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス
                longbtflag= LongPush.BLANKINGPLUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();
                break;
            case R.id.blankingMbutton:
                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス
                longbtflag= LongPush.BLANKINGMINUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();

                break;
            case R.id.thresholdPbutton:
                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス
                longbtflag= LongPush.THRESHOLDPLUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();

            case R.id.thresholdMbutton:
                //連続イベントフラグをtrue
                isRepeat = true;
                //連続マイナス
                longbtflag= LongPush.THRESHOLDMINUS;
                cntThread = new Thread(repeatBtPush);
                //連続イベント開始
                cntThread.start();
                break;
        }
        btRepiatNUM=1;
        btRpeatCnt=0;
        return false;
    }



    //onTouchのイベント
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //onTouchは、画面を押した時と、離した時の両方のイベントを取得する
        int action = event.getAction();
        switch (action) {
            //ボタンから指が離れた時
            case MotionEvent.ACTION_UP:
                //連続イベントフラグをfalse
                longbtflag= LongPush.NONE;
                isRepeat = false;


                switch (v.getId()) {

                    case R.id.dfrqPbutton:
                    case R.id.dfrqMbutton:
/*
                        String frqstr=String.format("%09d",dfrq);

                        try {
                            SndDfrq = frqstr.getBytes("UTF-8");
                        } catch (UnsupportedEncodingException e) {

                        }
*/
                        break;
                    case R.id.PH1Mbutton:
                    case R.id.PH1Pbutton:
                    case R.id.PH2Mbutton:
                    case R.id.PH2Pbutton:


                        compPhaseSet();

                        break;
                    case R.id.PwPbutton:
                    case R.id.PwMbutton:

                        break;
                }


                break;
        }
        return false;
    }
  /*
        //連続プラス
    private Runnable repeatDfrqPlus = new Runnable(){
        @Override
        public void run(){
            while(isRepeat){
                try{
                    Thread.sleep(100); //0.1秒イベント中断。値が小さいほど、高速で連続する
                }catch(InterruptedException e){
                }
                handler.post(new Runnable(){
                    @Override
                    public void run(){
                        //カウントプラスを呼び出し
                        dfrqPlus();
                    }
                });
            }
        }
    };
*/
    //連続プラス
    private Runnable repeatBtPush = new Runnable(){
        @Override
        public void run(){;
            while(isRepeat){
                try{
                    Thread.sleep(100); //0.1秒イベント中断。値が小さいほど、高速で連続する
                }catch(InterruptedException e){
                }
                handler.post(new Runnable(){
                    @Override
                    public void run(){
                        //カウントプラスを呼び出し
                        switch (longbtflag){
                            case LPFPLUS:
                                lpfPlus();;
                                break;
                            case LPFMINUS:
                                 lpfMinus();

                                break;
                            case HPFPLUS:
                                hpfPlus();
                                break;
                            case HPFMINUS:
                                hpfMinus();
                                break;

                            case FRQPLUS:
                                if(btRpeatCnt>100){
                                    btRepiatNUM=1000;
                                }else if(btRpeatCnt>50) {
                                    btRepiatNUM=100;
                                }

                                dfrqPlus();
                                break;
                            case FRQMINUS:
                                if(btRpeatCnt>100){
                                    btRepiatNUM=1000;
                                }else if(btRpeatCnt>50) {
                                    btRepiatNUM = 100;
                                }
                                dfrqMinus();
                                break;
                            case GAINPLUS:;
                                gainPlus();
                                break;
                            case  GAINMINUS:;
                                gainMinus();
                                break;
                            case CMPPWPLUS:
                                cmpPwPlus();
                                break;
                            case CMPPWMINUS:
                                cmpPwMinus();
                                break;
                            case CMPPH1PLUS:;
                                cmpPh1Plus();
                                break;
                            case  CMPPH1MINUS:;
                                cmpPh1Minus();
                                break;
                            case CMPPH2PLUS:;
                                cmpPh2Plus();
                                break;
                            case  CMPPH2MINUS:;
                                cmpPh2Minus();
                                break;
                            case PHASEPLUS:;
                                phasePlus();;
                                break;
                            case PHASEMINUS:;
                                phaseMinus();
                                break;
                            case BLANKINGPLUS:;
                                blankingPlus();
                                break;
                            case BLANKINGMINUS:;
                                blankingMinus();;
                                break;
                            case THRESHOLDPLUS:
                                locThersholdPlus();;
                                break;
                            case THRESHOLDMINUS:;
                                locThersholdMinus();
                                break;
                            default:

                        }

                    }
                });
                if(btRpeatCnt < 1000)
                btRpeatCnt++;
            }
        }
    };


    private void dfrqPlus(){


            if (dfrq[settingCh] < 10000) {
                dfrq[settingCh] = dfrq [settingCh]+ 1;
            } else if (dfrq[settingCh] < 100000) {
                dfrq[settingCh] = dfrq[settingCh] + 10;
            } else if (dfrq[settingCh] < 1000000) {
                dfrq[settingCh] = dfrq[settingCh] + 100;
            } else if (dfrq[settingCh] < 10000000) {
                dfrq[settingCh] = dfrq[settingCh] + 1000;
            } else {
                dfrq[settingCh] = 10000000;
            }

        dfrqDisplay();
        filterSet();
    };


    private void dfrqMinus(){

            if (dfrq[settingCh] <= 1000) {
                dfrq[settingCh] = 1000;
            } else if (dfrq[settingCh] <= 10000) {
                dfrq[settingCh] = dfrq[settingCh] - 1;
            } else if (dfrq[settingCh] <= 100000) {
                dfrq[settingCh] = dfrq[settingCh] - 10;
            } else if (dfrq[settingCh] <= 1000000) {
                dfrq[settingCh] = dfrq[settingCh] - 100;
            } else if (dfrq[settingCh] <= 10000000) {
                dfrq[settingCh] = dfrq[settingCh] - 1000;
            } else {
                dfrq[settingCh] = 10000000;
            }

            dfrqDisplay();
            filterSet();
    }

    private void dfrqDisplay(){

        if(dfrq[settingCh] < 1000){
            dfrqText.setText(String.valueOf( dfrq )+"Hz");
        }else if(dfrq[settingCh]<1000000) {
            dfrqText.setText(String.valueOf( (double)dfrq[settingCh] / 1000) +"kHz");
        }else{
            dfrqText.setText(String.valueOf( (double)dfrq[settingCh] / 1000000 )+"MHz");
        }

    }
    private void lpfPlus() {
        lpf[settingCh]=lpf[settingCh]+2;
        filterSet();
    }
    private void lpfMinus() {
        lpf[settingCh]=lpf[settingCh]-2;
        filterSet();
    }
    private void hpfPlus() {
        hpf[settingCh]=hpf[settingCh]+2;
        filterSet();
    }
    private void hpfMinus() {
        hpf[settingCh]=hpf[settingCh]- 2;
        filterSet();
    }

 /*
        //カウントをプラスするメソッド
    private void dfrqPlus() {
        dfrq=dfrq+10;
        dfrqText.setText(String.format("%04d",( dfrq / 1000) ) );
    }
    //カウントをマイナスするメソッド
    private void dfrqMinus() {
        dfrq=dfrq-10;
        if(dfrq< 0 )
            dfrq=0;
        dfrqText.setText(String.format("%04d", (dfrq / 1000)));
    }
    */
    //カウントをマイナスするメソッド
    private void gainPlus() {
        gain[settingCh]=gain[settingCh]+10;
        if(gain[settingCh]>6000)
            gain[settingCh]=6000;
        gainText.setText(String.format("%04d", gain[settingCh]));
    }
    //カウントをマイナスするメソッド
    private void gainMinus() {
        gain[settingCh]=gain[settingCh]-10;
        if(gain[settingCh]< 0 )
            gain[settingCh]=0;
        gainText.setText(String.format("%04d", gain[settingCh]) );
    }

    private  void cmpPwPlus(){
        cmpPower = cmpPower + 5 ;
        if(cmpPower > 254)
            cmpPower=254;
        mSurfaceView.setCmpPW(cmpPower);
    }
    private  void cmpPwMinus(){
        cmpPower = cmpPower - 5 ;
        if(cmpPower < 128)
            cmpPower=128;
        mSurfaceView.setCmpPW(cmpPower);
    }

    private  void cmpPh1Plus(){
        cmpPhase1 = cmpPhase1 + 5;
        if(cmpPhase1 > 90)
            cmpPhase1=90;
        mSurfaceView.setCmpPH1(cmpPhase1);
    }
    private  void cmpPh1Minus(){
        cmpPhase1 = cmpPhase1 - 5;
        if(cmpPhase1 < -90)
            cmpPhase1=-90;
        mSurfaceView.setCmpPH1(cmpPhase1);
    }
    private  void cmpPh2Plus(){
        cmpPhase2=cmpPhase2 + 5;
        if(cmpPhase2 > 180)
            cmpPhase2=180;
        mSurfaceView.setCmpPH2(cmpPhase2);
    }
    private  void cmpPh2Minus(){
        cmpPhase2=cmpPhase2 - 5;
        if(cmpPhase2 < 0)
            cmpPhase2=0;
        mSurfaceView.setCmpPH2(cmpPhase2);
    }
    private void phaseMinus() {
        phase[settingCh]=phase[settingCh]-10;
        if(phase[settingCh] <= 0 )
            phase[settingCh]=0xffff;
        phaseText.setText(String.format("%05d", phase[settingCh]) );
    }
    private void phasePlus() {
        phase[settingCh]=phase[settingCh]+10;
        if(phase[settingCh] > 0xffff )
            phase[settingCh]=0;
        phaseText.setText(String.format("%05d", phase[settingCh]) );
    }
    private void blankingMinus() {
        locBlankingtime = locBlankingtime-10;
        if(locBlankingtime <= 0 )
            locBlankingtime=0xffff;
        blankingText.setText(String.format("%05d", locBlankingtime ) );
    }
    private void blankingPlus() {
        locBlankingtime = locBlankingtime+10;
        if(locBlankingtime > 0xffff )
            locBlankingtime=0;
        blankingText.setText(String.format("%05d", locBlankingtime ) );
    }

    private void locThersholdMinus() {
        locThreshold = locThreshold -10;
        if(locThreshold <= 0 )


            locThreshold =0xffff;
        thresholdText.setText(String.format("%05d", locThreshold) );
    }
    private void locThersholdPlus() {
        locThreshold = locThreshold +10;
        if(locBlankingtime > 0xffff )
            locThreshold =0;
        thresholdText.setText(String.format("%04x", locThreshold) );
    }



    //連続マイナス}
     //ハンドラー
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            if(cntThread != null){
                cntThread.stop();
                cntThread=null;
            }
        }
    };


    private Handler timerHandler = new Handler(){
        public void handleMessage(Message msg){
            if(timerThread != null){
                timerThread.stop();
                timerThread=null;
            }
        }
    };



    private  void compPhaseSet(){
        int i;
        double fcmpPHstart;
        double fcmpPHend;
        //                 Log.d(TAG, "timer2");
        //位相START計算
        double dcmpPHstart = cmpPhase1 - (double) cmpPhase2 / 2;
        if (dcmpPHstart < 0)
            dcmpPHstart = dcmpPHstart + 360;//0-360に変換
/*
            if(dcmpPHstart < 90) {
                fcmpPHstart =( 270 + dcmpPHstart)*(0xFFFF/360);//FPGA用に変換

            }else{
                fcmpPHstart =( dcmpPHstart-90)/(0xFFFF/360);//FPGA用に変換
            }
 */

        fcmpPHstart = (dcmpPHstart) * 180.0416;//FPGA用に変換
        cmpPHstart = Math.round(fcmpPHstart);



        //位相ＥＮＤ計算
        double dcmpPHend = cmpPhase1 + (double) cmpPhase2 / 2;
        if (dcmpPHend < 0)
            dcmpPHend = dcmpPHend + 360;//0-360に変換
           /*
            if(dcmpPHend < 90) {
                fcmpPHend =( 270 + dcmpPHend)/(0xFFFF/360);//FPGA用に変換

            }else{
                fcmpPHend =( dcmpPHend-90)/(0xFFFF/360);//FPGA用に変換
            }
            */
        fcmpPHend = dcmpPHend * 180.0416;//FPGA用に変換 0xFFFFを360度をで割った値
        cmpPHend = Math.round(fcmpPHend);

}

    /**
     * EDITTEXTに入力されたドライブ周波数のセット
     *
     */
    private  void editDfrqSet(){
        double d;
        long l;
        String frqstr = dfrqEdit.getText().toString();

        try {
            d = Double.parseDouble(frqstr);
        }catch (NumberFormatException e){
            d=0;
        }

        //テキスト入力が999.9桁以上(5桁）だったら制限
         if(d > 999.9 ){
            d=999.9;
        }

        String unit = (String) nSpinner.getSelectedItem();
        //kだった場合
        if(unit == "k")
            l =  Math.round(d * 1000.0);
       //Mだった場合
        else if (unit =="M"){
            l=  Math.round(d*1000000);
        //そのまま
        }else{
            l= Math.round(d);
        }
        //10M以上だったら制限
        if(l>10000000){
            l=10000000;
        }

        dfrq[settingCh]=l;
        //周波数表示
        dfrqDisplay();
        //ドライブ周波数によりフィルタが制限されことがあるのでフィルタ設定
        filterSet();
        frqstr = String.format("%09d", dfrq);
/*
        try {
            SndDfrq = frqstr.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {


            e.printStackTrace();
        }
*/
    }

    /**
     *
     * @param data
     */
    public void agsSet(byte[] data){
        byte[] getAgsPer=new byte[2];


        for(int count=0 ; count < 2 ; count++){
            getAgsPer[count]=data[count+4];//データをコピー
        }

        String perStr = null;
        try {
            perStr = new String(getAgsPer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int per=(int)(Integer.parseInt(perStr,16));//16進数を変換
        progressDialog.setProgress(per);


        Log.d(TAG, "per=" + per);
        if(per >= 10){
            if(progressDialog!=null)
            progressDialog.cancel();
            mainActivty.funcflag=EMIC_Activity.FuncEnable.NONE;
  //          agsBt.setText("設定開始");
        }
    }

    /**
     *
     * @param data
     */
    public void gainSet(byte[] data){

        byte getGain[]=new byte[4];

        for(int count=0 ; count < 4 ; count++){
            getGain[count]=data[count+4];//データをコピー
        }

        String gainStr = null;
        try {
            gainStr = new String(getGain, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        gain[settingCh]=(int)(Integer.parseInt(gainStr, 16)/10);//16進数を変換
        Log.d(TAG, "gain=" + gain);
        String   gainTextr=String.format("%04d", gain);//文字に変換
        gainText.setText(gainTextr);//表示

    }

    /**
     * フィルタ設定　
     */
    private void filterSet() {
/*200k 20k
        10k
        5k
        1k

*/

        if(lpf[settingCh]<=hpf[settingCh]) {
            lpf[settingCh]=hpf[settingCh]+1;
        }
        if(dfrq[settingCh] <= 5000){ //5k以下
            pre_lpf=0xC0;
            if(lpf[settingCh] > 39) {
                lpf[settingCh] = 39;

                if(hpf[settingCh] >= lpf[settingCh]) {
                    hpf[settingCh]=lpf[settingCh]-1;
                }
                if(toast1 != null) {
                    toast1.cancel();
                }
                toast1 = Toast.makeText(getActivity(), "ドライブ周波数によりＬＰＦが制限されます。", Toast.LENGTH_SHORT);
                toast1.show();

            }
        }else if(dfrq[settingCh] <= 10000){ //20k以下
            pre_lpf=0x80;
            if(lpf[settingCh] > 48) {
                lpf[settingCh] = 48;//48k

                if(hpf[settingCh] >= lpf[settingCh]) {
                    hpf[settingCh]=lpf[settingCh]-1;
                }
                if(toast1 != null) {
                    toast1.cancel();
                }
                toast1 = Toast.makeText(getActivity(), "ドライブ周波数によりＬＰＦが制限されます。", Toast.LENGTH_SHORT);
                toast1.show();
            }
        }else if(dfrq[settingCh] <= 20000){ //20k以下
            pre_lpf=0x40;
            if(lpf[settingCh] > 52) {
                lpf[settingCh] = 52;//10k

                if(hpf[settingCh] >= lpf[settingCh]) {
                    hpf[settingCh]=lpf[settingCh]-1;
                }
                if(toast1 != null) {
                    toast1.cancel();
                }
                Toast.makeText(getActivity(), "ドライブ周波数によりＬＰＦが制限されます。", Toast.LENGTH_SHORT).show();
            }
        }else{
            pre_lpf=0x00;
        }


        if(toast1 != null) {
            toast1.cancel();
        }


        if(hpf[settingCh]< 0)
            hpf[settingCh]=0;

        if(lpf[settingCh] <0)
            lpf[settingCh]=0;

        if(0 <= hpf[settingCh] &&  hpf[settingCh] <= 4) {//100
            cic=0;
            if(lpf[settingCh] >21) {
                lpf[settingCh] = 21;

                toast1 = Toast.makeText(getActivity(), LPF_LIMIT_NOTIFY, Toast.LENGTH_SHORT);
                toast1.show();
            }


            lpfIndex[settingCh] = LpsNum[0][lpf[settingCh]];

        }else if(5 <= hpf[settingCh] &&  hpf[settingCh] <= 8) {//200
            cic=1;

            if(lpf[settingCh] > 25) {
                lpf[settingCh] = 25;
                toast1 = Toast.makeText(getActivity(), LPF_LIMIT_NOTIFY, Toast.LENGTH_SHORT);
                toast1.show();
            }else if(lpf[settingCh] < 5 ) {
                lpf[settingCh]=5;
            }

            lpfIndex[settingCh] = LpsNum[1][lpf[settingCh]-5];

        }else if(9 <= hpf[settingCh] &&  hpf[settingCh] <= 12) {//400
            cic=2;

            if(lpf[settingCh] >29) {
                lpf[settingCh] = 29;
                toast1 = Toast.makeText(getActivity(), LPF_LIMIT_NOTIFY, Toast.LENGTH_SHORT);
                toast1.show();
            }else if(lpf[settingCh] < 8 ) {
                lpf[settingCh]=8;
            }

            lpfIndex[settingCh] = LpsNum[2][lpf[settingCh]-8];
        }else if (13 <= hpf[settingCh] &&  hpf[settingCh] <= 17) {//1000
            cic=3;
            if(lpf[settingCh] > 34) {
                lpf[settingCh] = 34;
                toast1 = Toast.makeText(getActivity(), LPF_LIMIT_NOTIFY, Toast.LENGTH_SHORT);
                toast1.show();
            }else if(lpf[settingCh] < 13 ) {
                lpf[settingCh]=13;
            }
            lpfIndex[settingCh] = LpsNum[0][lpf[settingCh]-13];
        }else if (18 <= hpf[settingCh] &&  hpf[settingCh] <= 21) {//2000
            cic=4;
            if(lpf[settingCh] > 38) {
                lpf[settingCh] = 38;
                toast1 = Toast.makeText(getActivity(), LPF_LIMIT_NOTIFY, Toast.LENGTH_SHORT);
                toast1.show();
            }else if(lpf[settingCh] < 17 ) {
                lpf[settingCh]=17;
            }
            lpfIndex[settingCh] = LpsNum[1][lpf[settingCh]-17];
        }else if(22 <= hpf[settingCh] &&  hpf[settingCh] <= 25) {//4000
            cic=5;

            if(lpf[settingCh] > 42) {
                lpf[settingCh] = 42;
                toast1 = Toast.makeText(getActivity(), LPF_LIMIT_NOTIFY, Toast.LENGTH_SHORT);
                toast1.show();
            }else if(lpf[settingCh] < 21 ) {
                lpf[settingCh]=21;
            }

            lpf[settingCh] = LpsNum[2][lpf[settingCh]-21];
        }else if(26 <= hpf[settingCh] &&  hpf[settingCh] <= 30) {//10k
            cic=6;

            if(lpf[settingCh] > 47) {
                lpf[settingCh] = 47;
                toast1 = Toast.makeText(getActivity(), LPF_LIMIT_NOTIFY, Toast.LENGTH_SHORT);
                toast1.show();
            }else if(lpf[settingCh] < 26 ) {
                lpf[settingCh]=26;
            }

            lpfIndex[settingCh] = LpsNum[0][lpf[settingCh]-26];
        }else if(31 <= hpf[settingCh] &&  hpf[settingCh] <= 34) {//20k
            cic=7;

            if(lpf[settingCh] > 51) {
                lpf[settingCh] = 51;
                toast1 = Toast.makeText(getActivity(), LPF_LIMIT_NOTIFY, Toast.LENGTH_SHORT);
                toast1.show();
            }else if(lpf[settingCh] < 30 ) {
                lpf[settingCh]=30;
            }

            lpfIndex[settingCh] = LpsNum[1][lpf[settingCh]-30];
        }else if(35 <= hpf[settingCh] &&  hpf[settingCh] <= 38) {//40k
            cic=8;

            if(lpf[settingCh] > 55) {
                lpf[settingCh] = 55;
                ;
            }else if(lpf[settingCh] < 35 ) {
                lpf[settingCh]=35;
            }


            lpfIndex[settingCh] = LpsNum[2][lpf[settingCh]-35];


        }else if(39 <= hpf[settingCh] &&  hpf[settingCh] <= 52) {//100kサンプリング

            cic=9;

            if(lpf[settingCh] > 56) {
                lpf[settingCh] = 56;
            }else if(lpf[settingCh] < 39 ) {
                lpf[settingCh]=39;
            }


            lpfIndex[settingCh] = LpsNum[0][lpf[settingCh]-39];
        }else{
            cic=9;
            hpf[settingCh] =52;
            lpf[settingCh]=56;

            lpfIndex[settingCh] = LpsNum[0][lpf[settingCh]-39];
        }
        lpfIndex[settingCh] |= pre_lpf;
        hpfText.setText(FilterStr[hpf[settingCh]] + "Hz");
        lpfText.setText(FilterStr[lpf[settingCh]]+"Hz");



    }




    private void locStart() {

        agcSQLiteOpenHelper ahlpr= new agcSQLiteOpenHelper(getActivity());



        //データベース呼び出し
        sdb = ahlpr.getWritableDatabase();
       //AgcCalNumに保存した値によりデータ読み込み
        Cursor cursor=sdb.query(ahlpr.DB_TABLE, null, ahlpr.Key_NUM + "='" +AgcCalNum+"'", null, null, null, null);
        int getcnt=cursor.getCount();
        Log.d(TAG, "getcnt" + getcnt);
        if(getcnt > 0) {
            cursor.moveToFirst();
            for(int i =0 ; i < AgcSettingFragment.CALNUMMAX ; i++ ) {
                int index = cursor.getColumnIndex(ahlpr.Key_P2DATA[i]);
                calPw2Data[ AgcSettingFragment.CALNUMMAX -1-i] = cursor.getInt(index);
                Log.d(TAG, "calPw2Data" + calPw2Data[ AgcSettingFragment.CALNUMMAX -1-i]);
            }
            for(int i =0 ; i < AgcSettingFragment.CALNUMMAX ; i++ ) {
                int index = cursor.getColumnIndex(ahlpr.Key_P1DATA[i]);
                calPw1Data[ AgcSettingFragment.CALNUMMAX -1-i] = cursor.getInt(index);
                Log.d(TAG, "calPw1Data" + calPw1Data[ AgcSettingFragment.CALNUMMAX -1-i]);
            }

            calSndNum=0;
            mainLoop=false;
            progressDialog = new ProgressDialog(getActivity());
            // プログレスダイアログのタイトルを設定します
            //                     progressDialog.setTitle("タイトル");
            // プログレスダイアログのメッセージを設定します
            progressDialog.setMessage("LOC初期設定をしています。");
            // プログレスダイアログの確定（false）／不確定（true）を設定します
            progressDialog.setIndeterminate(false);
            // プログレスダイアログのスタイルを水平スタイルに設定します
            //                      progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// プログレスダイアログのスタイルを円スタイルに設定します
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // ProgressDialog をキャンセル
                            Log.d("test", "LOC cancel clicked");
                            mainLoop=true;
                            //                    agsBt.setText("設定開始");
                            dialog.cancel();
                        }
                    });
            // プログレスダイアログの最大値を設定します
            progressDialog.setMax(10);
            // プログレスダイアログの値を設定します
            //                     progressDialog.incrementProgressBy(0);
            // プログレスダイアログのセカンダリ値を設定します
            //                    progressDialog.incrementSecondaryProgressBy(70);
            // プログレスダイアログのキャンセルが可能かどうかを設定します
            progressDialog.setCancelable(false);
            // プログレスダイアログを表示します
            progressDialog.show();
            //         agsBt.setText("キャンセル");

/*
            for(int i=0 ; i<9; i++) {
                agcCorrectTable[i] = (calPw1Data[i] - calPw1Data[i+1]) / (calPw2Data[i] - calPw2Data[i+1]);
            }


            reciprocal0_5mm =1/calPw1Data[4];//0.5mmのP1を基準に
*/



        }
        /*
        switch (AgcSampleType){
            case "Fe":
                agcCorrectTable =FeCalData;
                break;
            case "Al":
                agcCorrectTable =AlCalData;
                break;

        }
*/
    }

    private String agcControl(){
        int nowPw2=mainActivty.pw2Get();

        if (calPw2Data[9] > nowPw2) {
            agcCorrect =( agcCorrectTable[8] * calPw2Data[9] + calPw1Data[9] ) * reciprocal0_5mm;
        }else  if (calPw2Data[8] > nowPw2) {
            agcCorrect =( agcCorrectTable[8] * nowPw2 + calPw1Data[8] ) * reciprocal0_5mm;
        } else if (calPw2Data[7] > nowPw2) {
            agcCorrect =( agcCorrectTable[7] * nowPw2 + calPw1Data[7] ) * reciprocal0_5mm;
        } else if (calPw2Data[6] > nowPw2) {
            agcCorrect =( agcCorrectTable[6] * nowPw2 + calPw1Data[6] ) * reciprocal0_5mm;
        } else if (calPw2Data[5] > nowPw2) {
            agcCorrect =( agcCorrectTable[5] * nowPw2 + calPw1Data[5] ) * reciprocal0_5mm;
        } else if (calPw2Data[4] > nowPw2) {
            agcCorrect =( agcCorrectTable[4] * nowPw2 + calPw1Data[4] ) * reciprocal0_5mm;
        } else if (calPw2Data[3] > nowPw2) {
            agcCorrect =( agcCorrectTable[3] * nowPw2 + calPw1Data[3] ) * reciprocal0_5mm;
        } else if (calPw2Data[2] > nowPw2) {
            agcCorrect =( agcCorrectTable[2] * nowPw2 + calPw1Data[2] ) * reciprocal0_5mm;
        } else if (calPw2Data[1] > nowPw2) {
            agcCorrect =( agcCorrectTable[1] * nowPw2 + calPw1Data[1] ) * reciprocal0_5mm;
        } else if (calPw2Data[0] > nowPw2) {
            agcCorrect =( agcCorrectTable[0] * nowPw2 + calPw1Data[0] ) * reciprocal0_5mm;
        } else {
            agcCorrect =( agcCorrectTable[0] * calPw2Data[0] + calPw1Data[0] ) * reciprocal0_5mm;
        }
        /*
        if (calPw2Data[9] < nowPw2) {
            agcCorrect =( agcCorrectTable[8] * calPw2Data[9] + calPw1Data[9] ) * reciprocal0_5mm;
        }else  if (calPw2Data[8] < nowPw2) {
            agcCorrect =( agcCorrectTable[8] * nowPw2 + calPw1Data[8] ) * reciprocal0_5mm;
        } else if (calPw2Data[7] < nowPw2) {
            agcCorrect =( agcCorrectTable[7] * nowPw2 + calPw1Data[7] ) * reciprocal0_5mm;
        } else if (calPw2Data[6] < nowPw2) {
            agcCorrect =( agcCorrectTable[6] * nowPw2 + calPw1Data[6] ) * reciprocal0_5mm;
        } else if (calPw2Data[5] < nowPw2) {
            agcCorrect =( agcCorrectTable[5] * nowPw2 + calPw1Data[5] ) * reciprocal0_5mm;
        } else if (calPw2Data[4] < nowPw2) {
            agcCorrect =( agcCorrectTable[4] * nowPw2 + calPw1Data[4] ) * reciprocal0_5mm;
        } else if (calPw2Data[3] < nowPw2) {
            agcCorrect =( agcCorrectTable[3] * nowPw2 + calPw1Data[3] ) * reciprocal0_5mm;
        } else if (calPw2Data[2] < nowPw2) {
            agcCorrect =( agcCorrectTable[2] * nowPw2 + calPw1Data[2] ) * reciprocal0_5mm;
        } else if (calPw2Data[1] < nowPw2) {
            agcCorrect =( agcCorrectTable[1] * nowPw2 + calPw1Data[1] ) * reciprocal0_5mm;
        } else if (calPw2Data[0] < nowPw2) {
            agcCorrect =( agcCorrectTable[0] * nowPw2 + calPw1Data[0] ) * reciprocal0_5mm;
        } else {
            agcCorrect =( agcCorrectTable[8] * calPw2Data[9] + calPw1Data[0] ) * reciprocal0_5mm;
        }
        */
           /*

        switch (AgcSampleType) {

            case "Fe":
            if (calPw2Data[0] < nowPw2) {
                agcCorrect = agcCorrectTable[0];
            }else    if (calPw2Data[1] < nowPw2) {
                agcCorrect = agcCorrectTable[1] + (agcCorrectTable[0] - agcCorrectTable[1]) / (calPw2Data[0] - calPw2Data[1])
                        * (nowPw2 - calPw2Data[1]);
            } else if (calPw2Data[2] < nowPw2) {
                agcCorrect = agcCorrectTable[2] + (agcCorrectTable[1] - agcCorrectTable[2]) / (calPw2Data[1] - calPw2Data[2])
                        * (nowPw2 - calPw2Data[2]);
            } else if (calPw2Data[3] < nowPw2) {
                agcCorrect = agcCorrectTable[3] + (agcCorrectTable[2] - agcCorrectTable[3]) / (calPw2Data[2] - calPw2Data[3])
                        * (nowPw2 - calPw2Data[3]);
            } else if (calPw2Data[4] < nowPw2) {
                agcCorrect = agcCorrectTable[4] + (agcCorrectTable[3] - agcCorrectTable[4]) / (calPw2Data[3] - calPw2Data[4])
                        * (nowPw2 - calPw2Data[4]);
            } else if (calPw2Data[5] < nowPw2) {
                agcCorrect = agcCorrectTable[5] + (agcCorrectTable[4] - agcCorrectTable[5]) / (calPw2Data[4] - calPw2Data[5])
                        * (nowPw2 - calPw2Data[5]);
            } else if (calPw2Data[6] < nowPw2) {
                agcCorrect = agcCorrectTable[6] + (agcCorrectTable[5] - agcCorrectTable[6]) / (calPw2Data[5] - calPw2Data[6])
                        * (nowPw2 - calPw2Data[6]);
            } else if (calPw2Data[7] < nowPw2) {
                agcCorrect = agcCorrectTable[7] + (agcCorrectTable[6] - agcCorrectTable[7]) / (calPw2Data[6] - calPw2Data[7])
                        * (nowPw2 - calPw2Data[7]);
            } else if (calPw2Data[8] < nowPw2) {
                agcCorrect = agcCorrectTable[8] + (agcCorrectTable[7] - agcCorrectTable[8]) / (calPw2Data[7] - calPw2Data[8])
                        * (nowPw2 - calPw2Data[8]);
            } else if (calPw2Data[9] < nowPw2) {
                agcCorrect = agcCorrectTable[9] + (agcCorrectTable[8] - agcCorrectTable[9]) / (calPw2Data[8] - calPw2Data[9])
                        * (nowPw2 - calPw2Data[9]);
            } else {
                agcCorrect = agcCorrectTable[9];
            }
                break;
            case "Al":
            if (calPw2Data[9] < nowPw2) {
                agcCorrect = agcCorrectTable[0];
            }else  if (calPw2Data[8] < nowPw2) {
                agcCorrect = agcCorrectTable[8] + (agcCorrectTable[9] - agcCorrectTable[8]) / (calPw2Data[9] - calPw2Data[8])
                        * (nowPw2 - calPw2Data[8]);
            } else if (calPw2Data[7] < nowPw2) {
                agcCorrect = agcCorrectTable[7] + (agcCorrectTable[8] - agcCorrectTable[7]) / (calPw2Data[8] - calPw2Data[7])
                        * (nowPw2 - calPw2Data[7]);
            } else if (calPw2Data[6] < nowPw2) {
                agcCorrect = agcCorrectTable[6] + (agcCorrectTable[7] - agcCorrectTable[6]) / (calPw2Data[7] - calPw2Data[6])
                        * (nowPw2 - calPw2Data[6]);
            } else if (calPw2Data[5] < nowPw2) {
                agcCorrect = agcCorrectTable[5] + (agcCorrectTable[6] - agcCorrectTable[5]) / (calPw2Data[6] - calPw2Data[5])
                        * (nowPw2 - calPw2Data[5]);
            } else if (calPw2Data[4] < nowPw2) {
                agcCorrect = agcCorrectTable[4] + (agcCorrectTable[5] - agcCorrectTable[4]) / (calPw2Data[5] - calPw2Data[4])
                        * (nowPw2 - calPw2Data[4]);
            } else if (calPw2Data[3] < nowPw2) {
                agcCorrect = agcCorrectTable[3] + (agcCorrectTable[4] - agcCorrectTable[3]) / (calPw2Data[4] - calPw2Data[3])
                        * (nowPw2 - calPw2Data[3]);
            } else if (calPw2Data[2] < nowPw2) {
                agcCorrect = agcCorrectTable[2] + (agcCorrectTable[3] - agcCorrectTable[2]) / (calPw2Data[3] - calPw2Data[2])
                        * (nowPw2 - calPw2Data[2]);
            } else if (calPw2Data[1] < nowPw2) {
                agcCorrect = agcCorrectTable[1] + (agcCorrectTable[2] - agcCorrectTable[1]) / (calPw2Data[2] - calPw2Data[1])
                        * (nowPw2 - calPw2Data[1]);
            } else if (calPw2Data[0] < nowPw2) {
                agcCorrect = agcCorrectTable[0] + (agcCorrectTable[1] - agcCorrectTable[0]) / (calPw2Data[1] - calPw2Data[0])
                        * (nowPw2 - calPw2Data[0]);
            } else {
                agcCorrect = agcCorrectTable[0];
            }
                break;
        }
        */
       int tempgain =(int)( gain[settingCh]* agcCorrect);
        if(tempgain <0)
            tempgain=0;

        if(tempgain>6000)
            tempgain=6000;

        String gainStr = String.format("%04X", gain);
    return gainStr;
    }

    public void ackSet(){
        ackRecive =true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "invoked onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "invoked onStart");

        if(timerOn==false) {
            timerOn = true;
  //          timerHandler.post(timerRunnable1);
            timerThread = new Thread(timerRunnable1);
            //連続イベント開始
            timerThread.start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timerOn=false;
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
        Log.d(TAG, "invoked onStop");

        SharedPreferences sp = mainActivty.getSharedPreferences("measure", mainActivty.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putLong("dfrq", dfrq[settingCh]);
        editor.putInt("hpf", hpf[settingCh]);
        editor.putInt( "lpf", lpf[settingCh] );
        editor.putInt( "gain", gain[settingCh] );
        editor.putInt( "cmpPower", cmpPower );
        editor.putInt( "cmpPhase1", cmpPhase1 );
        editor.putInt( "cmpPhase2", cmpPhase2 );


        // 書き込みの確定（実際にファイルに書き込む）
        editor.commit();

        timerOn=false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "invoked onDestroy");
        cntThread=null;
    }
}
