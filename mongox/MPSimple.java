package com.example.mongo;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MPSimple {
    public static Query to(Object obj){
        try {
            MPSimple mp = new MPSimple();
            Criteria criteria = mp.getFieldCriteria(obj);
            Query query = Query.query(criteria);
            return query;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
    // 解析对象
    private  Criteria getFieldCriteria(Object obj) throws IllegalAccessException {
        Criteria criteria = new Criteria();
        Field[] fields =  obj.getClass().getDeclaredFields();
        List<Criteria> criteriaList = new ArrayList<>();
        Map<String,Criteria> ownerList = new HashMap<>();
        for(Field field:fields){
            QueryField queryField = field.getAnnotation(QueryField.class);
            if(queryField==null)continue;
            field.setAccessible(true);
            Object fieldValue = field.get(obj);
            if(queryField.ignoreType()==IgnoreType.NULL && fieldValue==null){
                continue;
            }
            if(queryField.ignoreType()==IgnoreType.EMPTY){
                if(fieldValue==null)continue;
                if(fieldValue.toString().length()==0)continue;
            }
            String key = queryField.value();
            if("".equals(key) && queryField.auto()){
                // 重赋值
                String fieldName = field.getName();
                String snakeCaseString = fieldName.replaceAll("([A-Z])", "_$1").toLowerCase();
                if (snakeCaseString.startsWith("_")) {
                    snakeCaseString = snakeCaseString.substring(1);
                }
                key = snakeCaseString;
            }

            switch (queryField.queryType()){
                case EQ:
                    criteriaList.add(new Criteria().where(key).is(fieldValue));
                    break;
                case NE:
                    criteriaList.add(new Criteria().where(key).ne(fieldValue));
                    break;
                case REGEX:
                    String regexValue = queryField.regex().replace("${-}",fieldValue.toString());
                    if(queryField.regexConfig() == null || queryField.regexConfig().length()==0){
                        criteriaList.add(new Criteria().where(key).regex(regexValue));
                    }else{
                        criteriaList.add(new Criteria().where(key).regex(regexValue, queryField.regexConfig()));
                    }
                    break;
                case GT:
                    criteriaList.add(new Criteria().where(key).gt(fieldValue));
                    break;
                case GE:
                    criteriaList.add(new Criteria().where(key).gte(fieldValue));
                    break;
                case LT:
                    criteriaList.add(new Criteria().where(key).lt(fieldValue));
                    break;
                case LE:
                    criteriaList.add(new Criteria().where(key).lte(fieldValue));
                    break;
                case IN:
                    if(fieldValue instanceof List){
                        criteriaList.add(new Criteria().where(key).in((List)fieldValue));
                    }else{
                        criteriaList.add(new Criteria().where(key).in((List)fieldValue));
                    }
                    break;
                case NOT_IN:
                    if(fieldValue instanceof List){
                        criteriaList.add(new Criteria().where(key).nin((List)fieldValue));
                    }else{
                        criteriaList.add(new Criteria().where(key).nin(fieldValue));
                    }
                    break;
            }
        }
        if(criteriaList.size()>0){
            criteria.andOperator(criteriaList.toArray(new Criteria[0]));
        }
        return criteria;
    }
}
