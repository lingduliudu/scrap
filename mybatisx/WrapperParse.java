package com.icc.account.mybatisx;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.icc.framework.api.annotation.KV;
import com.icc.framework.api.annotation.mybatisx.IMPXConverter;
import com.icc.framework.api.annotation.mybatisx.IgnoreType;
import com.icc.framework.api.annotation.mybatisx.QueryField;
import com.icc.framework.api.annotation.mybatisx.QueryType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class WrapperParse {

    Set<QueryType> ignoreExclude = new HashSet<>();
    Set<QueryType> querySet = new HashSet<>();


    public WrapperParse() {
        // 忽略
        ignoreExclude.add(QueryType.INNER);
        ignoreExclude.add(QueryType.GROUP);
        ignoreExclude.add(QueryType.DESC);
        ignoreExclude.add(QueryType.ASC);
        ignoreExclude.add(QueryType.NULL);
        ignoreExclude.add(QueryType.NOT_NULL);
        // 查询类型
        querySet.add(QueryType.EQ);
        querySet.add(QueryType.NE);
        querySet.add(QueryType.GE);
        querySet.add(QueryType.GT);
        querySet.add(QueryType.LT);
        querySet.add(QueryType.LE);
        querySet.add(QueryType.LIKE);
        querySet.add(QueryType.MULTI_LIKE);
        querySet.add(QueryType.LIKEL);
        querySet.add(QueryType.LIKER);
        querySet.add(QueryType.IN);
        querySet.add(QueryType.NOT_IN);
        querySet.add(QueryType.NULL);
        querySet.add(QueryType.NOT_NULL);
        querySet.add(QueryType.INNER);
        querySet.add(QueryType.MATCH);
    }

    public QueryWrapper to(Object data) throws Exception {
        QueryWrapper query = Wrappers.query(data);
        return toSet(query);
    }

    /**
     * @Description: 判断是否是排序
     * @author Hao.Yuan
     * @date 2023/11/8
     */
    public static boolean isOrderBy(QueryType qt) {
        switch (qt) {
            case ASC:
                return true;
            case DESC:
                return true;
            case ASC_NOTEMPTY:
                return true;
            case DESC_NOTEMPTY:
                return true;
        }
        return false;
    }



    public QueryWrapper toSet(QueryWrapper<?> queryWrapper) throws Exception {
        Object t = queryWrapper.getEntity();
        List<Field> fields = MpxReflectHelper.getAllField(t.getClass());
        Map<String, Object> fieldMap = new HashMap<>();
        Map<Integer, Field> sortMap = new TreeMap<>();
        for (Field field : fields) {
            field.setAccessible(true);
            Object fieldValue = field.get(t);
            fieldMap.put(field.getName(), fieldValue);
        }
        for (Field field : fields) {
            QueryField qf = field.getAnnotation(QueryField.class);
            if (qf == null) continue;
            // 如果是排序的则需要进行特殊处理
            if (isOrderBy(qf.type())) {
                sortMap.put(qf.sortIndex(), field);
                continue;
            }
            field.setAccessible(true);
            Object fieldValue = field.get(t);
            resetQueryWrapper(queryWrapper,field, fieldValue, qf, fieldMap);

        }
        // 如果排序不是空
        if (!sortMap.isEmpty()) {
            for (Field field : sortMap.values()) {
                QueryField qf = field.getAnnotation(QueryField.class);
                field.setAccessible(true);
                Object fieldValue = field.get(t);
                resetQueryWrapper(queryWrapper,field, fieldValue, qf, fieldMap);
            }
        }
        return queryWrapper;
    }


    public void resetQueryWrapper(QueryWrapper<?> queryWrapper,Field field, Object value, QueryField qf, Map<String, Object> fieldMap) throws Exception{
        Object t = queryWrapper.getEntity();
        boolean simpleQueryTrim = qf.simpleQueryTrim();
        if (simpleQueryTrim && value != null && value instanceof String)value = ((String) value).trim();
        // 非忽略
        if (!ignoreExclude.contains(qf.type())) {
            if (qf.ignoreType() == IgnoreType.NULL && value == null) return;
            if (qf.ignoreType() == IgnoreType.EMPTY && (value == null || "".equals(value.toString()))) return;
        }
        Class clazz = qf.using();
        if(!clazz.getName().equals(IMPXConverter.class.getName()))value = ((IMPXConverter)clazz.newInstance()).to(value);
        String key = qf.value();
        if("".equals(key) && qf.auto()){
            // 重赋值
            String fieldName = field.getName();
            String snakeCaseString = fieldName.replaceAll("([A-Z])", "_$1").toLowerCase();
            if (snakeCaseString.startsWith("_")) {
                snakeCaseString = snakeCaseString.substring(1);
            }
            key = snakeCaseString;
        }
        switch (qf.type()) {
            case EQ:
                queryWrapper.eq(key, value);
                break;
            case NE:
                queryWrapper.ne(key, value);
                break;
            case LIKE:
                queryWrapper.like(key, value);
                break;
            case MULTI_LIKE:
                AtomicReference<Object> ref = new AtomicReference<>();
                ref.set(value);
                queryWrapper.and(qw->{
                    for(String mulitKey:qf.mulitValue()){
                        qw.or().like(mulitKey,ref.get());
                    }
                    return qw;
                });
                break;
            case LIKER:
                queryWrapper.likeRight(key, value);
                break;
            case LIKEL:
                queryWrapper.likeLeft(key, value);
                break;
            case IN:
                Collection checkCollection = (Collection) value;
                if (checkCollection.isEmpty()) {
                    if(qf.empty()!=null && !"".equals(qf.empty()))queryWrapper.apply(qf.empty());
                } else {
                    queryWrapper.in(key, checkCollection);
                }
                break;
            case NOT_IN:
                checkCollection = (Collection) value;
                if (checkCollection.isEmpty()) {
                    if(qf.empty()!=null && !"".equals(qf.empty()))queryWrapper.apply(qf.empty());
                } else {
                    queryWrapper.notIn(key, checkCollection);
                }
                break;
            case GT:
                queryWrapper.gt(key, value);
                break;
            case GE:
                queryWrapper.ge(key, value);
                break;
            case LT:
                queryWrapper.lt(key, value);
                break;
            case LE:
                queryWrapper.le(key, value);
                break;
            case NOT_NULL:
                queryWrapper.isNotNull(key);
                break;
            case NULL:
                queryWrapper.isNull(key);
                break;
            case GROUP:
                queryWrapper.groupBy(key);
                break;
            case ASC:
                queryWrapper.orderByAsc(key);
                break;
            case ASC_NOTEMPTY:
                if(value !=null && !"".equals(value))queryWrapper.orderByAsc(key);
                break;
            case DESC:
                queryWrapper.orderByDesc(key);
                break;
            case DESC_NOTEMPTY:
                if(value !=null && !"".equals(value))queryWrapper.orderByDesc(key);
                break;
            case INNER:
                String newKey = resetHoldValue(key, fieldMap);
                queryWrapper.apply(newKey);
                break;
            case MATCH:
                KV[] KVS = qf.match();
                if (KVS == null || KVS.length == 0) {
                    return;
                }
                // 如果是map对应的则需要进行map的拆分
                Map<String, String> map = new HashMap<>();
                for (KV kv : KVS) {
                    map.put(kv.K(), kv.V());
                }
                // 开始匹配操作
                String mapValue = map.get(String.valueOf(value));
                mapValue = resetHoldValue(mapValue, fieldMap);
                if (mapValue != null) queryWrapper.apply(mapValue);
                if (mapValue == null && !"".equals(qf.miss())) queryWrapper.apply(qf.miss());
                break;
            case LAST:
                queryWrapper.last(value.toString());
                break;
            default:
                break;
        }
    }

    /*
     * 重置
     *
     */
    public String resetHoldValue(String value,Map<String, Object> fieldMap) {
        // 展示列
        for (String key : fieldMap.keySet()) {
            Object val = fieldMap.get(key);
            if (val == null) {
                continue;
            }
            value = value.replace("${" + key + "}", val.toString());
        }
        return value;
    }
}