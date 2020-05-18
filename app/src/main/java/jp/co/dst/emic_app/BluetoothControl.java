package jp.co.dst.emic_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/*********************************************************************
 *   Bluetoothで接続し送受信を実行するクラス
 *   1500バイト一括受信するバージョン
 *   端末探索はペアされたもののみ　
 *********************************************************************/
public class BluetoothControl {

	// Debugging
	private static final String TAG = "BluetoothControl";

	/**** クラス定数宣言 ********/
	public static final int MESSAGE_STATECHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int Max_Size = 4100;

	public static final int STATE_NONE = 0;
	public static final int STATE_CONNECTING = 1;
	public static final int STATE_CONNECTED = 2;

	// SPPのUUIDをセット
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private BluetoothAdapter BTadapter;
	private Handler handler;
	private int state;
	private ConnectThread connecting;
	private ConnectedThread transfer;
	private boolean btReadThreadGoing =false;

	// コンストラクタによるAdapterのインスタンス生成
	public BluetoothControl(Context context, Handler handler) {
		this.BTadapter = BluetoothAdapter.getDefaultAdapter();
		this.handler = handler;
		state = STATE_NONE;
	}


	/**
	 * Bluetoothの接続実行同期化処理
	 * @param device
	 */
	public synchronized void connect(BluetoothDevice device) {
		if (state == STATE_CONNECTING) {
			if (connecting != null) {				// 既に接続中なら
				connecting.cancel();				// いったんクローズする
				connecting = null;
			}
		}
		if (transfer != null) {						// 既に接続済みなら
			transfer.cancel();						// いったんクローズ
			transfer = null;
		}
		connecting = new ConnectThread(device);		// 接続スレッドを生成
		btReadThreadGoing = true;
		connecting.start();							// スレッド開始
		setState(STATE_CONNECTING);					// 状態返送
	}
	

	/**
	 * Bluetooth接続処理スレッド
	 */
	private class ConnectThread extends Thread {
		private BluetoothDevice BTdevice;
		private BluetoothSocket BTsocket;

		// UUIDでリモート端末との接続用ソケットの生成
		public ConnectThread(BluetoothDevice device) {
			try {
				this.BTdevice = device;
				BTsocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			}
			catch (IOException e) {
				Log.e(TAG,  "create() failed",e);
			}

		}
		// 接続実行処理
		public void run() {
			BTadapter.cancelDiscovery();			// 検索処理終了
			try {
				BTsocket.connect();					// 接続中同期化処理へ
			}
			catch (IOException e) {
				Log.e(TAG,  "connect failed",e);
				setState(STATE_NONE);				// 状態返送
				try {
					BTsocket.close();				// エラーならクローズ
				}
				catch (IOException e2) {
				}
				return;
			}
			synchronized (BluetoothControl.this) {	// 同期を取る
				connecting = null;					// ステート初期化
			}
			connected(BTsocket, BTdevice);			// 通信開始同期化処理へ
		}


		/**
		 *  Blutooth接続を切り離す処理
		 */
		public void cancel() {
			try {
				BTsocket.close();
			}
			catch (IOException e) {
			}
		}
	}

	/**
	 * Bluetooth通信開始同期化処理
	 * @param socket
	 * @param device
	 */
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
		if (connecting != null) {					// 既に接続中なら
			connecting.cancel();					// いったんクローズ
			connecting = null;
		}
		if (transfer != null) {						// 既に接続済みなら
			transfer.cancel();						// いったんクローズ
			transfer = null;
		}
		transfer = new ConnectedThread(socket);		// 送受信スレッドを生成
		transfer.start();							// スレッド開始
		setState(STATE_CONNECTED);					// 状態返送
	}


	/**
	 * Bluetooth受信処理実行スレッド
	 */
	private class ConnectedThread extends Thread {
		private BluetoothSocket BTsocket;

		// Socket初期化処理
		public ConnectedThread(BluetoothSocket bluetoothsocket) {
			this.BTsocket = bluetoothsocket;
		}
		/******** 受信を待ち、バッファからデータ取り出し処理 *********/
		/******** 常に4100バイトを受信するまで繰り返す      **********/
		public void run() {
			byte[][] buf = new byte[100][Max_Size];			// 受信バッファの用意
			byte[] Rcv = new byte[Max_Size];			// 取り出しバッファの用意
			int bytes,  i;						// 受信バイト数他
			byte Index;
			Index = 0;									// バイトカウンタリセット

			/***** 受信繰り返しループ *****/
			while (btReadThreadGoing== true) {
				try {
					InputStream input = BTsocket.getInputStream();
					bytes = input.read(buf[Index]);		// 受信実行

/*

					// 4100バイト受信完了まで受信繰り返し
					while(Index < Max_Size-1){
						InputStream input = BTsocket.getInputStream();
						bytes = input.read(buf);		// 受信実行
						for(i=0; i<bytes-1; i++){			// 受信バイト数だけ繰り返し
							Rcv[Index]=buf[i];			// バッファコピー
							if(Index < Max_Size-1)
								Index++;				// バイトカウンタ更新
						}
					}
					Index = 0;							// バイトカウンタリセット
*/
					/**** 取り出したデータを返す　****/
//					handler.obtainMessage(MESSAGE_READ, Max_Size, -1, Rcv).sendToTarget();
					if(bytes>=300)
						Log.e(TAG,"max"+bytes);

//				Log.e(TAG,"byets"+ bytes);
					handler.obtainMessage(MESSAGE_READ, bytes, -1, buf[Index]).sendToTarget();

					if(Index >= 99){
						Index=0;
					}else {
						Index++;
					}
				}
				catch (IOException e) {
					setState(STATE_NONE);				// 状態返送
					break;
				}
			}
		}
		/******** 送信処理 *******/
		public void write(byte[] buf) {
			try {
				OutputStream output = BTsocket.getOutputStream();	// 送信出力
				output.write(buf);
			}
			catch (IOException e) {
			}
		}
		/***** クローズ処理 ******/
		public void cancel() {
			try {
				BTsocket.close();
			}
			catch (IOException e) {
			}
		}
	}


	/**
	 *  送信実行メソッド
	 * @param out
	 */
	public synchronized void write(byte[] out) {
		ConnectedThread transfer;
		synchronized (this) {						// 同期化と初期化
			if (state != STATE_CONNECTED) {
				return;
			}
			transfer = this.transfer;
		}
	//	Log.e(TAG,  "実行");
		transfer.write(out);						// 送信実行
	}
	

	/**
	 * 状態の通知メソッド
	 * @param state
	 */
	private synchronized void setState(int state) {
		this.state = state;
		handler.obtainMessage(MESSAGE_STATECHANGE, state, -1).sendToTarget();
	}

	/**
	 * 状態の取得メソッド
 	 * @return
	 */
	public synchronized int getState() {
		return state;
	}

	/**
	 * Bluetoothの切断メソッド
	 */
	public synchronized void stop() {
		//接続スレッド終了
		if (null != connecting) {
			connecting.cancel();
			connecting = null;
		}
		//リード処理切断
		btReadThreadGoing=false;
		//送受信スレッド終了
		if (null != transfer) {
			transfer.cancel();
			transfer = null;
		}
		setState(STATE_NONE);					// 状態返送
	}
}
