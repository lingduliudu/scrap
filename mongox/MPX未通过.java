package com.locktalk.mongox;

import com.icc.framework.api.annotation.mongo.IgnoreType;
import com.icc.framework.api.annotation.mongo.QueryField;
import com.icc.framework.api.annotation.mongo.QueryLogic;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MPX {
    public static Query to(Object obj){
        try {
            MPX mp = new MPX();
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

    private Map<String, Map<QueryLogic,List<Criteria>>> initOwner(List<QueryField> queryFields){
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
            if(queryField.ignoreType()== IgnoreType.EMPTY){
                if(fieldValue==null)continue;
                if(fieldValue.toString().length()==0)continue;
            }
            String[] keys = queryField.value();
            Map<QueryLogic,List<Criteria>> currentQueryMap = owner.get(queryField.owner());
            List<Criteria> andCriteria = currentQueryMap.get(QueryLogic.AND);
            List<Criteria> orCriteria = currentQueryMap.get(QueryLogic.OR);
            switch (queryField.queryType()){
                case EQ:
                    Criteria addCriteria = new Criteria();
                    for(String key:keys){
                        addCriteria.where(key).is(fieldValue);
                    }
                    addCriteriaList(queryField,andCriteria,orCriteria,addCriteria);
                    break;
                case NE:
                    addCriteria = new Criteria();
                    for(String key:keys){
                        addCriteria.where(key).ne(fieldValue);
                    }
                    addCriteriaList(queryField,andCriteria,orCriteria,addCriteria);
                    break;
                case REGEX:
                    String regexValue = queryField.regex().replace("${-}",fieldValue.toString());
                    addCriteria = new Criteria();
                    if(queryField.regexConfig() == null || queryField.regexConfig().length()==0){
                        for(String key:keys){
                            addCriteria.where(key).regex(regexValue);
                        }
                        addCriteriaList(queryField,andCriteria,orCriteria,addCriteria);
                    }else{
                        for(String key:keys){
                            addCriteria.where(key).regex(regexValue, queryField.regexConfig());
                        }
                        addCriteriaList(queryField,andCriteria,orCriteria,addCriteria);
                    }
                    break;
                case GT:
                    addCriteria = new Criteria();
                    for(String key:keys){
                        addCriteria.where(key).gt(fieldValue);
                    }
                    addCriteriaList(queryField,andCriteria,orCriteria,addCriteria);
                    break;
                case GE:
                    addCriteria = new Criteria();
                    for(String key:keys){
                        addCriteria.where(key).gte(fieldValue);
                    }
                    addCriteriaList(queryField,andCriteria,orCriteria,addCriteria);
                    break;
                case LT:
                    addCriteria = new Criteria();
                    for(String key:keys){
                        addCriteria.where(key).lt(fieldValue);
                    }
                    addCriteriaList(queryField,andCriteria,orCriteria,addCriteria);
                    break;
                case LE:
                    addCriteria = new Criteria();
                    for(String key:keys){
                        addCriteria.where(key).lte(fieldValue);
                    }
                    addCriteriaList(queryField,andCriteria,orCriteria,addCriteria);
                    break;
                case IN:
                    addCriteria = new Criteria();
                    if(fieldValue instanceof List){
                        for(String key:keys){
                            addCriteria.where(key).in((List)fieldValue);
                        }
                        addCriteriaList(queryField,andCriteria,orCriteria,addCriteria);
                    }else{
                        for(String key:keys){
                            addCriteria.where(key).in(fieldValue);
                        }
                        addCriteriaList(queryField,andCriteria,orCriteria,addCriteria);
                    }
                    break;
                case NOT_IN:
                    addCriteria = new Criteria();
                    if(fieldValue instanceof List){
                        for(String key:keys){
                            addCriteria.where(key).nin((List)fieldValue);
                        }
                        addCriteriaList(queryField,andCriteria,orCriteria,addCriteria);
                    }else{
                        for(String key:keys){
                            addCriteria.where(key).nin(fieldValue);
                        }
                        addCriteriaList(queryField,andCriteria,orCriteria,addCriteria);
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
