package com.github.rift.manager.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;

@RequestMapping("/proxy")
@Controller
public class Proxy
{
	@Autowired
	private JdbcTemplate jt;
	
	private String defaultRoute = 
			"if (shExpMatch(host, '10.[0-9]+.[0-9]+.[0-9]+')) return d;\n" + 
			"if (shExpMatch(host, '172.[0-9]+.[0-9]+.[0-9]+')) return d;\n" + 
			"if (shExpMatch(host, '192.168.[0-9]+.[0-9]+')) return d;\n" + 
			"if (url.indexOf('/complete/search?client=chrome-omni') != -1)\n" + 
			"        return d;\n" + 
			"if (url.indexOf('http://clients1.google.com/generate_204') == 0)\n" + 
			"        return d;\n" + 
			"if (url.indexOf('http://chart.apis.google.com/') == 0)\n" + 
			"        return d;\n" + 
			"if (url.indexOf('http://toolbarqueries.google.com') == 0)\n" + 
			"        return d;\n";

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
				"var d = 'DIRECT';\n");
		
		script.append(defaultRoute);

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

	@Transactional
	@RequestMapping(value = "/add")
	@ResponseBody
	public void add(@AuthenticationPrincipal User user, @RequestParam String hostname)
	{
		String names[] = StringUtils.split(hostname, ",");
		for (String name : names)
		{
			if (StringUtils.isBlank(name))
				continue;
			int count = jt.queryForObject("select count(*) from proxy_host where username = ? and hostname = ?", Integer.class, user.getUsername(), name);
			if (count == 0)
			{
				jt.update("insert into proxy_host (username, hostname) values (?, ?)", user.getUsername(), name);
			}
		}
	}
	
	@Transactional
	@RequestMapping(value = "/remove")
	@ResponseBody
	public void remove(@AuthenticationPrincipal User user, @RequestParam String hostname)
	{
		String names[] = StringUtils.split(hostname, ",");
		for (String name : names)
		{
			if (StringUtils.isBlank(name))
				continue;
			jt.update("delete from proxy_host where username = ? and hostname = ?", user.getUsername(), name);
		}
	}
}
