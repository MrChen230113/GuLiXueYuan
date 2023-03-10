package com.atguigu.eduservice.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.eduservice.entity.EduSubject;
import com.atguigu.eduservice.entity.Tree;
import com.atguigu.eduservice.entity.excel.SubjectData;
import com.atguigu.eduservice.listener.SubjectExcelListener;
import com.atguigu.eduservice.mapper.EduSubjectMapper;
import com.atguigu.eduservice.service.EduSubjectService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 课程科目 服务实现类
 * </p>
 *
 * @author testjava
 * @since 2020-02-29
 */
@Service
public class EduSubjectServiceImpl extends ServiceImpl<EduSubjectMapper, EduSubject> implements EduSubjectService {

    //添加课程分类
    @Override
    public void saveSubject(MultipartFile file,EduSubjectService subjectService) {
        try {
            //文件输入流
            InputStream in = file.getInputStream();
            //调用方法进行读取
            EasyExcel.read(in, SubjectData.class,new SubjectExcelListener(subjectService)).sheet().doRead();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //获取课程分类树
    @Override
    public List<Tree> getTree() {
        // 获取课程树的一级分类
        QueryWrapper<EduSubject> eduSubjectQueryWrapper = new QueryWrapper<>();
        eduSubjectQueryWrapper.eq("parent_id",'0');
        List<EduSubject> eduSubjects = this.baseMapper.selectList(eduSubjectQueryWrapper);
        List<Tree> objects = new ArrayList<>();
        for (int i = 0; i < eduSubjects.size(); i++) {
            Tree Tree = new Tree();
            BeanUtils.copyProperties(eduSubjects.get(i),Tree );
            objects.add(Tree);
        }
        return getNode(objects);
    }

    /**
     * 根据父亲id，查询课程
     * @param parentId 父亲节点id
     * @return
     */
    @Override
    public List<EduSubject> getNode(String parentId) {
        QueryWrapper<EduSubject> eduSubjectQueryWrapper = new QueryWrapper<>();
        eduSubjectQueryWrapper.eq("parent_id",parentId);
        return this.baseMapper.selectList(eduSubjectQueryWrapper);
    }

    //获取树的子节点
    public List<Tree> getNode(List<Tree> Trees){
        for (int i = 0; i < Trees.size(); i++) {
            QueryWrapper<EduSubject> eduSubjectQueryWrapper = new QueryWrapper<>();
            eduSubjectQueryWrapper.eq("parent_id", Trees.get(i).getId());
            List<EduSubject> nodes = this.baseMapper.selectList(eduSubjectQueryWrapper);
            List<Tree> objects = new ArrayList<>();
            for (int j = 0; j < nodes.size(); j++) {
                Tree Tree = new Tree();
                BeanUtils.copyProperties(nodes.get(j),Tree);
                objects.add(Tree);
            }
            if (objects.size() == 0){
                Trees.get(i).setChildren(objects);
                break;
            }else {
                Trees.get(i).setChildren(objects);
                getNode(objects);
            }
        }
            return Trees;
    }
}
