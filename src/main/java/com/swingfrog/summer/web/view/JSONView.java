package com.swingfrog.summer.web.view;

import com.alibaba.fastjson.JSON;

public class JSONView extends TextView {

	public JSONView(JSON json) {
		super(json.toJSONString());
	}

	@Override
	public String toString() {
		return "JSONView";
	}
	
}
