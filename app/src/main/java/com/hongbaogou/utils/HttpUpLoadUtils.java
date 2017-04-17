package com.hongbaogou.utils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

public class HttpUpLoadUtils {
	private static final String TAG = "uploadFile";
	private static final int TIME_OUT = 10 * 1000; // 超时时间
	//private static final String CHARSET = "utf-8"; // 设置编码
	/**
	 * Android上传文件到服务端
	 *
	 * @param file 需要上传的文件
	 * @param RequestURL 请求的rul
	 * @return 返回响应的内容
	 */
	public static String uploadFile(File file, String RequestURL) {
		String result = null;
		String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
		String PREFIX = "--", LINE_END = "\r\n";
		String CONTENT_TYPE = "multipart/form-data"; // 内容类型
		try {
			URL url = new URL(RequestURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(TIME_OUT);
			conn.setConnectTimeout(TIME_OUT);
			conn.setDoInput(true); // 允许输入流
			conn.setDoOutput(true); // 允许输出流
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST"); // 请求方式
			conn.setRequestProperty("Charset", CHARSET); // 设置编码
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
			if (file != null) {
				/**
				 * 当文件不为空，把文件包装并且上传
				 */
				DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
				StringBuffer sb = new StringBuffer();
				sb.append(PREFIX);
				sb.append(BOUNDARY);
				sb.append(LINE_END);
				/**
				 * 这里重点注意： name里面的值为服务端需要key 只有这个key 才可以得到对应的文件
				 * filename是文件的名字，包含后缀名的 比如:abc.png
				 */
				sb.append("Content-Disposition: form-data; name=\"uploadfile\"; filename=\""
						+ file.getName() + "\"" + LINE_END);
				sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
				sb.append(LINE_END);
				dos.write(sb.toString().getBytes());
				InputStream is = new FileInputStream(file);
				byte[] bytes = new byte[1024];
				int len = 0;
				while ((len = is.read(bytes)) != -1) {
					dos.write(bytes, 0, len);
				}
				is.close();
				dos.write(LINE_END.getBytes());
				byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
				dos.write(end_data);
				dos.flush();
				/**
				 * 获取响应码 200=成功 当响应成功，获取响应的流
				 */
				int res = conn.getResponseCode();
				Log.e(TAG, "response code:" + res);
				// if(res==200)
				// {
				Log.e(TAG, "request success");
				InputStream input = conn.getInputStream();
				StringBuffer sb1 = new StringBuffer();
				int ss;
				while ((ss = input.read()) != -1) {
					sb1.append((char) ss);
				}
				result = sb1.toString();
				Log.e(TAG, "result : " + result);
				// }
				// else{
				// Log.e(TAG, "request error");
				// }
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}


	/**
	 * 通过拼接的方式构造请求内容，实现参数传输以及文件传输
	 *
	 * @param url Service net address
	 * @param params text content
	 * @param files pictures
	 * @return String result of Service response
	 * @throws IOException
	 */
	public static String post(String url, Map<String, String> params, Map<String, File> files)
			throws IOException {
		String BOUNDARY = java.util.UUID.randomUUID().toString();
		String PREFIX = "--", LINEND = "\r\n";
		String MULTIPART_FROM_DATA = "multipart/form-data";
		String CHARSET = "UTF-8";


		URL uri = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
		conn.setReadTimeout(10 * 1000); // 缓存的最长时间
		conn.setDoInput(true);// 允许输入
		conn.setDoOutput(true);// 允许输出
		conn.setUseCaches(false); // 不允许使用缓存
		conn.setRequestMethod("POST");
		conn.setRequestProperty("connection", "keep-alive");
		conn.setRequestProperty("Charsert", "UTF-8");
		conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);


		// 首先组拼文本类型的参数
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			sb.append(PREFIX);
			sb.append(BOUNDARY);
			sb.append(LINEND);
			sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
			sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
			sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
			sb.append(LINEND);
			sb.append(entry.getValue());
			sb.append(LINEND);
		}


		DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
		outStream.write(sb.toString().getBytes());
		// 发送文件数据
		if (files != null)
			for (Map.Entry<String, File> file : files.entrySet()) {
				StringBuilder sb1 = new StringBuilder();
				sb1.append(PREFIX);
				sb1.append(BOUNDARY);
				sb1.append(LINEND);
				sb1.append("Content-Disposition: form-data; name=\"uploadfile\"; filename=\""
						+ file.getValue().getName() + "\"" + LINEND);
				sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
				sb1.append(LINEND);
				outStream.write(sb1.toString().getBytes());

				InputStream is = new FileInputStream(file.getValue());
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = is.read(buffer)) != -1) {
					outStream.write(buffer, 0, len);
				}
				is.close();
				outStream.write(LINEND.getBytes());
			}

		// 请求结束标志
		byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
		outStream.write(end_data);
		outStream.flush();
		// 得到响应码
		int res = conn.getResponseCode();
		InputStream in = conn.getInputStream();
		StringBuilder sb2 = new StringBuilder();
		if (res == 200) {
			int ch;
			while ((ch = in.read()) != -1) {
				sb2.append((char) ch);
			}
		}
		outStream.close();
		conn.disconnect();
		return sb2.toString();
	}


	private final static String LINEND = "\r\n";
	private final static String BOUNDARY = "---------------------------7da2137580612"; //数据分隔线
	private final static String PREFIX = "--";
	private final static String MUTIPART_FORMDATA = "multipart/form-data";
	private final static String CHARSET = "utf-8";
	private final static String CONTENTTYPE = "application/octet-stream";

	public int post(String actionUrl,Map<String,String> params,String fileName) {
		Log.i("post-------------", "postfile");
		HttpURLConnection urlConn = null;
		BufferedReader br = null;
		try {
			//新建url对象
			URL url = new URL(actionUrl);
			//通过HttpURLConnection对象,向网络地址发送请求
			urlConn = (HttpURLConnection) url.openConnection();

			//设置该连接允许读取
			urlConn.setDoOutput(true);
			//设置该连接允许写入
			urlConn.setDoInput(true);
			//设置不能适用缓存
			urlConn.setUseCaches(false);
			//设置连接超时时间
			urlConn.setConnectTimeout(3000);   //设置连接超时时间
			//设置读取时间
			urlConn.setReadTimeout(4000);   //读取超时
			//设置连接方法post
			urlConn.setRequestMethod("POST");
			//设置维持长连接
			urlConn.setRequestProperty("connection", "Keep-Alive");
			//设置文件字符集
			urlConn.setRequestProperty("Charset", CHARSET);
			//设置文件类型
			urlConn.setRequestProperty("Content-Type", MUTIPART_FORMDATA + ";boundary=" + BOUNDARY);
			/********************************************************************/
			DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
			//构建表单数据

			int index = 1;

			StringBuffer sb = new StringBuffer("");
			sb.append(PREFIX + BOUNDARY + LINEND)
					.append("Content-Type:" + CONTENTTYPE + ";" +
							"charset=" + CHARSET + LINEND)
					.append(LINEND);
			dos.write(sb.toString().getBytes());
			FileInputStream fis = new FileInputStream(fileName);
			byte[] buffer = new byte[10000];
			int len = 0;
			while ((len = fis.read(buffer)) != -1) {
				dos.write(buffer, 0, len);
			}
			dos.write(LINEND.getBytes());
			fis.close();

			//请求的结束标志
			byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
			dos.write(end_data);
			dos.flush();
			dos.close();
			// 发送请求数据结束

			//接收返回信息
			int code = urlConn.getResponseCode();
			if (code != 200) {
				urlConn.disconnect();
				return code;
			} else {
				br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String result = "";
				String line = null;
				while ((line = br.readLine()) != null) {
					result += line;
				}
				Log.i("post-------------", result);
				if ("true".equals(result)) {
					return 200;
				} else {
					return 500;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("--------上传图片错误--------", e.getMessage());
			return -1;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (urlConn != null) {
					urlConn.disconnect();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}}
