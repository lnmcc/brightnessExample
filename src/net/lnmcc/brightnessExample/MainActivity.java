package net.lnmcc.brightnessExample;

import net.lnmcc.brightnessExample.R;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private static final int REQUEST_CODE = 0;

	ImageView chosenImageView;
	ImageView alteredImageView;
	
	Bitmap alteredBitmap;
	Bitmap bmp;
	
	Button choosePicture;
	//亮度
	float brightness;

	//调节亮度的进度条
	SeekBar brightnessSB;
	
	Canvas canvas;
	Paint paint;
	ColorMatrix cm;
	Matrix matrix;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		alteredImageView = (ImageView) findViewById(R.id.AlteredImageView);
		chosenImageView = (ImageView) findViewById(R.id.ChooseImageView);
		choosePicture = (Button) findViewById(R.id.ChoosePictureButton);
		choosePicture.setOnClickListener(this);
		//变换矩阵，可以通过这个矩阵做很多图像缩放、平移和旋转等操作
		matrix = new Matrix();
		//颜色矩阵
		cm = new ColorMatrix();
		//画笔
		paint = new Paint();
		paint.setColorFilter(new ColorMatrixColorFilter(cm));

		brightnessSB = (SeekBar) findViewById(R.id.brightnessSB);
		brightnessSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						
						if (null != bmp) {
							//seekbar的范围为[0, 200]
							//brightness的范围为[-100, 100]
							brightness = (float) (progress - 100);
							cm.set(new float[] { 1, 0, 0, 0, brightness,
									             0, 1, 0, 0, brightness, 
									             0, 0,1, 0, brightness, 
									             0, 0, 0, 1, 0 });
							
							paint.setColorFilter(new ColorMatrixColorFilter(cm));
							canvas.drawBitmap(bmp, matrix, paint);
							alteredImageView.setImageBitmap(alteredBitmap);
						} else {
							brightnessSB.setProgress(100);
						}
					}
				});
	}

	@Override
	public void onClick(View v) {
		//请求Android的gallery程序，选取sdcard中的照片
		Intent choosePictureIntent = new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(choosePictureIntent, REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			
			brightnessSB.setProgress(100);
			Uri imageFileUri = data.getData();
			Display currentDisplay = getWindowManager().getDefaultDisplay();
			int dw = currentDisplay.getWidth();
			int dh = currentDisplay.getHeight() / 3 - 100;
			try {
				BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
				bmpFactoryOptions.inJustDecodeBounds = true;
				bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageFileUri), null, bmpFactoryOptions);
				
				int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight
						/ (int) dh);
				int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth
						/ (int) dw);

				if (heightRatio > 1 && widthRatio > 1) {
					if (heightRatio > widthRatio) {
						bmpFactoryOptions.inSampleSize = heightRatio;
					} else {
						bmpFactoryOptions.inSampleSize = widthRatio;
					}
				}
				bmpFactoryOptions.inJustDecodeBounds = false;
				bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageFileUri), null, bmpFactoryOptions);

				alteredBitmap = Bitmap.createBitmap(bmp.getWidth(),
						bmp.getHeight(), bmp.getConfig());

				canvas = new Canvas(alteredBitmap);
				canvas.drawBitmap(bmp, matrix, paint);

				alteredImageView.setImageBitmap(alteredBitmap);
				chosenImageView.setImageBitmap(bmp);

			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
