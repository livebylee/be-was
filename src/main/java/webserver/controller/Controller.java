package webserver.controller;

import http.HttpRequest;
import http.HttpResponse;

public interface Controller {

    void process(HttpRequest request, HttpResponse response);
}