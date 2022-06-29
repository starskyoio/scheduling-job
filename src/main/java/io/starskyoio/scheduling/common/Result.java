package io.starskyoio.scheduling.common;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公共接口返回结果
 *
 * @author lizh
 * @date 2021-10-21
 */
@Data
@NoArgsConstructor
public final class Result<T> {
    private int code;
    private T data;
    private String message;

    private Result(int code, T data) {
        this.code = code;
        this.data = data;
    }

    private Result(int code, T data, String message) {
        this(code, data);
        this.message = message;
    }

    public static Result<?> ok(int code) {
        return new Result<>(code, null);
    }

    public static Result<?> ok() {
        return new Result<>(0, null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, data);
    }

    public static <T> Result<T> ok(int code, T data) {
        return new Result<>(code, data);
    }

    public static Result<?> fail(int code) {
        return new Result<>(code, null);
    }

    public static Result<?> fail(int code, String message) {
        return new Result<>(code, null, message);
    }

    public static <T> Result<T> fail(int code, T data, String message) {
        return new Result<>(code, data, message);
    }
}
