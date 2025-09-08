package com.cleveronion.blog.presentation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 用户列表响应DTO
 * 用于封装分页查询用户列表的响应数据
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Schema(description = "用户列表响应")
public class UserListResponse {
    
    @Schema(description = "用户列表")
    private List<UserResponse> users;
    
    @Schema(description = "总记录数")
    private long totalCount;
    
    @Schema(description = "当前页码（从0开始）")
    private int currentPage;
    
    @Schema(description = "每页大小")
    private int pageSize;
    
    @Schema(description = "总页数")
    private int totalPages;
    
    @Schema(description = "是否有下一页")
    private boolean hasNext;
    
    @Schema(description = "是否有上一页")
    private boolean hasPrevious;
    
    /**
     * 默认构造函数
     */
    public UserListResponse() {
    }
    
    /**
     * 全参数构造函数
     * 
     * @param users 用户列表
     * @param totalCount 总记录数
     * @param currentPage 当前页码
     * @param pageSize 每页大小
     */
    public UserListResponse(List<UserResponse> users, long totalCount, int currentPage, int pageSize) {
        this.users = users;
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
    }
    
    /**
     * 静态工厂方法，用于创建UserListResponse实例
     * 
     * @param users 用户列表
     * @param totalCount 总记录数
     * @param currentPage 当前页码
     * @param pageSize 每页大小
     * @return UserListResponse实例
     */
    public static UserListResponse of(List<UserResponse> users, long totalCount, int currentPage, int pageSize) {
        return new UserListResponse(users, totalCount, currentPage, pageSize);
    }
    
    // Getter 和 Setter 方法
    
    public List<UserResponse> getUsers() {
        return users;
    }
    
    public void setUsers(List<UserResponse> users) {
        this.users = users;
    }
    
    public long getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
        // 重新计算总页数和分页状态
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        // 重新计算分页状态
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        // 重新计算总页数和分页状态
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
    
    @Override
    public String toString() {
        return "UserListResponse{" +
                "users=" + users +
                ", totalCount=" + totalCount +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", totalPages=" + totalPages +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                '}';
    }
}