package com.example.qkx.speechtotext;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qkx.speechtotext.model.ResultBean;
import com.example.qkx.speechtotext.rest.RestSource;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    protected static final int RESULT_SPEECH = 1;

    private RestSource mRestSource;

    private TextView tvText;
    private Button btnTalk;

    private TextView tvTranslation;
    private Button btnTranslate;

    private Button btnSpeak;

    private StringBuffer buffer = new StringBuffer();
    /**
     *
     */
    private SpeechRecognizer mIat;
    private Toast mToast;
    private com.iflytek.cloud.RecognizerListener mRecoListener;
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID + "=" + Constants.APPID);
        mIat = SpeechRecognizer.createRecognizer(MainActivity.this, mInitListener);

        tvText = (TextView) findViewById(R.id.tv_text);
        btnTalk = (Button) findViewById(R.id.btn_talk);
        tvTranslation = (TextView) findViewById(R.id.tv_translation);
        btnTranslate = (Button) findViewById(R.id.btn_translate);

        btnTalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechToText();
            }
        });
        btnTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translate();
            }
        });

        mToast = Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT);

        mRestSource = RestSource.getInstance();
    }

    private void translate() {
        String q = tvText.getText().toString();
//        String q = "这是一段语音";
        mRestSource.query(q);
    }

    @Subscribe
    public void onEventMainThread(ResultBean bean) {
        System.out.println("onMainThread!");
        String res = bean.trans_result.get(0).dst;
        tvTranslation.setText(res);
    }

    int ret = 0; //函数返回值

    private void speechToText() {
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");


        ret = mIat.startListening(new com.iflytek.cloud.RecognizerListener() {
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {

            }

            @Override
            public void onBeginOfSpeech() {
                showTip("speech start!");

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onResult(com.iflytek.cloud.RecognizerResult recognizerResult, boolean isLast) {
                String json = recognizerResult.getResultString();
                Log.d(TAG, "speech result is " + json);
                String ret = parseJson(json);

                Log.d(TAG, "outcome is " + ret);

                buffer.append(ret);
                if (isLast) {
                    tvText.setText(buffer.toString());
                    buffer.delete(0, buffer.length());
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                String dep = speechError.getPlainDescription(true);
                Log.d(TAG, "speech error is " + dep);
                showTip(dep);
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });
        if (ret != ErrorCode.SUCCESS) {
            showTip("听写失败,错误码：" + ret);
        } else {
            showTip("请开始说话");
        }


    }

    private String parseJson(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject object = items.getJSONObject(0);
                ret.append(object.getString("w"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret.toString();
    }

    private void showTip(String str) {
        mToast.setText(str);
        mToast.show();
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        BusManager.getUiBus().register(this);
        BusManager.getDeufaultBus().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BusManager.getUiBus().unregister(this);
        BusManager.getDeufaultBus().unregister(this);
    }
}
