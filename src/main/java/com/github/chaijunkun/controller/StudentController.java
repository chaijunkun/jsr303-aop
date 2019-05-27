package com.github.chaijunkun.controller;

import com.github.chaijunkun.service.StudentService;
import com.github.chaijunkun.validation.group.StudentGroup;
import com.github.chaijunkun.entity.Resp;
import com.github.chaijunkun.entity.Student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 学生控制器
 * @author chaijunkun
 * @since 2015年4月3日
 */
@Controller
@RequestMapping(value = "student")
public class StudentController {
	
	@Autowired
	private StudentService studentService;
	
	@ResponseBody
	@RequestMapping(value = "add", method = {RequestMethod.GET})
	public Resp<?> add(@Validated(StudentGroup.Add.class) Student student, BindingResult result){
		Integer id = studentService.add(student);
		if (id == null){
			return Resp.fail("添加学生信息失败");
		}else{
			return Resp.success(id);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "del", method = {RequestMethod.GET})
	public Resp<?> del(@Validated(StudentGroup.Del.class) Student student, BindingResult result){
		if (studentService.del(student.getId())){
			return Resp.success(true);
		}else{
			return Resp.fail("删除学生信息失败");
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "get", method = {RequestMethod.GET})
	public Resp<?> get(@Validated(StudentGroup.Get.class) Student student, BindingResult result){
		Student data = studentService.get(student.getId());
		if (data == null){
			return Resp.fail("未找到指定学生");
		}else{
			return Resp.success(data);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "update", method = {RequestMethod.POST})
	public Resp<?> update(@Validated(StudentGroup.Update.class) Student student, BindingResult result){
		if (studentService.update(student)){
			return Resp.success(true);
		}else{
			return Resp.fail("更新学生信息失败");
		}
	}
	
}
