//package com.usst.adfluxbackend.config;
//
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.databind.JsonSerializer;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializerProvider;
//import com.fasterxml.jackson.databind.module.SimpleModule;
//import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
//import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
//import org.springframework.boot.jackson.JsonComponent;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
//
//import java.io.IOException;
//
///**
// * Spring MVC json配置
// */
//@Configuration
//public class JsonConfig {
//
////    @Bean
////    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
////        return builder -> {
////            // 将 Long 类型序列化为 String
////            builder.serializerByType(Long.class, ToStringSerializer.instance);
////            // 将 long 基本类型序列化为 String
////            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
////        };
////    }
//
//    @Bean
//    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
//        return builder -> {
//            builder.serializerByType(Long.class, new BigNumberSerializer());
//            builder.serializerByType(Long.TYPE, new BigNumberSerializer());
//        };
//    }
//
//    /**
//     * 自定义序列化器：超过 JS 安全长度的转 String，否则转 Number
//     */
//    static class BigNumberSerializer extends JsonSerializer<Long> {
//        // JS 最大安全整数：2^53 - 1
//        private static final long MAX_SAFE_INTEGER = 9007199254740991L;
//        private static final long MIN_SAFE_INTEGER = -9007199254740991L;
//
//        @Override
//        public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
//            if (value == null) {
//                gen.writeNull();
//                return;
//            }
//            // 如果数值在 JS 安全范围内，直接写数字
//            if (value > MIN_SAFE_INTEGER && value < MAX_SAFE_INTEGER) {
//                gen.writeNumber(value);
//            } else {
//                // 超出范围（比如雪花ID），转成字符串
//                gen.writeString(value.toString());
//            }
//        }
//    }
//}