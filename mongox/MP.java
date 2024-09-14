package com.example.mongo;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MP {
    public static Query to(Object obj){
        try {
            MP mp = new MP();
            Criteria criteria = mp.getFieldCriteria(obj);
            Query query = Query.query(criteria);
            return query;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private List<Field> getMatchField(Class clazz){
        Field[] fields =  clazz.getDeclaredFields();
        List<Field> result = new ArrayList<>();
        for(Field field:fields) {
            QueryField queryField = field.getAnnotation(QueryField.class);
            if(queryField==null)continue;
            result.add(field);
        }
        return result;
    }
    private List<QueryField> getMatchQueryField(Class clazz){
        Field[] fields =  clazz.getDeclaredFields();
        List<QueryField> result = new ArrayList<>();
        for(Field field:fields) {
            QueryField queryField = field.getAnnotation(QueryField.class);
            if(queryField==null)continue;
            result.add(queryField);
        }
        return result;
    }

    private Map<String,Map<QueryLogic,List<Criteria>>> initOwner(List<QueryField> queryFields){
        Map<String,Map<QueryLogic,List<Criteria>>> result = new HashMap<>();
        for(QueryField queryField:queryFields){
            if(result.containsKey(queryField.owner()))continue;
            // 初始化2个基本存储
            Map<QueryLogic,List<Criteria>> map = new HashMap<>();
            map.put(QueryLogic.AND,new ArrayList<>());
            map.put(QueryLogic.OR,new ArrayList<>());
            result.put(queryField.owner(),map);
        }
        return result;
    }

    private void addCriteriaList(QueryField queryField,List<Criteria> andList,List<Criteria> orList,Criteria criteria){
        if(queryField.queryLogic()==QueryLogic.AND){
            andList.add(criteria);
        }
        if(queryField.queryLogic()==QueryLogic.OR){
            orList.add(criteria);
        }
    }


    // 解析对象
    private  Criteria getFieldCriteria(Object obj) throws IllegalAccessException {
        Criteria criteria = new Criteria();
        List<Field> fields = getMatchField(obj.getClass());
        if(fields.size()==0){
            return criteria;
        }
        List<QueryField> queryFields = getMatchQueryField(obj.getClass());
        Map<String,Map<QueryLogic,List<Criteria>>> owner = initOwner(queryFields);
        // 初始化
        for(Field field:fields){
            QueryField queryField = field.getAnnotation(QueryField.class);
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
            Map<QueryLogic,List<Criteria>> currentQueryMap = owner.get(queryField.owner());
            List<Criteria> andCriteria = currentQueryMap.get(QueryLogic.AND);
            List<Criteria> orCriteria = currentQueryMap.get(QueryLogic.OR);
            switch (queryField.queryType()){
                case EQ:
                    addCriteriaList(queryField,andCriteria,orCriteria,new Criteria().where(key).is(fieldValue));
                    break;
                case NE:
                    addCriteriaList(queryField,andCriteria,orCriteria,new Criteria().where(key).ne(fieldValue));
                    break;
                case REGEX:
                    String regexValue = queryField.regex().replace("${-}",fieldValue.toString());
                    if(queryField.regexConfig() == null || queryField.regexConfig().length()==0){
                        addCriteriaList(queryField,andCriteria,orCriteria,new Criteria().where(key).regex(regexValue));
                    }else{
                        addCriteriaList(queryField,andCriteria,orCriteria,new Criteria().where(key).regex(regexValue, queryField.regexConfig()));
                    }
                    break;
                case GT:
                    addCriteriaList(queryField,andCriteria,orCriteria,new Criteria().where(key).gt(fieldValue));
                    break;
                case GE:
                    addCriteriaList(queryField,andCriteria,orCriteria,new Criteria().where(key).gte(fieldValue));
                    break;
                case LT:
                    addCriteriaList(queryField,andCriteria,orCriteria,new Criteria().where(key).lt(fieldValue));
                    break;
                case LE:
                    addCriteriaList(queryField,andCriteria,orCriteria,new Criteria().where(key).lte(fieldValue));
                    break;
                case IN:
                    if(fieldValue instanceof List){
                        addCriteriaList(queryField,andCriteria,orCriteria,new Criteria().where(key).in((List)fieldValue));
                    }else{
                        addCriteriaList(queryField,andCriteria,orCriteria,new Criteria().where(key).in(fieldValue));
                    }
                    break;
                case NOT_IN:
                    if(fieldValue instanceof List){
                        addCriteriaList(queryField,andCriteria,orCriteria,new Criteria().where(key).nin((List)fieldValue));
                    }else{
                        addCriteriaList(queryField,andCriteria,orCriteria,new Criteria().where(key).nin(fieldValue));
                    }
                    break;
            }
        }

        for(String ownerKey:owner.keySet()){
            List<Criteria> andList = owner.get(ownerKey).get(QueryLogic.AND);
            List<Criteria> orList = owner.get(ownerKey).get(QueryLogic.OR);
            if(andList.size()==0 && orList.size()==0)continue;
            //
            if(andList.size()>0 && orList.size()==0){
                criteria.andOperator(andList.toArray(new Criteria[0]));
            }
            if(orList.size()>0 && andList.size()==0){
                criteria.orOperator(orList.toArray(new Criteria[0]));
            }
            if(orList.size()>0 && andList.size()>0){
                Criteria[] or = orList.toArray(new Criteria[0]);
                Criteria[] newOr = new Criteria[or.length+1];
                for(int i =0;i<or.length;i++){
                    newOr[i]=or[i];
                }
                newOr[or.length]=new Criteria().andOperator(andList.toArray(new Criteria[0]));
                criteria.orOperator(newOr);
            }
        }
        return criteria;
    }
}
