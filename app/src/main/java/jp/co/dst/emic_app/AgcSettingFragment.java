package jp.co.dst.emic_app;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AgcSettingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class AgcSettingFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "AgcSettingFragment";
    private OnFragmentInteractionListener mListener;
    private EMIC_Activity mainActivty;
    private SQLiteDatabase sdb;
    private TextView explainText;
    private boolean mesure_do;
    private static  final String EXPALIN1="試料との距離を";
    private static  final String EXPALIN2="とし、開始ボタンを押してください。";
    Button nextBt;
    private boolean calTimerOn=false;
    private static final byte  AclCmd[]={0x41, 0x43, 0x4C, 0x20} ;//ACL
    private static final byte  Cr[] ={0x0d};
    private byte[]  SndCalDistanse = new byte[2];
    public  static final int CALNUMMAX =5;
    public static final String calDisStr[]={
                    "1mm",
     //               "0.9mm",
                    "0.8mm",
     //               "0.7mm",
                    "0.6mm",
     //               "0.5mm",
                    "0.4mm",
     //               "0.3mm",
                    "0.2mm",
      //              "0.1mm",
            };

    private int[] pw2Result=new int[CALNUMMAX];
    private int[] pw1Result=new int[CALNUMMAX];
    private int pw2Int;
    public int CalDisNum=0 ;
    private String SelCalNum;
    private String nowDate;
    private TextView pw1_1mmText;
  //  private TextView pw1_0_9mmText;
    private TextView pw1_0_8mmText;
   // private TextView pw1_0_7mmText;
    private TextView pw1_0_6mmText;
 //   private TextView pw1_0_5mmText;
    private TextView pw1_0_4mmText;
//    private TextView pw1_0_3mmText;
    private TextView pw1_0_2mmText;
 //   private TextView pw1_0_1mmText;
    private TextView pw2_1mmText;
 //   private TextView pw2_0_9mmText;
    private TextView pw2_0_8mmText;
  //  private TextView pw2_0_7mmText;
    private TextView pw2_0_6mmText;
  //  private TextView pw2_0_5mmText;
    private TextView pw2_0_4mmText;
  //  private TextView pw2_0_3mmText;
    private TextView pw2_0_2mmText;
 //   private TextView pw2_0_1mmText;

    private enum CALSTATE{NONE,cal1_0mm,cal0_9mm,cal0_8mm,cal0_7mm,cal0_6mm,cal0_5mm,cal0_4mm,cal0_3mm,cal0_2mm,cal0_1mm,cal_complete}

    CALSTATE calState=CALSTATE.NONE;
    private enum MEASRESTATE{NONE,DO,COMPLETE}

    MEASRESTATE measureState=MEASRESTATE.NONE;


    /**
     * 起動時ビュー設定
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_agc_setting, container, false);
        //ボタン設定
        Button retBt = (Button) v.findViewById(R.id.agcSetRtbutton);
        retBt.setOnClickListener(this);
        nextBt = (Button) v.findViewById(R.id.agcNextButton);
        nextBt.setOnClickListener(this);
        Button interruptBt = (Button) v.findViewById(R.id.agcInterruptbutton);
        interruptBt.setOnClickListener(this);


        //選択された番号を取得
        SelCalNum = getArguments().getString("CalNum");
        Log.v(TAG, "dataを表示" + SelCalNum);
        //表示
        TextView calselText=(TextView) v.findViewById(R.id.nowCalText);
        calselText.setText(SelCalNum);

        //説明文設定
        explainText=(TextView) v.findViewById(R.id.agsExplainText);
        explainText.setText(EXPALIN1 + "1mm" + EXPALIN2);

        //データ部
        pw2_1mmText =(TextView) v.findViewById(R.id.pw2Text1mm);
      //  pw2_0_9mmText =(TextView) v.findViewById(R.id.pw2Text0_9mm);
        pw2_0_8mmText =(TextView) v.findViewById(R.id.pw2Text0_8mm);
     //   pw2_0_7mmText =(TextView) v.findViewById(R.id.pw2Text0_7mm);
        pw2_0_6mmText =(TextView) v.findViewById(R.id.pw2Text0_6mm);
     //   pw2_0_5mmText =(TextView) v.findViewById(R.id.pw2Text0_5mm);
        pw2_0_4mmText =(TextView) v.findViewById(R.id.pw2Text0_4mm);
     //   pw2_0_3mmText =(TextView) v.findViewById(R.id.pw2Text0_3mm);
        pw2_0_2mmText =(TextView) v.findViewById(R.id.pw2Text0_2mm);
     //   pw2_0_1mmText =(TextView) v.findViewById(R.id.pw2Text0_1mm);
        pw1_1mmText =(TextView) v.findViewById(R.id.pw1Text1mm);
     //   pw1_0_9mmText =(TextView) v.findViewById(R.id.pw1Text0_9mm);
        pw1_0_8mmText =(TextView) v.findViewById(R.id.pw1Text0_8mm);
     //   pw1_0_7mmText =(TextView) v.findViewById(R.id.pw1Text0_7mm);
        pw1_0_6mmText =(TextView) v.findViewById(R.id.pw1Text0_6mm);
     //   pw1_0_5mmText =(TextView) v.findViewById(R.id.pw1Text0_5mm);
        pw1_0_4mmText =(TextView) v.findViewById(R.id.pw1Text0_4mm);
      //  pw1_0_3mmText =(TextView) v.findViewById(R.id.pw1Text0_3mm);
        pw1_0_2mmText =(TextView) v.findViewById(R.id.pw1Text0_2mm);
      //  pw1_0_1mmText =(TextView) v.findViewById(R.id.pw1Text0_1mm);



        measureState=MEASRESTATE.NONE;
        calState= CALSTATE.cal1_0mm;
        CalDisNum=0;
        String sndStr = String.format("%02x", CalDisNum);
        try {
            SndCalDistanse = sndStr.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        TextView timeText=(TextView) v.findViewById(R.id.nowTimeText);
        measureState=MEASRESTATE.NONE;
        calState= CALSTATE.cal1_0mm;


        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);// 0 - 11
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        nowDate = year + "/" + (month+1) + "/" + day + "/"+ hour + ":" + minute;


        timeText.setText(nowDate);

        mainActivty =(EMIC_Activity) getActivity();




        agcSQLiteOpenHelper ahlpr= new agcSQLiteOpenHelper(getActivity());
        //データベース呼び出し
        sdb = ahlpr.getWritableDatabase();
        //AgcCalNumに保存した値によりデータ読み込み
        Cursor cursor=sdb.query(ahlpr.DB_TABLE, null, ahlpr.Key_NUM + "='" +SelCalNum+"'", null, null, null, null);
        int getcnt=cursor.getCount();
        Log.d(TAG, "getcnt" + getcnt);
        if(getcnt > 0) {
            cursor.moveToFirst();
            pw2_1mmText.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(ahlpr.Key_P2DATA[0]))));
            pw1_1mmText.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(ahlpr.Key_P1DATA[0]))));
            pw2_0_8mmText.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(ahlpr.Key_P2DATA[1]))));
            pw1_0_8mmText.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(ahlpr.Key_P1DATA[1]))));
            pw2_0_6mmText.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(ahlpr.Key_P2DATA[2]))));
            pw1_0_6mmText.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(ahlpr.Key_P1DATA[2]))));
            pw2_0_4mmText.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(ahlpr.Key_P2DATA[3]))));
            pw1_0_4mmText.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(ahlpr.Key_P1DATA[3]))));
            pw2_0_2mmText.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(ahlpr.Key_P2DATA[4]))));
            pw1_0_2mmText.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(ahlpr.Key_P1DATA[4]))));
            timeText.setText(cursor.getString(cursor.getColumnIndex(ahlpr.Key_DATE)));
        }

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        calTimerOn = false;
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    /**
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //中断
            case R.id.agcInterruptbutton:
                calTimerOn = false;
                measureState=MEASRESTATE.NONE;
                calState= CALSTATE.cal1_0mm;
                nextBt.setVisibility(View.VISIBLE);
                nextBt.setText("開始");
                explainText.setText(EXPALIN1 + "1mm" + EXPALIN2);
                CalDisNum=0;

                break;
            //戻るボタン
            case R.id.agcSetRtbutton:
                mainActivty.displayBackStack();
                break;
            //開始　次へボタン
            case R.id.agcNextButton:
                switch (measureState) {
                    //開始
                    case NONE:
                        nextBt.setVisibility(View.INVISIBLE);
                        explainText.setText("測定中");
                        //ステートをＤＯへ
                        measureState = MEASRESTATE.DO;
                       //一定周期の処理を開始
                        calTimerOn = true;
                        calTimerHandler.post(calTimerRunnable);
                        break;
                   //終わるまでは何もしない
                    case DO:
                        break;
                    //次へ
                    case COMPLETE:
                        nextBt.setVisibility(View.VISIBLE);
                        nextBt.setText("開始");
                        if(CalDisNum < CALNUMMAX-1) {
                            CalDisNum++;
                        }else {
                            //終了していたら保存処理
                            CalDisNum=0;
                            // 確認ダイアログの生成
                            AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
 //                           alertDlg.setTitle("ダイアログタイトル");
                            alertDlg.setMessage("保存しますか？");
                            alertDlg.setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // OK ボタンクリック処理
                                            Log.d(TAG, "保存処理");
                                            saveCalData(pw2Result,pw1Result);
                                        }
                                    });
                            alertDlg.setNegativeButton(
                                    "Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Cancel ボタンクリック処理
                                        }
                                    });

                            // 表示
                            alertDlg.create().show();
                        }

                        //説明文表示
                        explainText.setText(EXPALIN1 + calDisStr[CalDisNum] + EXPALIN2);
                        measureState = MEASRESTATE.NONE;

                        break;
                       /*
                        switch (calState) {
                            case NONE:
                                calState = CALSTATE.cal1_0mm;
                                break;
                            case cal1_0mm:

                                calState = CALSTATE.cal0_9mm;
                                calDisStr = "0.9mm";
                                CalDistanse[0] = 0x30;//0
                                CalDistanse[1] = 0x39;//9

                                break;
                            case cal0_9mm:
                                explainText.setText(EXPALIN1 + "0.8mm" + EXPALIN2);
                                calState = CALSTATE.cal0_8mm;
                                calDisStr = "0.9mm";
                                CalDistanse[0] = 0x30;//0
                                CalDistanse[1] = 0x38;//8
                                break;
                            case cal0_8mm:
                                explainText.setText(EXPALIN1 + "0.7mm" + EXPALIN2);
                                calState = CALSTATE.cal0_7mm;
                                calDisStr = "0.7mm";
                                CalDistanse[0] = 0x30;//0
                                CalDistanse[1] = 0x38;//7
                                break;
                            case cal0_7mm:
                                explainText.setText(EXPALIN1 + "0.6mm" + EXPALIN2);
                                calState = CALSTATE.cal0_6mm;
                                calDisStr = "0.6mm";
                                CalDistanse[0] = 0x30;//0
                                CalDistanse[1] = 0x38;//6
                                break;
                            case cal0_6mm:
                                explainText.setText(EXPALIN1 + "0.5mm" + EXPALIN2);
                                calState = CALSTATE.cal0_5mm;
                                calDisStr = "0.5mm";
                                CalDistanse[0] = 0x30;//0
                                CalDistanse[1] = 0x38;//5
                                break;
                            case cal0_5mm:
                                explainText.setText(EXPALIN1 + "0.4mm" + EXPALIN2);
                                calState = CALSTATE.cal0_4mm;
                                calDisStr = "0.4mm";
                                CalDistanse[0] = 0x30;//0
                                CalDistanse[1] = 0x38;//4
                                break;
                            case cal0_4mm:
                                explainText.setText(EXPALIN1 + "0.3mm" + EXPALIN2);
                                calState = CALSTATE.cal0_3mm;
                                calDisStr = "0.3mm";
                                CalDistanse[0] = 0x30;//0
                                CalDistanse[1] = 0x38;//3
                                break;
                            case cal0_3mm:
                                explainText.setText(EXPALIN1 + "0.2mm" + EXPALIN2);
                                calState = CALSTATE.cal0_2mm;
                                calDisStr = "0.9mm";
                                CalDistanse[0] = 0x30;//0
                                CalDistanse[1] = 0x38;//2
                                break;
                            case cal0_2mm:
                                explainText.setText(EXPALIN1 + "0.1mm" + EXPALIN2);
                                calState = CALSTATE.cal0_1mm;
                                calDisStr = "0.1mm";
                                CalDistanse[0] = 0x30;//0
                                CalDistanse[1] = 0x38;//1
                                break;
                            case cal0_1mm:

                                // 確認ダイアログの生成
                                AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
                                alertDlg.setTitle("ダイアログタイトル");
                                alertDlg.setMessage("メッセージ");
                                alertDlg.setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // OK ボタンクリック処理
                                                Log.d(TAG, "保存処理");
                                                saveCalData();
                                            }
                                        });
                                alertDlg.setNegativeButton(
                                        "Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Cancel ボタンクリック処理
                                            }
                                        });

                                // 表示
                                alertDlg.create().show();
                                explainText.setText(EXPALIN1 + "1mm" + EXPALIN2);
                                calDisStr = "1mm";
                                calState = CALSTATE.cal_complete;

                                break;

                        }
                        */


                }
        }


    }
    private Runnable calTimerRunnable = new Runnable() {

        int i;
        byte[] sum=new byte[1];
        @Override
        public void run() {
      /* do what you need to do */


            Log.d(TAG, "calTimerRunnable");
            String sndStr = String.format("%02x", CalDisNum);
            try {
                SndCalDistanse = sndStr.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mainActivty.write(AclCmd);            // 送信実行
            mainActivty.write(SndCalDistanse);            // 送信実行
            sum[0]=(byte)(~SndCalDistanse[0]+SndCalDistanse[1]);
            mainActivty.write(sum);
            mainActivty.write(Cr);            // 送信実行


            if (calTimerOn == true) {


                calTimerHandler.postDelayed(calTimerRunnable, 1000);

            }
        }

    };
    private Thread calTimerThread;
    private android.os.Handler calTimerHandler = new android.os.Handler() {

        public void handleMessage(Message msg) {
            if (calTimerThread != null) {
                calTimerThread.stop();
                calTimerThread = null;
            }
        }
    };

    /**
     *
     * @param data
     */
    void pw2_set(byte[] data){
        byte[] now_pw2 = new byte[2];
        String pw2Str="00";
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

    }

    /**
     *
     * @param data
     */
    public  void mpuAgcCalNUM_set(byte[] data){
            byte[] MpuCalDistance = new byte[2];
        //System.arraycopy(data,0,MpuCalDistance, 0, 1); //debug用
        System.arraycopy(data,4,MpuCalDistance, 0, 2); //

        //送信と受信した番号が一致したら次へ
        if(Arrays.equals(MpuCalDistance,SndCalDistanse)&& calTimerOn==true){
            calTimerOn = false;

        //    pw2Result[CalDisNum]=CalDisNum;//デバック用
            pw2Result[CalDisNum]=mainActivty.pw2AverageGet();
            pw1Result[CalDisNum]=mainActivty.pw1Get();
            Log.d(TAG, "pw2Result=" +  pw2Result[CalDisNum]);
            switch (CalDisNum){
                case 0:
                    pw2_1mmText.setText(String.valueOf(pw2Result[CalDisNum]));
                    pw1_1mmText.setText(String.valueOf(pw1Result[CalDisNum]));
                    break;
 /*
                case 1:
                    pw2_0_9mmText.setText(String.valueOf(pw2Result[CalDisNum]));
                    pw1_0_9mmText.setText(String.valueOf(pw1Result[CalDisNum]));
                    break;

                case 2:
   */
                case 1:
                    pw2_0_8mmText.setText(String.valueOf(pw2Result[CalDisNum]));
                    pw1_0_8mmText.setText(String.valueOf(pw1Result[CalDisNum]));
                    break;
 /*
                case 3:
                    pw2_0_7mmText.setText(String.valueOf(pw2Result[CalDisNum]));
                    pw1_0_7mmText.setText(String.valueOf(pw1Result[CalDisNum]));
                    break;

                case 4:
                 */
                case 2:
                    pw2_0_6mmText.setText(String.valueOf(pw2Result[CalDisNum]));
                    pw1_0_6mmText.setText(String.valueOf(pw1Result[CalDisNum]));
                    break;
 /*
                case 5:
                    pw2_0_5mmText.setText(String.valueOf(pw2Result[CalDisNum]));
                    pw1_0_5mmText.setText(String.valueOf(pw1Result[CalDisNum]));
                    break;

                case 6:
                */
                case 3:
                    pw2_0_4mmText.setText(String.valueOf(pw2Result[CalDisNum]));
                    pw1_0_4mmText.setText(String.valueOf(pw1Result[CalDisNum]));
                    break;
/*
                case 7:
                    pw2_0_3mmText.setText(String.valueOf(pw2Result[CalDisNum]));
                    pw1_0_3mmText.setText(String.valueOf(pw1Result[CalDisNum]));
                    break;

                case 8:
                */
                case 4:
                    pw2_0_2mmText.setText(String.valueOf(pw2Result[CalDisNum]));
                    pw1_0_2mmText.setText(String.valueOf(pw1Result[CalDisNum]));
                    break;
/*
                case 9:
                    pw2_0_1mmText.setText(String.valueOf(pw2Result[CalDisNum]));
                    pw1_0_1mmText.setText(String.valueOf(pw1Result[CalDisNum]));
                    break;
 */
           }


            measureState=MEASRESTATE.COMPLETE;
            if(CalDisNum < CALNUMMAX -1){
 //             CalDisNum++;
                explainText.setText("距離" + calDisStr[CalDisNum]+ "の校正が完了しました");
                nextBt.setVisibility(View.VISIBLE);
                nextBt.setText("次へ");
            }else{
                explainText.setText("全ての校正が完了しました");
                nextBt.setVisibility(View.VISIBLE);
                nextBt.setText("保存");
            }
        }

    }

    public  void saveCalData(int[] pw2Data,int[] pw1Data){

        agcSQLiteOpenHelper ahlpr= new agcSQLiteOpenHelper(getActivity());
        sdb = ahlpr.getWritableDatabase();
  //      sdb = SQLiteDatabase.openOrCreateDatabase( ahlpr.DB_NAME, null);
        ContentValues values=new ContentValues();
        values.put(agcSQLiteOpenHelper.Key_NUM, SelCalNum);
        for(int i=0 ; i < CALNUMMAX ; i++ ) {
            values.put(agcSQLiteOpenHelper.Key_P2DATA[i], pw2Result[i]);
            values.put(agcSQLiteOpenHelper.Key_P1DATA[i], pw1Result[i]);
        }
         values.put(agcSQLiteOpenHelper.Key_DATE, nowDate);
        int colNum= sdb.update(agcSQLiteOpenHelper.DB_TABLE,values, agcSQLiteOpenHelper.Key_NUM + "='"+ SelCalNum+ "'",null);
        Log.d(TAG, "colNum" + colNum);
        if(colNum==0)
            sdb.insert(agcSQLiteOpenHelper.DB_TABLE,null,values);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
