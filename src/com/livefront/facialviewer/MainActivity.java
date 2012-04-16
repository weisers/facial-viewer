package com.livefront.facialviewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		try {
			InputStream is = getResources().openRawResource(
					R.raw.lbpcascade_frontalface);
			File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
			File cascadeFile = new File(cascadeDir,
					"lbpcascade_frontalface.xml");
			FileOutputStream os = new FileOutputStream(cascadeFile);

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			is.close();
			os.close();

			// Get the cascade classifier
			CascadeClassifier cascade = new CascadeClassifier(
					cascadeFile.getAbsolutePath());
			if (cascade.empty()) {
				Log.e(TAG, "Failed to load cascade classifier");
				cascade = null;
			} else
				Log.i(TAG,
						"Loaded cascade classifier from "
								+ cascadeFile.getAbsolutePath());

			cascadeFile.delete();
			cascadeDir.delete();

			//String filename = "face-recognition.jpg";
			String filename = "sean-200x200-bw.png";
			File imgFile = new File(Environment.getExternalStorageDirectory(),
					filename);

			if (imgFile.exists()) {
				Mat mRgba = Highgui.imread(imgFile.getAbsolutePath(),
						Highgui.CV_LOAD_IMAGE_COLOR);
				Mat mGray = Highgui.imread(imgFile.getAbsolutePath(),
						Highgui.CV_LOAD_IMAGE_GRAYSCALE);

				if (cascade != null) {
					int height = mGray.rows();
					int faceSize = Math.round(height * .5f);
					List<Rect> faces = new LinkedList<Rect>();
					cascade.detectMultiScale(mGray, faces, 1.1, 2, 2 // TODO:
																		// objdetect.CV_HAAR_SCALE_IMAGE
							, new Size(faceSize, faceSize));

					Log.d(TAG, String.format("Faces detected: %d", faces.size()));
					
					for (Rect r : faces)
						Core.rectangle(mRgba, r.tl(), r.br(), new Scalar(0,
								255, 0, 255), 3);
				}

				Log.d(TAG, String.format("Cols: %d; Rows: %d", mRgba.cols(), mRgba.rows()));
				Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(),
						Bitmap.Config.ARGB_8888);
				
				if (Utils.matToBitmap(mRgba, bmp)) {
					ImageView image = (ImageView) findViewById(R.id.photo);
					
					image.setImageBitmap(bmp);
				}
		        
			}

		} catch (IOException e) {

		}
	}
}