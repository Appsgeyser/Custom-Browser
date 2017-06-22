package acr.browser.lightning.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Created by roma on 16.06.2017.
 */

public class NewsCategory implements Serializable{
    private String name;
    private List<News> newsList;
    //------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<News> getNewsList() {
        return newsList;
    }

    public void setNewsList(List<News> newsList) {
        this.newsList = newsList;
    }
}
