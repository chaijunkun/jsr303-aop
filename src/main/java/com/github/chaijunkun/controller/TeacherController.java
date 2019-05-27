package com.github.chaijunkun.controller;

import com.github.chaijunkun.service.TeacherService;
import com.github.chaijunkun.validation.group.TeacherGroup;
import com.github.chaijunkun.entity.Resp;
import com.github.chaijunkun.entity.Teacher;

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
@RequestMapping(value = "teacher")
public class TeacherController {
	
	@Autowired
	private TeacherService teacherService;
	
	@ResponseBody
	@RequestMapping(value = "add", method = {RequestMethod.POST})
	public Resp<?> add(@Validated(TeacherGroup.Add.class) Teacher teacher, BindingResult result){
		Integer id = teacherService.add(teacher);
		if (id == null){
			return Resp.fail("添加学生信息失败");
		}else{
			return Resp.success(id);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "del", method = {RequestMethod.GET})
	public Resp<?> del(@Validated(TeacherGroup.Del.class) Teacher teacher, BindingResult result){
		if (teacherService.del(teacher.getId())){
			return Resp.success(true);
		}else{
			return Resp.fail("删除学生信息失败");
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "get", method = {RequestMethod.GET})
	public Resp<?> get(@Validated(TeacherGroup.Get.class) Teacher teacher, BindingResult result){
		Teacher data = teacherService.get(teacher.getId());
		if (data == null){
			return Resp.fail("未找到指定学生");
		}else{
			return Resp.success(data);
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "update", method = {RequestMethod.POST})
	public Resp<?> update(@Validated(TeacherGroup.Update.class) Teacher teacher, BindingResult result){
		if (teacherService.update(teacher)){
			return Resp.success(true);
		}else{
			return Resp.fail("更新学生信息失败");
		}
	}
	
}
