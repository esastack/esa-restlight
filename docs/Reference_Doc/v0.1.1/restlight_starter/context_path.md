---
sort: 12
---

## Context Path

Restlight支持全局Path，使用时需要做如下配置：
```profile
restlight.server.context-path=/global-path/
```
原始Controller方法为：
```java
@Controller
@RequestMapping("/restlight/employee/")
public class EmployeeController {

    @GetMapping("/list")
    @ResponseBody
    public List<Employee> listAll() {
        List<Employee> employeeList = new ArrayList<>(16);

        employeeList.add(new Employee("LiMing", 25, "1403063"));
        employeeList.add(new Employee("LiSi", 36, "1403064"));
        employeeList.add(new Employee("WangWu", 31, "1403065"));

        return employeeList;
    }
}
```
使用全局Path后的请求路径为：**/global-path/restlight/employee/list**

```note
健康检查对应的请求路径不受全局Path影响
```
