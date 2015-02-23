package com.github.rift.manager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/proxy")
@Controller
public class Proxy
{
	@Autowired
	private JdbcTemplate jt;

	@Transactional
	@RequestMapping(value = "/list", produces = "application/json")
	@ResponseBody
	public List<String> list(@AuthenticationPrincipal User user)
	{
		List<String> hostnames = jt.queryForList("select hostname from PROXY_HOST where username = ?", String.class, user.getUsername());
		
		return hostnames;
	}
}
