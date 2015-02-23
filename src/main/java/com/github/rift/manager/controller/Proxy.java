package com.github.rift.manager.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;

@RequestMapping("/proxy")
@Controller
public class Proxy
{
	@Autowired
	private JdbcTemplate jt;

	@Transactional
	@RequestMapping(value = "/data", produces = "application/json")
	@ResponseBody
	public Map<String, Object> data(@AuthenticationPrincipal User user)
	{
		List<String> hostnames = jt.queryForList("select hostname from PROXY_HOST where username = ?", String.class, user.getUsername());
		
		Map<String, Object> result = Maps.newHashMap();
		
		result.put("hostnames", createHostnameMap(hostnames));
		result.put("pac", createPacScript(hostnames));
		return result;
	}

	private String createPacScript(List<String> hostnames)
	{
		StringBuilder script = new StringBuilder();
		
		script.append("function FindProxyForURL(url, host) {\n" +
				"var p = 'PROXY 211.149.217.191:13128';\n" +
				"if (shExpMatch(host, '*.google.*'))\n" +
				"	return p;\n");

		script.append("var a = [");
		boolean first = true;
		for (String hostname : hostnames)
		{
			if (!first)
				script.append(",");
			script.append("'");
			script.append(hostname);
			script.append("'");
			first = false;
		}
		script.append("];\n" +
				"if (a.indexOf(host) != -1) return p;\n" +
				"return 'DIRECT';\n"+
				"}");
		return script.toString();
	}
	
	private Map<String, Boolean> createHostnameMap(List<String> hostnames)
	{
		HashMap<String, Boolean> map = Maps.newHashMap();
		for (String hostname : hostnames)
		{
			map.put(hostname, true);
		}
		return map;
	}
}
