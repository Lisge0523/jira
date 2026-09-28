package com.jira.common;

import java.util.List;

/**
 * 分页响应体，作为 Result.data 返回：{ code:0, data:{ list:[...], total:100 } }
 *
 * 适用范围：仅「项目列表」等表格型列表。
 * 看板的任务查询不分页（需要一次性拿到项目下全部任务来渲染多列）。
 */
public class PageResult<T> {

    private List<T> list;
    private long total;

    public PageResult() {
    }

    public PageResult(List<T> list, long total) {
        this.list = list;
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
