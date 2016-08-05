package com.example.qkx.speechtotext;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qkx.speechtotext.utils.ImageUtil;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

/**
 * Created by qkx on 16/7/25.
 */
public class ORCActivity extends AppCompatActivity {

    private static final String TAG = ORCActivity.class.getSimpleName();

    private static final int WHAT_ORC = 0;
    private static final int WHAT_RESOURCE = 1;

    private String SD_PATH;

    private Bitmap bitmap;

    private Button btnPick;
    private Button btnOrc;
    private ImageView imageView;
    private TextView tvResult;

    private MyHandler handler;

    private TessBaseAPI tessBaseAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orc);

        init();
    }

    private void init() {
        SD_PATH = Environment.getExternalStorageDirectory().getPath();
//        SD_PATH = Environment.get
        Log.d(TAG, "SD ---> " + SD_PATH);

        btnPick = (Button) findViewById(R.id.btn_pick);
        btnOrc = (Button) findViewById(R.id.btn_orc);
        imageView = (ImageView) findViewById(R.id.iv_pic);
        tvResult = (TextView) findViewById(R.id.tv_result);

        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhoto();
            }
        });

        btnOrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orcTest();
            }
        });

//        initTest();

        handler = new MyHandler(this);

        initOrcResource();
    }

    private void initOrcResource() {
        if (!checkResourceExist()) {
            Toast.makeText(this, "初始化资源文件......", Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream in = null;
                    FileOutputStream fos = null;
                    try {
                        in = ORCActivity.this.getAssets().open("eng.traineddata");

//                        String path = SD_PATH + "/tessdata/eng.traineddata";
                        String dirPath = SD_PATH + "/tessdata";
                        File dir = new File(dirPath);
                        dir.mkdir();

                        File file = new File(dir, "eng.traineddata");
                        fos = new FileOutputStream(file);
                        byte[] bytes = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(bytes)) > 0) {
                            fos.write(bytes, 0, bytesRead);
                        }
                        Log.i(TAG, file.getPath() + " : copy finish!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

//                    tessBaseAPI = new TessBaseAPI();
//                    tessBaseAPI.init(SD_PATH, "eng");
//                    tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
                    initTessBaseAPI();

                    handler.sendEmptyMessage(WHAT_RESOURCE);
                }
            }).start();
        } else {
            initTessBaseAPI();
        }
    }

    private void initTessBaseAPI() {
        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(SD_PATH, "eng");
        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
    }

    private boolean checkResourceExist() {
        String path = SD_PATH + "/tessdata/eng.traineddata";
        File file = new File(path);
        if (file.exists()) {
            Log.i(TAG, "eng.traineddata exist!");
            return true;
        } else {
            Log.i(TAG, "eng.traineddata do not exist!");
            return false;
        }
    }

//    private Button btnTest;
//
//    private void initTest() {
//        btnTest = (Button) findViewById(R.id.btn_test);
//        btnTest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    String[] files = ORCActivity.this.getAssets().list("");
//                    for (String s : files) {
//                        Log.i(TAG, "path ---> " + s);
//                    }
//
//                    InputStream in = ORCActivity.this.getAssets().open("test.txt");
//                    if (in != null) {
//                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//                        String line;
//                        while ((line = reader.readLine()) != null) {
//                            Log.i(TAG, "res ---> " + line);
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    private boolean isRunning = false;

    private void orcTest() {
        if (bitmap != null && !isRunning) {
            Toast.makeText(this, "开始识别!", Toast.LENGTH_SHORT).show();
            isRunning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    String SD_PATH = Environment.getExternalStorageDirectory().getPath();
//                    Log.d(TAG, "SD ---> " + SD_PATH);

//                    TessBaseAPI tessBaseAPI = new TessBaseAPI();

//                    tessBaseAPI.init(SD_PATH, "eng");
//                    tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);


                    tessBaseAPI.setImage(bitmap);

                    String res = tessBaseAPI.getUTF8Text();
                    tessBaseAPI.clear();

                    Message msg = handler.obtainMessage();
                    msg.what = WHAT_ORC;
                    msg.obj = res;
                    handler.sendMessage(msg);
                }
            }).start();


//            res.replaceAll("/n", " ");
//            tvResult.setText(res);
//            bitmap.recycle();
        }
    }

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Constants.PHOTO_REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        Uri uri = null;
        switch (requestCode) {
            case Constants.PHOTO_REQUEST_GALLERY:
                uri = data.getData();
                Log.d(TAG, "uri ---> " + uri.toString());
                try {
                    bitmap = ImageUtil.decodeBitmapByRatioSize(this, 800, 800, uri);
                    imageView.setImageBitmap(bitmap);

//                    String SD_PATH = Environment.getExternalStorageState();
//
//                    TessBaseAPI tessBaseAPI = new TessBaseAPI();
//
//                    tessBaseAPI.init(SD_PATH, "eng");
//                    tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
//                    tessBaseAPI.setImage(bitmap);
//
//                    String res = tessBaseAPI.getUTF8Text();
//                    tessBaseAPI.clear();
//
//                    tvResult.setText(res);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    static class MyHandler extends Handler {
        WeakReference<ORCActivity> weakReference;

        public MyHandler(ORCActivity orcActivity) {
            super();
            weakReference = new WeakReference<ORCActivity>(orcActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_ORC:
                    String res = (String) msg.obj;
                    ORCActivity orcActivity = weakReference.get();
                    if (orcActivity != null) {
                        orcActivity.tvResult.setText("识别结果:\n" + res);
                        Toast.makeText(orcActivity, "识别完成!", Toast.LENGTH_SHORT).show();
                        orcActivity.isRunning = false;
                    }
                    break;
                case WHAT_RESOURCE:
                    Toast.makeText(weakReference.get(), "加载完成!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
