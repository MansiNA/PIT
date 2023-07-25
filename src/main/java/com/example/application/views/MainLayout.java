package com.example.application.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.HighlightCondition;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class MainLayout extends AppLayout {

    public MainLayout(){
        
    createHeader();
    createDrawer();
    
    }

    private void createHeader() {
        H1 logo = new H1("Controlling AdminTool CRM");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.MEDIUM);

 //       String u = securityService.getAuthenticatedUser().getUsername();
 //       Button logout = new Button("Log out " + u, e -> securityService.logout()); // <2>

        var header = new HorizontalLayout(new DrawerToggle(), logo);//, logout);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo); // <4>
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(header);

    }
    private void createDrawer() {

        addToDrawer(new VerticalLayout(
                new RouterLink("Product-Hier-Subscriber", ListView.class)//,
             //   new RouterLink("Dashboard", DashboardView.class)
        ));


    }




}
