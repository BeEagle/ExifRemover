package com.nathanhaze.exifremover;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

//import com.google.analytics.tracking.android.EasyTracker;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public Button importImage, removeExif;
	public CheckBox deleteImage, saveFolder;
	public ImageView userImage;

	public static final int RESULT_SETTINGS = 1;
	public static final int FILE_SELECT_CODE = 0;
	public static final int GALLERY_KITKAT_INTENT_CALLED =2;
	public static final int GALLERY_INTENT_CALLED = 3;
	
	ProgressDialog pd;
	
	Context context;
	
	Bitmap userBitmap = null;
	
	String path ="";
	
	
	ImageUtil imgUtil;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = this;
		importImage = (Button)findViewById(R.id.add_image);
		removeExif = (Button)findViewById(R.id.remove);
		
		userImage = (ImageView)findViewById(R.id.user_image);
		
		deleteImage = (CheckBox)findViewById(R.id.delete_photo);
		saveFolder = (CheckBox)findViewById(R.id.different_photo);
		
		importImage.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	if (Build.VERSION.SDK_INT <19){
				    Intent intent = new Intent(); 
				    intent.setType("image/jpeg");
				    intent.setAction(Intent.ACTION_GET_CONTENT);
				    startActivityForResult(Intent.createChooser(intent, "Select a background"),GALLERY_INTENT_CALLED);
				} else {
				    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
				    intent.addCategory(Intent.CATEGORY_OPENABLE);
				    intent.setType("image/jpeg");
				    startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
				}   
		    }
		});
		
		removeExif.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	new RemoveExifTask().execute();
		    }
		});
		
		setHidden();
		
		pd = new ProgressDialog(this);
		
		 AdView mAdView = (AdView) findViewById(R.id.adView);
	        AdRequest adRequest = new AdRequest.Builder().build();
	        mAdView.loadAd(adRequest);
	        
	        imgUtil = new ImageUtil(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		/*
		if (id == R.id.action_settings) {
			return true;
		}
		*/
		
		return super.onOptionsItemSelected(item);
	}
	
	
	/** image import **/
	
	 public void importImage(View v){
		 
		 Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		 Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
		  );
		 intent.setDataAndType(uri, "*/*");
		 startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
		 
		 
		 /*
			if (Build.VERSION.SDK_INT <19){
			    Intent intent = new Intent(); 
			    intent.setType("image/jpeg");
			    intent.setAction(Intent.ACTION_GET_CONTENT);
			    startActivityForResult(Intent.createChooser(intent, "Select a background"),GALLERY_INTENT_CALLED);
			} else {
			    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			    intent.addCategory(Intent.CATEGORY_OPENABLE);
			    intent.setType("image/jpeg");
			    startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
			}
			*/
}
	 
	 private class RemoveExifTask extends AsyncTask<Void, Void, String> {
		 
		 @Override
		 protected void onPreExecute(){
			 pd.setTitle( "Removing Exif...");
			 pd.show();
		 }
		 
		 @Override
	     protected String doInBackground(Void... urls) {
			 String result = "";
			 if(userBitmap !=null){
			     result = saveBitmap("clean", userBitmap);
			 }
	         return result;
	     }

         @Override
	     protected void onPostExecute(String result) {
             pd.dismiss();
             
             if(result != ""){
            	
  				Toast.makeText(context, "Your Image was saved in the Clean folder" , Toast.LENGTH_LONG).show();
              }else{
            	  
  				Toast.makeText(context, "Your exif could not be removed", Toast.LENGTH_LONG).show();
              }
	     }
	 }
	 
	 
	 
	 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
	 
	        if (null == data || resultCode != RESULT_OK) return;
	        
	        Uri originalUri = null;
	        if (requestCode == GALLERY_INTENT_CALLED) {
	            originalUri = data.getData();
	        } else if (requestCode == GALLERY_KITKAT_INTENT_CALLED) {
	            originalUri = data.getData();
	            final int takeFlags = data.getFlags()
	                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
	                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
	            // Check for the freshest data.
	            getContentResolver().takePersistableUriPermission(originalUri, takeFlags);
	        }
	        
	        //String path = "";
			path = getPathX(this, originalUri);
			if(path != null){
				userBitmap = BitmapFactory.decodeFile(path);
				
				try {
					ExifInterface exif = new ExifInterface(path);
					int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

					if(orientation == 6){
						userBitmap  = imgUtil.rotateBitmp(userBitmap, false);
					}
					
					
				
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				userImage.setImageBitmap(userBitmap);
				setVisible();
			}
			else{
				Toast toast = Toast.makeText(this, "Something went wrong grabbing the image", Toast.LENGTH_LONG);
				toast.show();
			}
	    }
	 
	void setVisible(){
		 removeExif.setVisibility(View.VISIBLE);
		// deleteImage.setVisibility(View.VISIBLE);
		// saveFolder.setVisibility(View.VISIBLE);
	 }
	 
	 void setHidden(){ 
		 removeExif.setVisibility(View.GONE);
		 deleteImage.setVisibility(View.GONE);
		 saveFolder.setVisibility(View.INVISIBLE);
	 }
	 
	 public static String getPathX(final Context context, final Uri uri) {

		    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		    // DocumentProvider
		    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
		        // ExternalStorageProvider
		        if (isExternalStorageDocument(uri)) {
		            final String docId = DocumentsContract.getDocumentId(uri);
		            final String[] split = docId.split(":");
		            final String type = split[0];

		            if ("primary".equalsIgnoreCase(type)) {
		                return Environment.getExternalStorageDirectory() + "/" + split[1];
		            }

		            // TODO handle non-primary volumes
		        }
		        // DownloadsProvider
		        else if (isDownloadsDocument(uri)) {

		            final String id = DocumentsContract.getDocumentId(uri);
		            final Uri contentUri = ContentUris.withAppendedId(
		                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

		            return getDataColumn(context, contentUri, null, null);
		        }
		        // MediaProvider
		        else if (isMediaDocument(uri)) {
		            final String docId = DocumentsContract.getDocumentId(uri);
		            final String[] split = docId.split(":");
		            final String type = split[0];

		            Uri contentUri = null;
		            if ("image".equals(type)) {
		                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		            } else if ("video".equals(type)) {
		                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		            } else if ("audio".equals(type)) {
		                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		            }

		            final String selection = "_id=?";
		            final String[] selectionArgs = new String[] {
		                    split[1]
		            };

		            return getDataColumn(context, contentUri, selection, selectionArgs);
		        }
		    }
		    // MediaStore (and general)
		    else if ("content".equalsIgnoreCase(uri.getScheme())) {
		        return getDataColumn(context, uri, null, null);
		    }
		    // File
		    else if ("file".equalsIgnoreCase(uri.getScheme())) {
		        return uri.getPath();
		    }

		    return null;
		}
	 
		/**
		 * Get the value of the data column for this Uri. This is useful for
		 * MediaStore Uris, and other file-based ContentProviders.
		 *
		 * @param context The context.
		 * @param uri The Uri to query.
		 * @param selection (Optional) Filter used in the query.
		 * @param selectionArgs (Optional) Selection arguments used in the query.
		 * @return The value of the _data column, which is typically a file path.
		 */
		public static String getDataColumn(Context context, Uri uri, String selection,
		        String[] selectionArgs) {

		    Cursor cursor = null;
		    final String column = "_data";
		    final String[] projection = {
		            column
		    };

		    try {
		        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
		                null);
		        if (cursor != null && cursor.moveToFirst()) {
		            final int column_index = cursor.getColumnIndexOrThrow(column);
		            return cursor.getString(column_index);
		        }
		    } finally {
		        if (cursor != null)
		            cursor.close();
		    }
		    return null;
		}


		/**
		 * @param uri The Uri to check.
		 * @return Whether the Uri authority is ExternalStorageProvider.
		 */
		public static boolean isExternalStorageDocument(Uri uri) {
		    return "com.android.externalstorage.documents".equals(uri.getAuthority());
		}

		/**
		 * @param uri The Uri to check.
		 * @return Whether the Uri authority is DownloadsProvider.
		 */
		public static boolean isDownloadsDocument(Uri uri) {
		    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
		}

		/**
		 * @param uri The Uri to check.
		 * @return Whether the Uri authority is MediaProvider.
		 */
		public static boolean isMediaDocument(Uri uri) {
		    return "com.android.providers.media.documents".equals(uri.getAuthority());
		}
		
		 public String saveBitmap(String type, Bitmap image){
			 String NewPath = "";
			 File imageFile = getOutputMediaFile(1, type );
			 FileOutputStream out = null;
			 
			 try {
				 NewPath = imageFile.getAbsolutePath();
			     out = new FileOutputStream(NewPath);
				 image.compress(Bitmap.CompressFormat.JPEG, 93, out);
				 
			     if(deleteImage.isChecked()){
			         File orig = new File(path);
			         orig.delete();
			     }
			     
			 } catch (Exception e) {
			     e.printStackTrace();
			     return "";
			 } finally {
			     try {
			         if (out != null) {
			             out.close();
			         }
			     } catch (IOException e) {
			         e.printStackTrace();
			         return NewPath;
			     }
			}
		    return NewPath;
		 }
		 
		 /** Create a File for saving an image or video */
			private File getOutputMediaFile(int type, String append){
				File mediaStorageDir;
				int start = path.lastIndexOf("/");	
				int end = path.lastIndexOf(".");
			//	if(saveFolder.isChecked()){
			         mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Clean");
			//	}else{
			//		mediaStorageDir = new File(path.substring(0, start));
		//		}
			    if (! mediaStorageDir.exists()){
			        if (! mediaStorageDir.mkdirs()){
			        	return null;
			        }
			    }

			    // Create a media file name
			    String fileName = path.substring(start +1, end);
			    
			    File mediaFile;
			    if (type == 1){
			    		mediaFile = new File(mediaStorageDir.getPath() + File.separator ,
						        "clean_"+ fileName + ".jpg");
			    } else {
			        return null;
			    }

			    scanMedia(mediaFile.getAbsolutePath());
			    return mediaFile;
			}

			private  void scanMedia(String path) {
			    File file = new File(path);
			    Uri uri = Uri.fromFile(file);
			    Intent scanFileIntent = new Intent(
			            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
			    this.sendBroadcast(scanFileIntent);
			}
			
		    @Override
		    protected void onStart() {
		        super.onStart();  
		    //    EasyTracker.getInstance(this).activityStart(this);  // Add this method.

		    }
		    
		    @Override
		    protected void onStop() {
		        super.onStop();
		    //    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
		    }
			
}
