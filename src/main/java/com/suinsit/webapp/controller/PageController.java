/**
 * 
 */
package com.suinsit.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author manuel
 *
 */
@Controller
public class PageController {
	@GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/console/{page}")
    public String secure(@PathVariable String page) {
        return "console/" + page;
    }
    @GetMapping("/resources")
	public String resourcesExample() {
		return "resources";
	}
}
