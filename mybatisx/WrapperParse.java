package com.spider.mybatix;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.lang.reflect.Field;
import java.util.*;

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
        Map<String,List<Field>> orGroupMap = new HashMap<>();
        for (Field field : fields) {
            QueryField qf = field.getAnnotation(QueryField.class);
            if (qf == null) continue;
            // 如果属于特殊组
            if(qf.isOr()){
                if(orGroupMap.get(qf.group())!=null){
                    List<Field> gFs = orGroupMap.get(qf.group());
                    gFs.add(field);
                    orGroupMap.put(qf.group(),gFs);
                }else{
                    List<Field> gFs = new ArrayList<>();
                    gFs.add(field);
                    orGroupMap.put(qf.group(),gFs);
                }
                continue;
            }
            // 如果是排序的则需要进行特殊处理
            if (isOrderBy(qf.type())) {
                sortMap.put(qf.sortIndex(), field);
                continue;
            }
            field.setAccessible(true);
            Object fieldValue = field.get(t);
            resetQueryWrapper(queryWrapper, fieldValue, qf, fieldMap);

        }
        resetOrGroup(queryWrapper,orGroupMap,t,fieldMap);

        // 如果排序不是空
        if (!sortMap.isEmpty()) {
            for (Field field : sortMap.values()) {
                QueryField qf = field.getAnnotation(QueryField.class);
                field.setAccessible(true);
                Object fieldValue = field.get(t);
                resetQueryWrapper(queryWrapper, fieldValue, qf, fieldMap);
            }
        }
        return queryWrapper;
    }

    public boolean emptyOr(List<Field> orList,Object obj)throws Exception{
        for(Field f:orList){
            f.setAccessible(true);
            Object value = f.get(obj);
            QueryField qf = f.getAnnotation(QueryField.class);
            boolean simpleQueryTrim = qf.simpleQueryTrim();
            if (simpleQueryTrim && value != null && value instanceof String)value = ((String) value).trim();
            // 非忽略
            if (!ignoreExclude.contains(qf.type())) {
                if (qf.ignoreType() == IgnoreType.NULL) {
                    if (value == null) {
                        continue;
                    }
                }
                if (qf.ignoreType() == IgnoreType.EMPTY) {
                    if (value == null || "".equals(value.toString())) {
                        continue;
                    }
                }
            }
            // 非忽略需要查询
            if(!querySet.contains(qf.type()))continue;
            if(qf.type()==QueryType.MATCH){
                KV[] KVS = qf.match();
                if (KVS == null || KVS.length == 0) {
                    break;
                }
                // 如果是map对应的则需要进行map的拆分
                Map<String, String> map = new HashMap<>();
                for (KV kv : KVS) {
                    map.put(kv.K(), kv.V());
                }
                // 开始匹配操作
                String mapValue = map.get(String.valueOf(value));
                if (mapValue != null) return false;
                if (mapValue == null && !"".equals(qf.miss()))return false;
                continue;
            }

            return false;
        }
        return true;
    }



    public void resetOrGroup(QueryWrapper<?> queryWrapper,Map<String,List<Field>> orGroupMap,Object obj,Map<String, Object> fieldMap)throws Exception{
        if(orGroupMap.keySet().size() == 0)return;
        for(String groupKey:orGroupMap.keySet()){
            List<Field> orList = orGroupMap.get(groupKey);
            boolean emptyOr = emptyOr(orList,obj);
            if(emptyOr)continue;
            queryWrapper.and(x->{
                for(Field f:orList){
                    try {
                        f.setAccessible(true);
                        Object value = f.get(obj);
                        QueryField qf = f.getAnnotation(QueryField.class);
                        boolean simpleQueryTrim = qf.simpleQueryTrim();
                        if (simpleQueryTrim && value != null && value instanceof String)value = ((String) value).trim();
                        // 非忽略
                        if (!ignoreExclude.contains(qf.type())) {
                            if (qf.ignoreType() == IgnoreType.NULL && value == null) continue;
                            if (qf.ignoreType() == IgnoreType.EMPTY && (value == null || "".equals(value.toString()))) continue;
                        }
                        if (value != null && qf.frontTimeMode() == FrontTimeMode.DAYEND)  value = value.toString() + " 23:59:59";
                        if (value != null && qf.frontTimeMode() == FrontTimeMode.DAYSTART) value = value.toString() + " 00:00:00";

                        Class clazz = qf.using();
                        if(!clazz.getName().equals(IMPXConvert.class.getName()))value = ((IMPXConvert)clazz.newInstance()).convert(value);
                        String[] keys = qf.value();
                        switch (qf.type()){
                            case EQ:
                                for(String skey:keys){
                                    x.or().eq(skey, value);
                                }
                                break;
                            case NE:
                                for(String skey:keys){
                                    x.or().ne(skey, value);
                                }
                                break;
                            case LIKE:
                                for(String skey:keys){
                                    x.or().like(skey, value);
                                }
                                break;
                            case LIKER:
                                for(String skey:keys){
                                    x.or().likeRight(skey, value);
                                }
                                break;
                            case LIKEL:
                                for(String skey:keys){
                                    x.or().likeLeft(skey, value);
                                }
                                break;
                            case IN:
                                Collection checkCollection = (Collection) value;
                                if (checkCollection.isEmpty()) {
                                    if(qf.empty()!=null && !"".equals(qf.empty()))x.or().apply(qf.empty());
                                } else {
                                    for(String skey:keys){
                                        x.or().in(skey, checkCollection);
                                    }
                                }
                                break;
                            case NOT_IN:
                                checkCollection = (Collection) value;
                                if (checkCollection.isEmpty()) {
                                    if(qf.empty()!=null && !"".equals(qf.empty()))x.or().apply(qf.empty());
                                } else {
                                    for(String skey:keys){
                                        x.or().notIn(skey, checkCollection);
                                    }
                                }
                                break;
                            case GT:
                                for(String skey:keys){
                                    x.or().gt(skey, value);
                                }
                                break;
                            case GE:
                                for(String skey:keys){
                                    x.or().ge(skey, value);
                                }
                                break;
                            case LT:
                                for(String skey:keys){
                                    x.or().lt(skey, value);
                                }
                                break;
                            case LE:
                                for(String skey:keys){
                                    x.or().le(skey, value);
                                }
                                break;
                            case NOT_NULL:
                                for(String skey:keys){
                                    x.or().isNotNull(skey);
                                }
                                break;
                            case NULL:
                                for(String skey:keys){
                                    x.or().isNull(skey);
                                }
                                break;
                            case INNER:
                                for(String skey:keys){
                                    String newKey = resetHoldValue(skey, fieldMap);
                                    x.or().apply(newKey);
                                }
                                break;
                            case MATCH:
                                KV[] KVS = qf.match();
                                if (KVS == null || KVS.length == 0) {
                                    break;
                                }
                                // 如果是map对应的则需要进行map的拆分
                                Map<String, String> map = new HashMap<>();
                                for (KV kv : KVS) {
                                    map.put(kv.K(), kv.V());
                                }
                                // 开始匹配操作
                                String mapValue = map.get(String.valueOf(value));
                                mapValue = resetHoldValue(mapValue, fieldMap);
                                if (mapValue != null) x.or().apply(mapValue);
                                if (mapValue == null && !"".equals(qf.miss())) x.or().apply(qf.miss());
                                break;
                        }
                    } catch (IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }

                }
            });
        }

    }

    public void resetQueryWrapper(QueryWrapper<?> queryWrapper, Object value, QueryField qf, Map<String, Object> fieldMap) throws Exception{
        Object t = queryWrapper.getEntity();
        boolean simpleQueryTrim = qf.simpleQueryTrim();
        if (simpleQueryTrim && value != null && value instanceof String)value = ((String) value).trim();
        // 非忽略
        if (!ignoreExclude.contains(qf.type())) {
            if (qf.ignoreType() == IgnoreType.NULL && value == null) return;
            if (qf.ignoreType() == IgnoreType.EMPTY && (value == null || "".equals(value.toString()))) return;
        }
        if (value != null && qf.frontTimeMode() == FrontTimeMode.DAYEND)  value = value.toString() + " 23:59:59";
        if (value != null && qf.frontTimeMode() == FrontTimeMode.DAYSTART) value = value.toString() + " 00:00:00";
        Class clazz = qf.using();
        if(!clazz.getName().equals(IMPXConvert.class.getName()))value = ((IMPXConvert)clazz.newInstance()).convert(value);
        String[] keys = qf.value();
        switch (qf.type()) {
            case EQ:
                for(String skey:keys){
                    queryWrapper.eq(skey, value);
                }

                break;
            case NE:
                for(String skey:keys){
                    queryWrapper.ne(skey, value);
                }
                break;
            case LIKE:
                for(String skey:keys){
                    queryWrapper.like(skey, value);
                }
                break;
            case LIKER:
                for(String skey:keys){
                    queryWrapper.likeRight(skey, value);
                }
                break;
            case LIKEL:
                for(String skey:keys){
                    queryWrapper.likeLeft(skey, value);
                }
                break;
            case IN:
                Collection checkCollection = (Collection) value;
                if (checkCollection.isEmpty()) {
                    if(qf.empty()!=null && !"".equals(qf.empty()))queryWrapper.apply(qf.empty());
                } else {
                    for(String skey:keys){
                        queryWrapper.in(skey, checkCollection);
                    }
                }
                break;
            case NOT_IN:
                checkCollection = (Collection) value;
                if (checkCollection.isEmpty()) {
                    if(qf.empty()!=null && !"".equals(qf.empty()))queryWrapper.apply(qf.empty());
                } else {
                    for(String skey:keys){
                        queryWrapper.notIn(skey, checkCollection);
                    }
                }
                break;
            case GT:
                for(String skey:keys){
                    queryWrapper.gt(skey, value);
                }
                break;
            case GE:
                for(String skey:keys){
                    queryWrapper.ge(skey, value);
                }
                break;
            case LT:
                for(String skey:keys){
                    queryWrapper.lt(skey, value);
                }
                break;
            case LE:
                for(String skey:keys){
                    queryWrapper.le(skey, value);
                }
                break;
            case NOT_NULL:
                for(String skey:keys){
                    queryWrapper.isNotNull(skey);
                }
                break;
            case NULL:
                for(String skey:keys){
                    queryWrapper.isNull(skey);
                }
                break;
            case GROUP:
                for(String skey:keys){
                    queryWrapper.groupBy(skey);
                }
                break;
            case ASC:
                for(String skey:keys){
                    queryWrapper.orderByAsc(skey);
                }
                break;
            case ASC_NOTEMPTY:
                for(String skey:keys){
                    queryWrapper.orderByAsc(skey);
                }
                break;
            case DESC:
                for(String skey:keys){
                    queryWrapper.orderByDesc(skey);
                }
                break;
            case DESC_NOTEMPTY:
                for(String skey:keys){
                    queryWrapper.orderByDesc(skey);
                }
                break;
            case INNER:
                for(String skey:keys){
                    String newKey = resetHoldValue(skey, fieldMap);
                    queryWrapper.apply(newKey);
                }
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
                for(String skey:keys){
                    queryWrapper.last(value.toString());
                }
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