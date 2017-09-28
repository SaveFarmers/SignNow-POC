package com.esign.signnow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Servlet implementation class SignNowIntegrationServlet
 */
@WebServlet("/eSign")
@MultipartConfig(location="/tmp", fileSizeThreshold=1024*1024, 
maxFileSize=1024*1024*5, maxRequestSize=1024*1024*5*5)
public class SignNowIntegrationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String eSignBaseUrl = "https://api-eval.signnow.com";

	private static final String clientID = "0fccdbc73581ca0f9bf8c379e6a96813";
	private static final String clientSecret = "3719a124bcfc03c534d4f5c05b5a196b";
	private static final String encodedClientCredentials = "MGZjY2RiYzczNTgxY2EwZjliZjhjMzc5ZTZhOTY4MTM6MzcxOWExMjRiY2ZjMDNjNTM0ZDRmNWMwNWI1YTE5NmI=";
	private static final String charset = "UTF-8";
	private static final int TIMEOUT = 2000;
	
	private static final String HTTP_METHOD_POST = "POST";
	private static final String HTTP_METHOD_PUT = "PUT";
	private static final String HTTP_METHOD_GET = "GET";
	private static final String HTTP_METHOD_DELETE = "DELETE";
	
	private static final String AUTHENTICATION_METHOD_BASIC = "BASIC";
	private static final String AUTHENTICATION_METHOD_OAUTH = "OAUTH";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SignNowIntegrationServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			JSONParser parser = new JSONParser();
			JSONObject obj = null; 
			JSONObject resObj = null;
			String resValue = null;
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			String documentId = null;
			String url = null;

			String requestedAction = request.getParameter("request");

			System.out.println("Requested Action : " + requestedAction);

			if (requestedAction != null) {
				switch (requestedAction) {
				case "GET_OAUTH_TOKEN":
					obj = (JSONObject) parser.parse(request.getReader());
					JSONObject dataObj = (JSONObject) obj.get("data");
					String username = (String) dataObj.get("email");
					String password = (String) dataObj.get("password");
					resValue = getOAuthToken(username, password);
					resObj = (JSONObject) parser.parse(resValue);
					out.print(resObj);

					break;

				case "FILE_UPLOAD":
					processFileUpload(request, response);
				    break;
				    
				case "GET_DOCUMENT":
					documentId = request.getParameter("documentId");
					resValue = processGetRequest(eSignBaseUrl + "/document/" + documentId, request);
					resObj = (JSONObject) parser.parse(resValue);
					out.print(resObj);
					break;
					
				case "GET_ALL_DOCUMENTS":
					resValue = processGetRequest(eSignBaseUrl + "/user/documentsv2", request);
					JSONArray jsonArray = (JSONArray) parser.parse(resValue);
					out.print(jsonArray);
					break;
					
				case "DELETE_DOCUMENT":
					documentId = request.getParameter("documentId");
					url = eSignBaseUrl + "/document/" + documentId;
					resValue = processPostPutRequest(HTTP_METHOD_DELETE, url, null, AUTHENTICATION_METHOD_OAUTH, request);
					resObj = (JSONObject) parser.parse(resValue);
					out.print(resObj);
					break;
					
				case "SEND_INVITE":
					obj = (JSONObject) parser.parse(request.getReader());
					documentId = (String) obj.get("documentId");
					JSONObject involvedParties = (JSONObject) obj.get("involvedParties");
					
					url = eSignBaseUrl + "/document/" + documentId + "/invite";

					resValue = processPostPutRequest(HTTP_METHOD_POST, url, involvedParties.toString(), AUTHENTICATION_METHOD_OAUTH, request);
					resObj = (JSONObject) parser.parse(resValue);
					out.print(resObj);
					break;
					
					
				case "THUMBNAILS":
//					obj = (JSONObject) parser.parse(request.getReader());
//					String requestUrl = (String) obj.get("requestUrl");
					String requestUrl = request.getParameter("requestUrl");
					resValue = processGetRequest(requestUrl, request);
//					resObj = (JSONObject) parser.parse(resValue);
					out.print(resValue);
					break;
					
				default:

				}
			}

			// response.setCharacterEncoding(charset);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getFileName(final Part part) {
	    final String partHeader = part.getHeader("content-disposition");
	    System.out.println( "Part Header = {0}" + partHeader);
	    for (String content : part.getHeader("content-disposition").split(";")) {
	        if (content.trim().startsWith("filename")) {
	            return content.substring(
	                    content.indexOf('=') + 1).trim().replace("\"", "");
	        }
	    }
	    return null;
	}

	private void processFileUpload(HttpServletRequest request, HttpServletResponse response) {
	    String path = "/tmp";
	    String authInfo = request.getHeader("Authorization");
	    
	    OutputStream out = null;
	    InputStream filecontent = null;
	    PrintWriter writer = null;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    try {
			final Part filePart = request.getPart("file");
		    final String fileName = getFileName(filePart);

		    out = new FileOutputStream(new File(path + File.separator
	                + fileName));
	        filecontent = filePart.getInputStream();

	        int read = 0;
	        final byte[] bytes = new byte[1024];

	        while ((read = filecontent.read(bytes)) != -1) {
	        	System.out.println("read : " + read);
	            out.write(bytes, 0, read);
	        }
	        writer.println("New file " + fileName + " created at " + path);
	        System.out.println("File{0}being uploaded to {1}" +
	                new Object[]{fileName, path});
	        
	        uploadFile(eSignBaseUrl + "/document", authInfo, path + File.separator + fileName);
	    } catch (FileNotFoundException fne) {
	        writer.println("You either did not specify a file to upload or are "
	                + "trying to upload a file to a protected or nonexistent "
	                + "location.");
	        writer.println("<br/> ERROR: " + fne.getMessage());

	        System.out.println( "Problems during file upload. Error: {0}" +
	                new Object[]{fne.getMessage()});
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        if (out != null) {
	            try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        if (filecontent != null) {
	            try {
					filecontent.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        if (writer != null) {
	            writer.close();
	        }
	    }
	}

	private void createUser(String data, HttpServletRequest request) {

		processPostPutRequest(HTTP_METHOD_POST, eSignBaseUrl + "/user", data, AUTHENTICATION_METHOD_BASIC, request);
	}

	/**
	 * For PUT request
	 * 
	 * @param connectionUrl
	 * @param query
	 * @param timeout
	 * @return String
	 */
	private String processPostPutRequest(String method, String connectionUrl, String query, String authenticationMethod, HttpServletRequest request) {
		String responseJSON = "";

		HttpURLConnection con = null;
		try {
			URL url = new URL(connectionUrl);
			con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Accept-Charset", charset);
			con.setRequestProperty("Content-Type", "application/json");
			if (authenticationMethod.equalsIgnoreCase(AUTHENTICATION_METHOD_BASIC)) {
				con.setRequestProperty("Authorization", "Basic " + encodedClientCredentials);
			} else {
				String authInfo = request.getHeader("Authorization");
				con.setRequestProperty("Authorization", authInfo);
			}

			con.setRequestMethod(method);
			con.setConnectTimeout(TIMEOUT);

			OutputStream out = null;
			try {
				out = con.getOutputStream();
				if (query != null) {
					out.write(query.getBytes(charset));
				}

				int status = con.getResponseCode();
				BufferedReader br;

				System.out.println("Response Code : " + status);

				if (status == 200) { // success
					br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				} else {
					br = new BufferedReader(new InputStreamReader((con.getErrorStream())));
					System.out.println("-----ERROR------");
				}

				responseJSON = readResponseAsJson(br);
				System.out.println(responseJSON);
			} finally {
				if (out != null) {
					out.close();
				}
			}

		} catch (IOException ex) {
			System.out.println("Error: " + ex);
		} finally {
			closeConnection(con);
		}
		return responseJSON;
	}

	private void closeConnection(HttpURLConnection con) {
		if (con != null) {
			try {
				con.disconnect();
			} catch (Exception ex) {
				System.out.println("Error: " + ex);
			}
		}
	}

	private String processGetRequest(String urlString, HttpServletRequest request) {
		String responseJSON = null;
		try {

			URL url = new URL(urlString);
			String authInfo = request.getHeader("Authorization");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", authInfo);

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			System.out.println("Output from Server .... \n");
			responseJSON = readResponseAsJson(br);

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
		return responseJSON;
	}

	/***
	 * createApplication
	 *
	 * @param appName
	 * @param answerUrl
	 * @param eventUrl
	 * @return String
	 * @throws IOException
	 *
	 ***/
	public String getOAuthToken(String userName, String password) throws IOException {

		if (userName == null)
			userName = "email.at.ssk@gmail.com";
		if (password == null)
			password = "esignPwd";

		String query = "username=" + userName + "&password=" + password + "&grant_type=password";
		System.out.println("query : " + query);
		String responseJSON = postRequest(eSignBaseUrl + "/oauth2/token", query, TIMEOUT);
		System.out.println("responseJSON : " + responseJSON);
		return responseJSON;
	}

	/**
	 * For POST request
	 * 
	 * @param connectionUrl
	 * @param query
	 * @param timeout
	 * @return String
	 */
	private String postRequest(String connectionUrl, String query, int timeout) {
		String responseJSON = "";
		HttpURLConnection con = null;
		try {
			URL url = new URL(connectionUrl);
			con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Accept-Charset", charset);
			con.setRequestProperty("Content-length", "0");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
			con.setRequestProperty("Authorization", "Basic " + encodedClientCredentials);

			con.setUseCaches(false);
			con.setAllowUserInteraction(false);
			con.setConnectTimeout(timeout);

			OutputStream out = null;
			try {
				out = con.getOutputStream();
				out.write(query.getBytes(charset));

				System.out.println(con.getResponseCode());

				int status = con.getResponseCode();
				BufferedReader br = null;

				System.out.println("Response Code : " + status);

				if (status == 200 || status == 201) { // success
					br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				} else {
					br = new BufferedReader(new InputStreamReader((con.getErrorStream())));
					System.out.println("-----ERROR------");
				}

				responseJSON = readResponseAsJson(br);
			} finally {
				if (out != null) {
					out.close();
				}
			}

		} catch (IOException ex) {
			System.out.println("Error: " + ex);
		} finally {
			closeConnection(con);
		}
		return responseJSON;
	}

	private void uploadDocument(String url, String authinfo, String file) {
		
	}
	
	private void uploadFile(String url, String authInfo, String file) {
		 CloseableHttpClient httpclient = HttpClients.createDefault();
	        try {
	            HttpPost httppost = new HttpPost(url);

	            FileBody bin = new FileBody(new File(file));
//	            StringBody comment = new StringBody("A binary file of some kind", ContentType.TEXT_PLAIN);

	            HttpEntity reqEntity = MultipartEntityBuilder.create()
	                    .addPart("file", bin)
//	                    .addPart("comment", comment)
	                    .build();

	            System.out.println("Auth Info : " + authInfo);

	            httppost.setEntity(reqEntity);
	            httppost.setHeader("Authorization", authInfo);

	            System.out.println("executing request " + httppost.getRequestLine());
	            CloseableHttpResponse response = httpclient.execute(httppost);
	            try {
	                System.out.println("----------------------------------------");
	                System.out.println(response.getStatusLine());
 	                HttpEntity resEntity = response.getEntity();
	                if (resEntity != null) {
	                    System.out.println("Response content length: " + resEntity.getContentLength());
	                }
	                EntityUtils.consume(resEntity);
	            } finally {
	                response.close();
	            }

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
	            try {
					httpclient.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	}
	
	private String readResponseAsJson(BufferedReader br) {

		String inputLine, responseJSON = null;
		StringBuffer response = new StringBuffer();

		if (br != null) {
			try {
				while ((inputLine = br.readLine()) != null) {
					response.append(inputLine);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			responseJSON = response.toString();
		}
		return responseJSON;
	}
}