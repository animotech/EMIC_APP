package jp.co.dst.emic_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 *周波数フラグメントに表示する画面ビュー
 */
public class FreqSweepView extends SurfaceView implements SurfaceHolder.Callback,Runnable {

    private static final String TAG = "FreqSweepView";
    private Thread freqviewThread = null;
    private boolean freqviewStart;
    private int pw1viewData[]=new int[62];
    private int pw2viewData[]=new int[62];
    private int sweepNum=0;
    private SurfaceHolder freqSurfaceHolder;


    /**
     * コンストラクタ　スレッドスタート
     * @param context
     * @param sv
     */
    public FreqSweepView(Context context, SurfaceView sv) {
        super(context);

        // サーフェイスフォルダの取得
        freqSurfaceHolder = sv.getHolder();
        // サーフェイスイベントの通知先の指定（自身のクラス）
        freqSurfaceHolder.addCallback(this);
        freqviewStart=true;

        if(freqviewThread == null) {
            freqviewThread = new Thread(this);
            freqviewThread.start();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        //   while(mThread.isAlive());
        freqviewStart=false;
        freqviewThread=null;


    }

    /**
     *サーフェスビュー　runスレッド
     * freqviewStartがtrueとなった時に描画を実行
     * freqviewThreadがnullで終了
     */
    @Override
    public void run() {
        while (freqviewThread != null) {
            if(freqviewStart==true) {
                freqdraw();
                freqviewStart = false;
            }
        }
    }

    /**
     * 周波数掃引描画処理　受け取ったカウントまで表示
     */
    void freqdraw(){
        Canvas canvas = freqSurfaceHolder.lockCanvas();
        Log.d(TAG, "freqdraw");
        int i;
        if (canvas != null) {

            Paint set_paint = new Paint();
           //黒で塗りつぶす
            canvas.drawColor(Color.BLACK);
            // 座標の表示 青色で表示
            set_paint.setColor(Color.BLUE);
            set_paint.setStrokeWidth(1);
           // 縦軸の表示　6本
            for (i = 0; i <= 6; i++)
                canvas.drawLine(30 + i * 100, 30, 30 + i * 100, 630, set_paint);
            // 横軸の表示　6本　
            for (i = 0; i < 6; i++)
                canvas.drawLine(30, 30 + i * 100, 630, 30 + i * 100, set_paint);
            // X軸とY軸の0ライン白色で描画
            //dp*1.3312501倍

           //枠の表示
            set_paint.setColor(Color.WHITE);
            set_paint.setStrokeWidth(2);

            canvas.drawLine(30, 30, 30, 630, set_paint);
            canvas.drawLine(630, 30, 630, 630, set_paint);
            canvas.drawLine(30, 30, 630, 30 ,set_paint);
            canvas.drawLine(30, 630, 630, 630, set_paint);
            // 軸目盛の表示
            set_paint.setAntiAlias(true);
            set_paint.setTextSize(20f);
            for(i=0; i<=10; i++)
                canvas.drawText(Integer.toString(212 + i * 100), 18 + i * 100, 645, set_paint);


       //     for(i=-3; i<=3; i++)
        //        canvas.drawText(Integer.toString(i), 8, 368-i*100, set_paint);
            //PW1の表示
           // set_paint.setColor(Color.YELLOW);
            //オレンジに変更
            set_paint.setColor(Color.parseColor("#FFA07A"));
            for(i=0; i<=sweepNum-1; i++)
                canvas.drawLine(10 * i + 30, 630 - pw1viewData[i], 10 * (i + 1) + 30,  630 - pw1viewData[i+1  ], set_paint);
            //PW2の表示
            set_paint.setColor(Color.GREEN);
            for(i=0; i<=sweepNum-1;i++)
                canvas.drawLine(10 * i + 30, 630 - pw2viewData[i], 10 * (i + 1) + 30,  630 - pw2viewData[i+1 ], set_paint);

            //     for(i=-3; i<=3; i++)
            //        canvas.drawText(Integer.toString(i), 8, 368-i*100, set_paint);
            //描画
            freqSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * 画面に表示するデータをセット
     * @param pw1　PW1受信データ
     * @param pw2　PW2受信データ　
     * @param num　受け取ったカウント（この値まで表示する)
     */
    void sweepDataSet(int pw1,int pw2 ,int num){
        pw1viewData[num]=Math.round(pw1*600/65536);
        pw2viewData[num]=Math.round(pw2*600/65536);
        //System.arraycopy(pw1, 0, pw1viewData, 0, num);
        //numSystem.arraycopy(pw2, 0, pw2viewData, 0, num);
        sweepNum = num ;
        freqviewStart = true;
    }

}







