package com.weijian.li;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ConfigManager {
    private static final String TAG = "ConfigManager";
    private static final String CONFIG_FILE = "config.json";
    
    private boolean showAddressBar = true;
    private boolean showTitle = true;
    private boolean showActionBar = true;
    private Map<String, String> nfcMapping = new HashMap<>();
    private boolean showTagPrompt = false; // 是否显示标签提示控件
    private boolean mytest = false; // 自定义控制参数
    private String defaultUrl = "http://192.168.91.2";
    private String customTitle = "NFC Reader"; // 自定义标题
    
    private static ConfigManager instance;
    
    private ConfigManager() {
    }
    
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    public void loadConfig(Context context) {
        try {
            // 获取应用数据目录的配置文件路径
            File configFile = new File(context.getExternalFilesDir(null), CONFIG_FILE);
            
            // 如果配置文件不存在，从assets复制默认配置
            if (!configFile.exists()) {
                copyDefaultConfig(context, configFile);
                Log.i(TAG, "Default config file copied to: " + configFile.getAbsolutePath());
            }
            
            // 从数据目录读取配置文件
            FileInputStream fis = new FileInputStream(configFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            
            reader.close();
            fis.close();
            
            parseConfig(stringBuilder.toString());
            Log.i(TAG, "Config loaded from: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error reading config file: " + e.getMessage());
            // 如果从数据目录读取失败，尝试从assets读取默认配置
            try {
                loadDefaultConfig(context);
            } catch (IOException ex) {
                Log.e(TAG, "Failed to load default config: " + ex.getMessage());
            }
        }
    }
    
    /**
     * 从assets复制默认配置到数据目录
     */
    private void copyDefaultConfig(Context context, File destFile) throws IOException {
        InputStream inputStream = context.getAssets().open(CONFIG_FILE);
        FileOutputStream outputStream = new FileOutputStream(destFile);
        
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }
    
    /**
     * 加载默认配置（当数据目录配置文件读取失败时使用）
     */
    private void loadDefaultConfig(Context context) throws IOException {
        InputStream inputStream = context.getAssets().open(CONFIG_FILE);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        
        reader.close();
        inputStream.close();
        
        parseConfig(stringBuilder.toString());
        Log.i(TAG, "Default config loaded from assets");
    }
    
    private void parseConfig(String jsonString) {
        try {
            JSONObject config = new JSONObject(jsonString);
            
            // 解析浏览器配置
            if (config.has("browser_config")) {
                JSONObject browserConfig = config.getJSONObject("browser_config");
                // 只解析mytest参数
                mytest = browserConfig.optInt("mytest", 0) == 1;
                // 解析自定义标题
                if (browserConfig.has("custom_title")) {
                    customTitle = browserConfig.getString("custom_title");
                }
            }
            
            // 解析NFC映射
            if (config.has("nfc_mapping")) {
                JSONObject nfcMappingObj = config.getJSONObject("nfc_mapping");
                Iterator<String> keys = nfcMappingObj.keys();
                while (keys.hasNext()) {
                    String tagId = keys.next();
                    String url = nfcMappingObj.getString(tagId);
                    nfcMapping.put(tagId, url);
                }
            }
            
            // 解析默认URL
            if (config.has("default_url")) {
                defaultUrl = config.getString("default_url");
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing config file: " + e.getMessage());
        }
    }
    
    public boolean isMytestEnabled() {
        return mytest;
    }
    
    // 根据mytest参数获取最终的配置值
    public boolean shouldShowAddressBar() {
        if (mytest) {
            return false; // mytest为1时隐藏地址栏
        }
        return true; // 默认显示地址栏
    }
    
    public boolean shouldShowTitle() {
        if (mytest) {
            return false; // mytest为1时隐藏标题
        }
        return true; // 默认显示标题
    }
    
    public boolean shouldShowActionBar() {
        if (mytest) {
            return false; // mytest为1时隐藏顶栏
        }
        return true; // 默认显示顶栏
    }
    
    public boolean shouldShowTagPrompt() {
        if (mytest) {
            return true; // mytest为1时显示标签
        }
        return false; // 默认隐藏标签
    }
    
    public String getUrlForTagId(String tagId) {
        if (tagId != null && nfcMapping.containsKey(tagId)) {
            return nfcMapping.get(tagId);
        }
        return defaultUrl;
    }
    
    public String getDefaultUrl() {
        return defaultUrl;
    }
    
    public String getCustomTitle() {
        return customTitle;
    }
}