/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sparqlbuilder.www;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author atsuko
 */
@WebServlet(name = "CLServlet", urlPatterns = {"/clist"})
public class CLServlet extends HttpServlet {

    private static final String FILENAME = "ddata/";
    private static final String WORKDIR = "cc/";
    private Set<String> yumep = null;
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet CLServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet CLServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //processRequest(request, response);
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET");
        response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
        response.setHeader("Access-Control-Max-Age", "-1");     
	PrintWriter out = response.getWriter();
        String ep = request.getParameter("ep");
        String yum = request.getParameter("yum");
        String ex = request.getParameter("ex");
        String keyword = request.getParameter("keyword");
        
        String classURI = request.getParameter("class");
        
        if ( ex != null){
            if (ex.equals("true")){
                JsonArray ja = getJsonArrayExClasses();
                out.print(ja);
                return;
            }
        }
     
        Set<String> yumep = null;
        if ( yum !=null ){
            yumep = YummyEP.getYummyEP(yum);
        }
        
        Set<String> classes = new HashSet<>();  
        Map<String, String> cinfo = new HashMap<>();
        try{
            BufferedReader br = new BufferedReader(new FileReader("cc/cl.txt"));
            String w_buf;
            while ( (w_buf = br.readLine()) != null ){
                String data[] = w_buf.split("\t"); // 0: url, 1: label, 2: ep, 3: #ins
                if ( yum != null ){
                    if ( ! yumep.contains(data[2])){
                        continue;
                    }
                }
                String label = null;
                if (data.length == 1 ){
                    System.out.println("here");
                }
                if (data[1].length() == 0){
                    String data2[] = data[0].split("/");
                    String data3[] = data2[data2.length - 1].split("#");
                    label = data3[data3.length -1];
                }else{
                    label = data[1];
                }
                if (data.length < 4){continue;}
                String cl =  data[0]+data[2]; // url +ep
                String orginfo = cinfo.get(cl);
                String info = "";
                if (orginfo == null){
                    info = label+"\t"+data[0]+"\t"+data[3]+"\t"+data[2]; //label, url, ins, ep                
                    cinfo.put(cl,info);
                }else{
                    String orgd[] = orginfo.split("\t");
                    int ins = Integer.parseInt(orgd[2])+Integer.parseInt(data[3]);
                    info = label+"\t"+data[0]+"\t"+Integer.toString(ins)+"\t"+data[2];
                    cinfo.put(cl,info);
                }
            }
            br.close();
        }catch(IOException e){
            e.printStackTrace();
        }   
        
        if ( classURI != null ){
            if ( ep == null ){
                System.out.println("EP is not determined");
                return;
            }
            try{
                classes = new HashSet<>();
                String epc = ep.split("//")[1].replace("/", "_").replace("#", "-");;
                BufferedReader br = new BufferedReader(new FileReader("cc/".concat(epc).concat(".cr")));
                String w_buf;
                while ( (w_buf = br.readLine()) != null ){
                    String data[] = w_buf.split("\t");
                    if ( classURI.equals(data[0])){               
                        String cls[] = data[1].split(",");
                        for (int i = 0; i < cls.length; i++ ){
                            String info = cinfo.get(cls[i]+ep);
                            classes.add(info);            
                        }
                        break;
                    }
                }
            }catch(IOException e){
                e.printStackTrace();
            }
            //qpg.setOWLClassGraph(classURI); 
            //qpg.setPartClassGraph(classURI);
            //classes = qpg.getClasses(null);
            //classes = qpg.getReachableClasses(classURI);            
        }else{
            classes = new HashSet<>(cinfo.values());
            //classes = qpg.getClasses(null);
            // KOKO TODO
        }
        //sortedClasses = qpg.getSortedClasses(classes);
        
        JsonBuilderFactory jbfactory = Json.createBuilderFactory(null);
        JsonArray ja = getJsonArrayFromClasses(jbfactory, classes, keyword);
        out.print(ja);
        //JsonArray ja = getJsonArrayFromSortedClasses(jbfactory, sortedClasses, ep);
        //out.print(ja);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private JsonArray getJsonArrayExClasses(){
        JsonBuilderFactory jbfactory = Json.createBuilderFactory(null);
        JsonArrayBuilder jab = jbfactory.createArrayBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File("cc/exc.txt")));
            String buf;
            while ((buf = br.readLine()) != null ){
                JsonObjectBuilder job = jbfactory.createObjectBuilder();
                String data[] = buf.split("\t");
                if (data.length < 4 ){ continue; }
                job.add("ep", data[2]);
                job.add("uri", data[0]);
                job.add("label", data[1]);
                job.add("number", data[3]);
                jab.add(job);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        JsonArray ja = jab.build();
        return ja;        
    }    
    
    /*
    private JsonArray getJsonArrayFromSortedClasses(JsonBuilderFactory jbfactory, 
             SortedSet<String> sortedClasses, String ep){
        JsonArrayBuilder jab = jbfactory.createArrayBuilder();
        Iterator<String> cit = sortedClasses.iterator();
        List<String> tmpclasses = new LinkedList<String>();
        JsonObjectBuilder job = jbfactory.createObjectBuilder();
        while( cit.hasNext() ){
            String classinfo = cit.next();
            String[] data = classinfo.split("\t"); 
            if (data.length != 3 ){
                System.out.println("data is wrong?");
            }
            if (data[0].matches("^[0-9]*$")){
                tmpclasses.add(classinfo);
            }else{
                job.add("ep", ep);
                job.add("uri", data[2]);
                job.add("label", data[0]);
                job.add("number", data[1]);
                jab.add(job);
            }
        }
        cit = tmpclasses.iterator();
        while( cit.hasNext() ){
            String classinfo = cit.next();
            String[] data = classinfo.split("\t"); 
            if (data.length != 3 ){
                System.out.println("data is wrong?");
            }
            job.add("ep", ep);
            job.add("uri", data[2]);
            job.add("label", data[0]);
            job.add("number", data[1]);
            jab.add(job);
        }       
        JsonArray ja = jab.build();
        return ja;
    }
    */

    private JsonArray getJsonArrayFromClasses(JsonBuilderFactory jbfactory, Set<String> cl, 
            String keyword){
        String rg = "";
        if ( keyword != null ){
            rg = ".*".concat(keyword).concat(".*");
        }
        Pattern p = Pattern.compile(rg, Pattern.CASE_INSENSITIVE);
        JsonArrayBuilder jab = jbfactory.createArrayBuilder();
        JsonObjectBuilder job = jbfactory.createObjectBuilder();
        Iterator<String> cit = cl.iterator();
        
        List<String> ncl = new LinkedList<String>();
        
        while( cit.hasNext() ){
            String classinfo = cit.next();
            String[] data = classinfo.split("\t"); 
            if (data.length != 4 ){
                System.out.println("data is wrong?"); // KOKO
            }else if( data[0].matches("^[0-9].*") ){
                ncl.add(classinfo); // number should be last
            }else{
                if ( keyword != null ){
                    if (( ! p.matcher(data[0] ).find()) && ( ! p.matcher(data[1] ).find() )){
                        continue;
                    }
                }
                job.add("ep", data[3]);
                job.add("uri", data[1]);
                job.add("label", data[0]);
                job.add("number", data[2]);
                jab.add(job);
            }
        }
        /*
        ListIterator<String> nit = ncl.listIterator();
        while(nit.hasNext()){
            String classinfo = nit.next();
            String[] data = classinfo.split("\t"); 
            job.add("ep", data[3]);
            job.add("uri", data[1]);
            job.add("label", data[0]);
            job.add("number", data[2]);
            jab.add(job);           
        }
        */
        JsonArray ja = jab.build();
        return ja;
    }
}
