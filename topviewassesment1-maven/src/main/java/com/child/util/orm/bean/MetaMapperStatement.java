package com.child.util.orm.bean;

/**
 * 用于保存xml映射文件中的CRUD标签的相关信息。<br/>
 * <p/>
 * @author silent_child
 * @version 1.0
 **/

public class MetaMapperStatement {

    /**
     * 加上了namespace的全限定id
     */
    private String sqlId;
    /**
     * CURD的类型
     */
    private String sqlType;

    /**
     * 编写在xml映射文件中的原生sql语句
     */
    private String prototypeSql;
    /**
     * 每个CRUD中的返回值类型属性
     */
    private String resultType;
    public MetaMapperStatement() {}

    public MetaMapperStatement(String sqlId, String sqlType, String prototypeSql, String resultType) {
        this.sqlId = sqlId;
        this.sqlType = sqlType;
        this.prototypeSql = prototypeSql;
        this.resultType = resultType;
    }

    @Override
    public String toString() {
        return "MapperStatement{" +
                "sqlId='" + sqlId + '\'' +
                ", sqlType='" + sqlType + '\'' +
                ", prototypeSql='" + prototypeSql + '\'' +
                ", resultType='" + resultType + '\'' +
                '}';
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getSqlId() {
        return sqlId;
    }

    public void setSqlId(String sqlId) {
        this.sqlId = sqlId;
    }

    public String getPrototypeSql() {
        return prototypeSql;
    }

    public void setPrototypeSql(String prototypeSql) {
        this.prototypeSql = prototypeSql;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }
}
