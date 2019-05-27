package com.github.chaijunkun.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * 响应对象
 * @author chaijunkun
 * @since 2015年4月3日
 */
@JsonPropertyOrder(alphabetic = false)
public class Resp<T> {
	
	/**
	 * 生成成功返回对象
	 * @param msg
	 * @return
	 */
	public static <T> Resp<T> success(T msg){
		Resp<T> resp = new Resp<T>();
		resp.setCode(0);
		resp.setMsg(msg);
		return resp;
	}
	
	/**
	 * 生成失败返回对象
	 * @param msg
	 * @return
	 */
	public static Resp<String> fail(String msg){
		Resp<String> resp = new Resp<String>();
		resp.setCode(-1);
		resp.setMsg(msg);
		return resp;
	}

	/** 响应代码 */
	private Integer code;

	/** 响应消息 */
	private T msg;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public T getMsg() {
		return msg;
	}

	public void setMsg(T msg) {
		this.msg = msg;
	}

}
