package com.iosdevlog.a221collegenoticeboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iosdevlog on 2016/10/22.
 */


public class CNB_Login extends Activity {

    Spinner Spinn_Frame;
    EditText Edit_Frame_Pwd;
    Button But_Go;

    List<String> List_Frame, List_Frame_Pwd, List_cnbid;

    String Str_Frame_Pwd, Str_Alert_Msg, Str_Alert_Title;
    int Int_Frame_Pos;

    InputStream is = null;
    String result = null;
    String line = null;

    String IP_Address;

    private SharedPreferences prefs;
    private String prefName = "report";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cnb_login);

        prefs = getSharedPreferences(prefName, MODE_PRIVATE);
        int Conn_Check = prefs.getInt("connection", 100);

        if (Conn_Check == 0) {
            AlertDialog.Builder Alert_Conn_Error = new AlertDialog.Builder(CNB_Login.this);
            Alert_Conn_Error.setMessage("Check your Internet Connection..");
            Alert_Conn_Error.setTitle("Connection Error");
            Alert_Conn_Error.setPositiveButton("Ok", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            Alert_Conn_Error.show();
        } else {
            initialise_variables();
        }
    }

    public void adapter() {
        ArrayAdapter<String> adp = new ArrayAdapter<>
                (this, android.R.layout.simple_expandable_list_item_1, List_Frame);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinn_Frame.setAdapter(adp);
        Int_Frame_Pos = 0;
        Spinn_Frame.setSelection(Int_Frame_Pos);
        Str_Frame_Pwd = List_Frame_Pwd.get(Int_Frame_Pos).toString();

        Spinn_Frame.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                Int_Frame_Pos = arg2;
                Str_Frame_Pwd = List_Frame_Pwd.get(Int_Frame_Pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        But_Go.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Edit_Frame_Pwd.getText().toString().equalsIgnoreCase("")) {

                    Str_Alert_Msg = "Enter Password !!!";
                    Str_Alert_Title = "Error";
                    alert_method();
                } else {

                    if (Edit_Frame_Pwd.getText().toString().equals(Str_Frame_Pwd)) {

                        if (Int_Frame_Pos == 0) {

                            save();
                        }
                        if (Int_Frame_Pos == 1) {

                            save();
                        }
                        if (Int_Frame_Pos == 2) {

                            save();
                        }
                        if (Int_Frame_Pos == 3) {

                            save();
                        }
                    } else {
                        Str_Alert_Msg = "Wrong Password !!!";
                        Str_Alert_Title = "Error";
                        alert_method();
                    }
                }
            }

            private void save() {
                prefs = getSharedPreferences(prefName, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                //---save the values in the EditText view to preferences---
                editor.putInt("cnb_id",
                        Integer.parseInt(List_cnbid.get(Int_Frame_Pos).toString()));

                //---saves the values---
                editor.commit();

                finish();

                Intent i = new Intent(CNB_Login.this, User_Login.class);
                startActivity(i);
                Edit_Frame_Pwd.setText(null);
            }
        });
    }

    public void initialise_variables() {

        Spinn_Frame = (Spinner) findViewById(R.id.spinner1);
        Edit_Frame_Pwd = (EditText) findViewById(R.id.editText1);
        But_Go = (Button) findViewById(R.id.button1);

        List_Frame = new ArrayList<>();
        List_Frame_Pwd = new ArrayList<>();
        List_cnbid = new ArrayList<>();

        prefs = getSharedPreferences(prefName, MODE_PRIVATE);
        IP_Address = prefs.getString("ip", "http://10.0.0.21/cnb/");

        new Thread(runnable).start();
    }


    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            DB_CNB_Frame();
        }
    };

    public void DB_CNB_Frame() {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(IP_Address + "DB_CNB_Frame.php");
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            Log.e("DB_CNB_Frame", e.toString());
        }

        try {
            BufferedReader reader = new BufferedReader
                    (new InputStreamReader(is, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
        } catch (Exception e) {
            Log.e("DB_CNB_Frame", e.toString());
        }

        try {
            JSONArray jarray = new JSONArray(result);
            JSONObject jobj = null;

            for (int i = 0; i < jarray.length(); i++) {

                jobj = jarray.getJSONObject(i);

                List_Frame.add(jobj.getString("cnb_name"));
                List_Frame_Pwd.add(jobj.getString("cnb_pwd"));
                List_cnbid.add(String.valueOf(jobj.getInt("cnb_id")));
            }

            prefs = getSharedPreferences(prefName, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("ip", IP_Address);

            editor.commit();

            handler.sendEmptyMessage(1);
        } catch (Exception e) {
            Log.e("DB_CNB_Frame", e.toString());
        }
    }

    //定义Handler对象
    private Handler handler = new Handler() {
        @Override
        //当有消息发送出来的时候就执行Handler的这个方法
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //处理UI
            switch (msg.what) {
                case 1:
                    adapter();
                    break;
                default:
                    break;
            }
        }
    };

    public void alert_method() {

        AlertDialog.Builder alert = new AlertDialog.Builder(CNB_Login.this);
        alert.setMessage(Str_Alert_Msg);
        alert.setTitle(Str_Alert_Title);
        alert.setPositiveButton("OK", null);
        alert.show();
        Edit_Frame_Pwd.setText(null);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}