package com.pengjing.config;

import com.google.gson.*;
import com.pengjing.protocol.Serial;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Properties;

/**
 * @Description:
 * @Version: V1.0
 */
public abstract class Config {
    //读取配置文件
    static Properties properties;
    static {
        try(InputStream in = Config.class.getResourceAsStream("/application.properties")){
            properties = new Properties();
            properties.load(in);
        }catch (IOException e){
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Serial.decodec getSerialDecodec(){
        String value = properties.getProperty("Serial.decodec");
        if(value != null){
            return Serial.decodec.valueOf(value);
        }else {
            return Serial.decodec.Java;
        }
    }

    //添加Gson的序列化支持
    public static class ClassTypeAdapater implements JsonSerializer<Class>, JsonDeserializer<Class>{

        //反序列化
        @Override
        public Class deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
                String asString = jsonElement.getAsString();
                return Class.forName(asString);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        //序列化
        @Override
        public JsonElement serialize(Class aClass, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(aClass.getName());
//            return null;
        }
    }

}
