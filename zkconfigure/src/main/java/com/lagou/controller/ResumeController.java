package com.lagou.controller;

import com.lagou.pojo.Resume;
import com.lagou.service.IResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ResumeController {
    @Autowired
    private IResumeService resumeService;

    @RequestMapping("/list")
    public String list(){
        List<Resume> list = resumeService.list();
        return list.toString();
    }


}
