package com.pengjing.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.pengjing.config.Config;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @Description:
 * @Version: V1.0
 */
public interface Serial {

    //序列化
    <T> byte[] decode(T object);

    //反序列化
      <T> T encode(Class<T> clazz, byte[] bytes);

     enum decodec implements Serial{
         //使用jdk自带的序列化
         Java{
             @Override
             public <T> byte[] decode(T object) {
                 try {
                     ByteArrayOutputStream bos = new ByteArrayOutputStream();
                     ObjectOutputStream oos = new ObjectOutputStream(bos);
                     oos.writeObject(object);
                     return bos.toByteArray();
                 } catch (IOException e) {
                     throw new RuntimeException("序列化失败");
                 }
             }

             @Override
             public <T> T encode(Class<T> clazz, byte[] bytes) {
                 try {
                     ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                     ObjectInputStream ois = new ObjectInputStream(bis);
                     return (T)ois.readObject();
                 } catch (Exception e) {
                     throw new RuntimeException("反序列化失败");
                 }
             }
         },
         Json{
             @Override
             public <T> byte[] decode(T object) {

                 try {
                     //使用自定义的Gson序列化方式,添加Class的序列化
                     Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new Config.ClassTypeAdapater()).create();
                     return gson.toJson(object).getBytes(StandardCharsets.UTF_8);
                 } catch (Exception e) {
                     throw new RuntimeException("序列化失败");
                 }
             }

             @Override
             public <T> T encode(Class<T> clazz, byte[] bytes) {
                 try {
                     Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new Config.ClassTypeAdapater()).create();
                     String json = new String(bytes, StandardCharsets.UTF_8);
                     return gson.fromJson(json,clazz);
                 } catch (JsonSyntaxException e) {
                     throw new RuntimeException("反序列化失败");
                 }
             }
         }
     }
}
