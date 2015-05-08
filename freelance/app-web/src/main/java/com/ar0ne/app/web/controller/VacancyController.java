package com.ar0ne.app.web.controller;

import com.ar0ne.app.core.request.Vacancy;
import com.ar0ne.app.core.user.UserAbstract;
import com.ar0ne.app.service.UserService;
import com.ar0ne.app.service.VacancyService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class VacancyController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private VacancyService vacancyService;

    
    @RequestMapping(value = { "/findJob" }, method = RequestMethod.GET)
    public ModelAndView findJobPage() {
        
        ModelAndView modelAndView = new ModelAndView("findJob");
        
        List<Vacancy> vacancyList = vacancyService.getOpenVacancy();
        modelAndView.addObject("vacancyList", vacancyList);
        
        return modelAndView;
    }
    
    @RequestMapping(value = "/addVacancy", method = RequestMethod.GET)
    public ModelAndView addVacancyPage() {
        
        ModelAndView modelAndView = new ModelAndView("addVacancy");
//        modelAndView.addObject("userId", userId);
        
        return modelAndView;
    }
    
    
    @RequestMapping(value = "/submitNewVacancy", method = RequestMethod.POST)
    public String addNewVacancyAction(  @RequestParam("title")          String   title,
                                        @RequestParam("description")    String   description,
                                        @RequestParam("payment")        String   payment ) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();

        if (login != null) {
            UserAbstract user = userService.findUserByLogin(login);
            if (user != null) {
        
                Vacancy vacancy = new Vacancy();
                vacancy.setPayment(payment);
                vacancy.setDescription(description);
                vacancy.setTitle(title);
                vacancy.setStatus(false);
                vacancy.setUserId(user.getId());

                vacancyService.addVacancy(vacancy);
            } else {
                System.out.println("User didn't find!");
            }
        } else {
            System.out.println("Problem with authentification!");
        }
        
        return "redirect:/userProfile";
    }
    
    
    // REST for ajax 
    @RequestMapping(value = "/deleteVacancy", method = RequestMethod.POST)
    public ResponseEntity deleteVacancyAction( @RequestParam("vacancyId") long vacancyId ) {
               
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();
        
        if (login != null) {
            UserAbstract user = userService.findUserByLogin(login);
            Vacancy vacancy = vacancyService.findVacancyById(vacancyId);
            if( user != null ) {
                if (user.getId() == vacancy.getUserId()) {
                    // only author of vacancy can delete (and admin)
                    vacancyService.deleteVacancy(vacancyId);
                    return new ResponseEntity("", HttpStatus.OK);
                }
            }
        }
        
        return new ResponseEntity("", HttpStatus.NOT_FOUND);
    }
    
    
    @RequestMapping(value = "/vacancy/{id}", method = RequestMethod.GET)
    public ModelAndView vacancyFullPage(@PathVariable("id") long vacancyId) {
        
        ModelAndView modelAndView = null;
        
        Vacancy vacancy = vacancyService.findVacancyById(vacancyId);
        
        if(vacancy == null) {
            System.out.println("Vacancy with id = " + vacancyId + " not found!");
            modelAndView = new ModelAndView("redirect:/userProfile");
        } else {
            modelAndView = new ModelAndView("vacancy");
            modelAndView.addObject("vacancy", vacancy);
            
            UserAbstract vacancy_user = userService.findUserById(vacancy.getUserId());
            
            modelAndView.addObject("vacancy_user", vacancy_user);
            
        }
        
        
        
        return modelAndView;
    }
    
}