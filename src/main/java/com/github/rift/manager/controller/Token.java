package com.github.rift.manager.controller;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/token")
@Controller
public class Token
{
	@RequestMapping(value = "/refresh", produces = "application/json")
	@ResponseBody
	public String refresh(@AuthenticationPrincipal User user)
	{
		System.out.println(user.getUsername());
		return "OK";
	}
}
