package com.obank.maintain.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.PublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.TimeZone;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MaintainController {
	// 獲取當前工作目錄
	private String currentDir = System.getProperty("user.dir");
	private Path path = Paths.get(currentDir + "/maintain.txt");
	private Gson gson = new Gson();
	private ArrayList<ＭTTimeData> DataList = new ArrayList<ＭTTimeData>();
	private int IsDelete = 0;

	@GetMapping("/maintain")
	public String maintain() {
		Log("maintain執行|");
		GetDataList();
		String isMaintain = "Success";
		// 定義日期和時間的格式
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
		if (DataList != null) {
			for (ＭTTimeData item : DataList) {
				LocalDateTime Sdatetime = LocalDateTime.parse(item.Sdatetime, formatter);
				LocalDateTime Edatetime = LocalDateTime.parse(item.Edatetime, formatter);
				LocalDateTime NdateTime = LocalDateTime.now();
				Log("現在時刻|" + NdateTime + "|Sdatetime|" + Sdatetime+ "|Edatetime|" + Edatetime);
				if (NdateTime.isAfter(Sdatetime) & NdateTime.isBefore(Edatetime)) {
					isMaintain = "Maintain";
					break;
				}
			}
		}
		IsDelete = 1;
		Log("maintain結束|" + isMaintain);
		return isMaintain;
	}

	private void GetDataList() {
		String content = "";
		// 檢查檔案是否存在
		if (Files.exists(path)) {
			try (BufferedReader br = new BufferedReader(new FileReader(path.toString()))) {
				String line;
				while ((line = br.readLine()) != null) {
					content += line;
				}
				br.close();
			} catch (IOException e) {
			}
			Type listType = new TypeToken<ArrayList<ＭTTimeData>>() {
			}.getType();
			if (!content.trim().equals("")) // 有資料
				DataList = gson.fromJson(content, listType);// 轉型 convert
		}
	}

	public class ＭTTimeData {
		private String Sdatetime;
		private String Edatetime;

		public String getSdatetime() {
			return Sdatetime;
		}

		public void setSdatetime(String sdatetime) {
			this.Sdatetime = sdatetime;
		}

		public String getEdatetime() {
			return Edatetime;
		}

		public void setEdatetime(String edatetime) {
			this.Edatetime = edatetime;
		}
	}

	@GetMapping("/getip")
	public String getip(HttpServletRequest request) {

		String ipAddress = null;
		try {
			ipAddress = request.getHeader("x-forwarded-for");
			if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getHeader("Proxy-Client-IP");
			}
			if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getRemoteAddr();
				if (ipAddress.equals("127.0.0.1")) {
					// 根据网卡取本机配置的IP
					InetAddress inet = null;
					try {
						inet = InetAddress.getLocalHost();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
					ipAddress = inet.getHostAddress();
				}
			}
			if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
				// = 15
				if (ipAddress.indexOf(",") > 0) {
					ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
				}
			}
		} catch (Exception e) {
			ipAddress = "";
		}
		return ipAddress;
	}

	@GetMapping("/maintain/clear")
	public String maintainclear() {
		DataList = new ArrayList<ＭTTimeData>();
		try (FileWriter writer = new FileWriter(path.toString())) {
			writer.write(gson.toJson(DataList));
			writer.close();
		} catch (IOException e) {
		}
		return "maintainclear" + "|" + gson.toJson(DataList);
	}

	@GetMapping("/maintain/set")
	public String maintainset(@RequestParam(value = "yyyyMMdd", defaultValue = "2000/01/01") String yyyyMMdd,
			@RequestParam(value = "shhmmss", defaultValue = "00:00") String shhmmss,
			@RequestParam(value = "ehhmmss", defaultValue = "00:00") String ehhmmss) {
		// 讀取檔案
		GetDataList();
		ＭTTimeData newContent = new ＭTTimeData();
		newContent.Sdatetime = yyyyMMdd + " " + shhmmss;
		newContent.Edatetime = yyyyMMdd + " " + ehhmmss;
		DataList.add(newContent);
		try (FileWriter writer = new FileWriter(path.toString())) {
			writer.write(gson.toJson(DataList));
			writer.close();
		} catch (IOException e) {
		}
		return "maintainset" + "|" + gson.toJson(DataList);
	}

	// log 紀錄
	private void Log(String msg) {
		String pathStr = currentDir + "/logs";
		Checkfolder(pathStr);

		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
		String formattedDateTime = now.format(formatter);
		String formattedDateTime2 = now.format(formatter2);

		Path pathLog = Paths.get(pathStr.toString() + "/" + formattedDateTime + "_log.txt");
		String outMsgString = formattedDateTime2 + "\t" + msg + System.lineSeparator();
		try {
			Files.write(Paths.get(pathLog.toString()), outMsgString.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
		}
		if (IsDelete == 1) {
			DeleteFiles(pathStr, formattedDateTime2, pathLog);
		}
	}

	private void Checkfolder(String folderPath) {
		// 先建立資料夾
		File folder = new File(folderPath);
		// 檢查資料夾是否存在
		if (!folder.exists())
			folder.mkdirs();
	}

	private void DeleteFiles(String folderPath, String formattedDateTime2, Path pathLog) {
		// 先建立資料夾
		File folder = new File(folderPath);
		// 刪除修改時間以前
		// 檢查資料夾是否存在且為資料夾
		if (folder.exists() && folder.isDirectory()) {
			// 獲取資料夾中的所有檔案和子目錄
			File[] files = folder.listFiles();
			// 列出所有檔案和子目錄
			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						long lastModified = file.lastModified();
						LocalDateTime triggerTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified),
								TimeZone.getDefault().toZoneId());
						// 計算兩個日期之間的天數差異
						long daysBetween = ChronoUnit.DAYS.between(triggerTime, LocalDateTime.now());
						if (daysBetween >= 15) {
							String outMsgString = formattedDateTime2 + "\t" + "刪除檔案|" + file.getName()
									+ System.lineSeparator();
							try {
								Files.write(Paths.get(pathLog.toString()), outMsgString.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
							} catch (IOException e) {
							}
							file.delete();
						}
					}
				}
			}
		}
	}
}
