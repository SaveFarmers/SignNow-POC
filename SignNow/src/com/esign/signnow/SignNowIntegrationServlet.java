package com.esign.signnow;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import org.springframework.util.FileCopyUtils;

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
	 * @see HttpServlet#doPut(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
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
			PrintWriter out = null;
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
					out = response.getWriter();
					out.print(resObj);

					break;

				case "FILE_UPLOAD":
					
					processFileUpload(request, response);
				    break;
				    
				case "GET_DOCUMENT":
					documentId = request.getParameter("documentId");
					resValue = processGetRequest(eSignBaseUrl + "/document/" + documentId, request);
					resObj = (JSONObject) parser.parse(resValue);
					out = response.getWriter();
					out.print(resObj);
					break;
					
				case "GET_ALL_DOCUMENTS":
					resValue = processGetRequest(eSignBaseUrl + "/user/documentsv2", request);
					JSONArray jsonArray = (JSONArray) parser.parse(resValue);
					out = response.getWriter();
					out.print(jsonArray);
					break;
					
				case "DELETE_DOCUMENT":
					documentId = request.getParameter("documentId");
					url = eSignBaseUrl + "/document/" + documentId;
					resValue = processPostPutRequest(HTTP_METHOD_DELETE, url, null, AUTHENTICATION_METHOD_OAUTH, request);
					resObj = (JSONObject) parser.parse(resValue);
					out = response.getWriter();
					out.print(resObj);
					break;
					
				case "UPDATE_DOCUMENT":
					documentId = request.getParameter("documentId");
					url = eSignBaseUrl + "/document/" + documentId;
					obj = (JSONObject) parser.parse(request.getReader());
					dataObj = (JSONObject) obj.get("document");
					resValue = processPostPutRequest(HTTP_METHOD_PUT, url, dataObj.toString(), AUTHENTICATION_METHOD_OAUTH, request);
					resObj = (JSONObject) parser.parse(resValue);
					out = response.getWriter();
					out.print(resObj);
					break;
					
				case "SEND_INVITE":
					obj = (JSONObject) parser.parse(request.getReader());
					documentId = (String) obj.get("documentId");
					JSONObject involvedParties = (JSONObject) obj.get("involvedParties");
					
					url = eSignBaseUrl + "/document/" + documentId + "/invite";

					resValue = processPostPutRequest(HTTP_METHOD_POST, url, involvedParties.toString(), AUTHENTICATION_METHOD_OAUTH, request);
					resObj = (JSONObject) parser.parse(resValue);
					out = response.getWriter();
					out.print(resObj);
					break;
					
					
				case "THUMBNAILS":
//					String requestUrl = request.getParameter("requestUrl");
					obj = (JSONObject) parser.parse(request.getReader());
					String requestUrl = (String) obj.get("requestUrl");
					processGetPNGRequest(requestUrl, request, response);
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
	    	final Part dataPart = request.getPart("data");
	    	String inputData = null;
	    	if (dataPart != null) {
	    		inputData = getValue(dataPart);
	    	}
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
//	        writer.println("New file " + fileName + " created at " + path);
	        System.out.println("File{0}being uploaded to {1}" +
	                new Object[]{fileName, path});
	        
	        if (uploadFile(eSignBaseUrl + "/document", authInfo, path + File.separator + fileName, inputData)) {
	        	JSONObject res = new JSONObject();
	        	res.put("status", "success");
	        	writer.println(res.toString());
	        };
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
	
	 /**
     * Returns the text value of the given part.
     */
    private String getValue(Part part) throws IOException {
        BufferedReader reader = 
            new BufferedReader(new InputStreamReader(part.getInputStream(), charset));
        StringBuilder value = new StringBuilder();
        char[] buffer = new char[10240];
        for (int length = 0; (length = reader.read(buffer)) > 0;) {
            value.append(buffer, 0, length);
        }
        return value.toString();
    }
    
/*	private void processMultiPart(HttpServletRequest request, HttpServletResponse response) {
		try {
			for (Part part : request.getParts()) {
			    String filename = getFilename(part);
			    if (filename == null) {
			        processTextPart(part);
			    } else if (!filename.isEmpty()) {
			        processFilePart(part, filename);
			    }
			}
		} catch (IOException | ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	*//**
     * Returns the filename from the content-disposition header of the given part.
     *//*
    private String getFilename(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    *//**
     * Returns the text value of the given part.
     *//*
    private String getValue(Part part) throws IOException {
        BufferedReader reader = 
            new BufferedReader(new InputStreamReader(part.getInputStream(), "UTF-8"));
        StringBuilder value = new StringBuilder();
        char[] buffer = new char[10240];
        for (int length = 0; (length = reader.read(buffer)) > 0;) {
            value.append(buffer, 0, length);
        }
        return value.toString();
    }

    *//**
     * Process given part as Text part.
     *//*
    private void processTextPart(Part part) throws IOException {
        String name = part.getName();
        String[] values = (String[]) super.get(name);

        if (values == null) {
            // Not in parameter map yet, so add as new value.
            put(name, new String[] { getValue(part) });
        } else {
            // Multiple field values, so add new value to existing array.
            int length = values.length;
            String[] newValues = new String[length + 1];
            System.arraycopy(values, 0, newValues, 0, length);
            newValues[length] = getValue(part);
            put(name, newValues);
        }
    }

    *//**
     * Process given part as File part which is to be saved in temp dir with the given filename.
     *//*
    private void processFilePart(Part part, String filename) throws IOException {
        // First fix stupid MSIE behaviour (it passes full client side path along filename).
        filename = filename
            .substring(filename.lastIndexOf('/') + 1)
            .substring(filename.lastIndexOf('\\') + 1);

        // Get filename prefix (actual name) and suffix (extension).
        String prefix = filename;
        String suffix = "";
        if (filename.contains(".")) {
            prefix = filename.substring(0, filename.lastIndexOf('.'));
            suffix = filename.substring(filename.lastIndexOf('.'));
        }

        // Write uploaded file.
        File file = File.createTempFile(prefix + "_", suffix, new File(location));
        if (multipartConfigured) {
            part.write(file.getName()); // Will be written to the very same File.
        } else {
            InputStream input = null;
            OutputStream output = null;
            try {
                input = new BufferedInputStream(part.getInputStream(), DEFAULT_BUFFER_SIZE);
                output = new BufferedOutputStream(new FileOutputStream(file), DEFAULT_BUFFER_SIZE);
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                for (int length = 0; ((length = input.read(buffer)) > 0);) {
                    output.write(buffer, 0, length);
                }
            } finally {
                if (output != null) try { output.close(); } catch (IOException logOrIgnore) {  }
                if (input != null) try { input.close(); } catch (IOException logOrIgnore) {  }
            }
        }

        put(part.getName(), file);
        part.delete(); // Cleanup temporary storage.
    }*/


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

	private void processGetPNGRequest(String urlString, HttpServletRequest request, HttpServletResponse response) {
		try {

			URL url = new URL(java.net.URLDecoder.decode(urlString, "UTF-8"));
			String authInfo = request.getHeader("Authorization");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "image/png");
			conn.setRequestProperty("Authorization", authInfo);

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedInputStream in = new BufferedInputStream(conn.getInputStream());

		    FileCopyUtils.copy(in, response.getOutputStream());
		    conn.disconnect();
		    response.flushBuffer();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
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
	
	private boolean uploadFile(String url, String authInfo, String file, String input) {
		 CloseableHttpClient httpclient = HttpClients.createDefault();
	        try {
	            HttpPost httppost = new HttpPost(url);

	            FileBody bin = new FileBody(new File(file));
//	            StringBody comment = new StringBody("A binary file of some kind", ContentType.TEXT_PLAIN);

	            HttpEntity reqEntity = null;
	            
	            if (input != null) {
	            	reqEntity = MultipartEntityBuilder.create()
		                    .addPart("file", bin)
		                    .addTextBody("data", input)
		                    .build();
	            } else {
	            	reqEntity = MultipartEntityBuilder.create()
		                    .addPart("file", bin)
		                    .build();
	            }

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
	                    return true;
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
	        return false;
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
			
			System.out.println(response);

			responseJSON = response.toString();
		}
		return responseJSON;
	}
}