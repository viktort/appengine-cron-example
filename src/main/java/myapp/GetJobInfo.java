package myapp;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by viktor.trako on 13/11/16.
 */

@WebServlet(name = "dataflowscheduler", value = "/info/job")
public class GetJobInfo extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    System.out.println("req = " + req.getParameter("name"));
    if (req.getParameter("name") == "minimalwordcount") {
      getMinimalWordCountInfo(req, resp);
    } else {
      resp.setContentType("text/plain");
      resp.getWriter().println("{ \"scheduled\": \"obj listing scheduled jobs and paths\""
          + " , \"version\": \"1.0\"}");
    }

  }

  public void getMinimalWordCountInfo(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    if (req.getParameter("name") == "minimalwordcount") {
      resp.setContentType("text/plain");
      resp.getWriter().println("{ \"name\": \"ScheduledMinimalWordCount\" , \"version\": \"1.0\","
          + " \"runs\": \"daily\"}");
    }
  }
}
