package uk.ac.ed.inf.pizzadronz.models;

import uk.ac.ed.inf.pizzadronz.constant.DayOfWeek;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class Restaurant {

    private String name;
    private LngLat location;
    private Set<DayOfWeek> openingDays;
    private List<Pizza> menu;

    public Restaurant() {
    }

    public Restaurant(String name, LngLat location, Set<DayOfWeek> openingDays, List<Pizza> menu) {
        this.name = name;
        this.location = location;
        this.openingDays = openingDays;
        this.menu = menu;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LngLat getLocation() {
        return location;
    }

    public void setLocation(LngLat location) {
        this.location = location;
    }

    public Set<DayOfWeek> getOpeningDays() {
        return openingDays;
    }

    public void setOpeningDays(Set<DayOfWeek> openingDays) {
        this.openingDays = openingDays;
    }

    public List<Pizza> getMenu() {
        return menu;
    }

    public void setMenu(List<Pizza> menu) {
        this.menu = menu;
    }

    public boolean isOpenOn(String date) {
        DayOfWeek dayOfWeek = findDayOfWeekFromDate(date);
        return openingDays.contains(dayOfWeek);
    }

    private DayOfWeek findDayOfWeekFromDate(String date) {
        LocalDate localDate = LocalDate.parse(date);
        return DayOfWeek.valueOf(localDate.getDayOfWeek().name());
    }
}
