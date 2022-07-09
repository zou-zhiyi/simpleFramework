package com.zzy.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.zzy.bean.lifeCycle.AbstractLifeCycle;
import com.zzy.bean.lifeCycle.interfaces.LifeCycle;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName MeFileUtils.java
 * @Description 文件读取类，用于读取json文件、节点组件
 * @createTime 2022年01月21日 10:52:00
 */
public class MeFileUtils {
    public static Logger logger = LoggerFactory.getLogger(MeFileUtils.class);
    private static Map<String, URLClassLoader> classLoaderMap = new ConcurrentHashMap<>();
    /**
     * 读取文件内容，将其转化为一个JSONObject
     * @param filePath 文件路径
     * @param encodeType 文件编码格式
     * @return 返回一个jsonObject
     * @throws IOException 抛出异常
     * @throws  JSONException 抛出异常
     */
    public static JSONObject readJSONObject(String filePath , String encodeType) {
        String content = null;
        try {
            content = FileUtils.readFileToString(new File(filePath),encodeType);
        } catch (IOException e) {
            logger.error("the file do noe exist, now direction is {}", new File("").getAbsolutePath());
        }
        if (content == null || content.isEmpty()) {
            return new JSONObject();
        }
        return JSON.parseObject(content);
    }

    /**
     * 读取文件内容，将其转化为一个JSONObject
     * @param filePath 文件路径
     * @return 返回一个jsonObject
     * @throws IOException 抛出异常
     * @throws JSONException 抛出异常
     */
    public static JSONObject readJSONObject(String filePath) {
        return readJSONObject(filePath, "UTF-8");
    }

    public static void wirteJSONObject(String filePath, JSONObject jsonObject) throws IOException {
        FileOutputStream fileOutputStream = FileUtils.openOutputStream(new File(filePath));
        fileOutputStream.write(jsonObject.toJSONString().getBytes());
    }

    /**
     * 读取文件内容，将其转化为一个parseArray
     * @param filePath 文件路径
     * @param encodeType 文件编码格式
     * @return 返回一个jsonObject
     * @throws IOException 抛出异常
     * @throws  JSONException 抛出异常
     */
    public static JSONArray readJSONArray(String filePath, String encodeType) throws IOException,JSONException {
        String content = FileUtils.readFileToString(new File(filePath),encodeType);
        return JSONArray.parseArray(content);
    }

    /**
     * 读取文件内容，将其转化为一个parseArray
     * @param filePath 文件路径
     * @return 返回一个jsonObject
     * @throws IOException 抛出异常
     * @throws JSONException 抛出异常
     */
    public static JSONArray readJSONArray(String filePath) throws  IOException,JSONException {
        return readJSONArray(filePath, "UTF-8");
    }

    /**
     * 读取组件，并返回一个lifeCycle
     * @param filePath 组件包文件路径
     * @param packagePath 组件包包路径
     * @return 返回组件
     * @throws MalformedURLException url路径异常
     * @throws ClassNotFoundException 未找到类异常
     * @throws IllegalAccessException newInstance抛出异常
     * @throws InstantiationException newInstance抛出异常
     */
    public static LifeCycle readModel(String filePath, String packagePath) throws MalformedURLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        File file = new File(filePath);
//        URLClassLoader classLoader=new URLClassLoader(new URL[]{url},Thread.currentThread().getContextClassLoader());
        // 如果不带filePath, 默认为加载当前包下的类
        if (filePath.equals("")){
            logger.debug("load local package");
            Class<?> aClass = Class.forName(packagePath);
            return (LifeCycle) aClass.newInstance();
        }
        logger.debug("load package filePath: {}",file.getAbsolutePath());
        URLClassLoader classLoader = classLoaderMap.get(file.getAbsolutePath());
        if (classLoader == null){
            URL url=new URL("file:"+file.getAbsolutePath());
            classLoader = new URLClassLoader(new URL[]{url},ClassLoader.getSystemClassLoader());
            classLoaderMap.put(file.getAbsolutePath(), classLoader);
        }
        Class<?> myClass = classLoader.loadClass(packagePath);
        return (LifeCycle) myClass.newInstance();
    }

    /**
     * 用于创建文件夹
     * @param filePath
     * @throws IOException
     */
    public static void constructDirectorys(String filePath) throws IOException {
        Files.createDirectories(Paths.get(filePath));
    }

    /**
     * 用于创建文件
     * @param filePath
     * @throws IOException
     */
    public static void constructFiles(String filePath) throws IOException {
        File file = new File(filePath);
        constructDirectorys(file.getParentFile().getPath());
        Files.createFile(Paths.get(filePath));
    }
}
