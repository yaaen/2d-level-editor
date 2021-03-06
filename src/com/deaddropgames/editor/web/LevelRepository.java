package com.deaddropgames.editor.web;


import com.badlogic.gdx.net.HttpStatus;
import com.deaddropgames.editor.pickle.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class LevelRepository {

    private CloseableHttpClient httpclient;
    private String token;
    private StatusLine statusLine;

    //private static final String baseUrl = "http://localhost:8000";
    private static final String baseUrl = "https://deaddropgames.com";
    private static final String initTokenPath = "auth/token/";
    private static final String getLevelPath = "stuntski/api/editor/%d/";
    private static final String getLevelListPath = "stuntski/api/levels/";
    private static final String getMyLevelListPath = "stuntski/api/levels/mine/";
    private static final String postLevelPath = "stuntski/api/editor/save/";
    private static final String putLevelPath = "stuntski/api/editor/save/%d/";

    public LevelRepository() {

        httpclient = createHttpClient();
        token = null;
        statusLine = null;
    }

    /**
     * Creating this method so we can trust the SSL cert for CloseableHttpClient - apparently Java doesn't support
     * Let's Encrypt yet!
     * @return a CloseableHttpClient that will ignore SSL errors for CloseableHttpClient
     */
    private CloseableHttpClient createHttpClient() {

        // This is all we should need - when JDK starts trusting Let's Encrypt!!!
        // return HttpClients.createDefault();

        SSLContext sslContext;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {

                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

                    return true;
                }
            }).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {

            e.printStackTrace();
            return HttpClients.createDefault();
        }

        return HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
    }

    /**
     * Checks if the user is logged in
     * @return true if the token is initialized
     */
    public boolean hasToken() {

        return token != null;
    }

    /**
     * @return the HTTP status from the last call
     */
    public StatusLine getStatusLine() {

        return statusLine;
    }

    /**
     * Sends user credentials to repository to get REST authentication token
     * @param username username or email
     * @param password password
     * @return true if successful
     * @throws IOException unless we messed up our URIs above, this won't throw
     * @throws URISyntaxException unless we messed up our URIs above, this won't throw
     */
    public boolean initToken(final String username, final char[] password)
            throws IOException, URISyntaxException {

        HttpPost httpPost = new HttpPost(createUrl(initTokenPath).toURI());
        List<NameValuePair> nvps = new ArrayList<>();

        nvps.add(new BasicNameValuePair("username", username));
        nvps.add(new BasicNameValuePair("password", new String(password)));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));

        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {

            statusLine = response.getStatusLine();

            // only try to parse the response if it was successful
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                token = Utils.getJsonizer().fromJson(AuthToken.class, response.getEntity().getContent()).getValue();
            }
        }

        return token != null;
    }

    /**
     * Gets a list of levels
     * @param path the relative URI path or null to get the default (path is not null if a next/previous linke is used)
     * @param queryParams filtering params
     * @param mineOnly only shows the current user's levels
     * @return the level list result
     * @throws IOException unless we messed up our URIs above, this won't throw
     * @throws URISyntaxException unless we messed up our URIs above, this won't throw
     */
    public ApiBaseList getLevelList(String path, List<NameValuePair> queryParams, boolean mineOnly)
            throws IOException, URISyntaxException {

        // if path is null use the default, otherwise assume the full path is passed through
        URI uri;
        if (path == null) {

            URL url;
            if (mineOnly) {

                url = createUrl(getMyLevelListPath);
            } else {

                url = createUrl(getLevelListPath);
            }

            // add query params if they are specified - NOTE: API will return the query params for pagination!
            URIBuilder builder = new URIBuilder(url.toURI());
            if (queryParams != null && queryParams.size() > 0) {

                builder.addParameters(queryParams);
            }

            uri = builder.build();
        } else {

            // NOTE: API will return the query params for pagination - so we can ignore them in this case
            uri = new URL(path).toURI();
        }

        HttpGet httpGet = new HttpGet(uri);
        addDefaultRequestHeaders(httpGet);

        ApiBaseList levelList = null;
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {

            statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                levelList = Utils.getJsonizer().fromJson(ApiBaseList.class, response.getEntity().getContent());
            }
        }

        return levelList;
    }

    /**
     * Downloads a level from the website
     * @param id the id of the web resource
     * @return an editor level if successful
     * @throws IOException unless we messed up our URIs above, this won't throw
     * @throws URISyntaxException unless we messed up our URIs above, this won't throw
     */
    public Level getLevel(long id)
            throws IOException, URISyntaxException {

        HttpGet httpGet = new HttpGet(createUrl(String.format(getLevelPath, id)).toURI());
        addDefaultRequestHeaders(httpGet);

        Level level = null;
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {

            statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                level = Utils.getJsonizer().fromJson(Level.class, response.getEntity().getContent());
            }
        }

        return level;
    }

    /**
     * Create's a level by uploading it via POST
     * @param uploadLevel the level to upload
     * @return the ID of the created level
     * @throws IOException unless we messed up our URIs above, this won't throw
     * @throws URISyntaxException unless we messed up our URIs above, this won't throw
     */
    public long createLevel(final UploadLevel uploadLevel)
            throws IOException, URISyntaxException {

        HttpPost httpPost = new HttpPost(createUrl(postLevelPath).toURI());
        addDefaultRequestHeaders(httpPost);
        addUploadLevelToBody(uploadLevel, httpPost);

        long id = 0;
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {

            statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_CREATED) {

                id = Utils.getJsonizer().fromJson(ResourceId.class, response.getEntity().getContent()).getId();
            }
        }

        return id;
    }

    /**
     * Modifies an existing level by uploading via PUT
     * @param id the ID of the level
     * @param uploadLevel the level to upload
     * @return the ID of the level
     * @throws IOException if the returned ID is different from the input
     * @throws URISyntaxException unless we messed up our URIs above, this won't throw
     */
    public long modifyLevel(long id, final UploadLevel uploadLevel)
            throws IOException, URISyntaxException {

        HttpPut httpPut = new HttpPut(createUrl(String.format(putLevelPath, id)).toURI());
        addDefaultRequestHeaders(httpPut);
        addUploadLevelToBody(uploadLevel, httpPut);

        long retId = 0;
        try (CloseableHttpResponse response = httpclient.execute(httpPut)) {

            statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                retId = Utils.getJsonizer().fromJson(ResourceId.class, response.getEntity().getContent()).getId();

                if (retId != id) {

                    throw new IOException("Returned ");
                }
            }
        }

        return retId;
    }

    private URL createUrl(final String path) throws MalformedURLException {

        return new URL(new URL(baseUrl), path);
    }

    private void addDefaultRequestHeaders(HttpRequestBase request) {

        if (token != null) {

            request.addHeader(HttpHeaders.AUTHORIZATION, "Token " + token);
        }
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.addHeader(HttpHeaders.CONTENT_ENCODING, "UTF-8");
    }

    private void addUploadLevelToBody(final UploadLevel uploadLevel, HttpEntityEnclosingRequestBase request)
            throws UnsupportedEncodingException {

        String data = Utils.getJsonizer().toJson(uploadLevel);
        HttpEntity entity = new ByteArrayEntity(data.getBytes("UTF-8"));
        request.setEntity(entity);
    }
}
