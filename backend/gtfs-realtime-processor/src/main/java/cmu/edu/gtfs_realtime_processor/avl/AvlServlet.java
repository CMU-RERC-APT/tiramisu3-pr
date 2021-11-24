package cmu.edu.gtfs_realtime_processor.avl;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class AvlServlet extends HttpServlet {
    private static final String FORM = 
"<script src=\"https://code.jquery.com/jquery-2.1.3.min.js\"></script>\n"
+ "<script>\n"
+ "$(function() {\n"
+ "	$(\"#form-id\").submit(function (e) {\n"
+ "	e.preventDefault();\n"
+ "	var postData = $(this).serializeArray();\n"
+ "	var formURL = $(this).attr(\"action\");\n"
+ "	$.post(formURL, postData, function(responseText) {\n"
+ "		alert(responseText);\n"
+ "		});\n"
+ "	}); \n"
+ "});\n"
+ "</script>\n"
+ "<form id=\"form-id\" class=\"form-horizontal\" method=\"post\" action=\"predict/\">\n"
+ "  <label class=\"control-label\" for=\"selectstop\">Select Stop</label>\n"
+ "    <select id=\"selectstop\" name=\"selectstop\" class=\"input-xlarge\">\n"
+ "<option>FORBES AVE AT BEECHWOOD BLVD E19150</option>\n"
+ "<option>FORBES AVE AT BOUQUET ST FS E19210</option>\n"
+ "<option>FORBES AVE AT BRADDOCK AVE FS E19270</option>\n"
+ "<option>FORBES AVE AT CRAIG ST E19320</option>\n"
+ "<option>FORBES AVE AT DALLAS AVE E19350</option>\n"
+ "<option>FORBES AVE AT DENNISTON ST E19370</option>\n"
+ "<option>FORBES AVE AT MARGARET MORRISON ST NS E19520</option>\n"
+ "<option>FORBES AVE AT MCANULTY DR E19470</option>\n"
+ "<option>FORBES AVE AT MCKEE PL E19570</option>\n"
+ "<option>FORBES AVE AT MILTENBERGER ST E19590</option>\n"
+ "<option>FORBES AVE AT MOREWOOD AVE FS (CARNEGIE MELLON) E19610</option>\n"
+ "<option>FORBES AVE AT MOULTRIE ST E19620</option>\n"
+ "<option>FORBES AVE AT MURDOCH ST E19630</option>\n"
+ "<option>FORBES AVE AT MURRAY AVE E19660</option>\n"
+ "<option>FORBES AVE AT NORTHUMBERLAND ST E19680</option>\n"
+ "<option>FORBES AVE AT PEEBLES ST E19690</option>\n"
+ "<option>FORBES AVE AT PLAINFIELD ST E19700</option>\n"
+ "<option>FORBES AVE AT WIGHTMAN ST E19800</option>\n"
+ "<option>FORBES AVE BTW DENNISTON ST & SHADY AVE E19360</option>\n"
+ "<option>FORBES AVE OPP BEELER ST E19830</option>\n"
+ "<option>FORBES AVE OPP MARGARET MORRISON ST E19890</option>\n"
+ "<option>FORBES AVE OPP MOREWOOD AVE (CARNEGIE MELLON) E19910</option>\n"
+ "    </select>\n"
+ "    <button type=\"submit\" id=\"submit\" name=\"submit\" class=\"btn btn-primary\">Predict!</button>\n"
+ "</form>";

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
                      throws ServletException, IOException {

        // Set response content type
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.println(FORM);
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response)
                      throws ServletException, IOException {

        // Set response content type
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.println(request.getParameter("selectstop"));
    }

    public void destroy() {
      // do nothing.
    }
}
