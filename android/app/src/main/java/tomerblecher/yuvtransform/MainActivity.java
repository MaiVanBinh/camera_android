package tomerblecher.yuvtransform;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import android.content.Context;
import android.content.ContextWrapper;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;
import org.opencv.imgproc.Imgproc;
import android.graphics.BitmapFactory;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.android.OpenCVLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends FlutterActivity {
    // Set a name for the method chanel.
    // This name is a key for the Flutter MethodChannel and needs to be equal to the name configured at the dart part
    private static final String CHANNEL = "tomer.blecher.yuv_transform/yuv";
    private Mat mRgba = null;
    private Mat bitmapToMat(Bitmap bmp) {
        Mat mat = new Mat();
        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        return mat;
    }

    private Bitmap matToBitmap(Mat m) {
        Bitmap myBitmap = null;
        myBitmap = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, myBitmap);
        return  myBitmap;
    }
    
    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);
        System.out.println("JAVA REGISTERED ");
        OpenCVLoader.initDebug();
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            // 
                            switch (call.method) {
                                case "yuv_transform": {

                                    List<byte[]> bytesList = call.argument("platforms");
                                    int[] strides = call.argument("strides");
                                    int width = call.argument("width");
                                    int height = call.argument("height");
                                    byte[] data = YuvConverter.NV21toJPEG(YuvConverter.YUVtoNV21(bytesList, strides, width, height), width, height, 100);
                                    Bitmap bitmapRaw = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(90);
                                    Bitmap finalbitmap = Bitmap.createBitmap(bitmapRaw, 0, 0, bitmapRaw.getWidth(), bitmapRaw.getHeight(), matrix, true);
                                    // String path = saveToInternalStorage(finalbitmap, "bitmapRawtestAndroid1.jpeg");
                                    mRgba = bitmapToMat(finalbitmap);
                                    mRgba.release();
                                    result.success(" {200, 300, 400}");
                                }
                            }
                        }
                );

    }
    private String saveToInternalStorage(Bitmap bitmapImage, String path){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,path);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            // Log.i(TAG, "saveToInternalStorage: Flia" + mypath);
        } catch (Exception e) {
            // Log.i(TAG, "saveToInternalStorage: Flia" + e);
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

}
