package com.nathanhaze.exifremover;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

import com.google.android.gms.common.images.ImageManager;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.widget.ImageView;


public class ImageUtil {
	static final int BITMAP_WIDTH = 640;
	static final int BITMAP_HEIGHT = 480;
	private int[] highRGB;
	private int[] lowRGB;
	private int[] avgRGB;
	private Activity act;

	public ImageUtil(Activity act) {
		this.act = act;
		lowRGB = new int[3];// {139, 155, 0};
		highRGB = new int[3];// {109, 193, 39};
		avgRGB = new int[3];
	}
         
	public Bitmap rotateBitmp(Bitmap original, boolean landScape){
		/*
		Matrix matrix = new Matrix();
        Bitmap scaledBitmap;
		
		//Landscape
		if(landScape){
			matrix.postRotate(-90);
		}else{
			matrix.postRotate(90);
			
		}
		scaledBitmap = Bitmap.createScaledBitmap(original , original.getHeight(), 
				original.getWidth(),true);

		Bitmap newBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, 
				scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
		
		return newBitmap;
		*/
		
		Matrix matrix = new Matrix();

		matrix.postRotate(90);

		Bitmap scaledBitmap = Bitmap.createScaledBitmap(original ,original.getWidth(), original.getHeight(),true);

		return Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
	}

	public Bitmap rotateVertical(Bitmap original){
		Matrix matrix = new Matrix();
        Bitmap scaledBitmap;

	   matrix.postRotate(180);
			

		scaledBitmap = Bitmap.createScaledBitmap(original , original.getHeight(), 
				original.getWidth(),true);

		Bitmap newBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, 
				scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
		
		return newBitmap;
	}
	
	public boolean bitmapIsLandscape(Bitmap image){
		int width = image.getWidth();
		int height = image.getHeight();
		if(width> height){
			return true;
		}
		else 
			return false;
	}

	public Bitmap getBitmapFromPhotoUrl(File saveToFile, String remoteUrl) {
		// from SD cache
		Bitmap b = decodeFileAsBitmap(saveToFile);
		if (b != null)
			return b;

		// from web
		try {
			Bitmap bitmap = null;
			InputStream is = new URL(remoteUrl).openStream();
			OutputStream os = new FileOutputStream(saveToFile);
			CopyStream(is, os);
			os.close();
			bitmap = decodeFileAsBitmap(saveToFile);
			return bitmap;
		} catch (Exception ex) {
			return null;
		}
	}

	public void setChromaKeys(int[] low, int[] high) {
		highRGB = high;
		lowRGB = low;
		System.out.println("Low array:" + Arrays.toString(lowRGB));
		System.out.println(" High array :" + Arrays.toString(highRGB));
	}

	public void retrievePhotoFromUrl(File saveToFile, String remoteUrl) {

		// from web
		try {
			InputStream is = new URL(remoteUrl).openStream();
			OutputStream os = new FileOutputStream(saveToFile);
			CopyStream(is, os);
			os.close();
		} catch (Exception ex) {
		}
	}

	public Bitmap decodeFileAsBitmap(String fileUrl) {
		return null; //implement
	}

	public Bitmap decodeFileAsBitmap(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 70;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale++;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	public Bitmap decodeByteArrayAsBitmap(byte[] d) {
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(d, 0, d.length, opts);
		opts.inJustDecodeBounds = false;
		opts.inSampleSize = opts.outWidth / BITMAP_WIDTH;
		Bitmap bitmap = BitmapFactory.decodeByteArray(d, 0, d.length, opts);
		return bitmap;
	}

	public Uri insertImageToMedia(Context context, Bitmap bmp) {
		return Uri.parse(android.provider.MediaStore.Images.Media.insertImage(
				context.getContentResolver(), bmp, null, null));
	}

	public Uri insertImageToMedia(Context context, String title, byte[] img) {
		ContentValues values = new ContentValues();
		values.put(MediaColumns.TITLE, title);
		values.put(ImageColumns.BUCKET_ID, title);
		values.put(ImageColumns.DESCRIPTION, "");
		values.put(MediaColumns.MIME_TYPE, "image/jpeg");

		ContentResolver resolver = context.getContentResolver();
		Uri uri = resolver.insert(Media.EXTERNAL_CONTENT_URI, values);

		OutputStream outstream;
		try {
			outstream = resolver.openOutputStream(uri);
			outstream.write(img);
			outstream.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return uri;
	}

	public Intent getCropImageIntent(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scaleUpIfNeeded", true);
		intent.putExtra("return-data", true);
		return intent;
	}

	public Intent getCropImageIntent(Context context, byte[] d) {
		Bitmap bmp = decodeByteArrayAsBitmap(d);
		Uri uri = insertImageToMedia(context, bmp);
		return getCropImageIntent(uri);
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}



	private int[] getAverageRGB() {
		int[] arr = new int[3];
		arr[0] = (this.highRGB[0] + this.lowRGB[0]) / 2;
		arr[1] = (this.highRGB[1] + this.lowRGB[1]) / 2;
		arr[2] = (this.highRGB[2] + this.lowRGB[2]) / 2;

		System.out.println("running avg:" + Arrays.toString(avgRGB));
		System.out.println("simple avg:" + Arrays.toString(arr));
		return avgRGB;

	}

	private double getPixelDistance(int r, int g, int b, int[] refRGB) {
		double dist = 0.0;
		dist = Math.sqrt(Math.pow(r - refRGB[0], 2)
				+ Math.pow(g - refRGB[1], 2) + Math.pow(b - refRGB[2], 2));
		return dist;
	}

/*
	public Bitmap composeGreenScreenDistanceMethod(Bitmap src, Bitmap bg) {
		Bitmap fgCopy = src.copy(src.getConfig(), true);
		fgCopy = Bitmap.createScaledBitmap(fgCopy, targetWidth, targetHeight,
				false);
		// get some sample green pixels
		//getKeySample(fgCopy);
		long startTime = System.currentTimeMillis();
		Bitmap result = Bitmap.createBitmap(fgCopy.getWidth(),
				fgCopy.getHeight(), fgCopy.getConfig());
		int r, g, b, pixel, bgPixel;
		int bg_r, bg_g, bg_b;
		int result_r, result_b, result_g;
		int[] refRGB = getAverageRGB();
		for (int x = 0; x < fgCopy.getWidth(); x++) {
			for (int y = 0; y < fgCopy.getHeight(); y++) {
				pixel = fgCopy.getPixel(x, y);
				bgPixel = bg.getPixel(x, y);
				
				g = Color.green(pixel);
				r = Color.red(pixel);
				b = Color.blue(pixel);
				// bg rgb
				bg_g = Color.green(bgPixel);
				bg_r = Color.red(bgPixel);
				bg_b = Color.blue(bgPixel);
				double dist = this.getPixelDistance(r, g, b, refRGB);
				dist = Math.min(dist, 100f);
				// Hue Saturation Lightness
				double[] HSL = this.convertRGBToHSL(r, g, b);
				double translucency = getTranslucency(HSL);
				result_r = (int) (translucency * r + (1 - translucency) * bg_r);
				result_g = (int) (translucency * g + (1 - translucency) * bg_g);
				result_b = (int) (translucency * b + (1 - translucency) * bg_b);
				int resultPixel = Color.rgb(result_r, result_g, result_b);
				result.setPixel(x, y, resultPixel);
			}
		}
		return result;
	}
	
*/


	/*
	 * WARNING: accept angle should never be set greater than "somewhat less
	 * than 90 degrees" to avoid dealing with negative/infinite tg. In reality,
	 * 80 degrees should be enough if foreground is reasonable. If this seems to
	 * be a problem, go to alternative ways of checking point position (scalar
	 * product or line equations). This angle should not be too small either to
	 * avoid infinite ctg (used to suppress foreground without use of division)
	 */

	private class ChromaKey {
		private final int angle = 80;
		int R, G, B;
		double Y, Cb, Cr;
		double k_cos_angle;
		double k_sin_angle;
		double k_tan_angle;
		double k_ctan_angle;
		double cb_cr_dist;
		double one_over_key;
		double yscale;
		double kg;
		double noise_lvl;

		ChromaKey(int r, int g, int b) {
			R = r;
			G = g;
			B = b;
			// use 6 pixels for now
			noise_lvl = 3;
			init();
		}

		private void init() {
			initRGB2Y();
			k_cos_angle = Math.cos(Math.PI * angle / 180);
			k_sin_angle = Math.sin(Math.PI * angle / 180);
			double temp = 0xF * Math.tan(Math.PI * angle / 180);
			if (temp > 0xFF) {
				temp = 0xFF;
			}
			k_tan_angle = temp;

			temp = 0xF / Math.tan(Math.PI * angle / 180);
			if (temp > 0xFF) {
				temp = 0xFF;
			}
			k_ctan_angle = temp;
			temp = 1.0 / cb_cr_dist;
			one_over_key = 0xFF * 2 * temp - 0xFF;

			temp = (0xF * Y) / cb_cr_dist;
			if (temp > 0xFF) {
				temp = 0xFF;
			}
			yscale = temp;

			if (cb_cr_dist > 127) {
				cb_cr_dist = 127;
			}
			kg = cb_cr_dist;

		}

		// function to convert RGB y Y color
		private void initRGB2Y() {
			double tmp1, tmp2;
			Y = 0.257 * R + 0.504 * G + 0.098 * B;
			tmp1 = -0.148 * R - 0.291 * G + 0.439 * B;
			tmp2 = 0.439 * R - 0.368 * G - 0.071 * B;

			cb_cr_dist = Math.sqrt(tmp1 * tmp1 + tmp2 * tmp2);
			Cb = 127 * (tmp1 / cb_cr_dist);
			Cr = 127 * (tmp2 / cb_cr_dist);
		}

	}

	/*
	private double getTranslucency(double[] hsl) {
		float targetHue = 1.0f / 3.0f;
		float tolerance = 30; // ImageManager.getInstance().getChromakeyThreshold();

		// compute the translucency
		double deltaHue = Math.abs(hsl[0] - targetHue);
		double translucency = (deltaHue / tolerance);
		translucency = Math.min(translucency, 1f);
		return translucency;
	}
*/
	private float[] convertRGBtoHSL(int r, int g, int b) {

		float H = 0;
		float S = 0;
		float L = 0;

		float cr = r / 255.0f;
		float cg = g / 255.0f;
		float cb = b / 255.0f;

		float delta_R, delta_G, delta_B;

		float min = cr;
		if (cg < min) {
			min = cg;
		}
		if (cb < min) {
			min = cb;
		}

		float max = cr;
		if (cg > max) {
			max = cg;
		}
		if (cb > max) {
			max = cb;
		}

		if (max == min) {
			H = 0;
			S = 0;
		} else {
			float delta = max - min;
			if (L > 0.5) {
				S = delta / (2 - max - min);
			} else {
				S = delta / (max + min);
			}

			if (cr == max) {
				H = (cg - cb) / delta + (g < b ? 6 : 0);

			} else if (cg == max) {
				H = (cb - cr) / delta + 2;

			} else if (cb == max) {
				H = (cr - cg) / delta + 4;
			}

			H /= 6.0f;

		}

		float[] HSL = { H, S, L };
		return HSL;

	}
/*
	private double[] convertRGBToHSL(int R, int G, int B) {
		double r = R / 255.0;
		double g = G / 255.0;
		double b = B / 255.0;
		// double[] HSL;
		double v;
		double m;
		double vm;
		double r2, g2, b2;
		double h, s, l;
		h = 0; // default to black
		s = 0;
		l = 0;
		v = Math.max(r, g);
		v = Math.max(v, b);
		m = Math.min(r, g);
		m = Math.min(m, b);

		l = (m + v) / 2.0;
		if (l <= 0.0) {
			double[] HSL = { h, s, l };
			return HSL;
		}
		vm = v - m;
		s = vm;
		if (s > 0.0) {
			s /= (l <= 0.5) ? (v + m) : (2.0 - v - m);
		} else {
			double[] HSL = { h, s, l };
			return HSL;
		}
		r2 = (v - r) / vm;
		g2 = (v - g) / vm;
		b2 = (v - b) / vm;
		if (r == v) {
			h = (g == m ? 5.0 + b2 : 1.0 - g2);
		} else if (g == v) {
			h = (b == m ? 1.0 + r2 : 3.0 - b2);
		} else {
			h = (r == m ? 3.0 + g2 : 5.0 - r2);
		}
		h /= 6.0;

		double[] HSL = { h, s, l };
		return HSL;
	}
*/
	//private final int targetWidth = 1024;
	//private final int targetHeight = 768;


	// function to test if the pixel is in the sample range
	private boolean isPixelInRange(int r, int g, int b) {
		if ((r >= lowRGB[0] && r <= highRGB[0])
				&& (g >= lowRGB[1] && g <= highRGB[1])
				&& (b >= lowRGB[2] && b <= highRGB[2])) {
			return true;
		}
		return false;
	}

	private final int SAMPLE_WIDTH = 25;
	private final int SAMPLE_HEIGHT = 50;

	private void getKeySample(Bitmap pic, int expand) {

	   Bitmap result = Bitmap.createBitmap(pic.getWidth(), pic.getHeight(), Bitmap.Config.ARGB_8888);


		// System.out.println("starting getting key sample:");
		int[] lowestRGB = { 255, 255, 255 };
		int[] highestRGB = { 0, 0, 0 };
		int r, g, b, pixel;
		long sum_r = 0;
		long sum_g = 0;
		long sum_b = 0;
		int pixCount = 0;
		// this is for the top loop, take sample of the first 50 rows from the
		// top

		for (int y = 0; y < SAMPLE_HEIGHT; y++) {
			for (int x = 0; x < pic.getWidth(); x++) {
				pixel = pic.getPixel(x, y);
				g = Color.green(pixel);
				r = Color.red(pixel);
				b = Color.blue(pixel);
				if (r < lowestRGB[0]) {
					lowestRGB[0] = r;
				}
				if (g < lowestRGB[1]) {
					lowestRGB[1] = g;
				}
				if (b < lowestRGB[2]) {
					lowestRGB[2] = b;
				}
				// System.out.println("r: "+r+" g:"+g+" b:"+b);

				// Set value for highest
				if (r > highestRGB[0]) {
					highestRGB[0] = r;
				}
				if (g > highestRGB[1]) {
					highestRGB[1] = g;
				}
				if (b > highestRGB[2]) {
					highestRGB[2] = b;
				}

				sum_r += r;
				sum_g += g;
				sum_b += b;
				pixCount++;

			//   result.setPixel(x, y, Color.RED);

			}
		}

		// /this is the left loop, take the sample of the 100 left columns

		for (int x = 0; x < SAMPLE_WIDTH; x++) {
			for (int y = 0; y < pic.getHeight(); y++) {
				pixel = pic.getPixel(x, y);
				g = Color.green(pixel);
				r = Color.red(pixel);
				b = Color.blue(pixel);

				if (r < lowestRGB[0]) {
					lowestRGB[0] = r;
				}
				if (g < lowestRGB[1]) {
					lowestRGB[1] = g;
				}
				if (b < lowestRGB[2]) {
					lowestRGB[2] = b;
				}
				// System.out.println("r: "+r+" g:"+g+" b:"+b);

				// Set value for highest
				if (r > highestRGB[0]) {
					highestRGB[0] = r;
				}
				if (g > highestRGB[1]) {
					highestRGB[1] = g;
				}
				if (b > highestRGB[2]) {
					highestRGB[2] = b;
				}
				// conpute sum to get the average
				sum_r += r;
				sum_g += g;
				sum_b += b;
				pixCount++;
			//	result.setPixel(x, y, Color.RED);

			}
		}

		// This is the right loop, take the samples of the 100 right columns
		for (int x = pic.getWidth() - 1; x > pic.getWidth() - SAMPLE_WIDTH; x--) {
			for (int y = 0; y < pic.getHeight(); y++) {
				pixel = pic.getPixel(x, y);
				g = Color.green(pixel);
				r = Color.red(pixel);
				b = Color.blue(pixel);
				if (r < lowestRGB[0]) {
					lowestRGB[0] = r;
				}
				if (g < lowestRGB[1]) {
					lowestRGB[1] = g;
				}
				if (b < lowestRGB[2]) {
					lowestRGB[2] = b;
				}
				// System.out.println("r: "+r+" g:"+g+" b:"+b);

				// Set value for highest
				if (r > highestRGB[0]) {
					highestRGB[0] = r;
				}
				if (g > highestRGB[1]) {
					highestRGB[1] = g;
				}
				if (b > highestRGB[2]) {
					highestRGB[2] = b;
				}

				sum_r += r;
				sum_g += g;
				sum_b += b;
				pixCount++;
			//	result.setPixel(x, y, Color.RED);

			}
		}
		avgRGB[0] = (int) (sum_r / pixCount);
		avgRGB[1] = (int) (sum_g / pixCount);
		avgRGB[2] = (int) (sum_b / pixCount);
		highRGB = highestRGB;
		lowRGB = lowestRGB;
		
	}

	public Bitmap composeGreenScreen(Bitmap src, int tolb) {
		Bitmap fgImage = src.copy(src.getConfig(), true);
		Bitmap result = Bitmap.createBitmap(fgImage.getWidth(), fgImage.getHeight(), Bitmap.Config.ARGB_8888);

		int r, g, b, pixel, bgPixel, bg_r, bg_g, bg_b, res_r, res_g, res_b;

		int tola= 1;
		
		//take a sample of the right, left , and top
		//place them in a class array with lowest and highest RGB
		 this.getKeySample(fgImage, tola);
		
		/*
		 * The difference between YCbCr and RGB is that YCbCr represents color 
		 * as brightness and two color difference signals, while RGB represents 
		 * color as red, green and blue. In YCbCr, the Y is the brightness (luma), 
		 * Cb is blue minus luma (B-Y) and Cr is red minus luma (R-Y)
		 */
		YBCRcolor keyColor = new YBCRcolor(avgRGB[0], avgRGB[1], avgRGB[2]);

		double mask = 0;

		count =0;
		for (int x =0; x < fgImage.getWidth(); x++) {
			for (int y = 0; y < fgImage.getHeight(); y++) {

				pixel = fgImage.getPixel(x, y);
				//bgPixel = bg.getPixel(x, y);
				g = Color.green(pixel);
				r = Color.red(pixel);
				b = Color.blue(pixel);
				YBCRcolor pixColor = new YBCRcolor(r, g, b);
			    mask = getColorDist(pixColor, keyColor, 0.4f,tolb);
				mask = 1 - mask;
			
				if(mask == 0){
					result.setPixel(x, y,pixel);						
				}else{
				     result.setPixel(x, y, Color.TRANSPARENT);	
			     }

			}
		}

		Bitmap test = Bitmap.createBitmap(fgImage.getWidth(), fgImage.getHeight(), Bitmap.Config.ARGB_8888);

		if(tola ==0){
	    test = SobelEdgeDetect(result);
	    result = test;
		}

		return result;
	}
	
	private boolean hueInRange(Color compare, float range){
		
		float[] hsvAVG = new float[3];
		
		Color.RGBToHSV(avgRGB[0], avgRGB[1], avgRGB[2], hsvAVG);
		return false;
	}
	
	public double hueDifference(int compare){
		float[] hueA = new float[3];
		float[] hueB = new float[3];

		Color.colorToHSV(compare, hueA);
		Color.colorToHSV(getIntFromColor(avgRGB[0], avgRGB[1], avgRGB[2]), hueB);
		
		double distance = hueA[0] - hueB[1];

		return  Math.abs(distance);
	}
	
	
	private Bitmap SobelEdgeDetect(Bitmap b)
	{
		    Bitmap bb = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Bitmap.Config.ARGB_8888);
	        int width = b.getWidth();
	        int height = b.getHeight();
	        int[][] gx = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
	        int[][] gy = { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } };

	        int[][] allPixR = new int[width] [height];
	        int[][] allPixG = new int[width] [height];
	        int[][] allPixB = new int[width] [height];

	        int limit = 128 * 128;

	        for (int i = 0; i < width; i++)
	        {
	            for (int j = 0; j < height; j++)
	            {
	            	int px = b.getPixel(i, j);
	                allPixR[i] [j] = Color.red(px);
	                allPixG[i] [j] = Color.green(px);
	                allPixB[i] [j] = Color.blue(px);
	            }
	        }

	        int new_rx = 0, new_ry = 0;
	        int new_gx = 0, new_gy = 0;
	        int new_bx = 0, new_by = 0;
	        int rc, gc, bc;
	        for (int i = 1; i < b.getWidth() - 1; i++)
	        {
	            for (int j = 1; j < b.getHeight() - 1; j++)
	            {

	                new_rx = 0;
	                new_ry = 0;
	                new_gx = 0;
	                new_gy = 0;
	                new_bx = 0;
	                new_by = 0;
	                rc = 0;
	                gc = 0;
	                bc = 0;

	                for (int wi = -1; wi < 2; wi++)
	                {
	                    for (int hw = -1; hw < 2; hw++)
	                    {
	                        rc = allPixR[i + hw] [j + wi];
	                        new_rx += gx[wi + 1] [hw + 1] * rc;
	                        new_ry += gy[wi + 1] [hw + 1] * rc;

	                        gc = allPixG[i + hw] [j + wi];
	                        new_gx += gx[wi + 1] [hw + 1] * gc;
	                        new_gy += gy[wi + 1] [hw + 1] * gc;

	                        bc = allPixB[i + hw] [j + wi];
	                        new_bx += gx[wi + 1] [hw + 1] * bc;
	                        new_by += gy[wi + 1] [hw + 1] * bc;
	                    }
	                }
	                if (new_rx * new_rx + new_ry * new_ry > limit || new_gx * new_gx + new_gy * new_gy > limit || new_bx * new_bx + new_by * new_by > limit)
	                    bb.setPixel(i, j, Color.BLACK);

	                //bb.SetPixel (i, j, Color.FromArgb(allPixR[i,j],allPixG[i,j],allPixB[i,j]));
	                else
	                    bb.setPixel(i, j, Color.TRANSPARENT);
	            }
	        }
	        return bb;

	  }
	

	public int getIntFromColor(int Red, int Green, int Blue){
	    Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
	    Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
	    Blue = Blue & 0x000000FF; //Mask out anything not blue.

	    return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
	}
	
	public boolean isIncluded( int pixel) {
		
	    int rT = avgRGB[0];
	    int gT = avgRGB[1];
	    int bT = avgRGB[2];
	    int rP = Color.red(pixel);
	    int gP = Color.green(pixel);
	    int bP = Color.blue(pixel);	 

		YBCRcolor keyColor = new YBCRcolor(avgRGB[0], avgRGB[1], avgRGB[2]);

	    double mask = 0;
		int g = Color.green(pixel);
		int r = Color.red(pixel);
		int b = Color.blue(pixel);
			YBCRcolor pixColor = new YBCRcolor(r, g, b);
			mask = getColorDist(pixColor, keyColor, 0.4f,30);// ImageManager.getInstance().getToleranceA());
			mask = 1 - mask;
	
			if(mask == 0){
				 return false;					
			}else{
			     return true; //make it transparent
			}
			
			
	  //  boolean transparent = (pixel & 0xff000000) == 0x0;
	  //  return !transparent;
	}
	

	 public Bitmap FloodFillV2(Bitmap unfilter,  Bitmap key, Point node) {
		    int width = key.getWidth();
		    int height = key.getHeight();
		    
		   // Bitmap image = org.copy(key.getConfig(), true);
		    Bitmap image = key.copy(key.getConfig(), true);
			this.getKeySample(unfilter, 30);// ImageManager.getInstance().getToleranceA());
			
			int max = (key.getWidth() * key.getHeight() )/4;
			if(isIncluded(image.getPixel(node.x, node.y))){
		      Deque<Point> queue = new LinkedList<Point>();
		      do {
		        int x = node.x;
		        int y = node.y;
		        if(count >max)break;
		        while (x > 0 && isIncluded(image.getPixel(x - 1, y))) {
		          x--;
		        }
		        boolean spanUp = false;
		        boolean spanDown = false;
		        while (x < width && isIncluded(image.getPixel(x, y))) {
		        	//image.setPixel(x, y, background.getPixel(x, y));
		        	image.setPixel(x, y, Color.TRANSPARENT);
		          if (!spanUp && y > 0 && isIncluded(image.getPixel(x, y - 1))) {
		            queue.add(new Point(x, y - 1));
		            spanUp = true;
		          } else if (spanUp && y > 0 && !isIncluded(image.getPixel(x, y - 1))) {
		            spanUp = false;
		          }
		          if (!spanDown && y < height - 1 && isIncluded(image.getPixel(x, y + 1))) {
		            queue.add(new Point(x, y + 1));
		            spanDown = true;
		          } else if (spanDown && y < height - 1 && !isIncluded(image.getPixel(x, y + 1))) {
		            spanDown = false;
		          }
		          x++;
		        }
		      } while ((node = queue.pollFirst()) != null);
		    }	 
			
		return image;
	 }

	 
	public Bitmap FloodFill(Bitmap org, Bitmap unfilter, Bitmap background, Bitmap key,  Point pt, int replacementColor) 
	{
		
		Bitmap bmp =  org.copy(org.getConfig(), true);
	    this.getKeySample(unfilter, 30);//ImageManager.getInstance().getToleranceA());
	    	    
	    int  px = bmp.getPixel(pt.x, pt.y);
		int r = Color.red(px);
		int g = Color.green(px);
		int b = Color.blue(px);

	    int tolerance = 30;// ImageManager.getInstance().getToleranceA() * 2;
	    Queue<Point> q = new LinkedList<Point>();
	    q.add(pt);
	    while (q.size() > 0) {
	    	if(q.size() > 15000) break;
	    	count++;
	        Point n = q.poll();
	        if (!isIncluded(bmp.getPixel(n.x, n.y)))
	            continue;

	        Point w = n, e = new Point(n.x + 1, n.y);
	        while ((w.x > 0) && isIncluded(bmp.getPixel(w.x, w.y))) {
	        	bmp.setPixel(w.x, w.y, background.getPixel(w.x, w.y));
	            if ((w.y > 0) && (isIncluded( bmp.getPixel(w.x, w.y - 1))))
	                q.add(new Point(w.x, w.y - 1));
	            if ((w.y < bmp.getHeight() - 1)
	                    && (isIncluded(bmp.getPixel(w.x, w.y + 1))))
	                q.add(new Point(w.x, w.y + 1));
	            w.x--;
	        }
	        while ((e.x < bmp.getWidth() - 1)
	                && (isIncluded(bmp.getPixel(e.x, e.y)))){

	            if ((e.y > 0) && (isIncluded(bmp.getPixel(e.x, e.y - 1))))
	                q.add(new Point(e.x, e.y - 1));
	            if ((e.y < bmp.getHeight() - 1)
	                    && (isIncluded(bmp.getPixel(e.x, e.y + 1))))
	                q.add(new Point(e.x, e.y + 1));
	            e.x++;
	        }
	    }
	    return bmp;
	}
	
	public boolean colorInRange(int r, int g, int b, float tolerance){
		float tolHigh = tolerance + 1f;
		float tolLow = 1f - tolerance;

		if((highRGB[0] * tolHigh > r) && (lowRGB[0] * tolLow < r)){
			if((highRGB[1] * tolHigh > g) && (lowRGB[1] * tolLow < g)){
				if((highRGB[2] * tolHigh  > b) && (lowRGB[2] * tolLow <  b)){					
					return true;
				}
			}
		}
		return false;
	}
	
	
	public Bitmap composeBackground(Bitmap fgImg, Bitmap bgImg){
		Bitmap bgCopy = bgImg.copy(bgImg.getConfig(), true);
		Canvas canvas = new Canvas(bgCopy);
		canvas.drawBitmap(fgImg, 0f, 0f, null);
		return bgCopy;
	}

	public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    float scaleWidth = ((float) newWidth) / width;
	    float scaleHeight = ((float) newHeight) / height;
	    // CREATE A MATRIX FOR THE MANIPULATION
	    Matrix matrix = new Matrix();
	    // RESIZE THE BIT MAP
	    matrix.postScale(scaleWidth, scaleHeight);

	    // "RECREATE" THE NEW BITMAP
	    Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	    return resizedBitmap;
	}
	
	
	private class YBCRcolor {
		int Y;
		int Cb;
		int Cr;

		YBCRcolor(int r, int g, int b) {
			Y = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
			Cb = (int) Math.round(128 + -0.168736 * r - 0.331264 * g + 0.5 * b);
			Cr = (int) Math.round(128 + 0.5 * r - 0.418688 * g - 0.081312 * b);

		}
		

	}

	/**
	 * 
	 close region: is when distance is less than tola, and is all background
	 * Far region: is the foreground middle region: is the between region of
	 * tola and tolb
	 * far region: is when the pixel belongs to the fg
	 */
	//float baseThreshold = 9.9f;

	int count; 
	private double getColorDist(YBCRcolor pixel, YBCRcolor key, float tolA,int tolB) {
		//float tola = baseThreshold + threshold;
		//float tolb = 14f;
		
	
		double temp = Math.sqrt((key.Cb - pixel.Cb) * (key.Cb - pixel.Cb)
				+ (key.Cr - pixel.Cr) * (key.Cr - pixel.Cr));
		
		if (temp <= tolA) {
			count ++;
			return 0.0;  // the pixel is keyed out
		}
		if (temp < tolB && temp > tolA) {

			return (temp - tolA) / (tolA - tolB); // the pixel is keyed out since it returns 0
		}

		return 1.0;	  
	}

	private final int sw = 400;
	private final int sh = 400;

	public void setChromaKey(Bitmap keySample) {
		// Bitmap result=Bitmap.createBitmap(keySample);

		int w = keySample.getWidth();
		int h = keySample.getHeight();
		int startX = (w - sw) / 2;
		int startY = (h - sh) / 2;

		int[] lowestRGB = { 255, 255, 255 };
		int[] highestRGB = { 0, 0, 0 };
		int r, g, b;
		int px;
		for (int x = startX; x < startX + sw; x++) {

			for (int y = startY; y < startY + sh; y++) {
				px = keySample.getPixel(x, y);
				r = Color.red(px);
				g = Color.green(px);
				b = Color.blue(px);
				if (r < lowestRGB[0]) {
					lowestRGB[0] = r;
				}
				if (g < lowestRGB[1]) {
					lowestRGB[1] = g;
				}
				if (b < lowestRGB[2]) {
					lowestRGB[2] = b;
				}
				//System.out.println("r: " + r + " g:" + g + " b:" + b);

				// Set value for highest
				if (r > highestRGB[0]) {
					highestRGB[0] = r;
				}
				if (g > highestRGB[1]) {
					highestRGB[1] = g;
				}
				if (b > highestRGB[2]) {
					highestRGB[2] = b;
				}

			}
		}
		//ImageManager.getInstance().saveChromaKey(lowestRGB, highestRGB);
		System.out.println("lowest array:" + Arrays.toString(lowestRGB)
				+ " highest array :" + Arrays.toString(highestRGB));
	}

	// Start method computeImage set colors for the first Image. 
	public Bitmap computeImage1(Bitmap result, int grayAmount, int pX, int pY) {
	int red;
	int green;
	int blue;

	if (grayAmount < 128) {
	red = 100;
	green = 230;
	blue = 34;
	} else {
	red = 200;
	green = 105;
	blue = 80;
	} 

	// restore the color value of the pixel

	int pixel = Color.rgb(red,  green,  blue);
    result.setPixel(pX, pY, pixel);
    return result;
	} // End method
	// Start method computeImage2 set colors for the second Image.
	public Bitmap computeImage2(Bitmap result, int grayAmount, int pX, int pY) {
	int red;
	int green;
	int blue;

	if (grayAmount < 128) {
	red = 100;
	green = 50;
	blue = 250;
	} else {
	red = 0;
	green = 0;
	blue = 0;
	}

	int pixel = Color.rgb(red,  green,  blue);
    result.setPixel(pX, pY, pixel);
    return result;
	} //End Method 
	// Start method computeImage3 set colors for the third Image.
	public Bitmap computeImage3(Bitmap result, int grayAmount, int pX, int pY) {
	int red;
	int green;
	int blue;

	if (grayAmount < 128) {
	red = 100;
	green = 50;
	blue = 250;
	} else {
	red = 10;
	green = 255;
	blue = 127;
	}

	int pixel = Color.rgb(red,  green,  blue);
    result.setPixel(pX, pY, pixel);
    return result;
	} //End method 
	
	// Start method computeImage4 set colors for the fourth Image.
	public Bitmap computeImage4(Bitmap result, int grayAmount, int pX, int pY) {
	int red;
	int green;
	int blue;

	if (grayAmount < 128) {
	red = 255;
	green = 254;
	blue = 102;
	} else {
	red = 177;
	green = 101;
	blue = 255;
	}

	int pixel = Color.rgb(red,  green,  blue);
    result.setPixel(pX, pY, pixel);
    return result;
	} //End method 
	// Start method computeImage5 set colors for the Five Image.
	public Bitmap computeImage5(Bitmap result, int grayAmount, int pX, int pY) {
	int red;
	int green;
	int blue;

	if (grayAmount < 128) {
	red = 254;
	green = 103;
	blue = 103;
	} else {
	red = 104;
	green = 254;
	blue = 104;
	}

	int pixel = Color.rgb(red,  green,  blue);
    result.setPixel(pX, pY, pixel);
    return result;
	} //End method 
	
	// Start method computeImage6 set colors for the six Image.
	public Bitmap computeImage6(Bitmap result, int grayAmount, int pX, int pY) {
		int red;
		int green;
		int blue;

		if (grayAmount < 128) {
			red = 255;
			green = 27;
			blue = 139;
	    } else {
			red = 104;
			green = 104;
			blue = 254;
	   }

		int pixel = Color.rgb(red,  green,  blue);
	    result.setPixel(pX, pY, pixel);
	    return result;
	} //End method 

	

	public ArrayList<Point> NoiseFinder(Bitmap key, Bitmap unfilter, Point node) {
		
	    int width = key.getWidth();
	    int height = key.getHeight();
	    
	    Bitmap image = key.copy(key.getConfig(), true);
	    
		this.getKeySample(unfilter, 30);// ImageManager.getInstance().getToleranceA());
		
		int count =0;
		
         ArrayList<Point> pointList = new ArrayList<Point>();
		 if(isIncluded(image.getPixel(node.x, node.y))){
	      Deque<Point> queue = new LinkedList<Point>();
	      do {
	        int x = node.x;
	        int y = node.y;
	        count ++;
	        if(count >1500)break;
	        while (x > 0 && isIncluded(image.getPixel(x - 1, y))) {
	          x--;
	        }
	        boolean spanUp = false;
	        boolean spanDown = false;
	        while (x < width && isIncluded(image.getPixel(x, y))) {
	        	pointList.add(new Point(x,y));
	          if (!spanUp && y > 0 && isIncluded(image.getPixel(x, y - 1))) {
	            queue.add(new Point(x, y - 1));
	            spanUp = true;
	          } else if (spanUp && y > 0 && !isIncluded(image.getPixel(x, y - 1))) {
	            spanUp = false;
	          }
	          if (!spanDown && y < height - 1 && isIncluded(image.getPixel(x, y + 1))) {
	            queue.add(new Point(x, y + 1));
	            spanDown = true;
	          } else if (spanDown && y < height - 1 && !isIncluded(image.getPixel(x, y + 1))) {
	            spanDown = false;
	          }
	          x++;
	        }
	      } while ((node = queue.pollFirst()) != null);
	    }	 		 		
		return pointList;
	}
	
	public final static float[] getPointerCoords(ImageView view, MotionEvent e)
	{
	    final int index = e.getActionIndex();
	    final float[] coords = new float[] { e.getX(index), e.getY(index) };
	    Matrix matrix = new Matrix();
	    view.getImageMatrix().invert(matrix);
	    matrix.postTranslate(view.getScrollX(), view.getScrollY());
	    matrix.mapPoints(coords);
	    return coords;
	}
	
	public final static int[] getPointerCoords(ImageView view, DragEvent e)
	{
	    final float[] coords = new float[] { e.getX(), e.getY() };
	    Matrix matrix = new Matrix();
	    view.getImageMatrix().invert(matrix);
	    matrix.postTranslate(view.getScrollX(), view.getScrollY());
	    matrix.mapPoints(coords);
	    
	    int[] temp = {Math.round(coords[0]), Math.round(coords[1]) };
	    return temp;
	}
	
	 public Bitmap addOverlayDiffSize(Bitmap larger, Bitmap smaller) { // bmp1 is larger
	        Bitmap bmOverlay = Bitmap.createBitmap(larger.getWidth(), larger.getHeight(), larger.getConfig());
	        Canvas canvas = new Canvas(bmOverlay);
	        int x = (larger.getWidth() - smaller.getWidth())/2;
	        int y = larger.getHeight() - smaller.getHeight();
	        canvas.drawBitmap(smaller, x, y, null);
	        canvas.drawBitmap(larger, new Matrix(), null);
	        return bmOverlay;
	}
	 
	 public Bitmap moveKeyedImage(Bitmap larger, Bitmap smaller, int x, int y) { // bmp1 is larger
	        Bitmap bmOverlay = Bitmap.createBitmap(larger.getWidth(), larger.getHeight(), larger.getConfig());
	        Canvas canvas = new Canvas(bmOverlay);
	        canvas.drawBitmap(larger, new Matrix(), null);

	        canvas.drawBitmap(smaller, x, y, null);
	        return bmOverlay;
	}
	 
	 public String saveBitmap(String type, Bitmap image){
		 String path = "";
		 File imageFile = getOutputMediaFile(1, type );
		 FileOutputStream out = null;
		 
		 try {
			 path = imageFile.getAbsolutePath();
		     out = new FileOutputStream(path);
		     image.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
		     // PNG is a lossless format, the compression factor (100) is ignored
		 } catch (Exception e) {
		     e.printStackTrace();
		     return path;
		 } finally {
		     try {
		         if (out != null) {
		             out.close();
		         }
		     } catch (IOException e) {
		         e.printStackTrace();
		         return path;
		     }
		 }
	    return path;
	 }
	 
		/** Create a File for saving an image or video */
		private File getOutputMediaFile(int type, String append){
		    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "GreenScreen");
		    
		    if (! mediaStorageDir.exists()){
		        if (! mediaStorageDir.mkdirs()){
		        	
		            Log.d("MyCameraApp", "failed to create directory");
		            return null;
		        }
		    }

		    // Create a media file name
		    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		    File mediaFile;
		    if (type == 1){
		        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
		        "IMG_"+ timeStamp + append + ".jpg");
		    } else {
		        return null;
		    }

		    scanMedia(mediaFile.getAbsolutePath());
		    return mediaFile;
		}

		/**
		 * Sends a broadcast to have the media scanner scan a file
		 * 
		 * @param path
		 *            the file to scan
		 */
		private  void scanMedia(String path) {
		    File file = new File(path);
		    Uri uri = Uri.fromFile(file);
		    Intent scanFileIntent = new Intent(
		            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
		    act.sendBroadcast(scanFileIntent);
		}
}