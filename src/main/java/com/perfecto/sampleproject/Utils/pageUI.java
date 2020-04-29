package com.perfecto.sampleproject.Utils;


import java.util.LinkedList;

public class pageUI {
    String name;
    String location ;
    LinkedList<UIElement> UIElements;

    public pageUI(String name, String location)
    {
        this.name=name;
        this.location=location;
        UIElements = new LinkedList<UIElement>();

        System.out.print("Page name: " +  this.name +"\n");
        System.out.print("Screenshot location: " +  this.location +"\n");
    }
    public void addUIElement(UIElement UIElement)
    {
        UIElements.add(UIElement);
    }

    public String getFullFileName()
    {
        return location+"/"+name+".jpg";
    }

    public String getNameWitoutSuff()
    {
        return location+"/"+name+"_";
    }

    public LinkedList<UIElement> getUIElements() {
        return UIElements;
    }
}
