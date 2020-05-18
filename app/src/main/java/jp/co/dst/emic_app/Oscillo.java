package jp.co.dst.emic_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

/**
 * IQデータ表示用サーフェスビュー
 */
public class Oscillo extends SurfaceView
implements Callback,Runnable {
    private SurfaceHolder mHolder;
    private static final String TAG = "Oscillo";
    private int i, j, DATA;
    private Thread mThread;
    private boolean mIsAttached;
    private int head, tail;
    public static final int Max_Size = 400;
    private short iData[][] = new short[10][Max_Size];
    private short qData[][] = new short[10][Max_Size];
    private short viewIdata[] = new short[Max_Size];
    private short viewQdata[] = new short[Max_Size];
    private int dummy, viewstart, previewstart;
    private int cmpPW = 0;
    private int cmpPH1 = 0;
    private int cmpPHstart = 0;
    private int cmpPHrange = 0;


    /**
     *コンストラクタ　holder　取得
     * @param context
     * @param sv
     */
    public Oscillo(Context context, SurfaceView sv) {
        super(context);

        // サーフェスフォルダの取得
        mHolder = sv.getHolder();
        // サーフェイスイベントの通知先の指定（自身のクラス）
        mHolder.addCallback(this);
    }


    /**
     *サーフェス生成時　スレッド開始　初期描画
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");

        mThread = new Thread(this);
        mThread.start();
        //初期描画
        preViewStart();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
    }

    /**
     * サーフェスビュー破棄時　スレッド停止
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        //   while(mThread.isAlive());
        mThread = null;

    }


    /**
     * iデータの設定
     * @param data i_data
     */
    public void putIdata(short[] data) {
        //Log.d(TAG, "putIData");

        if ((head == 10 && tail == 0) || (tail + 1) == head) {
            Log.d(TAG, "que is full");
        } else {

            if (tail >= 9) {
                tail = 0;
            } else {
                tail = tail + 1;
            }
        }
        //    viewstart =1;
        iData[0] = data;
    }


    /**
     * qデータの設定
     * @param data q_data
     */
    public void putQdata(short[] data) {
       // Log.d(TAG, "putQData");

        if ((head == 10 && tail == 0) || (tail + 1) == head) {
            Log.d(TAG, "que is full");
        } else {

            if (tail >= 9) {
                tail = 0;
            } else {
                tail = tail + 1;
            }
        }
        if (previewstart == 0)
                 viewstart = 1;
        qData[0] = data;

    }


    /**
     * iデータの取得
     * @return　iデータ
     */
    public short[] getIdata() {

//       Log.d(TAG, "getData");
 /*
        if(head == tail){
           Log.d(TAG, "queu is empty");
        }else {
            if(head >= 9 ){
              head=0;
            }else {
                head = head + 1;
            }
        }
  */
        return iData[0];
    }


    /**
     * qデータの取得
     * @return　qデータ
     */
    public short[] getQdata() {

//       Log.d(TAG, "getData");
 /*
        if(head == tail){
           Log.d(TAG, "queu is empty");
        }else {
            if(head >= 9 ){
              head=0;
            }else {
                head = head + 1;
            }
        }
  */
        return qData[0];
    }


    /**
     * 傷検出パワー値　設定
     * @param data
     */
    public void setCmpPW(int data) {

        cmpPW = (int) Math.round(data * 0.7874);
        preViewStart();

    }


    /**
     * 傷検出位相中心値　設定
     * @param data
     */
    public void setCmpPH1(int data) {
        cmpPH1 = 360-data;
        cmpPHstart = cmpPH1 - cmpPHrange / 2;
        preViewStart();
    }


    /**
     * 傷検出位相幅　設定
     * @param data
     */
    public void setCmpPH2(int data) {
        cmpPHrange = data;
        cmpPHstart = cmpPH1 -cmpPHrange/2;
        preViewStart();
    }


    /**
     * 傷検出閾値設定時に画面更新
     */
    public void preViewStart() {
       //
        if (viewstart == 0)
            previewstart = 1;
    }

    /**
     *ＩＱデータサーフェスビュー　スレッド
     * mThreadがNULLで動作停止
     * viewstart、previewstartのどちらかが１で描画
     */
    @Override
    public void run() {

        while (mThread != null) {
            if (viewstart == 1 || previewstart == 1) {

                drawIQ(mHolder);
                viewstart = 0;
                previewstart = 0;
            }
        }
    }

    /**
     * ＩＱデータ描画処理
     * @param holder
     */
    public void drawIQ(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
    //        Log.d(TAG, "drawIQ");
        if (canvas != null) {

            Paint set_paint = new Paint();
            //青で塗りつぶす
            canvas.drawColor(Color.BLUE);

            // X軸とY軸を白色で描画
            //dp*1.3312501倍
            set_paint.setColor(Color.WHITE);
            set_paint.setStrokeWidth(2);
            set_paint.setStyle(Paint.Style.STROKE);
            //x0,y0,x1,y1
            canvas.drawLine(5, 205, 405, 205, set_paint);
            canvas.drawLine(205, 5, 205, 405, set_paint);
      //      canvas.drawLine(30, 10, 1030, 10, set_paint);
        //    canvas.drawLine(30, 710, 1030, 710, set_paint);
            //枠を描画
            canvas.drawRect(5, 5, 405, 405, set_paint);

            //傷閾値を描画
            Paint dot_paint = new Paint();
            dot_paint.setStrokeWidth(2);
            dot_paint.setStyle(Paint.Style.STROKE);
            dot_paint.setAntiAlias(true);
            dot_paint.setPathEffect(new DashPathEffect(new float[]{2.0f, 2.0f}, 0));
            set_paint.setColor(Color.BLACK);
            RectF oval1 = new RectF(205-cmpPW, 205-cmpPW, 205+cmpPW, 205+cmpPW);
            canvas.drawArc(oval1, cmpPHstart, cmpPHrange, true, dot_paint);
            canvas.drawArc(oval1, cmpPHstart + 180, cmpPHrange, true, dot_paint);

            //傷閾値の最低値を円で描画
            dot_paint.setStrokeWidth(1);
            canvas.drawCircle(205, 205, 100, dot_paint);


            //波形描画用設定
            dot_paint.setStrokeWidth(2);
            set_paint.setColor(Color.GREEN);

            viewIdata = this.getIdata();
            viewQdata = this.getQdata();
            //        Log.d(TAG, "drawIQ"+Viewdata);

            //閾値のみ設定のときは表示しない
            if (previewstart == 0) {
                for (i = 0; i < Max_Size - 2; i++) {
   //             if(viewIdata[i]!=0)
     //               Log.d(TAG, "drawIQ"+viewIdata[i]);

    //               canvas.drawLine((float) (200 - viewIdata[i] * 1.5), (float)(200 + viewQdata[i] * 1.5), (float)(200 - viewIdata[i + 1] * 1.5),(float)(200 + viewQdata[i + 1] * 1.5), set_paint);
                   canvas.drawLine((205 - viewIdata[i] ), (205 + viewQdata[i] ), 205 - viewIdata[i + 1] ,(205 + viewQdata[i + 1]) , set_paint);
                }

            }
            holder.unlockCanvasAndPost(canvas);
            // 水平軸時間表示


//            Log.d(TAG, "drawIQ END");
        }
    }
};
