package test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.codec.binary.Base64;


/**
 * Servlet implementation class TS7
 */
@WebServlet("/TS7")
public class TS7 extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	/**
	 * Hive base URL
	 */
	//private String baseURL = "https://65.155.233.91/api";
	// NOTE - THE SERVER IS LOOKING FOR THE WORD 'helpdesk' IN
	//        THE QUERY.  THIS WAS PUT INTO THE 
	//            C:\Windows\System32\drivers\etc\hosts
	//        FILE MANUALLY!!
	private String baseURL = "https://helpdesk/api";
	
	/*
	 * GetTicket Endpoint (for testing)
	 */
	String endPoint = "Tickets?count=100";
	
	/**
	 * Update endpoint (change status to closed (3)).
	 */
	String updateEndPoint="/UpdateTicket"; 
	
	/**
	 * Username to login with (data drive with config file)
	 */
	String username = "SP\\davidl";
	
	/**
	 * Password (data drive in config file)
	 */
	String password = "3edcVFR$3edc";
 
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TS7() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			response.getWriter().append("Served at: ").append(request.getContextPath());
			
			// Verify connectivity to outside world.
			response.getWriter().append("SDJL TEST").append(request.getContextPath());
			response.getWriter().append("SDJL TEST2: "+request.getParameter("TEST2"));
			
			// Verify connectivity to Zapier (change GET URL on Zapier Site)
			// This may need to be a post with JSON data in the long run, just
			// verifying connectivity at this point.
			String remoteAddr = request.getRemoteAddr();
			System.out.println("SDJL SourceIP: " + remoteAddr);
			StringBuffer requestURL = request.getRequestURL();
			System.out.println("SDJL TEST2: " + requestURL);
			String ticketTitle = request.getParameter("TICKET");
			StringTokenizer tok = new StringTokenizer(ticketTitle,"-");
			String pref = tok.nextToken();
			String ticketNumber=tok.nextToken().trim();
			
			System.out.println("SDJL TICKET: "+ticketTitle);
			System.out.println("SDJL NUMBER: "+ticketNumber);
			String ticketStatus = request.getParameter("STATUS");
			System.out.println("SDJL STATUS: "+ticketStatus);
			String originalID = request.getParameter("originalId");
			System.out.println("SDJL OriginalID: "+originalID);
			
		
			// HELPDESK IS CURRENTLY SETUP WITH SELF-SIGNED CERTIFICATES
			// IGNORE THOSE UNTIL IT IS SETUP CORRECTLY.
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
			        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			            return null;
			        }
			        public void checkClientTrusted(X509Certificate[] certs, String authType) {
			        }
			        public void checkServerTrusted(X509Certificate[] certs, String authType) {
			        }
			    }
			};
 
			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
			    public boolean verify(String hostname, SSLSession session) {
			        return true;
			    }
			};
 
			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			String name = "SP\\davidl";
			String password = "3edcVFR$3edc";

			// Basic HTTP authentication required by Help Desk server.
			String authString = name + ":" + password;
			System.out.println("auth string: " + authString);
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			System.out.println("Base64 encoded auth string: " + authStringEnc);
			
			// Send update request
			// CONTACT HELPDESK
			// CLOSE THE TICKET
			if (ticketStatus.equals("Completed")) {
				String parameters = "?id="+ticketNumber+"&statusId=3";
				String fullUrlString = baseURL+updateEndPoint+parameters;
				
				System.out.println("Full URL = '"+fullUrlString+"'");
				URL fullURL = new URL(fullUrlString);
				HttpURLConnection conn = (HttpURLConnection)fullURL.openConnection();			
				conn.setRequestMethod("POST");			
				conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
				conn.setDoOutput(true);
				conn.setDoInput(true);
				OutputStream os = conn.getOutputStream();
				os.write(parameters.getBytes("UTF-8"));
				os.close();
				
				InputStream is = null;
				int responseCode = conn.getResponseCode();
				System.out.println("Response Code: '"+responseCode+"'");
				if (responseCode != 200) {
					is = conn.getErrorStream();
				}
				else {
					is = conn.getInputStream();
				}
				System.out.println("*** BEGIN ***");
				Scanner scan = new Scanner(is);
				while (scan.hasNextLine()) {
					System.out.println(scan.nextLine());
				}
				System.out.println("*** END ***");
				
			}
			
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
