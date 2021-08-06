package cn.jystudio.local.barcode.recognizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;
import com.facebook.react.bridge.*;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;

import java.util.*;

public class LocalBarcodeRecognizerModule extends ReactContextBaseJavaModule {
    public static final String BARCODE_CODE_TYPE_KEY = "codeTypes";

    public static final Map<String, Object> VALID_BARCODE_TYPES = Collections
            .unmodifiableMap(new HashMap<String, Object>() {
                {
                    put("aztec", BarcodeFormat.AZTEC.toString());
                    put("ean13", BarcodeFormat.EAN_13.toString());
                    put("ean8", BarcodeFormat.EAN_8.toString());
                    put("qr", BarcodeFormat.QR_CODE.toString());
                    put("pdf417", BarcodeFormat.PDF_417.toString());
                    put("upc_e", BarcodeFormat.UPC_E.toString());
                    put("datamatrix", BarcodeFormat.DATA_MATRIX.toString());
                    put("code39", BarcodeFormat.CODE_39.toString());
                    put("code93", BarcodeFormat.CODE_93.toString());
                    put("interleaved2of5", BarcodeFormat.ITF.toString());
                    put("codabar", BarcodeFormat.CODABAR.toString());
                    put("code128", BarcodeFormat.CODE_128.toString());
                    put("maxicode", BarcodeFormat.MAXICODE.toString());
                    put("rss14", BarcodeFormat.RSS_14.toString());
                    put("rssexpanded", BarcodeFormat.RSS_EXPANDED.toString());
                    put("upc_a", BarcodeFormat.UPC_A.toString());
                    put("upc_ean", BarcodeFormat.UPC_EAN_EXTENSION.toString());
                }
            });

    public LocalBarcodeRecognizerModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    /**
     * @return the name of this module. This will be the name used to
     *         {@code require()} this module from javascript.
     */
    @Override
    public String getName() {
        return "LocalBarcodeRecognizer";
    }

    @ReactMethod
    public void decode(String base64Data, ReadableMap options, final Promise p) {
        try {
            byte[] decodedString = Base64.decode(base64Data, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            Result result = null;
            MultiFormatReader reader = new MultiFormatReader();

            if (options.hasKey(BARCODE_CODE_TYPE_KEY)) {
                ReadableArray codeTypes = options.getArray(BARCODE_CODE_TYPE_KEY);
                if (codeTypes.size() > 0) {
                    EnumMap<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
                    EnumSet<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
                    for (int i = 0; i < codeTypes.size(); i++) {
                        String code = codeTypes.getString(i);
                        String formatString = (String) VALID_BARCODE_TYPES.get(code);
                        if (formatString != null) {
                            decodeFormats.add(BarcodeFormat.valueOf(formatString));
                        }
                    }
                    hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
                    reader.setHints(hints);
                }
            }

            try {
                BinaryBitmap bitmap = generateBitmapFromImageData(decodedByte);
                result = reader.decode(bitmap);
            } catch (NotFoundException e) {
                // BinaryBitmap bitmap = generateBitmapFromImageData(rotateImage(decodedByte,
                // 90));
                // try {
                // result = reader.decode(bitmap);
                // } catch (NotFoundException e1) {
                // // no barcode Found
                // }
            } catch (Throwable t) {
                t.printStackTrace();
            }

            p.resolve(result != null ? result.getText() : "");
        } catch (Exception e) {
            p.reject(e);
        }
    }

    public static Bitmap getSmallerBitmap(Bitmap bitmap) {
        int size = bitmap.getWidth() * bitmap.getHeight() / 160000;
        if (size <= 1)
            return bitmap; // 如果小于
        else {
            Matrix matrix = new Matrix();
            matrix.postScale((float) (1 / Math.sqrt(size)), (float) (1 / Math.sqrt(size)));
            Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                    true);
            return resizeBitmap;
        }
    }

    private BinaryBitmap generateBitmapFromImageData(Bitmap bitmap) {
        bitmap = getSmallerBitmap(bitmap);
        int[] mImageData = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(mImageData, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int inputWidth = bitmap.getWidth();
        int inputHeight = bitmap.getHeight();
        byte[] yuv = new byte[inputWidth * inputHeight + ((inputWidth % 2 == 0 ? inputWidth : (inputWidth + 1))
                * (inputHeight % 2 == 0 ? inputHeight : (inputHeight + 1))) / 2];
        encodeYUV420SP(yuv, mImageData, inputWidth, inputHeight);
        bitmap.recycle();
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(yuv, // byte[] yuvData
                inputWidth, // int dataWidth
                inputHeight, // int dataHeight
                0, // int left
                0, // int top
                inputWidth, // int width
                inputHeight, // int height
                false // boolean reverseHorizontal
        );
        return new BinaryBitmap(new HybridBinarizer(source));
        // LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(),
        // bitmap.getHeight(),mImageData);
        // return new BinaryBitmap(new HybridBinarizer(source));
    }

    private static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        // 帧图片的像素大小
        final int frameSize = width * height;
        // Y的index从0开始
        int yIndex = 0;
        // UV的index从frameSize开始
        int uvIndex = frameSize;
        // YUV数据, ARGB数据
        int Y, U, V, a, R, G, B;
        ;
        int argbIndex = 0;
        // ---循环所有像素点，RGB转YUV---
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                // a is not used obviously
                a = (argb[argbIndex] & 0xff000000) >> 24;
                R = (argb[argbIndex] & 0xff0000) >> 16;
                G = (argb[argbIndex] & 0xff00) >> 8;
                B = (argb[argbIndex] & 0xff);
                argbIndex++;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                Y = Math.max(0, Math.min(Y, 255));
                U = Math.max(0, Math.min(U, 255));
                V = Math.max(0, Math.min(V, 255));
                yuv420sp[yIndex++] = (byte) Y;
                // ---UV---
                if ((j % 2 == 0) && (i % 2 == 0)) {
                    yuv420sp[uvIndex++] = (byte) V;
                    yuv420sp[uvIndex++] = (byte) U;
                }
            }
        }
    }

    private Bitmap rotateImage(Bitmap src, float degree) {
        // create new matrix
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);
        Bitmap bmp = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        return bmp;
    }
}