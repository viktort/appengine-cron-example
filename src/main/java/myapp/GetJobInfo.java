package myapp;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by viktor.trako on 13/11/16.
 */

@WebServlet(name = "dataflowscheduler", value = "/minimalwc/info")
public class GetJobInfo extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.setContentType("text/plain");
    resp.getWriter().println("{ \"name\": \"Vitez\" , \"version\": \"1.0\","
        + " \"running_daflow\": \"ScheduledMinimalWordCount\"}");
  }
}
