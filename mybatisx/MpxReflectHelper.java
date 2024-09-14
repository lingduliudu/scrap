package com.spider.mybatix;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MpxReflectHelper {

    /**
     * @Description: 获取方法
     * @author Hao.Yuan
     * @date 2023/11/8
     */
    public static Method getMethod(Class clazz, String methodName){
        // 获取方法集合
        List<Method> methods = getAllMethod(clazz);;
        for(Method method:methods){
            if(method.getName().equals(methodName)){
                return method;
            }
        }
        return null;
    }

    /**
     * @Description: 获取方法
     * @author Hao.Yuan
     * @date 2023/11/8
     */
    public static Field getField(Class clazz, String fieldName){
        // 获取方法集合
        List<Field> fields = getAllField(clazz);;
        for(Field field:fields){
            if(field.getName().equals(fieldName)){
                return field;
            }
        }
        return null;
    }

    /**
     * @Description: 获取所有属性
     * @author Hao.Yuan
     * @date 2023/11/8
     */
    public static List<Field> getAllField(Class clazz){
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null){
            List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
            // 按照屬性名
            for(Field in:fields){
                boolean find = false;
                for(Field out:fieldList){
                    if(in.getName().equals(out.getName())){
                        find = true;
                        break;
                    }
                }
                if(!find){
                    fieldList.add(in);
                }
            }

            // 如果不存在則添加
            clazz = clazz.getSuperclass();
        }
        return fieldList;
    }

    /**
     * @Description: 判断一个对象中是否有属性含有某个注解
     * @author Hao.Yuan
     * @date 2023/11/9
     */
    public static boolean hasAnnotation(Class clazz, Class annotationClazz){
        List<Field> fieldList =getAllField(clazz);
        for(Field f:fieldList){
            Annotation a = f.getAnnotation(annotationClazz);
            if(a !=null){
                return true;
            }
        }
        return  false;
    }

    /**
     * @Description: 获取所有方法
     * @author Hao.Yuan
     * @date 2023/11/8
     */
    public static List<Method> getAllMethod(Class clazz){
        List<Method> methodList = new ArrayList<>();
        while (clazz != null){
            List<Method> parentMethods = new ArrayList<>(Arrays.asList(clazz.getDeclaredMethods()));
            // 按照屬性名
            for(Method in:methodList){
                boolean find = false;
                for(Method out:parentMethods){
                    if(in.getName().equals(out.getName())){
                        find = true;
                        break;
                    }
                }
                if(!find){
                    methodList.add(in);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return methodList;
    }
}