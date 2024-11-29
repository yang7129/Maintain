package com.obank.maintain.controllers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.lang.reflect.Type; 
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MaintainController {
	@GetMapping("/maintain")
	public String maintain() {
		// 獲取當前工作目錄
		String currentDir = Paths.get("").toAbsolutePath().toString();
		Path path = Paths.get(currentDir + "\\maintain.txt");
		// 檢查檔案是否存在
		String content = "";
		if (Files.exists(path)) {
			try (BufferedReader br = new BufferedReader(new FileReader(path.toString()))) {
				String line;
				while ((line = br.readLine()) != null) {
					content += line;
				}
				br.close();
			} catch (IOException e) {
			}
		}
		Gson gson = new Gson();
		//ＭaintainData ｍaintainData = new ＭaintainData();
		//ｍaintainData.DataList = new ArrayList<ＭTTimeData>();
		Type listType = new TypeToken<ArrayList<ＭTTimeData>>() {}.getType();
		String test="";
		String isMaintain ="";
		
		if (!content.trim().equals("")) { // 有資料
			//ｍaintainData =gson.fromJson(content, ＭaintainData.class);
			ArrayList<ＭTTimeData> DataList = gson.fromJson(content, listType);
//			ＭTTimeData timeData = new ＭTTimeData();
//			timeData.Sdatetime = "2024/11/20 00:01:02";
//			timeData.Edatetime = "2024/11/28 23:01:02";
//			ｍaintainData.DataList.add(timeData);
			// 定義日期和時間的格式 
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
			
			for (ＭTTimeData item : DataList) {
				LocalDateTime Sdatetime = LocalDateTime.parse(item.Sdatetime, formatter);
				LocalDateTime Edatetime = LocalDateTime.parse(item.Edatetime, formatter);
				LocalDateTime NdateTime  = LocalDateTime.now();
				test+= "Sdatetime|" + Sdatetime + "|Edatetime|" + Edatetime + "|NdateTime|" + NdateTime +  "|isMaintain|" + (NdateTime.isAfter(Sdatetime) & NdateTime.isBefore(Edatetime))   + "/r/n";
				if (NdateTime.isAfter(Sdatetime) & NdateTime.isBefore(Edatetime)) {
					isMaintain=  String.valueOf((NdateTime.isAfter(Sdatetime) & NdateTime.isBefore(Edatetime)));
					break;
				}
			}
		}
		//JSONObject jsonObject = new JSONObject(ｍaintainData);

		return "202411281342" + "|" + LocalDateTime.now() + "|"  + test  + "|isMaintain|"  + isMaintain ;
	}

	public class ＭaintainData {
		private ArrayList<ＭTTimeData> DataList;
		public ArrayList<ＭTTimeData> getDataList() {
			return DataList;
		}

		public void setDataList(ArrayList<ＭTTimeData> dataList) {
			this.DataList = dataList;
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
}
