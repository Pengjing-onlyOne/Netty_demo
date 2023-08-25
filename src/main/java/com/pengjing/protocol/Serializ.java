package com.pengjing.protocol;

import com.google.gson.Gson;
import com.pengjing.message.Message;

import java.io.*;

public interface Serializ {

    //反序列化
    <T> T decode(Class<T> clazz,byte[] bytes);

    //序列化
    byte[]  encode(Message message);

    enum serializChoose implements Serializ{

        Java{
            @Override
            public <T> T decode(Class<T> clazz, byte[] bytes) {
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    return (T) ois.readObject();
                } catch (Exception e) {
                    throw new RuntimeException("反序列化失败");
                }
            }

            @Override
            public  byte[] encode(Message message) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(message);
                    //对象
                    return  bos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("序列化失败");
                }
            }
        },
        Json{
            @Override
            public <T> T decode(Class<T> clazz, byte[] bytes) {
                Gson gson = new Gson();
                return  gson.fromJson(new String(bytes), clazz);
            }

            @Override
            public byte[] encode(Message message) {
                Gson gson = new Gson();
                return gson.toJson(message).getBytes();
            }
        }
    }
}
