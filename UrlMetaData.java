
package com.zokudo.sor.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlMetaData {

    public final String AUTHENTICATE_AND_AUTHORIZE_USER;
    public final String GET_TRANSACTION_DETAILS_INCREMENTAL;
    public final String GET_CUSTOMER_CARD_SERVICE;
    public final String GET_CUSTOMERS_DETAILS_INCREMENTAL;


    private static final String dynamicUrl = "{}/api/";

    public UrlMetaData(@Value(value = "${version}") String version,
                       @Value(value = "${url.context.authcontext}") String AUTH_CONTEXT,
                       @Value("${url.context.productcontext}") String PRODUCT_CONTEXT,
                       @Value("${url.context.customercontext}") String CUSTOMER_CONTEXT,
                       @Value(value = "${url.context.card.service}") final String CARD_CONTEXT,
                       @Value(value = "${url.context.equitas.service}") final String EQUITAS_CONTEXT,
                       @Value(value = "${url.context.walletContext}") String WALLET_CONTEXT) {

        AUTH_CONTEXT = AUTH_CONTEXT + version;
        PRODUCT_CONTEXT = PRODUCT_CONTEXT + dynamicUrl + version;
        this.AUTHENTICATE_AND_AUTHORIZE_USER = AUTH_CONTEXT + "/authentication/authrequest";
        this.GET_TRANSACTION_DETAILS_INCREMENTAL = WALLET_CONTEXT + "mss/api/v1/sor/transaction";
        this.GET_CUSTOMER_CARD_SERVICE = CARD_CONTEXT + "mss/api/v1/sor/cards";
        this.GET_CUSTOMERS_DETAILS_INCREMENTAL =CUSTOMER_CONTEXT + "mss/api/v1/sor/customerDetails";

    }
}
