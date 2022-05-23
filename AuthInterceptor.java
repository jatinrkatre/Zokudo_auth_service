package com.zokudo.sor.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.zokudo.sor.util.SecurityUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import com.zokudo.sor.exceptions.BizException;
import com.zokudo.sor.exceptions.ForbiddenException;
import com.zokudo.sor.exceptions.UnAuthorizedException;
import com.zokudo.sor.util.CommonUtil;
import com.zokudo.sor.util.UrlMetaData;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    private final UrlMetaData urlMetaData;
    
    private  Client client;
    private final String applicationLevelUserName;
    private final String applicationLevelUserPassword;
    private final SecurityUtil securityUtil;


    @Autowired
    public AuthInterceptor(@Qualifier(value = "client") Client client,
                           @Value("${applicationLevel.user.name}") String applicationLevelUserName,
                           @Value("${applicationLevel.user.password}") String applicationLevelUserPassword,
                           UrlMetaData urlMetaData, SecurityUtil securityUtil) {
        //this.urlMetaData = urlMetaData;
        this.client = client;
        this.applicationLevelUserName = applicationLevelUserName;
        this.applicationLevelUserPassword = applicationLevelUserPassword;
        this.urlMetaData = urlMetaData;
        this.securityUtil = securityUtil;
    }


    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {

    	log.info("** Call to pre handle");
        try {
            final MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap<>();
            String[] requesturl = CommonUtil.getProgramAndRequestUrl(request);
            headerMap.add("program_url", requesturl[2]);
            headerMap.add("request_url", requesturl[requesturl.length - 1]);
            headerMap.add("Authorization", request.getHeader("Authorization"));
            headerMap.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            Response clientResponse = client.target(urlMetaData.AUTHENTICATE_AND_AUTHORIZE_USER)
                    .request()
                    .headers(headerMap)
                    .get();

            if (clientResponse.getStatus() != 200) {
                String stringResponse = clientResponse.readEntity(String.class);
                JSONObject jsonResponse = new JSONObject(stringResponse);
                String errorMessage = jsonResponse.getString("message") != null ? jsonResponse.getString("message") : "";
                if (clientResponse.getStatus() == 401) throw new ForbiddenException(errorMessage);
                throw new UnAuthorizedException(errorMessage);
            }
        } catch (JSONException e) {
            log.error("Exception occurred", e);
            throw new BizException("An exception occurred while parsing!");
        }
        return true;
    }
}
