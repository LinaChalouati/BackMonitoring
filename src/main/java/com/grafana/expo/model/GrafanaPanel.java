package com.grafana.expo.model;

public class GrafanaPanel {
    private String type;
    private int id;
    private String title;
   // private String url;

    public String getType() {
        return type;
    }

    public void setPanelType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String setPanelTitle(String title) {
        return title;
    }

    public void setPanelId(String title) {
        this.title = title;
    }

    //public String getUrl() {
      //  return url;
    //}

  //  public void setPanelUrl(String url) {
     //   this.url = url;
    //}
}
