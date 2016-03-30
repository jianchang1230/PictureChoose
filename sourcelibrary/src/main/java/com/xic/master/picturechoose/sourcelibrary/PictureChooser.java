package com.xic.master.picturechoose.sourcelibrary;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ProjectName: PictureChoose
 * Describe: ͼƬѡ����
 * Author: �ܽ���
 * Date: 2016/3/16 10:07
 * Email: jianchang1230@163.com
 * QQ: 939635660
 * Copyright (c) 2016, *******.com All Rights Reserved.
 */
public class PictureChooser {

    private static final String TAG = "PictureChooser";
    /**
     * �����ѡ��
     */
    private final int PIC_GALLERY_REQUESTCODE = 10101;
    /**
     * ���������
     */
    private final int PIC_CAMERA_REQUESTCODE = 10102;
    /**
     * �ü�ͼƬ
     */
    private final int PIC_CLIP_REQUESTCODE = 10103;

    private Activity mContext;
    private Fragment mFragment;

    /**
     * ͼƬ����   tempΪĬ��ֵ
     */
    private String cameraPicName = "temp.jpg";
    private File tempFile;
    private String cameraFilePath;
    private String galleryFilePath;
    private String clipFilePath;
    private String fileDir = "pic";

    /**
     * �Ƿ����
     */
    private boolean isClip = false;

    /**
     * �Ƿ�ѹ��
     */
    private boolean isCompressor = false;
    /**
     * ��ͼ ��߱���
     */
    private int aspectX = 1;
    private int aspectY = 1;
    /**
     * ���ͼƬ���
     */
    private int outputX = 0;
    private int outputY = 0;
    /**
     * ѹ��ͼƬ�������
     */
    private int maxkb = 0;
    /**
     * ѹ��ͼƬ���
     */
    private int reqWidth = 0;
    private int reqHeight = 0;
    /**
     * ��¼ʱ���
     */
    private long currentTimeMillis = 0;
    /**
     * ���û�������3��
     */
    private int maxFile = 3;

    private OnPicturePickListener mOnPicturePickListener;

    /**
     * ͼƬѡ��ʽ
     */
    private PictureFrom mPictureFrom = PictureFrom.CAMERA;

    public PictureChooser(Fragment fragment) {
        this(null, fragment);
    }

    public PictureChooser(Context context) {
        this(context, null);
    }

    PictureChooser(Context context, Fragment fragment) {
        if (null == fragment && null == context) {
            throw new IllegalArgumentException("fragment == null && context == null");
        }
        if (null != fragment) {
            this.mFragment = fragment;
            this.mContext = mFragment.getActivity();
        } else {
            this.mContext = (Activity) context;
        }
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            this.tempFile = new File(mContext.getExternalCacheDir().getPath() + File.separator + fileDir);
        } else {
            this.tempFile = new File(mContext.getCacheDir().getPath() + File.separator + fileDir);
        }
        tempFile.mkdirs();
        if (isHaseMaxFile()) {
            clearPics();
        }
    }

    public void setCameraPicName(String cameraPicName) {
        this.cameraPicName = cameraPicName;
    }

    public void setmPictureFrom(PictureFrom mPictureFrom) {
        this.mPictureFrom = mPictureFrom;
    }

    public void setIsClip(boolean isClip, int aspectX, int aspectY, int outputX, int outputY) {
        this.isClip = isClip;
        this.aspectX = aspectX;
        this.aspectY = aspectY;
        this.outputX = outputX;
        this.outputY = outputY;
    }

    public void setIsCompressor(boolean isCompressor, int maxkb, int reqWidth, int reqHeight) {
        this.isCompressor = isCompressor;
        this.maxkb = maxkb;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
    }

    /**
     * ��ʼִ��ѡ��ͼƬ����
     *
     * @param listener ִ���¼��ļ���
     */
    public void execute(OnPicturePickListener listener) {
        if (null == listener) {
            throw new NullPointerException("OnPicturePickListener == NULL");
        }
        this.mOnPicturePickListener = listener;
        if (mPictureFrom == PictureFrom.GALLERY) {
            galleryPic();
        } else {
            openCamera();
        }
    }

    /**
     * �����
     */
    protected void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//������Ƭ
        cameraFilePath = tempFile.getPath() + File.separator + currentTimeMillis + cameraPicName;
        Uri uri = Uri.parse("file://" + cameraFilePath);
//        Uri uri = Uri.fromFile(new File(cameraFilePath));
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.setClipData(ClipData.newRawUri(null, uri));
//        intent.putExtra("return-data", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, PIC_CAMERA_REQUESTCODE);
    }

    private void startActivityForResult(Intent intent, int requestCode) {
        if (null != mFragment) {
            mFragment.startActivityForResult(intent, requestCode);
        } else {
            mContext.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * �жϻ�����ļ��ǲ��ǳ�����
     *
     * @return �Ƿ񳬹��������ֵ ture���� falseû��
     */
    protected boolean isHaseMaxFile() {
        if (tempFile.isDirectory()) {
            if (null != tempFile.listFiles() && tempFile.listFiles().length >= maxFile) {
                // File[] files = tempFile.listFiles();
                Log.i(TAG, tempFile.listFiles().length + "");
                return true;
            } else {
                return false;
            }
        } else {
            if (null != tempFile.getParentFile().listFiles() && tempFile.getParentFile().listFiles().length >= maxFile) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * �������
     *
     * @return   ��������Ƿ�ɹ�
     */
    protected boolean clearPics() {
        boolean state = true;
        File directory = null;
        if (tempFile.isDirectory()) {
            directory = tempFile;
        } else {
            directory = tempFile.getParentFile();
        }
        File[] files = directory.listFiles();
        if (null != files) {
            for (File file : files) {
                state = state & file.delete();
            }
        }
        return state;
    }

    protected boolean delPic(String patch) {
        File file = new File(patch);
        return delPic(file);
    }

    /**
     * ɾ��ͼƬ
     *
     * @param file Ҫɾ����ͼƬ��ַ
     * @return ɾ�������Ƿ�ɹ�
     */
    protected boolean delPic(File file) {
        boolean state = false;
        if (null == file) {
            return state;
        }
        if (!file.exists()) {
            return state;
        }
        return file.delete();
    }

    /**
     * �����ѡ��
     */
    protected void galleryPic() {
        // Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);//��Ƭ
        Intent intent = new Intent(Intent.ACTION_PICK, null);//��Ƭ
        intent.setType("image/*");
        startActivityForResult(intent, PIC_GALLERY_REQUESTCODE);
    }

    /**
     * ��ͼ
     *
     * @param uri Ҫ��ͼ���ļ�ӳ���ַ
     */
    private void startPhotoClip(Uri uri) {
        if (tempFile.isDirectory()) {
            clipFilePath = tempFile.getPath() + File.separator + System.currentTimeMillis() + cameraPicName;
        } else {
            clipFilePath = tempFile.getParent() + File.separator + System.currentTimeMillis() + cameraPicName;
        }
        Log.i(TAG, "url" + clipFilePath);
        // Uri uri1 = Uri.parse(clipFilePath);
        Uri uri1 = Uri.fromFile(new File(clipFilePath));   //һЩ�֙Cֻ����fromFile̎����t��ݔ�����ɲ��ˈDƬ
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // cropΪtrue�������ڿ�����intent��������ʾ��view���Լ���
        intent.putExtra("crop", "true");
        // aspectX aspectY �ǿ�ߵı���
        if (aspectX > 0 && aspectY > 0) {
            intent.putExtra("aspectX", aspectX);
            intent.putExtra("aspectY", aspectY);
        } else if (aspectX == 0 && aspectY == 0) {
            //0�Ͳ��������и����������
        } else {
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
        }
        if (outputX > 0 && outputY > 0) {
            // outputX,outputY �Ǽ���ͼƬ�Ŀ��
            intent.putExtra("outputX", outputX);
            intent.putExtra("outputY", outputY);
        }
        intent.putExtra("return-data", false);//�Ƿ�ͨ��//�ص�����data.getExtras().getParcelable("data") false��������Ϊ��
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri1);//ͼ�����
        startActivityForResult(intent, PIC_CLIP_REQUESTCODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            /**
             * ���
             */
            case PIC_CAMERA_REQUESTCODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (tempFile.isDirectory()) {
                        cameraFilePath = tempFile.getPath() + File.separator + currentTimeMillis + cameraPicName;
                    }
                    if (isClip) {
                        Uri uri1 = Uri.parse("file://" + cameraFilePath);
                        //Uri uri1 = Uri.fromFile(new File(cameraFilePath));
                        startPhotoClip(uri1);
                    } else {
                        senCompressorFile(cameraFilePath);
                    }
                }
                break;
            /**
             * ���ѡ��
             */
            case PIC_GALLERY_REQUESTCODE:
                if (data != null) {
                    //ȡ�÷��ص�Uri,������ѡ����Ƭ��ʱ�򷵻ص�����Uri��ʽ���������������еû�����Uri�ǿյģ�����Ҫ�ر�ע��
                    Uri uri = data.getData();
                    //���ص�Uri��Ϊ��ʱ����ôͼƬ��Ϣ���ݶ�����Uri�л�á����Ϊ�գ���ô���Ǿͽ�������ķ�ʽ��ȡ
                    if (uri != null) {
                        if (isClip) {
                            startPhotoClip(uri);
                        } else {
                            galleryFilePath = getRealPathFromURI(uri);
                            senCompressorFile(galleryFilePath);
                        }
                    } else {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            //��������Щ���պ��ͼƬ��ֱ�Ӵ�ŵ�Bundle�е��������ǿ��Դ��������ȡBitmapͼƬ
                            Bitmap image = extras.getParcelable("data");
                        }
                    }

                }
                break;
            /**
             * ���ź�
             */
            case PIC_CLIP_REQUESTCODE:
                if (null != data || resultCode == Activity.RESULT_OK) {
                    if (null == clipFilePath) {
                        clipFilePath = getRealPathFromURI(data.getData());
                        Log.i(TAG, clipFilePath);
                    }
                    Log.i(TAG, "clipFilePath " + clipFilePath);
                    File endFile = new File(clipFilePath);
                    Log.i(TAG, "file��С" + endFile.length());
                    senCompressorFile(clipFilePath);
                }
                break;
        }
    }

    /**
     * data.getData() uri ת��ʵ·��
     *
     * @param contentUri Ҫת����ӳ���ַ
     * @return ת������ļ�·��
     */
    public String getRealPathFromURI(Uri contentUri) {
        String res = contentUri.getPath();
        Log.e(TAG, contentUri.getPath());
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = mContext.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    /**
     * ��ȡͼƬѹ������
     *
     * @param filePath ͼƬ��ַ
     */
    protected void senCompressorFile(final String filePath) {
        Log.i(TAG, filePath);
        senFile(filePath);
        if (isCompressor) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    fileCompressor(filePath);
                }
            }).start();
        }
    }


    /**
     * ����ͼƬ������ֵ
     * @param options ���Ų���
     * @param reqWidth ��
     * @param reqHeight ��
     * @return ����
     */
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * �ߴ�ѹ��
     * ����·�����ͼƬ��ѹ��������bitmap������ʾ
     * @param filePath ͼƬ��ַ
     * @param reqWidth ѹ�����ͼƬ��
     * @param reqHeight ѹ�����ͼƬ��
     * @return ѹ�����ͼƬ
     */
    public Bitmap getSmallBitmap(String filePath, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * �ߴ�ѹ��
     * ����·�����ͼƬ��ѹ��������bitmap������ʾ
     * @param filePath ͼƬ��ַ
     * @param inSampleSize ѹ������
     * @return ѹ�����ͼƬ
     */
    public Bitmap getSmallBitmap(String filePath, int inSampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = inSampleSize;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * ����ѹ��
     * @param image Ҫѹ�����ļ�
     * @param maxkb ѹ��������С
     * @param filePath ѹ�����ļ���ŵ�ַ
     */
    public void compressBitmap(Bitmap image, int maxkb, String filePath) {
        Log.i(TAG, "ԭʼbitmap��С" + getBitmapSize(image));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// ����ѹ������������100��ʾ��ѹ������ѹ��������ݴ�ŵ�baos��
        int options = 100;
        Log.i(TAG, "ԭʼ��С" + baos.toByteArray().length);
        while (baos.toByteArray().length / 1024 > maxkb) { // ѭ���ж����ѹ����ͼƬ�Ƿ����(maxkb)50kb,���ڼ���ѹ��
            Log.i(TAG, "ѹ��һ��!  options " + options + "  " + baos.toByteArray().length);
            baos.reset();// ����baos�����baos
            options -= 10;// ÿ�ζ�����10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// ����ѹ��options%����ѹ��������ݴ�ŵ�baos��
        }
        Log.i(TAG, "ѹ������options" + options);
        Log.i(TAG, "ѹ�����С" + baos.toByteArray().length);
        Log.i(TAG, "ѹ����bitmap��С" + getBitmapSize(image));
//        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// ��ѹ���������baos��ŵ�ByteArrayInputStream��
//        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// ��ByteArrayInputStream��������ͼƬ
//        File file = cf(options, bitmap);
        bitmapToFile(options, image, filePath);
        // ���ͼƬ��û�л��գ�ǿ�ƻ���
        if (!image.isRecycled()) {
            Log.i(TAG, "������image");
            image.recycle();
        }
    }

    /**
     * ��ȡbitmap��С
     *
     * @param bitmap Ҫ�����ͼƬ�ļ�
     * @return ͼƬ��������С
     */
    public int getBitmapSize(Bitmap bitmap) {
        if (null == bitmap)
            return -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {    //API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {//API 12
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();                //earlier version
    }

    /**
     * ֱ��bitmapѹ�����ļ�
     * @param options ѹ������ֵ
     * @param bitmap Ҫѹ����ͼƬ
     * @param filePath ѹ�����ļ���ŵ�ַ
     */
    private void bitmapToFile(int options, Bitmap bitmap, String filePath) {
        try {
            FileOutputStream fos = null;
            fos = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, fos);
            Log.i(TAG, filePath);
            fos.flush();
            fos.close();
            //ѹ���ɹ��ص�
            compressorSuccess(filePath);
            // ���ͼƬ��û�л��գ�ǿ�ƻ���
            if (!bitmap.isRecycled()) {
                Log.i(TAG, "������bitmap");
                bitmap.recycle();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ��bitmapת����String
     * @param filePath ͼƬ��ַ
     * @param reqWidth ת�����ͼƬ��
     * @param reqHeight ת�����ͼƬ��
     * @return 64λ��ͼƬ�ļ�
     */
    public String bitmapToString(String filePath, int reqWidth, int reqHeight) {
        Bitmap bm = getSmallBitmap(filePath, reqWidth, reqHeight);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] b = baos.toByteArray();
        // ���ͼƬ��û�л��գ�ǿ�ƻ���
        if (!bm.isRecycled()) {
            Log.i(TAG, "������bm");
            bm.recycle();
        }
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    /**
     * ��ͼƬ�ļ�ѹ��
     * @param filePath ͼƬ��ַ
     */
    public void fileCompressor(String filePath) {
        File endFile = new File(filePath);
        Log.i(TAG, "file��С" + endFile.length());
        if (0 == maxkb) {
            bitmapToFile(100, getSmallBitmap(filePath, reqWidth, reqHeight), filePath);
        } else if (0 == reqWidth || 0 == reqHeight) {
            try {
                Bitmap bit = BitmapFactory.decodeFile(filePath);
                if (bit != null) {
                    compressBitmap(bit, maxkb, filePath);
                }
            } catch (OutOfMemoryError oom) {
                oom.printStackTrace();
                compressBitmap(getSmallBitmap(filePath, 4), maxkb, filePath);
            }
        } else {
            compressBitmap(getSmallBitmap(filePath, reqWidth, reqHeight), maxkb, filePath);
        }
    }

    /**
     * ����ѡ������
     */
    public void reset() {
        this.cameraPicName = "temp.jpg";
        this.isClip = false;
        this.isCompressor = false;
        this.mPictureFrom = PictureFrom.GALLERY;
        this.aspectX = 1;
        this.aspectY = 1;
        this.outputX = 0;
        this.outputY = 0;
        this.maxkb = 0;
        this.reqWidth = 0;
        this.reqHeight = 0;
    }

    /**
     * ��ȡͼƬ�󴥷�
     *
     * @param filePath ͼƬ��ַ
     */
    protected void senFile(String filePath) {
        if (null != mOnPicturePickListener) {
            mOnPicturePickListener.senFile(filePath);
        }
    }

    /**
     * ѹ���󴥷�
     * @param filePath ͼƬ��ַ
     */
    protected void compressorSuccess(String filePath) {
        if (null != mOnPicturePickListener) {
            mOnPicturePickListener.compressorSuccess(filePath);
        }
    }


    public interface OnPicturePickListener {

        void senFile(String filePath);

        void compressorSuccess(String filePath);

    }

    public enum PictureFrom {
        GALLERY, CAMERA
    }
}
