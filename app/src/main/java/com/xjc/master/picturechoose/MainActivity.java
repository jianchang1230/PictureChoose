package com.xjc.master.picturechoose;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.xic.master.picturechoose.sourcelibrary.PictureChooser;

/**
 * ProjectName: PictureChoose
 * Describe: ͼƬѡ������������
 * Author: �ܽ���
 * Date: 2016/3/16 10:20
 * Email: jianchang1230@163.com
 * QQ: 939635660
 * Copyright (c) 2016, *******.com All Rights Reserved.
 */
public class MainActivity extends AppCompatActivity {

    ImageView imgAddPhoto;

    private String photoPath = null; //��¼ͷ���ַ��Ҫ�ϴ���ʱ����Խ���ַ�����ļ��ϴ�

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        setListeners();
    }


    private void findViews() {
        imgAddPhoto = (ImageView) findViewById(R.id.img_add_photo);
    }

    private void setListeners() {
        imgAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChoosePictureDialog();
            }
        });
    }

    PictureChooser pictureChooser;

    /**
     * ��ʾѡ��ͼƬ�Ĵ���
     */
    private void showChoosePictureDialog() {
        final String[] pictureFrom = {"�����ϴ�", "���ѡ��"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("��ѡ��ͼƬ��Դ");
        builder.setItems(pictureFrom, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (null == pictureChooser) {
                    pictureChooser = new PictureChooser(MainActivity.this);
                    //ͼƬ���ƣ������洢
                    pictureChooser.setCameraPicName("headPhoto.jpg");
                    //�Ƿ�ü����ü��ı����Ϳ��
                    pictureChooser.setIsClip(true, 1, 1, 0, 0);
                    //�Ƿ�ѹ����ѹ����������������λkb����ͼƬ���
                    pictureChooser.setIsCompressor(true, 500, 240, 240);
                }
                if (0 == which) {
                    //����ѡ�����
                    pictureChooser.setmPictureFrom(PictureChooser.PictureFrom.CAMERA);
                }
                if (1 == which) {
                    //����ѡ�����
                    pictureChooser.setmPictureFrom(PictureChooser.PictureFrom.GALLERY);
                }
                pictureChooser.execute(new PictureChooser.OnPicturePickListener() {
                    /**
                     * �����ǻ�ȡ��ͼƬ·��
                     * @param filePath ѡ�е�ͼƬ���ֻ��ļ�ϵͳ���·��
                     */
                    @Override
                    public void senFile(String filePath) {
                        //����ĳ��Լ���ͼƬ���ؿ�ܣ���ֻ�Ǽ���һ�°���
                        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                        imgAddPhoto.setImageBitmap(bitmap);
                        //��ͼƬ·���洢�������ϴ���ʱ��Ҫ�õ�
                        photoPath = filePath;
                    }

                    /**
                     * �������ѹ����������᷵��ѹ�����·��
                     * @param filePath ѡ�е�ͼƬ���ֻ��ļ�ϵͳ���·��
                     */
                    @Override
                    public void compressorSuccess(String filePath) {
                        photoPath = filePath;
                    }
                });
            }
        });
        builder.setNegativeButton("ȡ��", null);
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Ҫ��ҳ��ص�����Ӧ�����Ѳ��������ݴ���ͼƬѡ�������
        pictureChooser.onActivityResult(requestCode, resultCode, data);
    }
}
