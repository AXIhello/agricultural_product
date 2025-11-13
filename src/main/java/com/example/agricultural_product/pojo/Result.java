package com.example.agricultural_product.pojo; 

public class Result<T> { 

    private boolean success;
    private String message;
    private T data; // 使用泛型 T 作为 data 字段的类型

    private Result() {}

    // --- Getters and Setters ---
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


    /**
     * 创建一个成功的响应，包含数据
     * 静态方法也需要声明泛型 <E>，并接收 E 类型的参数
     */
    public static <E> Result<E> success(E data) {
        Result<E> result = new Result<>();
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    /**
     * 创建一个成功的响应，不包含数据
     */
    public static <E> Result<E> success() {
        return success(null);
    }
    
    /**
     * 创建一个失败的响应
     */
    public static <E> Result<E> error(String message) {
        Result<E> result = new Result<>();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
}