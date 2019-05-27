# 前言
如今互联网项目都采用HTTP接口形式进行开发。无论是Web调用还是智能设备APP调用，只要约定好参数形式和规则就能够协同开发。返回值用得最多的就是JSON形式。服务端除了保证正常的业务功能，还要经常对传进来的参数进行验证，例如某些参数不能为空，字符串必须含有可见字符，数值必须大于0等这样的要求。那么如何做到最佳实践，让接口开发的效率提升呢？今天我们就来聊一聊JSR 303和AOP的结合。

# 什么是JSR 303
首先JSR 303是Java的标准规范，根据官方文档的描述（https://jcp.org/en/jsr/proposalDetails?id=303）：在一个应用的不同层面（例如呈现层到持久层），验证数据是一个是反复共同的任务。许多时候相同的验证要在每一个独立的验证框架中出现很多次。为了提升开发效率，阻止重复造轮子，于是形成了这样一套规范。该规范定义了一个元数据模型，默认的元数据来源是注解（annotation）。针对该规范的验证API不是为某一个编程模型来开发的，因此它不束缚于Web或者持久化。也就是说不仅仅是服务端应用编程可以用它，甚至富客户端swing应用开发也可以用它。相关的入门参考资料可以参见我之前的博文：http://blog.csdn.net/chaijunkun/article/details/9083171，也可以参阅IBM开发者社区的一篇文章：http://www.ibm.com/developerworks/cn/java/j-lo-jsr303/。

# 什么是AOP
然后再聊聊AOP。AOP就是Aspect Oriented Programming（面向切面编程）的缩写。AOP 是一个概念，一个规范，本身并没有设定具体语言的实现，这实际上提供了非常广阔的发展的空间。AspectJ就是AOP的一个很悠久的实现，在Java语言中，他使用的范围很广。到底什么是切面呢？举个例子吧。在Spring MVC中，开发了若干个Controller（控制器），并且这些控制器负责不同的模块。每一个控制器中都有若干个public的方法来对应各自的@RequestMapping。现在我想增加一个日志，记录调用每个URL请求后端处理的时间。如果只有一两个public的方法还好一些，无非在方法开头加个开始时间startTime，在末尾加个结束时间endTime。endTime - startTime=执行时间，最后输出就好了。可是如果一个系统有几十上百个控制器方法呢？挨个写吗？老板说要改下日志格式呢？整个人会崩溃的！那我们就把这个描述抽象出来：public * com.github.chaijunkun.controller.*.*(..)，在包com.github.chaijunkun.controller下面的所有类，所有public的，无论有没有返回值，也无论参数是什么样的方法，全部聚合在一起。就像划定了一个满足特定条件的“圈”，那么这个“圈”就是切面，所有满足这个条件的方法都是”切点“。我们的编程就建立在这之上。只要在这个切面上加上开始时间，调用切点，再记录结束时间，最后输出就可以了。只写一次，改也很方便。

BTW，实现AOP有三种方式：①在编译期修改源代码；②在运行期字节码加载前修改字节码；③字节码加载后动态创建代理类的字节码。当采用第二种方式时如果你的项目在发布时使用了代码混淆，那么有些时候面向切面的代码将会失效，这点要特别注意。

# 实例
接下来我们就来个实例，说明一下Web项目中JSR 303为什么要和AOP结合。该实例的场景是返回JSON数据的接口，功能是对Student实体和Teacher实体进行CRUD操作：

#建立Web项目及其依赖
为了简化描述，使用maven来建立Web项目。使用JSR 303需要引入一个该规范实现使用的框架，这里使用Hibernate Validator。另外针对AOP，需要引入AspectJ相关依赖。具体如下：

```xml
<dependency>
	<groupId>org.hibernate</groupId>
	<artifactId>hibernate-validator</artifactId>
	<version>5.1.3.Final</version>
</dependency>
<dependency>
	<groupId>org.aspectj</groupId>
	<artifactId>aspectjrt</artifactId>
	<version>1.6.8</version>
</dependency>
<dependency>
	<groupId>org.aspectj</groupId>
	<artifactId>aspectjweaver</artifactId>
	<version>1.6.8</version>
</dependency>
```

另外还需要引入Spring Web和MVC相关的包，这里就不赘述了

# 配置Spring Servlet
这里我们要启用注解，并且打开Spring对JSR 303的支持，另外扫描指定包下的Controller进行实例化：

```xml
<bean id="validator" class="org.springframework.validation.beanvalidation.LocalValidatorFactoryBean" />
<mvc:annotation-driven />
<context:component-scan base-package="com.github.chaijunkun.controller" />
```

# 编写持久化对象
在本例中，为了简化代码，将传参VO与持久化PO共用一个。

Student实体：

```java
/**
 * 学生对象
 * @author chaijunkun
 * @since 2015年4月3日
 */
public class Student {
	
	@NotNull(groups = {Get.class, Del.class, Update.class})
	private Integer id;
	
	@NotBlank(groups = {Add.class, Update.class})
	private String name;
	
	@NotNull(groups = {Add.class, Update.class})
	private Boolean male;
	
	private Integer teacherId;
 
	//getters and setters
 
}
```

在Student实体约束中引入了groups。主要是针对不同场景下验证的字段不同。该参数必须是interface类型，不用实现，就是一个标记而已。声明如下：

```java
/**
 * 学生验证分组
 * @author chaijunkun
 * @since 2015年4月3日
 */
public interface StudentGroup {
	
	public static interface Add{}
	
	public static interface Del{}
	
	public static interface Get{}
	
	public static interface Update{}
 
}
```

Teachers实体：

```java
/**
 * 教师对象
 * @author chaijunkun
 * @since 2015年4月3日
 */
public class Teacher {
	
	@NotNull(groups = {Get.class, Del.class, Update.class})
	private Integer id;
	
	@NotBlank(groups = {Add.class, Update.class})
	private String name;
	
	@NotNull(groups = {Add.class, Update.class})
	private Boolean male;
 
	//getters and setters
 
}
```

同Student实体类似，需要定义一个Teacher实体专用的验证编组：

```java
/**
 * 教师验证分组
 * @author chaijunkun
 * @since 2015年4月3日
 */
public interface TeacherGroup {
	
	public static interface Add{}
	
	public static interface Del{}
	
	public static interface Get{}
	
	public static interface Update{}
 
}
```

# 编写虚拟的持久化服务
Student实体持久化服务：

```java
/**
 * 学生持久化服务
 * @author chaijunkun
 * @since 2015年4月3日
 */
@Service
public class StudentService {
	
	private static Map<Integer, Student> vDB = new HashMap<Integer, Student>();
	
	private static int counter = 1;
	
	public Integer add(Student student){
		student.setId(counter);
		vDB.put(counter, student);
		counter++;
		return student.getId();
	}
	
	public boolean del(Integer id){
		Student student = vDB.remove(id);
		return student != null ? true : false;
	}
	
	public Student get(Integer id){
		return vDB.get(id);
	}
	
	public boolean update(Student student){
		Student dbObj = vDB.get(student.getId());
		if (dbObj==null){
			return false;
		}else{
			vDB.put(student.getId(), student);
			return true;
		}
	}
 
}
```

Teacher实体持久化服务

```java
/**
 * 教师持久化服务
 * @author chaijunkun
 * @since 2015年4月3日
 */
@Service
public class TeacherService {
	
	private static Map<Integer, Teacher> vDB = new HashMap<Integer, Teacher>();
	
	private static int counter = 1;
	
	public Integer add(Teacher teacher){
		teacher.setId(counter);
		vDB.put(counter, teacher);
		counter++;
		return teacher.getId();
	}
	
	public boolean del(Integer id){
		Teacher teacher = vDB.remove(id);
		return teacher != null ? true : false;
	}
	
	public Teacher get(Integer id){
		return vDB.get(id);
	}
	
	public boolean update(Teacher teacher){
		Teacher dbObj = vDB.get(teacher.getId());
		if (dbObj==null){
			return false;
		}else{
			vDB.put(teacher.getId(), teacher);
			return true;
		}
	}
 
}
```

# 规定接口返回数据结构
返回数据结构为JSON。当出现错误时，格式为：{"code":-1,"msg":"必选参数丢失"}，当成功时，格式为：{"code":0,"msg":{返回数据}}

```java
/**
 * 响应对象
 * @author chaijunkun
 * @since 2015年4月3日
 */
@JsonPropertyOrder(alphabetic = false)
public class Resp<T> {
	
	/**
	 * 生成成功返回对象
	 * @param msg
	 * @return
	 */
	public static <T> Resp<T> success(T msg){
		Resp<T> resp = new Resp<T>();
		resp.setCode(0);
		resp.setMsg(msg);
		return resp;
	}
	
	/**
	 * 生成失败返回对象
	 * @param msg
	 * @return
	 */
	public static Resp<String> fail(String msg){
		Resp<String> resp = new Resp<String>();
		resp.setCode(-1);
		resp.setMsg(msg);
		return resp;
	}
 
	/** 响应代码 */
	private Integer code;
 
	/** 响应消息 */
	private T msg;
 
	//getters and setters
 
}
```

# 编写API接口
由于Teacher接口与Student接口类似，本文只给出一个接口代码

```java
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
```


使用JSR 303进行验证，需要在Controller参数前加入@Validated注解。如果指定特别的编组，需要将编组class作为参数附加给该注解。最后一个参数定义为BindingResult类型。这样，在进入该Controller方法后使用result.hassErrors()方法来判断参数是否通过了约束验证。若没通过，可以通过BindingResult对象来获取详细的错误信息。当然，这不是我们本文的用法，我们要突破这种麻烦的写法。
# 针对Controller方法的切面编程
由于例子总的所有Controller都放在com.github.chaijunkun.controller包下，因此切面的配置应该是这样（在dispatcher-servlet.xml中配置）：

```xml
<!-- JSR 303验证切面 -->
<bean id="jsrValidationAdvice" class="com.github.chaijunkun.aop.JSRValidationAdvice" />
<aop:config>
	<aop:pointcut id="jsrValidationPC" expression="execution(public * com.github.chaijunkun.controller.*.*(..))" />
	<aop:aspect id="jsrValidationAspect" ref="jsrValidationAdvice">
		<aop:around method="aroundMethod" pointcut-ref="jsrValidationPC" />
	</aop:aspect>
</aop:config>
```

重点来了，我们来看看JSRValidationAdvice是如何实现的：

```java
/**
 * JSR303验证框架统一处理
 * @author chaijunkun
 * @since 2015年4月1日
 */
public class JSRValidationAdvice {
 
	Logger logger = LoggerFactory.getLogger(JSRValidationAdvice.class);
 
	/**
	 * 判断验证错误代码是否属于字段为空的情况
	 * @param code 验证错误代码
	 */
	private boolean isMissingParamsError(String code){
		if (code.equals(NotNull.class.getSimpleName()) || code.equals(NotBlank.class.getSimpleName()) || code.equals(NotEmpty.class.getSimpleName())){
			return true;
		}else{
			return false;
		}
	}
 
	/**
	 * 切点处理
	 * @param joinPoint
	 * @return
	 * @throws Throwable
	 */
	public Object aroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
		BindingResult result = null;
		Object[] args = joinPoint.getArgs();
		if (args != null && args.length != 0){
			for (Object object : args) {
				if (object instanceof BindingResult){
					result = (BindingResult)object;
					break;
				}
			}
		}
		if (result != null && result.hasErrors()){
			FieldError fieldError = result.getFieldError();
			String targetName = joinPoint.getTarget().getClass().getSimpleName();
			String method = joinPoint.getSignature().getName();
			logger.info("验证失败.控制器:{}, 方法:{}, 参数:{}, 属性:{}, 错误:{}, 消息:{}", targetName, method, fieldError.getObjectName(), fieldError.getField(), fieldError.getCode(), fieldError.getDefaultMessage());
			String firstCode = fieldError.getCode();
			if (isMissingParamsError(firstCode)){
				return Resp.fail("必选参数丢失");
			}else{
				return Resp.fail("其他错误");
			}
		}
		return joinPoint.proceed();
	}
 
}
```

该切面处理方法属于围绕Controller方法的形式，在进入Controller方法前会先调用该切面的aroundMethod（别问为什么，看上文中这个配置：<aop:around method="aroundMethod" pointcut-ref="jsrValidationPC" />），切面方法要求第一个参数类型必须为org.aspectj.lang.ProceedingJoinPoint。进入切面方法后，遍历Controller的所有参数类型，看下有没有BindingResult类型的参数。如果有，就调用它，判断是否有错误。如果有错误，通过日志将详细信息输出。并且返回错误信息。如果没有错误，执行切点的proceed()方法，按预定Controller逻辑进行计算。



另外多说 一句，在非web项目中也可以使用JSR 303，当引入Hibernate Validator后我们可以使用下面语句来初始化一个Validator：

```java
protected static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
```

然后用这个validator去验证输入的参数（验证分组可以不填，使用默认分组；也可以指定一个或多个验证分组。得到的集合是所有违规数据，可以通过是否为空来判断是否存在违规，若不为空则对这个集合进行遍历从而得到违规信息的细节）：

```java
Set<ConstraintViolation<QueryParam>> commonValidate = validator.validate(param, CommonGroup.class);
if (CollectionUtils.isNotEmpty(commonValidate)){
	throw new IllegalArgumentException(commonValidate.iterator().next().getMessage());
}
```

# 实例总结
通过上面的例子，可以看到最终业务逻辑并没有验证代码，只需要注意参数前使用@Validated注解，在最后加入BindingResult类型参数即可。切面会自动帮你做验证检查。今后的接口开发只需要关注业务即可，恭喜你，再也不用为验证的事情烦心了。