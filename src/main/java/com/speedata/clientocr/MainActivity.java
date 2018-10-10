package com.speedata.clientocr;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Vibrator;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lpr.ILPR;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String PATH = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera/";
    AlertDialog alertDialog = null;
    AlertDialog alertDialoginfo = null;
    Toast toast;
    String FilePath = "";
    long waitTimes = 3 * 1000;
    long touchTime = 0;
    private Camera mycamera;
    private SurfaceView surfaceView;
    private RelativeLayout re_c;
    private SurfaceHolder surfaceHolder;
    //	private LPR   api=null;
    private ILPR api;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //连接后拿到 Binder，转换成 AIDL，在不同进程会返回个代理
            api = ILPR.Stub.asInterface(service);
            System.out.println("===lianjie===");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            api = null;
        }
    };
    private int preWidth = 0;
    private int preHeight = 0;
    private int width;
    private int height;
    private TimerTask timer;
    private Timer timer2;
    private byte[] tackData;
    private LPRfinderView myView = null;
    private long recogTime;
    private boolean isFatty = false;
    private boolean bInitKernal = false;
    private ImageButton back;
    private ImageButton info;
    private Vibrator mVibrator;
    private String resultStr = null;
    private boolean bROI = false;
    private int[] m_ROI = {0, 0, 0, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent mIntent = new Intent();
        mIntent.setAction("com.speedata.aidl.CarCardService");
        mIntent.setPackage("lprkj.com.lpr");
        //绑定aidl服务
        boolean result = bindService(mIntent, mConnection, Service.BIND_AUTO_CREATE);
        System.out.println("===bangding===" + result);


        requestWindowFeature(Window.FEATURE_NO_TITLE);//
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int metricwidth = metric.widthPixels;
        int metricheight = metric.heightPixels;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Configuration cf = this.getResources().getConfiguration();
        int noriention = cf.orientation;
        requestWindowFeature(Window.FEATURE_NO_TITLE);//
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// ÉèÖÃÈ«ÆÁ

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        findView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    private void findView() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceViwe);
        re_c = (RelativeLayout) findViewById(R.id.re_c);
        back = (ImageButton) findViewById(R.id.back);
        info = (ImageButton) findViewById(R.id.info);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        width = metric.widthPixels;
        height = metric.heightPixels;

        int back_w = (int) (height * 0.066796875);
        int back_h = (int) (back_w * 1);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(back_w, back_h);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        layoutParams.topMargin = (int) ((back_h / 2));
        layoutParams.leftMargin = (int) (width * 0.15);
        back.setLayoutParams(layoutParams);

        int info_w = (int) (height * 0.066796875);
        int info_h = (int) (info_w);
        layoutParams = new RelativeLayout.LayoutParams(info_w, info_h);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        layoutParams.topMargin = (int) ((back_h / 2));
        layoutParams.rightMargin = (int) (height * 0.15);
        info.setLayoutParams(layoutParams);

        if (myView == null) {
            if (isFatty) {
                myView = new LPRfinderView(MainActivity.this, width, height, isFatty);
            } else {
                myView = new LPRfinderView(MainActivity.this, width, height);
            }
            re_c.addView(myView);
        }


        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        info.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getDialogStatus()) {
                    alertDialoginfo.setTitle("机型");

                    alertDialoginfo.setMessage( Build.MODEL);
                    Window window = alertDialoginfo.getWindow();
                    WindowManager.LayoutParams lp = window.getAttributes();

                    lp.alpha = 0.8f;
                    window.setAttributes(lp);
                    window.setGravity(Gravity.CENTER);
                    alertDialoginfo.show();
                }

            }
        });
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(MainActivity.this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.setFocusable(true);
        //surfaceView.invali.date();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void surfaceCreated(SurfaceHolder holder) {

        if (mycamera == null) {
            try {
                mycamera = Camera.open();
            } catch (Exception e) {
                e.printStackTrace();
                String mess = "打开摄像头失败";
                Toast.makeText(getApplicationContext(), mess, Toast.LENGTH_LONG).show();
                return;
            }
        }
        if (mycamera != null) {
            try {

                mycamera.setPreviewDisplay(holder);
                //mycamera.setDisplayOrientation(180);
                timer2 = new Timer();
                if (timer == null) {
                    timer = new TimerTask() {
                        public void run() {
                            if (mycamera != null) {
                                try {
                                    mycamera.autoFocus(new AutoFocusCallback() {
                                        public void onAutoFocus(boolean success, Camera camera) {

                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        ;
                    };
                }
                timer2.schedule(timer, 500, 2500);
                initCamera();


            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        boolean result = initAPI();
    }

    private boolean initAPI() {
        int nRet = 0;
        try {
            nRet = api.Init(m_ROI[0], m_ROI[1], m_ROI[2], m_ROI[3], preHeight, preWidth);
        } catch (RemoteException e) {

            e.printStackTrace();
        }
        if (nRet != 0) {
            Toast.makeText(getApplicationContext(), "激活失败" + nRet, Toast.LENGTH_SHORT).show();
            bInitKernal = false;
        } else {
            bInitKernal = true;
        }
        //        }
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(this).create();
            alertDialoginfo = new AlertDialog.Builder(this).create();
        }
        return bInitKernal;
    }

    public boolean getDialogStatus() {
        if (alertDialog != null && alertDialoginfo != null) {
            if (alertDialog.isShowing() || alertDialoginfo.isShowing()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            if (mycamera != null) {
                mycamera.setPreviewCallback(null);
                mycamera.stopPreview();
                mycamera.release();
                mycamera = null;
            }
        } catch (Exception e) {
        }
        if (bInitKernal) {
            bInitKernal = false;
            api = null;
        }
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        if (timer2 != null) {
            timer2.cancel();
            timer2 = null;
        }
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog.cancel();
            alertDialog = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            try {
                if (mycamera != null) {
                    mycamera.setPreviewCallback(null);
                    mycamera.stopPreview();
                    mycamera.release();
                    mycamera = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (bInitKernal) {
                bInitKernal = false;
                api = null;
            }
            finish();
            if (toast != null) {
                toast.cancel();
                toast = null;
            }
            if (timer2 != null) {
                timer2.cancel();
                timer2 = null;
            }
            if (alertDialog != null) {
                alertDialog.cancel();
                alertDialog = null;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @TargetApi(14)
    private void initCamera() {
        Camera.Parameters parameters = mycamera.getParameters();
        List<Size> list = parameters.getSupportedPreviewSizes();
        Size size;
        Size tmpsize = getOptimalPreviewSize(list, height, width);
        int length = list.size();
        int previewWidth = list.get(0).width;
        int previewheight = list.get(0).height;
        int second_previewWidth = 0;
        int second_previewheight = 0;
        int nlast = -1;
        int nThird = -1;
        int Third_previewWidth = 0;
        int Third_previewheight = 0;
        previewWidth = tmpsize.width;
        previewheight = tmpsize.height;
        if (length == 1) {
            preWidth = previewWidth;
            preHeight = previewheight;
        } else {
            second_previewWidth = previewWidth;
            second_previewheight = previewheight;
            for (int i = 0; i < length; i++) {
                size = list.get(i);
                if (size.height > 700 && size.height < previewheight) {
                    if (size.width * previewheight == size.height * previewWidth && size.height < second_previewheight) {
                        second_previewWidth = size.width;
                        second_previewheight = size.height;
                    }
                }
            }
            preWidth = second_previewWidth;
            preHeight = second_previewheight;
        }
        parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.setPreviewSize(preWidth, preHeight);
        if (!bROI) {
            int l, t, r, b;
            l = (int) (width * 0.2);
            r = (int) (width * 0.8);
            int ntmpH = (r - l) * 58 / 66;
            t = (height - ntmpH) / 2;
            b = t + ntmpH;

            double proportion = (double) width / (double) preHeight;
            double hproportion = (double) height / (double) preWidth;
            l = (int) (l / proportion);
            t = (int) (t / hproportion);
            r = (int) (r / proportion);
            b = (int) (b / hproportion);
            m_ROI[0] = l;
            m_ROI[1] = t;
            m_ROI[2] = r;
            m_ROI[3] = b;

            bROI = true;
        }
        if (parameters.getSupportedFocusModes().contains(
                parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);// 1Á¬Ðø¶Ô½¹
        }
        if (parameters.isZoomSupported()) {
            parameters.setZoom(2);
        }
        mycamera.setPreviewCallback(MainActivity.this);
        mycamera.setParameters(parameters);
        mycamera.setDisplayOrientation(90);
        mycamera.startPreview();
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        //surfaceView.invalidate();
        byte[] tackData = data;
        resultStr = "";
        if (!getDialogStatus() && bInitKernal) {

            byte result[];
            String res = "";
            try {
                //                System.out.println("===" + tackData.length());
                System.out.println("===tackData=" + tackData);
                bytesToImageFile(data);
                result = api.VideoRec(preWidth, preHeight, 1);
                System.out.println("===VideoRec=" + result);
                res = new String(result, "gb2312");
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (res != null && !"".equals(res.trim()))

            {
                Camera.Parameters parameters = mycamera.getParameters();
                mVibrator = (Vibrator) getApplication().getSystemService(
                        Service.VIBRATOR_SERVICE);
                mVibrator.vibrate(50);
                int[] datas = convertYUV420_NV21toARGB8888(tackData,
                        parameters.getPreviewSize().width,
                        parameters.getPreviewSize().height);

                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inInputShareable = true;
                opts.inPurgeable = true;
                Bitmap bitmap = Bitmap.createBitmap(datas,
                        parameters.getPreviewSize().width,
                        parameters.getPreviewSize().height,
                        Bitmap.Config.ARGB_8888);
                //savePicture(bitmap, "M");
                resultStr = "";
                resultStr = res;
                if (resultStr != "") {
                    alertDialog.setMessage(resultStr);
                    alertDialog.setTitle("识别结果");
                    Window window = alertDialog.getWindow();
                    WindowManager.LayoutParams lp = window.getAttributes();

                    lp.alpha = 0.8f;
                    lp.width = width * 2 / 3;

                    window.setAttributes(lp);
                    window.setGravity(Gravity.LEFT | Gravity.BOTTOM);
                    alertDialog.show();
                } else {
                    System.out.println("===shibiejieguo null");
                }
            }


        }

    }


    private void bytesToImageFile(byte[] bytes) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/aaa.jpeg");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes, 0, bytes.length);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public int[] convertYUV420_NV21toARGB8888(byte[] data, int width, int height) {
        int size = width * height;
        int offset = size;
        int[] pixels = new int[size];
        int u, v, y1, y2, y3, y4;

        // i along Y and the final pixels
        // k along pixels U and V
        for (int i = 0, k = 0; i < size; i += 2, k += 2) {
            y1 = data[i] & 0xff;
            y2 = data[i + 1] & 0xff;
            y3 = data[width + i] & 0xff;
            y4 = data[width + i + 1] & 0xff;

            u = data[offset + k] & 0xff;
            v = data[offset + k + 1] & 0xff;
            u = u - 128;
            v = v - 128;

            pixels[i] = convertYUVtoARGB(y1, u, v);
            pixels[i + 1] = convertYUVtoARGB(y2, u, v);
            pixels[width + i] = convertYUVtoARGB(y3, u, v);
            pixels[width + i + 1] = convertYUVtoARGB(y4, u, v);

            if (i != 0 && (i + 2) % width == 0)
                i += width;
        }

        return pixels;
    }

    private int convertYUVtoARGB(int y, int u, int v) {
        int r, g, b;

        r = y + (int) 1.402f * u;
        g = y - (int) (0.344f * v + 0.714f * u);
        b = y + (int) 1.772f * v;
        r = r > 255 ? 255 : r < 0 ? 0 : r;
        g = g > 255 ? 255 : g < 0 ? 0 : g;
        b = b > 255 ? 255 : b < 0 ? 0 : b;
        return 0xff000000 | (r << 16) | (g << 8) | b;
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - touchTime) >= waitTimes) {
            Toast.makeText(this, "再按一次返回键退出程序", (int) waitTimes).show();
            touchTime = currentTime;
        } else {
            //finish();
            //android.os.Process.killProcess(android.os.Process.myPid());
            try {
                api.Release();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
    }
}
