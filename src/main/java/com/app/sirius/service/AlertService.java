package com.app.sirius.service;

import com.app.sirius.config.Pagination;
import com.app.sirius.domain.Admin;
import com.app.sirius.domain.Alert;
import com.app.sirius.repository.AdminRepository;
import com.app.sirius.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlertService {

    private AlertRepository alertRepository;
    private AdminRepository adminRepository;

    @Autowired
    private void setAlertRepository(AlertRepository alertRepository){this.alertRepository =alertRepository;}
    @Autowired
    private void setAdminRepository(AdminRepository adminRepository){this.adminRepository= adminRepository;}

    public AlertService(){System.out.println("###LOG : AlertService() 생성");}

    @Transactional
    public List<Alert> detail(long alertId){
        List<Alert> list = new ArrayList<>();
        Alert alert = alertRepository.findByAlertId(alertId);

        if(alert != null){
            alertRepository.saveAndFlush(alert);
            list.add(alert);
        }
        return list;
    }

    //전체조회
    public List<Alert> list(){return alertRepository.findAll();}

    //페이징 전체조회
    public List<Alert> list(Integer page, Model model){
        if(page == null) page = 1;
        if(page < 1) page = 1;

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attrs.getRequest().getSession();

        //ALERTPAGES : 한 페이지당 몇개의 페이지 표시할지
        //PAGEROWS : 한 페이지에 몇개 글 리스트할지
        Integer alertPages = (Integer)session.getAttribute("alertPages");
        if(alertPages == null) alertPages = Pagination.ALERT_PAGES; //세션에 없으면 default(10) 값으로 동작
        Integer pageRows = (Integer)session.getAttribute("pageRows");
        if(pageRows == null) pageRows = Pagination.PAGE_ROWS; //세션에 없으면 default(10) 값으로 동작
        session.setAttribute("page",page); //현재 페이지 번호를 session에 저장

        Page<Alert> pageAlert = alertRepository.findAll(PageRequest.of(page-1, pageRows, Sort.by(Sort.Order.desc("alertId"))));

        long cnt = pageAlert.getTotalElements();
        int totalPage = pageAlert.getTotalPages();

        if(page > totalPage) page = totalPage;

        //페이징에 표시할 시작페이지 와 마지막페이지 계산
        int startPage = ((int)((page-1) / alertPages) * alertPages) + 1;
        int endPage = startPage + alertPages - 1;
        if(endPage >= totalPage) endPage = totalPage;

        model.addAttribute("cnt", cnt); //전체 글 개수
        model.addAttribute("page", page); //현재 페이지
        model.addAttribute("totalPage", totalPage); //총 페이지 수
        model.addAttribute("pageRows",pageRows); //한 페이지에 표시할 글 개수

        //페이징
        model.addAttribute("url", attrs.getRequest().getRequestURI());
        model.addAttribute("alertPages", alertPages); // 페이징에 표시할 숫자 개수
        model.addAttribute("startPage", startPage); //페이징에 표시할 시작페이지
        model.addAttribute("endPage", endPage); //페이징에 표시할 마지막 페이지

        //해당 페이지의 글 목록 조회
        List<Alert> list = pageAlert.getContent();
        model.addAttribute("alert_list", list);

        return list;
    }

    //단일 글 조회
    public List<Alert> selectByAlertId(Long alertId){
        List<Alert> list = new ArrayList<>();

        Alert alert = alertRepository.findByAlertId(alertId);
        if(alert != null){list.add(alert);}
        return list;
    }

    public int delete(Long alertId){
        int result = 0;
        Alert alert = alertRepository.findByAlertId(alertId);

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attrs.getRequest().getSession();
        Admin admin = (Admin) session.getAttribute("admin");

        if (admin!= null) {
            if(adminRepository.findByAdminId(admin.getAdminId())!=null && alert!=null){
                alertRepository.delete(alert);
                return 1;
            }
        }

        return 0;
    }


}
