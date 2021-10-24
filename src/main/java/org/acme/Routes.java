package org.acme;

import org.acme.model.TelegramMessage;
import org.apache.camel.builder.RouteBuilder;

public class Routes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:foo?period=10000&delay=10000")
                .setBody().simple("{\"from\":\"timer\",  \"content\": \"Message automatique #${exchangeProperty.CamelTimerCounter}\"}")
                        .to("kafka:test");

        from("telegram:bots?authorizationToken={{telegram-token-api}}")
                .transform(simple("{\"from\":\"Telegram\",  \"content\": \"${body}\"}"))
                .to("kafka:test")
                        . transform(simple("Merci pour votre message 🐪."))
                        .to("telegram:bots?authorizationToken={{telegram-token-api}}");

        from("kafka:test")
                .log("Message entrant : ${body}")
                .choice()
                .when(simple("${body} contains 'Telegram'"))
                .unmarshal().json(TelegramMessage.class)
                .to("jpa:" + TelegramMessage.class.getName())
                .end();

        from("platform-http:/messages?httpMethodRestrict=GET")
                .to("jpa:" + TelegramMessage.class.getName() + "?namedQuery=findAll")
                .marshal().json();


    }
}
