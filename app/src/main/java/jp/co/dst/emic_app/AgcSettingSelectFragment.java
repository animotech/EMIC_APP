package jp.co.dst.emic_app;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AgcSettingSelectFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AgcSettingSelectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AgcSettingSelectFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "AgcSettingSelectFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public static String[] calNumStr = {
            "CAL DATA 1",
            "CAL DATA 2",
            "CAL DATA 3",
            "CAL DATA 4",
            "CAL DATA 5",
            "CAL DATA 6",
    };
    public static String[] sampleTypeStr = {
            "Fe",
            "Al",

    };
    private OnFragmentInteractionListener mListener;
    private EMIC_Activity mainActivty;
    private SQLiteDatabase sdb;
    private Spinner dataSelSP;
    private String calSelNUM= "CAL DATA 1";
    private Spinner sampleTypeSp;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AgcSettingSelectFragment.
     */

    /**
     * 空のコンストラクタ
     */
    public AgcSettingSelectFragment() {
        // Required empty public constructor
    }

    /**
     * 生成時呼び出し関数
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

    //特に使用してない
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mainActivty =(EMIC_Activity) getActivity();

    }

    /**
     * 生成時描画処理
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_agc_setting_select, container, false);

        //ボタン
        Button retBt = (Button) v.findViewById(R.id.agcSetSelRetBT);
        retBt.setOnClickListener(this);
        Button C1Bt = (Button) v.findViewById(R.id.cal1Bt);
        C1Bt.setOnClickListener(this);
        Button C2Bt = (Button) v.findViewById(R.id.cal2Bt);
        C2Bt.setOnClickListener(this);
        Button C3Bt = (Button) v.findViewById(R.id.cal3Bt);
        C3Bt.setOnClickListener(this);
        Button C4Bt = (Button) v.findViewById(R.id.cal4Bt);
        C4Bt.setOnClickListener(this);
        Button C5Bt = (Button) v.findViewById(R.id.cal5Bt);
        C5Bt.setOnClickListener(this);
        Button C6Bt = (Button) v.findViewById(R.id.cal6Bt);
        C6Bt.setOnClickListener(this);

        //テキストビュー
        TextView date1Text=(TextView) v.findViewById(R.id.cal1DateText);
        TextView date2Text=(TextView) v.findViewById(R.id.cal2DateText);
        TextView date3Text=(TextView) v.findViewById(R.id.cal3DateText);
        TextView date4Text=(TextView) v.findViewById(R.id.cal4DateText);
        TextView date5Text=(TextView) v.findViewById(R.id.cal5DateText);
        TextView date6Text=(TextView) v.findViewById(R.id.cal6DateText);

        //データベースにデータがあれば時間を表示
        agcSQLiteOpenHelper ahlpr= new agcSQLiteOpenHelper(getActivity());
        sdb = ahlpr.getWritableDatabase();
        Cursor cursor=sdb.query(ahlpr.DB_TABLE, null, ahlpr.Key_NUM + "='" + calNumStr[0] + "'", null, null, null, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            date1Text.setText(cursor.getString(cursor.getColumnIndex(ahlpr.Key_DATE)));
        }

        cursor=sdb.query(ahlpr.DB_TABLE, null, ahlpr.Key_NUM +"='" + calNumStr[1]+ "'", null, null, null, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            date2Text.setText(cursor.getString(cursor.getColumnIndex(ahlpr.Key_DATE)));
        }

        cursor=sdb.query(ahlpr.DB_TABLE, null, ahlpr.Key_NUM + "='" + calNumStr[2]+ "'", null, null, null, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            date3Text.setText(cursor.getString(cursor.getColumnIndex(ahlpr.Key_DATE)));
        }

        cursor=sdb.query(ahlpr.DB_TABLE, null, ahlpr.Key_NUM + "='" +  calNumStr[3]+ "'", null, null, null, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            date4Text.setText(cursor.getString(cursor.getColumnIndex(ahlpr.Key_DATE)));
        }

        cursor=sdb.query(ahlpr.DB_TABLE, null, ahlpr.Key_NUM + "='" +  calNumStr[4]+ "'", null, null, null, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            date5Text.setText(cursor.getString(cursor.getColumnIndex(ahlpr.Key_DATE)));
        }

        cursor=sdb.query(ahlpr.DB_TABLE, null, ahlpr.Key_NUM + "='" +  calNumStr[5]+ "'", null, null, null, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            date6Text.setText(cursor.getString(cursor.getColumnIndex(ahlpr.Key_DATE)));
        }

        //校正データを選択するスピナーをセット
        dataSelSP = (Spinner)v.findViewById(R.id.calDataSpinner);
        // ArrayAdapter
        ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, calNumStr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // spinner に adapter をセット
        dataSelSP.setAdapter(adapter);
        dataSelSP.setOnItemSelectedListener(this);
/*
        // ArrayAdapter

        //サンプル（Ｆe、Al)などデータを選択するスピナーをセット
        ArrayAdapter<String> sampleadapter
                = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, sampleTypeStr);
        sampleadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // spinner に adapter をセット
        Spinner sampleType1Sp = (Spinner)v.findViewById(R.id.cal1Spinner);
        sampleType1Sp.setAdapter(sampleadapter);
        sampleType1Sp.setOnItemSelectedListener(this);
        Spinner sampleType2Sp = (Spinner)v.findViewById(R.id.cal2Spinner);
        sampleType2Sp.setAdapter(sampleadapter);
        sampleType2Sp.setOnItemSelectedListener(this);
        Spinner sampleType3Sp = (Spinner)v.findViewById(R.id.cal3Spinner);
        sampleType3Sp.setAdapter(sampleadapter);
        sampleType3Sp.setOnItemSelectedListener(this);

        Spinner sampleType4Sp = (Spinner)v.findViewById(R.id.cal4Spinner);
        sampleType4Sp.setAdapter(sampleadapter);
        sampleType4Sp.setOnItemSelectedListener(this);
        Spinner sampleType5Sp = (Spinner)v.findViewById(R.id.cal5Spinner);
        sampleType5Sp.setAdapter(sampleadapter);
        sampleType5Sp.setOnItemSelectedListener(this);
        Spinner sampleType6Sp = (Spinner)v.findViewById(R.id.cal6Spinner);
        sampleType6Sp.setAdapter(sampleadapter);
        sampleType6Sp.setOnItemSelectedListener(this);
  */

        SharedPreferences sp = mainActivty.getSharedPreferences("agcSettingSel", mainActivty.MODE_PRIVATE);

        int spinnerPosition = adapter.getPosition(sp.getString("AgcCalNum", AgcSettingSelectFragment.calNumStr[0]));
        dataSelSP.setSelection(spinnerPosition);

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.agcSetSelRetBT:
                mainActivty.displayBackStack();
                break;
            case R.id.cal1Bt:
                mainActivty.displayAgcSetting("CAL DATA 1");
                break;
            case R.id.cal2Bt:
                mainActivty.displayAgcSetting("CAL DATA 2");
            break;
            case R.id.cal3Bt:
                mainActivty.displayAgcSetting("CAL DATA 3");
                break;
            case R.id.cal4Bt:
                mainActivty.displayAgcSetting("CAL DATA 4");
                break;
            case R.id.cal5Bt:
                mainActivty.displayAgcSetting("CAL DATA 5");
                break;
            case R.id.cal6Bt:
                mainActivty.displayAgcSetting("CAL DATA 6");
                break;

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        Spinner spinner = (Spinner) parent;
        SharedPreferences sp = mainActivty.getSharedPreferences("agcSettingSel", mainActivty.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String tempStr = (String) spinner.getSelectedItem();
        switch (parent.getId()) {
            case R.id.calDataSpinner:


            editor.putString("AgcCalNum", tempStr);
            Log.e(TAG, "AgcCalNum = " + tempStr);
            // 書き込みの確定（実際にファイルに書き込む）

                break;
  /*
            case R.id.cal1Spinner:
                editor.putString( calNumStr[0]+ "TYPE", tempStr);
                break;
            case R.id.cal2Spinner:
                editor.putString(calNumStr[1]+ "TYPE", tempStr);
            case R.id.cal3Spinner:
                editor.putString(calNumStr[2]+ "TYPE", tempStr);
            case R.id.cal4Spinner:
                editor.putString(calNumStr[3]+ "TYPE", tempStr);
            case R.id.cal5Spinner:
                editor.putString(calNumStr[4]+ "TYPE", tempStr);
            case R.id.cal6Spinner:
                editor.putString(calNumStr[5]+ "TYPE", tempStr);
                break;
*/

        }
        editor.commit();
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Spinner spinner = (Spinner) parent;
        String item = (String) spinner.getSelectedItem();

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
        public void onFragmentInteraction(Uri uri);
    }

}
