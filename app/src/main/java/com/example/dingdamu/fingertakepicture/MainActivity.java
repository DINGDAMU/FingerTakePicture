package com.example.dingdamu.fingertakepicture;


        import java.io.File;
        import java.io.IOException;
        import java.io.OutputStream;
        import java.text.SimpleDateFormat;
        import java.util.Date;
        import java.util.Locale;


        import android.app.Activity;
        import android.graphics.PixelFormat;
        import android.hardware.Camera;
        import android.os.Bundle;
        import android.os.Environment;
        import android.view.MotionEvent;
        import android.view.Surface;
        import android.view.SurfaceHolder;
        import android.view.SurfaceHolder.Callback;
        import android.view.SurfaceView;
        import android.view.View;
        import android.view.ViewGroup;
        import android.view.WindowManager;
        import android.widget.Button;
        import android.widget.FrameLayout;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;
        import android.view.Menu;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.view.Window;

        import com.jraska.falcon.Falcon;

/**
 * Android手指拍照
 *
 * @author wwj
 * @date 2013/4/29
 */
public class MainActivity extends Activity {
    private View layout;
    private Camera camera;
    private Camera.Parameters parameters = null;
    private Compass compass;
    Process process = null;








    Bundle bundle = null; // 声明一个Bundle对象，用来存储数据

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 显示界面
        setContentView(R.layout.activity_main);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags=WindowManager.LayoutParams.FLAG_FULLSCREEN;

        Compass.mText=(TextView)findViewById(R.id.compass_information);
        compass = new Compass(this);



        compass.arrowView = (ImageView) findViewById(R.id.main_image_hands);


        layout = this.findViewById(R.id.buttonLayout);


        SurfaceView surfaceView=(SurfaceView) findViewById(R.id.surfaceView);
         surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
         surfaceView.getHolder().setFixedSize(176, 144);	//设置Surface分辨率
         surfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
         surfaceView.getHolder().addCallback(new SurfaceCallback());//为SurfaceView的句柄添加一个回调函数


        try{
            process = Runtime.getRuntime().exec("su");
        }catch(IOException e){
            e.printStackTrace();
        }




        Button add = (Button)findViewById(R.id.takepicture);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appName = MainActivity.this.getString(R.string.app_name);
                File extStorageDir = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appName);


                //设置文件名
                File mFile;
                Date mCurrentDate = new Date();
                String mTimestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALY).format(mCurrentDate);
                String path = extStorageDir.getPath() + File.separator;
                mFile = new File(path + "FEEDIMG_" + mTimestamp + ".jpg");



                try{
                    OutputStream outputStream = null;
                    try {
                        outputStream = process.getOutputStream();
                        outputStream.write(("screencap -p " + mFile).getBytes("ASCII"));
                        outputStream.flush();
                    }catch(Exception e){
                    } finally {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                    process.waitFor();
                }catch(Exception e){
                }finally {
                    if(process != null){
                        process.destroy();
                    }
                }

            }
        });

    }







            /**
             * 图片被点击触发的时间
             *
             * @param
             */



           private final class SurfaceCallback implements Callback {

    //拍照状态变化时调用该方法
              @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                           int height) {
                    parameters = camera.getParameters(); // 获取各项参数
                    parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
                    parameters.setPreviewSize(width, height); // 设置预览大小
                    parameters.setPreviewFrameRate(5);    //设置每秒显示4帧
                    parameters.setPictureSize(width, height); // 设置保存的图片尺寸
                    parameters.setJpegQuality(80); // 设置照片质量
                }

                // 开始拍照时调用该方法
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        camera = Camera.open(); // 打开摄像头
                        camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
                        camera.setDisplayOrientation(getPreviewDegree(MainActivity.this));
                        camera.startPreview(); // 开始预览
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                // 停止拍照时调用该方法
               @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    if (camera != null) {
                        camera.release(); // 释放照相机
                        camera = null;
                    }
                }
            }


            /**
             * 点击手机屏幕是，显示两个按钮
             */
            public boolean onTouchEvent(MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        layout.setVisibility(ViewGroup.VISIBLE); // 设置视图可见
                        break;
                }
                return true;
            }



            // 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
            public static int getPreviewDegree(Activity activity) {
                // 获得手机的方向
                int rotation = activity.getWindowManager().getDefaultDisplay()
                        .getRotation();
                int degree = 0;
                // 根据手机的方向计算相机预览画面应该选择的角度
                switch (rotation) {
                    case Surface.ROTATION_0:
                        degree = 90;
                        break;
                    case Surface.ROTATION_90:
                        degree = 0;
                        break;
                    case Surface.ROTATION_180:
                        degree = 270;
                        break;
                    case Surface.ROTATION_270:
                        degree = 180;
                        break;
                }
                return degree;
            }




            @Override
            protected void onStart() {
                super.onStart();
                compass.start();

            }

            @Override
            protected void onPause() {
                super.onPause();
                compass.stop();
            }

            @Override
            protected void onResume() {
                super.onResume();
                compass.start();
            }

            @Override
            protected void onStop() {
                super.onStop();
                compass.stop();
            }








        }



