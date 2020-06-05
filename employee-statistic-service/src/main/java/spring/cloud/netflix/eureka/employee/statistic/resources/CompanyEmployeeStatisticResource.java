package spring.cloud.netflix.eureka.employee.statistic.resources;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import spring.cloud.netflix.eureka.employee.statistic.entities.CompanyEmployee;
import spring.cloud.netflix.eureka.employee.statistic.services.StatisticService;

import java.util.List;

/**
 * REST-API для получения статистики над сотрудниками фирмы сервиса "employee-service".
 */
@RequestMapping("/company-employees-statistic")
@RestController // Комбинация @Controller и @ResponseBody. Возвращаемые компоненты преобразуются в / из JSON / XML.
public class CompanyEmployeeStatisticResource {

    private final StatisticService statisticService;

    private final String companyEmployeesUrl;

    private final RestTemplate restTemplate;

    public CompanyEmployeeStatisticResource(
            @Autowired EurekaClient eurekaClient,
            @Autowired StatisticService statisticService
    ) {
        this.statisticService = statisticService;
        this.restTemplate = new RestTemplate();

        Application application = eurekaClient.getApplication("employee-service");
        InstanceInfo instanceInfo = application.getInstances().get(0);

        this.companyEmployeesUrl = String.format(
                "http://%s:%d/company-employees/",
                instanceInfo.getHostName(),
                instanceInfo.getPort()
        );
    }

    // STATISTICS ----------------------------------------------------

    /**
     * Вычислить средний период работы в компании сотрудников
     * сумма количеств дней работы каждого поделить на число сотрудников
     * @return средний период работы в компании
     */
    @GetMapping("/average-work-period")
    public ResponseEntity<Double> calculateAverageWorkPeriodInCompany() {
        CompanyEmployee[] companyEmployees = restTemplate.getForObject(companyEmployeesUrl + "/all", CompanyEmployee[].class);

        return new ResponseEntity<>(
                statisticService.calculateAverageWorkPeriodInCompany(companyEmployees),
                HttpStatus.OK
        );
    }

    /**
     * Получить список отделов с наибольшим числом сотрудников
     * @return список названий отделов
     */
    @GetMapping("/largest-employee-number-department")
    public ResponseEntity<List<String>> getDepartmentWithLargestEmployeeNumber() {
        CompanyEmployee[] companyEmployees = restTemplate.getForObject(companyEmployeesUrl + "/all", CompanyEmployee[].class);

        return new ResponseEntity<>(
                statisticService.getDepartmentWithLargestEmployeeNumber(companyEmployees),
                HttpStatus.OK
        );
    }
}