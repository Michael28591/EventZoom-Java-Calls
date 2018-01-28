/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eventzoom;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * REST Web Service
 *
 * @author admin
 */
@Path("events")
public class events {

    JSONArray ja = new JSONArray();
    JSONObject jb = new JSONObject();
    JSONObject main = new JSONObject();
    static final String JDBC_DRIVER = "oracle.jdbc.OracleDriver";
    static final String DB_URL = "jdbc:oracle:thin:@144.217.163.57:1521:XE";

    static final String USER = "mad303p2";
    static final String PASS = "mad303p2pw";

    Connection conn = null;
    Statement stmt = null;
    Statement Hstmt = null;

    PreparedStatement pst = null;
    int UserTypeId;
    String sql = "";
    boolean empty = true;

    ResultSet rs1 = null;
    ResultSet rs;
    int maxid;
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of GenericResource
     */
    public int myTimestamp() {
        int time_cust = (int) (new Date().getTime() / 1000);
        return time_cust;
    }

    public Connection getCon() {
        Connection conn = null;
        try {
            //Register JDBC Driver
            Class.forName(JDBC_DRIVER);

            //Open Connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    private void closeConn() throws SQLException {
        rs.close();
        stmt.close();
        conn.close();
    }

    private void createStatement() throws SQLException {
        stmt = conn.createStatement();
        rs = stmt.executeQuery(sql);
    }

    private JSONObject ConnFailed() {
        jb.accumulate("Status", "ERROR");
        jb.accumulate("Timestamp", myTimestamp());
        jb.accumulate("MESSAGE", "database connection failed");

        return jb;
    }

    /**
     * Creates a new instance of events
     */
    public events() {
    }

    /**
     * Retrieves representation of an instance of eventzoom.events
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public String getXml() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    /**
     * PUT method for updating or creating an instance of events
     *
     * @param content representation for the resource
     */
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    public void putXml(String content) {
    }

// SIGN UP
    @GET
    @Path("signup&{fname}&{lname}&{email}&{password}&{phone}&{address}&{nationality}&{admin}&{type}")
    @Produces("application/json")
    public String signup(@PathParam("fname") String fname,
            @PathParam("lname") String lname,
            @PathParam("email") String email,
            @PathParam("password") String password,
            @PathParam("phone") String phone,
            @PathParam("address") String address,
            @PathParam("nationality") String nationality,
            @PathParam("admin") String admin,
            @PathParam("type") String userType) {
        conn = getCon();
        int count = 0;
        try {
            sql = "select count(*) from eusers where email='" + email + "'";
            createStatement();
            while (rs.next()) {
                count = rs.getInt(1);
            }
            jb.accumulate("Status", "warning");
            jb.accumulate("Timestamp", myTimestamp());
            jb.accumulate("Email", email);
            jb.accumulate("Message", "You are already registered");

            if (count == 0) {
                jb.clear();
                sql = "select TYPEID from euserstype where TYPENAME = '" + userType + "'";
                createStatement();
                while (rs.next()) {
                    UserTypeId = rs.getInt(1);
                }
                sql = "INSERT into eusers values (eusers_sequence.nextval,?,?,?,?,?,?,?,?,?)";
                pst = conn.prepareStatement(sql);
                pst.setString(1, fname);
                pst.setString(2, lname);
                pst.setString(3, email);
                pst.setString(4, password);
                pst.setString(5, phone);
                pst.setString(6, address);
                pst.setString(7, nationality);
                pst.setString(8, admin);
                pst.setInt(9, UserTypeId);
                if (pst.executeUpdate() == 1) {
                    sql = "select max(userid) from eusers";
                    createStatement();
                    while (rs.next()) {
                        maxid = rs.getInt(1);
                    }
                    jb.accumulate("Status", "OK");
                    jb.accumulate("Timestamp", myTimestamp());
                    jb.accumulate("Message", "successfull Added");
                    jb.accumulate("Userid", maxid);
                }
                pst.close();
            }

            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = jb.toString();
        return result;
    }

// ADD EVENT    
    @GET
    @Path("addevent&{ename}&{location}&{description}&{maxusers}&{buyticket}&{eventpic}&{starttime}&{endtime}&{type}&{userid}")
    @Produces("application/json")
    public String addevent(@PathParam("ename") String ename,
            @PathParam("location") String location,
            @PathParam("description") String description,
            @PathParam("maxusers") String maxusers,
            @PathParam("buyticket") String buyticket,
            @PathParam("eventpic") String eventpic,
            @PathParam("starttime") int starttime,
            @PathParam("endtime") int endtime,
            @PathParam("type") String eventType,
            @PathParam("userid") int userid) {
        conn = getCon();
        int eventId = 0;
        try {

            sql = "select etid from eventtype where NAME = '" + eventType + "'";
            createStatement();
            while (rs.next()) {
                eventId = rs.getInt(1);
            }
            sql = "INSERT into events values (events_sequence.nextval,?,?,?,?,?,?,?,?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setString(1, ename);
            pst.setString(2, location);
            pst.setString(3, description);
            pst.setString(4, maxusers);
            pst.setString(5, buyticket);
            pst.setString(6, eventpic);
            pst.setInt(7, starttime);
            pst.setInt(8, endtime);
            pst.setInt(9, eventId);
            pst.setInt(10, userid);
            if (pst.executeUpdate() == 1) {
                sql = "select max(eid) from events";
                createStatement();
                while (rs.next()) {
                    maxid = rs.getInt(1);
                }
                jb.accumulate("Status", "OK");
                jb.accumulate("Timestamp", myTimestamp());
                jb.accumulate("Message", "successfull Added");
                jb.accumulate("Event Id", maxid);
            }
            pst.close();

            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = jb.toString();
        return result;
    }

// ADD USER HOBBIES
    @GET
    @Path("adduserhobbies&{HobbyList}&{userid}")
    @Produces("application/json")
    public String adduserhobbies(@PathParam("HobbyList") String hobbyList, @PathParam("userid") int userid) {
        conn = getCon();
        try {
            String[] SingleHobby = hobbyList.split("|");
            boolean empty = true;
            boolean notinserted = true;
            for (int i = 0; i < SingleHobby.length; i++) {

                sql = "select hid from hobbies where name='" + SingleHobby[i] + "'";
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    maxid = rs.getInt(1);
                    empty = false;
                }
                if (empty) {
                    sql = "select max(hid) from hobbies";
                    stmt = conn.createStatement();
                    rs = stmt.executeQuery(sql);
                    while (rs.next()) {
                        maxid = rs.getInt(1);
                    }
                    maxid++;
                    String sql23 = "insert into hobbies values (" + maxid + ",'" + SingleHobby[i] + "')";
                    Hstmt = conn.createStatement();
                    Hstmt.executeUpdate(sql23);
                }
                String sqlau = "insert into userhobbies values (" + maxid + "," + userid + ")";
                stmt = conn.createStatement();
                if (stmt.executeUpdate(sqlau) != 1) {
                    notinserted = false;
                }
            }
            if (notinserted) {
                jb.clear();
                jb.accumulate("Status", "OK");
                jb.accumulate("Timestamp", myTimestamp());
                jb.accumulate("Message", "Value Inserted");
            }
        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = jb.toString();
        return result;
    }

// SIGNIN
    @GET
    @Path("signIn&{email}&{password}")
    @Produces("application/json")
    public String signIn(@PathParam("email") String email, @PathParam("password") String password) {
        conn = getCon();
        try {
            sql = "select * from eusers where email=? and password=?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, email);
            pst.setString(2, password);
            rs = pst.executeQuery();
            while (rs.next()) {
                jb.accumulate("Status", "OK");
                jb.accumulate("Timestamp", myTimestamp());
                jb.accumulate("Message", "successfull");
            }
            if (jb.isEmpty()) {
                jb.accumulate("Status", "Warning");
                jb.accumulate("Timestamp", myTimestamp());
                jb.accumulate("Message", "Info Not Found");
            }
            rs.close();
            pst.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            ConnFailed();
        }
        String result = jb.toString();
        return result;
    }

// ADD FAV
    @GET
    @Path("addfav&{userid}&{eventid}")
    @Produces("application/json")
    public String addfav(@PathParam("userid") int userid, @PathParam("eventid") int eventid ) {
        conn = getCon();
        try {

            sql = "INSERT into favlist values (?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, userid);
            pst.setInt(2, eventid);
            pst.setInt(3, myTimestamp());
            if (pst.executeUpdate() == 1) {
                jb.clear();
                jb.accumulate("Status", "OK");
                jb.accumulate("Timestamp", myTimestamp());
                jb.accumulate("Message", "Fav added successfully");
            }
            
            pst.close();
            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = jb.toString();
        return result;
    }

// ADD COMMENT
    @GET
    @Path("addcomment&{userid}&{eventid}&{comment}")
    @Produces("application/json")
    public String addreview(@PathParam("userid") int userid, @PathParam("eventid") int eventid, @PathParam("comment") String comment) {
        conn = getCon();
        try {
            sql = "select max(commentid) from comments";
            createStatement();
            while (rs.next()) {
                maxid = rs.getInt(1);
            }
            sql = "INSERT into comments values (?,?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, ++maxid);
            pst.setInt(2, userid);
            pst.setInt(3, eventid);
            pst.setString(4, comment);
            if (pst.executeUpdate() == 1) {
                jb.clear();
                jb.accumulate("Status", "OK");
                jb.accumulate("Timestamp", myTimestamp());
                jb.accumulate("Message", "Comment Added Successfully");
            }

            pst.close();
            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = jb.toString();
        return result;
    }

// ADD SOCIAL SHARE
    @GET
    @Path("socialshare&{userid}&{eventid}&{platformid}")
    @Produces("application/json")
    public String socialshare(@PathParam("userid") int userid, @PathParam("eventid") int eventid, @PathParam("platformid") int platformid) {
        conn = getCon();
        try {
            sql = "select max(ssid) from socialshare";
            createStatement();
            while (rs.next()) {
                maxid = rs.getInt(1);
            }
            sql = "INSERT into socialshare values (?,?,?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, ++maxid);
            pst.setInt(2, userid);
            pst.setInt(3, eventid);
            pst.setInt(4, platformid);
            pst.setInt(5, 1517119543);
            if (pst.executeUpdate() == 1) {
                jb.clear();
                jb.accumulate("Status", "OK");
                jb.accumulate("Timestamp", myTimestamp());
                jb.accumulate("Message", "Successfull Added");
            }

            pst.close();
            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = jb.toString();
        return result;
    }

// ADD USER SHARE
    @GET
    @Path("usershare&{senderUserId}&{eventId}&{ReceiverUserId}")
    @Produces("application/json")
    public String usershare(@PathParam("senderUserId") int senderUserId, @PathParam("eventId") int eventid, @PathParam("ReceiverUserId") int ReceiverUserId) {
        conn = getCon();
        try {

            sql = "INSERT into usershare values (?,?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, senderUserId);
            pst.setInt(2, eventid);
            pst.setInt(3, myTimestamp());
            pst.setInt(4, ReceiverUserId);
            if (pst.executeUpdate() == 1) {
                jb.clear();
                jb.accumulate("Status", "OK");
                jb.accumulate("Timestamp", myTimestamp());
                jb.accumulate("Message", "Successfull Added");
            }

            pst.close();
            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = jb.toString();
        return result;
    }

// ADD USER ACTION
    @GET
    @Path("useraction&{userid}&{eventid}&{action}")
    @Produces("application/json")
    public String useraction(@PathParam("userid") int userid, @PathParam("eventid") int eventid, @PathParam("action") int actionId) {
        conn = getCon();
        try {

            sql = "INSERT into useraction values (?,?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, userid);
            pst.setInt(2, eventid);
            pst.setInt(3, actionId); 
            pst.setInt(4, myTimestamp());
            if (pst.executeUpdate() == 1) {
                jb.clear();
                jb.accumulate("Status", "OK");
                jb.accumulate("Timestamp", myTimestamp());
                jb.accumulate("Message", "Successfull Added");
            }

            pst.close();
            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = jb.toString();
        return result;
    }

// EVENT LIST
    @GET
    @Path("eventlist")
    @Produces("application/json")
    public String eventlist() {
        conn = getCon();
        try {
            sql = "select * from events where ETID = (select ETID from eventtype where name='PUBLIC')";
            createStatement();

            while (rs.next()) {
                int eid = rs.getInt("eid");
                String ename = rs.getString("ename");
                int startTime = rs.getInt("starttime");
                jb.accumulate("EventId", eid);
                jb.accumulate("Name", ename);
                jb.accumulate("Start Date", startTime);
                ja.add(jb);
                jb.clear();
            }

            main.accumulate("Status", "OK");
            main.accumulate("Timestamp", myTimestamp());
            main.accumulate("Events", ja);
            if (ja.isEmpty()) {
                main.clear();
                main.accumulate("Status", "WARNING");
                main.accumulate("Timestamp", myTimestamp());
                main.accumulate("Message", "No data found");
            }

            closeConn();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = main.toString();
        return result;
    }

    // HOBBIES LIST 
    @GET
    @Path("hobbylist")
    @Produces("application/json")
    public String hobbieslist() {
        conn = getCon();
        try {
            sql = "select * from hobbies ";
            createStatement();

            while (rs.next()) {
                int hid = rs.getInt("hid");
                String hobbyname = rs.getString("name");
                jb.accumulate("HobbyId", hid);
                jb.accumulate("Name", hobbyname);

                ja.add(jb);
                jb.clear();
            }

            main.accumulate("Status", "OK");
            main.accumulate("Timestamp", myTimestamp());
            main.accumulate("Hobbies", ja);
            if (ja.isEmpty()) {
                main.clear();
                main.accumulate("Status", "WARNING");
                main.accumulate("Timestamp", myTimestamp());
                main.accumulate("Message", "No data found");
            }

            closeConn();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = main.toString();
        return result;
    }

// search by category 
    @GET
    @Path("searchbycategory&{catid}")
    @Produces("application/json")
    public String searchbycategory(@PathParam("catid") int cid) {
        conn = getCon();
        try {
             sql="SELECT e.* FROM ECATEGORY ec left join EVENTS e on ec.EID=e.EID WHERE e.ETID = (select ETID from eventtype where name='PUBLIC') and ec.catid=" +cid;
            createStatement();

            while (rs.next()) {
                int eid = rs.getInt("eid");
                String ename = rs.getString("ename");
                String loc = rs.getString("location");
                String desc = rs.getString("description");
                String pic = rs.getString("eventpic");
                String sTime = rs.getString("starttime");
                String eTime = rs.getString("endtime");
                int maxusers =  rs.getInt("maxusers");
                String buyticketurl = rs.getString("buyticket");
                jb.accumulate("EventId", eid);
                jb.accumulate("EventName", ename);
                jb.accumulate("Location", loc);
                jb.accumulate("EventPic", pic);
                jb.accumulate("StartTime", sTime);
                ja.add(jb);
                jb.clear();
            }

            main.accumulate("Status", "OK");
            main.accumulate("Timestamp", myTimestamp());
            main.accumulate("Events By Category", ja);
            if (ja.isEmpty()) {
                main.clear();
                main.accumulate("Status", "WARNING");
                main.accumulate("Timestamp", myTimestamp());
                main.accumulate("Message", "No data found");
            }

            closeConn();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = main.toString();
        return result;
    }

    // Categories LIST
    @GET
    @Path("categorylist")
    @Produces("application/json")
    public String categorylist() {
        conn = getCon();
        try {
            sql = "select * from ECATEGORYLIST ";
            createStatement();

            while (rs.next()) {
                int catId = rs.getInt("catid");
                String cName = rs.getString("catname");
                jb.accumulate("CategoryID", catId);
                jb.accumulate("Name", cName);

                ja.add(jb);
                jb.clear();
            }

            main.accumulate("Status", "OK");
            main.accumulate("Timestamp", myTimestamp());
            main.accumulate("Categories", ja);
            if (ja.isEmpty()) {
                main.clear();
                main.accumulate("Status", "WARNING");
                main.accumulate("Timestamp", myTimestamp());
                main.accumulate("Message", "No data found");
            }

            closeConn();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = main.toString();
        return result;
    }
    
    
// list of events depending on user action

    @GET
    @Path("useractiononevents&{actionN}&{userid}")
    @Produces("application/json")
    public String useractiononevents(@PathParam("actionN") int actionId,@PathParam("userid") int userid) {
        conn = getCon();
        try {
            
            sql = "SELECT e.ENAME,ual.ACTIONNAME,e.eid FROM useraction ua left join USERACTIONLIST ual on ual.ACTIONID=ua.ACTIONID left join EVENTS e on ua.EID=e.EID where ual.ACTIONID='"+actionId+"' and ua.USERID="+userid;
            createStatement();
            while (rs.next()) {
                int eventid=rs.getInt("eid");
                String evantName = rs.getString("ename");
                String aName = rs.getString("actionname");
                jb.accumulate("Action Name", aName);
                jb.accumulate("EventName", evantName);
                jb.accumulate("Event Id", eventid);
                ja.add(jb);
                jb.clear();
            }

            main.accumulate("Status", "OK");
            main.accumulate("Timestamp", myTimestamp());
            main.accumulate("Event List", ja);
            if (ja.isEmpty()) {
                main.clear();
                main.accumulate("Status", "WARNING");
                main.accumulate("Timestamp", myTimestamp());
                main.accumulate("Message", "No data found");
            }

            closeConn();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = main.toString();
        return result;
    }
    
    
    
// list of comments for single event 
    
      @GET
    @Path("allcomments&{eventid}")
    @Produces("application/json")
    public String allcomments(@PathParam("eventid") int eventid) {
         conn = getCon();
        try {
            sql = "select c.*,eu.fname,eu.lname from comments c left join eusers eu on c.userid=eu.userid where eid ="+eventid;
            createStatement();

            while (rs.next()) {
               
                int commentid = rs.getInt("commentid");
                String fname = rs.getString("fname");
                String lname = rs.getString("lname");
                String comments = rs.getString("comments");

                jb.accumulate("comment ID", commentid);
                jb.accumulate("comment", comments);
                jb.accumulate("First Name", fname);
                jb.accumulate("Last Name", lname);

                ja.add(jb);
                jb.clear();
            }
               
            main.accumulate("Status", "OK");
            main.accumulate("Timestamp", myTimestamp());
            main.accumulate("Comments", ja);
            if (ja.isEmpty()) {
                main.clear();
                main.accumulate("Status", "WARNING");
                main.accumulate("Timestamp", myTimestamp());
                main.accumulate("Message", "No data found");
            }

              closeConn();
        
        }
        
        catch(SQLException se) {
            se.printStackTrace();
            ConnFailed();   
        }
        String result = main.toString();
        return result; 
    }
    
    
// USER PROFILE
    
    @GET
    @Path("userprofile&{userid}")
    @Produces("application/json")
    public String userprofile(@PathParam("userid") int userid) {
        
        conn = getCon();

        try {
            
            sql = "select e.*,eut.typename from eusers e,euserstype eut WHERE e.typeid=eut.typeid and userid ="+userid;
            createStatement();
            boolean empty = true;
            while (rs.next()) {
                userid = rs.getInt("userid");
                String fname = rs.getString("fname");
                String lname = rs.getString("lname");
                String email = rs.getString("email");
                String password = rs.getString("password");
                String phone = rs.getString("phone");
                String address = rs.getString("address");
                String nationality = rs.getString("nationality");
                String typename = rs.getString("typename");
                jb.accumulate("Status", "OK");
                jb.accumulate("Timestamp", myTimestamp());
                jb.accumulate("First Name", fname);
                jb.accumulate("Last Name", lname);
                jb.accumulate("Email", email);
                jb.accumulate("Password", password);
                jb.accumulate("Phone", phone);
                jb.accumulate("Address", address);
                jb.accumulate("Nationality", nationality);
                jb.accumulate("User Type", typename);
               
                empty = false;
            }
            
            if (empty) {
                jb.accumulate("Status", "WARNING");
                jb.accumulate("Timestamp", myTimestamp());
                jb.accumulate("MESSAGE", "No information available");
            }

            closeConn();

        } catch (SQLException se) {
            //se.printStackTrace();
            ConnFailed();
        } 
        String result = jb.toString();

        return result;
    }
    
// SINGLE EVENT
   
    @GET
    @Path("singleevent&{eventid}")
    @Produces("application/json")
    public String singleevent(@PathParam("eventid") int eventid) {
        
        conn = getCon();

        try {
            
            sql = "select e.*,et.name as type from events e,eventtype et WHERE e.etid=et.etid and eid="+eventid;
            createStatement();
            boolean empty = true;
            while (rs.next()) {
                String ename = rs.getString("ename");
                String location = rs.getString("location");
                String desc = rs.getString("description");
                String maxusers = rs.getString("maxusers");
                String buyticket = rs.getString("buyticket");
                String eventpic = rs.getString("eventpic");
                String starttime = rs.getString("starttime");
                String endtime = rs.getString("endtime");
                String type = rs.getString("type");
                main.accumulate("Status", "OK");
                main.accumulate("Timestamp", myTimestamp());
                main.accumulate("Event Name", ename);
                main.accumulate("Location", location);
                main.accumulate("Description", desc);
                main.accumulate("Maxusers", maxusers);
                main.accumulate("Buyticket", buyticket);
                main.accumulate("Eventpic", eventpic);
                main.accumulate("Start Time", starttime);
                main.accumulate("End Time", endtime);
                main.accumulate("Event Type", type);
               
                empty = false;
            }
            if (!empty) {
                sql = "select ecl.CATID,ecl.CATNAME from ECATEGORY ec left join ECATEGORYLIST ecl on ec.CATID=ecl.CATID where ec.EID="+eventid;
                createStatement();
                while(rs.next()) {
                    String CatName = rs.getString("CATNAME");
                    int catId = rs.getInt("catid");
                    jb.accumulate("Category ID", catId);
                    jb.accumulate("Category Name", CatName);
                    ja.add(jb);
                    jb.clear();
                }
                main.accumulate("Categories", ja);
            }
            if (empty) {
                main.accumulate("Status", "WARNING");
                main.accumulate("Timestamp", myTimestamp());
                main.accumulate("MESSAGE", "No information available");
            }

            closeConn();

        } catch (SQLException se) {
            //se.printStackTrace();
            ConnFailed();
        } 
        String result = main.toString();

        return result;
    }
     

    
  //list of shared events
    
    
    @GET
    @Path("platformlist")
    @Produces("application/json")
    public String platformlist() {
        conn = getCon();
        try {
            sql = "select * from platform ";
            createStatement();

            while (rs.next()) {
                int pid = rs.getInt("pid");
                String pname = rs.getString("pname");
                jb.accumulate("Platform id", pid);
                jb.accumulate("Platform Name", pname);

                ja.add(jb);
                jb.clear();
            }

            main.accumulate("Status", "OK");
            main.accumulate("Timestamp", myTimestamp());
            main.accumulate("Platforms", ja);
            if (ja.isEmpty()) {
                main.clear();
                main.accumulate("Status", "WARNING");
                main.accumulate("Timestamp", myTimestamp());
                main.accumulate("Message", "No data found");
            }

            closeConn();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = main.toString();
        return result;
    }
    
    
    
    //list of user actions
    
    
    @GET
    @Path("useractionlist")
    @Produces("application/json")
    public String useractionlist() {
        conn = getCon();
        try {
            sql = "select * from useractionlist ";
            createStatement();

            while (rs.next()) {
                int pid = rs.getInt("actionid");
                String pname = rs.getString("actionname");
                jb.accumulate("Action id", pid);
                jb.accumulate("Action Name", pname);

                ja.add(jb);
                jb.clear();
            }

            main.accumulate("Status", "OK");
            main.accumulate("Timestamp", myTimestamp());
            main.accumulate("Action List", ja);
            if (ja.isEmpty()) {
                main.clear();
                main.accumulate("Status", "WARNING");
                main.accumulate("Timestamp", myTimestamp());
                main.accumulate("Message", "No data found");
            }

            closeConn();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = main.toString();
        return result;
    }
    
    
    
    
    // FAV LIST
     @GET
    @Path("favlist&{userid}")
    @Produces("application/json")
   public String favlist(@PathParam("userid")int userid) {
        conn = getCon();
        try {
            sql = "select * from events where eid in (select eid from favlist where userid="+userid+")";
            createStatement();

            while (rs.next()) {
                int eid = rs.getInt("eid");
                String ename = rs.getString("ename");
                int startTime = rs.getInt("starttime");
                jb.accumulate("EventId", eid);
                jb.accumulate("Name", ename);
                jb.accumulate("Start Date", startTime);
                ja.add(jb);
                jb.clear();
            }

            main.accumulate("Status", "OK");
            main.accumulate("Timestamp", myTimestamp());
            main.accumulate("Favourate Events", ja);
            if (ja.isEmpty()) {
                main.clear();
                main.accumulate("Status", "WARNING");
                main.accumulate("Timestamp", myTimestamp());
                main.accumulate("Message", "No data found");
            }

            closeConn();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = main.toString();
        return result;
    }

    
    // ADD GROUP
    @GET
    @Path("addgroup&{groupname}&{description}&{userid}")
    @Produces("application/json")
    public String addgroup(@PathParam("groupname") String groupName ,@PathParam("description") String description,@PathParam("userid") int userid) {
        conn = getCon();
        try {
            sql = "select max(groupid) from groups";
            createStatement();
            while (rs.next()) {
                maxid = rs.getInt(1);
            }
            sql = "INSERT into groups values (?,?,?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, ++maxid);
            pst.setString(2, groupName);
            pst.setString(3, description);
            pst.setInt(4, myTimestamp());
            pst.setInt(5, userid);
            if (pst.executeUpdate() == 1) {
                jb.clear();
                jb.accumulate("Status", "OK");
                jb.accumulate("Timestamp", myTimestamp());
                jb.accumulate("Group id", maxid);
                jb.accumulate("Message", "Group Added Successfully");
            }

            pst.close();
            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = jb.toString();
        return result;
    }

    // Add user to group
     @GET
    @Path("addusertogroup&{groupid}&{userid}")
    @Produces("application/json")
    public String addusertogroup(@PathParam("groupid") int groupid ,@PathParam("userid") int userid) {
        conn = getCon();
        try {
            
            sql = "INSERT into groupeusers values (?,?)";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, groupid);
            pst.setInt(2, userid);
            if (pst.executeUpdate() == 1) {
                jb.clear();
                jb.accumulate("Status", "OK");
                jb.accumulate("Timestamp", myTimestamp());
                jb.accumulate("Message", "User added to Group");
            }

            pst.close();
            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = jb.toString();
        return result;
    }

    // users in single group 
    @GET
    @Path("usersingroup&{groupid}")
    @Produces("application/json")
   public String usersingroup(@PathParam("groupid")int groupid) {
        conn = getCon();
        try {
            sql = "select u.* from groupeusers gu left join eusers u on gu.userid = u.userid where gu.groupid="+groupid;
            createStatement();

            while (rs.next()) {
                int userid = rs.getInt("userid");
                String Fname = rs.getString("fname");
                String LastName = rs.getString("Lname");
                String Email = rs.getString("email");
                jb.accumulate("userid", userid);
                jb.accumulate("Name", Fname+" "+ LastName);
                jb.accumulate("Email", Email);
                ja.add(jb);
                jb.clear();
            }

            main.accumulate("Status", "OK");
            main.accumulate("Timestamp", myTimestamp());
            main.accumulate("Users", ja);
            if (ja.isEmpty()) {
                main.clear();
                main.accumulate("Status", "WARNING");
                main.accumulate("Timestamp", myTimestamp());
                main.accumulate("Message", "No data found");
            }

            closeConn();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = main.toString();
        return result;
    }

    // Users list
   @GET
    @Path("userslist")
    @Produces("application/json")
   public String userslist() {
        conn = getCon();
        try {
            sql = "select * from eusers";
            createStatement();

            while (rs.next()) {
                int userid = rs.getInt("userid");
                String Fname = rs.getString("fname");
                String LastName = rs.getString("Lname");
                String Email = rs.getString("email");
                jb.accumulate("userid", userid);
                jb.accumulate("Name", Fname+" "+ LastName);
                jb.accumulate("Email", Email);
                ja.add(jb);
                jb.clear();
            }

            main.accumulate("Status", "OK");
            main.accumulate("Timestamp", myTimestamp());
            main.accumulate("Users", ja);
            if (ja.isEmpty()) {
                main.clear();
                main.accumulate("Status", "WARNING");
                main.accumulate("Timestamp", myTimestamp());
                main.accumulate("Message", "No data found");
            }

            closeConn();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = main.toString();
        return result;
    }

    //count share for each platform
    @GET
    @Path("sharecountforeachplatfrom&{eventid}")
    @Produces("application/json")
   public String sharecountforeachplatfrom(@PathParam("eventid") int eventid) {
        conn = getCon();
        try {
            sql = "select pid,count(*) as count FROM SOCIALSHARE WHERE eid="+eventid+" GROUP BY PID";
            createStatement();
            String platformName="";
            while (rs.next()) {
                int pid = rs.getInt("pid");
                String count = rs.getString("count");
                 String sql1="SELECT pname from platform where pid ="+pid;
                    Hstmt = conn.createStatement();
                    ResultSet rs1 = Hstmt.executeQuery(sql1);
                    while (rs1.next()) {
                        platformName = rs1.getString("pname");
                    }
                jb.accumulate("Platform Id", pid);
                jb.accumulate("Count", count);
                jb.accumulate("Platform Name", platformName);
                ja.add(jb);
                jb.clear();
            }

            main.accumulate("Status", "OK");
            main.accumulate("Timestamp", myTimestamp());
            main.accumulate("Total Shares", ja);
            if (ja.isEmpty()) {
                main.clear();
                main.accumulate("Status", "WARNING");
                main.accumulate("Timestamp", myTimestamp());
                main.accumulate("Message", "No data found");
            }

            closeConn();

        } catch (SQLException se) {
            se.printStackTrace();
            ConnFailed();
        }
        String result = main.toString();
        return result;
    }
}
