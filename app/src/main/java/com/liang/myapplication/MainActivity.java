package com.liang.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int SUCCESSCODE = 100;
    public Button mButton1;
    public Button mButton2;
    private String mPublicPhotoPath;
    private static final int REQ_GALLERY = 333;
    private static final int REQUEST_CODE_PICK_IMAGE = 222;
    public ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionsUtil.applyPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        PermissionsUtil.applyPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        PermissionsUtil.applyPermissions(this, Manifest.permission.CAMERA);
        mButton1 = ((Button) findViewById(R.id.bt1));
        mButton2 = ((Button) findViewById(R.id.bt2));
        mButton1.setOnClickListener(this);
        mButton2.setOnClickListener(this);
        mImageView = ((ImageView) findViewById(R.id.iv));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //获取相册中的照片
            case R.id.bt1:
                getImageFromAlbum();
                break;
            //拍照功能
            case R.id.bt2:
                startTake();
                break;
        }
    }

    /**
     * 获取相册中的图片
     */
    public void getImageFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    //拍照的功能
    private void startTake() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //判断是否有相机应用
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //创建临时图片文件
            File photoFile = null;
            try {
                photoFile = PictureUtils.createPublicImageFile();
                mPublicPhotoPath = photoFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //设置Action为拍照
            if (photoFile != null) {
                takePictureIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                //这里加入flag
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri photoURI = FileProvider.getUriForFile(this, "applicationId.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQ_GALLERY);
            }
        }
    }

    private Uri uri;
    String path;
    int mTargetW;
    int mTargetH;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mTargetW = mImageView.getWidth();
        mTargetH = mImageView.getHeight();

        switch (requestCode) {
            //拍照
            case REQ_GALLERY:
                if (resultCode != Activity.RESULT_OK) return;
                uri = Uri.parse(mPublicPhotoPath);
                path = uri.getPath();
                PictureUtils.galleryAddPic(mPublicPhotoPath, this);
                break;
            //获取相册的图片
            case REQUEST_CODE_PICK_IMAGE:
                if (data == null) return;
                uri = data.getData();
                int sdkVersion = Integer.valueOf(Build.VERSION.SDK);
                if (sdkVersion >= 19) {
                    path = this.uri.getPath();
                    path = PictureUtils.getPath_above19(MainActivity.this, this.uri);
                } else {
                    path = PictureUtils.getFilePath_below19(MainActivity.this, this.uri);
                }
                break;
        }
        mImageView.setImageBitmap(PictureUtils.getSmallBitmap(path, mTargetW, mTargetH));
    }
}

