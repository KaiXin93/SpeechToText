package com.example.qkx.speechtotext;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qkx.speechtotext.model.ResultBean;
import com.example.qkx.speechtotext.rest.RestSource;
import com.example.qkx.speechtotext.utils.FileUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    protected static final int RESULT_SPEECH = 1;

    private static final int MODE_CH = 0;
    private static final int MODE_EN = 1;

    private int currentMode = MODE_CH;

    private RestSource mRestSource;

    @Bind(R.id.tv_rt)
    TextView tvRt;
    @Bind(R.id.tv_rt_en)
    TextView tvRtEn;

    @Bind(R.id.tv_res_syc)
    TextView tvResSyc;
    @Bind(R.id.tv_translation_syc)
    TextView tvTranslationSyc;

    @Bind(R.id.tv_res_transfer)
    TextView tvResTransfer;
    @Bind(R.id.tv_translation_transfer)
    TextView tvTranslationTransfer;

    @Bind(R.id.spinner_name)
    Spinner mSpinnerName;
    @Bind(R.id.spinner_speed)
    Spinner mSpinnerSpeed;
    @Bind(R.id.spinner_volume)
    Spinner mSpinnerVolume;

    @Bind(R.id.spinner_bos)
    Spinner mSpinnerBos;
    @Bind(R.id.spinner_eos)
    Spinner mSpinnerEos;

    @Bind(R.id.edt_test_voice)
    EditText edtTest;

    @Bind(R.id.tv_rt_record_hint)
    TextView tvRtRecordHint;

    @Bind(R.id.tv_syc_record_hint)
    TextView tvSycRecordHint;

    private StringBuffer buffer = new StringBuffer();
    /**
     *
     */
    private SpeechRecognizer mIat;
    private Toast mToast;
    private com.iflytek.cloud.RecognizerListener mRecoListener;
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private SynthesizerListener mSynListener;

    private String[] mVoiceDisplayNames = new String[]{"青年女声(小燕)", "青年男声(小峰)", "中年男声(老孙)", "女声播音员(小筠)"};
    private String[] mVoiceNames = new String[]{"xiaoyan", "xiaofeng", "vils", "aisjying"};
    private String[] mSpeeds = new String[]{"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};
    private String[] mVolumes = new String[]{"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};

    private String mDefaultName = "xiaoyan";
    private String mDefaultSpeed = "50";
    private String mDefaultVolume = "80";

    private String[] mBosTimes = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    private String[] mBosTimeMillis = new String[]{"1000", "2000", "3000", "4000", "5000", "6000", "7000",
            "8000", "9000", "10000"};

    private String[] mEosTimes = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    private String[] mEosTimeMillis = new String[]{"1000", "2000", "3000", "4000", "5000", "6000", "7000",
            "8000", "9000", "10000"};

    private String mDefaultAvdBosMillis = "4000";
    private String mDefaultAvdEosMillis = "1000";

    int ret = 0; //函数返回值

    private String mRtRecordPath = null;
    private String mSycRecordPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        init();
    }

    private void init() {
        SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID + "=" + Constants.APPID);
        mIat = SpeechRecognizer.createRecognizer(MainActivity.this, mInitListener);

        mToast = Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT);

        mRestSource = RestSource.getInstance();

        //合成监听器
        mSynListener = new SynthesizerListener() {
            //会话结束回调接口,没有错误时,error为null
            public void onCompleted(SpeechError error) {
            }

            //缓冲进度回调
            //percent为缓冲进度0~100,beginPos为缓冲音频在文本中开始位置,endPos表示缓冲音频在文本中结束位置,info为附加信息。
            public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            }

            //开始播放
            public void onSpeakBegin() {
            }

            //暂停播放
            public void onSpeakPaused() {
            }

            //播放进度回调
            //percent为播放进度0~100,beginPos为播放音频在文本中开始位置,endPos表示播放音频在文本中结束位置.
            public void onSpeakProgress(int percent, int beginPos, int endPos) {
            }

            //恢复播放回调接口
            public void onSpeakResumed() {
            }

            //会话事件回调接口
            public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
            }
        };

        setupSpinner();

    }

    private void setupSpinner() {
        // 发音人
        ArrayAdapter<String> adapterVoiceName = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mVoiceDisplayNames);
        adapterVoiceName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerName.setAdapter(adapterVoiceName);
        mSpinnerName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDefaultName = mVoiceNames[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 语速
        ArrayAdapter<String> adapterSpeed = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mSpeeds);
        adapterSpeed.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSpeed.setAdapter(adapterSpeed);
        mSpinnerSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDefaultSpeed = mSpeeds[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinnerSpeed.setSelection(4);

        // 音量
        ArrayAdapter<String> adapterVolume = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mVolumes);
        adapterVolume.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerVolume.setAdapter(adapterVolume);
        mSpinnerVolume.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDefaultVolume = mVolumes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinnerVolume.setSelection(7);

        // 静音超时
        ArrayAdapter<String> adapterBos = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mBosTimes);
        adapterBos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerBos.setAdapter(adapterBos);
        mSpinnerBos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDefaultAvdBosMillis = mBosTimeMillis[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinnerBos.setSelection(3);

        // 说话超时
        ArrayAdapter<String> adapterEos = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mEosTimes);
        adapterEos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerEos.setAdapter(adapterEos);
        mSpinnerEos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDefaultAvdEosMillis = mEosTimeMillis[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinnerEos.setSelection(0);

    }

    @OnClick(R.id.btn_syc_start_record)
    void startSycRecord() {
        String fileDir = Environment.getExternalStorageDirectory().getPath() + "/record/同声翻译";
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String date = FileUtil.getCurrentDate();
        mSycRecordPath = fileDir + "/" + date + ".txt";
        String header = date + "\n\n ";
        FileUtil.addStringToFile(header, mSycRecordPath);
        tvSycRecordHint.setText(String.format("开始记录，记录保存至%s", mSycRecordPath));
    }

    @OnClick(R.id.btn_syc_stop_record)
    void stopSycRecord() {
        if (mSycRecordPath != null) {
            tvSycRecordHint.setText(String.format("停止记录，记录保存至%s", mSycRecordPath));
            mSycRecordPath = null;
        }
    }

    @OnClick(R.id.btn_rt_start_record)
    void startRtRecord() {
        String fileDir = Environment.getExternalStorageDirectory().getPath() + "/record/实时翻译";
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String date = FileUtil.getCurrentDate();
        mRtRecordPath = fileDir + "/" + date + ".txt";
        String header = date + "\n\n";
        FileUtil.addStringToFile(header, mRtRecordPath);
        tvRtRecordHint.setText(String.format("开始记录，记录保存至%s", mRtRecordPath));
    }

    @OnClick(R.id.btn_rt_stop_record)
    void stopRtRecord() {
        if (mRtRecordPath != null) {
            tvRtRecordHint.setText(String.format("停止记录，记录保存至%s", mRtRecordPath));
            mRtRecordPath = null;
        }
    }

    @OnClick(R.id.btn_orc)
    void startOrc() {
        Intent intent = new Intent(MainActivity.this, ORCActivity.class);
        startActivity(intent);
    }


    @OnClick(R.id.btn_test_voice)
    void testVoice() {
//        String str = "你好";
        String str = edtTest.getText().toString();
        if (str.length() == 0) return;

        speak(str, mDefaultName, mDefaultSpeed, mDefaultVolume);
    }

    private void speak(String str) {
        speak(str, "xiaoyan", "50", "80");
    }

    private void speak(String str, String voiceName, String speed, String volume) {
        //1.创建 SpeechSynthesizer 对象, 第二个参数:本地合成时传 InitListener
        SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer(this, null);
        //2.合成参数设置,详见《MSC Reference Manual》SpeechSynthesizer 类
        //设置发音人(更多在线发音人,用户可参见 附录13.2
        mTts.setParameter(SpeechConstant.VOICE_NAME, voiceName); //设置发音人
        mTts.setParameter(SpeechConstant.SPEED, speed);//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, volume);//设置音量,范围 0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        //设置合成音频保存位置(可自定义保存位置),保存在“./sdcard/iflytek.pcm”
        //保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
        // 仅支持保存为 pcm 和 wav 格式,如果不需要保存合成音频,注释该行代码
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
        //3.开始合成
//        mTts.startSpeaking("科大讯飞,让世界聆听我们的声音", mSynListener);
        mTts.startSpeaking(str, mSynListener);
    }

    private void translateEn(String q, TranslateCallback callback) {
//        String q = tvText.getText().toString();
//        String q = "这是一段语音";
        mRestSource.queryEn(q, callback);
    }

    private void translateCh(String q, TranslateCallback callback) {
        mRestSource.queryCh(q, callback);
    }

    @OnClick(R.id.btn_rt)
    void rtTrans() {
        speechToTextCh(new RetCallback() {
            @Override
            public void onProcessResult(String result) {
                tvRt.setText(result);

                translateEn(result, new TranslateCallback() {
                    @Override
                    public void onProcessResult(ResultBean resultBean) {
                        String origin = tvRt.getText().toString();
                        String res = resultBean.trans_result.get(0).dst;
                        String data = origin + '\n' + res;

                        tvRt.setText(data);

                        speak(res);

                        // 保存记录
                        if (mRtRecordPath != null) {
                            FileUtil.addStringToFile(data + "\n\n", mRtRecordPath);
                        }
                    }
                });

            }
        });
    }

    @OnClick(R.id.btn_rt_en)
    void rtTransEn() {
        speechToTextEn(new RetCallback() {
            @Override
            public void onProcessResult(String result) {
                tvRtEn.setText(result);

                translateCh(result, new TranslateCallback() {
                    @Override
                    public void onProcessResult(ResultBean resultBean) {
                        String origin = tvRtEn.getText().toString();
                        String res = resultBean.trans_result.get(0).dst;
                        String data = origin + '\n' + res;

                        tvRtEn.setText(data);

                        speak(res);

                        // 保存记录
                        if (mRtRecordPath != null) {
                            FileUtil.addStringToFile(data + "\n\n", mRtRecordPath);
                        }
                    }
                });
            }
        });
    }

    @OnClick(R.id.btn_speak_syc)
    void speakSyc() {
        tvResSyc.setText("");
        tvTranslationSyc.setText("");
        speechToTextSyc(new RetCallback() {
            @Override
            public void onProcessResult(String result) {
                String origin = tvResSyc.getText().toString();
                tvResSyc.setText(origin + result);

                mRestSource.queryEn(result, new TranslateCallback() {
                    @Override
                    public void onProcessResult(ResultBean resultBean) {
                        if (resultBean.trans_result == null ||
                                resultBean.trans_result.size() == 0) return;

                        ResultBean.TransResult transResult = resultBean.trans_result.get(0);
                        String src = transResult.src;
                        String dst = transResult.dst;

                        String origin = tvTranslationSyc.getText().toString();
                        tvTranslationSyc.setText(origin + dst);

                        if (mSycRecordPath != null) {
                            src = src.replace('，', ' ');
                            dst = dst.replace(',', ' ');
                            FileUtil.addStringToFile(String.format("%s\n%s\n\n", src, dst),
                                    mSycRecordPath);
                        }

                    }
                });

            }
        }, "zh_cn");
    }

    @OnClick(R.id.btn_speak_transfer)
    void speakTransfer() {
        tvResTransfer.setText("");
        tvTranslationTransfer.setText("");

        switch (currentMode) {
            case MODE_CH:
                Toast.makeText(this, "请说中文!", Toast.LENGTH_LONG).show();
                currentMode = MODE_EN;
                speechToTextSyc(new RetCallback() {
                    @Override
                    public void onProcessResult(String result) {
                        String origin = tvResTransfer.getText().toString();
                        tvResTransfer.setText(origin + result);

                        mRestSource.queryEn(result, new TranslateCallback() {
                            @Override
                            public void onProcessResult(ResultBean resultBean) {
                                if (resultBean.trans_result == null ||
                                        resultBean.trans_result.size() == 0) return;

                                String or = tvTranslationTransfer.getText().toString();
                                ResultBean.TransResult transResult = resultBean.trans_result.get(0);
                                tvTranslationTransfer.setText(or + transResult.dst);
                            }
                        });
                    }
                }, "zh_cn");
                break;
            case MODE_EN:
                Toast.makeText(this, "请说英文!", Toast.LENGTH_LONG).show();
                currentMode = MODE_CH;
                speechToTextSyc(new RetCallback() {
                    @Override
                    public void onProcessResult(String result) {
                        String origin = tvResTransfer.getText().toString();
                        tvResTransfer.setText(origin + result);

                        mRestSource.queryCh(result, new TranslateCallback() {
                            @Override
                            public void onProcessResult(ResultBean resultBean) {
                                if (resultBean.trans_result == null ||
                                        resultBean.trans_result.size() == 0) return;

                                String or = tvTranslationTransfer.getText().toString();
                                ResultBean.TransResult transResult = resultBean.trans_result.get(0);
                                tvTranslationTransfer.setText(or + transResult.dst);
                            }
                        });
                    }
                }, "en_us");
                break;
        }
    }

    private void stopListening() {
        if (mIat == null) return;

        mIat.stopListening();
    }

    private void speechToTextEn(RetCallback callback) {
        speechToText(callback, "en_us");
    }

    private void speechToTextCh(RetCallback callback) {
        speechToText(callback, "zh_cn");
    }

    private void speechToText(final RetCallback callback, String language) {
//        speechToText(callback, language, "4000", "1000");
        speechToText(callback, language, mDefaultAvdBosMillis, mDefaultAvdEosMillis);
    }

    /**
     * 识别结束后整段文本处理
     *
     * @param callback     结果回调接口
     * @param language     语言种类
     * @param avdBosMillis 说话未开始时的超时时间
     * @param avdEosMillis 说话停止后的超时时间
     */
    private void speechToText(final RetCallback callback, String language, String avdBosMillis, String avdEosMillis) {
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, language);
        if (language.equals("zh_cn")) {
            mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        }
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, avdBosMillis);

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, avdEosMillis);

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
//                Log.d(TAG, "speech result >> " + json);
                String ret = parseJson(json);

                Log.d(TAG, "outcome >> " + ret);

                buffer.append(ret);
                if (isLast) {
                    callback.onProcessResult(buffer.toString());
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

    /**
     * 分段文本处理
     *
     * @param callback 结果回调接口
     * @param language 语言种类
     */
    private void speechToTextSyc(final RetCallback callback, String language) {
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, language);
        if (language.equals("zh_cn")) {
            mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        }
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "5000");

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
                String ret = parseJson(json);

                Log.d(TAG, "res >> " + ret);
                callback.onProcessResult(ret);
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
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    interface RetCallback {
        void onProcessResult(String result);
    }

    public interface TranslateCallback {
        void onProcessResult(ResultBean resultBean);
    }
}
