package com.personal.revenant.oulewaimai.bean;

public class UploadImgBean {

    /**
     * code : 1
     * msg : 修改成功
     * data : {"url":"http://xinlian.nxiapk.top\\recommend\\20181226\\33476b5dcd6de512c38ca28fb0a49ae7.jpeg"}
     */

    private int code;
    private String msg;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * url : http://xinlian.nxiapk.top\recommend\20181226\33476b5dcd6de512c38ca28fb0a49ae7.jpeg
         */

        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
