package myservlet

import javax.servlet.http._

class Servlet extends HttpServlet {
  /** Servlet's main method. */
  protected def welcome (request: HttpServletRequest, response: HttpServletResponse): Unit = {
    response.getWriter.write ("hi, My name is Scala")
  }
  override def doGet (request: HttpServletRequest, response: HttpServletResponse): Unit = welcome (request, response)
  override def doPost (request: HttpServletRequest, response: HttpServletResponse): Unit = welcome (request, response)
}
