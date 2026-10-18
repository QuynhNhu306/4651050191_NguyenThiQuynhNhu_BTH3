
package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

public class HomeController extends Controller {

    public Result index() {
        return Results.ok("Product Management Service is running!");
    }
}
