package com.wash.entity;

public  class BaseEntity {
    /**
     * 从字符串转换为对象
     * @param str 要转换的字符串
     * @return 转换后的对象
     */
    public static BaseEntity fromString(String str) {
        throw new UnsupportedOperationException("此方法需要在子类中重写");
    }

    /**
     * 获取用户 ID
     *
     * @return 用户 ID
     */
    public Integer getUid() {
        return 0;
    }
}