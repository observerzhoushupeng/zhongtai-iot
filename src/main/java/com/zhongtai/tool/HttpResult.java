package com.zhongtai.tool;

public class HttpResult {

	private int status_code;
	private String result;

	public int getStatus_code() {
		return status_code;
	}

	public void setStatus_code(int status_code) {
		this.status_code = status_code;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
	public HttpResult() {}
	
	public HttpResult(int status_code, String result) {
		this.status_code = status_code;
		this.result = result;
	}
}
