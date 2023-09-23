package com.xuecheng.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author jxchen
 * @version 1.0
 * @description FreeMarker
 * @date 2023/9/23 19:33
 */
@Controller
public class FreeMarkerController {

    @GetMapping("/testfreemarker")
    public ModelAndView test() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "小明");
        modelAndView.setViewName("test");
        return modelAndView;
    }

}
