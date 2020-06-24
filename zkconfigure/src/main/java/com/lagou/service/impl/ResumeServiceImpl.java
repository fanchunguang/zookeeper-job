package com.lagou.service.impl;

import com.lagou.ZkconfigureApplication;
import com.lagou.pojo.Resume;
import com.lagou.service.IResumeService;
import com.lagou.util.JdbcUtil;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeServiceImpl implements IResumeService {

    public List<Resume> list(){
        Connection conn = null;
        PreparedStatement ps=null;
        ResultSet rs=null;

        ArrayList<Resume> resumes = new ArrayList<>();
        try {
            conn = JdbcUtil.getConnection();
            String sql="select * from tb_resume";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()){
                long id = rs.getLong("id");
                String address = rs.getString("address");
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                Resume resume = new Resume(id, address, name, phone);
                resumes.add(resume);
            }
            return resumes;
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }finally {
            //关闭链接
            JdbcUtil.closeConnection(conn);
        }
    }


}
