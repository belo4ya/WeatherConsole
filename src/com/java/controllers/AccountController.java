package com.java.controllers;

import com.java.exceptions.ApiException;
import com.java.exceptions.CityNotExistException;
import com.java.exceptions.ServiceNotExistException;
import com.java.models.DataBase;
import com.java.models.OpenWeatherMap;
import com.java.models.User;
import com.java.models.Weather;
import com.java.views.AccountView;
import com.java.views.Menu;
import com.java.views.ScreenSpacer;
import com.java.views.Service1View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class AccountController {
    private User user;
    private final Menu menu = new Menu();
    private final HashMap<Menu.Commands, MenuItem> addresses = new HashMap<Menu.Commands, MenuItem>(){{
        put(Menu.Commands.logOut, AccountController.this::logOut);
        put(Menu.Commands.setCity, AccountController.this::setCity);
        put(Menu.Commands.setService, AccountController.this::setService);
        put(Menu.Commands.updateCity, AccountController.this::updateCity);
        put(Menu.Commands.updateService, AccountController.this::updateService);

        put(Menu.Commands.service1Current, AccountController.this::displayService1Current);
        put(Menu.Commands.service1Hourly, AccountController.this::displayService1Hourly);
        put(Menu.Commands.service1Daily, AccountController.this::displayService1Daily);
    }};

    public AccountController(User user) {
        this.user = user;
    }

    public void displayHeader() {
        String city = user.getCity();
        Integer serviceId = user.getServiceId();
        String weatherService;
        try {
            weatherService = DataBase.getInstance().getService(serviceId);
        } catch (ServiceNotExistException e) {
            weatherService = "unspecified";
        }

        if (city == null) {
            city = "unspecified";
        }

        AccountView.displayHeader(user.getUsername(), city, weatherService);
        ScreenSpacer.smallIndent();
    }

    public void updateMenu() {
        menu.reset();

        if (user.getCity() == null) {
            menu.put(Menu.Commands.setCity);
        }

        if (user.getServiceId() == null || user.getServiceId() == 0) {
            menu.put(Menu.Commands.setService);
        }

        if (user.getCity() != null && user.getServiceId() != null && user.getServiceId() != 0) {
            Integer id = user.getServiceId();
            if (id == 1) {
                menu.put(Menu.Commands.service1Current);
                menu.put(Menu.Commands.service1Hourly);
                menu.put(Menu.Commands.service1Daily);
            } else if (id == 2) {
                menu.put(Menu.Commands.service2Current);
                menu.put(Menu.Commands.service2Hourly);
                menu.put(Menu.Commands.service2Part);
                menu.put(Menu.Commands.service2Daily);
            }

            menu.put(Menu.Commands.updateCity);
            menu.put(Menu.Commands.updateService);
        }

        menu.put(Menu.Commands.logOut);
    }

    public void menuNavigate() {
        updateMenu();
        displayHeader();
        menu.display();
        ScreenSpacer.smallIndent();
        int item = InputController.menuItemInput(menu);
        addresses.get(menu.get(item)).select();
    }

    public void setCity() {
        ScreenSpacer.safelyClean();
        displayHeader();
        String city = InputController.cityInput();
        user.setCity(city);
        DataBase.getInstance().setCity(user);
        menuNavigate();
    }

    public void setService() {
        ScreenSpacer.safelyClean();
        displayHeader();

        menu.reset();
        menu.put(Menu.Commands.service1Name);
        menu.put(Menu.Commands.service2Name);
        menu.display();

        ScreenSpacer.smallIndent();
        int item = InputController.menuItemInput(menu);
        user.setServiceId(item);
        DataBase.getInstance().setServiceId(user);
        menuNavigate();
    }

    public void updateCity() {
        setCity();
    }

    public void updateService() {
        setService();
    }

    public void logOut() {
        menu.reset();
        ScreenSpacer.safelyClean();
        user = AuthorizationController.authorize();
        ScreenSpacer.safelyClean();
        menuNavigate();
    }

    public void displayService1Current() {
        Weather weather;
        try {
            weather = OpenWeatherMap.getInstance().getCurrent(user.getCity());
        } catch (ApiException | CityNotExistException | IOException e) {
            Service1View.badApiResponse();
            return;
        }

        ScreenSpacer.safelyClean();
        displayHeader();
        Service1View.currentWeather(weather);
        menu.backToMainView();
        InputController.in.nextLine();
        ScreenSpacer.safelyClean();
        menuNavigate();
    }

    public void displayService1Hourly() {
        ArrayList<Weather> weatherList;
        try {
            weatherList = OpenWeatherMap.getInstance().getHourly(user.getCity());
        } catch (ApiException | CityNotExistException | IOException e) {
            Service1View.badApiResponse();
            return;
        }

        ScreenSpacer.safelyClean();
        displayHeader();
        Service1View.hourlyWeather(weatherList);
        menu.backToMainView();
        InputController.in.nextLine();
        ScreenSpacer.safelyClean();
        menuNavigate();
    }

    public void displayService1Daily() {
        ArrayList<Weather> weatherList;
        try {
            weatherList = OpenWeatherMap.getInstance().getDaily(user.getCity());
        } catch (ApiException | CityNotExistException | IOException e) {
            Service1View.badApiResponse();
            return;
        }

        ScreenSpacer.safelyClean();
        displayHeader();
        Service1View.dailyWeather(weatherList);
        menu.backToMainView();
        InputController.in.nextLine();
        ScreenSpacer.safelyClean();
        menuNavigate();
    }

}

@FunctionalInterface
interface MenuItem {
    void select();
}
