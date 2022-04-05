package cn.marwin.service;

import org.springframework.data.domain.PageRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

public interface TestService {
    File createUserExcelFile(PageRequest pageRequest);
}