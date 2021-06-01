package tomerblecher.yuvtransform;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class YuvConverter {
    /**
     * Converts an NV21 image into JPEG compressed.
     * @param nv21 byte[] of the input image in NV21 format
     * @param width Width of the image.
     * @param height Height of the image.
     * @param quality Quality of compressed image(0-100)
     * @return byte[] of a compressed Jpeg image.
     */
    public static byte[] NV21toJPEG(byte[] nv21, int width, int height, int quality) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        yuv.compressToJpeg(new Rect(0, 0, width, height), quality, out);
        return out.toByteArray();
    }

    /**
     * Format YUV_420 planes in to NV21.
     * Removes strides from planes and combines the result to single NV21 byte array.
     * @param planes  List of Bytes list
     * @param strides contains the strides of each plane. The structure :
     *                strideRowFirstPlane,stridePixelFirstPlane, strideRowSecondPlane
     * @param width   Width of the image
     * @param height  Height of given image
     * @return NV21 image byte[].
     */
    public static byte[] YUVtoNV21 (List<byte[]> planes, int[] strides, int width, int height) {
        Rect crop = new Rect(0, 0, width, height);
        int format = ImageFormat.YUV_420_888;
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[strides[0]];
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.size(); i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = width * height + 1;
                    outputStride = 2;
                    break;
                case 2:
                    channelOffset = width * height;
                    outputStride = 2;
                    break;
            }

            ByteBuffer buffer = ByteBuffer.wrap(planes.get(i));
            int rowStride;
            int pixelStride;
            if(i ==0 ) {
                rowStride = strides[i];
                pixelStride = strides[i+1];
            }
            else {
                rowStride = strides[i *2 ];
                pixelStride = strides[i *2 +1];
            }
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;

    }
    // ByteBuffer feedInputTensorFrame(List<byte[]> bytesList, int imageHeight, int imageWidth, float mean, float std, int rotation) throws IOException {
    //     ByteBuffer Y = ByteBuffer.wrap(bytesList.get(0));
    //     ByteBuffer U = ByteBuffer.wrap(bytesList.get(1));
    //     ByteBuffer V = ByteBuffer.wrap(bytesList.get(2));
    
    //     int Yb = Y.remaining();
    //     int Ub = U.remaining();
    //     int Vb = V.remaining();
    
    //     byte[] data = new byte[Yb + Ub + Vb];
    
    //     Y.get(data, 0, Yb);
    //     V.get(data, Yb, Vb);
    //     U.get(data, Yb + Vb, Ub);
    
    //     Bitmap bitmapRaw = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
    //     Allocation bmData = renderScriptNV21ToRGBA888(
    //         mRegistrar.context(),
    //         imageWidth,
    //         imageHeight,
    //         data);
    //     bmData.copyTo(bitmapRaw);
    
    //     Matrix matrix = new Matrix();
    //     matrix.postRotate(rotation);
    //     bitmapRaw = Bitmap.createBitmap(bitmapRaw, 0, 0, bitmapRaw.getWidth(), bitmapRaw.getHeight(), matrix, true);
    
    //     return feedInputTensor(bitmapRaw, mean, std);
    //   }
    //   ByteBuffer feedInputTensor(Bitmap bitmapRaw, float mean, float std) throws IOException {
    //     Tensor tensor = tfLite.getInputTensor(0);
    //     int[] shape = tensor.shape();
    //     inputSize = shape[1];
    //     int inputChannels = shape[3];
    
    //     int bytePerChannel = tensor.dataType() == DataType.UINT8 ? 1 : BYTES_PER_CHANNEL;
    //     ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * inputChannels * bytePerChannel);
    //     imgData.order(ByteOrder.nativeOrder());
    
    //     Bitmap bitmap = bitmapRaw;
    //     if (bitmapRaw.getWidth() != inputSize || bitmapRaw.getHeight() != inputSize) {
    //       Matrix matrix = getTransformationMatrix(bitmapRaw.getWidth(), bitmapRaw.getHeight(),
    //           inputSize, inputSize, false);
    //       bitmap = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888);
    //       final Canvas canvas = new Canvas(bitmap);
    //       canvas.drawBitmap(bitmapRaw, matrix, null);
    //     }
    
    //     if (tensor.dataType() == DataType.FLOAT32) {
    //       for (int i = 0; i < inputSize; ++i) {
    //         for (int j = 0; j < inputSize; ++j) {
    //           int pixelValue = bitmap.getPixel(j, i);
    //           imgData.putFloat((((pixelValue >> 16) & 0xFF) - mean) / std);
    //           imgData.putFloat((((pixelValue >> 8) & 0xFF) - mean) / std);
    //           imgData.putFloat(((pixelValue & 0xFF) - mean) / std);
    //         }
    //       }
    //     } else {
    //       for (int i = 0; i < inputSize; ++i) {
    //         for (int j = 0; j < inputSize; ++j) {
    //           int pixelValue = bitmap.getPixel(j, i);
    //           imgData.put((byte) ((pixelValue >> 16) & 0xFF));
    //           imgData.put((byte) ((pixelValue >> 8) & 0xFF));
    //           imgData.put((byte) (pixelValue & 0xFF));
    //         }
    //       }
    //     }
    
    //     return imgData;
    //   }
}
