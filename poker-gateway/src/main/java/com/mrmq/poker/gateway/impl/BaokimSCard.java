package com.mrmq.poker.gateway.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mrmq.poker.common.glossary.MsgCode;
import com.mrmq.poker.gateway.Gateway;
import com.mrmq.poker.gateway.bean.DepositResult;

/**
 *
 * @author HieuNguyen
 */
public class BaokimSCard implements Gateway {

	//Merchant cần cấu hình các tham số sau đây
    private final String BAOKIM_CARD_API = "https://www.baokim.vn/the-cao/restFul/send";
    private final String HTTP_USERNAME = "test_12537";
    private final String HTTP_PASSWORD = "9dj3mmarjms46n2n3jhdsdsadasq369";
    private final String API_USERNAME = "API_USERNAME";
    private final String API_PASSWORD = "API_PASSWORD";
    private final String SECURE_CODE = "SECURE_CODE";
    private final String MERCHANT_ID = "MERCHANT_ID";
    
    private final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private String supplier;
    private String seri;
    private String pincode;
    private String transaction_id;

    public MsgCode send(String supplier, String seri, String pincode) {
    	MsgCode result = MsgCode.UNKNOWN;
    	try {
	        this.supplier = supplier;
	        this.seri = seri;
	        this.pincode = pincode;
	        Date date = new Date();
	        this.transaction_id = String.valueOf(date.getTime());
	
	        Map<String, String> mapA = new HashMap<String, String>();
	        mapA.put("merchant_id", this.MERCHANT_ID);
	        mapA.put("api_username", this.API_USERNAME);
	        mapA.put("api_password", this.API_PASSWORD);
	        mapA.put("transaction_id", this.transaction_id);
	        mapA.put("card_id", this.supplier);
	        mapA.put("pin_field", this.pincode);
	        mapA.put("seri_field", this.seri);
	        mapA.put("algo_mode", "hmac");
	
			//Sort map by key
//			Map<String, String> arrayPost = new TreeMap<String, String>(mapA);
	        //Hash data post create data sign
	        String data_sign = hmacCreateDataSign(mapA);
	        mapA.put("data_sign", data_sign);
	        String data = this.createRequestUrl(mapA);
	        
	        //Post dữ liệu thẻ
			return this.doPost(data);
		} catch (IOException e) {
			result = MsgCode.BAD_GATEWAY;
			result.setMsg(e.getMessage());
		} catch (InvalidKeyException e) {
			result = MsgCode.BAD_GATEWAY;
			result.setMsg(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			result = MsgCode.BAD_GATEWAY;
			result.setMsg(e.getMessage());
		} catch (Exception e) {
			result = MsgCode.BAD_GATEWAY;
			result.setMsg(e.getMessage());
		}
    	
    	return result;
    }

    private MsgCode doPost(String data) throws IOException {
    	MsgCode result = MsgCode.UNKNOWN;
    	
        installAllTrustManager();
        System.setProperty("jsse.enableSNIExtension", "false");
        
		URL url = new URL(this.BAOKIM_CARD_API);
        HttpsURLConnection request = (HttpsURLConnection) url.openConnection();
        
        Authenticator.setDefault( new MyAuthenticator(this.HTTP_USERNAME,this.HTTP_PASSWORD) );
        request.setAllowUserInteraction(true);
        request.setConnectTimeout(3000);

        request.setUseCaches(false);
        request.setDoOutput(true);
        request.setDoInput(true);

        HttpsURLConnection.setFollowRedirects(true);
        request.setInstanceFollowRedirects(true);

        request.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        request.setRequestProperty("Content-length", String.valueOf(data.length()));
        request.setRequestMethod("POST");
        
        PrintWriter post = new PrintWriter(request.getOutputStream());
        post.print(data);
        post.close();
        
        InputStream is;
        String responseCode = String.valueOf(request.getResponseCode());
        if (responseCode == "200") {
        	result = MsgCode.SUCCESS;
            is = request.getInputStream();
        } else {
        	result = MsgCode.FAIL;
            is = request.getErrorStream();
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null)
        {
            stringBuilder.append(line + "\n");
        }
        
        String responseBody = stringBuilder.toString();
        result.setMsg(responseBody);
        
        return result;
    }

    /**
     * Raw String Data to generateHmacSHASignature
     *
     * @param params	
     * @return
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
    private String hmacCreateDataSign(Map<String, String> params) throws InvalidKeyException, NoSuchAlgorithmException {
        SortedSet<String> keys = new TreeSet<String>(params.keySet());
        String rawData = "";
        for (String key : keys) {
            String value = params.get(key);
            rawData = rawData + value;
        }

        return generateHmacSHASignature(rawData, this.SECURE_CODE);
    }

    /**
     * Create HmacSHAS1 Signature With Key
     *
     * @param data
     * @param key
     * @return
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
    private String generateHmacSHASignature(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        // get an hmac_sha1 key from the raw key bytes
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        // get an hmac_sha1 Mac instance and initialize with the signing key
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        // base64-encode the hmac
        return toHexString(mac.doFinal(data.getBytes()));
    }

    /**
     * toHexString
     *
     * @param bytes
     * @return
     */
    private String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();

        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        String result = formatter.toString();
        formatter.close();
        
        return result;
    }

    /**
     *
     * @param map
     * @return
     */
    private String createRequestUrl(Map<String, String> map) {
        String url_params = "";
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if(url_params == "")
                url_params += entry.getKey() + "=" +  entry.getValue();
            else
                url_params += "&" + entry.getKey() + "=" +  entry.getValue();
        }
        return url_params;
    }

    /**
     *
     *
     */
    public void installAllTrustManager() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection
                    .setDefaultHostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String urlHostname,
                                javax.net.ssl.SSLSession _session) {
                            return true;
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DepositResult deposit(String cardType, String cardSeri, String cardPin) {
    	DepositResult result = new DepositResult();
    	MsgCode msgCode = null;
    	try {
	        BaokimSCard bksc = new BaokimSCard();
	        msgCode = bksc.send(cardType, cardSeri, cardPin);
	        
	        String responseCode = msgCode.getCode();
			String responseBody = msgCode.getMsg();
			
			result.setMsgCode(msgCode);
			if(msgCode != MsgCode.BAD_GATEWAY){
				JSONParser parser = new JSONParser();
				Object obj;
				obj = parser.parse(responseBody);
			
				JSONObject jsonObject = (JSONObject) obj;
				result.setTransactionId(String.valueOf(jsonObject.get("transaction_id")));
				
				if(!responseCode.equals("200")){
					msgCode.setMsg(String.valueOf(jsonObject.get("errorMessage")));
				} else {
					result.setAmount(new BigDecimal(String.valueOf(jsonObject.get("amount"))));
				}
			}
    	} catch (Exception e) {
    		msgCode = MsgCode.BAD_GATEWAY;
    		msgCode.setMsg(e.getMessage());
    	}
    	
    	result.setMsgCode(msgCode);
    	return result;
    }
}

class MyAuthenticator extends Authenticator {
	private String http_username;
	private String http_password;
	public MyAuthenticator(String http_username, String http_password){
		this.http_username=http_username;
		this.http_password=http_password;
	}
	/**
	 * Called when password authorization is needed.
	 * 
	 * @return The PasswordAuthentication collected from the user, or null if
	 *         none is provided.
	 */
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.http_username,
				this.http_password.toCharArray());
	}
}
