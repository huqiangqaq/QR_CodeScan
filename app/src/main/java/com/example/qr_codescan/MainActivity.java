package com.example.qr_codescan;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.okhttp.MediaType;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;




public class MainActivity extends Activity {
	private final static int START_UPLOAD = 1;
	private final static int SCANNIN_GREQUEST_CODE = 2;
	private final static int END_UPLOAD = 3;
	private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/jpg");
	private static final String BOUNDARY = "----WebKitFormBoundaryT1HoybnYeFOGFlBR";
	private Map<String,String> map = new HashMap<>();

	private static final String TAG = "uploadFile";
	private static final int TIME_OUT = 10*1000;   //超时时间
	private static final String CHARSET = "utf-8"; //设置编码
	/**
	 * 显示扫描结果
	 */
	private TextView mTextView ;
	/**
	 * 图片
	 */
	//扫描二维码的结果
	private int code;
	private String Result = "";
	private ImageView mimg_show;
	private Button btn_start,btn_end;
	private File currentImageFile = null;
	private boolean isLoginSuccess = true;
	private String filename = "";
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
				switch (msg.what){
					case 0x123:
						Toast.makeText(MainActivity.this,"上传成功",Toast.LENGTH_SHORT).show();
						break;
					case 0x124:
						Toast.makeText(MainActivity.this,"上传失败",Toast.LENGTH_SHORT).show();
						break;
				}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (!checkCameraHardware(MainActivity.this)){
			Toast.makeText(MainActivity.this,"相机不可用，请更换手机",Toast.LENGTH_LONG).show();
		}
		mTextView = (TextView) findViewById(R.id.result); 
		mimg_show = (ImageView) findViewById(R.id.img_show);
		btn_end = (Button) findViewById(R.id.btn_end);
		btn_start = (Button) findViewById(R.id.btn_start);
		//扫描二维码
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, MipcaActivityCapture.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
		//扫描二维码
//		Button mButton = (Button) findViewById(R.id.button1);

		//拍摄完图片之后上传给服务器
		btn_start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isLoginSuccess){
					if ("".equals(Result)){
						SweetAlertDialog dialog1 = new SweetAlertDialog(MainActivity.this,SweetAlertDialog.ERROR_TYPE);
						dialog1.setTitleText("请先扫描二维码");
						dialog1.setCancelable(true);
						dialog1.show();
						
					}else {
						File dir = new File(Environment.getExternalStorageDirectory(), "pictures");
						if (dir.exists()) {
							dir.mkdirs();
						}
						filename = System.currentTimeMillis()+".jpg";
						currentImageFile = new File(dir, filename);

						if (!currentImageFile.exists()) {
							try {
								currentImageFile.createNewFile();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentImageFile));
						startActivityForResult(it, START_UPLOAD);
					}

				}

			}
		});
		btn_end.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isLoginSuccess){
					if ("".equals(Result)){
						SweetAlertDialog dialog1 = new SweetAlertDialog(MainActivity.this,SweetAlertDialog.ERROR_TYPE);
						dialog1.setTitleText("请先扫描二维码");
						dialog1.setCancelable(true);
						dialog1.show();
					}else {
						File dir = new File(Environment.getExternalStorageDirectory(), "pictures");
						if (dir.exists()) {
							dir.mkdirs();
						}
						currentImageFile = new File(dir, System.currentTimeMillis() + ".jpg");

						if (!currentImageFile.exists()) {
							try {
								currentImageFile.createNewFile();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentImageFile));
						startActivityForResult(it, END_UPLOAD);
					}

				}
			}
		});
	}
	
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
		case SCANNIN_GREQUEST_CODE:
			if(resultCode == RESULT_OK){
				Bundle bundle = data.getExtras();
				//二维码扫描结果
				Result = (String) bundle.get("result");
				map.put("cardnum",Result);
				mTextView.setText(bundle.getString("result"));
				//��ʾ
//				mImageView.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));
			}
			break;
			case START_UPLOAD:
				long lenth = currentImageFile.length();
				//上传图片
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {

							int code = uploadForm(map,"upFile",currentImageFile,filename,Constants.UPLOADPAHT_Start);
							if (code == 200){
								handler.sendEmptyMessage(0x123);
							}else {
								handler.sendEmptyMessage(0x123);
							}


						} catch (IOException e) {
							e.printStackTrace();
						}

					}
				}).start();

				break;
			case END_UPLOAD:
				long len = currentImageFile.length();
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							int code = uploadForm(map,"upFile",currentImageFile,filename,Constants.UPLOADPATH_END);
							if (code==200){
								handler.sendEmptyMessage(0x123);
							}else {
								handler.sendEmptyMessage(0x124);
							}

						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();

				break;
		}
    }


	//判断设备是否具有相机
	public boolean checkCameraHardware(Context context){
			if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
				return true;
			}else {
				return false;
			}
	}
	public int uploadForm(Map<String, String> params, String fileFormName,
						   File uploadFile, String newFileName, String urlStr)
			throws IOException {
		if (newFileName == null || newFileName.trim().equals("")) {
			newFileName = uploadFile.getName();
		}

		StringBuilder sb = new StringBuilder();
		/**
		 * 普通的表单数据
		 */
		if (params != null)
			for (String key : params.keySet()) {
				sb.append("--" + BOUNDARY + "\r\n");
				sb.append("Content-Disposition: form-data; name=\"" + key
						+ "\"" + "\r\n");
				sb.append("\r\n");
				sb.append(params.get(key) + "\r\n");
			}
		/**
		 * 上传文件的头
		 */
		sb.append("--" + BOUNDARY + "\r\n");
		sb.append("Content-Disposition: form-data; name=\"" + fileFormName
				+ "\"; filename=\"" + newFileName + "\"" + "\r\n");
		sb.append("Content-Type: image/jpeg" + "\r\n");// 如果服务器端有文件类型的校验，必须明确指定ContentType
		sb.append("\r\n");

		byte[] headerInfo = sb.toString().getBytes("UTF-8");
		byte[] endInfo = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");
		System.out.println(sb.toString());
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + BOUNDARY);
		conn.setRequestProperty("Content-Length", String
				.valueOf(headerInfo.length + uploadFile.length()
						+ endInfo.length));
		conn.setDoOutput(true);

		OutputStream out = conn.getOutputStream();
		InputStream in = new FileInputStream(uploadFile);
		out.write(headerInfo);

		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) != -1)
			out.write(buf, 0, len);

		out.write(endInfo);
		in.close();
		out.close();
		if (conn.getResponseCode() == 200) {
			String string = conn.getResponseMessage();
			Log.d("huqiang", conn.getResponseMessage());
			Log.d("huqiang", String.valueOf(conn.getResponseCode()));
			System.out.println("上传成功");
		}
		return conn.getResponseCode();
	}
	/**
	 *
	 * 上传工具类
	 * @author spring sky
	 * Email:vipa1888@163.com
	 * QQ:840950105
	 * MyName:石明政
	 */
		public  String uploadFile(File file,String RequestURL)
		{
			String result = null;
			String  BOUNDARY =  UUID.randomUUID().toString();  //边界标识   随机生成
			String PREFIX = "--" , LINE_END = "\r\n";
			String CONTENT_TYPE = "multipart/form-data";   //内容类型

			try {
				URL url = new URL(RequestURL);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(TIME_OUT);
				conn.setConnectTimeout(TIME_OUT);
				conn.setDoInput(true);  //允许输入流
				conn.setDoOutput(true); //允许输出流
				conn.setUseCaches(false);  //不允许使用缓存
				conn.setRequestMethod("POST");  //请求方式
				conn.setRequestProperty("Charset", CHARSET);  //设置编码
				conn.setRequestProperty("connection", "keep-alive");
				conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

				if(file!=null)
				{
					/**
					 * 当文件不为空，把文件包装并且上传
					 */
					DataOutputStream dos = new DataOutputStream( conn.getOutputStream());
					StringBuffer sb = new StringBuffer();
					sb.append(PREFIX);
					sb.append(BOUNDARY);
					sb.append(LINE_END);
					/**
					 * 这里重点注意：
					 * name里面的值为服务器端需要key   只有这个key 才可以得到对应的文件
					 * filename是文件的名字，包含后缀名的   比如:abc.png
					 */

					sb.append("Content-Disposition: form-data; name=\"img\"; filename=\""+file.getName()+"\""+LINE_END);
					sb.append("Content-Type: application/octet-stream; charset="+CHARSET+LINE_END);
					sb.append(LINE_END);
					dos.write(sb.toString().getBytes());
					InputStream is = new FileInputStream(file);
					byte[] bytes = new byte[1024];
					int len = 0;
					while((len=is.read(bytes))!=-1)
					{
						dos.write(bytes, 0, len);
					}
					is.close();
					dos.write(LINE_END.getBytes());
					byte[] end_data = (PREFIX+BOUNDARY+PREFIX+LINE_END).getBytes();
					dos.write(end_data);
					dos.flush();
					/**
					 * 获取响应码  200=成功
					 * 当响应成功，获取响应的流
					 */
					int res = conn.getResponseCode();
					Log.e(TAG, "response code:"+res);
//                if(res==200)
//                {
					Log.e(TAG, "request success");
					InputStream input =  conn.getInputStream();
					StringBuffer sb1= new StringBuffer();
					int ss ;
					while((ss=input.read())!=-1)
					{
						sb1.append((char)ss);
					}
					result = sb1.toString();
					Log.e(TAG, "result : "+ result);
//                }
//                else{
//                    Log.e(TAG, "request error");
//                }
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}
	}


